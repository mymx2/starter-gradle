// https://docs.github.com/en/actions/learn-github-actions/contexts#github-context
// https://github.com/actions/languageservices/blob/main/languageservice/src/context-providers/github.ts#L12
/**
 * GitHub Actions 上下文定义
 * 基于官方文档和 schema 定义
 */

import type { WebhookEventMap } from '@octokit/webhooks-definitions/schema.d.ts'

/**
 * github 上下文
 * 包含有关工作流运行的信息。
 * 更多信息请参阅 [`github` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#github-context)。
 */
export interface GithubContext {
  /**
   * 当前正在运行的操作的名称，或步骤的 [`id`](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsid)。
   * GitHub 会移除特殊字符，即如果当前步骤运行一个没有 `id` 的脚本，则使用名称 `__run`。
   * 如果你在同一个作业中多次使用同一个操作，名称将包含后缀，后缀为下划线前的序列号。
   * 例如，你运行的第一个脚本将命名为 `__run`，第二个脚本将命名为 `__run_2`。
   * 同样，`actions/checkout` 的第二次调用将是 `actionscheckout2`。
   */
  action: string

  /**
   * 操作所在的路径。
   * 此属性仅在复合操作（composite actions）中受支持。
   * 你可以使用此路径访问与操作位于同一存储库中的文件，例如通过更改目录到该路径：`cd ${{ github.action_path }}`。
   */
  action_path?: string

  /**
   * 对于执行操作的步骤，这是正在执行的操作的 ref。
   * 例如，`v2`。
   */
  action_ref?: string

  /**
   * 对于执行操作的步骤，这是操作的所有者和存储库名称。
   * 例如，`actions/checkout`。
   */
  action_repository?: string

  /**
   * 对于复合操作，这是复合操作的当前结果。
   */
  action_status?: string

  /**
   * 触发初始工作流运行的用户的用户名。
   * 如果工作流运行是重新运行（re-run），此值可能与 `github.triggering_actor` 不同。
   * 任何工作流重新运行都将使用 `github.actor` 的权限，即使发起重新运行的 actor (`github.triggering_actor`) 拥有不同的权限。
   */
  actor: string

  /**
   * 触发初始工作流运行的人员或应用程序的帐户 ID。
   * 例如，`1234567`。
   * 注意：这与 actor 用户名不同。
   * 版本要求: ghes >= 3.9, ghae >= 3.9
   */
  actor_id: string

  /**
   * GitHub REST API 的 URL。
   */
  api_url: string

  /**
   * 工作流运行中拉取请求（pull request）的 `base_ref` 或目标分支。
   * 仅当触发工作流运行的事件是 `pull_request` 或 `pull_request_target` 时，此属性才可用。
   */
  base_ref?: string

  /**
   * 运行器上用于设置工作流命令环境变量的文件路径。
   * 此文件对于当前步骤是唯一的，并且作业中的每个步骤都是不同的文件。
   * 更多信息请参阅 "[GitHub Actions 的工作流命令](https://docs.github.com/actions/using-workflows/workflow-commands-for-github-actions#setting-an-environment-variable)"。
   */
  env: string

  /**
   * 完整的事件 webhook 负载。你可以使用此上下文访问事件的各个属性。
   * 此对象与触发工作流运行的事件的 webhook 负载相同，并且每个事件都不同。
   * 每个 GitHub Actions 事件的 webhook 都在 "[触发工作流的事件](https://docs.github.com/actions/using-workflows/events-that-trigger-workflows)" 中链接。
   * 例如，对于由 [`push` 事件](https://docs.github.com/actions/using-workflows/events-that-trigger-workflows#push) 触发的工作流运行，此对象包含 [push webhook 负载](https://docs.github.com/webhooks-and-events/webhooks/webhook-events-and-payloads#push) 的内容。
   */
  event: WebhookEventMap

  /**
   * 触发工作流运行的事件名称。
   */
  event_name: string

  /**
   * 运行器上包含完整事件 webhook 负载的文件路径。
   */
  event_path: string

  /**
   * GitHub GraphQL API 的 URL。
   */
  graphql_url: string

  /**
   * 工作流运行中拉取请求（pull request）的 `head_ref` 或源分支。
   * 仅当触发工作流运行的事件是 `pull_request` 或 `pull_request_target` 时，此属性才可用。
   */
  head_ref?: string

  /**
   * 当前作业的 [`job_id`](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_id)。
   * 注意：此上下文属性由 Actions 运行器设置，并且仅在作业的执行 `steps` 中可用。否则，此属性的值将为 `null`。
   */
  job: string

  /**
   * 对于使用可重用工作流的作业，这是可重用工作流文件的提交 SHA。
   * 版本要求: ghes >= 3.9, ghae >= 3.9
   */
  job_workflow_sha?: string

  /**
   * 运行器上用于从工作流命令设置系统 PATH 变量的文件路径。
   * 此文件对于当前步骤是唯一的，并且作业中的每个步骤都是不同的文件。
   * 更多信息请参阅 "[GitHub Actions 的工作流命令](https://docs.github.com/actions/using-workflows/workflow-commands-for-github-actions#adding-a-system-path)"。
   */
  path: string

  /**
   * 触发工作流运行的分支或标记的完整格式引用（ref）。
   * 对于由 `push` 触发的工作流，这是被推送的分支或标记 ref。
   * 对于由 `pull_request` 触发的工作流，这是拉取请求合并分支。
   * 对于由 `release` 触发的工作流，这是创建的发布标记。
   * 对于其他触发器，这是触发工作流运行的分支或标记 ref。
   * 仅当事件类型有可用的分支或标记时才设置此项。
   * 给出的 ref 是完整格式的，这意味着对于分支，格式是 `refs/heads/<branch_name>`，对于拉取请求是 `refs/pull/<pr_number>/merge`，对于标记是 `refs/tags/<tag_name>`。
   * 例如，`refs/heads/feature-branch-1`。
   */
  ref: string

  /**
   * 触发工作流运行的分支或标记的短 ref 名称。
   * 此值与 GitHub 上显示的分支或标记名称匹配。
   * 例如，`feature-branch-1`。
   */
  ref_name: string

  /**
   * 如果为触发工作流运行的 ref 配置了分支保护，则为 `true`。
   */
  ref_protected: boolean

  /**
   * 触发工作流运行的 ref 类型。
   * 有效值为 `branch` 或 `tag`。
   */
  ref_type: 'branch' | 'tag'

  /**
   * 所有者和存储库名称。
   * 例如，`octocat/Hello-World`。
   */
  repository: string

  /**
   * 存储库的 ID。
   * 例如，`123456789`。
   * 注意：这与存储库名称不同。
   * 版本要求: ghes >= 3.9, ghae >= 3.9
   */
  repository_id: string

  /**
   * 存储库所有者的用户名。
   * 例如，`octocat`。
   */
  repository_owner: string

  /**
   * 存储库所有者的帐户 ID。
   * 例如，`1234567`。
   * 注意：这与所有者的名称不同。
   * 版本要求: ghes >= 3.9, ghae >= 3.9
   */
  repository_owner_id: string

  /**
   * 存储库的 Git URL。
   * 例如，`git://github.com/octocat/hello-world.git`。
   */
  repositoryUrl: string

  /**
   * 工作流运行日志和工件（artifacts）保留的天数。
   */
  retention_days: number

  /**
   * 存储库中每个工作流运行的唯一编号。
   * 如果你重新运行工作流，此编号不会改变。
   */
  run_id: number

  /**
   * 存储库中特定工作流每次运行的唯一编号。
   * 此编号从工作流的第一次运行开始为 1，并随着每次新运行而递增。
   * 如果你重新运行工作流，此编号不会改变。
   */
  run_number: number

  /**
   * 存储库中特定工作流运行的每次尝试的唯一编号。
   * 此编号从工作流运行的第一次尝试开始为 1，并随着每次重新运行而递增。
   */
  run_attempt: number

  /**
   * 工作流中使用的机密（secret）的来源。
   * 可能的值为 `None`, `Actions`, `Dependabot`, 或 `Codespaces`。
   */
  secret_source: 'None' | 'Actions' | 'Dependabot' | 'Codespaces'

  /**
   * GitHub 服务器的 URL。
   * 例如：`https://github.com`。
   */
  server_url: string

  /**
   * 触发工作流的提交 SHA。
   * 此提交 SHA 的值取决于触发工作流的事件。
   * 更多信息请参阅 "[触发工作流的事件](https://docs.github.com/actions/using-workflows/events-that-trigger-workflows)"。
   * 例如，`ffac537e6cbbf934b08745a378932722df287a53`。
   */
  sha: string

  /**
   * 代表安装在你的存储库上的 GitHub App 进行身份验证的令牌。
   * 这在功能上等同于 `GITHUB_TOKEN` 机密。
   * 更多信息请参阅 "[自动令牌身份验证](https://docs.github.com/actions/security-guides/automatic-token-authentication)"。
   * 注意：此上下文属性由 Actions 运行器设置，并且仅在作业的执行 `steps` 中可用。否则，此属性的值将为 `null`。
   */
  token: string

  /**
   * 发起工作流运行的用户的用户名。
   * 如果工作流运行是重新运行（re-run），此值可能与 `github.actor` 不同。
   * 任何工作流重新运行都将使用 `github.actor` 的权限，即使发起重新运行的 actor (`github.triggering_actor`) 拥有不同的权限。
   */
  triggering_actor: string

  /**
   * 工作流的名称。
   * 如果工作流文件没有指定 `name`，则此属性的值是存储库中工作流文件的完整路径。
   */
  workflow: string

  /**
   * 工作流的 ref 路径。
   * 例如，`octocat/hello-world/.github/workflows/my-workflow.yml@refs/heads/my_branch`。
   * 版本要求: ghes >= 3.9, ghae >= 3.9
   */
  workflow_ref: string

  /**
   * 工作流文件的提交 SHA。
   * 版本要求: ghes >= 3.9, ghae >= 3.9
   */
  workflow_sha: string

  /**
   * 运行器上用于步骤的默认工作目录，以及使用 [`checkout`](https://github.com/actions/checkout) 操作时存储库的默认位置。
   */
  workspace: string
}

/**
 * runner 上下文
 * 有关正在运行当前作业的运行器的信息。
 * 更多信息请参阅 [`runner` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#runner-context)。
 */
export interface RunnerContext {
  /**
   * 执行作业的运行器的名称。
   */
  name: string

  /**
   * 执行作业的运行器的操作系统。
   * 可能的值为 `Linux`, `Windows`, 或 `macOS`。
   */
  os: 'Linux' | 'Windows' | 'macOS'

  /**
   * 执行作业的运行器的架构。
   * 可能的值为 `X86`, `X64`, `ARM`, 或 `ARM64`。
   */
  arch: 'X86' | 'X64' | 'ARM' | 'ARM64'

  /**
   * 运行器上临时目录的路径。
   * 此目录在每个作业的开始和结束时清空。
   * 注意，如果运行器的用户帐户没有删除文件的权限，文件将不会被删除。
   */
  temp: string

  /**
   * 包含 GitHub 托管运行器预安装工具的目录路径。
   * 更多信息请参阅 "[关于 GitHub 托管的运行器](https://docs.github.com/actions/reference/specifications-for-github-hosted-runners/#supported-software)"。
   */
  tool_cache: string

  /**
   * 仅当启用了 [调试日志记录](https://docs.github.com/actions/monitoring-and-troubleshooting-workflows/enabling-debug-logging) 时才设置此项，并且值始终为 `1`。
   * 它可用作指示器，以便在你自己的作业步骤中启用其他调试或详细日志记录。
   */
  debug?: '1'
}

/**
 * strategy 上下文
 * 有关当前作业的矩阵执行策略的信息。
 * 更多信息请参阅 [`strategy` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#strategy-context)。
 */
export interface StrategyContext {
  /**
   * 当为 `true` 时，如果矩阵中的任何作业失败，则取消所有正在进行的作业。
   * 更多信息请参阅 "[GitHub Actions 的工作流语法](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstrategyfail-fast)"。
   */
  'fail-fast': boolean

  /**
   * 当前作业在矩阵中的索引。
   * **注意：** 这是一个基于零的数字。矩阵中第一个作业的索引是 `0`。
   */
  'job-index': number

  /**
   * 矩阵中作业的总数。
   * **注意：** 此数字 **不是** 基于零的数字。例如，对于有四个作业的矩阵，`job-total` 的值是 `4`。
   */
  'job-total': number

  /**
   * 使用矩阵作业策略时可以同时运行的最大作业数。
   * 更多信息请参阅 "[GitHub Actions 的工作流语法](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstrategymax-parallel)"。
   */
  'max-parallel': number
}

/**
 * 单个步骤的信息
 * 用于 steps 上下文
 */
export interface StepContext {
  /**
   * 为步骤定义的输出集。
   * 更多信息请参阅 "[GitHub Actions 的元数据语法](https://docs.github.com/actions/creating-actions/metadata-syntax-for-github-actions#outputs-for-docker-container-and-javascript-actions)"。
   */
  outputs: Record<string, string>

  /**
   * 应用 [`continue-on-error`](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepscontinue-on-error) 后完成步骤的结果。
   * 可能的值为 `success`, `failure`, `cancelled`, 或 `skipped`。
   * 当 `continue-on-error` 步骤失败时，`outcome` 是 `failure`，但最终的 `conclusion` 是 `success`。
   */
  conclusion: 'success' | 'failure' | 'cancelled' | 'skipped'

  /**
   * 应用 [`continue-on-error`](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepscontinue-on-error) 之前完成步骤的结果。
   * 可能的值为 `success`, `failure`, `cancelled`, 或 `skipped`。
   * 当 `continue-on-error` 步骤失败时，`outcome` 是 `failure`，但最终的 `conclusion` 是 `success`。
   */
  outcome: 'success' | 'failure' | 'cancelled' | 'skipped'
}

/**
 * 可重用工作流中的作业信息
 * 用于 jobs 上下文
 */
export interface JobContext {
  /**
   * 可重用工作流中作业的输出集。
   */
  outputs: Record<string, string>

  /**
   * 可重用工作流中作业的结果。
   * 可能的值为 `success`, `failure`, `cancelled`, 或 `skipped`。
   */
  result: 'success' | 'failure' | 'cancelled' | 'skipped'
}

/**
 * GitHub Actions 全局上下文对象
 */
export interface ActionsContext {
  /**
   * 信息关于工作流运行。
   */
  github: GithubContext

  /**
   * 包含在工作流、作业或步骤中设置的变量。
   * 更多信息请参阅 [`env` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#env-context)。
   */
  env: Record<string, string>

  /**
   * 包含在存储库、组织或环境级别设置的变量。
   * 更多信息请参阅 [`vars` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#vars-context)。
   */
  vars: Record<string, string>

  /**
   * 有关当前正在运行的作业的信息。
   * 更多信息请参阅 [`job` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#job-context)。
   */
  job: {
    /**
     * 只有在使用可重用工作流的上下文中，此对象才包含输出和结果。
     * 在常规作业上下文中，它通常包含状态和其他元数据。
     */
    status: string
    // eslint-disable-next-line ts/no-explicit-any
    services?: Record<string, any>
    // eslint-disable-next-line ts/no-explicit-any
    container?: Record<string, any>
  }

  /**
   * 仅用于可重用工作流，包含来自可重用工作流的作业输出。
   * 更多信息请参阅 [`jobs` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#jobs-context)。
   */
  jobs: Record<string, JobContext>

  /**
   * 有关当前作业中已运行的步骤的信息。
   * 键是步骤的 ID。
   * 更多信息请参阅 [`steps` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#steps-context)。
   */
  steps: Record<string, StepContext>

  /**
   * 有关运行当前作业的运行器的信息。
   * 更多信息请参阅 [`runner` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#runner-context)。
   */
  runner: RunnerContext

  /**
   * 包含可用于工作流运行的机密的名称和值。
   * 更多信息请参阅 [`secrets` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#secrets-context)。
   */
  secrets: Record<string, string> & {
    /**
     * 为每次工作流运行自动创建的令牌。
     * 更多信息请参阅 "[自动令牌身份验证](https://docs.github.com/actions/security-guides/automatic-token-authentication)"。
     */
    GITHUB_TOKEN?: string
  }

  /**
   * 有关当前作业的矩阵执行策略的信息。
   * 更多信息请参阅 [`strategy` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#strategy-context)。
   */
  strategy: StrategyContext

  /**
   * 包含定义在工作流中且适用于当前作业的矩阵属性。
   * 更多信息请参阅 [`matrix` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#matrix-context)。
   */
  // eslint-disable-next-line ts/no-explicit-any
  matrix: Record<string, any>

  /**
   * 包含定义为当前作业依赖项的所有作业的输出。
   * 更多信息请参阅 [`needs` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#needs-context)。
   */
  needs: Record<string, { outputs: Record<string, string>, result: string }>

  /**
   * 包含可重用或手动触发工作流的输入。
   * 更多信息请参阅 [`inputs` 上下文](https://docs.github.com/actions/learn-github-actions/contexts#inputs-context)。
   */
  inputs: Record<string, string | number | boolean>
}

/**
 * 可用的全局函数
 */
export interface ActionsFunctions {
  /**
   * 即使取消，也使步骤始终执行，并返回 `true`。
   * `always` 表达式最适合用于步骤级别，或用于你期望即使作业被取消也要运行的任务。
   * 例如，你可以使用 `always` 来发送日志，即使作业被取消。
   */
  always: () => boolean

  /**
   * 如果工作流被取消，则返回 `true`。
   */
  cancelled: () => boolean

  /**
   * 当作业的任何先前步骤失败时返回 `true`。
   * 如果你有一连串的依赖作业，如果任何祖先作业失败，`failure()` 返回 `true`。
   */
  failure: () => boolean

  /**
   * 为匹配 `path` 模式的文件集返回单个哈希。
   * 你可以提供单个 `path` 模式或由逗号分隔的多个 `path` 模式。
   * `path` 相对于 `GITHUB_WORKSPACE` 目录，并且只能包含 `GITHUB_WORKSPACE` 内部的文件。
   * 此函数计算每个匹配文件的单独 SHA-256 哈希，然后使用这些哈希计算文件集的最终 SHA-256 哈希。
   * 如果 `path` 模式不匹配任何文件，则返回空字符串。
   * 有关 SHA-256 的更多信息，请参阅 "[SHA-2](https://wikipedia.org/wiki/SHA-2)"。
   *
   * 你可以使用模式匹配字符来匹配文件名。模式匹配在 Windows 上不区分大小写。
   * 有关支持的模式匹配字符的更多信息，请参阅 "[GitHub Actions 的工作流语法](https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#filter-pattern-cheat-sheet)"。
   */
  hashFiles: (...paths: string[]) => string
}

/**
 * GitHub Actions default environment variables. These are set for every run of a
 * workflow and can be used in your actions.
 * - https://github.com/actions/typescript-action/blob/main/.env.example
 * - https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables
 */
export interface EnvContext {
  CI: string
  GITHUB_ACTION: string
  GITHUB_ACTION_PATH: string
  GITHUB_ACTION_REPOSITORY: string
  GITHUB_ACTIONS: string
  GITHUB_ACTOR: string
  GITHUB_ACTOR_ID: string
  GITHUB_API_URL: string
  GITHUB_BASE_REF: string
  GITHUB_ENV: string
  GITHUB_EVENT_NAME: string
  GITHUB_EVENT_PATH: string
  GITHUB_GRAPHQL_URL: string
  GITHUB_HEAD_REF: string
  GITHUB_JOB: string
  GITHUB_OUTPUT: string
  GITHUB_PATH: string
  GITHUB_REF: string
  GITHUB_REF_NAME: string
  GITHUB_REF_PROTECTED: string
  GITHUB_REF_TYPE: string
  GITHUB_REPOSITORY: string
  GITHUB_REPOSITORY_ID: string
  GITHUB_REPOSITORY_OWNER: string
  GITHUB_REPOSITORY_OWNER_ID: string
  GITHUB_RETENTION_DAYS: string
  GITHUB_RUN_ATTEMPT: string
  GITHUB_RUN_ID: string
  GITHUB_RUN_NUMBER: string
  GITHUB_SERVER_URL: string
  GITHUB_SHA: string
  GITHUB_STEP_SUMMARY: string
  GITHUB_TRIGGERING_ACTOR: string
  GITHUB_WORKFLOW: string
  GITHUB_WORKFLOW_REF: string
  GITHUB_WORKFLOW_SHA: string
  GITHUB_WORKSPACE: string
  RUNNER_ARCH: string
  RUNNER_DEBUG: string
  RUNNER_NAME: string
  RUNNER_OS: string
  RUNNER_TEMP: string
  RUNNER_TOOL_CACHE: string
}
