package io.github.mymx2.plugin.utils

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory

/**
 * 工具类：封装 Gradle reject 版本区间规则
 *
 * gt: greater than > x ge: greater or equal >= x lt: less than < x le: less or equal <= x
 */
object VersionRange {

  /** 拒绝所有 < version */
  fun lt(version: String): String = "(,$version)"

  /** 拒绝所有 <= version */
  fun le(version: String): String = "(,$version]"

  /** 拒绝所有 > version */
  fun gt(version: String): String = "($version,)"

  /** 拒绝所有 >= version */
  fun ge(version: String): String = "[$version,)"

  /** 自定义区间 */
  fun range(
    from: String,
    to: String,
    inclusiveFrom: Boolean = true,
    inclusiveTo: Boolean = false,
  ): String {
    val left = if (inclusiveFrom) "[" else "("
    val right = if (inclusiveTo) "]" else ")"
    return "$left$from,$to$right"
  }
}

object SemVerUtils {

  /**
   * return the short commit hash of the current Git commit. If not available, returns "unknown".
   */
  fun gitBuildMetadata(providers: ProviderFactory, layout: ProjectLayout): String {
    return runCatching {
        val output =
          providers
            .of(CommandOutputValueSource::class.java) {
              parameters {
                commandLine = listOf("git", "rev-parse", "HEAD")
                workingDir = layout.projectDirectory.asFile.absolutePath
              }
            }
            .get()
            .trim()
        output.ifBlank { "unknown" }
      }
      .getOrDefault("unknown")
  }
}
