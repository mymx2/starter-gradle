plugins {
  java
  id("io.freefair.lombok")
  id("io.github.mymx2.feature.openrewrite")
  id("io.github.mymx2.check.quality-nullaway")
}

val writeGitProperties =
  tasks.register<WriteProperties>("writeGitProperties") {
    property("git.build.version", project.version)
    property(
      "git.commit.id",
      runCatching {
          providers
            .exec {
              commandLine("git", "rev-parse", "HEAD")
              workingDir = layout.projectDirectory.asFile
            }
            .standardOutput
            .asText
            .map { it.trim() }
        }
        .getOrElse { "unknown" },
    )
    property(
      "git.commit.id.abbrev",
      runCatching {
          providers
            .exec {
              commandLine("git", "rev-parse", "HEAD")
              workingDir = layout.projectDirectory.asFile
            }
            .standardOutput
            .asText
            .map { it.trim().substring(0, 7) }
        }
        .getOrElse { "unknown" },
    )

    destinationFile = layout.buildDirectory.file("generated/git/git.properties")
  }

tasks.processResources { from(writeGitProperties) }

// ignore the content of 'git.properties' when using a classpath as task input
normalization.runtimeClasspath { ignore("git.properties") }
