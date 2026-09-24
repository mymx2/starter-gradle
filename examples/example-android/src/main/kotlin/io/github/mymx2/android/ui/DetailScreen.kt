package io.github.mymx2.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import io.github.mymx2.android.ui.theme.AppTheme

/**
 * 详情入口：读共享 ViewModel 的 count（与首页同一实例，单一事实源，实时值）。 路由只标识目的地不携带数据——DetailRoute 无参数，count 从共享 StateFlow
 * 读。
 */
@Composable
fun DetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
  val count by viewModel.count.collectAsStateWithLifecycle()
  DetailContent(count = count, onBack = onBack)
}

/** 详情内容：无 ViewModel 依赖，纯 state + 事件回调，可 Preview 可截图测试（与 HomeContent 对称分层）。 */
@Composable
fun DetailContent(count: Int, onBack: () -> Unit) {
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
      Text(
        text = "详情",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = "从首页带来的计数",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(Modifier.height(32.dp))

      Text(
        text = "$count",
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.tertiary,
      )

      Spacer(Modifier.height(40.dp))
      FilledTonalButton(onClick = dropUnlessResumed { onBack() }) { Text("返回") }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DetailContentPreview() {
  AppTheme { DetailContent(count = 3, onBack = {}) }
}
