package io.github.mymx2.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import io.github.mymx2.android.data.settingsDataStore
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * 组件交互层（Robolectric 跑在 test sourceSet，不起模拟器）。 真实 Activity + 真实 ViewModel + 真实 DataStore，验证 UI
 * 接线和状态联动。 runner 与 SDK 版本继承自 RobolectricTest 基类；每条测试是一段用户场景（Given-When-Then）。
 * 方法名为英文标识符，中文场景见各方法上方注释（JUnit4 无 @DisplayName，报告直接显示英文方法名）。
 */
class HomeScreenTest : RobolectricTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  /**
   * 每条测试前重置 rapidCount=false——Robolectric 同测试类共享 Application 与 DataStore 进程级单例， 前序测试（开启倍速/旋转）写入的
   * true 会以内存态残留在单例缓存（删文件清不掉内存）， 导致后续"初始应为计数 +1"的测试拿到 +10。经生产同一 settingsDataStore 单例写 false
   * 重置（复用单例不新建实例）。
   */
  @Before
  fun resetRapidCount() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    runBlocking {
      context.settingsDataStore.edit { it[booleanPreferencesKey("rapid_count")] = false }
    }
  }

  /** 打开应用，首页显示标题、初始计数 0、按钮为计数 +1、倍速默认关闭。 */
  @Test
  fun initialState_showsTitleZeroButtonAndRapidOff() {
    // Given：用户刚打开 App，第一眼
    // Then：标题、初始计数 0、主按钮"计数 +1"、倍速开关（默认关）都在——完整初始态
    composeTestRule.onNodeWithText("Material You").assertIsDisplayed()
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
    composeTestRule.onNodeWithText("计数 +1").assertIsDisplayed()
    composeTestRule.onNodeWithText("倍速计数").assertExists()
  }

  /** 点击计数按钮，数字从 0 变 1，旧值消失。 */
  @Test
  fun incrementClick_countGoesFromZeroToOne() {
    // Given：首页初始 count=0
    // When：点击"计数 +1"
    composeTestRule.onNodeWithText("计数 +1").performClick()
    // Then：变 1 且 0 消失——防双发（跳 2）与漏发（停 0）
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
    composeTestRule.onNodeWithText("0").assertDoesNotExist()
  }

  /** 点击重置按钮，数字归零。 */
  @Test
  fun resetClick_countReturnsToZero() {
    // Given：先点一次计数（count=1）
    composeTestRule.onNodeWithText("计数 +1").performClick()
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
    // When：点"重置"
    composeTestRule.onNodeWithText("重置").performClick()
    // Then：归 0 且 1 消失——验证 onReset 回调接线到 viewModel.reset
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
    composeTestRule.onNodeWithText("1").assertDoesNotExist()
  }

  /** 开启倍速，按钮变加 10，点击后数字跳 10。 */
  @Test
  fun rapidSwitchOn_buttonShowsPlusTenAndIncrementsByTen() {
    // 倍速联动（demo 核心卖点）：rapidCount → 步长映射的真实 UI 链路。
    // 这是之前最大的回归盲区——if (rapidCount) 10 else 1 写反/写死都能全绿。
    // Given：初始按钮"计数 +1"（倍速默认关）
    composeTestRule.onNodeWithText("计数 +1").assertIsDisplayed()
    // When：打开倍速开关（contentDescription 精确定位 Switch 本体，避免误点导航 tab）
    composeTestRule.onNodeWithContentDescription("倍速计数开关").performClick()
    // Then：按钮文案联动变"计数 +10"。DataStore 写入是异步的（viewModelScope.launch + Flow 回流），
    // waitForIdle 只等主线程，需 waitUntil 等 DataStore IO 完成、状态回流到 UI。
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      runCatching {
          composeTestRule.onNodeWithText("计数 +10").fetchSemanticsNode()
          true
        }
        .getOrDefault(false)
    }
    // When：点击计数按钮
    composeTestRule.onNodeWithText("计数 +10").performClick()
    // Then：count 跳 10（步长联动，非 +1）
    composeTestRule.onNodeWithText("10").assertIsDisplayed()
  }

  /** 旋转屏幕，计数保留，倍速开关状态保留。 */
  @Test
  fun recreate_retainsCountAndRapidState() {
    // 配置变更：WhileSubscribed(5000) 窗口覆盖 recreate，ViewModel 存活。
    // Given：点一次计数（count=1）+ 开倍速
    composeTestRule.onNodeWithText("计数 +1").performClick()
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("倍速计数开关").performClick()
    composeTestRule.waitForIdle()
    // When：旋转屏幕（Activity 重建）
    composeTestRule.activityRule.scenario.recreate()
    composeTestRule.waitForIdle()
    // Then：count 仍 1（ViewModel 存活），按钮仍"计数 +10"且 Switch 仍开（rapidCount 从 DataStore 恢复）
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
    composeTestRule.onNodeWithText("计数 +10").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("倍速计数开关").assertIsOn()
  }
}
