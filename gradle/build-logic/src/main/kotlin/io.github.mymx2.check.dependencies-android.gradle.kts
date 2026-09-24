@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.gradle.configureDependencyLockingDefaults
import io.github.mymx2.plugin.gradle.registerLockfileTasks

plugins {
  id("io.fuchs.gradle.classpath-collision-detector")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.base.jvm-conflict-android")
}

val isCI = EnvAccess.isCi(providers)

configureDependencyLockingDefaults()

configurations {
  // AGP 配置名模式：devDebugRuntimeClasspath / prodReleaseCompileClasspath 等，用后缀匹配
  matching { it.name.endsWith("RuntimeClasspath") }
    .configureEach {
      resolutionStrategy { activateDependencyLocking() }
    }
  matching { it.name.endsWith("CompileClasspath") }
    .configureEach {
      val runtimeConfig =
        configurations.findByName(name.replace("CompileClasspath", "RuntimeClasspath"))
      if (runtimeConfig != null) {
        shouldResolveConsistentlyWith(runtimeConfig)
      }
    }
}

registerLockfileTasks()
