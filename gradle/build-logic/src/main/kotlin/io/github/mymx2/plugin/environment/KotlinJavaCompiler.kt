package io.github.mymx2.plugin.environment

object KotlinCompilerOptions {

  val default =
    listOf(
      "-java-parameters", // 保留参数名
      "-Xjvm-default=all", // Java8 接口默认实现
      "-Xjsr305=strict", // 严格空安全
      "-Xemit-jvm-type-annotations", // 在泛型上保留类型注解
    )
}
