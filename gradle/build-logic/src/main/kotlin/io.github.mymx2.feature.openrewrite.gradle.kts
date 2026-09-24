// OpenRewrite 的 rewrite configuration 由 rewrite-recipe-bom 统一管理版本，不走 libs.versions.toml——有意为之。
@file:Suppress("UnstableApiUsage", "detekt:SpreadOperator", "UseTomlInstead")

import PluginHelpers.findToolConfig
import io.github.mymx2.plugin.DefaultExcludes
import io.github.mymx2.plugin.resetTaskGroup

plugins { id("org.openrewrite.rewrite") }

dependencies {
  rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:latest.release"))
  rewrite("org.openrewrite:rewrite-java")
  rewrite("org.openrewrite:rewrite-kotlin")
  rewrite("org.openrewrite.recipe:rewrite-rewrite")
  rewrite("org.openrewrite.recipe:rewrite-static-analysis")
  rewrite("org.openrewrite.recipe:rewrite-migrate-java")
  rewrite("org.openrewrite.recipe:rewrite-testing-frameworks")
  rewrite("org.openrewrite.recipe:rewrite-spring")
  rewrite("org.openrewrite.recipe:rewrite-jackson")
  rewrite("org.openrewrite.recipe:rewrite-okhttp")
  rewrite("org.openrewrite.recipe:rewrite-openapi")
  rewrite("org.openrewrite.recipe:rewrite-third-party") {
    exclude(group = "commons-logging", module = "commons-logging")
  }
  rewrite("org.openrewrite.recipe:rewrite-java-security:latest.release")
}

val rewriteYml = findToolConfig("rewrite", "rewrite.yml")
val rewriteActiveRecipes = listOf("io.github.mymx2.openrewrite.SanityCheck")
val rewriteActiveStyles = listOf("io.github.mymx2.openrewrite.SpotlessFormat")

// default excludes.
val defaultRewriteExcludes = DefaultExcludes.GLOB.toTypedArray()

rewrite {
  // https://docs.openrewrite.org/reference/gradle-plugin-configuration#configuring-the-rewrite-dsl
  // default value is `<project directory>/rewrite.yml`
  if (rewriteYml != null) {
    configFile = rewriteYml
    activeRecipe(*rewriteActiveRecipes.toTypedArray())
    activeStyle(*rewriteActiveStyles.toTypedArray())
  }
  exclusion(*defaultRewriteExcludes)
  isExportDatatables = true
  failOnDryRunResults = true
}

listOf("rewriteDiscover" to "help", "rewriteDryRun" to "toolbox", "rewriteRun" to "toolbox")
  .forEach { resetTaskGroup(it.first, it.second) }
