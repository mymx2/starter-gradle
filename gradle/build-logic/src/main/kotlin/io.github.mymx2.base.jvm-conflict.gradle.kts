import io.github.mymx2.plugin.gradle.CONSISTENT_RESOLUTION_ATTRIBUTE
import io.github.mymx2.plugin.gradle.applyConsistentResolutionAttributes
import io.github.mymx2.plugin.gradle.configureSharedResolution

plugins { id("org.gradlex.jvm-dependency-conflict-resolution") }

// Configure consistent resolution across the whole project
configurations.create(
  "allDependencies",
  Action {
    isCanBeConsumed = true
    isCanBeResolved = false
    sourceSets.configureEach {
      extendsFrom(
        configurations[this.implementationConfigurationName],
        configurations[this.compileOnlyConfigurationName],
        configurations[this.runtimeOnlyConfigurationName],
        configurations[this.annotationProcessorConfigurationName],
      )
    }
    applyConsistentResolutionAttributes(project)
  },
)

// Configure build wide consistent resolution. That is, the versions that are used on the
// runtime classpath of the web applications should also be used in all other places
// (e.g. also when compiling a project at the bottom of the dependency graph that does not
// see most of the other dependencies that may influence the version choices).
// Also configures logging capabilities plugin to default to Slf4JSimple, and prefers
// local project modules over published versions.
configureSharedResolution()

configurations.getByName("mainRuntimeClasspath") {
  attributes.attribute(CONSISTENT_RESOLUTION_ATTRIBUTE, "global")
}
