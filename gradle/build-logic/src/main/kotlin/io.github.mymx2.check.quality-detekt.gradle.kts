@file:Suppress("UnstableApiUsage", "detekt:SpreadOperator")

import dev.detekt.gradle.Detekt

plugins {
  // https://github.com/detekt/detekt
  // https://github.com/detekt/detekt/issues/7304#issuecomment-2740750935
  id("dev.detekt")
  id("io.github.mymx2.base.lifecycle")
}

tasks.named("qualityCheck") { dependsOn(tasks.detekt) }

// default excludes.
val defaultDetektExcludes = arrayOf("**/nocheck/**", "**/autogen/**", "**/generated/**")

tasks.withType<Detekt>().configureEach { exclude(*defaultDetektExcludes) }

val detektYml =
  layout.projectDirectory.file("configs/detekt/detekt.yml").asFile.takeIf { it.exists() }
    ?: isolated.rootProject.projectDirectory
      .file("gradle/configs/detekt/detekt.yml")
      .asFile
      .takeIf { it.exists() }

detekt {
  debug = false
  if (detektYml != null) {
    config.setFrom(detektYml)
    buildUponDefaultConfig = true
  }
}
