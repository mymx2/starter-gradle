package io.github.mymx2.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mymx2.android.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 应用共享状态容器（Home 与 Detail 共读，提升到 AppNavHost 层 Activity 作用域）：演示 ViewModel + StateFlow 单向数据流。 计数步长由
 * DataStore 的"倍速计数"开关驱动（读 Flow 转 StateFlow），演示 DataStore 与 UI 状态联动。 SettingsRepository
 * 经构造函数注入（接口化）——纯逻辑单测用 test double 即可构造，不依赖 Application。
 */
class MainViewModel(private val settings: SettingsRepository) : ViewModel() {

  /** 会话态：进程死即归零，不落 DataStore。持久化判据是"用户重启后是否期望它还在"——count 期望归零，rapidCount 期望保留。 */
  private val _count = MutableStateFlow(0)
  val count: StateFlow<Int> = _count.asStateFlow()

  /**
   * 倍速开关：DataStore 冷 Flow 经 stateIn 转热。WhileSubscribed(5000) 在停止订阅 5s 后才重启上游，覆盖"离开页面后快速返回"场景（配合
   * ViewModel 存活跨配置变更）。
   */
  val rapidCount: StateFlow<Boolean> =
    settings.rapidCount.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = false,
    )

  /**
   * 计数 +step。步长由调用方（UI 层）按当前 rapidCount 决定后传入—— 不在此读 rapidCount.value（WhileSubscribed 冷流在无订阅时是
   * initial 快照，即时判断不可靠）。
   *
   * 边界：步长是 UI 展示决策（按开关选 1/10），故外露给 UI。若步长是业务规则（如"VIP 用户步长 x2"）， 应在 VM 内 combine rapidCount
   * 流计算，不外露——本 demo 是 UI 决策，故传参即可。
   */
  fun increment(step: Int) {
    _count.update { it + step }
  }

  /** 归零：与 increment 同路径的可逆写回，演示 UDF 多事件写同一状态。 */
  fun reset() {
    _count.update { 0 }
  }

  fun setRapidCount(enabled: Boolean) {
    viewModelScope.launch { settings.setRapidCount(enabled) }
  }
}
