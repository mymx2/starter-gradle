import com.autonomousapps.tasks.ProjectHealthTask
import io.fuchs.gradle.collisiondetector.DetectCollisionsTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
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

  /**
   * Adds a dependency to the project.
   *
   * @param configuration The configuration to add the dependency to.
   */
  fun Project.useDependencies(configuration: DependencyHandlerScope.() -> Unit) =
    DependencyHandlerScope.of(dependencies).configuration()
}
