import io.github.mymx2.plugin.injected
import io.github.mymx2.plugin.utils.ExeFinder
import java.io.File
import org.gradle.api.tasks.Exec

plugins { id("io.github.mymx2.base.lifecycle") }

// Only apply to root project
if (path == ":") {
  val pnpmPath = ExeFinder.findExePath(providers, layout, "pnpm").orEmpty()
  val pnpmExists = pnpmPath.isNotBlank() && File(pnpmPath).exists()
  val projectRoot = rootDir

  val prettierInstall =
    tasks.register<Exec>("prettierInstall") {
      workingDir = projectRoot
      commandLine(if (pnpmExists) pnpmPath else "pnpm", "install")
      onlyIf { pnpmExists && !projectRoot.resolve("node_modules/prettier").exists() }
    }

  val prettierTargets =
    fileTree(projectRoot) {
      include("**/*.md", "**/*.json", "**/*.json5", "**/*.yaml", "**/*.yml", "**/*.xml")
      exclude(
        "build/**",
        "**/build/**",
        ".gradle/**",
        "**/.gradle/**",
        "**/node_modules/**",
        "**/*-lock.yaml",
        "**/*-lock.json",
        ".github/actions/**",
        "**/nocheck/**",
        "**/autogen/**",
        "**/generated/**",
        "docs/**",
      )
    }

  val prettierFiles = prettierTargets.files.toList()
  val filePaths = prettierFiles.map { it.absolutePath }

  val prettierCheck =
    tasks.register("prettierCheck") {
      dependsOn(prettierInstall)
      enabled = pnpmExists && filePaths.isNotEmpty()
      inputs.files(prettierFiles)
      val inject = injected
      doLast {
        inject.exec.exec {
          workingDir = projectRoot
          commandLine(pnpmPath, "exec", "prettier", "--check")
          args(filePaths)
          isIgnoreExitValue = false
        }
      }
    }

  val prettierApply =
    tasks.register("prettierApply") {
      dependsOn(prettierInstall)
      enabled = pnpmExists && filePaths.isNotEmpty()
      val inject = injected
      doLast {
        inject.exec.exec {
          workingDir = projectRoot
          commandLine(pnpmPath, "exec", "prettier", "--write")
          args(filePaths)
          isIgnoreExitValue = false
        }
      }
    }

  tasks.named("qualityCheck") { dependsOn(prettierCheck) }

  tasks.named("qualityGate") { dependsOn(prettierApply) }
}
