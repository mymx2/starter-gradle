# 快速开始

欢迎使用 Starter Gradle！本指南将帮助你在几分钟内开始使用这个强大的构建系统模板。

## 📋 前置要求

在开始之前，请确保你的开发环境满足以下要求：

| 工具 | 最低版本 | 推荐版本 |
|------|---------|---------|
| JDK | 17 | 21+ |
| Git | 2.0+ | 最新稳定版 |
| Gradle | 8.5 | 由 Wrapper 自动管理 |

### 检查 Java 版本

```bash
java -version
```

你应该看到类似输出：
```
openjdk version "21.0.1" 2023-10-17 LTS
OpenJDK Runtime Environment (build 21.0.1+12-LTS)
OpenJDK 64-Bit Server VM (build 21.0.1+12-LTS, mixed mode, sharing)
```

## 🚀 快速开始

### 方式一：使用模板创建新项目

#### 1. 克隆项目

```bash
git clone https://github.com/mymx2/starter-gradle.git my-new-project
cd my-new-project
```

#### 2. 设置执行权限

**Linux/macOS:**
```bash
chmod +x gradlew
```

**Windows (PowerShell):**
```powershell
.\gradlew.bat
```

#### 3. 验证安装

```bash
./gradlew --version
```

#### 4. 运行首次构建

```bash
./gradlew build
```

### 方式二：作为现有项目的构建系统

如果你已有项目，可以复制关键配置文件：

1. 复制 `gradle/` 目录到你的项目
2. 复制 `gradlew` 和 `gradlew.bat` 脚本
3. 复制 `settings.gradle.kts` 并根据需要修改
4. 在你的模块中应用相应的插件

## 📁 基础项目结构

Starter Gradle 提供了清晰的项目组织方式：

```
my-project/
├── gradle/
│   ├── build-logic/          # 约定插件（核心）
│   │   └── src/main/kotlin/
│   ├── configs/              # 工具配置
│   ├── wrapper/              # Gradle Wrapper
│   └── libs.versions.toml    # 依赖版本管理 ⭐
├── app/                      # 主应用模块
├── examples/                 # 示例模块
├── gradlew                   # Unix 启动脚本
├── gradlew.bat               # Windows 启动脚本
├── settings.gradle.kts       # 项目设置
└── build.gradle.kts          # 根构建配置
```

## 🔧 第一个构建配置

### 创建 Java 模块

1. 在 `settings.gradle.kts` 中添加模块：

```kotlin
include(":app")
```

2. 创建 `app/build.gradle.kts`：

```kotlin
plugins {
    id("io.github.mymx2.module.java")
}

dependencies {
    // 使用版本目录管理依赖
    implementation(libs.guava)
    testImplementation(libs.junit.jupiter)
}
```

3. 创建源代码目录：

```bash
mkdir -p app/src/main/java/com/example
mkdir -p app/src/test/java/com/example
```

4. 创建简单的 Java 类：

```java
// app/src/main/java/com/example/Hello.java
package com.example;

public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Starter Gradle!");
    }
}
```

5. 运行程序：

```bash
./gradlew :app:run
```

## 🎯 常用命令速查

### 基础命令

```bash
# 查看帮助
./gradlew help

# 查看所有可用任务
./gradlew tasks

# 清理构建
./gradlew clean

# 构建项目
./gradlew build
```

### 开发工作流

```bash
# 编译代码
./gradlew compileJava

# 运行测试
./gradlew test

# 格式化代码
./gradlew spotlessApply

# 运行所有检查
./gradlew check
```

### 依赖管理

```bash
# 查看依赖树
./gradlew dependencies

# 检查依赖更新
./gradlew dependencyUpdates
```

## 📦 依赖管理详解

### 版本目录（Version Catalog）

在 `gradle/libs.versions.toml` 中定义依赖：

```toml
[versions]
guava = "32.1.3-jre"
junit = "5.10.1"

[libraries]
guava = { module = "com.google.guava:guava", version.ref = "guava" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }

[bundles]
testing = ["junit-jupiter"]
```

在构建脚本中使用：

```kotlin
dependencies {
    implementation(libs.guava)
    testImplementation(libs.bundles.testing)
}
```

## ✅ 验证安装

运行以下命令确保一切正常工作：

```bash
# 1. 检查 Gradle 版本
./gradlew --version

# 2. 运行构建
./gradlew build

# 3. 运行测试
./gradlew test

# 4. 检查代码质量
./gradlew check
```

如果所有命令都成功执行，恭喜你！🎉 你已经成功配置好了 Starter Gradle。

## 🆘 遇到问题？

### 常见问题

**问题 1: `gradlew` 没有执行权限**

```bash
# 解决方案
chmod +x gradlew
```

**问题 2: Java 版本不兼容**

```bash
# 检查当前 Java 版本
java -version

# 解决方案：安装 JDK 21 或更高版本
```

**问题 3: 依赖下载失败**

```bash
# 清除缓存重试
./gradlew clean build --refresh-dependencies
```

### 获取帮助

- 📖 [完整文档](/guide/introduction)
- 💬 [GitHub Issues](https://github.com/mymx2/starter-gradle/issues)
- 🔍 [DeepWiki 分析](https://deepwiki.com/mymx2/starter-gradle)

## ➡️ 下一步

现在你已经完成了快速开始，可以继续学习：

- [📖 完整介绍](/guide/introduction) - 了解项目特性和架构
- [🔌 插件系统](/guide/plugins) - 深入学习约定插件
- [⚙️ 配置指南](/guide/configuration) - 自定义构建配置
- [📝 最佳实践](/guide/best-practices) - 学习行业最佳实践

祝你构建愉快！🚀
