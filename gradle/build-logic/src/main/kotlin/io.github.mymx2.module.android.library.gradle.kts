@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.LibraryExtension
import io.github.mymx2.plugin.android.androidNamespace

plugins {
  id("com.android.library")
  // AGP 9 起 Kotlin 支持内置（built-in Kotlin），禁止再应用 org.jetbrains.kotlin.android。
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
  // 共享约定（SDK/ABI/flavor/compose/testOptions）由 feature.android-common 承载。
  id("io.github.mymx2.feature.android-common")
  // Dokka KDoc 生成，发布 AAR 时需要
  id("io.github.mymx2.feature.doc-kotlin")
  // AAR 发布到 Maven Central（vanniktech 插件原生支持 Android library）
  id("io.github.mymx2.feature.publish-vanniktech")
}

configure<LibraryExtension> {
  // library 专属：namespace（androidNamespace 共享推导），无需 applicationId/签名/APK 命名。
  namespace = project.androidNamespace()

  // library 专属：R8 关闭，keep 规则经 consumerProguardFiles 随 AAR 传给使用方。
  buildTypes {
    release { isMinifyEnabled = false }
  }
}
