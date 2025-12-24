import io.fuchs.gradle.collisiondetector.DetectCollisionsTask
import io.github.mymx2.plugin.gradle.eagerSharedCache
import io.github.mymx2.plugin.utils.SemVerUtils
import org.gradle.kotlin.dsl.withType

plugins {
  java
  id("io.freefair.lombok")
  id("io.github.mymx2.feature.openrewrite")
  id("io.github.mymx2.check.quality-nullaway")
}

val writeGitProperties =
  tasks.register<WriteProperties>("writeGitProperties") {
    property("git.build.version", project.version)

    val gitCommit =
      System.getenv("GITHUB_SHA")
        ?: project.eagerSharedCache("gitCommitId") {
          SemVerUtils.gitBuildMetadata(providers, layout)
        }

    property("git.commit.id", gitCommit)
    property("git.commit.id.abbrev", gitCommit.take(7))

    destinationFile = layout.buildDirectory.file("generated/git/git.properties")
  }

tasks.processResources { from(writeGitProperties) }

// ignore the content of 'git.properties' when using a classpath as task input
normalization.runtimeClasspath { ignore("git.properties") }

tasks.withType<DetectCollisionsTask>().configureEach { collisionFilter.exclude("git.properties") }
