@file:Suppress("UnstableApiUsage")

import io.fuchs.gradle.collisiondetector.DetectCollisionsTask
import io.github.mymx2.plugin.InternalDependencies
import io.github.mymx2.plugin.libs
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.base.TestingExtension

object PluginHelpers {

  /** Enables the collision detection plugin. */
  fun Project.enableCollisionDetection() {
    tasks.named("qualityCheck") { dependsOn(tasks.withType<DetectCollisionsTask>()) }

    tasks.named("qualityGate") { dependsOn(tasks.withType<DetectCollisionsTask>()) }
  }

  /**
   * Enables the testing suite with dependencies.
   *
   * effect:
   * ```
   * dependencies {
   *   implementation(platform("org.junit:junit-bom:<version>"))
   *   implementation(platform("org.assertj:assertj-bom:<version>"))
   *   runtimeOnly("org.junit.platform:junit-platform-launcher")
   *   implementation("org.junit.jupiter:junit-jupiter")
   *   implementation("org.assertj:assertj-core")
   * }
   * ```
   */
  fun Project.useJUnitJupiterM2(junitBomVersion: String = "", assertjBomVersion: String = "") {
    extensions.getByType(TestingExtension::class.java).suites.withType<JvmTestSuite> {
      dependencies {
        implementation(
          platform(
            if (junitBomVersion.isNotBlank()) {
              "org.junit:junit-bom:${junitBomVersion}"
            } else {
              runCatching { libs.findLibrary("junitBom").get().get() }
                .getOrElse { InternalDependencies.useLibrary("junitBom") }
                .toString()
            }
          )
        )
        implementation(
          platform(
            if (assertjBomVersion.isNotBlank()) {
              "org.assertj:assertj-bom:${assertjBomVersion}"
            } else {
              runCatching { libs.findLibrary("assertjBom").get().get() }
                .getOrElse { InternalDependencies.useLibrary("assertjBom") }
                .toString()
            }
          )
        )
        runtimeOnly("org.junit.platform:junit-platform-launcher")
        implementation("org.junit.jupiter:junit-jupiter")
        implementation("org.assertj:assertj-core")
      }
    }
  }
}

/**
 * module-info style dependency block.
 *
 * ```
 * Usage:
 * jpmsModule {
 *   require("org.slf4j:slf4j-api:<version>")
 *   requireTransitive(project(":core"))
 * }
 * ```
 */
@Suppress("detekt:UnusedPrivateMember")
fun Project.jpmsModule(configuration: ModuleInfoDependencyScope.() -> Unit) {
  ModuleInfoDependencyScope(dependencies, false).configuration()
}

@Suppress("detekt:UnusedPrivateMember")
fun Project.testJpmsModule(configuration: ModuleInfoDependencyScope.() -> Unit) {
  ModuleInfoDependencyScope(dependencies, true).configuration()
}

class ModuleInfoDependencyScope(
  private val dependencies: DependencyHandler,
  private val isTest: Boolean,
) {

  /** requires */
  fun requires(implementation: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testImplementation", implementation)
    } else {
      dependencies.add("implementation", implementation)
    }
  }

  /** requires transitive */
  fun requiresTransitive(api: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testApi", api)
    } else {
      dependencies.add("api", api)
    }
  }

  /** requires static */
  fun requiresStatic(compileOnly: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testCompileOnly", compileOnly)
    } else {
      dependencies.add("compileOnly", compileOnly)
    }
  }

  /** requires static transitive */
  fun requireStaticsTransitive(compileOnlyApi: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testCompileOnlyApi", compileOnlyApi)
    } else {
      dependencies.add("compileOnlyApi", compileOnlyApi)
    }
  }

  /** requires runtime */
  fun requiresRuntime(runtimeOnly: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testRuntimeOnly", runtimeOnly)
    } else {
      dependencies.add("runtimeOnly", runtimeOnly)
    }
  }
}
