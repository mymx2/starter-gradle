package io.github.mymx2.plugin.utils

import java.io.File
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/** Parameters for [CommandOutputValueSource]. */
interface CommandOutputParameters : ValueSourceParameters {
  var commandLine: List<String>
  var workingDir: String?
}

/**
 * Configuration-cache-safe [ValueSource] that captures stdout of an external command.
 *
 * Uses [ProcessBuilder] (pure JVM) instead of Gradle's `ExecOperations.exec` inside `obtain()`.
 * Both `providers.exec` and `ExecOperations.exec` are internally serialized as
 * `ProcessOutputValueSource` by the configuration cache, which fails on Windows with UAC error 740
 * when the target process cannot be started.
 *
 * ProcessBuilder bypasses Gradle's native process launcher (rubygrapefruit), so a start failure is
 * just a catchable `IOException` rather than a fatal CC error.
 *
 * **Note**: gradle/gradle#38399 deprecates `ProcessBuilder.start()` at configuration time only
 * *outside* `ValueSource` (see
 * [comment](https://github.com/gradle/gradle/issues/38464#issuecomment-4924508565)). Using
 * ProcessBuilder inside `obtain()` remains valid for the foreseeable future. Once Gradle provides a
 * graceful error path inside `ProcessOutputValueSource` (tracked in gradle/gradle#38464), this
 * workaround can be replaced with the official API.
 */
abstract class CommandOutputValueSource : ValueSource<String, CommandOutputParameters> {

  override fun obtain(): String {
    return try {
      val process =
        ProcessBuilder(parameters.commandLine)
          .apply { parameters.workingDir?.let { directory(File(it)) } }
          .redirectErrorStream(true)
          .start()
      val output = process.inputStream.bufferedReader().readText().trim()
      process.waitFor()
      output
    } catch (_: Exception) {
      // Gracefully handle process start failures (e.g. exe not on daemon PATH,
      // Windows UAC error 740). Callers already handle empty results.
      ""
    }
  }
}

/** exe finder */
object ExeFinder {

  /**
   * Find exe path via a configuration-cache-safe [ValueSource].
   *
   * Uses absolute path for system commands (where.exe / which) to avoid PATH resolution failures in
   * the Gradle daemon, especially under configuration cache mode where the daemon process may not
   * inherit the shell's PATH.
   *
   * @param providers Gradle providers
   * @param layout Gradle layout
   * @param name exe name
   * @return Provider of exe path (empty string if not found)
   */
  fun findExePath(
    providers: ProviderFactory,
    layout: ProjectLayout,
    name: String,
  ): Provider<String> {
    val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
    val execCommand =
      if (isWindows) listOf("C:\\Windows\\System32\\where.exe", name)
      else listOf("/usr/bin/which", name)
    return providers
      .of(CommandOutputValueSource::class.java) {
        parameters {
          commandLine = execCommand
          workingDir = layout.projectDirectory.asFile.absolutePath
        }
      }
      .map { it.ifBlank { "" } }
  }
}
