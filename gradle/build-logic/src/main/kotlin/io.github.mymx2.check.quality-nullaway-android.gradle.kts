@file:Suppress("UnstableApiUsage")

import PluginHelpers.libsOrInternal
import io.github.mymx2.plugin.quality.configureErrorProneWithNullaway
import net.ltgt.gradle.errorprone.errorprone

plugins {
  // 不 apply java 插件——AGP 模块自带 JavaCompile 任务
  // https://github.com/tbroyer/gradle-errorprone-plugin
  id("net.ltgt.errorprone")
  // https://github.com/tbroyer/gradle-nullaway-plugin
  id("net.ltgt.nullaway")
  id("io.github.mymx2.base.lifecycle")
}

dependencies {
  // AGP 模块没有 java 插件的 compileOnly 配置，用字符串形式引用（AGP 内置 compileOnly）
  "compileOnly"(libsOrInternal("jspecify"))
  // https://github.com/PicnicSupermarket/error-prone-support/tree/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns
  // https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/
  listOf("errorProneCore", "errorProneContrib", "refasterRunner", "nullaway").forEach { name ->
    errorprone(libsOrInternal(name))
  }
}

// AGP 模块不 apply java 插件，net.ltgt.errorprone 不会自动把 errorprone 配置挂到 annotationProcessor 上。
// 手动桥接：AGP 为每个 variant 创建 <variant>AnnotationProcessorClasspath，
// 必须等 AGP 完成 variant 配置后这些 configuration 才存在。这里在 AGP 插件应用完毕后再桥接，
// 等价于 JVM 模块上 ErrorPronePlugin 内部的 sourceSet.annotationProcessor.extendsFrom(errorprone)。
afterEvaluate {
  val errorproneConf = configurations.getByName("errorprone")
  configurations.forEach { conf ->
    if (conf.name.endsWith("AnnotationProcessorClasspath")) {
      conf.extendsFrom(errorproneConf)
    }
  }
}

configureErrorProneWithNullaway()

// AGP 的 test 编译任务名是 compile{Variant}UnitTestJavaWithJavac（main 为 compile{Variant}JavaWithJavac）。
// 与 JVM 版 compileTestJava 同款语义：test 编译禁用 errorprone。
// 注意 AGP 任务名全部以 "Javac" 结尾（不是 "Java"），按 endsWith("UnitTestJavaWithJavac") 匹配。
tasks
  .matching { it.name.startsWith("compile") && it.name.endsWith("UnitTestJavaWithJavac") }
  .configureEach {
    if (this is JavaCompile) {
      options.errorprone { isEnabled = false }
    }
  }

/*
    * Add other Error Prone flags here. See:
    * - https://github.com/tbroyer/gradle-errorprone-plugin#configuration
    * - https://errorprone.info/docs/flags
    * - https://github.com/ben-manes/caffeine/blob/master/gradle/plugins/src/main/kotlin/quality/errorprone.caffeine.gradle.kts
    */
