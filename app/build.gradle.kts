@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault

plugins {
  id("io.github.mymx2.module.kotlin")
  id("io.github.mymx2.module.app")
  id("io.github.mymx2.feature.test-end2end")
  id("io.github.mymx2.feature.benchmark") apply false
}

val isJmh = project.getPropOrDefault(LocalConfig.Props.IS_JMH).toBoolean()

if (isJmh) {
  apply(plugin = "io.github.mymx2.feature.benchmark")
}

application { mainClass.set("io.github.mymx2.app.Application") }

dependencies {
  implementation("org.slf4j:slf4j-api")
  runtimeOnly("org.slf4j:slf4j-simple")
}

dependencies { mockApiImplementation(projects.app) }

dependencies {
  testEndToEndImplementation(projects.app) { capabilities { requireFeature("mock-api") } }
}
