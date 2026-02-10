package com.profiletailors.plugin.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextHandleTest {

  /**
   * ```
   * 测试空字符串输入
   * Expected: 返回空字符串
   * ```
   */
  @Test
  fun `wrapText with empty string should return empty string`() {
    val result = TextHandle.wrapText("")
    assertEquals("", result)
  }

  /**
   * ```
   * 测试单个单词输入
   * Expected: 返回原单词
   * ```
   */
  @Test
  fun `wrapText with single word should return the word`() {
    val result = TextHandle.wrapText("Hello", 10)
    assertEquals("Hello", result)
  }

  /**
   * ```
   * 测试多个短单词在行宽内的情况
   * Expected: 所有单词在同一行，用空格分隔
   * ```
   */
  @Test
  fun `wrapText with multiple short words within line width should keep them in one line`() {
    val result = TextHandle.wrapText("Hello world test", 20)
    assertEquals("Hello world test", result)
  }

  /**
   * ```
   * 测试单词长度超过行宽的情况
   * Expected: 长单词会被换行到新行
   * ```
   */
  @Test
  fun `wrapText with word longer than line width should wrap the word`() {
    val result = TextHandle.wrapText("Hello verylongword test", 10)
    val expected = "Hello\nverylongword\ntest"
    assertEquals(expected, result)
  }

  /**
   * ```
   * 测试正好等于行宽的情况
   * Expected: 正好填满一行，不需要换行
   * ```
   */
  @Test
  fun `wrapText with text exactly matching line width should not wrap`() {
    val result = TextHandle.wrapText("Hello world", 11) // "Hello world" is 11 characters
    assertEquals("Hello world", result)
  }

  /**
   * ```
   * 测试行宽为1的情况
   * Expected: 每个单词都换行
   * ```
   */
  @Test
  fun `wrapText with line width of 1 should wrap each word`() {
    val result = TextHandle.wrapText("A B C", 1)
    val expected = "A\nB\nC"
    assertEquals(expected, result)
  }

  /**
   * ```
   * 测试默认行宽(80)的情况
   * Expected: 使用默认行宽进行包装
   * ```
   */
  @Test
  fun `wrapText with default line width should use 80 characters`() {
    val longText = "A".repeat(79) + " B" // 79个A + 空格 + B = 81个字符
    val result = TextHandle.wrapText(longText)
    val expected = "${"A".repeat(79)}\nB"
    assertEquals(expected, result)
  }

  /**
   * ```
   * 测试多个连续空格的处理
   * Expected: 多个空格被规范化为单个空格
   * ```
   */
  @Test
  fun `wrapText should handle multiple spaces by normalizing them`() {
    val result = TextHandle.wrapText("Hello    world    test", 20)
    assertEquals("Hello world test", result)
  }

  /**
   * ```
   * 测试包含换行符的文本
   * Expected: 原始换行符被保留并规范化
   * ```
   */
  @Test
  fun `wrapText should handle text with newlines`() {
    val result = TextHandle.wrapText("Hello\n\nworld", 10)
    assertEquals("Hello\nworld", result)
  }

  /**
   * ```
   * 测试很长的单词需要被强制换行
   * Expected: 长单词独占一行
   * ```
   */
  @Test
  fun `wrapText with very long word should place it on separate line`() {
    val result = TextHandle.wrapText("short supercalifragilisticexpialidocious end", 15)
    val expected = "short\nsupercalifragilisticexpialidocious\nend"
    assertEquals(expected, result)
  }

  /**
   * ```
   * 测试最后一行的处理
   * Expected: 最后一行正确添加到结果中
   * ```
   */
  @Test
  fun `wrapText should properly handle the last line`() {
    val result = TextHandle.wrapText("one two three", 7)
    val expected = "one two\nthree"
    assertEquals(expected, result)
  }

  /**
   * ```
   * 测试只有空格的字符串
   * Expected: 返回空字符串
   * ```
   */
  @Test
  fun `wrapText with only spaces should return empty string`() {
    val result = TextHandle.wrapText("   ")
    assertEquals("", result)
  }

  /**
   * ```
   * 测试行宽刚好容纳单词加空格的情况
   * Expected: 正确换行
   * ```
   */
  @Test
  fun `wrapText with exact fit including space should wrap correctly`() {
    // "Hello"(5) + " "(1) + "world"(5) = 11 characters
    val result = TextHandle.wrapText("Hello world test", 11)
    val expected = "Hello world\ntest"
    assertEquals(expected, result)
  }
}
