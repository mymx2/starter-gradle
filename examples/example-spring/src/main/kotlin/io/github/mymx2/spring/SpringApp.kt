package io.github.mymx2.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication class SpringApp

@Suppress("detekt:SpreadOperator")
fun main(args: Array<String>) {
  runApplication<SpringApp>(*args)
}

@RestController
class ExampleController {

  @GetMapping("/") fun index() = let { "Greetings from Spring Boot!" }
}
