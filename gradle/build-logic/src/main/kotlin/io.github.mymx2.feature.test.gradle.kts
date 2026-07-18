@file:Suppress("UnstableApiUsage")

import PluginHelpers.useJUnitJupiterM2
import io.github.mymx2.plugin.environment.buildProperties
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import java.nio.charset.StandardCharsets
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

plugins {
  java
  // https://docs.gradle.org/nightly/userguide/jacoco_plugin.html
  jacoco
}

val jepEnablePreview = project.getPropOrDefault(LocalConfig.Props.JEP_ENABLE_PREVIEW).toBoolean()
// [perf] 本地开发时跳过被 @Tag("integration") 标记的集成测试(如 example-spring 的 @SpringBootTest)，
// 这类测试启动重(上下文 + forked JVM)。SKIP_INTEGRATION 单独控制，也随 SKIP_ALL_LOCAL 一并跳过；
// 默认 false 保持原行为(CI 仍运行它们)。推送前需跑一次完整 check 补回。
val skipIntegration = project.getPropOrDefault(LocalConfig.Props.SKIP_INTEGRATION).toBoolean()
val skipAllLocal = project.getPropOrDefault(LocalConfig.Props.SKIP_ALL_LOCAL).toBoolean()

val buildProperties = project.buildProperties()
val junitJupiterM2Enabled =
  buildProperties
    .getProperty("junit.jupiter.m2.enabled", "")
    .ifBlank { project.getPropOrDefault(LocalConfig.Props.JUNIT_JUPITER_M2_ENABLED) }
    .toBoolean()

testing {
  suites {
    named<JvmTestSuite>("test") {
      targets.configureEach {
        // Use JUnit 5 as test framework
        useJUnitJupiter()

        // Configure details for test executions directly on 'Test' task
        testTask.configure {
          group = "verification"
          maxParallelForks = 4
          testLogging.showStandardStreams = true
          maxHeapSize = "1g"
          // Enable dynamic agent loading for tests - eg: Mockito, ByteBuddy
          jvmArgs(
            mutableListOf("-XX:+EnableDynamicAgentLoading").also {
              if (jepEnablePreview) {
                it.add("--enable-preview")
              }
            }
          )
          systemProperty("file.encoding", StandardCharsets.UTF_8.name())
          if (skipIntegration || skipAllLocal) {
            // 本地循环排除 @Tag("integration") 的集成测试(如 @SpringBootTest)，保留单元/其他测试
            useJUnitPlatform { excludeTags("integration") }
          }
        }
      }
    }
  }
}

if (junitJupiterM2Enabled) {
  useJUnitJupiterM2()
}

configurations.testCompileOnly { extendsFrom(configurations.compileOnly.get()) }

// [perf] Decouple jacoco coverage from the local dev loop.
// By default (SKIP_COVERAGE=false) `check` still depends on `jacocoTestReport`,
// preserving the original behavior. Set SKIP_COVERAGE=true ... for fast local builds.
val skipCoverage = project.getPropOrDefault(LocalConfig.Props.SKIP_COVERAGE).toBoolean()

tasks.check {
  if (!skipCoverage && !skipAllLocal) {
    dependsOn(tasks.jacocoTestReport)
  }
}

if (skipCoverage || skipAllLocal) {
  // Disable the jacoco java agent so test execution is not instrumented (the main
  // per-test overhead). The report tasks are simply no longer wired into `check`.
  tasks.withType<Test>().configureEach {
    extensions.findByType(JacocoTaskExtension::class)?.isEnabled = false
  }
}
