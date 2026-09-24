import PluginHelpers.libsOrInternal
import io.github.mymx2.plugin.quality.configureErrorProneWithNullaway
import net.ltgt.gradle.errorprone.errorprone

plugins {
  java
  // https://github.com/tbroyer/gradle-errorprone-plugin
  id("net.ltgt.errorprone")
  // https://github.com/tbroyer/gradle-nullaway-plugin
  id("net.ltgt.nullaway")
}

dependencies {
  compileOnly(libsOrInternal("jspecify"))
  // https://github.com/PicnicSupermarket/error-prone-support/tree/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns
  // https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/
  listOf("errorProneCore", "errorProneContrib", "refasterRunner", "nullaway").forEach { name ->
    errorprone(libsOrInternal(name))
  }
}

configureErrorProneWithNullaway()

tasks.compileTestJava {
  options.errorprone { isEnabled = false }
}

/*
  * Add other Error Prone flags here. See:
  * - https://github.com/tbroyer/gradle-errorprone-plugin#configuration
  * - https://errorprone.info/docs/flags
  * - https://github.com/ben-manes/caffeine/blob/master/gradle/plugins/src/main/kotlin/quality/errorprone.caffeine.gradle.kts
  */
