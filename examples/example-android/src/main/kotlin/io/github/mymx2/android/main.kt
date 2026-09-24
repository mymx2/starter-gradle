package io.github.mymx2.android

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.mymx2.android.data.DataStoreSettingsRepository
import io.github.mymx2.android.ui.DetailScreen
import io.github.mymx2.android.ui.HomeScreen
import io.github.mymx2.android.ui.MainViewModel
import io.github.mymx2.android.ui.theme.AppTheme
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute : NavKey

@Serializable data object DetailRoute : NavKey

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { AppTheme { AppNavHost() } }
  }
}

/**
 * 自适应导航宿主：NavigationSuiteScaffold 按窗口尺寸自动切导航形态—— 手机（compact）底部
 * NavigationBar，平板/桌面（medium/expanded）侧边 NavigationRail。 Nav3 的 NavDisplay 挂在其内容区，线性进栈（home→detail
 * 按钮）与 tab 直达（自适应骨架）并存。
 */
@Composable
fun AppNavHost() {
  val backStack = rememberNavBackStack(HomeRoute)

  // 共享 ViewModel：提升到 NavHost 层（Activity 作用域），Home 与 Detail 读同一实例的 StateFlow，
  // 单一事实源——count 全局一致，路由只标识目的地不携带数据（DetailRoute 无参数）。
  val app = LocalContext.current.applicationContext as Application
  val viewModel: MainViewModel =
    viewModel(
      factory =
        remember(app) {
          viewModelFactory { initializer { MainViewModel(DataStoreSettingsRepository(app)) } }
        }
    )

  // compact → 底部导航栏；其余 → 侧边导航栏（官方默认计算，无需手动判断 windowSizeClass）
  val layoutType =
    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfoV2())

  // tab 选中态派生自 backStack（单一事实源），不再用独立 destination 状态——
  // 独立状态需手动与 backStack 同步，系统返回/返回按钮路径会漏，造成"高亮详情但人在首页"漂移。
  val onDetail = backStack.lastOrNull() is DetailRoute

  NavigationSuiteScaffold(
    layoutType = layoutType,
    navigationSuiteItems = {
      item(
        icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
        label = { Text("首页") },
        selected = !onDetail,
        onClick = { while (backStack.size > 1) backStack.removeLastOrNull() },
      )
      item(
        icon = { Icon(Icons.Default.Info, contentDescription = "详情") },
        label = { Text("详情") },
        selected = onDetail,
        onClick = { if (!onDetail) backStack.add(DetailRoute) },
      )
    },
  ) {
    NavDisplay(
      backStack = backStack,
      // 当前路由标识：E2E 据此判定"当前路由目标"。testTag 用 NavKey 全限定名（编译期确定、全局唯一、
      // 改类名/包名时此处与测试一起编译失败，漂移即暴露而非静默错），多包同名 NavKey 也不撞。
      modifier =
        Modifier.testTag(
          backStack.lastOrNull()?.let { it::class.qualifiedName }
            ?: HomeRoute::class.qualifiedName!!
        ),
      onBack = { backStack.removeLastOrNull() },
      entryProvider =
        entryProvider {
          entry<HomeRoute> {
            HomeScreen(
              viewModel = viewModel,
              onNavigateToDetail = { backStack.add(DetailRoute) },
            )
          }
          entry<DetailRoute> {
            DetailScreen(viewModel = viewModel, onBack = { backStack.removeLastOrNull() })
          }
        },
    )
  }
}
