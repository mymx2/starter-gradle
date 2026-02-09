@file:Suppress("UnstableApiUsage")

import PluginHelpers.m2JvmTestSuite
import io.github.mymx2.plugin.environment.buildProperties
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import java.nio.charset.StandardCharsets

plugins {
  java
  // https://docs.gradle.org/nightly/userguide/jacoco_plugin.html
  jacoco
}

val jepEnablePreview = project.getPropOrDefault(LocalConfig.Props.JEP_ENABLE_PREVIEW).toBoolean()

val buildProperties = project.buildProperties()
val m2JvmTestSuiteEnabled: String =
  project.getPropOrDefault(LocalConfig.Props.M2_JVM_TEST_SUITE_ENABLED).ifBlank {
    runCatching { buildProperties.getProperty("m2.jvm.test.suite.enabled", "") }
      .getOrNull()
      .orEmpty()
  }

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

if (m2JvmTestSuiteEnabled != "false") {
  m2JvmTestSuite()
}

configurations.testCompileOnly { extendsFrom(configurations.compileOnly.get()) }

tasks.check { dependsOn(tasks.jacocoTestReport) }
