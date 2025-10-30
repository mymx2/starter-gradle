import io.github.mymx2.plugin.environment.buildProperties
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import io.github.mymx2.plugin.versionFromCatalog

plugins { java }

val jepEnablePreview = project.getPropOrDefault(LocalConfig.Props.JEP_ENABLE_PREVIEW).toBoolean()
val docFailOnError = project.getPropOrDefault(LocalConfig.Props.DOC_FAIL_ON_ERROR).toBoolean()

val buildProperties = project.buildProperties()
val jdkVersion: String =
  buildProperties.getProperty("jdk", "").ifBlank {
    runCatching { versionFromCatalog("jdk") }.getOrNull().orEmpty()
  }

// the default output directory is `/build/docs/javadoc`.
tasks.withType<Javadoc>().configureEach {
  isFailOnError = docFailOnError
  val opt = (options as StandardJavadocDocletOptions)
  opt.encoding = Charsets.UTF_8.name()
  opt.addBooleanOption("html5", true)
  if (jepEnablePreview && jdkVersion.isNotBlank()) {
    opt.addStringOption("source", jdkVersion)
    opt.addBooleanOption("-enable-preview", true)
  }
  // https://stackoverflow.com/questions/29519085/how-to-fail-gradle-build-on-javadoc-warnings
  if (docFailOnError) {
    opt.addBooleanOption("Xwerror", true)
    opt.addBooleanOption("Xdoclint:all,-missing", true)
  } else {
    opt.addBooleanOption("Xdoclint:none", true)
  }
}

tasks.register("docJava") {
  group = "docs"
  description = "Generate Java docs [group = docs]"
  dependsOn(tasks.javadoc)
}
