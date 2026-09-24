package io.github.mymx2.android

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric 测试基类：统一 runner 与 SDK 版本，消除各测试类的注解重复。 JUnit4 的 @RunWith 与 Robolectric 的 @Config
 * 均支持 @Inherited 继承（子类免注）。 仅 Robolectric 组件交互/截图测试继承；纯逻辑 JUnit5 测试（MainViewModelTest）不继承（不同引擎）。
 */
@RunWith(RobolectricTestRunner::class) @Config(sdk = [34]) abstract class RobolectricTest
