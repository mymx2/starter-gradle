<p align="center">
  <a href="https://maven-badges.sml.io/maven-central/io.github.mymx2/example-kotlin" target="_blank"><img alt="maven-central-version"
    src="https://img.shields.io/maven-central/v/io.github.mymx2/example-kotlin?strategy=latestProperty"/></a>
  <a href="https://central.sonatype.com/repository/maven-snapshots/io/github/mymx2/example-kotlin/maven-metadata.xml" target="_blank"><img alt="maven-metadata-url"
    src="https://img.shields.io/maven-metadata/v?label=snapshot&metadataUrl=https://central.sonatype.com/repository/maven-snapshots/io/github/mymx2/example-kotlin/maven-metadata.xml&strategy=latestProperty"/></a>
  <a href="https://github.com/mymx2/starter-gradle" target="_blank"><img alt="git-hub-release"
    src="https://img.shields.io/github/v/release/mymx2/starter-gradle"/></a>
</p>

<p align="center">
  <a href="https://app.codacy.com/gh/mymx2/starter-gradle" target="_blank"><img alt="codacy-grade"
    src="https://img.shields.io/codacy/grade/a2f3fd9b1e564fa3a3b558d1dfaf2a34"/></a>
  <a href="https://app.codecov.io/gh/mymx2/starter-gradle" target="_blank"><img alt="codecov"
    src="https://img.shields.io/codecov/c/github/mymx2/starter-gradle"/></a>
  <a href="https://github.com/mymx2/starter-gradle" target="_blank"><img alt="git-hub-actions-workflow-status"
    src="https://img.shields.io/github/actions/workflow/status/mymx2/starter-gradle/pull-request-check.yml"/></a>
</p>

<p align="center">
  <a href="https://jdk.java.net" target="_blank"><img alt="JDK"
    src="https://img.shields.io/badge/dynamic/toml?logo=openjdk&label=JDK&color=brightgreen&url=https%3A%2F%2Fraw.githubusercontent.com%2Fmymx2%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.jdk&suffix=%2B"/></a>
  <a href="https://gradle.org" target="_blank"><img alt="GRADLE"
    src="https://img.shields.io/badge/dynamic/toml?logo=gradle&label=Gradle&color=209BC4&url=https%3A%2F%2Fraw.githubusercontent.com%2Fmymx2%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.gradle"/></a>
  <a href="https://kotlinlang.org/docs/getting-started.html" target="_blank"><img alt="KOTLIN"
    src="https://img.shields.io/badge/dynamic/toml?logo=kotlin&label=Kotlin&color=7f52ff&url=https%3A%2F%2Fraw.githubusercontent.com%2Fmymx2%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.kotlin"/></a>
  <a href="https://nodejs.org/en/download" target="_blank"><img alt="NODE"
    src="https://img.shields.io/badge/dynamic/toml?logo=nodedotjs&label=Node&color=5FA04E&url=https%3A%2F%2Fraw.githubusercontent.com%2Fmymx2%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.node"/></a>
</p>

<p align="center">
  <a href="https://github.com/mymx2" target="_blank"><img alt="mymx2"
      src="https://img.shields.io/badge/author-🤖_mymx2-E07A28?logo=github"/></a>
  <a href="https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax" target="_blank"><img alt="Markdown"
      src="https://img.shields.io/badge/md-GFM-0070C0?logo=Markdown"/></a>
  <a href="https://github.com/mymx2/starter-gradle" target="_blank"><img alt="git-hub-license"
        src="https://img.shields.io/github/license/mymx2/starter-gradle"/></a>
  <a href="https://deepwiki.com/mymx2/starter-gradle" target="_blank"><img alt="Ask DeepWiki"
      src="https://deepwiki.com/badge.svg"/></a>
</p>

## 🏕️ Project Template

This repo contains a Gradle project structure with:

- **Centralized and maintainable** build configuration and custom build logic
- **No dependency hell** through smart dependency management with dependency rules and analysis

The starter project contains everything for a traditional JVM project.
The structure though, is good for any kind of project you may build with Gradle
(**Kotlin**, **Groovy**, **Scala**, ...).

## 🧱 Project Overview

You can find a detailed explanation of the project structure
in [Gradle Basics](https://docs.gradle.org/current/userguide/gradle_basics.html).

### Core Concepts

![Gradle Project Structure](https://docs.gradle.org/current/userguide/img/gradle-basic-1.png)

### Project Structure

```
project
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── settings.gradle(.kts)
├── subproject-a
│   ├── build.gradle(.kts)
│   └── src
└── subproject-b
    ├── build.gradle(.kts)
    └── src
```

### Build Lifecycle

![Build Lifecycle](https://docs.gradle.org/current/userguide/img/build-lifecycle-example.png)

### Configuration Cache

![Configuration Cache](https://docs.gradle.org/nightly/userguide/img/configuration-cache-4.png)

The [Configuration Cache](https://docs.gradle.org/nightly/userguide/configuration_cache.html) improves build performance
by caching the result of the configuration phase and reusing it for subsequent builds.

### Dependency Scopes

![https://docs.gradle.org/nightly/userguide/java_library_plugin.html#sec:java_library_configurations_graph](https://docs.gradle.org/nightly/userguide/img/java-library-ignore-deprecated-main.png)

Mapping between Java module directives and Gradle configurations to declare
dependencies: [declaring_module_dependencies](https://docs.gradle.org/nightly/userguide/java_library_plugin.html#declaring_module_dependencies)

## 🍰 Project Usage

you can run `./gradlew help` to get
gradle [commands-line usage help](https://docs.gradle.org/current/userguide/command_line_interface.html).

## 🏝️ Thanks

This project is heavily inspired by the following awesome projects.

- [https://github.com/jjohannes/gradle-project-setup-howto](https://github.com/jjohannes/gradle-project-setup-howto)
- [https://github.com/hiero-ledger/hiero-gradle-conventions](https://github.com/hiero-ledger/hiero-gradle-conventions)
- [https://github.com/android/nowinandroid](https://github.com/android/nowinandroid)
