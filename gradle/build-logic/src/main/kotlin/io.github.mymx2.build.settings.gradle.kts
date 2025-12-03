@file:Suppress("UnstableApiUsage")

// Allow local projects to be referred to by accessor
// https://doc.qzxdp.cn/gradle/8.1.1/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// https://docs.gradle.org.cn/current/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

plugins {
  id("org.gradlex.java-module-dependencies")
  id("io.github.mymx2.build.feature.repositories")
  id("io.github.mymx2.build.feature.build-cache")
  id("io.github.mymx2.build.report.develocity")
  id("io.github.mymx2.build.feature.project-structure")
}

gradle.lifecycle.beforeProject {
  // only root project
  if (this.path == ":") {
    // lifecycle tasks
    apply(plugin = "io.github.mymx2.base.lifecycle")
    // git hook
    apply(plugin = "io.github.mymx2.feature.git-hook")
    // spotless format
    apply(plugin = "io.github.mymx2.check.format-gradle-root")
    // action lint
    apply(plugin = "io.github.mymx2.check.actionlint-root")
    // dependency check
    apply(plugin = "io.github.mymx2.check.dependencies-root")
  }
}

fun Settings.cachesRemoveUnusedEntriesAfterDays() {
  gradle.beforeSettings {
    // https://docs.gradle.org/nightly/userguide/directory_layout.html#dir:gradle_user_home:configure_cache_cleanup
    caches {
      releasedWrappers.setRemoveUnusedEntriesAfterDays(30)
      snapshotWrappers.setRemoveUnusedEntriesAfterDays(7)
      downloadedResources.setRemoveUnusedEntriesAfterDays(30)
      createdResources.setRemoveUnusedEntriesAfterDays(7)
      buildCache.setRemoveUnusedEntriesAfterDays(7)
    }
  }
}
