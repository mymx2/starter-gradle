import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  java
  id("io.github.mymx2.base.lifecycle")
}

tasks.withType<KotlinJvmCompile>().configureEach {
  jvmTargetValidationMode.set(JvmTargetValidationMode.WARNING)
}

tasks.withType<JavaCompile>().configureEach {
  options.apply {
    // kotlinc
    compilerArgs.add("-java-parameters") // 保留参数名
    compilerArgs.add("-Xjvm-default=all") // Java8 接口默认实现
    compilerArgs.add("-Xjsr305=strict") // 严格空安全
    compilerArgs.add("-Xemit-jvm-type-annotations") // 在泛型上保留类型注解
  }
}
