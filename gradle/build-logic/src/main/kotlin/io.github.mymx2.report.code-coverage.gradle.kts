@file:Suppress("UnstableApiUsage")

plugins {
  java
  `jacoco-report-aggregation`
  // https://kotlin.github.io/kotlinx-kover/gradle-plugin/
  id("org.jetbrains.kotlinx.kover")
  id("io.github.mymx2.base.jpms-modules")
  id("io.github.mymx2.base.lifecycle")
}

kover {
  useJacoco()
  reports {
    filters {
      excludes {
        classes("**.internal.**")
        classes("**.codegen.**")
      }
    }
  }
}

// Make aggregation "classpath" use the platform for versions (gradle/versions)
configurations.aggregateCodeCoverageReportResults { extendsFrom(configurations["internal"]) }

// Integrate testEndToEnd results into the aggregated UNIT_TEST coverage results
tasks.testCodeCoverageReport {
  reports.html.outputLocation = reporting.baseDirectory.dir("coverage")
  reports.xml.outputLocation = reporting.baseDirectory.file("coverage/coverage.xml")
  executionData.from(
    configurations.aggregateCodeCoverageReportResults
      .get()
      .incoming
      .artifactView {
        withVariantReselection()
        attributes {
          attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
          attribute(TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE, objects.named("testEndToEnd"))
          attribute(
            VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
            objects.named(VerificationType.JACOCO_RESULTS),
          )
          attribute(
            ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
            ArtifactTypeDefinition.BINARY_DATA_TYPE,
          )
        }
      }
      .files
  )
}

// Generate report when running 'check'
tasks.check { dependsOn(tasks.testCodeCoverageReport) }
