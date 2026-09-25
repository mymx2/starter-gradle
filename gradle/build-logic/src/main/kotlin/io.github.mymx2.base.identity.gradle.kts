import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.environment.buildProperties
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import net.swiftzer.semver.SemVer

plugins { base }

val buildProperties = project.buildProperties()

// Set the group required to refer to a Module "from outside".
// I.e., when it is published or used in Included Builds.
group = project.getPropOrDefault(LocalConfig.Props.GROUP)

val isCI = EnvAccess.isCi(providers)
val currVer =
  buildProperties
    .getProperty("version", "")
    .ifBlank { project.getPropOrDefault(LocalConfig.Props.VERSION) }
    .let { SemVer.parse(it) }

currVer.preRelease?.also {
  val preReleasePattern = "SNAPSHOT|dev\\d*|preview\\d*|alpha\\d*|beta\\d*|m\\d+|rc\\d+"
  require(currVer.preRelease!!.matches(preReleasePattern.toRegex())) {
    "Pre-release should match: $preReleasePattern"
  }
}

val githubEventName = System.getenv("GITHUB_EVENT_NAME") ?: ""
val githubRefName = System.getenv("GITHUB_REF_NAME") ?: ""
val isScheduled = githubEventName == "schedule"
val isManual = githubEventName == "workflow_dispatch"
val isGithubTag = githubRefName.startsWith("v")

// release check: push tag should match code version.
// 只对发布模块（应用了 maven-publish 系插件）校验；独立版本模块（如 Android app 的 build.properties
// version=1.0.0）不参与根项目发版，不应被根 tag 阻塞。pluginManager.withPlugin 保证
// 发布插件在 identity 之前或之后应用都能触发校验。
if (isCI && githubEventName == "push" && isGithubTag) {
  fun verifyTagMatchesVersion() {
    require(currVer.toString() == githubRefName.removePrefix("v")) {
      "CI Release: GitHub tag ($githubRefName) must match Code version (${currVer})"
    }
  }
  pluginManager.withPlugin("com.vanniktech.maven.publish") { verifyTagMatchesVersion() }
  pluginManager.withPlugin("maven-publish") {
    if (!pluginManager.hasPlugin("com.vanniktech.maven.publish")) {
      verifyTagMatchesVersion()
    }
  }
}

version =
  if (isCI && (isScheduled || isManual)) {
    SemVer(
        currVer.major,
        currVer.minor,
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")).toInt(),
        "SNAPSHOT",
      )
      .toString()
  } else {
    // local build
    SemVer(currVer.major, currVer.minor, currVer.patch, currVer.preRelease, null).toString()
  }
