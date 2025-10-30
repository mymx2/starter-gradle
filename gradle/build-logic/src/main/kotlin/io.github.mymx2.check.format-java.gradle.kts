import io.github.mymx2.plugin.spotless.SpotlessConfig.spotlessFileTree
import io.github.mymx2.plugin.spotless.SpotlessLicense
import io.github.mymx2.plugin.spotless.defaultStep

plugins { id("io.github.mymx2.check.format-base") }

spotless {
  java {
    defaultStep {
      removeUnusedImports()
      importOrder()
      formatAnnotations()
      cleanthat()
      palantirJavaFormat().style("GOOGLE").formatJavadoc(true)
    }
    target(spotlessFileTree().include("**/*.java"))
    val spotlessLicenseHeader = SpotlessLicense.getComment(project)
    if (spotlessLicenseHeader.isNotBlank()) {
      licenseHeader(spotlessLicenseHeader)
    }
  }
}
