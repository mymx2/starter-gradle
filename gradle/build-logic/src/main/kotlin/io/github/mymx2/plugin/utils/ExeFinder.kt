package io.github.mymx2.plugin.utils

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory

/** exe finder */
object ExeFinder {

  /**
   * Find exe path
   *
   * @param providers Gradle providers
   * @param layout Gradle layout
   * @param name exe name
   * @return Exe path
   */
  fun findExePath(providers: ProviderFactory, layout: ProjectLayout, name: String): String? {
    return runCatching {
        val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
        val execCommand = if (isWindows) listOf("where.exe", name) else listOf("which", name)
        val fullCommit =
          providers
            .exec {
              isIgnoreExitValue = true
              commandLine(execCommand)
              workingDir = layout.projectDirectory.asFile
            }
            .standardOutput
            .asText
            .get()
            .trim()
        fullCommit.ifBlank { null }
      }
      .getOrNull()
  }
}
