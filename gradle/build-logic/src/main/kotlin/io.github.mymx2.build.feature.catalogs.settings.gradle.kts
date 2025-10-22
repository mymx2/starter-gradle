@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
  versionCatalogs {
    layout.settingsDirectory
      .dir("gradle/versions")
      .asFile
      .listFiles()
      .filter {
        if (
          !(it.isFile && (it.name.endsWith(".versions.toml") || it.name.endsWith(".gradle.kts")))
        ) {
          error("${it.name} is not a valid version catalog file.")
        }
        !it.name.startsWith("__") && it.name.endsWith(".versions.toml")
      }
      .forEach {
        val name = it.name.removeSuffix(".versions.toml")
        create(name) { from(layout.settingsDirectory.files(it.path)) }
      }
  }
}
