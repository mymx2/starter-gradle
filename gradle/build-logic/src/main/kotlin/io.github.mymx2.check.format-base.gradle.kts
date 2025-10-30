import io.github.mymx2.plugin.resetTaskGroup

plugins {
  id("com.diffplug.spotless")
  id("io.github.mymx2.base.lifecycle")
}

tasks.withType<JavaCompile>().configureEach {
  // When doing a 'qualityGate' run, make sure spotlessApply is done before doing compilation
  // and
  // other checks based on compiled code
  mustRunAfter(tasks.spotlessApply)
}

tasks.named("qualityCheck") { dependsOn(tasks.spotlessCheck) }

tasks.named("qualityGate") { dependsOn(tasks.spotlessApply) }

spotless {
  // Disable Gradle's check task from automatically running spotlessCheck
  isEnforceCheck = false
}

resetTaskGroup(Regex("spotless.*"), "others")
