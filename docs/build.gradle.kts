@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.gradle.cachedFlatMap
import io.github.mymx2.plugin.gradle.cachedProvider
import io.github.mymx2.plugin.injected
import io.github.mymx2.plugin.spotless.SpotlessConfig
import io.github.mymx2.plugin.spotless.defaultStep
import java.io.ByteArrayOutputStream

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
  cachedProvider { tasks.npmSetup }
    .cachedFlatMap(objects) { npmSetup ->
      npmSetup.map {
        val npmExec = if (isWindows) "npm.cmd" else "bin/npm"
        it.npmDir.get().file(npmExec)
      }
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
    val workDirProvider = provider { isolated.projectDirectory.dir(website) }

    tasks.register("writeLocks") {
      group = "toolbox"
      description = "write dependencies to lockfile"
      val inject = injected
      dependsOn(tasks.npmSetup)
      doFirst {
        inject.layout.projectDirectory.file("${website}/pnpm-lock.yaml").asFile.also {
          if (it.exists()) {
            it.copyTo(
              inject.layout.buildDirectory.file("/tmp/locks/pnpm-lock.yaml.bak").get().asFile,
              true,
            )
          }
        }
      }
      val npmProvider = provider { npm.get() }
      doLast {
        val output = ByteArrayOutputStream()
        inject.exec.exec {
          workingDir(workDirProvider.get().asFile.path)
          commandLine(npmProvider.get(), "run", "yo")
          standardOutput = output
        }
        println("\u001B[32m${output}\u001B[0m")
      }
    }
    tasks.register("checkLocks") {
      group = "toolbox"
      description = "Check dependencies for lockfile"
      dependsOn(tasks.named("writeLocks"))
      val inject = injected
      doLast {
        val bakLockContent =
          inject.layout.buildDirectory.file("/tmp/locks/pnpm-lock.yaml.bak").orNull?.let {
            val file = it.asFile
            if (file.exists()) file.readText() else null
          }
        if (bakLockContent != null) {
          val lockFile =
            inject.layout.projectDirectory.file("${website}/pnpm-lock.yaml").asFile.takeIf {
              it.exists()
            }
          val lockContent = lockFile?.readText()
          if (lockFile != null && bakLockContent != lockContent) {
            throw GradleException(
              "$lockFile has been modified, please run 'writeLocks' to update lockfile"
            )
          }
        }
      }
    }

    val vitedoc =
      tasks.register("docVite") {
        group = "docs"
        description = "Generate Vite docs [group = docs]"
        dependsOn(tasks.npmSetup)

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
