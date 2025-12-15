@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault

plugins {
  id("io.github.mymx2.module.kotlin")
  id("io.github.mymx2.module.app")
  id("io.github.mymx2.feature.test-end2end")
  id("io.github.mymx2.feature.benchmark") apply false
}

application { mainClass.set("io.github.mymx2.app.Application") }

dependencies {
  implementation(libs.slf4jApi)
  runtimeOnly(libs.slf4jSimple)

  testImplementation(libs.junitJupiterApi)
}

dependencies { mockApiImplementation(projects.app) }

dependencies {
  testEndToEndImplementation(projects.app) { capabilities { requireFeature("mock-api") } }
  testEndToEndApi(libs.junitJupiterApi)
}

val isJmh = project.getPropOrDefault(LocalConfig.Props.IS_JMH).toBoolean()

if (isJmh) {
  apply(plugin = "io.github.mymx2.feature.benchmark")
}
