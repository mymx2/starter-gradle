import io.github.mymx2.plugin.spotless.SpotlessConfig
import io.github.mymx2.plugin.spotless.SpotlessConfig.spotlessFileTree
import io.github.mymx2.plugin.spotless.SpotlessLicense
import io.github.mymx2.plugin.spotless.defaultStep
import io.github.mymx2.plugin.spotless.nodeFile

plugins { id("io.github.mymx2.check.format-base") }

spotless {
  shell { defaultStep { shfmt() } }

  val misc = listOf("**/*.md", "**/*.json", "**/*.json5", "**/*.yaml", "**/*.yml")
  val xml = listOf("**/*.xml")
  val targetFiles = spotlessFileTree().apply { include(misc + xml) }

  format("prettierXml") {
    defaultStep {
      prettier(SpotlessConfig.prettierDevDependenciesWithXmlPlugin)
        .nodeExecutable(nodeFile().orNull)
        .config(
          mapOf(
            "plugins" to listOf("@prettier/plugin-xml"),
            "parser" to "xml",
            "useTabs" to false,
            "tabWidth" to 2,
          )
        )
    }
    target(targetFiles.matching { include(xml) })
    val spotlessLicenseHeader = SpotlessLicense.getXml(project)
    if (spotlessLicenseHeader.isNotBlank()) {
      licenseHeader(spotlessLicenseHeader, "(<[^!?])")
    }
  }
  format("prettierMisc") {
    defaultStep {
      prettier(SpotlessConfig.prettierDevDependencies).nodeExecutable(nodeFile().orNull)
    }
    target(targetFiles.matching { include(misc) })
  }
}
