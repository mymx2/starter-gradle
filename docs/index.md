---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "Starter Gradle 文档"
  text: "Gradle 构建系统模板"
  tagline: 提供约定插件、质量检查和 CI/CD 自动化支持
  image:
    src: /favicon.ico
    alt: Starter Gradle Logo
  actions:
    - theme: brand
      text: 🚀 快速开始
      link: /guide/quickstart
    - theme: alt
      text: 📖 完整文档
      link: /guide/introduction
    - theme: alt
      text: 💻 示例代码
      link: /examples/markdown-examples

features:
  - icon: 🔌
    title: 约定插件系统
    details: 预配置的构建插件，统一管理 Java、Kotlin、Spring Boot 等项目类型
  - icon: ✅
    title: 代码质量保证
    details: 集成 Spotless、Detekt、SpotBugs 等工具，自动检查代码规范和潜在问题
  - icon: 🔒
    title: 依赖安全管理
    details: 使用版本目录集中管理依赖，支持 CVE 漏洞扫描和自动更新
  - icon: 📊
    title: 测试与覆盖率
    details: 内置 JaCoCo/Kover 代码覆盖率，JUnit 5 测试框架支持
  - icon: 🚀
    title: CI/CD 集成
    details: GitHub Actions 配置，支持自动构建、测试和发布到 Maven Central
  - icon: 📦
    title: 多模块支持
    details: 清晰的项目结构，轻松管理大型多模块 Gradle 项目
---
