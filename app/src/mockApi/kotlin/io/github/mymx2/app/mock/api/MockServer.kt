package io.github.mymx2.app.mock.api

import io.github.mymx2.app.MainModule

/** A mock server. */
class MockServer {

  /**
   * Starts the mock server.
   *
   * @return The list of mock endpoints.
   */
  fun get(): List<String> {
    MainModule()
    return listOf()
  }
}
