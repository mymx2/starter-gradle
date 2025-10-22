import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import net.swiftzer.semver.SemVer

plugins { base }

// Set the group required to refer to a Module "from outside".
// I.e., when it is published or used in Included Builds.
group = project.getPropOrDefault(LocalConfig.Props.GROUP)

val isCI = EnvAccess.isCi(providers)
val currVer = SemVer.parse(project.getPropOrDefault(LocalConfig.Props.VERSION_NAME))

currVer.preRelease?.also {
  val tags =
    listOf(
      "preview",
      "Preview",
      "dev",
      "Dev",
      "alpha",
      "Alpha",
      "beta",
      "Beta",
      "snapshot",
      "SNAPSHOT",
      "rc",
      "RC",
      "m",
      "M",
    )
  require(tags.contains(it)) { "Pre-release should be one of: ${tags.joinToString()}" }
}

// We dont publish releases on CI.
if (EnvAccess.isCi(providers)) {
  var preRelease = "SNAPSHOT"
  var buildMetadata = currVer.buildMetadata
  if (currVer.buildMetadata != null) {
    val gitCommitTimestamp =
      try {
        providers
          .exec { commandLine("git", "log", "-1", "--format=%ad", "--date=format:%Y%m%d%H%M%S") }
          .standardOutput
          .asText
          .get()
          .trim()
      } catch (_: Exception) {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
      }
    buildMetadata = gitCommitTimestamp
  }
  version =
    SemVer(currVer.major, currVer.minor, currVer.patch, preRelease, buildMetadata).toString()
} else {
  version = currVer.toString()
}
