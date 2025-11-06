package io.github.mymx2.spring

import org.junit.jupiter.api.Test
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class DemoApplicationTests(private val buildProperties: BuildProperties) {

  @Test
  fun printBuildProperties() {
    buildProperties.iterator().forEach { println(it.key + ": " + it.value) }
  }
}
