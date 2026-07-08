@file:Suppress("UnstableApiUsage") // withVariantReselection() is @Incubating

/**
 * Precompiled script plugin: unzip-sources
 *
 * Registers the `unzipSourceJars` task which resolves all dependency source JARs (compile +
 * runtime + test classpaths) via variant-aware artifact resolution, then extracts them into a
 * shared project-level cache directory.
 *
 * Apply this plugin to any sub-project that needs dependency source indexing.
 */
import io.github.mymx2.plugin.tasks.UnzipSourceJarsTask
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaPlugin

plugins { java }

// Output to root project's .gradle/gradle_module — shared across all sub-projects,
// no need to write into Gradle's own dependency cache.
val outputDirProvider: Provider<Directory> =
  providers.provider { rootProject.layout.projectDirectory }.map { it.dir(".gradle/gradle_module") }

// Resolve source JARs from all four standard classpaths (compile, runtime, testCompile,
// testRuntime).
val classpathNames =
  setOf(
    JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME,
    JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME,
    JavaPlugin.TEST_COMPILE_CLASSPATH_CONFIGURATION_NAME,
    JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME,
  )

// Build a lazy Provider<Set<File>> that resolves source JARs at execution time.
// Avoids ConfigurableFileCollection.from() chain which causes StackOverflowError in Gradle 9.x
// due to recursive finalization across artifact views sharing parent configurations.
// Configurations are matched lazily — no afterEvaluate needed.
val sourceFilesProvider: Provider<Set<File>> = providers.provider {
  configurations
    .matching { it.name in classpathNames }
    .flatMap { config ->
      config.incoming
        .artifactView {
          withVariantReselection()
          isLenient = true
          attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
          }
        }
        .artifacts
        .artifactFiles
        .files
    }
    .toSet()
}

// Register the task — sourceFiles is wired lazily via Provider,
// resolution only happens at execution time.
tasks.register<UnzipSourceJarsTask>("unzipSourceJars") {
  description = "Download and extract all dependency source JARs for AI code indexing"
  sourceFiles.set(sourceFilesProvider)
  outputDir.set(outputDirProvider)
}
