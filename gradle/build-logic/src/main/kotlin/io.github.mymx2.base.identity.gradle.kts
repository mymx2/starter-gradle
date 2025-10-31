import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import net.swiftzer.semver.SemVer

plugins { base }

// Set the group required to refer to a Module "from outside".
// I.e., when it is published or used in Included Builds.
group = project.getPropOrDefault(LocalConfig.Props.GROUP)

val isCI = EnvAccess.isCi(providers)
val currVer = SemVer.parse(project.getPropOrDefault(LocalConfig.Props.VERSION))

currVer.preRelease?.also {
  val tags = listOf("preview", "dev", "alpha", "beta", "SNAPSHOT", "rc", "m")
  require(tags.contains(it)) { "Pre-release should be one of: ${tags.joinToString()}" }
}

// We dont publish releases on CI.
version =
  if (EnvAccess.isCi(providers)) {
    SemVer(
        currVer.major,
        currVer.minor,
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")).toInt(),
        "SNAPSHOT",
        null,
      )
      .toString()
  } else {
    SemVer(currVer.major, currVer.minor, currVer.patch, currVer.preRelease, null).toString()
  }

@Suppress("unused")
object SemVerUtils {

  fun gitBuildMetadata(providers: ProviderFactory): String? {
    return runCatching {
        providers
          .exec { commandLine("git", "log", "-1", "--format=%ad", "--date=format:%Y%m%d%H%M%S") }
          .standardOutput
          .asText
          .get()
          .trim()
      }
      .getOrNull()
  }
}
