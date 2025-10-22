@file:Suppress("UnstableApiUsage")

import com.github.spotbugs.snom.SpotBugsTask

plugins {
  // https://plugins.gradle.org/plugin/com.github.spotbugs
  id("com.github.spotbugs")
  id("io.github.mymx2.base.lifecycle")
}

// auto bind to checks task:
// https://spotbugs.readthedocs.io/en/latest/gradle.html#tasks-introduced-by-this-gradle-plugin
afterEvaluate {
  // https://spotbugs.readthedocs.io/en/latest/gradle.html#tasks-introduced-by-this-gradle-plugin
  tasks.named("qualityCheck") {
    dependsOn(tasks.named("spotbugsMain"), tasks.named("spotbugsTest"))
  }
}

val excludeFilterFile =
  layout.projectDirectory.file("configs/spotbugs/spotbugs.xml").asFile.takeIf { it.exists() }
    ?: isolated.rootProject.projectDirectory
      .file("gradle/configs/spotbugs/spotbugs.xml")
      .asFile
      .takeIf { it.exists() }

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
