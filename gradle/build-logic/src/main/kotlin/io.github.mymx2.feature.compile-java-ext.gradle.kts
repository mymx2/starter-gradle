import io.github.mymx2.plugin.sourceFolder

plugins {
  java
  id("io.freefair.lombok")
  id("io.github.mymx2.feature.openrewrite")
  id("io.github.mymx2.check.quality-nullaway")
}

tasks.processResources { from(sourceFolder()) { include("**/*.xml") } }
