plugins {
  id("io.github.mymx2.base.identity")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.base.jvm-conflict")
  id("io.github.mymx2.feature.use-all-catalog-versions")
  id("io.github.mymx2.check.format-gradle")
  id("io.github.mymx2.tools.check-version")
}

javaPlatform.allowDependencies()

dependencies { api(platform(libs.slf4jBom)) }

// Reject versions that should not be upgraded beyond a certain point.
// This makes Dependabot PR builds fail that attempt to update these.
// https://docs.gradle.org/nightly/userguide/dependency_constraints.html
dependencies.constraints {}
