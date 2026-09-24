package io.github.mymx2.android

import com.github.takahirom.roborazzi.captureRoboImage
import io.github.mymx2.android.ui.DetailContent
import io.github.mymx2.android.ui.HomeContent
import io.github.mymx2.android.ui.theme.AppTheme
import org.junit.Test
import org.robolectric.annotation.GraphicsMode

/**
 * 样式回归截图测试（Roborazzi + Robolectric，test sourceSet）。 顶层 captureRoboImage 直接渲染 Compose（不带
 * activity，无系统窗口 debug 叠加）。 截 Content 层（纯 state，无 ViewModel/DataStore 依赖），与 Screen 层解耦。
 * recordRoborazziDevDebug 录基线到 src/test/screenshots（入库），verifyRoborazziDevDebug 校验，防止样式被改崩。 runner
 * 与 SDK 版本继承自 RobolectricTest 基类；@GraphicsMode(NATIVE) 为本类截图渲染专属。
 * 覆盖三帧：首页初始、首页深色、详情带计数——主题/排版/状态的三个真实回归面。 方法名保持 snake_case（Roborazzi 基线 PNG
 * 文件名依赖方法名，不可改），中文场景见各方法上方注释。
 */
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenScreenshotTest : RobolectricTest() {

  /** 首页初始态。 */
  @Test
  fun homeScreen_initial() {
    captureRoboImage {
      AppTheme {
        HomeContent(
          count = 0,
          rapidCount = false,
          onIncrement = {},
          onReset = {},
          onRapidCountChange = {},
          onNavigateToDetail = {},
        )
      }
    }
  }

  /** 首页深色模式。 */
  @Test
  fun homeScreen_darkTheme() {
    captureRoboImage {
      AppTheme(darkTheme = true) {
        HomeContent(
          count = 0,
          rapidCount = false,
          onIncrement = {},
          onReset = {},
          onRapidCountChange = {},
          onNavigateToDetail = {},
        )
      }
    }
  }

  /** 详情页带计数。 */
  @Test
  fun detailScreen_withCount() {
    captureRoboImage { AppTheme { DetailContent(count = 3, onBack = {}) } }
  }
}
