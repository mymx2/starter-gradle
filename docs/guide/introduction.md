# 项目介绍

Starter Gradle 是一个功能完善的 Gradle 构建系统模板，专为现代 JVM 项目设计。它提供了开箱即用的最佳实践配置，让你能够专注于业务逻辑而非构建配置。

## 🎯 项目目标

### 解决的核心问题

1. **构建配置复杂** - 传统的 Gradle 项目需要大量重复的配置工作
2. **依赖管理混乱** - 版本冲突、依赖地狱是常见问题
3. **代码质量不一** - 缺少统一的代码规范和质量检查
4. **CI/CD 集成困难** - 持续集成配置繁琐且易出错

### 设计理念

- **约定优于配置** - 提供合理的默认配置，减少样板代码
- **模块化设计** - 清晰的插件分类，易于理解和扩展
- **质量优先** - 内置多种代码质量检查工具
- **自动化驱动** - 与 CI/CD 工具无缝集成

## ✨ 核心特性

### 🔌 约定插件系统

Starter Gradle 的核心是一套精心设计的约定插件，分为五大类：

#### Build 插件（构建设置）
配置全局构建行为：
- `io.github.mymx2.build.settings` - 基础设置
- `io.github.mymx2.build.feature.build-cache` - 构建缓存优化
- `io.github.mymx2.build.feature.repositories` - 仓库配置
- `io.github.mymx2.build.feature.catalogs` - 版本目录支持

#### Base 插件（基础配置）
所有模块的通用配置：
- `io.github.mymx2.base.identity` - 项目标识和元数据
- `io.github.mymx2.base.lifecycle` - 生命周期管理
- `io.github.mymx2.base.jvm-conflict` - JVM 兼容性处理
- `io.github.mymx2.base.jpms-modules` - Java 模块系统支持

#### Check 插件（质量检查）
代码质量和格式检查工具：

| 插件 | 工具 | 作用 |
|------|------|------|
| `format-java` | Spotless | Java 代码格式化 |
| `format-kotlin` | Spotless + ktfmt | Kotlin 代码格式化 |
| `format-gradle` | Spotless | Gradle 脚本格式化 |
| `quality-detekt` | Detekt | Kotlin 静态分析 |
| `quality-spotbugs` | SpotBugs | Java Bug 检测 |
| `quality-pmd` | PMD | 源代码分析 |
| `quality-nullaway` | NullAway | 空指针检查 |

#### Feature 插件（功能扩展）
特定功能支持：
- `compile-java` / `compile-kotlin` - 编译配置
- `test` - 测试框架配置（JUnit 5）
- `doc-java` / `doc-kotlin` - 文档生成
- `publish-library` - 库发布到 Maven Central
- `shadow` - 创建 Fat JAR
- `openrewrite` - 自动化代码重构

#### Module 插件（模块类型）
组合上述插件定义标准模块类型：

```kotlin
// Java 库模块
plugins {
    id("io.github.mymx2.module.java")
}

// Kotlin 库模块
plugins {
    id("io.github.mymx2.module.kotlin")
}

// Spring Boot 应用
plugins {
    id("io.github.mymx2.module.spring-boot")
}

// BOM (Bill of Materials) 模块
plugins {
    id("io.github.mymx2.module.bom")
}
```

### 🔒 依赖安全管理

#### 版本目录（Version Catalogs）

使用 `gradle/libs.versions.toml` 集中管理所有依赖：

```toml
[versions]
# 统一版本定义
spring-boot = "3.2.0"
kotlin = "1.9.20"
junit = "5.10.1"

[libraries]
# 声明依赖
spring-boot-starter = { module = "org.springframework.boot:spring-boot-starter" }
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }

[bundles]
# 依赖束
spring-web = ["spring-boot-starter", "spring-boot-starter-web"]
```

优势：
- ✅ 单一事实来源，避免版本不一致
- ✅ IDE 自动补全和类型安全
- ✅ 轻松升级依赖版本
- ✅ 支持依赖束，简化常用组合

#### 依赖规则检查

内置依赖规则检查插件，防止常见错误：
- 禁止循环依赖
- 检查依赖范围使用是否正确
- 识别未使用的依赖
- 检测传递依赖冲突

### 📊 测试与覆盖率

#### 测试框架支持

- **JUnit 5** - 默认测试框架
- **Mockito** - Mock 对象支持
- **AssertJ** - 流式断言
- **TestContainers** - 集成测试容器

#### 代码覆盖率

- **JaCoCo** - Java 代码覆盖率
- **Kover** - Kotlin 代码覆盖率
- 自动生成 HTML 和 XML 报告
- 可配置覆盖率阈值

```bash
# 运行测试并生成覆盖率报告
./gradlew test jacocoTestReport

# 查看 HTML 报告
open build/reports/jacoco/test/html/index.html
```

### 🚀 CI/CD 集成

#### GitHub Actions

预配置的工作流：

| 工作流 | 触发条件 | 功能 |
|--------|---------|------|
| `build.yml` | PR/Push | 构建、测试、质量检查 |
| `publish-release.yml` | Release Tag | 发布到 Maven Central |
| `publish-snapshot.yml` | Push to main | 发布快照版本 |
| `codeql.yml` | 定期/Schedule | CodeQL 安全扫描 |

#### 集成的外部服务

- **Develocity** - 构建扫描和性能分析
- **Dependency Track** - SBOM 和漏洞跟踪
- **Codecov** - 覆盖率可视化
- **Codacy/SonarQube** - 代码质量分析
- **Renovate** - 依赖自动更新

### 📦 多模块项目管理

#### 推荐的项目结构

```
my-project/
├── gradle/
│   ├── build-logic/          # 约定插件
│   └── libs.versions.toml    # 依赖管理
├── bom/                      # BOM 模块（可选）
├── core/                     # 核心模块
│   ├── api/                  # 公共 API
│   ├── impl/                 # 实现
│   └── test-fixtures/        # 测试夹具
├── features/                 # 功能模块
│   ├── feature-a/
│   └── feature-b/
├── applications/             # 应用模块
│   ├── app-cli/
│   └── app-web/
└── examples/                 # 示例代码
```

#### 模块间依赖规则

```kotlin
// 允许的依赖方向
applications → features → core → api
examples → applications, features, core
```

## 🏗️ 架构概览

### 构建流程

```
┌─────────────┐
│ 开发者操作   │
│ git push    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ GitHub Actions  │
│ 触发 CI 流水线   │
└──────┬──────────┘
       │
       ▼
┌─────────────────────┐
│ 应用约定插件         │
│ - Base Plugins      │
│ - Feature Plugins   │
│ - Check Plugins     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ 执行构建任务         │
│ - compileJava       │
│ - test              │
│ - spotlessCheck     │
│ - detekt            │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ 生成报告和产物       │
│ - JAR/WAR           │
│ - Test Reports      │
│ - Coverage Reports  │
│ - SBOM              │
└─────────────────────┘
```

### 插件层级关系

```
Module Plugins (模块插件)
    │
    ├── Base Plugins (基础插件)
    ├── Feature Plugins (功能插件)
    ├── Check Plugins (检查插件)
    └── Report Plugins (报告插件)
```

## 📈 与其他方案对比

| 特性 | Starter Gradle | 原生 Gradle | Spring Initializr |
|------|---------------|-------------|-------------------|
| 约定插件 | ✅ 完整体系 | ❌ 需手动配置 | ⚠️ 基础配置 |
| 依赖管理 | ✅ 版本目录 | ⚠️ 需自行组织 | ✅ 预设依赖 |
| 代码质量 | ✅ 多种工具 | ❌ 需单独配置 | ⚠️ 有限支持 |
| CI/CD | ✅ 开箱即用 | ❌ 需手动配置 | ⚠️ 基础配置 |
| 多模块 | ✅ 最佳实践 | ⚠️ 需自行设计 | ❌ 单模块为主 |
| 灵活性 | ✅ 高度可定制 | ✅ 完全灵活 | ⚠️ 受限于模板 |

## 🎓 适用场景

### 推荐使用

✅ **企业级应用开发**
- 需要长期维护的大型项目
- 多人协作的开发团队
- 对代码质量有严格要求

✅ **开源库开发**
- 需要发布到 Maven Central
- 需要完善的文档和示例
- 需要自动化发布流程

✅ **微服务架构**
- 多个相关服务的统一管理
- 共享的构建配置和依赖
- 一致的代码质量标准

### 可能不适合

❌ **快速原型验证**
- 一次性实验项目
- 不需要长期维护的代码

❌ **超小型项目**
- 单个源文件的工具脚本
- 简单的学习练习

## 📚 学习资源

### 官方文档

- [Gradle 用户指南](https://docs.gradle.org/current/userguide/userguide.html)
- [Kotlin DSL 文档](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [版本目录](https://docs.gradle.org/current/userguide/platforms.html)

### 社区资源

- [Gradle 论坛](https://discuss.gradle.org/)
- [Stack Overflow - Gradle 标签](https://stackoverflow.com/questions/tagged/gradle)
- [r/gradle](https://www.reddit.com/r/gradle/)

## ➡️ 继续阅读

- [🚀 快速开始](/guide/quickstart) - 立即上手
- [🔌 插件详解](/guide/plugins) - 深入了解插件系统
- [⚙️ 配置指南](/guide/configuration) - 自定义构建行为
- [📝 最佳实践](/guide/best-practices) - 学习行业经验
