@file:Suppress("UnstableApiUsage")

import io.fuchs.gradle.collisiondetector.DetectCollisionsTask
import io.github.mymx2.plugin.InternalDependencies
import io.github.mymx2.plugin.libs
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.base.TestingExtension

object PluginHelpers {

  /** 从版本目录查找依赖坐标，找不到时回退到 InternalDependencies。 */
  fun Project.libsOrInternal(name: String): Any = runCatching {
    libs.findLibrary(name).get().get()
  }
    .getOrElse { InternalDependencies.useLibrary(name) }

  /** 在项目目录查找工具配置文件，找不到时回退到根项目的 gradle/configs/ 目录。 */
  fun Project.findToolConfig(tool: String, filename: String): java.io.File? =
    layout.projectDirectory.file("configs/$tool/$filename").asFile.takeIf { it.exists() }
      ?: isolated.rootProject.projectDirectory
        .file("gradle/configs/$tool/$filename")
        .asFile
        .takeIf { it.exists() }

  /** Enables the collision detection plugin. */
  fun Project.enableCollisionDetection() {
    tasks.named("qualityCheck") { dependsOn(tasks.withType<DetectCollisionsTask>()) }

    tasks.named("qualityGate") { dependsOn(tasks.withType<DetectCollisionsTask>()) }
  }

  /**
   * Enables the testing suite with dependencies.
   *
   * effect:
   * ```
   * dependencies {
   *   implementation(platform("org.junit:junit-bom:<version>"))
   *   implementation(platform("org.assertj:assertj-bom:<version>"))
   *   runtimeOnly("org.junit.platform:junit-platform-launcher")
   *   implementation("org.junit.jupiter:junit-jupiter")
   *   implementation("org.assertj:assertj-core")
   * }
   * ```
   */
  fun Project.useJUnitJupiterM2(junitBomVersion: String = "", assertjBomVersion: String = "") {
    extensions.getByType(TestingExtension::class.java).suites.withType<JvmTestSuite> {
      dependencies {
        implementation(
          platform(
            if (junitBomVersion.isNotBlank()) {
              "org.junit:junit-bom:${junitBomVersion}"
            } else {
              libsOrInternal("junitBom").toString()
            }
          )
        )
        implementation(
          platform(
            if (assertjBomVersion.isNotBlank()) {
              "org.assertj:assertj-bom:${assertjBomVersion}"
            } else {
              libsOrInternal("assertjBom").toString()
            }
          )
        )
        runtimeOnly("org.junit.platform:junit-platform-launcher")
        implementation("org.junit.jupiter:junit-jupiter")
        implementation("org.assertj:assertj-core")
      }
    }
  }
}

/**
 * module-info style dependency block.
 *
 * ```
 * Usage:
 * jpmsModule {
 *   require("org.slf4j:slf4j-api:<version>")
 *   requireTransitive(project(":core"))
 * }
 * ```
 */
@Suppress("detekt:UnusedPrivateMember")
fun Project.jpmsModule(configuration: ModuleInfoDependencyScope.() -> Unit) {
  ModuleInfoDependencyScope(dependencies, false).configuration()
}

@Suppress("detekt:UnusedPrivateMember")
fun Project.testJpmsModule(configuration: ModuleInfoDependencyScope.() -> Unit) {
  ModuleInfoDependencyScope(dependencies, true).configuration()
}

/** 本地开发跳过开关：各 flag 自动随 SKIP_ALL_LOCAL 一并跳过。 */
class SkipFlags(private val project: Project) {
  private fun p(prop: LocalConfig.Props) = project.getPropOrDefault(prop).toBoolean()

  val allLocal: Boolean by lazy { p(LocalConfig.Props.SKIP_ALL_LOCAL) }
  val quality: Boolean by lazy { allLocal || p(LocalConfig.Props.SKIP_QUALITY) }
  val coverage: Boolean by lazy { allLocal || p(LocalConfig.Props.SKIP_COVERAGE) }
  val e2e: Boolean by lazy { allLocal || p(LocalConfig.Props.SKIP_E2E) }
  val doc: Boolean by lazy { allLocal || p(LocalConfig.Props.SKIP_DOC) }
  val integration: Boolean by lazy { allLocal || p(LocalConfig.Props.SKIP_INTEGRATION) }
}

val Project.skipFlags: SkipFlags
  get() = SkipFlags(this)

class ModuleInfoDependencyScope(
  private val dependencies: DependencyHandler,
  private val isTest: Boolean,
) {

  /** requires */
  fun requires(implementation: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testImplementation", implementation)
    } else {
      dependencies.add("implementation", implementation)
    }
  }

  /** requires transitive */
  fun requiresTransitive(api: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testApi", api)
    } else {
      dependencies.add("api", api)
    }
  }

  /** requires static */
  fun requiresStatic(compileOnly: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testCompileOnly", compileOnly)
    } else {
      dependencies.add("compileOnly", compileOnly)
    }
  }

  /** requires static transitive */
  fun requireStaticsTransitive(compileOnlyApi: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testCompileOnlyApi", compileOnlyApi)
    } else {
      dependencies.add("compileOnlyApi", compileOnlyApi)
    }
  }

  /** requires runtime */
  fun requiresRuntime(runtimeOnly: Any): Dependency? {
    return if (isTest) {
      dependencies.add("testRuntimeOnly", runtimeOnly)
    } else {
      dependencies.add("runtimeOnly", runtimeOnly)
    }
  }
}
