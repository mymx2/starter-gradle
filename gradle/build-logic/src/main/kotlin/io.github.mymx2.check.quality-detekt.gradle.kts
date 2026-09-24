@file:Suppress("UnstableApiUsage", "detekt:SpreadOperator")

import PluginHelpers.findToolConfig
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.plugin.getSupportedKotlinVersion
import io.github.mymx2.plugin.DefaultExcludes

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
val defaultDetektExcludes = DefaultExcludes.GLOB.toTypedArray()

tasks.withType<Detekt>().configureEach {
  enabled = true
  exclude(*defaultDetektExcludes)
}

// [perf] The detekt plugin wires `detekt` directly into `check` (in addition to
// `qualityCheck`/`qualityGate`). Gating `check -> qualityCheck` alone is therefore not
// enough to keep detekt out of the local dev loop. When SKIP_QUALITY is set, disable all
// detekt tasks so they drop out of the task graph entirely. CI keeps SKIP_QUALITY=false,
// so qualityGate / qualityCheck still run detekt there.
if (skipFlags.quality) {
  tasks.withType<Detekt>().configureEach { enabled = false }
}

val detektYml = findToolConfig("detekt", "detekt.yml")

detekt {
  debug = false
  if (detektYml != null) {
    config.setFrom(detektYml)
    buildUponDefaultConfig = true
  }
}
