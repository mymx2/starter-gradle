@file:Suppress("PackageDirectoryMismatch")

package io.github.mymx2.plugin

object InternalDependencies {

  // spotless 8.10.2 默认 ktfmt 0.63 不支持中括号析构（lambda 参数 [k, v]），0.64 起支持。
  // 但 0.64 仍不支持 val [a, b] 声明式析构，需等 ktfmt > 0.64 发版。
  // TODO: 跟踪 spotless 默认 ktfmt 版本，>0.64 后移除此属性并恢复无参 ktfmt() 调用
  // 上游 PR: https://github.com/diffplug/spotless/pull/2988
  // ktfmt val [a, b] 声明式析构修复（未发版）: https://github.com/Kotlin/ktfmt/issues/704
  const val KTFMT_VERSION = "0.64"

  data class Library(
    val key: String,
    val module: String,
    val version: String,
    val type: String,
    val url: String,
  )

  // spotless:off
  val data =
    """
    [libraries]
    # Maven dependencies
    junitBom = { module = "org.junit:junit-bom", version = "6.1.3", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/org/junit/junit-bom/maven-metadata.xml" }
    assertjBom = { module = "org.assertj:assertj-bom", version = "4.0.0-M1", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/org/assertj/assertj-bom/maven-metadata.xml" }
    nullaway = { module = "com.uber.nullaway:nullaway", version = "0.14.1", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/uber/nullaway/nullaway/maven-metadata.xml" }
    errorProneCore = { module = "com.google.errorprone:error_prone_core", version = "2.50.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/google/errorprone/error_prone_core/maven-metadata.xml" }
    errorProneContrib = { module = "tech.picnic.error-prone-support:error-prone-contrib", version = "0.30.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/tech/picnic/error-prone-support/error-prone-contrib/maven-metadata.xml" }
    refasterRunner = { module = "tech.picnic.error-prone-support:refaster-runner", version = "0.30.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/tech/picnic/error-prone-support/refaster-runner/maven-metadata.xml" }
    spotbugsAnnotations = { module = "com.github.spotbugs:spotbugs-annotations", version = "4.10.4", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/github/spotbugs/spotbugs-annotations/maven-metadata.xml" }

    # Android 测试坐标（Android 依赖不进 libs.versions.toml——它只放纯 JVM；但版本须纳入 checkVersions 升级检查，故在此登记）
    uiTestJunit4 = { module = "androidx.compose.ui:ui-test-junit4", version = "1.13.0-alpha03", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/androidx/compose/ui/ui-test-junit4/maven-metadata.xml" }
    robolectric = { module = "org.robolectric:robolectric", version = "4.17", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/org/robolectric/robolectric/maven-metadata.xml" }
    espressoCore = { module = "androidx.test.espresso:espresso-core", version = "3.7.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/androidx/test/espresso/espresso-core/maven-metadata.xml" }
    testExtJunit = { module = "androidx.test.ext:junit", version = "1.3.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/androidx/test/ext/junit/maven-metadata.xml" }
    roborazzi = { module = "io.github.takahirom.roborazzi:roborazzi", version = "1.75.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/io/github/takahirom/roborazzi/roborazzi/maven-metadata.xml" }
    roborazziCompose = { module = "io.github.takahirom.roborazzi:roborazzi-compose", version = "1.75.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/io/github/takahirom/roborazzi/roborazzi-compose/maven-metadata.xml" }
    kotlinTest = { module = "org.jetbrains.kotlin:kotlin-test", version = "2.4.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/org/jetbrains/kotlin/kotlin-test/maven-metadata.xml" }
    kotlinxCoroutinesTest = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version = "1.11.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/org/jetbrains/kotlinx/kotlinx-coroutines-test/maven-metadata.xml" }
    turbine = { module = "app.cash.turbine:turbine", version = "1.2.1", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/app/cash/turbine/turbine/maven-metadata.xml" }

    # NPM dependencies
    prettier = { module = "prettier", version = "3.9.9", type = "npm", url = "https://registry.npmjs.org/prettier" }
    prettierPluginXml = { module = "@prettier/plugin-xml", version = "3.4.2", type = "npm", url = "https://registry.npmjs.org/@prettier/plugin-xml" }
    """
      .trimIndent()

  // spotless:on

  val libraries: Map<String, Library> by lazy {
    data
      .lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
      .associate { line ->
        val (key, value) = line.split("=", limit = 2).map { it.trim() }
        val props =
          value
            .removePrefix("{")
            .removeSuffix("}")
            .split(",")
            .map { it.trim() }
            .associate {
              val (k, v) = it.split("=", limit = 2).map { s -> s.trim().removeSurrounding("\"") }
              k to v
            }
        key to
          Library(
            key = key,
            module = props.getValue("module"),
            version = props.getValue("version"),
            type = props.getValue("type"),
            url = props.getValue("url"),
          )
      }
  }

  fun get(key: String): Library = libraries[key] ?: error("Unknown dependency: $key")

  fun useLibrary(key: String): String = get(key).let { "${it.module}:${it.version}" }

  fun maven(): List<Library> = libraries.values.filter { it.type == "maven" }

  fun npm(): List<Library> = libraries.values.filter { it.type == "npm" }
}
