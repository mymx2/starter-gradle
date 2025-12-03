## GitHub Actions

- [GitHub REST API](https://docs.github.com/en/rest/quickstart)
- [GitHub Actions Marketplace](https://github.com/marketplace?type=actions)
- [GitHub Actions](https://github.com/features/actions)
- [Accelerating new GitHub Actions workflows](https://github.com/actions/starter-workflows)

---

1. [工作流](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)是由一个或多个作业组成的可配置自动化流程，需通过**YAML 文件**（扩展名为.yml 或.yaml）定义，且必须存储在仓库的
   `.github/workflows`目录中。其核心配置包括**name**（工作流名称）、**on**
   （触发事件，如 push、pull_request、schedule 等，可通过 branches、paths 等过滤器限制）、**jobs**
   （由步骤 steps 组成，需指定运行环境 runs-on，支持矩阵策略 strategy.matrix 生成多作业），以及**permissions**（配置 GITHUB_TOKEN 权限）、
   **concurrency**
   （控制并发执行）等。工作流可重用（workflow_call）、支持手动触发（workflow_dispatch）和定时触发（cron 语法），步骤 steps 可运行命令或调用 actions，通过环境变量 env 和输出 outputs 传递信息。

示例：

```yaml
name: learn-github-actions
run-name: ${{ github.actor }} is learning GitHub Actions
on: [push]
jobs:
  check-bats-version:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@08c6903cd8c0fde910a37f88322edcfb5dd907a8
      - uses: actions/setup-node@2028fbc5c25fe9cf00d9f06a71cc4710d4507903
        with:
          node-version: "24"
      - run: npm install -g bats
      - run: bats -v
```

![可视化工作流](https://docs.github.com/assets/cb-34473/mw-1440/images/help/actions/overview-actions-event.webp)

---

2. 思维导图：

```mindmap
## **工作流基础**
- 定义：由一个或多个作业组成的可配置自动化流程
- 文件要求：YAML格式（.yml/.yaml），存储于.github/workflows
- YAML语法：需遵循YAML规则，可参考Learn YAML in Y minutes
## **核心配置字段**
- name：工作流名称，显示在Actions tab
- run-name：工作流运行名称（支持表达式）
- on：触发事件
  - 事件类型：push、pull_request、fork等
  - 过滤器：branches、paths、tags等
  - 定时：schedule（cron语法）
  - 手动：workflow_dispatch（支持输入）
  - 重用：workflow_call（定义输入输出）
- permissions：配置GITHUB_TOKEN权限，支持（read/write/none），可全局或作业级设置
- env：环境变量，支持（工作流/作业/步骤级），重名时更具体的生效
- defaults：默认设置（run的shell和工作目录），作业级覆盖全局
- concurrency：控制并发执行，通过组名和cancel-in-progress设置
- jobs：作业配置
  - 基本信息：job_id、name
  - 依赖：needs指定前置作业
  - 条件：if控制是否运行
  - 运行环境：runs-on指定runner（GitHub托管/自托管）
  - 环境：environment定义部署环境
  - 并发：job级concurrency
  - 输出：outputs定义作业输出，供下游作业使用
  - 步骤：steps包含命令、动作等，有id、if、name、uses、run等配置
```

---

3. 详细总结：

### 一、工作流基础

1. **定义**：工作流是由一个或多个作业（jobs）组成的可配置自动化流程，需通过 YAML 文件定义。
2. **文件要求**：

- 格式：必须使用`.yml`或`.yaml`扩展名。
- 存储位置：必须放在仓库的`.github/workflows`目录中。

3. **YAML 语法**：遵循 YAML 语法规则，新手可参考 [Learn YAML in Y minutes](https://learnxinyminutes.com/docs/yaml/)。

### 二、核心配置字段

#### 1. 名称与运行名称

- **name**：工作流的名称，显示在仓库的“Actions”标签下；若省略，显示工作流文件相对于仓库根目录的路径。
- **run-name**：工作流运行的名称，显示在工作流运行列表中；可包含表达式，引用`github`和`inputs`[上下文](https://docs.github.com/en/actions/reference/workflows-and-actions/contexts)；省略时为事件特定信息（如 push 的提交消息）。

### 2. 触发事件配置（`on`）

- **基本事件**：支持单个事件（如`on: push`）或多个事件（如`on: [push, fork]`），事件发生时触发工作流。
- **事件类型与过滤器**：
  - **activity types**：部分事件支持类型过滤，如`issues: { types: [opened, labeled] }`。
  - **分支/标签过滤**：通过`on.push.branches`（包含）、`branches-ignore`（排除）等限制，支持 glob 模式（如`releases/**`）。
  - **路径过滤**：`on.push.paths`（包含）、`paths-ignore`（排除）指定文件变化触发，如`'**.js'`匹配所有 JS 文件。
- **定时触发**：`on.schedule`使用 POSIX cron 语法，最短间隔 5 分钟，如`cron: '30 5,17 * * *'`（每天 5:30 和 17:30 触发）。
- **手动触发**：`on.workflow_dispatch`支持输入参数（类型包括 boolean、choice 等），仅在默认分支的工作流文件生效。
- **工作流重用与依赖**：`on.workflow_call`定义可重用工作流的输入、输出和 secrets；`on.workflow_run`在其他工作流运行后触发，可通过 branches 过滤。

#### 3. 权限配置（permissions）

- 作用：修改`GITHUB_TOKEN`的默认权限，遵循最小权限原则。
- 配置层级：可全局设置（应用于所有作业）或作业级设置（仅应用于特定作业）。
- 权限级别：对各权限（如 actions、contents、issues 等）可设`read`、`write`、`none`，`write`包含`read`；未指定的权限默认`none`。
- 快捷配置：`read-all`（所有权限为 read）、`write-all`（所有权限为 write）、`{}`（所有权限为 none）。
- 特殊情况：分叉仓库通常仅能配置 read 权限，除非管理员开启相关设置；Dependabot 触发的工作流`GITHUB_TOKEN`为只读。

| 权限            | 允许的操作示例                                                          |
| --------------- | ----------------------------------------------------------------------- |
| `actions`       | 处理 GitHub Actions，如`actions: write`可取消工作流运行                 |
| `contents`      | 处理仓库内容，如`contents: read`可列出提交，`contents: write`可创建发布 |
| `issues`        | 处理 issues，如`issues: write`可添加评论                                |
| `pull-requests` | 处理拉取请求，如`pull-requests: write`可添加标签                        |

#### 4. 环境变量（env）

- 定义：键值对形式的变量，供步骤使用。
- 层级：全局（所有作业）、作业级（特定作业的所有步骤）、步骤级（仅该步骤）。
- 优先级：重名时，步骤级 > 作业级 > 全局。

#### 5. 默认设置（defaults）

- 作用：为所有作业或步骤设置默认值。
- `defaults.run`：配置`run`步骤的默认`shell`和`working-directory`，作业级设置可覆盖全局。

#### 6. 并发控制（concurrency）

- 作用：确保同一并发组中只有一个作业或工作流运行。
- 配置：`group`（组名，可含表达式）和`cancel-in-progress`（是否取消正在运行的实例，默认 false）。
- 注意：组名不区分大小写，同一组内运行顺序不保证。

#### 7. 作业（jobs）

- **基本配置**：
  - `job_id`：唯一标识符，需以字母或*开头，含字母、数字、-、*。
  - `name`：作业名称，显示在 UI 中。
- **依赖（needs）**：指定前置作业，需前置作业成功才运行；可用`always()`使作业无论前置结果都运行。
- **条件（if）**：通过表达式控制作业是否运行，支持上下文和表达式（如`if: github.repository == 'owner/repo'`）。
- **运行环境（runs-on）**：指定运行作业的机器，可为 GitHub 托管[runner](https://github.com/actions/runner-images)（如`ubuntu-latest`）、自托管 runner（通过标签或组指定）。
- **环境（environment）**：定义部署环境，可含名称和 URL，需通过部署保护规则。
- **并发（concurrency）**：作业级并发控制，同全局配置逻辑。
- **输出（outputs）**：定义作业输出，供下游依赖作业使用，单个作业输出最大**1MB**，总输出最大**50MB**。
- **步骤（steps）**：作业包含的任务序列，可运行命令、动作等，每个步骤在独立进程中运行。
  - 关键配置：`id`（唯一标识）、`if`（条件）、`name`（名称）、`uses`（引用动作）、`run`（运行命令）、`shell`（指定 shell）等。
  - 限制：最多显示 1000 个检查，步骤数量受工作流使用限制。
  - 超时：作业超时时间，默认 360 分钟。

### 三、其他关键信息

- **输入输出**：`workflow_call`支持输入（需指定 type：boolean/number/string）和输出；`workflow_dispatch`支持输入，类型包括 boolean/choice/number/environment/string。
- **secrets 管理**：`workflow_call.secrets`定义可传递的 secrets，调用方传递未定义的 secrets 会报错。
- **Git diff 限制**：路径过滤基于 diff，diff 限制 300 个文件，超过 1000 次提交或超时则工作流必运行。

---

1. 关键问题：
2. 问题：如何配置工作流仅在特定分支的特定文件路径变更时触发？

   答案：可通过`on`字段的分支过滤器和路径过滤器组合实现。例如，对`push`事件，使用`branches`指定目标分支，`paths`指定需监控的文件路径，两者需同时满足才触发。示例：

   ```yaml
   on:
     push:
       branches: [main, "releases/**"]
       paths: ["**.js", "!docs/**"] # 监控.js文件，排除docs目录
   ```

3. 问题：`GITHUB_TOKEN`的权限如何配置，不同层级的配置有何优先级？

   答案：`GITHUB_TOKEN`权限通过`permissions`配置，支持`read`、`write`、`none`三个级别。配置层级包括：企业/组织/仓库默认设置 → 工作流全局`permissions` → 作业级`jobs.<job_id>.permissions`。优先级为：作业级配置覆盖全局配置，全局配置覆盖默认设置；若为分叉仓库且未开启“Send write tokens”，则写权限会降为只读。

4. 问题：如何控制工作流的并发执行，避免同一环境的部署冲突？

   答案：可通过`concurrency`配置实现。设置`group`定义并发组（如环境名称），`cancel-in-progress: true`则新实例触发时取消正在运行的实例。示例：

   ```yaml
   concurrency:
     group: staging_environment # 以环境名为组
     cancel-in-progress: true # 取消正在运行的实例
   ```

5. 问题：如何创建 JavaScript 操作，如何调试工作流？

   答案：
   - 创建 JavaScript 操作：可以参考 GitHub Actions 官方文档：https://docs.github.com/en/actions/tutorials/create-actions/create-a-javascript-action
   - 调试工作流：要启用工作流的调试模式，可以在 GitHub 仓库添加密钥或变量`ACTIONS_RUNNER_DEBUG`，并设为`true`。密钥的优先级高于变量，启用了步骤调试日志，会输出更多调试信息。
