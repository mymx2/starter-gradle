package io.github.mymx2.plugin.utils

import java.io.Console
import java.util.*
import kotlin.reflect.full.memberFunctions

/** ANSI控制台工具类 */
object Ansi {

  enum class Color(val code: String) {
    RESET("0"),
    BLACK("30"),
    RED("31"),
    GREEN("32"),
    YELLOW("33"),
    BLUE("34"),
    MAGENTA("35"),
    CYAN("36"),
    WHITE("37"),
    DEFAULT("39"),
    BACKGROUND_BLACK("40"),
    BACKGROUND_RED("41"),
    BACKGROUND_GREEN("42"),
    BACKGROUND_YELLOW("43"),
    BACKGROUND_BLUE("44"),
    BACKGROUND_MAGENTA("45"),
    BACKGROUND_CYAN("46"),
    BACKGROUND_WHITE("47"),
    BRIGHT_BLACK("90"),
    BRIGHT_RED("91"),
    BRIGHT_GREEN("92"),
    BRIGHT_YELLOW("93"),
    BRIGHT_BLUE("94"),
    BRIGHT_MAGENTA("95"),
    BRIGHT_CYAN("96"),
    BRIGHT_WHITE("97"),
  }

  /**
   * 彩色打印
   *
   * @param str 字符串
   * @param colorCode 颜色代码
   */
  fun color(str: String, colorCode: String? = "39"): String {
    val ansiCapable = detectIfAnsiCapable
    return if (ansiCapable) {
      "${colorCode!!.toAnsiCode()}$str${Color.RESET.code.toAnsiCode()}"
    } else str
  }

  fun colorBool(bool: Boolean, align: Boolean = true): String {
    val text =
      when (bool) {
        true -> color("true", Color.GREEN.code)
        false -> color("false", Color.RED.code)
      }
    return if (align) text.padStart(5) else text
  }

  private fun String.toAnsiCode() = "\u001B[${this}m"

  /** 检测当前终端是否支持ANSI */
  val detectIfAnsiCapable: Boolean by lazy {
    val console = System.console() ?: return@lazy false
    try {
      val operatingSystemName = System.getProperty("os.name").orEmpty().lowercase(Locale.ENGLISH)
      if (operatingSystemName.contains("win")) {
        return@lazy true
      }
      val isTerminalMethod = Console::class.memberFunctions.firstOrNull { it.name == "isTerminal" }
      if (isTerminalMethod != null) {
        val isTerminal = isTerminalMethod.call(console) as Boolean
        return@lazy isTerminal
      }
    } catch (_: Exception) {
      return@lazy false
    }
    return@lazy false
  }
}
