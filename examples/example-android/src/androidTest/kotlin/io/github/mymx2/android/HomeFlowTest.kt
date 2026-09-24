package io.github.mymx2.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 真机 E2E（Instrumented）：只覆盖 Robolectric 测不了的系统级行为——真实窗口回栈调度、导航往返状态。 纯逻辑（组件交互/渲染映射）留在 test sourceSet
 * 的 HomeScreenTest，本层不重复。 依赖由 feature.android-test-e2e 统一引入；运行需设备（本地受管设备 pixel7Api29/pixel9Api36 或
 * CI 矩阵）。 方法名为英文标识符（JUnit4 无 @DisplayName，中文场景见类与方法注释）。
 */
@RunWith(AndroidJUnit4::class)
class HomeFlowTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  /** 刚进首页：count=0，首页 tab 选中，所有交互入口可见。 */
  @Test
  fun initialState_homeTabSelected_countZero_allActionsVisible() {
    // 首页是 backStack 唯一元素，tab 选中态派生自 backStack（单一事实源）
    composeTestRule.onNodeWithTag(HomeRoute::class.qualifiedName!!).assertIsDisplayed()
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
    composeTestRule.onNodeWithText("计数 +1").assertIsDisplayed()
    composeTestRule.onNodeWithText("重置").assertIsDisplayed()
    composeTestRule.onNodeWithText("查看详情").assertIsDisplayed()
    assertHomeTabSelected()
  }

  /** 进详情按系统返回，回到首页且计数保留。 */
  @Test
  fun systemBack_fromDetail_restoresHomeAndKeepsCount() {
    // Given：首页点一次计数（count=1）
    composeTestRule.onNodeWithText("计数 +1").performClick()
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
    // When：点"查看详情"进详情，再按系统返回键（真实 onBackPressed 调度，非 Compose 模拟）
    composeTestRule.onNodeWithText("查看详情").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(DetailRoute::class.qualifiedName!!).assertIsDisplayed()
    Espresso.pressBack()
    composeTestRule.waitForIdle()
    // Then：回首页，详情消失，count 仍 1——Nav3 backstack 真实出栈 + 共享 StateFlow 一致
    composeTestRule.onNodeWithTag(DetailRoute::class.qualifiedName!!).assertDoesNotExist()
    composeTestRule.onNodeWithTag(HomeRoute::class.qualifiedName!!).assertIsDisplayed()
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
  }

  /** 两次往返详情，第二次看到最新计数，证明共享流非快照。 */
  @Test
  fun secondVisitToDetail_showsLatestCount_provesSharedFlowNotSnapshot() {
    // 堵快照洞：若详情读的是"进入时路由快照"而非共享流，第二次进详情会看到旧值。
    // Given：首页 count=1，进详情
    composeTestRule.onNodeWithText("计数 +1").performClick()
    composeTestRule.onNodeWithText("查看详情").performClick()
    composeTestRule.waitForIdle()
    // 锁死"确实在详情页"——详情页独有副标题。否则首次进详情失败时，首页 count=1 也会让后续断言假绿。
    composeTestRule.onNodeWithTag(DetailRoute::class.qualifiedName!!).assertIsDisplayed()
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
    // When：返回首页，再 +1（count=2），重进详情
    Espresso.pressBack()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("计数 +1").performClick()
    composeTestRule.onNodeWithText("2").assertIsDisplayed()
    composeTestRule.onNodeWithText("查看详情").performClick()
    composeTestRule.waitForIdle()
    // Then：详情显示 2（最新共享值），不是第一次的 1——证明读的是实时 StateFlow 而非旧快照
    composeTestRule.onNodeWithTag(DetailRoute::class.qualifiedName!!).assertIsDisplayed()
    composeTestRule.onNodeWithText("2").assertIsDisplayed()
  }

  /** 进详情按系统返回，首页 tab 恢复选中。 */
  @Test
  fun systemBack_fromDetail_homeTabIsSelected() {
    // tab 选中态派生自 backStack（单一事实源）：系统返回后首页 tab 应恢复选中。
    // Given：进详情（详情 tab 选中）
    composeTestRule.onNodeWithText("查看详情").performClick()
    composeTestRule.waitForIdle()
    // When：系统返回
    Espresso.pressBack()
    composeTestRule.waitForIdle()
    // Then：首页 tab 选中——若选中态是独立状态（destination）未回写，此断言必红（reproduction test）
    composeTestRule.onNodeWithTag(HomeRoute::class.qualifiedName!!).assertIsDisplayed()
    assertHomeTabSelected()
  }

  /** 详情点返回按钮，首页 tab 恢复选中。 */
  @Test
  fun detailBackButton_homeTabIsSelected() {
    // 详情"返回"按钮（非系统返回键）也应让首页 tab 恢复选中（同一派生逻辑）
    composeTestRule.onNodeWithText("查看详情").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("返回").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(HomeRoute::class.qualifiedName!!).assertIsDisplayed()
    assertHomeTabSelected()
  }

  /** 断言"首页"tab 当前为选中态（NavigationSuite 的 selected 语义属性）。 */
  private fun assertHomeTabSelected() {
    composeTestRule.onNode(hasText("首页") and isSelected(), useUnmergedTree = true).assertExists()
  }
}
