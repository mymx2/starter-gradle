@file:Suppress("UnstableApiUsage")

import com.autonomousapps.DependencyAnalysisExtension
import com.autonomousapps.DependencyAnalysisSubExtension
import com.autonomousapps.tasks.ProjectHealthTask
import io.fuchs.gradle.collisiondetector.DetectCollisionsTask
import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.injected
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import io.github.mymx2.plugin.resetTaskGroup
import java.io.ByteArrayOutputStream
import org.gradlex.javamodule.dependencies.tasks.ModuleDirectivesOrderingCheck
import org.gradlex.javamodule.dependencies.tasks.ModuleDirectivesScopeCheck

plugins {
  java
  id("io.fuchs.gradle.classpath-collision-detector")
  id("com.autonomousapps.dependency-analysis")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.base.jpms-modules")
}

// ordering check is done by SortModuleInfoRequiresStep
tasks.withType<ModuleDirectivesOrderingCheck> { enabled = false }

// Do not report dependencies from one source set to another as 'required'.
// In particular, in case of test fixtures, the analysis would suggest to
// add as testModuleInfo { require(...) } to the main module. This is
// conceptually wrong, because in whitebox testing the 'main' and 'test'
// module are conceptually considered one module (main module extended with tests)
// kotlin-kapt: https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/778
if (project.parent == null) {
  configure<DependencyAnalysisExtension> {
    issues {
      all {
        onAny { // Configure the dependency analysis plugin to fail if issues are found
          severity("fail")
          exclude("kotlin-kapt")
          onUnusedDependencies { exclude("org.junit.jupiter:junit-jupiter") }
        }
      }
    }
  }
} else {
  configure<DependencyAnalysisSubExtension> {
    issues {
      onAny { // Configure the dependency analysis plugin to fail if issues are found
        severity("fail")
        exclude("kotlin-kapt")
        onUnusedDependencies { exclude("org.junit.jupiter:junit-jupiter") }
      }
    }
  }
}

tasks.named("qualityCheck") {
  dependsOn(tasks.withType<DetectCollisionsTask>())
  dependsOn(tasks.withType<ProjectHealthTask>())
  dependsOn(tasks.withType<ModuleDirectivesScopeCheck>())
}

tasks.named("qualityGate") {
  dependsOn(tasks.withType<DetectCollisionsTask>())
  dependsOn(tasks.withType<ProjectHealthTask>())
  dependsOn(tasks.withType<ModuleDirectivesScopeCheck>())
}

listOf("artifactsReportMain" to "help", "fixDependencies" to "toolbox").forEach {
  resetTaskGroup(it.first, it.second)
}

val isCI = EnvAccess.isCi(providers)
val isDebug = project.getPropOrDefault(LocalConfig.Props.IS_DEBUG).toBoolean()

if (isCI || isDebug) {
  // https://docs.gradle.org/nightly/userguide/dependency_locking.html
  dependencyLocking {
    ignoredDependencies.add("com.example:*")
    lockMode = LockMode.LENIENT
  }
}

configurations {
  configureEach {
    resolutionStrategy {
      // https://docs.gradle.org/nightly/userguide/dependency_caching.html#sec:controlling-dynamic-version-caching
      cacheDynamicVersionsFor(7, TimeUnit.DAYS)
    }
  }
  runtimeClasspath { resolutionStrategy { activateDependencyLocking() } }
  compileClasspath { shouldResolveConsistentlyWith(runtimeClasspath.get()) }
}

tasks.register("writeLocks") {
  group = "toolbox"
  description = "Write dependencies to lockfile"
  val inject = injected
  val projectPathProperty = objects.property<String>().value(project.path)
  val workingDirProvider = provider { rootDir }
  doFirst {
    inject.layout.projectDirectory.file("gradle.lockfile").asFile.also {
      if (it.exists()) {
        it.copyTo(
          inject.layout.projectDirectory.file("build/tmp/locks/gradle.lockfile.bak").asFile,
          true,
        )
      }
    }
  }
  doLast {
    // https://stackoverflow.com/a/45013467/30654190
    val gradlew =
      if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        "${workingDirProvider.get().invariantSeparatorsPath}/gradlew.bat"
      } else {
        "${workingDirProvider.get().invariantSeparatorsPath}/gradlew"
      }
    val output = ByteArrayOutputStream()
    inject.exec.exec {
      workingDir(workingDirProvider.get())
      // https://docs.gradle.org/nightly/userguide/command_line_interface.html#sec:command_line_execution_options
      commandLine(
        gradlew,
        // "--refresh-dependencies",
        "${projectPathProperty.get()}:dependencies",
        "--write-locks",
      )
      standardOutput = output
    }
    val outputString = output.toString()
    // https://github.com/gradle/gradle/issues/19900
    if (!org.gradle.internal.os.OperatingSystem.current().isUnix) {
      val lockFile = inject.layout.projectDirectory.file("gradle.lockfile").asFile
      lockFile.writeText(lockFile.readText().replace(System.lineSeparator(), "\n"))
    }
    val runtimeClasspath =
      Regex(
          """(^runtimeClasspath - Runtime classpath of.*\.[\s\S]*)(runtimeElements\s-\s)""",
          RegexOption.MULTILINE,
        )
        .find(outputString)
        ?.groupValues[1]
        ?.trim()
    if (!runtimeClasspath.isNullOrBlank()) {
      inject.layout.projectDirectory
        .file("gradle.lockfile.txt")
        .asFile
        .writeText(runtimeClasspath.replace(System.lineSeparator(), "\n"))
    }
  }
}

tasks.register("checkLocks") {
  group = "toolbox"
  description = "Check dependencies for lockfile"
  dependsOn(tasks.named("writeLocks"))
  val inject = injected
  doLast {
    val bakLockContent =
      inject.layout.projectDirectory.file("build/tmp/locks/gradle.lockfile.bak").asFile.let {
        if (it.exists()) it.readText() else null
      }
    if (bakLockContent != null) {
      val lockFile =
        inject.layout.projectDirectory.file("gradle.lockfile").asFile.takeIf { it.exists() }
      val lockContent = lockFile?.readText()
      if (lockFile != null && bakLockContent != lockContent) {
        throw GradleException(
          "$lockFile has been modified, please run 'writeLocks' to update lockfile"
        )
      }
    }
  }
}
