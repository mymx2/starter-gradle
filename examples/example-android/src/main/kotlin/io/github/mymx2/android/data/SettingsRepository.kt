package io.github.mymx2.android.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 设置读写契约：ViewModel 依赖此接口，单测可用 test double 替换 DataStore 实现。 */
interface SettingsRepository {
  /** 读：倍速开关状态以 Flow 暴露，数据变更自动推送给订阅者。 */
  val rapidCount: Flow<Boolean>

  /** 写：edit 事务内修改，IO 在 Dispatchers.IO 执行，不阻塞主线程。 */
  suspend fun setRapidCount(enabled: Boolean)
}

/** Preferences DataStore 单例（进程级，top-level 委托保证唯一实例）。internal 供测试重置状态（复用同一单例，避免多实例抢同一文件）。 */
internal val Context.settingsDataStore by preferencesDataStore(name = "settings")

/**
 * 设置仓库（DataStore 实现）：Preferences DataStore 最小演示。 存"倍速计数"开关——演示 DataStore 读（Flow）与写（edit）两条路径，与
 * StateFlow 响应式模型同构。 不用 Proto DataStore（三五个布尔开关维护 .proto 过重，这是官方取舍）。
 */
class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

  private val keyRapidCount = booleanPreferencesKey("rapid_count")

  override val rapidCount: Flow<Boolean> =
    context.settingsDataStore.data.map { it[keyRapidCount] ?: false }

  override suspend fun setRapidCount(enabled: Boolean) {
    context.settingsDataStore.edit { it[keyRapidCount] = enabled }
  }
}
