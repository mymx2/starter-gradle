import io.github.mymx2.plugin.spring.SpringBootHelper.getSpringBootVersion
import io.github.mymx2.plugin.spring.SpringBootHelper.springBootStarterWebMvc

plugins {
  id("io.github.mymx2.module.kotlin")
  id("io.github.mymx2.module.spring-boot")
  id("io.github.mymx2.tools.spring-openapi")
}

springBootStarterWebMvc(getSpringBootVersion())
