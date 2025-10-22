@file:Suppress("UnstableApiUsage")

plugins { id("com.autonomousapps.dependency-analysis") }

if (path == ":") {
  dependencyAnalysis {
    // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1234
    // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1485
    structure {
      bundle("spring-boot") {
        primary("org.springframework.boot:spring-boot-starter-web")
        includeDependency("org.springframework.boot:spring-boot")
        includeDependency("org.springframework.boot:spring-boot-autoconfigure")
        includeDependency("org.springframework:spring-context")
        includeDependency("org.springframework:spring-web")
      }
      bundle("spring-boot-test") {
        primary("org.springframework.boot:spring-boot-starter-test")
        includeDependency("org.springframework.boot:spring-boot-test")
        includeDependency("org.springframework:spring-test")
        includeDependency("org.junit.jupiter:junit-jupiter-api")
      }
    }
  }
}
