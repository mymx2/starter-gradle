@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.spotless.SortDependenciesStep
import io.github.mymx2.plugin.spotless.SpotlessLicense
import io.github.mymx2.plugin.spotless.applyProjectKtfmt
import io.github.mymx2.plugin.spotless.defaultStep

plugins { id("io.github.mymx2.check.format-base") }

spotless {
  kotlinGradle {
    defaultStep {
      addStep(SortDependenciesStep.create())
      applyProjectKtfmt()
    }
    target(
      isolated.projectDirectory.files("settings.gradle.kts", "build.gradle.kts", "build.gradle.kt")
    )
    val spotlessLicenseHeader = SpotlessLicense.getComment(project)
    if (spotlessLicenseHeader.isNotBlank()) {
      licenseHeader(spotlessLicenseHeader, "(^(?![\\/ ]\\*).*$)")
    }
  }
}
