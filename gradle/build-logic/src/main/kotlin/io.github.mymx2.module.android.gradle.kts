@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import io.github.mymx2.plugin.android.androidApplicationName
import io.github.mymx2.plugin.android.androidNamespace
import net.swiftzer.semver.SemVer

plugins {
  id("com.android.application")
  // AGP 9 起 Kotlin 支持内置（built-in Kotlin），禁止再应用 org.jetbrains.kotlin.android。
  // Compose 编译器/serialization 插件版本须与 AGP built-in KGP 版本对齐（根 catalog 里同为 2.4.0）。
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
  // 共享约定（SDK/ABI/flavor/compose/testOptions/versionCode）由 feature.android-common 承载。
  id("io.github.mymx2.feature.android-common")
  // 签名（android.signing.* 四件，可选）由 feature.android-signing 承载。
  id("io.github.mymx2.feature.android-signing")
  // 四层测试：纯逻辑（JUnit5）、组件交互（Robolectric）、截图回归（Roborazzi）、真机 E2E（Instrumented）。
  id("io.github.mymx2.feature.android-test-unit")
  id("io.github.mymx2.feature.android-test-robolectric")
  id("io.github.mymx2.feature.android-test-roborazzi")
  id("io.github.mymx2.feature.android-test-e2e")
}

// application 专属：应用包名（androidNamespace 共享推导）。
val applicationIdString: String = project.androidNamespace()

// 校验 applicationId 是合法包名格式，恶意/误配的属性值在此拦截而非静默接受。
require(applicationIdString.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$"))) {
  "android.applicationId must be a valid package name, got: $applicationIdString"
}

configure<ApplicationExtension> {
  namespace = applicationIdString

  // application 专属：applicationId（唯一包名，library 无此概念故留本插件）+ R8。
  // 共享配置（SDK/flavor/packaging/testOptions/lint）由 feature.android-common 承载，签名由
  // feature.android-signing 承载。
  defaultConfig { applicationId = applicationIdString }

  buildTypes {
    release {
      // R8 官方模式：isMinifyEnabled + isShrinkResources 全开，以 proguard-android-optimize.txt 为基线。
      // 不用 -dontshrink（官方明示的反模式，直接关闭 R8 压缩）；需要的类用精确 -keep 保留。
      isMinifyEnabled = true
      isShrinkResources = true
      // proguard 规则内置：系统默认 proguard-android-optimize.txt + 模块本地 proguard-rules.pro（存在才加），
      // 免去每个 app 复制 9 行模板；模块要加规则只放 proguard-rules.pro 即可。
      val localRules = project.file("proguard-rules.pro")
      if (localRules.exists()) {
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), localRules)
      } else {
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
      }
    }
  }
}

extensions.configure<ApplicationAndroidComponentsExtension> {
  onVariants { variant ->
    // APK 文件名用简短应用名（android.applicationName），未配置回退 applicationId（见 androidApplicationName）。
    val appName = project.androidApplicationName()
    val versionNameString = SemVer.parse(project.version.toString()).toString()
    // APK 输出命名：{applicationName}-v{versionName}[-{env}]-{buildType}.apk；prod 省略 env 段。
    // applicationId 本身按环境动态（dev/staging 带 .dev/.staging 后缀，真机可同装多环境），与文件名解耦。
    val envSegment = variant.flavorName?.takeIf { it.isNotBlank() && it != "prod" }
    val variantLabel = listOfNotNull(envSegment, variant.buildType).joinToString("-")
    variant.outputs.forEach { output ->
      output.outputFileName = "$appName-v$versionNameString-$variantLabel.apk"
    }

    // mapping.txt 归档：R8 反混淆/崩溃符号化的公共前提，release 构建时复制到 build/reports/mapping/{variant}/ 稳定目录。
    // 丢了补不回来——任何 Crash 聚合/监控平台符号化都依赖它，故做成默认动作而非可选。
    // 归档到 reports/（AGP 不写的区域），避免与 AGP 内部 mapping 产物路径重叠引发任务依赖冲突。
    if (variant.buildType == "release") {
      val cap = variant.name.replaceFirstChar { it.uppercase() }
      val archiveMapping =
        project.tasks.register<Copy>("archive${cap}Mapping") {
          group = "build"
          description =
            "Archives the R8/ProGuard mapping.txt of ${variant.name} to build/reports/mapping/${variant.name} for crash de-obfuscation."
          from(
            variant.artifacts.get(
              com.android.build.api.artifact.SingleArtifact.OBFUSCATION_MAPPING_FILE
            )
          )
          into(project.layout.buildDirectory.dir("reports/mapping/${variant.name}"))
        }
      // variant 任务晚于 onVariants 回调注册，用 matching 惰性绑定避免 named 找不到任务。
      project.tasks
        .matching { it.name == "assemble$cap" }
        .configureEach { dependsOn(archiveMapping) }
    }
  }
}
