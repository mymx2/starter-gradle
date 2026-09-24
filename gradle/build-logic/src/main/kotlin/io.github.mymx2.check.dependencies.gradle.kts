@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.gradle.configureDependencyLockingDefaults
import io.github.mymx2.plugin.gradle.registerLockfileTasks

plugins {
  java
  id("io.fuchs.gradle.classpath-collision-detector")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.base.jvm-conflict")
}

val isCI = EnvAccess.isCi(providers)

// https://docs.gradle.org/nightly/userguide/dependency_locking.html
configureDependencyLockingDefaults()

configurations {
  runtimeClasspath { resolutionStrategy { activateDependencyLocking() } }
  compileClasspath { shouldResolveConsistentlyWith(runtimeClasspath.get()) }
  // consistentResolution 机制会创建 mainRuntimeClasspath 独立解析依赖版本，
  // 若不与其对齐，其解析结果会以 strictly 约束传递给 runtimeClasspath，
  // 与 lockfile 锁定的版本产生不可调和的冲突（如 dokka 带入的旧版 coroutines/jackson）。
  // 对 mainRuntimeClasspath 也激活 dependency locking，强制它使用 lockfile 中的版本，
  // 确保 consistentResolution 产生的 strictly 约束与 lockfile 一致。
  matching { it.name == "mainRuntimeClasspath" }
    .configureEach {
      resolutionStrategy { activateDependencyLocking() }
    }
}

registerLockfileTasks()
