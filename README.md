[![maven-central-version](https://img.shields.io/maven-central/v/com.profiletailors/dy-gradle-plugin?strategy=latestProperty)](https://central.sonatype.com/artifact/com.profiletailors/dy-gradle-plugin)
[![maven-metadata-url](https://img.shields.io/maven-metadata/v?label=snapshot&metadataUrl=https://central.sonatype.com/repository/maven-snapshots/io/github/dallay/dy-gradle-plugin/maven-metadata.xml&strategy=latestProperty)](https://central.sonatype.com/repository/maven-snapshots/io/github/dallay/dy-gradle-plugin/maven-metadata.xml)
[![git-hub-release](https://img.shields.io/github/v/release/dallay/starter-gradle)](https://github.com/dallay/starter-gradle/releases)

[![codacy-grade](https://img.shields.io/codacy/grade/64109c17cc5c4ea090db54cb773621fe)](https://app.codacy.com/gh/dallay/starter-gradle/dashboard)
[![codecov](https://img.shields.io/codecov/c/github/dallay/starter-gradle)](https://app.codecov.io/gh/dallay/starter-gradle)
[![git-hub-actions-workflow-status](https://img.shields.io/github/actions/workflow/status/dallay/starter-gradle/publish-release.yml)](https://github.com/dallay/starter-gradle/actions/workflows/publish-release.yml)

[![JDK](https://img.shields.io/badge/dynamic/toml?logo=openjdk&label=JDK&color=brightgreen&url=https%3A%2F%2Fraw.githubusercontent.com%2Fdallay%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.jdk&suffix=%2B)](https://jdk.java.net)
[![GRADLE](https://img.shields.io/badge/dynamic/toml?logo=gradle&label=Gradle&color=209BC4&url=https%3A%2F%2Fraw.githubusercontent.com%2Fdallay%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.gradle)](https://gradle.org)
[![KOTLIN](https://img.shields.io/badge/dynamic/toml?logo=kotlin&label=Kotlin&color=7f52ff&url=https%3A%2F%2Fraw.githubusercontent.com%2Fdallay%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.kotlin)](https://kotlinlang.org/docs/getting-started.html)
[![NODE](https://img.shields.io/badge/dynamic/toml?logo=nodedotjs&label=Node&color=5FA04E&url=https%3A%2F%2Fraw.githubusercontent.com%2Fdallay%2Fstarter-gradle%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.node)](https://nodejs.org/en/download)

[![dallay](https://img.shields.io/badge/author-🤖_dallay-E07A28?logo=github)](https://github.com/dallay)
[![Markdown](https://img.shields.io/badge/md-GFM-0070C0?logo=Markdown)](https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax)
[![git-hub-license](https://img.shields.io/github/license/dallay/starter-gradle)](https://github.com/dallay/starter-gradle)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/dallay/starter-gradle)

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

![https://docs.gradle.org/nightly/userguide/declaring_configurations.html#sec:resolvable-consumable-configs](https://docs.gradle.org/nightly/userguide/img/dependency-management-java-configurations.png)

Mapping between Java module directives and Gradle configurations to declare
dependencies: [declaring_module_dependencies](https://docs.gradle.org/nightly/userguide/java_library_plugin.html#declaring_module_dependencies)

## 🍰 Project Usage

you can run `./gradlew help` to get
gradle [commands-line usage help](https://docs.gradle.org/current/userguide/command_line_interface.html).

you can run `git update-index --chmod=+x gradlew` to make gradlew executable.

```shell
git update-index --chmod=+x gradlew
```

## 🏝️ Thanks

This project is heavily inspired by the following awesome projects.

- [https://github.com/jjohannes/gradle-project-setup-howto](https://github.com/jjohannes/gradle-project-setup-howto)
- [https://github.com/hiero-ledger/hiero-gradle-conventions](https://github.com/hiero-ledger/hiero-gradle-conventions)
- [https://github.com/android/nowinandroid](https://github.com/android/nowinandroid)
