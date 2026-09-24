@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.tasks.GenerateStartScript

plugins { java }

val projectRoot = isolated.rootProject.projectDirectory

val generateStartScript =
  tasks.register<GenerateStartScript>("generateStartScript") {
    description = "Generate platform-specific start script for the application"
    appJar.set(tasks.jar.flatMap { it.archiveFileName })
  }

val copyJarToRoot =
  tasks.register<Copy>("copyJarToRoot") {
    description = "Copy JAR and start script to root archives directory"
    from(tasks.jar)
    from(generateStartScript)
    exclude("**/*-plain.jar")
    into(projectRoot.dir("build/archives"))
  }

tasks.assemble { dependsOn(copyJarToRoot) }
