import io.github.mymx2.plugin.dyCreateVersionCatalogs
import io.github.mymx2.plugin.dyIncludeProjects

pluginManagement { includeBuild("gradle/build-logic") }

plugins {
  id("io.github.mymx2.build")
  id("io.github.mymx2.plugin.dy.example.settings")
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = rootDir.name

dyCreateVersionCatalogs(
  mapOf(
    //    "bomLibs" to "gradle/bomLibs.versions.toml",
  )
)

dyIncludeProjects(
  mapOf(
    ":docs" to "docs",
    ":app" to "app",
    ":example-java" to "examples/example-java",
    ":example-kotlin" to "examples/example-kotlin",
    ":example-spring" to "examples/example-spring",
  )
)
