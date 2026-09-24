@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.android.AndroidTestDeps

plugins {
  // Robolectric 组件交互层：在 test sourceSet 跑 Android 逻辑 + Compose UI（不起设备，毫秒级）。
  // 官方首选：Compose UI 行为测试放 test sourceSet 用 Robolectric，仅真实设备/系统行为才上 androidTest（E2E 层）。
  id("io.github.mymx2.base.lifecycle")
}

// Robolectric 运行时 + Compose UI 测试驱动（与 E2E 层共享 ui-test-junit4，版本单点见 AndroidTestDeps）。
// junit-vintage-engine：JUnit5 平台跑 JUnit4 的 Robolectric @RunWith 桥，版本由
// junit-bom（feature.android-test-unit）统一管理。
dependencies {
  add("testImplementation", AndroidTestDeps.ROBOLECTRIC)
  add("testImplementation", AndroidTestDeps.COMPOSE_UI_TEST_JUNIT4)
  add("testRuntimeOnly", AndroidTestDeps.JUNIT_VINTAGE_ENGINE)
}
