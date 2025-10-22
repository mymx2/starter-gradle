package io.github.mymx2.plugin.utils

import io.github.mymx2.fixtures.consoleLog
import java.time.Duration
import org.junit.jupiter.api.Test

/** HttpUtil 单元测试类 */
class HttpUtilTest {

  @Test
  fun testBaidu() {
    val baidu =
      runCatching { HttpUtil.get("https://www.baidu.com", Duration.ofSeconds(10)) }
        .getOrDefault("获取失败") ?: ""

    consoleLog(baidu)
    assert(baidu.contains("百度一下"))
  }
}
