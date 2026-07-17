@file:Suppress("UnstableApiUsage", "detekt:SpreadOperator")

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.plugin.getSupportedKotlinVersion
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault

plugins {
  // https://github.com/detekt/detekt
  id("dev.detekt")
  id("io.github.mymx2.base.lifecycle")
}

val gradleKotlinVersion = embeddedKotlinVersion
val detektKotlinVersion = getSupportedKotlinVersion()
val enableDetekt = detektKotlinVersion >= gradleKotlinVersion

tasks.named("qualityCheck") { dependsOn(tasks.detekt) }

tasks.named("qualityGate") { dependsOn(tasks.detekt) }

// default excludes.
val defaultDetektExcludes = arrayOf("**/nocheck/**", "**/autogen/**", "**/generated/**")

tasks.withType<Detekt>().configureEach {
  enabled = true
  exclude(*defaultDetektExcludes)
}

// [perf] The detekt plugin wires `detekt` directly into `check` (in addition to
// `qualityCheck`/`qualityGate`). Gating `check -> qualityCheck` alone is therefore not
// enough to keep detekt out of the local dev loop. When SKIP_QUALITY is set, disable all
// detekt tasks so they drop out of the task graph entirely. CI keeps SKIP_QUALITY=false,
// so qualityGate / qualityCheck still run detekt there.
if (project.getPropOrDefault(LocalConfig.Props.SKIP_QUALITY).toBoolean()) {
  tasks.withType<Detekt>().configureEach { enabled = false }
}

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
