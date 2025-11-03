@file:Suppress("UnstableApiUsage", "detekt:MaxLineLength")

import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
  idea
  `kotlin-dsl` // https://plugins.gradle.org/plugin/org.gradle.kotlin.kotlin-dsl
  alias(libs.plugins.com.gradle.plugin.publish)
  alias(libs.plugins.com.vanniktech.maven.publish)
}

dependencies {
  // https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.kotlin.dsl/kotlin.html
  // org.jetbrains.kotlin:kotlin-gradle-plugin
  implementation(embeddedKotlin("gradle-plugin"))
  implementation(embeddedKotlin("reflect"))
  // https://kotlinlang.org/api/core/kotlin-test/
  // implementation(embeddedKotlin("test-junit5"))
  implementation(libs.semver)
  implementation(libs.errorproneGradlePlugin)
  implementation(libs.nullawayGradlePlugin)
  implementation(libs.develocityGradlePlugin)
  implementation(libs.spotlessGradlePlugin)
  implementation(libs.detektGradlePlugin)
  implementation(libs.dokkaGradlePlugin)
  implementation(libs.jvmDependencyConflictResolutionPlugin)
  implementation(libs.javaModuleDependenciesPlugin)
  implementation(libs.extraJavaModuleInfoPlugin)
  implementation(libs.shadowGradlePlugin)
  implementation(libs.gradleMavenPublishPlugin)
  implementation(libs.dependencyAnalysisGradlePlugin)
  implementation(libs.classpathCollisionDetectorPlugin)
  implementation(libs.cyclonedxGradlePlugin)
  implementation(libs.spotbugsGradlePlugin)
  implementation(libs.koverGradlePlugin)
  implementation(libs.kspPlugin)

  implementation(libs.lombokGradlePlugin)
  listOf(
      // https://kotlinlang.org/docs/kapt.html
      "org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin",
      // https://kotlinlang.org/docs/lombok.html
      "org.jetbrains.kotlin.plugin.lombok:org.jetbrains.kotlin.plugin.lombok.gradle.plugin",
      // https://kotlinlang.org/docs/sam-with-receiver-plugin.html
      "org.jetbrains.kotlin.plugin.sam.with.receiver:org.jetbrains.kotlin.plugin.sam.with.receiver.gradle.plugin",
      // https://kotlinlang.org/docs/all-open-plugin.html
      "org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin",
      // https://kotlinlang.org/docs/no-arg-plugin.html
      "org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin",
    )
    .forEach { implementation("${it}:${embeddedKotlinVersion}") }
  implementation(libs.openrewritePlugin)
  implementation(libs.springBootPlugin)
  implementation(libs.openapiGradlePlugin)
}

val isCI = EnvAccess.isCi(providers)

let {
  group = providers.gradleProperty("GROUP").get()
  val projectVersion = providers.gradleProperty("VERSION").get()
  version =
    if (isCI && projectVersion.count { it == '.' } == 2) {
      projectVersion.substringBeforeLast(".") +
        "." +
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) +
        "-SNAPSHOT"
    } else projectVersion
  description = "Zero-config Gradle plugin for building production-ready standalone JVM apps"
}

if (!isCI && providers.gradleProperty("IDEA_DOWNLOAD_SOURCES").orNull == "true") {
  idea {
    module {
      isDownloadSources = true
      isDownloadJavadoc = false
    }
  }
}

val javaLanguageVersion = libs.versions.jdk.get()

java {
  // https://docs.gradle.org/nightly/userguide/building_java_projects.html#sec:java_cross_compilation
  toolchain { languageVersion = JavaLanguageVersion.of(javaLanguageVersion) }
}

// isolated project cannot resolve kotlin dsl. https://github.com/gradle/gradle/issues/23795
kotlin { jvmToolchain { languageVersion = JavaLanguageVersion.of(javaLanguageVersion) } }

testing.suites.named<JvmTestSuite>("test") { targets.all { useJUnitJupiter() } }

tasks {
  validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
  }
  javadoc { isFailOnError = false }
}

publishing {
  repositories {
    maven {
      name = "tmp"
      url = uri(layout.settingsDirectory.dir("../../build/publishing/tmpRepo"))
    }
  }
  publications.configureEach {
    if (this is MavenPublication) {
      group = project.group
      artifactId = project.name
      version = project.version.toString()
      val projectName = project.group.toString() + ":" + project.name
      // the pom info
      pom {
        name = projectName
        description = project.description.orEmpty().ifBlank { projectName }
        url = "https://github.com/mymx2"
        scm { url = "https://github.com/mymx2" }
        licenses { license { url = "https://mit-license.org" } }
        developers { developer { name = "mymx2" } }
      }
    }
  }
}

mavenPublishing {
  publishToMavenCentral(automaticRelease = false)
  signAllPublications()
  configure(GradlePlugin(JavadocJar.None(), true))
}

val printPlugins =
  providers.gradleProperty("PRINT_GRADLE_PUBLISH_PLUGINS").orNull?.toBoolean() ?: false
val catalogLibs
  get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

gradlePlugin {
  website = "https://github.com/mymx2"
  vcsUrl = "https://github.com/mymx2"
  // Relying on Gradle script to generate plugins is slowing out the build:
  // 使用 gradle.kts 方式生成插件会很慢:
  // https://github.com/android/nowinandroid/issues/39
  // https://github.com/gradle/gradle/issues/15886
  plugins {
    val pluginAliasStart = "io.github.mymx2.plugin."
    catalogLibs.pluginAliases
      .filter { alias -> alias.startsWith(pluginAliasStart) }
      .map { Pair(it, catalogLibs.findPlugin(it).get().get()) }
      .forEach { pluginPair ->
        val (alias, plugin) = pluginPair
        val pluginId = plugin.pluginId
        val isPlugin = alias == pluginId
        if (!isPlugin) error("""plugin alias "$alias" is not equal to plugin id "$pluginId" """)
        val className =
          alias.replace(pluginAliasStart, "").replace("-", ".").split(".").joinToString("") {
            it.uppercaseFirstChar()
          }
        val pluginName = className.replaceFirstChar { it.lowercase() } + "Plugin"
        val pluginImplementationClass = pluginAliasStart + className + "Plugin"
        runCatching {
          register(pluginName) {
            displayName = pluginName
            description = "$pluginName gradle plugin, create by dy."
            tags.set(listOf("mymx2", pluginName, className))
            id = pluginId
            implementationClass = pluginImplementationClass
          }
        }
      }
  }
  if (printPlugins) println("|----------publish plugins----------")
  plugins.configureEach {
    if (printPlugins) println("| ${this.id}")
    if (displayName.isNullOrBlank()) displayName = this.name
    if (description.isNullOrBlank()) description = "${this.name} gradle plugin, create by dy."
    if (tags.orNull.isNullOrEmpty()) {
      tags.set(listOf("mymx2", implementationClass.substringAfterLast(".")))
    }
  }
  if (printPlugins) println("|----------publish plugins----------")
}

buildscript {
  configurations.classpath {
    val key = "CI"
    val buildCI =
      providers
        .environmentVariable(key)
        .orElse(providers.systemProperty(key))
        .orElse(providers.gradleProperty(key))
        .getOrNull()
        ?.toBoolean() ?: false
    resolutionStrategy {
      cacheDynamicVersionsFor(7, TimeUnit.DAYS)
      if (buildCI) {
        deactivateDependencyLocking()
      } else {
        activateDependencyLocking()
      }
    }
  }
}

configurations {
  configureEach { resolutionStrategy { cacheDynamicVersionsFor(7, TimeUnit.DAYS) } }
  runtimeClasspath {
    resolutionStrategy {
      activateDependencyLocking()
      if (isCI) {
        dependencyLocking { lockMode = LockMode.LENIENT }
      }
    }
  }
  compileClasspath { shouldResolveConsistentlyWith(runtimeClasspath.get()) }
}

object EnvAccess {

  /**
   * Returns true if the current build is running in a CI environment.
   *
   * @param providers The Gradle [ProviderFactory] instance.
   * @return True if the current build is running in a CI environment, false otherwise.
   */
  fun isCi(providers: ProviderFactory): Boolean {
    val key = "CI"
    val defaultValue = "false"
    val isCI =
      providers
        .environmentVariable(key)
        .orElse(providers.systemProperty(key))
        .orElse(providers.gradleProperty(key))
        .getOrNull() ?: defaultValue
    return isCI.toBoolean()
  }
}

interface ActionInjected {
  @get:Inject val execOps: ExecOperations
  @get:Inject val layout: ProjectLayout
}

tasks.register("writeLocks") {
  group = "toolbox"
  description = "Upgrade dependencies to latest versions"
  val injected = project.objects.newInstance<ActionInjected>()
  val workingDirProvider = provider { projectDir.parentFile.parentFile }
  doLast {
    val gradlew =
      if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        "${workingDirProvider.get().invariantSeparatorsPath}/gradlew.bat"
      } else {
        "${workingDirProvider.get().invariantSeparatorsPath}/gradlew"
      }
    val output = ByteArrayOutputStream()
    injected.execOps.exec {
      workingDir(workingDirProvider.get())
      commandLine(
        gradlew,
        // "--refresh-dependencies",
        ":build-logic:dependencies",
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
    if (!runtimeClasspath.isNullOrBlank()) {
      logger.lifecycle(runtimeClasspath)
      injected.layout.projectDirectory
        .file("gradle.lockfile.txt")
        .asFile
        .writeText(runtimeClasspath)
    } else {
      logger.lifecycle(outputString)
    }
  }
}

fun Project.resetTaskGroup(taskName: Any, distGroup: String) {
  runCatching {
    gradle.projectsEvaluated {
      tasks
        .named {
          when (taskName) {
            is String -> it == taskName
            is Regex -> it.matches(taskName)
            else -> false
          }
        }
        .configureEach {
          group = distGroup
          description = "$description [group = $distGroup]"
        }
    }
  }
}

val groups =
  mapOf(
    "build" to setOf("assemble", "build", "clean", "qualityGate"),
    "docs" to setOf("doc.*".toRegex()),
    "help" to
      setOf(
        "help",
        "projects",
        "properties",
        "tasks",
        "dependencies",
        "buildEnvironment",
        "kotlinDslAccessorsReport",
      ),
    "others" to setOf(".*".toRegex()),
    "publishing" to
      setOf(
        "publish",
        "publishAll.*".toRegex(),
        "publishTo.*".toRegex(),
        "publishPluginMaven.*".toRegex(),
      ),
    "toolbox" to setOf(".*".toRegex()),
    "verification" to setOf("check", "test.*".toRegex(), "qualityCheck"),
  )
val groupRegex = Regex(""" \[group = (.*)]""")

// Cleanup the task group by removing all tasks developers usually do not need to call
// directly
gradle.projectsEvaluated { tasks.configureEach { configureGroup(groupRegex, groups) } }

@Suppress("detekt:CyclomaticComplexMethod")
fun Task.configureGroup(groupRegex: Regex, groupMap: Map<String, Set<Any>>) {
  val printAll = false
  val printReset = false

  val taskClz = this::class.java.name

  fun printTaskGroup() {
    println("task group => ${group}\n  $name\n  $taskClz")
  }
  if (printAll) printTaskGroup()
  fun resetTaskGroup() {
    if (printReset) printTaskGroup()
    description = description.let { if (!it.isNullOrBlank()) "$it [from = $group]" else it }
    group = null
  }
  val reGroup = groupRegex.find(description.orEmpty())
  if (reGroup != null) {
    group = reGroup.groupValues[1]
  } else if (group != null) {
    if (!groupMap.keys.contains(group)) {
      resetTaskGroup()
    } else {
      if (
        groupMap[group!!]!!.none {
          when (it) {
            is String -> name == it
            is Regex -> name.matches(it)
            else -> false
          }
        }
      ) {
        resetTaskGroup()
      }
    }
  }
  if (!description.orEmpty().contains("[taskClz = ")) {
    description = "$description [taskClz = $taskClz]"
  }
}

listOf(
    "checkPomFileFor.*PluginMarker.*".toRegex() to "others",
    "generateMetadataFileFor.*PluginMarker.*".toRegex() to "others",
    "generatePomFileFor.*PluginMarker.*".toRegex() to "others",
    "sign.*PluginMarker.*".toRegex() to "others",
    "publish.*PluginMarker.*".toRegex() to "others",
    "run" to "build",
    "buildDependents" to "toolbox",
    "distZip" to "toolbox",
    "publishPlugins" to "publishing",
  )
  .forEach { resetTaskGroup(it.first, it.second) }
