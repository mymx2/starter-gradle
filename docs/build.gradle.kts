@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.gradle.cachedProvider
import io.github.mymx2.plugin.spotless.SpotlessConfig
import io.github.mymx2.plugin.spotless.defaultStep

plugins {
  alias(libs.plugins.com.github.node.gradle.node)
  id("io.github.mymx2.check.format-base")
}

val isCI = EnvAccess.isCi(providers)
val nodeVersion = libs.versions.node.get()

val nodeDir = isolated.rootProject.projectDirectory.dir(".gradle/nodejs")

// https://github.com/node-gradle/gradle-node-plugin/blob/main/docs/faq.md
node {
  download = nodeDir.asFile.exists().not()
  version = nodeVersion
  distBaseUrl = null // FAIL_ON_PROJECT_REPOS model configured in 'repositories' plugin
  npmInstallCommand.set(if (isCI) "ci" else "install")
  workDir = nodeDir
}

// https://github.com/diffplug/spotless/tree/main/plugin-gradle#npm-detection
val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
val npm =
  tasks.npmSetup.map {
    val npmExec = if (isWindows) "npm.cmd" else "bin/npm"
    it.npmDir.get().file(npmExec)
  }

@Suppress("ConstPropertyName")
object VitePressConfig {
  const val vitePressDist = ".vitepress/dist"
  const val vitePressCache = ".vitepress/cache"
  const val srcPages = "src/pages"
  const val distDir = "${srcPages}/${vitePressDist}"
}

"website"
  .also { website ->
    tasks.register("npmInstallGlobalNpm") {
      description = "Install npm globally"
      dependsOn(tasks.npmSetup)
      val result = cachedProvider {
        providers.exec { commandLine(npm.get(), "i", "-g", "npm") }.standardOutput.asText.get()
      }
      doLast { println("\u001B[32m${result.get()}\u001B[0m") }
    }

    val vitedoc =
      tasks.register("docVite") {
        group = "docs"
        description = "Generate Vite docs [group = docs]"
        dependsOn(tasks.npmSetup)
        val workDirProvider = provider { isolated.projectDirectory.dir(website) }
        val result = cachedProvider {
          providers
            .exec {
              workingDir(workDirProvider.get().asFile.path)
              commandLine(npm.get(), "run", "build")
            }
            .standardOutput
            .asText
            .get()
        }
        doLast {
          println(result.get())
          println(
            "${workDirProvider.get().asFile.invariantSeparatorsPath}/${VitePressConfig.distDir}"
          )
        }
      }
    tasks.register<Zip>("distZipWebsite") {
      group = "toolbox"
      description = "Zips the website dist directory"
      archiveFileName = "dist.zip"
      destinationDirectory.set(isolated.projectDirectory.dir("build/distributions"))
      from(isolated.projectDirectory.dir("${website}/${VitePressConfig.distDir}"))
      dependsOn(vitedoc)
    }
  }
  .also { website ->
    spotless {
      format("prettierDocs") {
        defaultStep {
          prettier(SpotlessConfig.prettierDevDependencies)
            .npmExecutable(npm.get())
            .npmrc("${website}/.npmrc")
            .npmInstallCache()
            .configFile("${website}/.prettierrc.json5")
        }
        target(
          isolated.projectDirectory.files(
            "${website}/README.md",
            "${website}/package.json",
            "${website}/.prettierrc.json5",
          ),
          fileTree("${website}/${VitePressConfig.srcPages}")
            .exclude(VitePressConfig.vitePressDist)
            .exclude(VitePressConfig.vitePressCache)
            .include(
              "**/*.md",
              "**/*.json",
              "**/*.json5",
              "**/*.yml",
              "**/*.js",
              "**/*.mjs",
              "**/*.ts",
              "**/*.mts",
              "**/*.tsx",
              "**/*.css",
              "**/*.less",
              "**/*.scss",
            ),
        )
      }
    }

    tasks.named("spotlessPrettierDocs") { dependsOn(tasks.npmSetup) }
  }
  .also { website ->
    tasks.register<Zip>("backupWebsite") {
      group = "toolbox"
      description = "Backup the website directory"
      from(isolated.projectDirectory.dir(website))
      exclude(
        "**/node_modules",
        "**/${VitePressConfig.vitePressDist}/**",
        "**/${VitePressConfig.vitePressCache}/**",
      )
      archiveFileName.set("${website}.zip")
      destinationDirectory.set(isolated.projectDirectory.dir("build"))
    }
  }
