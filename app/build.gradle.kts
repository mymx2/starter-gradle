@file:Suppress("UnstableApiUsage")

plugins {
  id("io.github.mymx2.module.kotlin")
  id("io.github.mymx2.module.app")
  id("io.github.mymx2.feature.test-end2end")
}

application { mainClass.set("io.github.mymx2.app.Application") }

dependencies {
  implementation("org.slf4j:slf4j-api")
  runtimeOnly(libs.slf4jSimple)

  testImplementation(libs.junitJupiterApi)
}

dependencies { mockApiImplementation(projects.app) }

dependencies {
  testEndToEndImplementation(projects.app) { capabilities { requireFeature("mock-api") } }
  testEndToEndApi(libs.junitJupiterApi)
}
