plugins {
  java
  id("io.freefair.lombok")
  id("io.github.mymx2.feature.openrewrite")
  id("io.github.mymx2.check.quality-nullaway")
}

tasks.processResources { include("**/*.xml") }
