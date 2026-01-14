@file:Suppress("UnstableApiUsage")

rootProject.name = "dy-gradle-plugin"

run {
  enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
  enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral() // https://repo1.maven.org/maven2
    maven {
      setUrl("https://central.sonatype.com/repository/maven-snapshots/")
      mavenContent { snapshotsOnly() }
      content { includeVersionByRegex(".*", ".*", ".*-SNAPSHOT(?:\\+.*)?") }
    }
    google {
      content {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
  }
}

dependencyResolutionManagement {
  versionCatalogs { create("libs", Action { from(files("../libs.versions.toml")) }) }
}

plugins {
  val m2Version = "1.3.3"
  id("io.github.mymx2.build.feature.repositories").version(m2Version)
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
