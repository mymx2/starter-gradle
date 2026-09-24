package io.github.mymx2.android

import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.github.mymx2.android.data.DataStoreSettingsRepository
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * 持久化层（Robolectric + 真实 DataStore）：验证 DataStoreSettingsRepository 的真实读写与跨实例持久化。 与 MainViewModelTest
 * 的 FakeRepository 区分——Fake 只验证接口契约，这里验证真实 DataStore 的 key/默认值/edit 事务。 验证 rapidCount 的真实持久化（之前只有
 * FakeRepository 测接口契约，真实 DataStore 无保障）。 方法名为英文标识符，中文场景见各方法上方注释（JUnit4/vintage 下报告直接显示英文方法名）。
 */
class SettingsRepositoryTest : RobolectricTest() {

  private val context
    get() = ApplicationProvider.getApplicationContext<android.content.Context>()

  /**
   * 每条测试前重置 rapidCount=false——同测试类共享 Application 与 DataStore 进程级单例，
   * "写入true"会残留到后续"默认未写入"测试（顺序耦合）。经生产同一 settingsDataStore 单例写 false，让隔离显式化。
   */
  @Before
  fun resetDataStore() {
    runBlocking { DataStoreSettingsRepository(context).setRapidCount(false) }
  }

  /** 默认未写入，倍速开关为 false。 */
  @Test
  fun defaultRapidCountIsFalse() = runTest {
    // Given：全新 DataStore（无任何写入）
    val repository = DataStoreSettingsRepository(context)
    // Then：rapidCount 默认 false——验证默认值兜底逻辑（it[key] ?: false）
    assertEquals(false, repository.rapidCount.first())
  }

  /** 写入 true 后，新建实例读取仍为 true，证明持久化。 */
  @Test
  fun rapidCountPersistsAcrossRepositoryRecreation() = runTest {
    // Given：第一个实例写入 rapidCount=true（经 edit 事务落盘）
    DataStoreSettingsRepository(context).setRapidCount(true)
    // When：新建一个 Repository 实例（模拟进程重启后的重新读取）
    val repositoryAfterRecreation = DataStoreSettingsRepository(context)
    // Then：读到持久化的 true——证明 edit 事务真的落盘、key 名正确、跨实例（重启）可恢复
    assertEquals(true, repositoryAfterRecreation.rapidCount.first())
  }

  /** 写入后 Flow 能收到最新值。 */
  @Test
  fun flowReceivesLatestValueAfterWrite() = runTest {
    val repository = DataStoreSettingsRepository(context)
    repository.rapidCount.test {
      // Given：@Before 已重置为 false，Flow 订阅先收到 false
      assertEquals(false, awaitItem())
      // When：写入 true（状态翻转，Flow 才会发射新值——写相同值 DataStore 不重复发射）
      repository.setRapidCount(true)
      // Then：Flow 收到新的 true——验证读路径的 Flow 响应式（map 转换正确）
      assertEquals(true, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }
}
