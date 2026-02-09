@file:Suppress("UnstableApiUsage", "detekt:MaxLineLength")

import io.github.mymx2.plugin.resetTaskGroup
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
  `kotlin-dsl` // https://plugins.gradle.org/plugin/org.gradle.kotlin.kotlin-dsl
  alias(libs.plugins.com.gradle.plugin.publish)
  id("io.github.mymx2.module.kotlin")
  id("io.github.mymx2.feature.publish-vanniktech")
}

description = "Zero-config Gradle plugin for building production-ready standalone JVM apps"

dependencies {
  // implementation(embeddedKotlin("test-junit5"))
  implementation(embeddedKotlin("gradle-plugin"))
  implementation(embeddedKotlin("reflect"))
  implementation(libs.semver)
}

dependencies {
  listOf(
      // https://kotlinlang.org/docs/kapt.html
      "org.jetbrains.kotlin.kapt",
      // https://kotlinlang.org/docs/lombok.html
      "org.jetbrains.kotlin.plugin.lombok",
      // https://kotlinlang.org/docs/sam-with-receiver-plugin.html
      "org.jetbrains.kotlin.plugin.sam.with.receiver",
      // https://kotlinlang.org/docs/all-open-plugin.html
      "org.jetbrains.kotlin.plugin.spring",
      // https://kotlinlang.org/docs/no-arg-plugin.html
      "org.jetbrains.kotlin.plugin.jpa",
    )
    .forEach { implementation("${it}:${it}.gradle.plugin:${embeddedKotlinVersion}") }
  listOf(
      libs.plugins.net.ltgt.errorprone,
      libs.plugins.net.ltgt.nullaway,
      libs.plugins.com.gradle.develocity,
      libs.plugins.com.diffplug.spotless,
      libs.plugins.dev.detekt,
      libs.plugins.org.jetbrains.dokka,
      libs.plugins.me.champeau.jmh,
      libs.plugins.org.gradlex.jvm.dependency.conflict.resolution,
      libs.plugins.org.gradlex.java.module.dependencies,
      libs.plugins.org.gradlex.java.module.testing,
      libs.plugins.org.gradlex.extra.java.module.info,
      libs.plugins.com.gradleup.shadow,
      libs.plugins.com.vanniktech.maven.publish,
      libs.plugins.com.autonomousapps.dependency.analysis,
      libs.plugins.io.fuchs.gradle.classpath.collision.detector,
      libs.plugins.org.cyclonedx.bom,
      libs.plugins.com.github.spotbugs,
      libs.plugins.org.jetbrains.kotlinx.kover,
      libs.plugins.com.google.devtools.ksp,
      libs.plugins.io.freefair.lombok,
      libs.plugins.org.openrewrite.rewrite,
      libs.plugins.org.springframework.boot,
      libs.plugins.org.openapi.generator,
    )
    .forEach {
      val plugin = it.get()
      val pluginDependency = "${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}"
      implementation(pluginDependency)
    }
}

val projectGroup: String = providers.gradleProperty("GROUP").get()
val pomDeveloperName: String = providers.gradleProperty("POM_DEVELOPER_NAME").get()
val pomUrl: String = providers.gradleProperty("POM_URL").get()
val pomScmConnection: String = providers.gradleProperty("POM_SCM_CONNECTION").get()
val pomLicenseUrl: String = providers.gradleProperty("POM_LICENSE_URL").get()

val catalogLibs
  get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

gradlePlugin {
  website = pomUrl
  vcsUrl = pomScmConnection
  // Relying on Gradle script to generate plugins is slowing out the build:
  // 使用 gradle.kts 方式生成插件会很慢:
  // https://github.com/android/nowinandroid/issues/39
  // https://github.com/gradle/gradle/issues/15886
  plugins {
    val pluginAliasStart = "${projectGroup}.plugin."
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
            description = "$pluginName gradle plugin, create by ${pomDeveloperName}."
            tags.set(listOf(pomDeveloperName, pluginName, className))
            id = pluginId
            implementationClass = pluginImplementationClass
          }
        }
      }
  }
  val printPlugins = false
  if (printPlugins) println("|----------publish plugins----------")
  plugins.configureEach {
    if (printPlugins) println("| ${this.id}")
    if (displayName.isNullOrBlank()) displayName = this.name
    if (description.isNullOrBlank())
      description = "${this.name} gradle plugin, create by ${pomDeveloperName}."
    if (tags.orNull.isNullOrEmpty()) {
      tags.set(listOf(pomDeveloperName, implementationClass.substringAfterLast(".")))
    }
  }
  if (printPlugins) println("|----------publish plugins----------")
}

buildscript {
  configurations.classpath {
    resolutionStrategy {
      cacheDynamicVersionsFor(7, TimeUnit.DAYS)
      activateDependencyLocking()
    }
  }
}

listOf(".*PluginMarker.*".toRegex() to "others").forEach { resetTaskGroup(it.first, it.second) }

tasks {
  validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
  }
  javadoc { isFailOnError = false }
}

// 禁用 dokka
dokka { dokkaSourceSets { configureEach { suppress = true } } }

// detekt
detekt { config.setFrom(rootDir.toPath().parent.resolve("configs/detekt/detekt.yml").toFile()) }

// 禁用 spotlessKotlin
gradle.projectsEvaluated {
  tasks.named { it.startsWith("spotlessKotlin") }.configureEach { enabled = false }
}

// TODO remove it
configurations.configureEach { resolutionStrategy { force("org.projectlombok:lombok:1.18.42") } }

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
