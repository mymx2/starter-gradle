import com.profiletailors.plugin.dyIncludeProjects

pluginManagement { includeBuild("gradle/build-logic") }

plugins {
  id("com.profiletailors.build")
  id("com.profiletailors.build.feature.catalogs")
  id("com.profiletailors.plugin.dy.example.settings")
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = rootDir.name

dyIncludeProjects(
  mapOf(
    ":docs" to "docs",
    ":app" to "app",
    ":example-java" to "examples/example-java",
    ":example-kotlin" to "examples/example-kotlin",
    ":example-spring" to "examples/example-spring",
  )
)
