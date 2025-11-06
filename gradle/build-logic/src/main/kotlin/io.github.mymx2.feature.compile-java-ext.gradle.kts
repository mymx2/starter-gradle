import io.github.mymx2.plugin.gradle.eagerSharedCache

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
      project.eagerSharedCache("gitCommitId") {
        providers
          .exec {
            isIgnoreExitValue = true
            commandLine("git", "rev-parse", "HEAD")
            workingDir = layout.projectDirectory.asFile
          }
          .standardOutput
          .asText
          .get()
          .let { it.trim().ifBlank { "unknown" } }
      }

    property("git.commit.id", gitCommit)
    property(
      "git.commit.id.abbrev",
      gitCommit.let { if (it.length >= 7) it.substring(0, 7) else it },
    )

    destinationFile = layout.buildDirectory.file("generated/git/git.properties")
  }

tasks.processResources { from(writeGitProperties) }

// ignore the content of 'git.properties' when using a classpath as task input
normalization.runtimeClasspath { ignore("git.properties") }
