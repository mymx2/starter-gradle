@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.DefaultProjects
import io.github.mymx2.plugin.spotless.SpotlessConfig.spotlessFileTree
import io.github.mymx2.plugin.spotless.SpotlessLicense
import io.github.mymx2.plugin.spotless.defaultStep

plugins { id("io.github.mymx2.check.format-base") }

// only root project
if (path == ":") {
  val buildLogic = DefaultProjects.buildLogic
  val subProjects = listOf(buildLogic, DefaultProjects.versions, DefaultProjects.aggregation)

  spotless {
    val ktAndKtsFiles =
      spotlessFileTree("${buildLogic}/src").apply { include("**/*.kt", "**/*.gradle.kts") }

    val spotlessLicenseHeader = SpotlessLicense.getComment(project)

    kotlinGradle {
      defaultStep { ktfmt().googleStyle().configure { it.setRemoveUnusedImports(true) } }
      target(
        isolated.projectDirectory.files(
          "settings.gradle.kts",
          "build.gradle.kts",
          "${buildLogic}/settings.gradle.kts",
          subProjects.map { "${it}/build.gradle.kts" }.toTypedArray(),
        ),
        ktAndKtsFiles.matching { include("**/*.gradle.kts") },
      )
      if (spotlessLicenseHeader.isNotBlank()) {
        licenseHeader(spotlessLicenseHeader, "(^(?![\\/ ]\\*).*$)")
      }
    }
    kotlin {
      defaultStep { ktfmt().googleStyle().configure { it.setRemoveUnusedImports(true) } }
      target(ktAndKtsFiles.matching { include("**/*.kt") })
      if (spotlessLicenseHeader.isNotBlank()) {
        licenseHeader(spotlessLicenseHeader)
      }
    }

    // prettier formatting is handled by io.github.mymx2.check.format-prettier (root project)
  }
}
