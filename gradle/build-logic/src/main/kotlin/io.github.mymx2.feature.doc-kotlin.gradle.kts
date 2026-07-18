import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault

plugins {
  // https://kotlinlang.org/docs/dokka-migration.html
  id("org.jetbrains.dokka")
}

val docFailOnError = project.getPropOrDefault(LocalConfig.Props.DOC_FAIL_ON_ERROR).toBoolean()
// [perf] 本地开发时跳过 dokka 文档生成(文档非"我改坏没"验证所需，且不可缓存、每次 check 都重跑)。
// SKIP_DOC 单独控制，也随 SKIP_ALL_LOCAL 一并跳过；默认 false 保持原行为。
val skipDoc = project.getPropOrDefault(LocalConfig.Props.SKIP_DOC).toBoolean()
val skipAllLocal = project.getPropOrDefault(LocalConfig.Props.SKIP_ALL_LOCAL).toBoolean()

gradle.projectsEvaluated {
  val vanniktechPlugin = plugins.hasPlugin("com.vanniktech.maven.publish")
  if (!vanniktechPlugin && !(skipDoc || skipAllLocal)) {
    val javadocJar = tasks.findByName("javadocJar")
    if (javadocJar != null) {
      tasks.named<Jar>("javadocJar") {
        val dokka = tasks.dokkaGeneratePublicationHtml
        archiveClassifier.set("javadoc")

        dependsOn(dokka)
        // the default output directory is `/build/dokka/html`
        // dokka.get().outputDirectory
        from(dokka)
      }
    }
  }
}

dokka { dokkaPublications.html { failOnWarning = docFailOnError } }

tasks.register("docKotlin") {
  group = "docs"
  description = "Generate Kotlin docs [group = docs]"
  dependsOn(tasks.dokkaGeneratePublicationHtml)
}

if (skipDoc || skipAllLocal) {
  // 关闭 dokka 文档生成任务(dokkaGenerateModuleHtml / dokkaGeneratePublicationHtml)，
  // 使其退出 check / build 任务图。文档生成不可缓存，本地循环每次都重跑，且非"我改坏没"验证所需。
  // 注：dokka 模块任务类型是 DokkaTaskPartial(非 DokkaTask)，故按任务名匹配更稳。
  tasks.configureEach {
    if (name.startsWith("dokkaGenerate")) {
      enabled = false
    }
  }
}
