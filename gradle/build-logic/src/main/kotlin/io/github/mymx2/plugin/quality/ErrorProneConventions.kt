package io.github.mymx2.plugin.quality

import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

const val DEFAULT_ERRORPRONE_EXCLUDES = "(.*/)?(nocheck|autogen|generated)/.*\\.java"

fun defaultDisabledChecks(): List<String> =
  listOf("AddNullMarkedToClass", "MissingSummary", "Java8ApiChecker")

@Suppress("CanConvertToMultiDollarString")
fun defaultDisabledRules(): List<String> = listOf("ImmutableTableRules\\\$ImmutableTableBuilder")

fun Project.configureErrorProneWithNullaway() {
  tasks.withType(JavaCompile::class.java).configureEach {
    options.errorprone {
      excludedPaths.set(DEFAULT_ERRORPRONE_EXCLUDES)
      enabled.set(true)
      allSuggestionsAsWarnings.set(true)
      allDisabledChecksAsWarnings.set(true)
      disableWarningsInGeneratedCode.set(true)

      errorproneArgs.add(
        buildString {
          append("-XepOpt:Refaster:NamePattern=^")
          defaultDisabledRules().forEach { rule ->
            append("(?!")
            append(rule)
            append(".*)")
          }
          append(".*")
        }
      )
      defaultDisabledChecks().forEach { disable(it) }

      nullaway {
        jspecifyMode.set(true)
        onlyNullMarked.set(true)
        treatGeneratedAsUnannotated.set(true)
        checkOptionalEmptiness.set(true)
        assertsEnabled.set(true)
        handleTestAssertionLibraries.set(true)
        checkContracts.set(true)
        suggestSuppressions.set(true)
        suppressionNameAliases.add("NullAway")
        error()
      }
    }
  }
}
