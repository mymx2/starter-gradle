# starter-gradle 构建性能优化报告

> 使用 Gradle 官方排查手段（`--profile`、配置缓存、构建缓存、`buildEnvironment`、Develocity/Build Scan）对
> `mymx2/starter-gradle` 进行的性能剖析与优化。
> 环境：Ubuntu 22.04 / 32 核 / 123 GiB RAM / JDK 25（sdkman）/ Gradle 9.7.0-milestone-3。

## 0. 一句话结论

**这个项目的构建配置在当前硬件上已经是最优的**——增量构建 1–13s，配置缓存、构建缓存、Kotlin 增量编译全部生效。
真正的瓶颈不在构建脚本，而在**环境**：一个损坏的全局 `init.gradle` 和一个过低的 JDK 版本让构建根本跑不起来。
修复环境后，再做的"优化"尝试（给 JVM/Kotlin daemon 限堆）反而**退化**了，证明大内存机器不该限堆。

最有价值的后续杠杆是**启用远程构建缓存**（项目已接线，只差 `BUILD_CACHE_USER`/`BUILD_CACHE_PWD` 凭证）。其次，本报告提出的"把重型静态分析 / 覆盖率采集 / 端到端测试套件从本地 `build`/`check` 解耦到 CI"**已实施**：通过 `SKIP_QUALITY`、`SKIP_COVERAGE`、`SKIP_E2E` 三个门控，本地 `build` 任务图质量任务 10 → 0、`check` 任务图 jacoco 任务 5 → 0、`:app` 的 e2e 套件 + mockApi 整套 −16 任务（解耦正确，见 §4.2 / §4.6 / §4.10 / §4.11）。但需注意——项目默认开启构建缓存（`org.gradle.caching=true`），无改动的热重建里质量产物从缓存恢复，SKIP_QUALITY 的**墙钟收益被缓存掩盖**（见 §4.6 的缓存热度实验）；真正收益体现在缓存未命中 / 冷构建 / 改代码后的场景。本地用 `check` 替代 `build` 可进一步跳过打包分发物（见 §4.7）。`isolated-projects` 经实验确认与本工程不兼容，已排除。本 PR 已提交一项**通用、与机器无关**的 `gradle.properties` 改动：`org.gradle.configuration-cache.max-problems` 由 `1 → 5`（见 §4.4，避免单点问题静默废掉配置缓存）；机器专属项（如 toolchain 路径、`watch-fs` 默认已开）仍不提交。作为收尾，新增复合门控 `SKIP_ALL_LOCAL`，让三旗解耦收成一面旗（见 §4.12）。

---

## 1. 环境就绪（这是"优化"的前提，否则构建直接失败）

按 `local-env` 规则安装/修复了三类工具与配置：

| 项 | 问题 | 处理 |
|---|---|---|
| `fd` | local-env 要求但缺失 | `apt install fd-find` 并软链为 `fd`（v9.0.0） |
| `vp` / `rg` | 已就位 | `vp v0.2.4`、`ripgrep 14.1.0` 确认可用 |
| 全局 `~/.gradle/init.gradle` | **语法损坏**（`maven { url https://... }` 漏 `=`、拼错 `mavelCentral()`），导致每个 Gradle 调用初始化即崩 | 备份后移除；项目自身已在 `settings.gradle.kts` 正确声明 gradlePluginPortal + mavenCentral + sonatype + google，无需该镜像脚本 |
| JDK 版本 | 项目 `jdk=25`（版本目录），但本机仅 JDK 20；且 `dy-gradle-plugin` 插件要求 JVM 25 | `sdk install java 25-open` 并设为默认，Gradle daemon 跑在 JDK 25 |

> 修复后 `./gradlew :app:build` 由"初始化即失败"变为可构建；首次冷构建（含 JDK25 toolchain 下载 + 依赖解析 + build-logic Kotlin 编译 + 测试 + 质量门禁）耗时 **13m29s**。

---

## 2. 官方剖析手段与基线测量

使用 `--profile` 生成 HTML 性能报告，并通过"无改动 / 改 1 文件 / clean / 强制全量 / 冷构建"多场景测量。

### 2.1 基线数据（原配置，已是最优）

| 场景 | 耗时 | 说明 |
|---|---|---|
| 无改动 `:app:build` | **1s** | 配置缓存复用，39 任务全 UP-TO-DATE |
| 改 1 文件重建 `:app:build` | **3.5s–13s** | 增量编译 + 质量门禁重跑（含 e2e 测试） |
| `clean :app:build` | **3s** | 构建缓存恢复输出，clean 不触发重编译 |
| 强制全量 `:app:build --rerun-tasks`（热 daemon，含 build-logic 重编译） | **35.9s** | 大头是 build-logic 的 Kotlin 重编译（见 §2.2） |
| 冷强制全量（daemon+配置缓存冷） | ~110s | daemon 冷启动 + 配置缓存重建 |
| 首次绝对冷构建（clone 后全量下载） | **13m29s** | 一次性成本：JDK25 toolchain + 依赖 + build-logic 编译 |

**诊断**：配置缓存（Configuration Cache）命中、构建缓存（Build Cache）命中、Kotlin 增量编译均正常工作 → **开发内循环已经极快（1–13s）**。

### 2.2 热点定位（来自 `--profile`）

`--rerun-tasks` 强制全量时 35.9s 的任务耗时分布：

```
Task Duration Result : 29.744s (total)
  :compileKotlin                          21.781s   ← build-logic（插件工程）Kotlin 重编译
  :generatePrecompiledScriptPluginAccessors  4.296s ← kotlin-dsl 预编译访问器
  :compilePluginsBlocks                    1.274s
  :kaptGenerateStubsKotlin                 0.998s
  :jar                                     0.693s
```

- **真正的成本在 `build-logic`（自定义插件工程）的重编译**，而非 `:app` 本身。这只在改插件源码或 `--rerun-tasks` 时发生，是插件开发的固有成本。
- **质量门禁（detekt / spotbugs / pmd / checkstyle / spotless）对 `:app` 几乎不耗时**（如 `detekt 0.2s`、`spotlessKotlin 0.09s`）——它们被硬编码进 `build → check → qualityCheck`，但对小模块增量成本极低。

---

## 3. 优化实验与教训：大内存机器**不要限堆**

我尝试通过 `gradle.properties` 调优 daemon 堆，结果**退化**，这是本任务最重要的发现：

| 配置 | 强制全量（热 daemon） | 结论 |
|---|---|---|
| 原配置（无 `-Xmx`，JVM 默认约 30g） | 35.9s | ✅ 最优 |
| `kotlin.daemon.jvmargs=-Xmx4g` | **47s** | ❌ 退化（GC 更频繁） |
| `kotlin.daemon.jvmargs=-Xmx8g` | **构建失败**（`Did not receive connection preamble within 10s`） | ❌ 严重退化 |

> 在 123 GiB 机器上，JVM 默认堆（≈25% 物理内存 ≈ 30g）远大于我设的 4g/8g 上限，反而让 build-logic（60+ 插件文件）的 Kotlin 重编译 GC 抖动、甚至 daemon 连接超时。
> **结论：大内存开发机保持堆不限制；`org.gradle.jvmargs` 维持原样即可。**

因此 `gradle.properties` **保持原配置不动**（已验证无回归，构建健康 `BUILD SUCCESSFUL`）。

---

## 4. 高价值优化建议（按收益排序）

### 4.1 启用远程构建缓存 —— 收益最大 ⭐
项目已在 `gradle/build-logic/src/main/kotlin/io.github.mymx2.build.feature.build-cache.settings.gradle.kts` 接好 HTTP 远程缓存：
- 本地缓存：非 CI 时启用。
- 远程缓存：`BUILD_CACHE_USER` 设置后启用 `https://cache.onepiece.software/cache/`，CI 时 push。

**只需设置凭证（或指向你自己的 Develocity / build-cache 节点）即可让冷构建与 CI 直接拉取编译产物，彻底消除 13.5min 的冷构建成本。** 这是官方首要推荐手段。

### 4.2 把重型静态分析从本地 `build` 解耦到 CI —— 已实施 ✅
当前 `base.lifecycle` 中 `tasks.check { dependsOn(qualityCheck) }`，使 `detekt/spotbugs/pmd/checkstyle/spotless` 在**每次** `build` 都跑。
已按官方建议实施解耦：**默认行为完全不变**（`SKIP_QUALITY=false` 时 `check` 仍依赖 `qualityCheck`），仅增加一个属性门控让本地可跳过。

**改动文件：**
1. `gradle/build-logic/.../io/github/mymx2/plugin/local/LocalConfig.kt`
   新增枚举项 `SKIP_QUALITY("SKIP_QUALITY", "false")`（默认 `false`，保持原行为）。
2. `gradle/build-logic/.../io.github.mymx2.base.lifecycle.gradle.kts`
   把硬编码的 `tasks.check { dependsOn(tasks.named("qualityCheck")) }` 改为门控：
   ```kotlin
   val skipQuality = project.getPropOrDefault(LocalConfig.Props.SKIP_QUALITY).toBoolean()
   if (!skipQuality) {
     tasks.check { dependsOn(tasks.named("qualityCheck")) }
   }
   ```
3. `gradle/build-logic/.../io.github.mymx2.check.quality-detekt.gradle.kts`
   **补刀**：`dev.detekt` 插件会**额外**把 `detekt` 直接挂到 `check`（不止 `qualityCheck`）。只门控 `check → qualityCheck` 拦不住 detekt，因此在这里补：
   ```kotlin
   if (project.getPropOrDefault(LocalConfig.Props.SKIP_QUALITY).toBoolean()) {
     tasks.withType<Detekt>().configureEach { enabled = false }
   }
   ```
   > 踩坑：预编译脚本插件（`*.gradle.kts`）被 kotlin-dsl 编译成合成接收者类型（非 `Project`/`Settings`），
   > 若用裸 `getPropOrDefault(...)` 会命中 `PluginAware.getPropOrDefault` 的 `else -> error(...)`。
   > 必须显式传 `project.getPropOrDefault(...)`，与仓库内其他脚本一致。

**用法：**
```bash
# 本地快速构建（跳过重型静态分析；CI 不要加这个，继续跑 qualityCheck/qualityGate）
./gradlew build -PSKIP_QUALITY=true
# 或写进 gradle.properties / local.properties： SKIP_QUALITY=true
```

**实测收益见 §4.6。** 关键结论：解耦对"质量任务必须真正执行"的冷构建/代码改动场景收益最大；当所有任务都已 UP-TO-DATE 的热构建，跳过它们几乎不省墙钟时间（符合预期）。


### 4.3 Toolchain 钉死到本地 JDK（可选、本地）⚡
为避免冷构建时 foojay 自动拉取 JDK25 toolchain，可在**本地** `gradle.properties` 添加：
```properties
# 本地专用，不要提交机器专属路径；其他机器上 Gradle 会忽略无效路径并回退到自动探测
org.gradle.java.installations.paths=/path/to/sdkman/candidates/java/25-open
```
（本沙箱已 `sdk install java 25-open`，该路径有效；JDK25 toolchain 已缓存，故当前冷构建不再下载。）

### 4.4 配置缓存问题容忍数与文件系统监视（已提交 `1 → 5`）
**`org.gradle.configuration-cache.max-problems` 偏严 —— 已修正并提交。** 原值 `1`：一旦出现任一配置缓存
问题，配置缓存会被**静默禁用**，回退到每次全量重新配置（本地增量构建因单点问题而整体变慢）。
已提到 `5`（见 `gradle.properties`）：既能暴露问题，又不会因少量问题直接废掉缓存，仍保持严格（非 `0`），CI 行为不受影响。
这是**通用、与机器无关**的改动，按最新授权已纳入本 PR（而非仅留作本地可选调整）。

```properties
# 配置缓存问题容忍数：默认 1 会在出现任一问题时静默禁用配置缓存(回退全量重新配置)。
# 提到 5 可在出现少量问题时仍保留缓存，避免本地增量构建因单点问题而整体变慢。
# 仍保持严格(非 0)，CI 行为不受影响。
org.gradle.configuration-cache.max-problems=5
```

> **文件系统监视（`org.gradle.watch-fs`）经核实 Gradle 7 起默认 `true`**：补一个 `=true` 是冗余 no-op，故**不提交**（保持默认即可）。本地专属项（如 `org.gradle.java.installations.paths` 工具链路径）仍**不提交**，见 §4.3。

**本 PR 已提交的非本地 `gradle.properties` 改动仅此一项 `max-problems=5`**；其余本地/机器专属项维持原状。

### 4.5 已确认生效的官方开关（无需改动）
`org.gradle.daemon=true`、`org.gradle.caching=true`、`org.gradle.parallel=true`、`org.gradle.configuration-cache=true`、`org.gradle.configuration-cache.parallel=true` 均已开启且工作正常；文件系统监视 Gradle 9 默认开启。

### 4.6 解耦实测：`SKIP_QUALITY` 的正确性 + 墙钟收益（依赖缓存）
环境同上（32 核 / 123 GiB / JDK25 / Gradle 9.7.0-milestone-3），项目默认 `org.gradle.caching=true`。
测量脚本：`/tmp/measure3/`（首次）、`/tmp/measure_repro/`（复现，gradle.properties 已回退到 main 原状）。

**A. 解耦正确性（最关键）——任务图层面已验证：**

| 场景 | 进入任务图的质量任务 |
|---|---|
| Quality ON（默认） | 10：`example-kotlin:detekt`、`example-spring:detekt`、`app:detekt` + 7× `qualityCheck`（versions/aggregation/root/example-kotlin/app/example-spring/example-java） |
| `SKIP_QUALITY=true` | **0（完全解耦）** |

`SKIP_QUALITY=true` 时 `detekt`（含插件自挂 `check` 的边）与整个 `qualityCheck` 链全部退出任务图——**解耦成立**；默认 `false` 对 CI 与现有行为零影响。

**B. 墙钟收益（依赖构建缓存热度——重要）：**

| 测量批次 | 场景 | 耗时 | 说明 |
|---|---|---|---|
| 首次（缓存较冷） | `build` ON | 26.2s | 质量任务真实执行 |
| 首次（缓存较冷） | `build -PSKIP_QUALITY=true` | 12.6s | 质量任务 0 |
| 复现（缓存已热） | `build` ON | 11.1s | detekt 走 `FROM-CACHE`，仅 qualityCheck×4 执行 |
| 复现（缓存已热） | `build -PSKIP_QUALITY=true` | 10.3s | 质量任务 0 |
| 复现（缓存已热） | `check -PSKIP_QUALITY=true` | 8.6s | 0 质量 + 跳过打包 |

- **关键发现：`clean` 只清 `build/` 目录，不清本地构建缓存（`~/.gradle/caches`）**。本任务累计跑了数十条构建后缓存很热，质量产物（detekt/spotbugs/pmd/checkstyle）从缓存恢复，所以"冷构建"其实是温的——这正是首次 26.2s 与复现 11.1s 差异的来源（缓存热度，非优化回归）。
- **结论**：在**无改动的热重建**里，质量产物已被缓存，`SKIP_QUALITY` 只省约 0.8s（11.1s vs 10.3s）。它的墙钟价值体现在**缓存未命中**场景：冷构建（CI 无缓存 / 全新 clone）、或你改了代码导致变更模块的 quality 任务真重跑（缓存 miss）。这类场景下收益等于被跳过的质量任务耗时（首次实测 ~13.6s，随模块数 / 代码量放大）。

**C. 一个被缓存掩盖的环境缺陷（顺带发现）：**
`--no-build-cache` 下 `build` 会 **BUILD FAILED**：`spotlessKotlinCheck` / `spotlessKotlinGradleCheck` 报
`NoClassDefFoundError: Could not initialize class com.facebook.ktfmt.format.Formatter`
——ktfmt 格式化器在 JDK 25 下初始化失败（老版本 ASM/Guava 与 JDK25 不兼容）。平时 spotless 结果从构建缓存恢复，所以不报错；一旦缓存失效就炸。这是**独立于本优化的预存环境问题**，建议单独修（升级 spotless/ktfmt 或评估 JDK 版本）。附带好处：`SKIP_QUALITY=true` 时 spotlessCheck 也被跳过，本地开发循环恰好绕开了这个雷。详见 §4.9。

**结论**：`SKIP_QUALITY` 默认 `false`，对 CI 与现有行为零影响；本地加 `-PSKIP_QUALITY=true` 即可在冷构建 / 缓存 miss / 改代码后获得质量任务的完整跳过，且不牺牲测试覆盖（`build` 仍跑 `check`/测试）。

### 4.7 跳过打包分发物（lean dev command）⚡
`build = check + assemble`。`assemble` 对 application 模块（`:app`、`:example-java/kotlin/spring`）会产出 `distTar`/`distZip`/`startScripts`/`bootJar`/`bootDistTar` 等**可分发产物**——本地"我改了东西有没有坏"的验证其实用不到。

实测冷构建（均 `-PSKIP_QUALITY=true`，构建缓存开）：

| 命令 | 耗时 | 执行任务 |
|---|---|---|
| `./gradlew clean build` | **14.2s** | 43 |
| `./gradlew clean check`（仅测试，跳过打包） | **9.3s** | 19 |

- **打包分发物约占 `build` 的 35%**（14.2s → 9.3s，省 ~4.9s）。
- **推荐本地开发循环用 `./gradlew check -PSKIP_QUALITY=true`**（冷 9.3s，增量更短）：只跑测试 + 编译，跳过静态分析**和**打包。只在需要可运行/可分发产物时再用 `build`。
- 完整优化栈：全量质量 `build` 26.2s → 跳过质量 `build` 14.2s → 跳过质量+打包 `check` **9.3s（≈全量 2.8× 加速）**。
- 注：14.2s / 9.3s 为缓存较冷时测得；缓存热时差值更小（复现：build_OFF 10.3s vs check_OFF 8.6s ≈ 1.7s）。打包任务减少（43→19）是结构性的，真正冷构建里差值更大。无论缓存状态，`check` 都比 `build` 少跑分发物打包，是更贴合"我改坏没"的验证命令。

### 4.8 实验：`org.gradle.unsafe.isolated-projects`（不可行，已排除）
试过 `-Dorg.gradle.unsafe.isolated-projects=true`（项目级配置隔离，可加速配置并行）。
结果：配置缓存报 1 个 problem 并 **BUILD FAILED** —— `Project ':build-logic' cannot access Gradle.getPlugins`（自定义插件 `doc-kotlin` 跨项目访问与隔离不兼容）。
结论：本工程（build-logic 复合构建 + 自定义插件）与 isolated-projects 不兼容，**不启用**。配置缓存已覆盖配置阶段优化，该开关在此无收益且有破坏风险。

### 4.9 顺带发现：spotless/ktfmt 在 JDK25 下的环境缺陷（非性能，建议另修）
`--no-build-cache` 下 `build` 会 **BUILD FAILED**：`spotlessKotlinCheck` / `spotlessKotlinGradleCheck` 报
`NoClassDefFoundError: Could not initialize class com.facebook.ktfmt.format.Formatter`
——ktfmt 格式化器在 JDK 25 下初始化失败（老版本 ASM/Guava 与 JDK25 不兼容）。平时 spotless 结果从构建缓存恢复，所以默认 `build` 不报错；一旦缓存失效（或全新机器无缓存）就会直接失败。这是**独立于本优化的预存环境问题**，建议单独处理（升级 spotless/ktfmt 到兼容 JDK25 的版本，或评估 JDK 版本）。附带好处：`SKIP_QUALITY=true` 时 `spotlessCheck` 也被跳过，本地开发循环恰好绕开了这个雷。详测见 §4.6-C。

### 4.10 把 jacoco 覆盖率采集从本地 `check` 解耦到 CI —— 已实施 ✅
`SKIP_QUALITY` 解耦了"静态分析"，但 `check` 仍跑 jacoco 覆盖率：每个 `Test` 任务挂着 jacoco **agent 插桩**，且 `check` 依赖各模块的 `jacocoTestReport` 与跨模块聚合 `testCodeCoverageReport`。本地"我改坏没"的验证其实不需要覆盖率。沿用 §4.2 同款门控模式再解耦一层。

**改动文件：**
1. `gradle/build-logic/.../io/github/mymx2/plugin/local/LocalConfig.kt`
   新增枚举项 `SKIP_COVERAGE("SKIP_COVERAGE", "false")`（默认 `false`，保持原行为）。
2. `gradle/build-logic/.../io.github.mymx2.feature.test.gradle.kts`
   把 `tasks.check { dependsOn(tasks.jacocoTestReport) }` 门控，并在 `SKIP_COVERAGE=true` 时关闭 jacoco agent：
   ```kotlin
   val skipCoverage = project.getPropOrDefault(LocalConfig.Props.SKIP_COVERAGE).toBoolean()
   tasks.check {
     if (!skipCoverage) {
       dependsOn(tasks.jacocoTestReport)
     }
   }
   if (skipCoverage) {
     // 关闭 jacoco java agent，避免对每个 Test 任务做字节码插桩（主要的单测开销）
     tasks.withType<Test>().configureEach {
       extensions.findByType(JacocoTaskExtension::class)?.isEnabled = false
     }
   }
   ```
3. `gradle/build-logic/.../io.github.mymx2.report.code-coverage.gradle.kts`
   把 `tasks.check { dependsOn(tasks.testCodeCoverageReport) }` 门控为 `if (!skipCoverage)`。

**用法：**
```bash
# 本地最快验证循环：跳过静态分析 + 跳过覆盖率采集
./gradlew check -PSKIP_QUALITY=true -PSKIP_COVERAGE=true
```

**实测收益（lean loop `clean check -PSKIP_QUALITY=true`，同硬件）：**

| 指标 | 覆盖率 ON | 覆盖率 OFF (`SKIP_COVERAGE=true`) | 说明 |
|---|---:|---:|---|
| 进入 `check` 的 jacoco/coverage 任务 | 5 | **0** | 每个模块 `jacocoTestReport` + 聚合 `testCodeCoverageReport` 全部退出任务图 |
| lean loop 总任务数 | 126 | 108 | 结构性减少 18 个 |
| jacoco 报告+聚合任务耗时（热） | 0.37s | 0 | 确定性移除（冷构建约 1.2s） |
| `:example-spring:test` 内 jacoco agent 开销 | 子秒级，难隔离 | 0 | 见下 |

- **确定性收益**：`SKIP_COVERAGE` 直接移除覆盖率的报告与聚合任务（热构建 ~0.37s、冷构建 ~1.2s，来自 `--profile` 实测），并关闭 jacoco agent——这部分不依赖缓存、始终生效，对 CI 与默认行为零影响（`false` 时 `check` 仍跑完整覆盖率）。
- **一个诚实的坑**：lean loop 真正的耗时大户是 `:example-spring:test`（Spring 上下文启动），且该任务**运行间方差极大（2.9s–9.3s，随 daemon/JVM 预热波动）**。jacoco agent 在单测上的插桩开销被这个方差完全淹没——背靠背同 daemon 测一次竟出现 ON 2.84s / OFF 5.07s（反序），说明 agent 增量（早前隔离测量估 ~0.9s）在 Spring 启动噪声下**无法稳定复现**。结论：agent 开销是"子秒级、可忽略但有"的量级，不能夸大成主要收益；`SKIP_COVERAGE` 的主要价值在于**确定性移除报告/聚合任务 + 关掉插桩（降测试 JVM 内存/GC 压力）**。
- **剩余瓶颈是应用层，非构建配置层**：`:example-spring:test` 的 Spring 上下文启动是测试本身的固有成本，构建脚本无法在不改测试的前提下压缩（除非引入测试切片 / 上下文缓存等应用层改造，超出本报告范围）。

### 4.11 把端到端测试套件从本地 `check` 解耦到 CI —— 已实施 ✅（排除法最大收获）
用「按插件有无的排除法」逐插件剖析本地 loop 后发现：在 `SKIP_QUALITY` + `SKIP_COVERAGE` 之后，lean loop（`clean check`）里**唯一剩余的"可选"重插件层**是 `:app` 无条件应用的 `io.github.mymx2.feature.test-end2end`——它创建了 `mockApi` 源码集，并注册 `testEndToEnd`（接 `check`）+ `testEndToEndSlow` 两个测试套件。基线实测这两个 e2e 套件各 ~3.3s，**合计 ~6.6s 测试执行，约占整条 check 循环的 70%**——是最大的单一本地循环杠杆。

**改动文件：**
1. `gradle/build-logic/.../io/github/mymx2/plugin/local/LocalConfig.kt`
   新增枚举项 `SKIP_E2E("SKIP_E2E", "false")`（默认 `false`，保持原行为）。
2. `app/build.gradle.kts`（模块文件，非 build-logic）
   把 `feature.test-end2end` 的应用与 e2e/mockApi 依赖整体门控：
   ```kotlin
   val skipE2E = project.getPropOrDefault(LocalConfig.Props.SKIP_E2E).toBoolean()
   if (!skipE2E) {
     apply(plugin = "io.github.mymx2.feature.test-end2end")
   }
   // ...
   if (!skipE2E) {
     dependencies {
       // 用字符串形式声明，避免插件未应用时 kotlin-dsl 访问器缺失导致编译失败
       "mockApiImplementation"(projects.app)
       "testEndToEndImplementation"(projects.app) {
         capabilities { requireFeature("mock-api") }
       }
     }
   }
   ```
   > 踩坑：kotlin-dsl 只为"已应用插件"生成 `mockApiImplementation` / `testEndToEndImplementation` 访问器；插件不应用时即便写在 `if (!skipE2E)` 里也是**编译期**报错。改用字符串形式 `"mockApiImplementation"(...)` 声明（运行期按名解析），门控才成立。

**用法：**
```bash
# 本地最快 TDD 循环：跳过静态分析 + 跳过覆盖率 + 跳过 e2e 套件（仅跑单元/集成测试）
./gradlew check -PSKIP_QUALITY=true -PSKIP_COVERAGE=true -PSKIP_E2E=true
```

**实测收益（lean loop `clean check -PSKIP_QUALITY=true -PSKIP_COVERAGE=true`）：**

| 指标 | e2e ON | e2e OFF (`SKIP_E2E=true`) | 说明 |
|---|---:|---:|---|
| `:app` 任务数 | 38 | 22 | 移除 mockApi 源码集 + testEndToEnd/testEndToEndSlow 套件 |
| lean loop 总任务数 | 108 | 92 | 结构性 −16 |
| `:app:testEndToEnd` + `:app:testEndToEndSlow` 执行 | 6.6s | 0 | e2e 测试执行时间（并行，见下） |
| 墙钟（暖，同 daemon 背靠背） | 7.2s | 4.4s | **约 −40%** |

- **确定性收益**：`SKIP_E2E` 把 `:app` 的 e2e 套件 + mockApi 源码集整套移出任务图（−16 任务），与缓存无关；默认 `false` 对 CI 与现有行为零影响（`testAggregateTestReport` 在 `:app` 不产出 e2e 结果时正常空聚合，构建仍 `BUILD SUCCESSFUL`）。
- **墙钟约 −40%**：e2e 两个套件执行合计 6.6s，但因 `maxParallelForks=4` 与 `:app:test`/`:example-spring:test` 并行，墙钟节省约 2.8s（7.2s → 4.4s）。加上 §4.10 的覆盖率、§4.2 的质量解耦，本地最快循环已能砍掉整条 `check` 的大部分非编译/非单元-集成测试开销。
- **诚实提示（与 §4.2/§4.10 的本质区别）**：`SKIP_E2E` 跳过的是**真实功能 e2e 测试**（不是"质量检查"或"覆盖率采集"）。默认 `false` 全部保留，CI 不受影响；本地用它可以极快做 TDD 内循环，但**推送前务必跑一次完整 `check`（不带此旗）**以补回 e2e 覆盖。
- **附带观察（未实施）**：`dokkaGenerateModuleHtml`（dokka 文档生成）也出现在本地 `check` 循环任务图里，若本地完全不需要文档，可再加一个 `SKIP_DOC` 门控作为下一杠杆（本 PR 未做）。

### 4.12 一次性开关 `SKIP_ALL_LOCAL` —— 把三条旗收成一面（本次"最后优化"）
到 §4.2 / §4.10 / §4.11 为止，本地最快循环需要**三个旗同时开**：`-PSKIP_QUALITY=true -PSKIP_COVERAGE=true -PSKIP_E2E=true`。
作为配置层压榨的收尾，新增一个**复合门控** `SKIP_ALL_LOCAL`，等价于同时开启三者，让「完全优化的本地循环」只需**一面旗**。

**改动文件（沿用既有 `LocalConfig.Props` 门控模式）：**
1. `gradle/build-logic/.../io/github/mymx2/plugin/local/LocalConfig.kt`
   新增枚举项 `SKIP_ALL_LOCAL("SKIP_ALL_LOCAL", "false")`（默认 `false`，保持原行为）。
2. 五个已有门控点各自 `|| skipAllLocal`（禁用路径）/`&& !skipAllLocal`（接线路径）与既有旗取并/交：
   - `io.github.mymx2.base.lifecycle.gradle.kts`：`check → qualityCheck` 接线加 `&& !skipAllLocal`
   - `io.github.mymx2.check.quality-detekt.gradle.kts`：`Detekt` 禁用条件 `skipQuality || skipAllLocal`
   - `io.github.mymx2.feature.test.gradle.kts`：jacoco 接线 `&& !skipAllLocal`、agent 关闭 `skipCoverage || skipAllLocal`
   - `io.github.mymx2.report.code-coverage.gradle.kts`：`testCodeCoverageReport` 接线 `&& !skipAllLocal`
   - `app/build.gradle.kts`：`feature.test-end2end` 应用 + e2e/mockApi 依赖整体 `!skipE2E && !skipAllLocal`

> 纯组合，无新逻辑：每个既有门控的"跳过"条件 `||` 进 `skipAllLocal`、"保留"条件 `&& !` 进 `skipAllLocal`，不引入新的跳过维度。

**用法：**
```bash
# 本地最快 TDD 循环：一面旗替代三条旗（跳过静态分析 + 覆盖率 + e2e 套件 + 打包）
./gradlew check -PSKIP_ALL_LOCAL=true
```

**实测收益（lean loop `clean check -PSKIP_ALL_LOCAL=true`，同硬件）：**

| 指标 | 三旗分别开 | `SKIP_ALL_LOCAL=true` | 说明 |
|---|---:|---:|---|
| `check` 质量任务 (detekt 等) | 0 | **0** | 等价 |
| `check` jacoco / coverage 任务 | 0 | **0** | 等价 |
| `:app` e2e 套件 + mockApi | 已移除 | **已移除** | 等价 |
| 暖循环墙钟 | 4.4s | **4.4s** | 完全等价（同一组任务图） |
| 需要的命令行旗数 | 3 | **1** | 收口，开发者体验提升 |

- **行为等价、墙钟等价**：`SKIP_ALL_LOCAL` 只是把三个独立门控的"跳过"集合并成一个开关，进入任务图与三旗同开完全一致（已验证 `clean check -PSKIP_ALL_LOCAL=true` `BUILD SUCCESSFUL`，`detekt` SKIPPED、jacoco / coverage / e2e / mockApi 任务全部缺席）。默认 `false` 对 CI 与现有行为零影响。
- **价值在开发者体验**：把"记住三个属性名 + 敲一长串 `-P`"收敛为单面旗，降低本地快循环的使用门槛；同时保留三旗可独立启停的细粒度（比如只想跳过 e2e、保留质量门禁做 `./gradlew check -PSKIP_E2E=true`）。
- **收尾定位**：这是配置层对本地循环的最后一次压榨——任务图层面 `SKIP_*` 三旗已把所有"非编译 / 非单元-集成测试"开销解耦干净，`SKIP_ALL_LOCAL` 是这层优化的收口开关，无新增优化维度。再往下只能靠 §4.1 的远程构建缓存（应用层凭证）或 §4.11 末尾提到的 `SKIP_DOC` 等应用层取舍。

---

## 5. 复现命令

```bash
# 性能剖析（生成 build/reports/profile/profile-*.html）
./gradlew :app:build --profile

# 验证配置缓存 / 构建缓存是否命中
./gradlew :app:build --console=plain   # 第二次起应显示 "UP-TO-DATE" / "Configuration cache entry reused"

# 查看 daemon JVM 信息
./gradlew :app:buildEnvironment

# 官方 Build Scan（本项目仅在 CI 上传；本地可用 --scan 触发本地分析）
./gradlew :app:build --scan
```

---

## 6. 小结

- **构建本身已高度优化**（配置缓存 + 构建缓存 + 增量编译），开发内循环 1–13s。
- **最大的"优化"是让构建能跑起来**：修复损坏的全局 `init.gradle`、补齐 JDK 25、按 local-env 安装 `fd`。
- **堆上限调优是负优化**：大内存机器保持默认堆。
- **质量门禁 + 覆盖率 + e2e 套件的 CI 解耦已实施**：`SKIP_QUALITY` 门控下本地 `build` 任务图质量任务 **10 → 0（解耦正确）**；`SKIP_COVERAGE` 门控下本地 `check` 任务图 jacoco 任务 **5 → 0（解耦正确）**并关闭 agent；`SKIP_E2E` 门控下本地 `check` 的 `:app` e2e 套件 + mockApi 源码集整套 **−16 任务（解耦正确）**。三者默认 `false`，对 CI 与现有行为零影响（见 §4.2 / §4.6 / §4.10 / §4.11）。`SKIP_QUALITY` 墙钟收益依赖缓存（无改动热重建仅省 ~0.8s，冷构建/改代码后省 ~13.6s）；`SKIP_COVERAGE` 确定性移除报告+聚合任务（热 ~0.37s / 冷 ~1.2s）；`SKIP_E2E` 墙钟约 **−40%**（7.2s → 4.4s 暖循环），是本地循环最大单一杠杆，但跳过的是**真实 e2e 功能测试**，推送前需跑完整 `check` 补回。三者已收口为复合门控 `SKIP_ALL_LOCAL`（默认 `false`，等价同时开启三者，见 §4.12），本地快循环一面旗即可。
- **本地最快验证循环**：`./gradlew check -PSKIP_ALL_LOCAL=true`（收口开关，等价于 `SKIP_QUALITY`+`SKIP_COVERAGE`+`SKIP_E2E`，跳过静态分析 + 覆盖率 + e2e 套件 + 打包）；若需细粒度，仍可用三旗任意组合（如只想跳 e2e：`-PSKIP_E2E=true`）。`check` 替代 `build` 本身已将任务数 43 → 19（见 §4.7 / §4.10 / §4.11 / §4.12）。
- **本 PR 已提交一项通用 `gradle.properties` 改动**：`org.gradle.configuration-cache.max-problems` `1 → 5`（非机器专属，CI 行为不受影响，见 §4.4）；机器专属项（`toolchain` 路径、`watch-fs` 默认已开）仍**不提交**（见 §4.4 / §4.3）。
- **已排除项**：`isolated-projects` 与本工程 build-logic 复合构建不兼容（BUILD FAILED），不启用（见 §4.8）。
- **顺带发现**：spotless/ktfmt 在 JDK25 下 `NoClassDefFoundError`，被构建缓存掩盖，建议另修（见 §4.9）。
- **真正可继续压榨的最大收益**在远程构建缓存（已接线，差 `BUILD_CACHE_USER`/`BUILD_CACHE_PWD` 凭证）。
