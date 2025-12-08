package io.github.mymx2.kotlin

/**
 * example-kotlin application
 *
 * @author dy
 */
class KotlinApp {

  /**
   * say hello
   *
   * @return Hello, kotlin!
   */
  fun sayHello(): String = let { "Hello, kotlin!" }

  companion object {
    /**
     * main function
     *
     * @param args arguments
     */
    @JvmStatic
    fun main(args: Array<String>) {
      println(KotlinApp().sayHello())
    }
  }
}
