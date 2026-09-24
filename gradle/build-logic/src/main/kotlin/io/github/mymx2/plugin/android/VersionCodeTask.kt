package io.github.mymx2.plugin.android

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * 生成时间序 versionCode：yyMMdd(6位) + 当日刻钟序号(2位)。
 *
 * 把 versionCode 做成任务输出（而非配置期常量），让 Gradle 配置缓存只缓存 "versionCode 来自本任务输出"这一 Provider 关系，每次构建执行时重算新值——
 * 既实现时间序自动递增，又不被配置缓存冻结（LocalDateTime.now() 在配置期取会被冻结）。
 *
 * 15 分钟一刻钟，一天 96 个(0-95)，最大 99123195 < Int 上限 2147483647。 同一刻钟内重复构建共享 code（无碍，下一刻钟即递增）。
 */
@DisableCachingByDefault(because = "versionCode 由当前时间推导，每次执行输出都不同，缓存无意义")
abstract class VersionCodeTask : DefaultTask() {

  @get:OutputFile abstract val versionCodeFile: RegularFileProperty

  @TaskAction
  fun writeVersionCode() {
    val now = LocalDateTime.now()
    val datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd")).toInt()
    val quarterOfDay = (now.hour * 60 + now.minute) / 15
    val versionCode = datePart * 100 + quarterOfDay
    versionCodeFile.get().asFile.writeText(versionCode.toString())
  }
}
