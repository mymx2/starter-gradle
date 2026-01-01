import io.github.mymx2.plugin.spotless.SpotlessConfig
import io.github.mymx2.plugin.spotless.SpotlessConfig.spotlessFileTree
import io.github.mymx2.plugin.spotless.SpotlessLicense
import io.github.mymx2.plugin.spotless.defaultStep
import io.github.mymx2.plugin.tasks.FileContentCheck

plugins { id("io.github.mymx2.check.format-base") }

val sources: FileTree = spotlessFileTree().matching { include("**/*.kt") }

spotless {
  kotlin {
    defaultStep {
      ktfmt(SpotlessConfig.ktfmtVersion).googleStyle().configure { it.setRemoveUnusedImports(true) }
    }
    target(sources)
    val spotlessLicenseHeader = SpotlessLicense.getComment(project)
    if (spotlessLicenseHeader.isNotBlank()) {
      licenseHeader(spotlessLicenseHeader)
    }
  }

  format("kotlinForbid") {
    SpotlessConfig.getForbidRegexList(project, "gradle/configs/spotless/forbid-regex.txt").forEach {
      addStep(it)
    }
    target(sources)
  }
}

tasks.named<FileContentCheck>("fileContentCheck") { sourceFiles = sources }
