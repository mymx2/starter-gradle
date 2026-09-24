@file:Suppress("AvoidApplyPluginMethod")

import com.github.jengelman.gradle.plugins.shadow.ShadowBasePlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins { id("java") }

// 只 apply 部分 Shadow 插件（排除 ShadowApplicationPlugin），不能用 plugins{} 静态声明——
// apply() 是按类精确选择的合法用法，IDE 提示误报。
pluginManager.apply(ShadowBasePlugin::class)

pluginManager.apply(ShadowJavaPlugin::class)

tasks.withType<ShadowJar>().configureEach {
  group = "toolbox"
  // https://gradleup.com/shadow/changes/#migration-example
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  mergeServiceFiles()
  manifest { attributes("Multi-Release" to "true") }
  // There is an issue in the shadow plugin that it automatically accesses the
  // files in 'runtimeClasspath' while Gradle is building the task graph.
  // See: https://github.com/GradleUp/shadow/issues/882
  dependencyFilter = NoResolveDependencyFilter(project)
}

class NoResolveDependencyFilter(p: Project) : DependencyFilter.AbstractDependencyFilter(p) {

  override fun resolve(configuration: Configuration): FileCollection {
    // override, to not do early dependency resolution
    return configuration
  }

  override fun resolve(
    dependencies: Set<ResolvedDependency>,
    includedDependencies: MutableSet<ResolvedDependency>,
    excludedDependencies: MutableSet<ResolvedDependency>,
  ) {
    // implementation is copied from DefaultDependencyFilter
    dependencies.forEach {
      if (if (it.isIncluded()) includedDependencies.add(it) else excludedDependencies.add(it)) {
        resolve(it.children, includedDependencies, excludedDependencies)
      }
    }
  }
}
