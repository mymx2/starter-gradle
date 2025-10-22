@file:Suppress("UnstableApiUsage")

rootProject.name = "dy-gradle-plugin"

run {
  // Allow local projects to be referred to by accessor
  // https://doc.qzxdp.cn/gradle/8.1.1/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
  enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
  // https://docs.gradle.org.cn/current/userguide/configuration_cache.html#config_cache:stable
  enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
}

pluginManagement {
  repositories {
    gradlePluginPortal()
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
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
  }

  versionCatalogs { create("libs", Action { from(files("../libs.versions.toml")) }) }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }
