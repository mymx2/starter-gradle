package io.github.mymx2.plugin.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.dsl.TestOptions
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.HasUnitTestBuilder
import com.android.build.api.variant.VariantOutputConfiguration
import io.github.mymx2.plugin.environment.buildProperties
import io.github.mymx2.plugin.utils.SemVerUtils
import java.io.File
import net.swiftzer.semver.SemVer
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * Android 约定插件的共享构建逻辑，供 application 与 library 两个插件复用。
 *
 * 拆分为 module.android（application）与 module.android.library（library）：
 * - 共享：属性读取、SDK 版本、versionCode/Name、ABI 过滤、flavor、compose、testOptions、质量插件。
 * - application 独有：applicationId、签名、APK 命名（见 module.android）。
 * - library 独有：不配置 applicationId/签名/APK 命名（库不打 APK、无需 applicationId 与签名）。
 */

/** 读取 android. 前缀的构建属性（支持根默认 + 模块覆盖，见 EnvAccess.buildProperties）。 */
fun Project.androidProp(key: String, default: String): String =
  buildProperties().getProperty("android.$key", default)

/**
 * namespace / applicationId 推导：android.applicationId 有值用之，否则按 group.模块名（- 转 .）推导。 application 与
 * library 共用（library 的 namespace 与 application 的 applicationId 同源）。
 */
fun Project.androidNamespace(): String =
  androidProp("applicationId", "").ifBlank {
    "${project.group}.${project.name}".replace("-", ".")
  }

/**
 * 应用名（APK 文件名用）：android.applicationName 有值用之，否则回退 applicationId。 简短应用名（如 demo）让 APK 文件名简洁；未配置时回退完整
 * applicationId 保证唯一。
 */
fun Project.androidApplicationName(): String =
  androidProp("applicationName", "").ifBlank { androidNamespace() }

/** compileSdk：顶到 37 碰 Android 17 新 API，可 build.properties 覆盖。 */
fun Project.compileSdkVersion(): Int = androidProp("compileSdk", "37").toInt()

/** targetSdk：钉 36 生产安全线（API 37 平台 Beta），可覆盖。 */
fun Project.targetSdkVersion(): Int = androidProp("targetSdk", "36").toInt()

/** minSdk：29（Android 10）面向 80% 用户，可覆盖。 */
fun Project.minSdkVersion(): Int = androidProp("minSdk", "29").toInt()

/**
 * versionName：android.versionName 有值时模块级覆盖（多 app 各发各版）， 否则回退 base.identity 解析的
 * SemVer（project.version，根统一版本）。 覆盖值也走 SemVer 校验，非法值在此拦截而非带进 APK。
 */
fun Project.versionNameString(): String {
  val override = androidProp("versionName", "")
  return if (override.isNotBlank()) {
    SemVer.parse(override).toString()
  } else {
    SemVer.parse(version.toString()).toString()
  }
}

/**
 * versionCode 两种模式：android.versionCode 有值时显式锁定（发版钉死）， 否则用 VersionCodeTask 自动递增（配置缓存安全，见 onVariants
 * 接线）。
 */
fun Project.versionCodeOverride(): String = androidProp("versionCode", "")

/** versionCode 显式值转 Int，非法值给出含 key 名的清晰错误（而非裸 NumberFormatException）。 */
fun Project.versionCodeOverrideInt(): Int? {
  val raw = versionCodeOverride()
  if (raw.isBlank()) return null
  val code = raw.toIntOrNull()
  require(code != null && code > 0) {
    "android.versionCode must be a positive Int, got: $raw"
  }
  return code
}

/**
 * ABI 过滤：默认 arm64-v8a + x86_64（64 位真机 + 模拟器调试），可 android.abiFilters 覆盖（逗号分隔）。 Wear/车机/老设备 app 需
 * armeabi-v7a 等 32 位时，在模块 build.properties 覆盖而非改 build-logic。
 */
fun Project.abiFilters(): List<String> =
  androidProp("abiFilters", "arm64-v8a,x86_64")
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

/** 配置共享段：SDK 版本、versionName、ABI 过滤、矢量图、flavor、packaging、buildFeatures、testOptions。 */
// AGP 9 的 CommonExtension 已非泛型接口，defaultConfig/productFlavors/packaging/testOptions/buildFeatures 等
// DSL lambda 只在 ApplicationExtension / LibraryExtension 具体类型上可用——故分两个具体扩展函数。
// 内容相同的部分抽成下方共享数据/helper：flavor 三件套数据、packaging 排除清单、testOptions 配置，
// 两个扩展函数只剩 defaultConfig 差异（application 多 targetSdk/versionName/versionCode/applicationIdSuffix）。

/** 环境 flavor 三件套：(名称, applicationIdSuffix?)。library 忽略 suffix（无 applicationId 概念）。 */
private val ENV_FLAVORS = listOf("dev" to ".dev", "staging" to ".staging", "prod" to null)

/** packaging 资源排除清单（application/library 共用）。 */
private val PACKAGING_EXCLUDES =
  listOf(
    "META-INF/DEPENDENCIES",
    "META-INF/LICENSE",
    "META-INF/LICENSE.txt",
    "META-INF/NOTICE",
    "META-INF/NOTICE.txt",
    "META-INF/*.SF",
    "META-INF/*.DSA",
    "META-INF/*.RSA",
  )

/** testOptions 公共配置：JUnit5 平台 + Robolectric conscrypt 本地库加载许可。 */
private fun TestOptions.configureUnitTests() {
  unitTests {
    isIncludeAndroidResources = true
    all {
      it.useJUnitPlatform()
      // Robolectric 的 conscrypt 本地库在新版 JDK 上需显式允许本地方法加载，消除 restricted method 警告。
      it.jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
  }
}

/** buildFeatures 公共配置：Compose + BuildConfig。 */
private fun BuildFeatures.configureBuildFeatures() {
  compose = true
  buildConfig = true
}

/**
 * lint 公共配置：NewApi 升 error（编译期拦截 minSdk 版本兼容 crash，ROI 最高的版本兼容拦截）+ baseline 渐进接入。 baseline 仅在
 * lint-baseline.xml 存在时启用——老项目先 `./gradlew :module:lint` 生成它冻结存量，新违规即报错； 无存量问题的新模块不放该文件（存在性检查避免 AGP
 * 因文件缺失反复重建空 baseline 并中断构建）。
 */
private fun com.android.build.api.dsl.Lint.configureAndroidLint(project: Project) {
  warningsAsErrors = true
  error += "NewApi"
  val baselineFile = project.file("lint-baseline.xml")
  if (baselineFile.exists()) {
    baseline = baselineFile
  }
}

/**
 * 环境 flavor 公共配置（application/library 共用）。 ProductFlavor 是 ApplicationProductFlavor 与
 * LibraryProductFlavor 的共同祖先， dimension/buildConfigField 在其上可达；applicationIdSuffix 仅
 * ApplicationProductFlavor 有，由调用方按类型补。
 */
private fun ProductFlavor.configureEnvFlavor(env: String) {
  dimension = "env"
  buildConfigField("String", "ENV_NAME", "\"$env\"")
}

/** Application 版共享配置。 */
fun ApplicationExtension.configureSharedAndroid(project: Project) {
  compileSdk = project.compileSdkVersion()

  defaultConfig {
    minSdk = project.minSdkVersion()
    targetSdk = project.targetSdkVersion()
    versionName = project.versionNameString()
    project.versionCodeOverrideInt()?.let { versionCode = it }
    ndk { abiFilters += project.abiFilters() }
    vectorDrawables.useSupportLibrary = true
    // 构建标识：git 短 commit（取 SemVerUtils.gitBuildMetadata 完整 commit 前 7 位，与 JVM 侧同源），
    // providers.exec 配置缓存安全，非 git 环境回退 unknown。
    buildConfigField(
      "String",
      "GIT_COMMIT",
      "\"${SemVerUtils.gitBuildMetadata(project.providers, project.layout).take(7)}\"",
    )
  }

  flavorDimensions += "env"
  productFlavors {
    ENV_FLAVORS.forEach { [env, suffix] ->
      create(env) {
        configureEnvFlavor(env)
        suffix?.let { applicationIdSuffix = it }
      }
    }
  }

  packaging { resources.excludes.addAll(PACKAGING_EXCLUDES) }
  buildFeatures { configureBuildFeatures() }
  testOptions { configureUnitTests() }
  lint { configureAndroidLint(project) }
}

/** Library 版共享配置（与 Application 共用公共段；库无 targetSdk/versionName/versionCode/applicationIdSuffix）。 */
fun LibraryExtension.configureSharedAndroid(project: Project) {
  compileSdk = project.compileSdkVersion()

  defaultConfig {
    minSdk = project.minSdkVersion()
    ndk { abiFilters += project.abiFilters() }
    vectorDrawables.useSupportLibrary = true
  }

  flavorDimensions += "env"
  productFlavors {
    ENV_FLAVORS.forEach { [env, _] -> create(env) { configureEnvFlavor(env) } }
  }

  packaging { resources.excludes.addAll(PACKAGING_EXCLUDES) }
  buildFeatures { configureBuildFeatures() }
  testOptions { configureUnitTests() }
  lint { configureAndroidLint(project) }
}

/** 在 application 的 onVariants 里接 versionCode 自动递增（仅 application 有 outputs/versionCode）。 */
fun ApplicationAndroidComponentsExtension.configureAutoVersionCode(
  project: Project,
  useAuto: Boolean,
) {
  if (!useAuto) return
  onVariants { variant ->
    val mainOutput =
      variant.outputs.single {
        it.outputType == VariantOutputConfiguration.OutputType.SINGLE
      }
    val versionCodeTask =
      project.tasks.register<VersionCodeTask>("${variant.name}VersionCodeProvider") {
        versionCodeFile.set(
          File(
            project.layout.buildDirectory.asFile.get(),
            "intermediates/versionProvider/versionCode",
          )
        )
        outputs.upToDateWhen { false }
      }
    mainOutput.versionCode.set(
      versionCodeTask.flatMap { task ->
        task.versionCodeFile.map { f -> f.asFile.readText().toInt() }
      }
    )
  }
}

/**
 * 单测任务收敛：先显式启用所有 variant 的 unit test（AGP 对 release 默认禁用，Compose Preview 需要它打包资源 APK）， 再禁用非 tested
 * flavor 的单测任务执行（enabled=false）。任务仍在，只是跳过。 单测与环境无关（纯 JVM 逻辑，BuildConfig.ENV_NAME 差异不影响断言），一套 dev
 * 足够。 可用 android.testedFlavor 覆盖被测环境（如需在 CI 单独跑 staging 单测）。
 */
fun AndroidComponentsExtension<*, *, *>.configureTestedFlavor(project: Project) {
  val testedFlavor = project.androidProp("testedFlavor", "dev")
  beforeVariants { variantBuilder ->
    (variantBuilder as? HasUnitTestBuilder)?.enableUnitTest = true
  }
  project.tasks.withType<Test>().configureEach {
    val taskName = name.removePrefix("test").removeSuffix("UnitTest")
    // taskName 形如 DevDebug / ProdRelease / StagingDebug 等
    if (
      taskName.isNotBlank() &&
        !taskName.startsWith(testedFlavor.replaceFirstChar { it.uppercase() })
    ) {
      enabled = false
    }
  }
}
