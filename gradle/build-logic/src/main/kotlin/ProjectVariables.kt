@file:Suppress("PackageDirectoryMismatch")

package io.github.mymx2.plugin

@Suppress("SpellCheckingInspection", "EnumEntryName", "detekt:all")
enum class ProjectVersions(val key: String, val value: String, val url: String) {
  ktfmt(
    "com.facebook/ktfmt",
    "0.59",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/facebook/ktfmt/maven-metadata.xml",
  ),
  prettier("prettier", "3.6.2", "https://registry.npmjs.org/prettier"),
  prettierXml("@prettier/plugin-xml", "3.4.2", "https://registry.npmjs.org/@prettier/plugin-xml"),
  jspecify(
    "org.jspecify:jspecify",
    "1.0.0",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/org/jspecify/jspecify/maven-metadata.xml",
  ),
  errorprone(
    "com.google.errorprone:error_prone_core",
    "2.43.0",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/google/errorprone/error_prone_core/maven-metadata.xml",
  ),
  errorpronePicnicContrib(
    "tech.picnic.error-prone-support:error-prone-contrib",
    "0.26.0",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/tech/picnic/error-prone-support/error-prone-contrib/maven-metadata.xml",
  ),
  errorpronePicnicRefaster(
    "tech.picnic.error-prone-support:refaster-runner",
    "0.26.0",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/tech/picnic/error-prone-support/refaster-runner/maven-metadata.xml",
  ),
  nullaway(
    "com.uber.nullaway:nullaway",
    "0.12.11",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/uber/nullaway/nullaway/maven-metadata.xml",
  ),
  spotbugsAnnotations(
    "com.github.spotbugs:spotbugs-annotations",
    "4.9.8",
    "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/com/github/spotbugs/spotbugs-annotations/maven-metadata.xml",
  ),
}
