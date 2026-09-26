@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.gradle.CONSISTENT_RESOLUTION_ATTRIBUTE
import io.github.mymx2.plugin.gradle.configureSharedResolution

plugins { id("org.gradlex.jvm-dependency-conflict-resolution") }

// Android 适配版：不用 sourceSets（Java 插件 API），直接匹配 AGP 的 variant 配置名后缀。
// jvm-dependency-conflict-resolution 插件本身不依赖 java 插件，可在 AGP 模块上使用。
configurations.create(
  "allDependencies",
  Action {
    // 纯 declarable bucket：只 extendsFrom 收集依赖供 consistent resolution 读取，
    // 不可被消费也不可被解析。若 isCanBeConsumed=true 且盖 category=library 等 variant 属性，
    // 会让它对任何 project(...) 解析成为合法候选，挤掉 AGP 9.5.0-alpha 缺 category 属性的
    // main runtimeElements，导致单测 runtime classpath 完全缺 main classes（NoClassDefFoundError）。
    isCanBeConsumed = false
    isCanBeResolved = false
    // AGP 配置名模式：devDebugImplementation / prodReleaseApi 等，用后缀匹配
    val target = this
    configurations
      .matching {
        it.name.endsWith("Implementation") ||
          it.name.endsWith("CompileOnly") ||
          it.name.endsWith("RuntimeOnly") ||
          it.name.endsWith("AnnotationProcessor") ||
          it.name == "implementation" ||
          it.name == "compileOnly" ||
          it.name == "runtimeOnly"
      }
      .configureEach { target.extendsFrom(this) }
  },
)

configureSharedResolution()

// AGP 没有 mainRuntimeClasspath，用后缀匹配所有 runtime classpath
configurations
  .matching { it.name.endsWith("RuntimeClasspath") }
  .configureEach { attributes.attribute(CONSISTENT_RESOLUTION_ATTRIBUTE, "global") }
