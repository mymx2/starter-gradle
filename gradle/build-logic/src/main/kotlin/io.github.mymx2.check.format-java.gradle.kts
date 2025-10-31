import io.github.mymx2.plugin.spotless.SpotlessConfig
import io.github.mymx2.plugin.spotless.SpotlessConfig.spotlessFileTree
import io.github.mymx2.plugin.spotless.SpotlessLicense
import io.github.mymx2.plugin.spotless.defaultStep
import io.github.mymx2.plugin.tasks.FileContentCheck

plugins { id("io.github.mymx2.check.format-base") }

val sources = spotlessFileTree().matching { include("**/*.java") }

spotless {
  java {
    defaultStep {
      removeUnusedImports()
      importOrder()
      formatAnnotations()
      cleanthat()
      palantirJavaFormat().style("GOOGLE").formatJavadoc(true)
    }
    target(sources)
    val spotlessLicenseHeader = SpotlessLicense.getComment(project)
    if (spotlessLicenseHeader.isNotBlank()) {
      licenseHeader(spotlessLicenseHeader)
    }
  }

  format("javaForbid") {
    SpotlessConfig.getForbidRegexList(project, "gradle/configs/spotless/forbid-regex.txt").forEach {
      addStep(it)
    }
    target(sources)
  }
}

tasks.named<FileContentCheck>("fileContentCheck") { sourceFiles = sources }
