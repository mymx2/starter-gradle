import io.github.mymx2.plugin.InternalDependencies
import io.github.mymx2.plugin.libs
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
  java
  // https://github.com/tbroyer/gradle-errorprone-plugin
  id("net.ltgt.errorprone")
  // https://github.com/tbroyer/gradle-nullaway-plugin
  id("net.ltgt.nullaway")
}

dependencies {
  compileOnly(
    runCatching { libs.findLibrary("jspecify").get().get() }
      .getOrElse { InternalDependencies.get("jspecify").let { "${it.module}:${it.version}" } }
  )
  errorprone(
    runCatching { libs.findLibrary("errorprone").get().get() }
      .getOrElse { InternalDependencies.get("errorProneCore").let { "${it.module}:${it.version}" } }
  )
  // https://github.com/PicnicSupermarket/error-prone-support/tree/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns
  errorprone(InternalDependencies.get("errorProneContrib").let { "${it.module}:${it.version}" })
  // https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/
  errorprone(InternalDependencies.get("refasterRunner").let { "${it.module}:${it.version}" })
  errorprone(
    runCatching { libs.findLibrary("nullaway").get().get() }
      .getOrElse { InternalDependencies.get("nullaway").let { "${it.module}:${it.version}" } }
  )
}

// https://github.com/google/error-prone/issues/623
// default excludes.
val defaultErrorProneExcludes = "(.*/)?(nocheck|autogen|generated)/.*\\.java"

tasks.withType<JavaCompile>().configureEach {
  options.errorprone {
    excludedPaths = defaultErrorProneExcludes
    isEnabled = true
    allSuggestionsAsWarnings = true
    allDisabledChecksAsWarnings = true
    disableWarningsInGeneratedCode = true

    // https://errorprone.info/docs/flags
    // -Xep:<checkName>[:severity] severity is one of {“OFF”, “WARN”, “ERROR”}
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
      error()
      treatGeneratedAsUnannotated = true
      suggestSuppressions = true
      isAssertsEnabled = true
      handleTestAssertionLibraries = true
      checkOptionalEmptiness = true
      checkContracts = true
      isJSpecifyMode = true
      onlyNullMarked = true
    }
  }
}

tasks.compileTestJava { options.errorprone { isEnabled = false } }

/*
 * Add other Error Prone flags here. See:
 * - https://github.com/tbroyer/gradle-errorprone-plugin#configuration
 * - https://errorprone.info/docs/flags
 * - https://github.com/ben-manes/caffeine/blob/master/gradle/plugins/src/main/kotlin/quality/errorprone.caffeine.gradle.kts
 */

fun defaultDisabledChecks() = listOf("MissingSummary", "Java8ApiChecker")

@Suppress("CanConvertToMultiDollarString")
fun defaultDisabledRules() = listOf("ImmutableTableRules\\\$ImmutableTableBuilder")
