plugins {
  `java-platform` // combination with the `java` and `java-library` plugins
  id("io.github.mymx2.feature.publish-base")
}

val publishTaskName = "mavenBom"

val mavenBom =
  publishing.publications.create<MavenPublication>(publishTaskName) {
    from(components["javaPlatform"])
  }

signing { sign(mavenBom) }
