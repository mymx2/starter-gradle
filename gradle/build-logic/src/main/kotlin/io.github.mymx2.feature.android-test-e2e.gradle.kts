@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestOptions
import io.github.mymx2.plugin.android.AndroidTestDeps
import io.github.mymx2.plugin.android.androidProp

plugins {
  // 真机 E2E（Instrumented）测试：端到端层，跑在设备/模拟器上验证真实系统行为（系统返回栈、进程重建）。
  // 无需额外 Gradle 插件，AGP 内置 androidTest sourceSet；此处统一引入依赖、runner 与受管设备。
  id("io.github.mymx2.base.lifecycle")
}

// E2E 依赖：Compose UI 测试在 androidTest sourceSet 驱动真实界面；Espresso + test.ext 是设备端通用组件。版本单点见
// AndroidTestDeps。
dependencies {
  add("androidTestImplementation", AndroidTestDeps.COMPOSE_UI_TEST_JUNIT4)
  add("androidTestImplementation", AndroidTestDeps.ESPRESSO_CORE)
  add("androidTestImplementation", AndroidTestDeps.TEST_EXT_JUNIT)
}

// 设备端测试入口：AndroidJUnitRunner 是官方 runner，支持 JUnit4 与 Compose 测试规则；可
// android.testInstrumentationRunner 覆盖。
// Gradle Managed Devices：CI 可复现跑 E2E（AGP 自动下载/启动/关停模拟器），无需外部已连接设备。
// application 与 library 双适配（库也可有 androidTest）；withPlugin 守卫避免裸 configure 在缺宿主插件时崩溃。
val instrumentationRunner: String =
  project.androidProp("testInstrumentationRunner", "androidx.test.runner.AndroidJUnitRunner")

pluginManager.withPlugin("com.android.application") {
  extensions.configure<ApplicationExtension> {
    defaultConfig { testInstrumentationRunner = instrumentationRunner }
    testOptions { configureManagedDevices() }
  }
}

pluginManager.withPlugin("com.android.library") {
  extensions.configure<LibraryExtension> {
    defaultConfig { testInstrumentationRunner = instrumentationRunner }
    testOptions { configureManagedDevices() }
  }
}

// 受管设备矩阵：API 29（minSdk 下限）与 API 36（targetSdk 生产线），aosp-atd 镜像为无头测试优化。
// TestOptions 是 ApplicationExtension 与 LibraryExtension 的共同类型，managedDevices DSL 在其上可达，故单点定义。
// 任务：{device}DevDebugAndroidTest（如
// pixel9Api36DevDebugAndroidTest）；组：phoneMatrixGroupDevDebugAndroidTest。
private fun TestOptions.configureManagedDevices() {
  managedDevices {
    val pixel7Api29 =
      localDevices.create("pixel7Api29") {
        device = "Pixel 7"
        apiLevel = 29
        systemImageSource = "aosp-atd"
      }
    val pixel9Api36 =
      localDevices.create("pixel9Api36") {
        device = "Pixel 9"
        apiLevel = 36
        systemImageSource = "aosp-atd"
      }
    groups {
      create("phoneMatrix") {
        targetDevices.add(pixel7Api29)
        targetDevices.add(pixel9Api36)
      }
    }
  }
}
