@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import io.github.mymx2.plugin.repo.RepositoryConfig

val enableProxyRepo = settings.getPropOrDefault(LocalConfig.Props.ENABLE_PROXY_REPO).toBoolean()

pluginManagement {
  repositories {
    // mavenLocal()
    if (enableProxyRepo) {
      // https://maven.aliyun.com/mvn/guide
      maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    }
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
    // mavenLocal()
    if (enableProxyRepo) {
      // https://mirrors.cloud.tencent.com
      maven { setUrl("https://mirrors.cloud.tencent.com/nexus/repository/maven-public") }
      // https://maven.aliyun.com/mvn/guide
      maven { setUrl("https://maven.aliyun.com/repository/public") }
    }
    // https://status.maven.org/
    mavenCentral() // https://repo1.maven.org/maven2
    maven {
      setUrl("https://central.sonatype.com/repository/maven-snapshots/")
      mavenContent { snapshotsOnly() }
      content { includeVersionByRegex(".*", ".*", ".*-SNAPSHOT") }
    }
    maven {
      setUrl("https://jitpack.io")
      content { includeGroupByRegex("com\\.github.*") }
    }
    maven {
      setUrl("https://maven.youzanyun.com/repository/maven-releases")
      // https://docs.gradle.org/current/userguide/filtering_repository_content.html
      content { includeGroupByRegex("com.youzan.*") }
    }
    google {
      content {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    gradlePluginPortal()
    /* for 'com.github.node-gradle.node' plugin. https://nodejs.org/dist */
    ivy("https://npmmirror.com/mirrors/node") {
      // https://docs.gradle.org/nightly/userguide/how_to_resolve_specific_artifacts.html
      patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
      metadataSources { artifact() }
      content { includeModule("org.nodejs", "node") }
    }
  }
}

val priRepos = RepositoryConfig.getPrivateRepositories(providers)

dependencyResolutionManagement {
  repositories {
    priRepos.forEach {
      val pass = it.password
      if (pass.isNotBlank()) {
        maven(it.url) {
          name = it.name
          credentials {
            username = it.username
            password = pass
          }
        }
      }
    }
  }
}
