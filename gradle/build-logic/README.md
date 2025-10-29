### The Convention Plugins

- [gradle-project-setup-howto](https://github.com/jjohannes/gradle-project-setup-howto)
- [organizing_gradle_projects](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html)

Convention plugins are used to configure each aspect of the build centrally. To keep it structured, we put
them into four categories: _Base_, _Feature_, _Check_, _Report_. Below you find all plugins listed. For more details,
inspect the corresponding plugin files.

DevSecOps toolchain:

- code-format
  - [Spotless](https://github.com/diffplug/spotless), [Detekt](https://github.com/detekt/detekt) [Checkstyle](https://github.com/checkstyle/checkstyle)
- code-quality
  - [Error Prone](https://github.com/PicnicSupermarket/error-prone-support), [Pmd](https://github.com/pmd/pmd), [Spotbugs](https://github.com/spotbugs/spotbugs)
- code-refactor
  - [OpenRewrite](https://github.com/openrewrite/rewrite)
- code-security(CVEs)
  - [Renovate](https://docs.renovatebot.com/), [DependencyCheck](https://github.com/dependency-check/DependencyCheck)
- code-coverage
  - [Jacoco](https://github.com/jacoco/jacoco), [kover](https://github.com/Kotlin/kotlinx-kover/issues/746)
- CI/CD(Continuous Integration/Continuous Deployment)
  - [Jenkins](https://www.jenkins.io/), [GitHub Actions](https://github.com/features/actions), [Yunxiao](https://devops.console.aliyun.com/organizations), [Travis](https://www.travis-ci.com)
- Tracing、Logging、Reporting、Monitoring、Automation...
- DevSecOps
  - ([CodeQL](https://codeql.github.com/), [Mend](https://www.mend.io/)), ([Codecov](https://about.codecov.io/)), ([Codacy](https://www.codacy.com/), [SonarQube](https://www.sonarsource.com/zh/), [Qodana](https://www.jetbrains.com.cn/qodana/))

Code Quality: `Dependency Security` `Test Coverage Visualization` `Comprehensive Analysis`

#### Build Script

_Build_ script plugins are applied to the root project.

[io.github.mymx2.build.feature.build-cache.settings.gradle.kts](src/main/kotlin/io.github.mymx2.build.feature.build-cache.settings.gradle.kts)
[io.github.mymx2.build.feature.catalogs.settings.gradle.kts](src/main/kotlin/io.github.mymx2.build.feature.catalogs.settings.gradle.kts)
[io.github.mymx2.build.feature.project-structure.settings.gradle.kts](src/main/kotlin/io.github.mymx2.build.feature.project-structure.settings.gradle.kts)
[io.github.mymx2.build.feature.repositories.settings.gradle.kts](src/main/kotlin/io.github.mymx2.build.feature.repositories.settings.gradle.kts)
[io.github.mymx2.build.report.develocity.settings.gradle.kts](src/main/kotlin/io.github.mymx2.build.report.develocity.settings.gradle.kts)
[io.github.mymx2.build.settings.gradle.kts](src/main/kotlin/io.github.mymx2.build.settings.gradle.kts)

#### Base Plugins

_Base_ plugins need to be used in all Modules to establish a certain foundation for the setup.
For example, the same dependency management configuration should be applied everywhere to consistently use the same
3rd party libraries everywhere.

[io.github.mymx2.base.identity.gradle.kts](src/main/kotlin/io.github.mymx2.base.identity.gradle.kts)
[io.github.mymx2.base.jpms-modules.gradle.kts](src/main/kotlin/io.github.mymx2.base.jpms-modules.gradle.kts)
[io.github.mymx2.base.lifecycle.gradle.kts](src/main/kotlin/io.github.mymx2.base.lifecycle.gradle.kts)

#### Check Plugins (optional)

Check plugins help with keeping the software maintainable over time.
They check things like the dependency setup or code formatting.
More style checkers or static code analysis tools could be added in this category.
Check plugins are not necessarily needed to build a working software.

[io.github.mymx2.check.dependencies.gradle.kts](src/main/kotlin/io.github.mymx2.check.dependencies.gradle.kts)
[io.github.mymx2.check.dependencies-root.gradle.kts](src/main/kotlin/io.github.mymx2.check.dependencies-root.gradle.kts)
[io.github.mymx2.check.format-base.gradle.kts](src/main/kotlin/io.github.mymx2.check.format-base.gradle.kts)
[io.github.mymx2.check.format-gradle.gradle.kts](src/main/kotlin/io.github.mymx2.check.format-gradle.gradle.kts)
[io.github.mymx2.check.format-gradle-root.gradle.kts](src/main/kotlin/io.github.mymx2.check.format-gradle-root.gradle.kts)
[io.github.mymx2.check.format-java.gradle.kts](src/main/kotlin/io.github.mymx2.check.format-java.gradle.kts)
[io.github.mymx2.check.format-kotlin.gradle.kts](src/main/kotlin/io.github.mymx2.check.format-kotlin.gradle.kts)
[io.github.mymx2.check.format-misc.gradle.kts](src/main/kotlin/io.github.mymx2.check.format-misc.gradle.kts)
[io.github.mymx2.check.quality-check-style.gradle.kts](src/main/kotlin/io.github.mymx2.check.quality-check-style.gradle.kts)
[io.github.mymx2.check.quality-detekt.gradle.kts](src/main/kotlin/io.github.mymx2.check.quality-detekt.gradle.kts)
[io.github.mymx2.check.quality-nullaway.gradle.kts](src/main/kotlin/io.github.mymx2.check.quality-nullaway.gradle.kts)
[io.github.mymx2.check.quality-pmd.gradle.kts](src/main/kotlin/io.github.mymx2.check.quality-pmd.gradle.kts)
[io.github.mymx2.check.quality-spotbugs.gradle.kts](src/main/kotlin/io.github.mymx2.check.quality-spotbugs.gradle.kts)

#### Report Plugins (optional)

_Report_ plugins add reporting functionality to discover potential issues with the software or the build setup.
They may generate data that is picked up and displayed by external tools like
[Develocity](https://scans.gradle.com/) or [Dependency Track](https://dependencytrack.org/).
More reporting tools may be integrated in this category.
Report plugins are not necessarily needed to build a working software.

[io.github.mymx2.report.code-coverage.gradle.kts](src/main/kotlin/io.github.mymx2.report.code-coverage.gradle.kts)
[io.github.mymx2.report.sbom.gradle.kts](src/main/kotlin/io.github.mymx2.report.sbom.gradle.kts)
[io.github.mymx2.report.test.gradle.kts](src/main/kotlin/io.github.mymx2.report.test.gradle.kts)

#### Feature Plugins (optional)

Each _feature_ plugin configures one aspect of building the different software(java, android, kotlin, etc.) – like
_compiling code_ or _testing code_.

[io.github.mymx2.feature.checksum.gradle.kts](src/main/kotlin/io.github.mymx2.feature.checksum.gradle.kts)
[io.github.mymx2.feature.compile-java.gradle.kts](src/main/kotlin/io.github.mymx2.feature.compile-java.gradle.kts)
[io.github.mymx2.feature.compile-java-ext.gradle.kts](src/main/kotlin/io.github.mymx2.feature.compile-java-ext.gradle.kts)
[io.github.mymx2.feature.compile-kotlin.gradle.kts](src/main/kotlin/io.github.mymx2.feature.compile-kotlin.gradle.kts)
[io.github.mymx2.feature.compile-kotlin-ext.gradle.kts](src/main/kotlin/io.github.mymx2.feature.compile-kotlin-ext.gradle.kts)
[io.github.mymx2.feature.copy-jar.gradle.kts](src/main/kotlin/io.github.mymx2.feature.copy-jar.gradle.kts)
[io.github.mymx2.feature.doc-java.gradle.kts](src/main/kotlin/io.github.mymx2.feature.doc-java.gradle.kts)
[io.github.mymx2.feature.doc-kotlin.gradle.kts](src/main/kotlin/io.github.mymx2.feature.doc-kotlin.gradle.kts)
[io.github.mymx2.feature.git-hook.gradle.kts](src/main/kotlin/io.github.mymx2.feature.git-hook.gradle.kts)
[io.github.mymx2.feature.openrewrite.gradle.kts](src/main/kotlin/io.github.mymx2.feature.openrewrite.gradle.kts)
[io.github.mymx2.feature.publish-base.gradle.kts](src/main/kotlin/io.github.mymx2.feature.publish-base.gradle.kts)
[io.github.mymx2.feature.publish-bom.gradle.kts](src/main/kotlin/io.github.mymx2.feature.publish-bom.gradle.kts)
[io.github.mymx2.feature.publish-library.gradle.kts](src/main/kotlin/io.github.mymx2.feature.publish-library.gradle.kts)
[io.github.mymx2.feature.publish-vanniktech.gradle.kts](src/main/kotlin/io.github.mymx2.feature.publish-vanniktech.gradle.kts)
[io.github.mymx2.feature.shadow.gradle.kts](src/main/kotlin/io.github.mymx2.feature.shadow.gradle.kts)
[io.github.mymx2.feature.test.gradle.kts](src/main/kotlin/io.github.mymx2.feature.test.gradle.kts)
[io.github.mymx2.feature.test-end2end.gradle.kts](src/main/kotlin/io.github.mymx2.feature.test-end2end.gradle.kts)
[io.github.mymx2.feature.test-fixtures.gradle.kts](src/main/kotlin/io.github.mymx2.feature.test-fixtures.gradle.kts)
[io.github.mymx2.feature.use-all-catalog-versions.gradle.kts](src/main/kotlin/io.github.mymx2.feature.use-all-catalog-versions.gradle.kts)
[io.github.mymx2.feature.war.gradle.kts](src/main/kotlin/io.github.mymx2.feature.war.gradle.kts)
[io.github.mymx2.tools.check-version.gradle.kts](src/main/kotlin/io.github.mymx2.tools.check-version.gradle.kts)
[io.github.mymx2.tools.spring-openapi.gradle.kts](src/main/kotlin/io.github.mymx2.tools.spring-openapi.gradle.kts)

#### Module Plugins

_Module_ plugins combine plugins from all categories above to define _Module Types_ that are then used in the
`build.gradle.kts` files of the individual Modules of our software.

[io.github.mymx2.module.android.gradle.kts](src/main/kotlin/io.github.mymx2.module.android.gradle.kts)
[io.github.mymx2.module.app.gradle.kts](src/main/kotlin/io.github.mymx2.module.app.gradle.kts)
[io.github.mymx2.module.bom.gradle.kts](src/main/kotlin/io.github.mymx2.module.bom.gradle.kts)
[io.github.mymx2.module.java.gradle.kts](src/main/kotlin/io.github.mymx2.module.java.gradle.kts)
[io.github.mymx2.module.kotlin.gradle.kts](src/main/kotlin/io.github.mymx2.module.kotlin.gradle.kts)
[io.github.mymx2.module.spring-boot.gradle.kts](src/main/kotlin/io.github.mymx2.module.spring-boot.gradle.kts)
[io.github.mymx2.module.war.gradle.kts](src/main/kotlin/io.github.mymx2.module.war.gradle.kts)

#### Gradle Testing Plugins

The [Gradle TestKit](https://docs.gradle.org/current/userguide/test_kit.html) can be used to test plugins.
This can be helpful to enforce a certain structure, e.g. by testing if each plugin works on its own.
And if you add custom tasks and advanced logic, you can add tests for that.

[ConventionPluginTest.kt](src/test/kotlin/io/github/mymx2/ConventionPluginTest.kt)

### Continuously build and report using GitHub Actions and Dependabot

- `build.yaml` Configure GitHub to run builds and produce reports (
  👉[inspect](https://github.com/jjohannes/gradle-project-setup-howto/actions/workflows/build.yaml)). Integrates with:
  - [Develocity Build Scans](https://scans.gradle.com/) (👉[inspect](https://scans.gradle.com/s/h3odwhbjjd2qm))
  - [Gradle Remote Build Cache](https://docs.gradle.com/develocity/build-cache-node/)
  - [Reposilite](https://reposilite.com/) (👉[inspect](https://repo.onepiece.software/#/snapshots))
  - [Dependency Track](https://dependencytrack.org/)
- `dependabot.yml` Configure [Dependabot](https://github.com/dependabot) to automatically get
  version updates (👉[inspect](https://github.com/jjohannes/gradle-project-setup-howto/pulls/app%2Fdependabot))

## Links
