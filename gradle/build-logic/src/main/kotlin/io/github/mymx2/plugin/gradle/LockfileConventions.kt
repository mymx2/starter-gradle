@file:Suppress("UnstableApiUsage")

package io.github.mymx2.plugin.gradle

import io.github.mymx2.plugin.GradleExtTool
import io.github.mymx2.plugin.injected
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.io.path.invariantSeparatorsPathString
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.LockMode
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register

/**
 * Shared dependency-locking defaults used by both JVM and Android `check.dependencies` plugins.
 *
 * Mirrors https://docs.gradle.org/nightly/userguide/dependency_locking.html and
 * https://docs.gradle.org/nightly/userguide/dependency_caching.html#sec:controlling-dynamic-version-caching
 */
fun Project.configureDependencyLockingDefaults() {
  dependencyLocking {
    ignoredDependencies.add("com.example:*")
    lockMode = LockMode.LENIENT
  }
  configurations.configureEach {
    resolutionStrategy { cacheDynamicVersionsFor(7, TimeUnit.DAYS) }
  }
}

/**
 * Registers the shared `writeLocks` / `checkLocks` task pair used by both JVM and Android
 * `check.dependencies` plugins.
 */
fun Project.registerLockfileTasks() {
  val writeLocks: TaskProvider<Task> =
    tasks.register<Task>("writeLocks") {
      group = "toolbox"
      description = "Write dependencies to lockfile"
      val inject = injected
      val gradlewPath =
        GradleExtTool.findGradlew(rootDir.invariantSeparatorsPath)
          ?.invariantSeparatorsPathString
          .orEmpty()
      val gradlewPathProvider = objects.property<String>().value(gradlewPath)
      val projectPathProperty = objects.property<String>().value(project.path)
      val workingDirProvider = provider { rootDir }
      doFirst {
        listOf("buildscript-gradle.lockfile", "gradle.lockfile").forEach {
          inject.layout.projectDirectory.file(it).asFile.also { file ->
            if (file.exists()) {
              file.copyTo(
                inject.layout.projectDirectory.file("build/tmp/locks/${file.name}.bak").asFile,
                true,
              )
            }
          }
        }
      }
      doLast {
        val gradlew = gradlewPathProvider.get()
        if (gradlew.isBlank()) {
          return@doLast
        }
        val output = ByteArrayOutputStream()
        inject.exec.exec {
          workingDir(workingDirProvider.get())
          val execPath = projectPathProperty.get().let { if (it == ":") "" else it }
          // https://docs.gradle.org/nightly/userguide/command_line_interface.html#sec:command_line_execution_options
          commandLine(
            gradlew,
            // "--refresh-dependencies",
            "${execPath}:dependencies",
            "--write-locks",
          )
          standardOutput = output
        }
        val outputString = output.toString()
        val runtimeClasspath =
          Regex(
              """(^runtimeClasspath - Runtime classpath of.*\.[\s\S]*)(runtimeElements\s-\s)""",
              RegexOption.MULTILINE,
            )
            .find(outputString)
            ?.groupValues[1]
            ?.trim()
            ?.let { if (it.endsWith("\n")) it else "$it\n" }
        if (!runtimeClasspath.isNullOrBlank()) {
          inject.layout.projectDirectory
            .file("gradle.lockfile.txt")
            .asFile
            .writeText(
              runtimeClasspath.replace(System.lineSeparator(), "\n"),
              StandardCharsets.UTF_8,
            )
        }
      }
    }

  tasks.register<Task>("checkLocks") {
    group = "toolbox"
    description = "Check dependencies for lockfile"
    dependsOn(writeLocks)
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
            "$lockFile has been modified, please run './gradlew writeLocks' to update lockfile"
          )
        }
      }
    }
  }
}
