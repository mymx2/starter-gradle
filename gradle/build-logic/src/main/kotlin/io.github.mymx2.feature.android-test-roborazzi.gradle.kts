@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.android.AndroidTestDeps

plugins {
  // Roborazzi 截图测试：样式回归层，本地 record/verify 对比渲染基线。
  // 需在 build-logic classpath 有 roborazzi-gradle-plugin（libs.plugins.io.github.takahirom.roborazzi）。
  id("io.github.takahirom.roborazzi")
  id("io.github.mymx2.base.lifecycle")
}

// Roborazzi 依赖：核心库 + Compose 截图支持，跑在 test sourceSet（JUnit5/Robolectric 环境）。版本单点见 AndroidTestDeps。
dependencies {
  add("testImplementation", AndroidTestDeps.ROBORAZZI)
  add("testImplementation", AndroidTestDeps.ROBORAZZI_COMPOSE)
}

// 截图基线输出到模块 src/test/screenshots，随源码入库，供 CI 统一校验。
// 工作流：recordRoborazzi{Variant} 录基线（改样式后）→ git 提交 PNG → verifyRoborazzi{Variant} 校验（CI 跑）。
// 用 roborazzi DSL outputDir——systemProperty("roborazzi.output.dir") 不存在，之前配置无效会回退默认
// build/outputs/roborazzi。
// record 落到 outputDir 需配
// roborazzi.record.filePathStrategy=relativePathFromRoborazziContextOutputDirectory，
// 该配置在**根** gradle.properties（全仓统一，非模块级）。
// roborazzi 插件对 application 与 library 均适用（设计系统/组件库同样需要样式回归），无需区分扩展类型。
roborazzi { outputDir.set(project.layout.projectDirectory.file("src/test/screenshots").asFile) }
