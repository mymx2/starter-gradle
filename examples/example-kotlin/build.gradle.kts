plugins {
  id("io.github.mymx2.module.kotlin")
  id("io.github.mymx2.module.app")
  id("io.github.mymx2.feature.publish-vanniktech")
}

application { mainClass.set("io.github.mymx2.kotlin.KotlinApp") }
