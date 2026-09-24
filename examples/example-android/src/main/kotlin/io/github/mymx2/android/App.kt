package io.github.mymx2.android

import android.app.Application
import android.os.StrictMode

/** 应用入口：进程级一次性初始化。诊断开关跟构建类型走（debug 开 StrictMode，release 零开销）。 */
class App : Application() {

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      enableStrictMode()
    }
  }

  /**
   * StrictMode 仅在 debug 开：主线程磁盘/网络与虚拟机泄漏诊断，penaltyLog 输出 logcat 供 CI/本地排查。 不用
   * penaltyDialog/penaltyDeath（自动化测试无用、干扰开发）；release 不装，零运行时开销。
   */
  private fun enableStrictMode() {
    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
  }
}
