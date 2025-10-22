plugins {
  id("io.github.mymx2.module.java")
  id("io.github.mymx2.module.app")
  id("io.github.mymx2.feature.publish-vanniktech")
}

application { mainClass.set("io.github.mymx2.java.JavaApp") }
