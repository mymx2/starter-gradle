@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.InternalDependencies
import io.github.mymx2.plugin.android.AndroidTestDeps

plugins {
  // Android 本地单元测试（纯逻辑层）：JUnit5 + 协程测试 + Turbine 断言 StateFlow。
  id("io.github.mymx2.base.lifecycle")
}

// JUnit5 基础件：junit-bom 版本单点在 InternalDependencies（与 JVM 侧同源，useJUnitJupiterM2 依赖 JVM 专属
// TestingExtension，
// AGP 模块无此扩展，故此处直接声明）。jupiter/launcher 版本由 BOM 统一管理无需指定。
dependencies {
  add("testImplementation", platform(InternalDependencies.useLibrary("junitBom")))
  add("testImplementation", "org.junit.jupiter:junit-jupiter")
  add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")

  // Android 专属测试增强件：kotlin-test 断言、协程测试调度器、Turbine StateFlow 序列断言。版本单点见 AndroidTestDeps。
  add("testImplementation", AndroidTestDeps.KOTLIN_TEST)
  add("testImplementation", AndroidTestDeps.COROUTINES_TEST)
  add("testImplementation", AndroidTestDeps.TURBINE)
}
