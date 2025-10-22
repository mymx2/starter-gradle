@file:Suppress("UnstableApiUsage")

import CheckVersionPluginConfig.taskConfigureCheckGradleVersion
import CheckVersionPluginConfig.taskConfigureCheckProjectVersions
import CheckVersionPluginConfig.taskConfigureCheckVersions
import io.github.mymx2.plugin.Injected
import io.github.mymx2.plugin.ProjectVersions
import io.github.mymx2.plugin.injected
import io.github.mymx2.plugin.tasks.DependencyVersionUpgradesCheck
import io.github.mymx2.plugin.tasks.JavaVersionConsistencyCheck
import io.github.mymx2.plugin.utils.Ansi
import io.github.mymx2.plugin.utils.CatalogUtil
import io.github.mymx2.plugin.utils.HttpUtil
import io.github.mymx2.plugin.utils.JsonParser
import io.github.mymx2.plugin.utils.chunkedVirtual
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference

// only version project
plugins {
  `java-platform`
  id("io.github.mymx2.base.lifecycle")
}

val checkVersionConsistency =
  tasks.register<JavaVersionConsistencyCheck>("checkVersionConsistency") {
    definedVersions = provider {
      configurations["api"].dependencyConstraints.associate {
        "${it.group}:${it.name}" to it.version!!
      }
    }
    aggregatedClasspath = provider {
      configurations["mainRuntimeClasspath"].incoming.resolutionResult.allComponents
    }
    reportFile = layout.buildDirectory.file("reports/version-consistency.txt")
  }

tasks.named("qualityCheck") { dependsOn(checkVersionConsistency) }

tasks.named("qualityGate") { dependsOn(checkVersionConsistency) }

val latestReleases =
  configurations.dependencyScope("dependencyVersionUpgrades") {
    withDependencies {
      add(project.dependencies.platform(project(project.path)))
      configurations.named("api").get().dependencies.forEach {
        add(
          project.dependencies.platform("${it.group}:${it.name}:latest.release") {
            isTransitive = false
          }
        )
      }
      configurations.named("api").get().dependencyConstraints.forEach {
        add(
          project.dependencies.create("${it.group}:${it.name}:latest.release").apply {
            isTransitive = false
          }
        )
      }
    }
  }
val latestReleasesPath =
  configurations.resolvable("latestReleasesPath") {
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    extendsFrom(latestReleases.get())
  }

tasks.register<DependencyVersionUpgradesCheck>("checkVersionUpgrades") {
  group = "toolbox"
  projectName.set(project.name)
  dependencies.set(
    configurations.named("api").get().dependencies.map { "${it.group}:${it.name}:${it.version}" }
  )
  dependencyConstraints.set(
    configurations.named("api").get().dependencyConstraints.map {
      "${it.group}:${it.name}:${it.version}"
    }
  )
  latestReleasesResolutionResult.set(
    latestReleasesPath.map { it.incoming.resolutionResult.allComponents }
  )
}

val currentGradleVersion = gradle.gradleVersion
val projectExtensions = project.extensions

tasks.register("checkVersions") {
  group = "toolbox"
  description = "Check gradle/*.versions.toml for updates"
  taskConfigureCheckProjectVersions(this)
  taskConfigureCheckVersions(this, injected, projectExtensions)
  taskConfigureCheckGradleVersion(this, currentGradleVersion)
}

object CheckVersionPluginConfig {

  fun taskConfigureCheckProjectVersions(task: Task) {
    task.configureCheckProjectVersions()
  }

  fun taskConfigureCheckVersions(
    task: Task,
    injected: Injected,
    projectExtensions: ExtensionContainer,
  ) {
    task.configureCheckVersions(injected, projectExtensions)
  }

  fun taskConfigureCheckGradleVersion(task: Task, currentGradleVersion: String) {
    task.configureCheckGradleVersion(currentGradleVersion)
  }

  private fun Task.configureCheckVersions(
    injected: Injected,
    projectExtensions: ExtensionContainer,
  ) {
    val configFiles =
      injected.providers.provider {
        projectExtensions.findByType<VersionCatalogsExtension>()?.map {
          injected.layout.settingsDirectory.file("gradle/${it.name}.versions.toml").asFile
        } ?: emptyList()
      }
    doLast {
      configFiles
        .get()
        .stream()
        .parallel()
        .map { file ->
          val lastUpdate = LocalDate.now().toString()
          val content = file.readText()
          val newContent = checkTomlDependencies(content)

          if (content != newContent) {
            val newFile = File(file.parentFile, "__${lastUpdate}-${file.name}")
            newFile.writeText("# last update: ${lastUpdate}\n" + newContent)
            Ansi.color("✏️ Updated file written to: ${newFile.invariantSeparatorsPath}", "32")
          } else {
            Ansi.color("🚩 No changes in ${file.name}", "32")
          }
        }
        .also {
          println(it.toList().joinToString("\n"))
          println("For locking dependencies, run: ./gradlew :[name]:dependencies --write-locks")
        }
    }
  }

  private fun getLatestVersionFromMetadata(metadata: String): String {
    return metadata.substringAfter("<release>").substringBefore("</release>").ifBlank {
      metadata.substringAfter("<latest>").substringBefore("</latest>")
    }
  }

  private fun processMetadata(
    metadataUrl: String,
    dependency: String,
    currentVersion: String,
    appendMsg: String = "",
    updateCallback: (String) -> Unit,
  ) {
    var jobMsg = metadataUrl
    val metadata =
      try {
        HttpUtil.get(metadataUrl, Duration.ofSeconds(30))
      } catch (_: Exception) {
        jobMsg += "\n  ❌ $dependency -> read metadata timeout${appendMsg}"
        ""
      }

    if (!metadata.isNullOrBlank()) {
      if (metadata.contains("</metadata>")) {
        val candidate = getLatestVersionFromMetadata(metadata)
        if (candidate != currentVersion) {
          jobMsg += "\n  ✅ $dependency -> $candidate${appendMsg}"
          updateCallback(candidate)
        }
      } else {
        jobMsg += "\n  ❌ $dependency -> can't find metadata${appendMsg}"
      }
    }
    println(jobMsg)
  }

  @Suppress("detekt:NestedBlockDepth")
  private fun checkTomlDependencies(content: String): String {
    val newContent = AtomicReference(content)
    val jobs = mutableListOf<() -> Unit>()
    var currentTopic = ""

    // val versionsRegex = Regex("""(\w+)\s*=\s*"([^"]+)"""")
    val libraryRegex =
      Regex("""(\w+)\s*=\s*\{\s*module\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"\s*}""")
    val pluginRegex =
      Regex("""([^\s=]+)\s*=\s*\{\s*id\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"\s*}""")

    content.lineSequence().forEach { line ->
      val trimmed = line.trim()
      if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
        currentTopic = line.removeSurrounding("[", "]")
      }
      if (!line.startsWith("#") && line.contains("=")) {
        when (currentTopic) {
          "libraries" -> {
            val match = libraryRegex.find(line)
            match?.destructured?.let { (alias, module, version) ->
              val (groupId, artifactId) = module.split(":")
              val dependency = "$groupId:$artifactId:$version"

              if (version != "latest.release") {
                val metadataUrl = getLibraryMetadataUrl(content, groupId, artifactId)
                jobs.add {
                  processMetadata(metadataUrl, dependency, version) { candidate ->
                    newContent.updateAndGet {
                      it.replace(
                        line,
                        """$alias = { module = "$module", version = "$candidate" }""",
                      )
                    }
                  }
                }
              }
            }
          }
          "plugins" -> {
            val match = pluginRegex.find(line)
            match?.destructured?.let { (alias, pluginId, version) ->
              if (version != "latest.release") {
                val metadataUrl = CatalogUtil.getPluginMetadataUrl(pluginId)
                jobs.add {
                  processMetadata(metadataUrl, pluginId, version) { candidate ->
                    newContent.updateAndGet {
                      it.replace(line, """$alias = { id = "$pluginId", version = "$candidate" }""")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    jobs.chunkedVirtual(size = 300, timeout = Duration.ofMinutes(5)) { it() }
    return newContent.get()
  }

  private fun getLibraryMetadataUrl(content: String, groupId: String, artifactId: String): String {
    return content
      .substringBefore("/${artifactId}/maven-metadata.xml", "")
      .let { if (it.isNotBlank()) it.plus("/${artifactId}/maven-metadata.xml") else it }
      .substringAfterLast("#", "")
      .trim()
      .let { url ->
        if (url.isNotBlank() && url.contains("http")) {
          url
        } else {
          CatalogUtil.getLibraryMetadataUrl("${groupId}:${artifactId}")
        }
      }
  }

  private fun Task.configureCheckProjectVersions() {
    doLast {
      val jobs = mutableListOf<() -> Unit>()
      val qualifiedName = ProjectVersions::class.qualifiedName
      ProjectVersions.entries.forEach {
        val moduleId = it.key
        val moduleVersion = it.value
        val moduleUrl = it.url
        val appendMsg = "\n    $qualifiedName"
        if (moduleUrl.endsWith("/maven-metadata.xml")) {
          jobs.add { processMetadata(moduleUrl, moduleId, moduleVersion, appendMsg) {} }
        } else if (moduleUrl.contains("npmjs.org")) {
          jobs.add {
            var jobMsg = moduleUrl
            val metadata =
              try {
                HttpUtil.get(moduleUrl, Duration.ofSeconds(30))
              } catch (_: Exception) {
                jobMsg += "\n  ❌ $moduleId -> read metadata timeout${appendMsg}"
                ""
              }
            if (!metadata.isNullOrBlank()) {
              if (metadata.contains("dist-tags")) {
                val candidate =
                  runCatching {
                      val map = JsonParser.parseMap(metadata)
                      (map["dist-tags"] as Map<*, *>)["latest"] as String
                    }
                    .getOrNull()
                if (candidate != moduleVersion) {
                  jobMsg += "\n  ✅ $moduleId -> $candidate${appendMsg}"
                }
              } else {
                jobMsg += "\n  ❌ $moduleId -> can't find metadata${appendMsg}"
              }
            }
            println(jobMsg)
          }
        }
      }
      jobs.chunkedVirtual(size = 300, timeout = Duration.ofMinutes(5)) { it() }
    }
  }

  private fun Task.configureCheckGradleVersion(currentGradleVersion: String) {
    doLast {
      val gradleVersionUrl = "https://services.gradle.org/versions/current"
      var newestGradleVersion: String?
      val gradleVersionContent =
        try {
          HttpUtil.get(gradleVersionUrl, Duration.ofSeconds(30))
        } catch (_: Exception) {
          ""
        }
      if (!gradleVersionContent.isNullOrBlank()) {
        newestGradleVersion =
          runCatching { JsonParser.parseMap(gradleVersionContent)["version"] as String }.getOrNull()
        if (newestGradleVersion != currentGradleVersion) {
          println(Ansi.color("🐘 Current Stable Gradle version => $newestGradleVersion", "31"))
        }
      }
    }
  }
}
