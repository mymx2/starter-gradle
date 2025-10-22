package io.github.mymx2.plugin.utils

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Executors

object HttpUtil {

  /** httpClient 构建虚拟线程 */
  val httpClient: HttpClient.Builder =
    HttpClient.newBuilder()
      .executor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vt-h-c", 0).factory()))
      .connectTimeout(Duration.ofMinutes(10))
      .followRedirects(HttpClient.Redirect.NORMAL)

  /** 创建 URI */
  fun String.toURI(queryMap: Map<String, String?>? = null): URI {
    return if (queryMap.isNullOrEmpty()) {
      URI(this)
    } else {
      val query = queryMap.entries.joinToString("&") { "${it.key}=${it.value.orEmpty()}" }
      URI("$this?$query")
    }
  }

  fun get(url: String, timeout: Duration = Duration.ofSeconds(10)): String? {
    val request = HttpRequest.newBuilder().uri(url.toURI()).GET().timeout(timeout).build()
    val response =
      httpClient.connectTimeout(timeout).build().send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
  }

  fun head(url: String, timeout: Duration = Duration.ofSeconds(5)): Boolean {
    return runCatching {
        val request = HttpRequest.newBuilder().uri(url.toURI()).HEAD().timeout(timeout).build()
        val response =
          httpClient
            .connectTimeout(timeout)
            .build()
            .send(request, HttpResponse.BodyHandlers.discarding())
        return response.statusCode() == 200
      }
      .getOrDefault(false)
  }
}
