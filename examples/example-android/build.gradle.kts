// 本模块是"Android 依赖显式引入、不进版本目录"的示例（libs 只放纯 JVM 通用依赖），
// 故业务依赖显式写坐标，压制 IDE 对硬编码版本建议走 catalog 的 UseTomlInstead 警告——这是有意为之，非疏漏。
@file:Suppress("UseTomlInstead")

plugins {
  id("io.github.mymx2.module.android")
}

// 环境变量示例：插件只提供 dev/staging/prod flavor 结构，业务环境变量由各模块自己声明。
// 在 defaultConfig 写默认值，在各 flavor 里按需覆盖。代码里用 BuildConfig.BASE_URL / PUBLIC_KEY 读取。
// 注意：BuildConfig 中的值会被打进 APK，反编译（JADX/strings）可提取——
// 只放公钥(PUBLIC_KEY，本就设计来分发到客户端，如支付/license/加解密公钥)与非敏感配置(BASE_URL)；
// 真实服务端密钥禁止放这里，走服务端鉴权。
android {
  defaultConfig {
    buildConfigField("String", "BASE_URL", """"https://api.example.com"""")
    buildConfigField(
      "String",
      "PUBLIC_KEY",
      """"-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...\n-----END PUBLIC KEY-----"""",
    )
  }
  productFlavors {
    named("dev") {
      buildConfigField("String", "BASE_URL", """"https://dev.api.example.com"""")
      buildConfigField(
        "String",
        "PUBLIC_KEY",
        """"-----BEGIN PUBLIC KEY-----\nDEV-KEY...\n-----END PUBLIC KEY-----"""",
      )
    }
    named("staging") {
      buildConfigField("String", "BASE_URL", """"https://staging.api.example.com"""")
      buildConfigField(
        "String",
        "PUBLIC_KEY",
        """"-----BEGIN PUBLIC KEY-----\nSTAGING-KEY...\n-----END PUBLIC KEY-----"""",
      )
    }
    // prod 用 defaultConfig 的默认值，无需覆盖
  }

  // 混淆已内置：module.android 默认 R8 官方模式（isMinifyEnabled + isShrinkResources）并引用
  // proguard-android-optimize.txt + 模块本地 proguard-rules.pro（本模块已提供，含精确 -keep 模板，无 -dontshrink）。
  // 若反射/序列化/SDK 被 R8 误删，在 proguard-rules.pro 追加精确 -keep，不要禁用 shrink。
}

// Compose：BOM 统一管理工件版本 + M3 组件 + 自适应导航骨架 + 预览/工具
dependencies {
  implementation("androidx.compose.material3.adaptive:adaptive")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
  implementation("androidx.compose.material:material-icons-core")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation(platform("androidx.compose:compose-bom:2026.09.00"))

  debugImplementation("androidx.compose.ui:ui-tooling")
}

// Activity 与系统能力：Compose 入口、KTX、启动屏
dependencies {
  implementation("androidx.activity:activity-compose:1.13.0")
  implementation("androidx.core:core-ktx:1.19.1")
  implementation("androidx.core:core-splashscreen:1.2.0")
}

// 状态与生命周期：StateFlow 订阅、ViewModel
dependencies {
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
}

// 导航：Navigation 3 类型安全路由
dependencies {
  implementation("androidx.navigation3:navigation3-runtime:1.2.0")
  implementation("androidx.navigation3:navigation3-ui:1.2.0")
}

// 序列化：@Serializable NavKey 与 DTO
dependencies { implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0") }

// 偏好存储：Preferences DataStore（协程/Flow 原生、事务性），不用 SharedPreferences（apply 主线程硬等落盘是 ANR 风险）
dependencies {
  implementation("androidx.datastore:datastore-preferences:1.2.1")
}

// 测试依赖全部由 feature 插件统一引入（四层），此处零声明：
//  纯逻辑     feature.android-test-unit         JUnit5 + kotlin-test + coroutines-test + turbine
//  组件交互   feature.android-test-robolectric  robolectric + ui-test-junit4(test) + junit-vintage
//  截图回归   feature.android-test-roborazzi    roborazzi + roborazzi-compose
//  真机 E2E   feature.android-test-e2e          ui-test-junit4(androidTest) + espresso + 受管设备
