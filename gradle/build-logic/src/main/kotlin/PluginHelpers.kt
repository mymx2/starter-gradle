import com.autonomousapps.tasks.ProjectHealthTask
import io.fuchs.gradle.collisiondetector.DetectCollisionsTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.util.VersionExtractor

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

  /**
   * Adds a dependency on the Spring Boot platform.
   *
   * @param dependencyName The name of the dependency.
   * @param springBootVersion The version of the dependency.
   */
  fun Project.useSpringBootPlatform(
    dependencyName: String = "org.springframework.boot:spring-boot-dependencies",
    springBootVersion: String = "",
  ) {
    val version =
      springBootVersion.ifBlank { VersionExtractor.forClass(BootBuildImage::class.java) }
    dependencies { add("implementation", platform("$dependencyName:$version")) }
  }

  /**
   * Adds a dependency on the Spring Boot auto module.
   *
   * @param autoKspVersion The version of the dependency.
   * @param springBootVersion The version of the dependency.
   */
  fun Project.kspSpringBootAuto(autoKspVersion: String = "1.0.3", springBootVersion: String = "") {
    val version =
      springBootVersion.ifBlank { VersionExtractor.forClass(BootBuildImage::class.java) }
    dependencies {
      add("ksp", "io.github.mymx2:mica-auto-ksp:$autoKspVersion")
      add("implementation", "io.github.mymx2:mica-auto-ksp:$autoKspVersion")
      add("implementation", "org.springframework.boot:spring-boot-autoconfigure:$version")
    }
  }

  /**
   * Adds a dependency on the Spring Boot processor.
   *
   * @param springBootVersion The version of the dependency.
   */
  fun Project.kaptSpringBootProcessor(springBootVersion: String = "") {
    val version =
      springBootVersion.ifBlank { VersionExtractor.forClass(BootBuildImage::class.java) }
    dependencies {
      add("kapt", "org.springframework.boot:spring-boot-autoconfigure-processor:$version")
      add("kapt", "org.springframework.boot:spring-boot-configuration-processor:$version")
    }
  }
}
