# 配置指南

本指南详细介绍如何自定义和配置 Starter Gradle 构建系统。

## 📋 目录

- [项目属性配置](#-项目属性配置)
- [环境变量配置](#-环境变量配置)
- [插件配置](#-插件配置)
- [依赖管理配置](#-依赖管理配置)
- [任务配置](#-任务配置)
- [性能优化配置](#-性能优化配置)

## ⚙️ 项目属性配置

### gradle.properties 文件

在 `gradle.properties` 中配置全局属性：

```properties
# ==================== 项目信息 ====================
projectGroup=io.github.yourname
projectVersion=1.0.0-SNAPSHOT
projectDescription=My awesome project

# ==================== JVM 参数 ====================
# Gradle Daemon JVM 参数
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8

# Kotlin Daemon JVM 参数
kotlin.daemon.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m

# ==================== 构建优化 ====================
# 启用构建缓存
org.gradle.caching=true

# 启用并行构建
org.gradle.parallel=true

# 启用配置缓存（实验性）
org.gradle.configuration-cache=true

# 守护进程设置
org.gradle.daemon=true
org.gradle.workers.max=4

# ==================== 仓库配置 ====================
# Maven 仓库镜像（中国大陆用户推荐）
# systemProp.org.gradle.internal.http.connectionTimeout=180000
# systemProp.org.gradle.internal.http.socketTimeout=180000

# ==================== 发布配置 ====================
# Maven Central 发布凭证（建议使用环境变量）
# signing.keyId=YOUR_KEY_ID
# signing.password=YOUR_PASSWORD
# signing.secretKeyRingFile=/path/to/secring.gpg
```

### 使用 profiles 区分环境

创建不同环境的配置文件：

**gradle-dev.properties** (开发环境):
```properties
projectVersion=1.0.0-SNAPSHOT
debugEnabled=true
```

**gradle-prod.properties** (生产环境):
```properties
projectVersion=1.0.0
debugEnabled=false
optimizationLevel=max
```

使用时指定：
```bash
./gradlew build -Pgradle-profile=prod
```

## 🔐 环境变量配置

### 敏感信息管理

永远不要将敏感信息提交到版本控制！使用环境变量：

```bash
# 在 CI/CD 或本地 shell 中设置
export ORG_GRADLE_PROJECT_signingKey="-----BEGIN PGP PRIVATE KEY BLOCK-----..."
export ORG_GRADLE_PROJECT_signingPassword="your-password"
export ORG_GRADLE_PROJECT_mavenCentralUsername="your-username"
export ORG_GRADLE_PROJECT_mavenCentralPassword="your-password"
```

Gradle 会自动读取 `ORG_GRADLE_PROJECT_` 前缀的环境变量作为项目属性。

### .env 文件支持

在项目根目录创建 `.env` 文件（需添加到 `.gitignore`）：

```bash
# .env 文件
SIGNING_KEY=your-signing-key
SIGNING_PASSWORD=your-signing-password
MAVEN_USERNAME=your-maven-username
MAVEN_PASSWORD=your-maven-password
```

在构建脚本中读取：
```kotlin
val signingKey by project
val signingPassword by project
```

## 🔌 插件配置

### 基础插件配置

#### Identity 插件

```kotlin
// build.gradle.kts
plugins {
    id("io.github.mymx2.base.identity")
}

// 配置示例（通常不需要，有默认值）
configure<IdentityExtension> {
    group.set("io.github.yourname")
    version.set("1.0.0")
    description.set("Project description")
}
```

#### Lifecycle 插件

```kotlin
configure<LifecycleExtension> {
    // 自定义生命周期行为
    enableCleanDependency.set(true)
}
```

### Check 插件配置

#### Spotless 格式化配置

```kotlin
// build.gradle.kts
plugins {
    id("io.github.mymx2.check.format-java")
}

spotless {
    java {
        target("**/*.java")
        googleJavaFormat("1.18.1")
        licenseHeaderFile(rootProject.file("gradle/configs/spotless/java.header"))
    }
    
    kotlin {
        target("**/*.kt")
        ktfmt("0.46").googleStyle()
        licenseHeaderFile(rootProject.file("gradle/configs/spotless/kotlin.header"))
    }
    
    format("misc") {
        target("**/*.md", "**/*.xml", "**/*.yml", "**/*.yaml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
```

#### Detekt 静态分析配置

```kotlin
// gradle/configs/detekt/detekt.yml
build:
  maxIssues: 0
  excludeCorrectable: false
  weights:
    complexity: 2
    formatting: 1
    style: 1
    comments: 1

config:
  validation: true
  warningsAsErrors: false

processors:
  active: true
  exclude:
    - "DetektProgressListener"

reports:
  html.enabled: true
  xml.enabled: true
  txt.enabled: false
  sarif.enabled: true

complexity:
  active: true
  ComplexMethod:
    active: true
    threshold: 15
  LongMethod:
    active: true
    threshold: 60
  LargeClass:
    active: true
    threshold: 200
```

#### SpotBugs 配置

```kotlin
// build.gradle.kts
spotbugs {
    ignoreFailures.set(false)
    showStackTraces.set(true)
    showProgress.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.HIGH)
    
    reportsDir.set(file("$buildDir/reports/spotbugs"))
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports {
        create("html") {
            enabled.set(true)
            outputLocation.set(file("$buildDir/reports/spotbugs/main.html"))
        }
        create("xml") {
            enabled.set(true)
            outputLocation.set(file("$buildDir/reports/spotbugs/main.xml"))
        }
    }
}
```

### Feature 插件配置

#### 编译配置

```kotlin
// build.gradle.kts
plugins {
    id("io.github.mymx2.feature.compile-java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.apply {
        encoding = "UTF-8"
        isIncremental = true
        isFork = true
        
        // 编译器参数
        compilerArgs.addAll(listOf(
            "-parameters",           // 保留方法参数名
            "-Xlint:all",           // 启用所有警告
            "-Xlint:-processing",   // 禁用注解处理警告
            "-Werror"               // 将警告视为错误
        ))
    }
}
```

#### 测试配置

```kotlin
// build.gradle.kts
tasks.named<Test>("test") {
    useJUnitPlatform()
    
    // JVM 参数
    jvmArgs = listOf(
        "-Xmx1g",
        "-XX:+HeapDumpOnOutOfMemoryError"
    )
    
    // 测试系统属性
    systemProperties["junit.jupiter.execution.parallel.enabled"] = true
    systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
    
    // 测试过滤器
    filter {
        includeTestsMatching("*Test")
        includeTestsMatching("*Tests")
        excludeTestsMatching("*IntegrationTest")
    }
    
    // 测试报告
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    
    // 失败时继续运行其他测试
    ignoreFailures = false
    
    // 超时设置
    timeout.set(Duration.ofMinutes(30))
}
```

#### 发布配置

```kotlin
// build.gradle.kts
plugins {
    id("io.github.mymx2.feature.publish-library")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "io.github.yourname"
            artifactId = project.name
            version = project.version.toString()
            
            from(components["java"])
            
            // 附加源码和文档
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
            
            // POM 元数据
            pom {
                name.set(project.name)
                description.set("Project description")
                url.set("https://github.com/yourname/project")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("yourname")
                        name.set("Your Name")
                        email.set("your.email@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/yourname/project.git")
                    developerConnection.set("scm:git:ssh://github.com/yourname/project.git")
                    url.set("https://github.com/yourname/project")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("mavenCentralUsername")?.toString()
                    ?: System.getenv("MAVEN_USERNAME")
                password = project.findProperty("mavenCentralPassword")?.toString()
                    ?: System.getenv("MAVEN_PASSWORD")
            }
        }
        
        maven {
            name = "Snapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = project.findProperty("mavenCentralUsername")?.toString()
                    ?: System.getenv("MAVEN_USERNAME")
                password = project.findProperty("mavenCentralPassword")?.toString()
                    ?: System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

// GPG 签名
signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD")
    )
    sign(publishing.publications["mavenJava"])
}
```

## 📦 依赖管理配置

### libs.versions.toml 完整示例

```toml
[versions]
# 平台版本
jdk = "21"
kotlin = "1.9.20"
gradle = "8.5"

# 主要依赖版本
spring-boot = "3.2.0"
spring-cloud = "2023.0.0"
hibernate = "6.4.1.Final"
postgresql = "42.7.1"

# 工具库版本
guava = "32.1.3-jre"
lombok = "1.18.30"
mapstruct = "1.5.5.Final"
jackson = "2.16.0"

# 测试版本
junit = "5.10.1"
mockito = "5.8.0"
assertj = "3.24.2"
testcontainers = "1.19.3"

# 代码质量版本
spotless = "6.23.3"
detekt = "1.23.4"
spotbugs = "4.8.2"

[libraries]
# Spring Boot
spring-boot-starter = { module = "org.springframework.boot:spring-boot-starter", version.ref = "spring-boot" }
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa", version.ref = "spring-boot" }

# Kotlin
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlin-reflect = { module = "org.jetbrains.kotlin:kotlin-reflect", version.ref = "kotlin" }

# 工具库
guava = { module = "com.google.guava:guava", version.ref = "guava" }
lombok = { module = "org.projectlombok:lombok", version.ref = "lombok" }
mapstruct = { module = "org.mapstruct:mapstruct", version.ref = "mapstruct" }
jackson-databind = { module = "com.fasterxml.jackson.core:jackson-databind", version.ref = "jackson" }

# 数据库
postgresql = { module = "org.postgresql:postgresql", version.ref = "postgresql" }
hibernate-core = { module = "org.hibernate.orm:hibernate-core", version.ref = "hibernate" }

# 测试
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
mockito-core = { module = "org.mockito:mockito-core", version.ref = "mockito" }
assertj-core = { module = "org.assertj:assertj-core", version.ref = "assertj" }
testcontainers = { module = "org.testcontainers:testcontainers", version.ref = "testcontainers" }

[bundles]
# 常用依赖束
spring-web = ["spring-boot-starter", "spring-boot-starter-web"]
spring-data = ["spring-boot-starter-data-jpa", "hibernate-core", "postgresql"]
testing = ["junit-jupiter", "mockito-core", "assertj-core"]
kotlin = ["kotlin-stdlib", "kotlin-reflect"]

[plugins]
# Gradle 插件版本
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

### 在构建脚本中使用

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // 使用版本目录
    implementation(libs.spring.boot.starter)
    implementation(libs.bundles.spring.web)
    implementation(libs.bundles.kotlin)
    
    runtimeOnly(libs.postgresql)
    
    // 注解处理器
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct)
    
    // 测试依赖
    testImplementation(libs.bundles.testing)
    testImplementation(libs.testcontainers)
}
```

## ⚡ 任务配置

### 自定义任务

```kotlin
// build.gradle.kts
tasks.register("customTask") {
    group = "Custom"
    description = "A custom task example"
    
    doLast {
        println("Executing custom task...")
    }
}

// 带输入输出的任务
tasks.register<Copy>("copyResources") {
    group = "Build"
    description = "Copy resources to output directory"
    
    from("src/main/resources")
    into("$buildDir/resources")
    include("**/*.properties", "**/*.yml")
    
    filteringCharset = "UTF-8"
}

// JavaExec 任务
tasks.register<JavaExec>("runMain") {
    group = "Application"
    description = "Run the main class"
    
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.example.Main")
    jvmArgs = listOf("-Xmx512m")
    args = listOf("--config", "application.yml")
}
```

### 任务依赖和顺序

```kotlin
// 设置任务依赖
tasks.named("build") {
    dependsOn("customTask")
}

// 设置任务顺序
tasks.named("compileJava") {
    mustRunAfter("generateSources")
}

// 最终化任务
tasks.named("test") {
    finalizedBy("jacocoTestReport")
}
```

## 🚀 性能优化配置

### settings.gradle.kts 优化

```kotlin
// settings.gradle.kts

// 启用构建缓存
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
    
    remote<HttpBuildCache> {
        isEnabled = true
        url = uri("https://your-build-cache-server.com/cache/")
        isPush = System.getenv("CI") != null
        credentials {
            username = System.getenv("CACHE_USERNAME")
            password = System.getenv("CACHE_PASSWORD")
        }
    }
}

// 配置缓存
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

// 并行执行
gradle.startParameter.isParallelProjectExecutionEnabled = true
```

### 增量编译配置

```kotlin
// build.gradle.kts
tasks.withType<AbstractCompile>().configureEach {
    options.isIncremental = true
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        incremental = true
    }
}
```

### 依赖解析优化

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    
    repositories {
        // 使用镜像加速（中国大陆）
        maven { 
            url = uri("https://maven.aliyun.com/repository/public") 
        }
        mavenCentral()
        mavenLocal()
    }
}
```

## 📝 检查清单

配置完成后，请检查：

- [ ] `gradle.properties` 已正确配置 JVM 参数
- [ ] 敏感信息已移至环境变量
- [ ] `libs.versions.toml` 包含所有依赖版本
- [ ] 代码格式化工具已配置
- [ ] 静态分析工具规则已定制
- [ ] 测试框架和覆盖率已配置
- [ ] 发布配置包含正确的仓库和签名
- [ ] 构建缓存已启用
- [ ] 并行构建已启用

## ➡️ 下一步

- [📝 最佳实践](/guide/best-practices) - 学习行业经验
- [🔧 故障排除](/guide/troubleshooting) - 解决常见问题
- [📖 完整示例](/examples/markdown-examples) - 查看实际配置
