import io.github.mymx2.plugin.spotless.SpotlessConfig
import io.github.mymx2.plugin.spotless.SpotlessConfig.spotlessFileTree
import io.github.mymx2.plugin.spotless.SpotlessLicense
import io.github.mymx2.plugin.spotless.defaultStep

plugins { id("io.github.mymx2.check.format-base") }

spotless {
  kotlin {
    defaultStep {
      ktfmt(SpotlessConfig.ktfmtVersion).googleStyle().configure { it.setRemoveUnusedImports(true) }
    }
    target(spotlessFileTree().include("**/*.kt"))
    val spotlessLicenseHeader = SpotlessLicense.getComment(project)
    if (spotlessLicenseHeader.isNotBlank()) {
      licenseHeader(spotlessLicenseHeader)
    }
  }
}
