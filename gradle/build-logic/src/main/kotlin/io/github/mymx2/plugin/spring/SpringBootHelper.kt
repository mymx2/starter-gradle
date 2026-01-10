package io.github.mymx2.plugin.spring

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.util.VersionExtractor

/** A helper class for Spring Boot projects. */
object SpringBootHelper {

  /**
   * Returns the version of the Spring Boot platform.
   *
   * @return The version of the Spring Boot platform.
   */
  fun getSpringBootVersion(default: String = "4.0.0"): String {
    return VersionExtractor.forClass(BootBuildImage::class.java) ?: default
  }

  /**
   * Adds a dependency on the Spring Boot platform.
   *
   * @param dependencyName The name of the dependency.
   * @param springBootVersion The version of the dependency.
   */
  fun Project.useSpringBootPlatform(
    dependencyName: String = "org.springframework.boot:spring-boot-dependencies",
    springBootVersion: String = getSpringBootVersion(),
  ) {
    dependencies { add("implementation", platform("$dependencyName:$springBootVersion")) }
  }

  /**
   * Adds a dependency on the Spring Boot auto module.
   *
   * @param autoKspVersion The version of the dependency.
   * @param springBootVersion The version of the dependency.
   */
  fun Project.kspSpringBootAuto(
    autoKspVersion: String = "1.0.3",
    springBootVersion: String = getSpringBootVersion(),
  ) {
    dependencies {
      add("ksp", "io.github.mymx2:mica-auto-ksp:$autoKspVersion")
      add("implementation", "io.github.mymx2:mica-auto-ksp:$autoKspVersion")
      add("implementation", "org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    }
  }

  /**
   * Adds a dependency on the Spring Boot processor.
   *
   * @param springBootVersion The version of the dependency.
   */
  fun Project.kaptSpringBootProcessor(springBootVersion: String = getSpringBootVersion()) {
    dependencies {
      add("kapt", "org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
      add("kapt", "org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
    }
  }

  /**
   * Adds a dependency on the Spring Boot starter web mvc.
   *
   * @param springBootVersion The version of the dependency.
   */
  fun Project.springBootStarterWebMvc(springBootVersion: String = getSpringBootVersion()) {
    useSpringBootPlatform(springBootVersion = springBootVersion)
    dependencies {
      add("implementation", "org.springframework.boot:spring-boot-starter-webmvc")
      add("testImplementation", "org.springframework.boot:spring-boot-starter-webmvc-test")
    }
  }
}
