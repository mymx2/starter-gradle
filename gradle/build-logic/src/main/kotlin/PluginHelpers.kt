import com.autonomousapps.tasks.ProjectHealthTask
import io.fuchs.gradle.collisiondetector.DetectCollisionsTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.withType

object PluginHelpers {

  /** Enables the project health plugin. */
  fun Project.enableProjectHealth() {
    tasks.named("qualityCheck") { dependsOn(tasks.withType<ProjectHealthTask>()) }

    tasks.named("qualityGate") { dependsOn(tasks.withType<ProjectHealthTask>()) }
  }

  /** Enables the collision detection plugin. */
  fun Project.enableCollisionDetection() {
    tasks.named("qualityCheck") { dependsOn(tasks.withType<DetectCollisionsTask>()) }

    tasks.named("qualityGate") { dependsOn(tasks.withType<DetectCollisionsTask>()) }
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
