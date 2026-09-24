@file:Suppress("UnstableApiUsage")

import PluginHelpers.findToolConfig
import PluginHelpers.libsOrInternal
import com.github.spotbugs.snom.SpotBugsTask

plugins {
  java
  // https://plugins.gradle.org/plugin/com.github.spotbugs
  id("com.github.spotbugs")
  id("io.github.mymx2.base.lifecycle")
}

dependencies {
  compileOnly(libsOrInternal("spotbugsAnnotations"))
}

// auto bind to checks task:
// https://spotbugs.readthedocs.io/en/latest/gradle.html#tasks-introduced-by-this-gradle-plugin
afterEvaluate {
  // https://spotbugs.readthedocs.io/en/latest/gradle.html#tasks-introduced-by-this-gradle-plugin
  tasks.named("qualityCheck") { dependsOn(tasks.spotbugsMain, tasks.spotbugsTest) }
}

val excludeFilterFile = findToolConfig("spotbugs", "spotbugs.xml")

spotbugs {
  ignoreFailures = false
  excludeFilter = excludeFilterFile
  // reportsDir = reporting.baseDirectory.dir("spotbugs")
}

tasks.withType<SpotBugsTask>().configureEach {
  reports.create(
    "html",
    Action {
      required = true
      setStylesheet("fancy-hist.xsl")
    },
  )
}
