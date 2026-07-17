@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import org.gradlex.javamodule.dependencies.tasks.ModuleDirectivesScopeCheck

plugins {
  java
  `jacoco-report-aggregation`
  // https://kotlin.github.io/kotlinx-kover/gradle-plugin/
  // id("org.jetbrains.kotlinx.kover")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.base.jvm-conflict")
}

// kover {
//  // default excludes.
//  val defaultKoverExcludes = arrayOf("**/nocheck/**", "**/autogen/**", "**/generated/**")
//  useJacoco()
//  reports { filters { excludes { defaultKoverExcludes.forEach { classes(it) } } } }
// }

tasks.withType<ModuleDirectivesScopeCheck> { enabled = false }

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
// [perf] Decouple jacoco coverage aggregation from the local dev loop.
// By default (SKIP_COVERAGE=false) `check` still depends on `testCodeCoverageReport`,
// preserving the original behavior. Set SKIP_COVERAGE=true ... for fast local builds.
val skipCoverage = project.getPropOrDefault(LocalConfig.Props.SKIP_COVERAGE).toBoolean()

tasks.check {
  if (!skipCoverage) {
    dependsOn(tasks.testCodeCoverageReport)
  }
}
