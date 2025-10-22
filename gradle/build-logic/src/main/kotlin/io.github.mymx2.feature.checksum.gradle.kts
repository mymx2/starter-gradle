import io.github.mymx2.plugin.tasks.MD5DirectoryChecksum

plugins { java }

// Generate additional resources required at application runtime
// This is an example for creating and integrating a custom task implementation.
val resourcesChecksum =
  tasks.register<MD5DirectoryChecksum>("resourcesChecksum") {
    inputDirectory.set(layout.projectDirectory.dir("src/main/resources"))
    checksumFile.set(layout.buildDirectory.file("generated-resources/md5/resources.MD5"))
  }

tasks.processResources { from(resourcesChecksum) }
