@file:Suppress("UnstableApiUsage")

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.AndroidComponents
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import io.github.mymx2.plugin.android.configureAutoVersionCode
import io.github.mymx2.plugin.android.configureSharedAndroid
import io.github.mymx2.plugin.android.configureTestedFlavor
import io.github.mymx2.plugin.android.versionCodeOverride

plugins {
  id("io.github.mymx2.base.identity")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.base.jvm-conflict-android")
  id("io.github.mymx2.check.dependencies-android")
  id("io.github.mymx2.check.format-base")
  id("io.github.mymx2.check.format-gradle")
  id("io.github.mymx2.check.format-java")
  id("io.github.mymx2.check.format-kotlin")
  id("io.github.mymx2.check.quality-detekt")
  id("io.github.mymx2.feature.unzip-sources")
  id("io.github.mymx2.feature.openrewrite")
  id("io.github.mymx2.check.quality-nullaway-android")
}

// AGP 版本下限守卫：本插件体系用 AGP 9 专属能力（built-in Kotlin、非泛型 CommonExtension、新 Variant API），
// 且 16KB 页对齐稳定产物需 AGP 8.5.1+。低于 9.0 直接报错，不在旧 AGP 上半路失败。
val minAgp = AndroidPluginVersion(9, 0)

fun assertAgpVersion(components: AndroidComponents) {
  val current = components.pluginVersion
  require(current >= minAgp) {
    "io.github.mymx2 Android 约定插件要求 AGP >= ${minAgp.major}.${minAgp.minor}，当前 ${current.major}.${current.minor}.${current.micro}。请升级 com.android.application/library 版本。"
  }
}

// application：配 applicationId 之外的共享项 + versionCode 自动递增 + 单测 flavor 收敛。
pluginManager.withPlugin("com.android.application") {
  assertAgpVersion(extensions.getByType(AndroidComponentsExtension::class.java))
  extensions.configure<ApplicationExtension> { configureSharedAndroid(project) }
  extensions.getByType(AndroidComponentsExtension::class.java).configureTestedFlavor(project)
  extensions
    .getByType(ApplicationAndroidComponentsExtension::class.java)
    .configureAutoVersionCode(project, project.versionCodeOverride().isBlank())
}

// library：配共享项（无 targetSdk/versionName/versionCode/applicationId），不配 versionCode 自动递增。
pluginManager.withPlugin("com.android.library") {
  assertAgpVersion(extensions.getByType(AndroidComponentsExtension::class.java))
  extensions.configure<LibraryExtension> { configureSharedAndroid(project) }
  extensions.getByType(AndroidComponentsExtension::class.java).configureTestedFlavor(project)
}
