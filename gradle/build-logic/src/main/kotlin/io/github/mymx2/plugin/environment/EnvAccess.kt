@file:Suppress("UnstableApiUsage")

package io.github.mymx2.plugin.environment

import io.github.mymx2.plugin.gradle.eagerSharedCache
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.projectKey
import java.util.*
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginAware
import org.gradle.api.provider.ProviderFactory

object EnvAccess {

  /**
   * Returns true if the current build is running in a CI environment.
   *
   * @param providers The Gradle [ProviderFactory] instance.
   * @return True if the current build is running in a CI environment, false otherwise.
   */
  fun isCi(providers: ProviderFactory): Boolean {
    val ci = LocalConfig.Props.CI
    val key = ci.key
    val defaultValue = ci.defaultValue
    val isCI =
      providers
        .environmentVariable(key)
        .orElse(providers.systemProperty(key))
        .orElse(providers.gradleProperty(key))
        .getOrNull() ?: defaultValue
    return isCI.toBoolean()
  }
}

/**
 * Returns the merged project properties, layered from lowest to highest precedence:
 *
 * 1. root `build.properties` — shared public build metadata
 * 2. module `build.properties` — module public metadata (e.g. applicationId, version)
 * 3. root `local.properties` — shared machine-local secrets (never committed)
 * 4. module `local.properties` — module machine-local secrets (highest precedence)
 *
 * Later layers override earlier ones. Public build metadata (applicationId, SDK levels, version)
 * belongs in `build.properties` (committed); machine-local secrets (signing passwords, API keys)
 * belong in `local.properties` (gitignored) and never in build.
 *
 * @return The merged properties.
 */
fun PluginAware.buildProperties(): Properties {
  val key = this.projectKey("projectBuildProperties")
  return eagerSharedCache<Properties>(key) {
    val properties = Properties()

    // 用 isolated.projectDirectory.file() 拿 RegularFile，再 providers.fileContents 读，
    // 让 Gradle 把这些文件登记为配置缓存输入——改 build/local.properties 会让配置缓存失效。
    fun load(providers: ProviderFactory, file: org.gradle.api.file.RegularFile) {
      providers.fileContents(file).asText.orNull?.also { properties.load(it.reader()) }
    }

    when (this) {
      is Project -> {
        val rootDir = rootProject.isolated.projectDirectory
        val moduleDir = isolated.projectDirectory
        load(providers, rootDir.file("build.properties"))
        load(providers, moduleDir.file("build.properties"))
        load(providers, rootDir.file("local.properties"))
        load(providers, moduleDir.file("local.properties"))
      }
      is Settings -> {
        val settingsDir = layout.settingsDirectory
        load(providers, settingsDir.file("build.properties"))
        load(providers, settingsDir.file("local.properties"))
      }
      else -> error("Unknown PluginAware type ${this.javaClass.name}")
    }
    properties
  }
}
