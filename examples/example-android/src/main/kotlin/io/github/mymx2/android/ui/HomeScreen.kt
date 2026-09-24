package io.github.mymx2.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mymx2.android.BuildConfig
import io.github.mymx2.android.ui.theme.AppTheme

/**
 * 首页入口：Screen 层保持薄——只 collect state + 分发事件，UI 下沉到 Content 层。 viewModel 由 AppNavHost 共享注入（Home 与
 * Detail 同一实例，单一事实源）。
 */
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToDetail: () -> Unit) {
  val count by viewModel.count.collectAsStateWithLifecycle()
  val rapidCount by viewModel.rapidCount.collectAsStateWithLifecycle()
  HomeContent(
    count = count,
    rapidCount = rapidCount,
    // 步长在此按最新 rapidCount 决定（UI 层持最新值），传入 ViewModel 而非让其读冷流快照
    onIncrement = { viewModel.increment(if (rapidCount) 10 else 1) },
    onReset = viewModel::reset,
    onRapidCountChange = viewModel::setRapidCount,
    onNavigateToDetail = onNavigateToDetail,
  )
}

/** 首页内容：无 ViewModel 依赖，纯 state + 事件回调，可 Preview 可单测。 */
@Composable
fun HomeContent(
  count: Int,
  rapidCount: Boolean,
  onIncrement: () -> Unit,
  onReset: () -> Unit,
  onRapidCountChange: (Boolean) -> Unit,
  onNavigateToDetail: () -> Unit,
) {
  Scaffold { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(innerPadding)
          .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HomeBrandHeader()

      Spacer(Modifier.height(40.dp))

      // 主角：count 是全屏唯一 display 级元素，视觉焦点。
      Text(
        text = "$count",
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
      )
      Spacer(Modifier.height(16.dp))
      // 按钮层级即语义权重：主操作 Filled（计数），破坏性低频 TextButton（重置，紧贴其作用对象）。
      Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = onIncrement) { Text(if (rapidCount) "计数 +10" else "计数 +1") }
        Spacer(Modifier.padding(horizontal = 4.dp))
        TextButton(onClick = onReset) { Text("重置") }
      }

      // DataStore 驱动：倍速开关状态持久化，重启保留
      Spacer(Modifier.height(12.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "倍速计数",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.padding(horizontal = 8.dp))
        Switch(
          checked = rapidCount,
          onCheckedChange = onRapidCountChange,
          modifier = Modifier.semantics { contentDescription = "倍速计数开关" },
        )
      }

      Spacer(Modifier.height(12.dp))
      FilledTonalButton(onClick = { onNavigateToDetail() }) {
        Text("查看详情")
      }
    }
  }
}

/** 品牌区：装饰星标 + 标题 + 环境信息，与计数逻辑无关。 */
@Composable
private fun HomeBrandHeader() {
  Surface(
    shape = MaterialTheme.shapes.extraLarge,
    color = MaterialTheme.colorScheme.surfaceVariant,
    tonalElevation = 2.dp,
  ) {
    Text(
      text = "✦",
      modifier = Modifier.padding(16.dp),
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }

  Spacer(Modifier.height(32.dp))

  Text(
    text = "Material You",
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onBackground,
  )
  Text(
    // 仅演示 BuildConfig 读取（环境变量经 flavor 注入）——生产不应把后端 URL 直接显示在 UI 上。
    text = "${BuildConfig.ENV_NAME} · ${BuildConfig.BASE_URL}",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
  )
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
  AppTheme {
    HomeContent(
      count = 3,
      rapidCount = true,
      onIncrement = {},
      onReset = {},
      onRapidCountChange = {},
      onNavigateToDetail = {},
    )
  }
}
