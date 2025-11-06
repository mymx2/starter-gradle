import io.github.mymx2.plugin.environment.KotlinCompilerOptions
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
  options.apply { KotlinCompilerOptions.default.forEach { compilerArgs.add(it) } }
}
