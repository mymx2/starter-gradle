package io.github.mymx2.android

import app.cash.turbine.test
import io.github.mymx2.android.data.SettingsRepository
import io.github.mymx2.android.ui.MainViewModel
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** SettingsRepository 的 test double：内存态替代 DataStore，验证 ViewModel 逻辑不依赖持久层。 */
private class FakeSettingsRepository(initialRapid: Boolean = false) : SettingsRepository {
  private val _rapidCount = MutableStateFlow(initialRapid)
  override val rapidCount: Flow<Boolean> = _rapidCount

  override suspend fun setRapidCount(enabled: Boolean) {
    _rapidCount.value = enabled
  }
}

/**
 * 纯逻辑层（JUnit5）：ViewModel 的状态演化与事件处理，不碰 Android/UI。 每条测试锚定一个用户语义（点击计数/开倍速/重置），断言状态变化而非实现细节。
 * 方法名为英文标识符，中文场景描述走 @DisplayName（JUnit5 规范，IDE/报告显示中文）。
 */
@DisplayName("MainViewModel 状态容器")
class MainViewModelTest {

  // viewModelScope.launch 跑 Main 调度器，runTest 是 Test 调度器；不替换 Main 时 launch 不执行，
  // setRapidCount 的写入断言挂（Linux CI 必挂，Windows 偶过——竞态窗口不同）。
  @OptIn(ExperimentalCoroutinesApi::class)
  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  @DisplayName("打开应用，初始计数为 0")
  fun initialCountIsZero() = runTest {
    // Given：ViewModel 刚创建，用户未操作
    // Then：count 从 0 开始
    assertEquals(0, MainViewModel(FakeSettingsRepository()).count.value)
  }

  @Test
  @DisplayName("连续点击计数，按传入步长累加")
  fun incrementAccumulatesByStep() = runTest {
    val vm = MainViewModel(FakeSettingsRepository())
    vm.count.test {
      // Given：初始 count=0
      assertEquals(0, awaitItem())
      // When：按 +1、再按 +10（步长由 UI 按倍速开关传入）
      vm.increment(1)
      // Then：变 1
      assertEquals(1, awaitItem())
      // When：再按 +10
      vm.increment(10)
      // Then：变 11（累加，非覆盖）——防"步长映射写反/写死"
      assertEquals(11, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  @DisplayName("大增后重置，归零且可重新计数")
  fun resetAfterLargeIncrementAllowsRecount() = runTest {
    val vm = MainViewModel(FakeSettingsRepository())
    vm.count.test {
      assertEquals(0, awaitItem())
      // When：大增（+10）后重置
      vm.increment(10)
      assertEquals(10, awaitItem())
      vm.reset()
      // Then：归 0（reset 与 increment 同路径的可逆写回）
      assertEquals(0, awaitItem())
      // When：重置后再 +1
      vm.increment(1)
      // Then：可重新计数——归零不是终态
      assertEquals(1, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  @DisplayName("开启倍速，写入经 viewModelScope 落到 Repository")
  fun setRapidCountWritesThroughToRepository() = runTest {
    val repository = FakeSettingsRepository()
    val vm = MainViewModel(repository)
    // When：用户开倍速（ViewModel 的写入路径，经 viewModelScope.launch 落到 Repository）
    vm.setRapidCount(true)
    // Then：Repository 收到 true（持久化证明见 SettingsRepositoryTest 的真实 DataStore 测试）
    repository.rapidCount.test {
      assertEquals(true, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }
}
