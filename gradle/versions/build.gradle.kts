plugins {
  id("io.github.mymx2.base.jpms-modules")
  id("io.github.mymx2.base.identity")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.feature.use-all-catalog-versions")
  id("io.github.mymx2.check.format-gradle")
  id("io.github.mymx2.tools.check-version")
}

// Reject versions that should not be upgraded beyond a certain point.
// This makes Dependabot PR builds fail that attempt to update these.
// https://docs.gradle.org/nightly/userguide/dependency_constraints.html
dependencies.constraints {}

tasks.checkVersionConsistency { unUsedExcludes.add("org.junit.jupiter:junit-jupiter-api") }
