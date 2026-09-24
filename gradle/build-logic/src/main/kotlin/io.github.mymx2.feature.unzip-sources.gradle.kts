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
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaPlugin

// 不强制 apply java 插件——Android 模块与 java 插件冲突。
// Java 模块由调用方（如 compile-java-ext）已 apply java；Android 模块靠后缀匹配。
// JavaPlugin 常量在此仅作引用，不要求插件已 apply。

// Output to root project's .gradle/gradle_modules — shared across all sub-projects,
// no need to write into Gradle's own dependency cache.
val outputDirProvider: Provider<Directory> =
  providers
    .provider { rootProject.layout.projectDirectory }
    .map { it.dir(".gradle/gradle_modules") }

// Resolve source JARs from all four standard classpaths (compile, runtime, testCompile,
// testRuntime).
// Android 模块也产 source JAR 索引，配置名模式不同（devDebugCompileClasspath 等），
// 用后缀匹配替代 Java 插件的固定配置名。
val classpathNames =
  setOf(
    JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME,
    JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME,
    JavaPlugin.TEST_COMPILE_CLASSPATH_CONFIGURATION_NAME,
    JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME,
  )

val classpathSuffixes = listOf("CompileClasspath", "RuntimeClasspath")

fun isClasspathConfig(name: String): Boolean =
  name in classpathNames || classpathSuffixes.any { name.endsWith(it) }

// Build a lazy Provider<Set<String>> that resolves source JARs at execution time.
// Each entry is "group|artifact|version|absolutePath" — GAV comes from the component
// identifier, NOT from parsing the file path, so artifacts outside Gradle's files-2.1
// cache (mavenLocal, flatDir, ivy) are handled correctly.
// Avoids ConfigurableFileCollection.from() chain which causes StackOverflowError in Gradle 9.x
// due to recursive finalization across artifact views sharing parent configurations.
// Configurations are matched lazily — no afterEvaluate needed.
val sourceFilesProvider: Provider<Set<String>> = providers.provider {
  configurations
    .matching { isClasspathConfig(it.name) }
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
        .mapNotNull { artifact ->
          val id = artifact.id.componentIdentifier
          if (id is ModuleComponentIdentifier) {
            "${id.group}|${id.module}|${id.version}|${artifact.file.absolutePath}"
          } else {
            null
          }
        }
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
