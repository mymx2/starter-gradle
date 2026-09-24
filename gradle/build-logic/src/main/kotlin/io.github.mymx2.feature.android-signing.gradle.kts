@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApplicationExtension
import io.github.mymx2.plugin.android.androidProp

plugins { id("io.github.mymx2.base.lifecycle") }

// 签名四件：android.signing.* 配齐才签名，否则 release 保持 unsigned。
// 私密值(storePassword/keyPassword)走 local.properties(本机,gitignore)或 CI 环境变量注入，
// 不写进会提交的 build.properties——签名密钥即应用身份，入库（哪怕私有库）即永久泄露。
// 多 app：每 app 可用模块级 local.properties 覆盖各自签名（见 buildProperties 分层）。
// 在 configure 块外计算，避免嵌套 lambda 里 IDE 误解析 receiver。
val signingProps: List<String> =
  listOf(
    project.androidProp("signing.storeFile", ""),
    project.androidProp("signing.storePassword", ""),
    project.androidProp("signing.keyAlias", ""),
    project.androidProp("signing.keyPassword", ""),
  )
val signingStoreFile: String = signingProps[0]
val hasSigning: Boolean = signingProps.all { it.isNotBlank() }

// withPlugin 守卫：被未应用 com.android.application 的模块误用时跳过而非配置期崩溃。
pluginManager.withPlugin("com.android.application") {
  extensions.configure<ApplicationExtension> {
    signingConfigs {
      create("release") {
        if (hasSigning) {
          // 校验 keystore 路径默认必须解析到仓库内，防止属性值指向仓库外任意文件。
          // 例外：android.signing.allowExternal=true 放宽（企业 CI 用外部签名服务/HSM，如 /opt/signing/...）。
          // 本机/私有仓库开发默认 false 保持拦截；CI 外部签名场景显式开。
          val allowExternal = project.androidProp("signing.allowExternal", "false").toBoolean()
          val storeFileResolved = rootProject.file(signingStoreFile)
          if (!allowExternal) {
            require(storeFileResolved.canonicalPath.startsWith(rootDir.canonicalPath)) {
              "android.signing.storeFile must resolve inside the project, got: $signingStoreFile (set android.signing.allowExternal=true to allow external signing service)"
            }
          }
          storeFile = storeFileResolved
          storePassword = project.androidProp("signing.storePassword", "")
          keyAlias = project.androidProp("signing.keyAlias", "")
          keyPassword = project.androidProp("signing.keyPassword", "")
        }
      }
    }

    buildTypes {
      release {
        if (hasSigning) {
          signingConfig = signingConfigs.getByName("release")
        }
      }
    }
  }
}
