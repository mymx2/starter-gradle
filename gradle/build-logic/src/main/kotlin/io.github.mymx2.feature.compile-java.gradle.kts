@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.environment.buildProperties
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import io.github.mymx2.plugin.versionFromCatalog
import java.nio.charset.StandardCharsets

plugins {
  java
  id("io.github.mymx2.base.lifecycle")
}

val jepEnablePreview = project.getPropOrDefault(LocalConfig.Props.JEP_ENABLE_PREVIEW).toBoolean()

val buildProperties = project.buildProperties()
val jdkVersion: String =
  buildProperties.getProperty("jdk", "").ifBlank {
    runCatching { versionFromCatalog("jdk") }.getOrNull().orEmpty()
  }

if (jdkVersion.isNotBlank()) {
  // https://docs.gradle.org/nightly/userguide/toolchains.html#comparison_table_for_setting_project_toolchains
  // Configure which JDK and Java version to build with.
  java { toolchain { languageVersion.set(JavaLanguageVersion.of(jdkVersion)) } }
}

// Configuration to make the build reproducible. This means we override settings that are, by
// default, platform dependent (e.g. different default encoding on Windows and Unix systems).
tasks.withType<JavaCompile>().configureEach {
  options.apply {
    // release = 21
    isFork = true
    encoding = StandardCharsets.UTF_8.name()
    if (jepEnablePreview) {
      compilerArgs.add("--enable-preview") // 启用预览特性
    }
    // javac
    compilerArgs.add("-parameters") // 保留方法参数名，方便反射/框架
    compilerArgs.add("-implicit:none") // 禁止编译未显式指定的源码
    compilerArgs.add("-Werror") // 把所有警告当成错误
    compilerArgs.add("-Xlint:all") // 打开全部lint警告
  }
}

// Tweak 'lifecycle tasks': These are the tasks in the 'build' group that are used in daily
// development. Under normal circumstances, these should be all the tasks developers needs
// in their daily work.
tasks.named("qualityCheck") { dependsOn(tasks.withType<JavaCompile>()) }

tasks.named("qualityGate") { dependsOn(tasks.withType<JavaCompile>()) }
