plugins {
  java
  id("io.github.mymx2.base.jpms-modules")
  id("io.github.mymx2.base.identity")
  id("io.github.mymx2.base.lifecycle")
  id("io.github.mymx2.check.format-gradle")
  id("io.github.mymx2.report.sbom")
  id("io.github.mymx2.report.test")
  id("io.github.mymx2.report.code-coverage")
}

dependencies { implementation(projects.app) }
