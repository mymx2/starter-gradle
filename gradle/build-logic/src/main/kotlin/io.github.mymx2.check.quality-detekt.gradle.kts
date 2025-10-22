@file:Suppress("UnstableApiUsage", "detekt:SpreadOperator")

import dev.detekt.gradle.Detekt
import io.github.mymx2.plugin.GradleExtTool

plugins {
  // https://github.com/detekt/detekt
  // https://github.com/detekt/detekt/issues/7304#issuecomment-2740750935
  id("dev.detekt")
  id("io.github.mymx2.base.lifecycle")
}

tasks.named("qualityCheck") { dependsOn(tasks.detekt) }

tasks.withType<Detekt>().configureEach { exclude(*GradleExtTool.defaultExclude.toTypedArray()) }

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
