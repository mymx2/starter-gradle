package io.github.mymx2.plugin.tasks

import io.github.mymx2.plugin.Injected
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Gradle task: `unzipSourceJars`
 *
 * Resolves ALL dependency source JARs (compile + runtime + test classpaths, including transitive
 * dependencies) using Gradle's variant-aware artifact resolution, then extracts them into
 * `<rootProject>/.gradle/gradle_module/<group>/<artifact>/<version>/`.
 *
 * ## Caching strategy
 *
 * Acts as a persistent, incremental cache: already-extracted artifacts are skipped on subsequent
 * runs, and old versions are preserved so that multiple projects can share the same source index.
 * Output is written to the root project's `.gradle/gradle_module` directory — NOT into Gradle's own
 * dependency cache — to keep it project-scoped and inspectable.
 *
 * ## Use case
 *
 * Useful for AI code indexing — lets tools like Qoder/Claude Code/Codex scan dependency sources the
 * same way they scan local project code.
 *
 * ## Implementation notes
 * - Source JARs are resolved lazily via [Property] — only iterated at execution time, so
 *   configuration phase stays cheap. Uses `SetProperty<File>` instead of
 *   `ConfigurableFileCollection` to avoid `from()` chain recursion in Gradle 9.x.
 * - Uses Java NIO Zip FileSystem instead of `Project.zipTree` to avoid coupling with the Project
 *   instance at execution time.
 * - GAV coordinates are derived from the Gradle cache path
 *   (`files-2.1/<group>/<artifact>/<version>`), so this only works for dependencies resolved
 *   through the standard Gradle cache layout.
 *
 * @see
 *   [Variant-aware resolution](https://docs.gradle.org/nightly/userguide/variant_aware_resolution.html)
 * @see [Artifact views](https://docs.gradle.org/nightly/userguide/artifact_views.html)
 */
@DisableCachingByDefault(because = "Outputs to shared project cache, not task-specific")
abstract class UnzipSourceJarsTask : DefaultTask(), Injected {

  /** Source JAR files resolved lazily via artifact views — only iterated at execution time. */
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val sourceFiles: SetProperty<File>

  /** Output directory for extracted sources, defaults to <rootProject>/.gradle/gradle_module */
  @get:Internal abstract val outputDir: DirectoryProperty

  init {
    group = "toolbox"
    description = "Download and extract all dependency source JARs for AI code indexing"
  }

  @TaskAction
  fun execute() {
    val outDir = outputDir.get().asFile.toPath()
    Files.createDirectories(outDir)

    var extracted = 0
    var skipped = 0
    var failed = 0

    sourceFiles.get().forEach { jarFile ->
      // Skip non-existent files or non-JAR artifacts (e.g. POM-only dependencies)
      if (!jarFile.exists() || !jarFile.name.endsWith(".jar")) {
        skipped++
        return@forEach
      }

      // Derive <group>/<artifact>/<version> from the Gradle cache path:
      //   …/files-2.1/<group>/<artifact>/<version>/<hash>/<file>-sources.jar
      val pathParts = jarFile.toPath().iterator().asSequence().map { it.toString() }.toList()
      val f21Idx = pathParts.indexOfFirst { it.startsWith("files-2.1") }
      if (f21Idx < 0 || f21Idx + 3 >= pathParts.size) {
        skipped++
        return@forEach
      }
      val group = pathParts[f21Idx + 1]
      val artifact = pathParts[f21Idx + 2]
      val version = pathParts[f21Idx + 3]
      val displayName = "$group:$artifact:$version"

      val targetDir = outDir.resolve(group).resolve(artifact).resolve(version)
      // Skip if already extracted and the target directory is non-empty
      if (Files.isDirectory(targetDir) && Files.list(targetDir).use { it.findFirst().isPresent }) {
        logger.info("Already cached: {}", displayName)
        skipped++
        return@forEach
      }

      logger.lifecycle("Extracting: {}", displayName)
      Files.createDirectories(targetDir)
      // Extract via Java NIO Zip FileSystem — avoids Project.copy / Project.zipTree
      // so we don't hold a reference to the Project at execution time.
      try {
        FileSystems.newFileSystem(URI.create("jar:${jarFile.toURI()}"), emptyMap<String, Any>())
          .use { fs ->
            fs.rootDirectories.forEach { root ->
              Files.walk(root).use { stream ->
                stream.forEach { entry ->
                  val relative = root.relativize(entry).toString()
                  // Skip the root directory entry itself (relativize produces empty string)
                  if (relative.isEmpty()) return@forEach
                  val target = targetDir.resolve(relative)
                  if (Files.isDirectory(entry)) {
                    Files.createDirectories(target)
                  } else {
                    target.parent?.let { Files.createDirectories(it) }
                    Files.copy(entry, target, StandardCopyOption.REPLACE_EXISTING)
                  }
                }
              }
            }
          }
        extracted++
      } catch (e: Exception) {
        logger.warn("Failed to extract {}: {}", displayName, e.toString())
        logger.info("Stack trace for {}:", displayName, e)
        failed++
      }
    }

    val total = extracted + skipped + failed
    logger.lifecycle(
      "--- Done! {} total: {} extracted, {} cached (skipped), {} failed. Output: {} ---",
      total,
      extracted,
      skipped,
      failed,
      outDir,
    )
  }
}
