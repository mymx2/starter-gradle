@file:Suppress("UnstableApiUsage")

import io.github.mymx2.plugin.tasks.GenerateStartScript
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.util.VersionExtractor

plugins {
  java
  id("io.github.mymx2.module.app")
  // https://github.com/spring-projects/spring-boot/blob/main/build-plugin/spring-boot-gradle-plugin/src/docs/antora/modules/gradle-plugin/pages/index.adoc
  id("org.springframework.boot")
}

tasks.run { enabled = false }

val springBootVersion = VersionExtractor.forClass(BootBuildImage::class.java)

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
  developmentOnly("org.springframework.boot:spring-boot-devtools:${springBootVersion}")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  implementation("org.springframework.boot:spring-boot-starter-web")
}

configurations {
  val internal = maybeCreate("internal")
  // https://docs.spring.io/spring-boot/gradle-plugin/reacting.html
  // productionRuntimeClasspath = runtimeClasspath - developmentOnly/testDevelopmentOnly
  productionRuntimeClasspath {
    extendsFrom(internal)
    shouldResolveConsistentlyWith(configurations.named("runtimeClasspath").get())
  }
  developmentOnly {
    extendsFrom(internal)
    shouldResolveConsistentlyWith(configurations.named("runtimeClasspath").get())
  }
}

springBoot {
  buildInfo {
    properties {
      additional.put("project.path", project.path)
      additional.put(
        "layout.settingsDirectory",
        layout.settingsDirectory.asFile.invariantSeparatorsPath,
      )
      additional.put(
        "layout.projectDirectory",
        layout.projectDirectory.asFile.invariantSeparatorsPath,
      )
      additional.put(
        "layout.buildDirectory",
        layout.buildDirectory.get().asFile.invariantSeparatorsPath,
      )
    }
  }
}

tasks.named<GenerateStartScript>("generateStartScript") {
  appJar.set(tasks.bootJar.flatMap { it.archiveFileName })
}

tasks.named<Copy>("copyJarToRoot") { from(tasks.bootJar) }
