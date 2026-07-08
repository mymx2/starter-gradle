package io.github.mymx2.plugin.utils

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory

/** exe finder */
object ExeFinder {

  /**
   * Find exe path
   *
   * Uses absolute path for system commands (where.exe / which) to avoid PATH resolution failures in
   * the Gradle daemon, especially under configuration cache mode where the daemon process may not
   * inherit the shell's PATH.
   *
   * @param providers Gradle providers
   * @param layout Gradle layout
   * @param name exe name
   * @return Exe path
   */
  fun findExePath(providers: ProviderFactory, layout: ProjectLayout, name: String): String? {
    return runCatching {
        val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
        val execCommand =
          if (isWindows) listOf("C:\\Windows\\System32\\where.exe", name)
          else listOf("/usr/bin/which", name)
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
