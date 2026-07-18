@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault

plugins {
  id("io.github.mymx2.module.kotlin")
  id("io.github.mymx2.module.app")
  id("io.github.mymx2.feature.benchmark") apply false
}

// [perf] 端到端测试套件(testEndToEnd + testEndToEndSlow 及其 mockApi 源码集)在本地开发循环里很重
// (约占 check 循环 70%)。默认 SKIP_E2E=false 保持原行为；本地加 -PSKIP_E2E=true 可跳过它们，
// 仅跑单元测试/集成测试，CI 不受影响。SKIP_ALL_LOCAL 等价于同时开启三个 SKIP_* 旗。
val skipE2E = project.getPropOrDefault(LocalConfig.Props.SKIP_E2E).toBoolean()
val skipAllLocal = project.getPropOrDefault(LocalConfig.Props.SKIP_ALL_LOCAL).toBoolean()

val isJmh = project.getPropOrDefault(LocalConfig.Props.IS_JMH).toBoolean()

if (isJmh) {
  apply(plugin = "io.github.mymx2.feature.benchmark")
}

if (!skipE2E && !skipAllLocal) {
  apply(plugin = "io.github.mymx2.feature.test-end2end")
}

application { mainClass.set("io.github.mymx2.app.Application") }

dependencies {
  implementation("org.slf4j:slf4j-api")
  runtimeOnly("org.slf4j:slf4j-simple")
}

if (!skipE2E && !skipAllLocal) {
  dependencies {
    // 用字符串形式声明，避免插件未应用时 kotlin-dsl 访问器缺失导致编译失败
    "mockApiImplementation"(projects.app)
    "testEndToEndImplementation"(projects.app) {
      capabilities { requireFeature("mock-api") }
    }
  }
}
