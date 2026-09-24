package io.github.mymx2.plugin.gradle

import io.github.mymx2.plugin.DefaultProjects
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.getByType
import org.gradlex.jvm.dependency.conflict.resolution.JvmDependencyConflictsExtension

val CONSISTENT_RESOLUTION_ATTRIBUTE: Attribute<String> =
  Attribute.of("consistent-resolution", String::class.java)

fun Configuration.applyConsistentResolutionAttributes(project: Project) {
  attributes {
    attribute(CONSISTENT_RESOLUTION_ATTRIBUTE, "global")
    attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    attribute(
      Category.CATEGORY_ATTRIBUTE,
      project.objects.named(Category::class.java, Category.LIBRARY),
    )
    attribute(
      LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
      project.objects.named(LibraryElements::class.java, LibraryElements.JAR),
    )
    attribute(
      Bundling.BUNDLING_ATTRIBUTE,
      project.objects.named(Bundling::class.java, Bundling.EXTERNAL),
    )
  }
}

fun Project.configureSharedResolution() {
  val jvmDependencyConflicts = extensions.getByType<JvmDependencyConflictsExtension>()
  jvmDependencyConflicts.consistentResolution {
    if (project.path == ":") {
      providesVersions(project.path)
    } else {
      val providedVersionsProject =
        project.findProject(DefaultProjects.aggregationPath)?.path ?: ":"
      providesVersions(providedVersionsProject)
      project.findProject(DefaultProjects.versionsPath)?.path?.let { platform(it) }
    }
  }
  jvmDependencyConflicts.logging { enforceSlf4JSimple() }
  configurations.configureEach { resolutionStrategy.preferProjectModules() }
}
