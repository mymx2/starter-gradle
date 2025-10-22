import io.github.mymx2.plugin.environment.EnvAccess
import io.github.mymx2.plugin.local.LocalConfig
import io.github.mymx2.plugin.local.getPropOrDefault
import java.net.URI

// https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_setup_http_backend
buildCache {
  val isCI = EnvAccess.isCi(providers)
  val userName = settings.getPropOrDefault(LocalConfig.Props.BUILD_CACHE_USER)
  local { isEnabled = !isCI }
  remote<HttpBuildCache> {
    this.isEnabled = userName.isNotBlank()
    url = URI.create("https://cache.onepiece.software/cache/")
    isPush = isCI
    credentials {
      username = userName
      password = settings.getPropOrDefault(LocalConfig.Props.BUILD_CACHE_PWD)
    }
  }
}
