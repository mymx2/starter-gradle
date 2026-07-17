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
  if (!skipCoverage) {
    dependsOn(tasks.jacocoTestReport)
  }
}

if (skipCoverage) {
  // Disable the jacoco java agent so test execution is not instrumented (the main
  // per-test overhead). The report tasks are simply no longer wired into `check`.
  tasks.withType<Test>().configureEach {
    extensions.findByType(JacocoTaskExtension::class)?.isEnabled = false
  }
}
