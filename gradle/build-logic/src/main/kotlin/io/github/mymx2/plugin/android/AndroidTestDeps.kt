package io.github.mymx2.plugin.android

import io.github.mymx2.plugin.InternalDependencies

/**
 * Android 测试依赖坐标单点定义。
 *
 * 版本单点在 InternalDependencies（纳入 checkVersions 升级检查，见 tools.check-version 插件）； 本对象仅提供类型安全的命名访问，避免
 * feature 插件散落 InternalDependencies.useLibrary("...") 字符串键。 Android 依赖不进 libs.versions.toml（它只放纯
 * JVM 通用依赖）。
 */
object AndroidTestDeps {
  // 设备端 / Robolectric 共享
  val COMPOSE_UI_TEST_JUNIT4 = InternalDependencies.useLibrary("uiTestJunit4")
  val ROBOLECTRIC = InternalDependencies.useLibrary("robolectric")
  val ESPRESSO_CORE = InternalDependencies.useLibrary("espressoCore")
  val TEST_EXT_JUNIT = InternalDependencies.useLibrary("testExtJunit")
  val ROBORAZZI = InternalDependencies.useLibrary("roborazzi")
  val ROBORAZZI_COMPOSE = InternalDependencies.useLibrary("roborazziCompose")

  // 本地单测增强件（JUnit5 基础件 junit-bom 已在 InternalDependencies，不在此重复）
  val KOTLIN_TEST = InternalDependencies.useLibrary("kotlinTest")
  val COROUTINES_TEST = InternalDependencies.useLibrary("kotlinxCoroutinesTest")
  val TURBINE = InternalDependencies.useLibrary("turbine")

  // JUnit5 平台跑 JUnit4 runner（Robolectric @RunWith）的桥。
  // 版本由 junit-bom（feature.android-test-unit 经 InternalDependencies 引入）统一管理，本身无独立版本号，
  // 故不登记进 InternalDependencies（该处按 module:version 拼坐标，无版本会拼出非法值）——这是本对象唯一用 const 的坐标。
  const val JUNIT_VINTAGE_ENGINE = "org.junit.vintage:junit-vintage-engine"
}
