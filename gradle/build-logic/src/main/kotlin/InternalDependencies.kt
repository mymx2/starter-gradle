@file:Suppress("PackageDirectoryMismatch")

package io.github.mymx2.plugin

object InternalDependencies {

  data class Library(
    val key: String,
    val module: String,
    val version: String,
    val type: String,
    val url: String,
  )

  // spotless:off
  val data =
    """
    [libraries]
    # Maven dependencies
    comFacebookKtfmt = { module = "com.facebook:ktfmt", version = "0.59", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/facebook/ktfmt/maven-metadata.xml" }
    jspecify = { module = "org.jspecify:jspecify", version = "1.0.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/org/jspecify/jspecify/maven-metadata.xml" }
    errorProneCore = { module = "com.google.errorprone:error_prone_core", version = "2.45.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/google/errorprone/error_prone_core/maven-metadata.xml" }
    errorProneContrib = { module = "tech.picnic.error-prone-support:error-prone-contrib", version = "0.27.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/tech/picnic/error-prone-support/error-prone-contrib/maven-metadata.xml" }
    refasterRunner = { module = "tech.picnic.error-prone-support:refaster-runner", version = "0.27.0", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/tech/picnic/error-prone-support/refaster-runner/maven-metadata.xml" }
    nullaway = { module = "com.uber.nullaway:nullaway", version = "0.12.15", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/uber/nullaway/nullaway/maven-metadata.xml" }
    spotbugsAnnotations = { module = "com.github.spotbugs:spotbugs-annotations", version = "4.9.8", type = "maven", url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/github/spotbugs/spotbugs-annotations/maven-metadata.xml" }

    # NPM dependencies
    prettier = { module = "prettier", version = "3.7.4", type = "npm", url = "https://registry.npmjs.org/prettier" }
    prettierPluginXml = { module = "@prettier/plugin-xml", version = "3.4.2", type = "npm", url = "https://registry.npmjs.org/@prettier/plugin-xml" }
    """
      .trimIndent()

  // spotless:on

  val libraries: Map<String, Library> by lazy {
    data
      .lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
      .map { line ->
        val (key, value) = line.split("=", limit = 2).map { it.trim() }
        val props =
          value
            .removePrefix("{")
            .removeSuffix("}")
            .split(",")
            .map { it.trim() }
            .associate {
              val (k, v) = it.split("=", limit = 2).map { s -> s.trim().removeSurrounding("\"") }
              k to v
            }
        key to
          Library(
            key = key,
            module = props.getValue("module"),
            version = props.getValue("version"),
            type = props.getValue("type"),
            url = props.getValue("url"),
          )
      }
      .toMap()
  }

  fun get(key: String): Library = libraries[key] ?: error("Unknown dependency: $key")

  fun maven(): List<Library> = libraries.values.filter { it.type == "maven" }

  fun npm(): List<Library> = libraries.values.filter { it.type == "npm" }
}
