@file:Suppress("UnstableApiUsage")

package io.github.mymx2.plugin.spotless

import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.spotless.kotlin.KtfmtStep
import io.github.mymx2.plugin.GradleExtTool
import io.github.mymx2.plugin.ProjectVersions
import io.github.mymx2.plugin.gradle.computedExtension
import io.github.mymx2.plugin.gradle.lazySharedCache
import io.github.mymx2.plugin.versionFromCatalog
import java.io.File
import java.math.BigDecimal
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Spotless default format steps.
 *
 * It's similar to `.editorconfig`, you no longer need the
 * [editorconfig-gradle-plugin](https://github.com/ec4j/editorconfig-gradle-plugin). However, it
 * doesn't cover all files same as .editorconfig.
 */
fun FormatExtension.defaultStep(step: () -> Unit) {
  targetExcludeIfContentContains("spotless${":"}disable")
  toggleOffOn()
  step()
  leadingTabsToSpaces(2)
  // lineEndings = LineEnding.GIT_ATTRIBUTES_FAST_ALLSAME
  // encoding = StandardCharsets.UTF_8
  trimTrailingWhitespace()
  endWithNewline()
}

internal fun Project.nodeFile(
  path: String = ".gradle/nodejs",
  version: String = "",
): Provider<File> {
  return lazySharedCache<File>("nodeFile") {
    val nodeVersion =
      version.ifBlank { runCatching { versionFromCatalog("node") }.getOrNull().orEmpty() }

    isolated.rootProject.projectDirectory.dir(path).asFile.let nodeFile@{
      if (it.exists()) {
        val workDir =
          it.listFiles().firstOrNull { file -> file.name.startsWith("node-v${nodeVersion}") }
        if (workDir != null) {
          return@nodeFile workDir.resolve("node.exe")
        }
      }
      return@nodeFile it
    }
  }
}

internal fun Project.npmFile(nodeFile: Provider<File>): Provider<File> {
  return provider {
    val workDir = nodeFile.orNull?.parentFile ?: return@provider null
    //    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
    if (isWindows) {
      workDir.resolve("npm.cmd")
    } else {
      workDir.resolve("bin/npm")
    }
  }
}

/** Spotless configuration */
object SpotlessConfig {
  /**
   * Returns a [org.gradle.api.file.ConfigurableFileTree] for the given source directory (default:
   * "src").
   *
   * It excludes:
   * - Any directory starting with "__" (recursively, all its content excluded).
   * - Any file starting with "__" at any directory level.
   * - Any directory in the "nocheck" directory.
   * - Any directory in the "autogen" directory.
   * - Any directory in the "generated" directory.
   *
   * @param src Relative source directory path (defaults to "src").
   */
  fun Project.spotlessFileTree(src: String = "src") =
    fileTree(isolated.projectDirectory.dir(src)) {
      // default excludes.
      val defaultSpotlessExcludes =
        GradleExtTool.defaultGitIgnore + listOf("**/nocheck/**", "**/autogen/**", "**/generated/**")
      exclude(defaultSpotlessExcludes)
    }

  val ktfmtVersion = run {
    val ktfmtVersion = ProjectVersions.ktfmt.value.toBigDecimal()
    val defaultKtfmtVersion = KtfmtStep.defaultVersion()
    return@run if (ktfmtVersion >= BigDecimal(defaultKtfmtVersion)) {
      ktfmtVersion.toString()
    } else {
      defaultKtfmtVersion
    }
  }

  val prettierDevDependencies =
    mutableMapOf(ProjectVersions.prettier.key to ProjectVersions.prettier.value)

  val prettierDevDependenciesWithXmlPlugin =
    mutableMapOf(
      ProjectVersions.prettier.key to ProjectVersions.prettier.value,
      ProjectVersions.prettierXml.key to ProjectVersions.prettierXml.value,
    )

  /**
   * Returns the editorconfig properties of group `[*]` for the project. see
   * [support .editorconfig](https://github.com/diffplug/spotless/issues/734)
   *
   * @param project The project to get the editorconfig properties for.
   * @return A map of editorconfig properties.
   */
  @Suppress("unused")
  private fun getDotEditorconfig(project: Project): Map<String, String> {
    return project.computedExtension("spotlessDotEditorconfig") {
      try {
        val file = project.rootProject.file(".editorconfig")
        if (!file.exists()) return@computedExtension emptyMap()
        val map = mutableMapOf<String, String>()
        var inStarGroup = false

        file.forEachLine { line ->
          val trimmed = line.trim()
          if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine

          if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            @Suppress("AssignedValueIsNeverRead")
            inStarGroup = trimmed == "[*]"
            return@forEachLine
          }

          if (inStarGroup && "=" in trimmed) {
            val (key, value) = trimmed.split("=", limit = 2)
            map[key.trim()] = value.trim()
          }
        }
        map
      } catch (_: Exception) {
        emptyMap()
      }
    }
  }

  /**
   * Returns the .gitignore file as a list of strings. see
   * [support .gitignore](https://github.com/diffplug/spotless/issues/365)
   *
   * @param project The project to get the .gitignore file from.
   * @return The .gitignore file as a list of strings.
   */
  @Suppress("unused")
  private fun getDotGitignore(project: Project): List<String> {
    return project.computedExtension("spotlessDotGitignore") {
      try {
        val gitignoreFile = project.rootProject.file(".gitignore")
        if (!gitignoreFile.exists()) return@computedExtension emptyList()
        gitignoreFile
          .readLines()
          .filter { it.isNotBlank() && !it.startsWith("#") }
          .map { it.trim() }
      } catch (_: Exception) {
        emptyList()
      }
    }
  }
}
