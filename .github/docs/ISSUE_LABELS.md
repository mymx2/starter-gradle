## Stander Github Labels

### **一、问题类型** `type|` (`#6F42C1` 深紫蓝)

_标识问题的根本性质_

- `type| bug` : BUG 反馈
- `type| question` : 问题反馈
- `type| regression` : 版本回归问题
- `type| task` : 一般任务
- `type| enhancement` : 功能增强
- `type| docs` : 文档相关
- `type| epic` : 大型任务追踪
- `type| dependency-update` : 依赖项升级

---

### **二、功能模块** `module|` (`#0366D6` 科技蓝)

_标识问题所属的技术领域_

- `module| code` : 代码相关
- `module| testing` : 测试相关
- `module| performance` : 性能优化
- `module| config` : 配置系统
- `module| plugins` : 插件生态
- `module| observability` : 可观测性
- `module| containers` : 容器化功能

---

### **三、处理状态** `status|` (`#22863A` 森林绿)

_动态跟踪问题解决进度_

```markdown
### 待处理

- `status| waiting-for-triage` : 待分类
- `status| waiting-reproduction` : 需复现
- `status| waiting-feedback` : 需补充信息

### 阻塞中

- `status| blocked-external` : 等待外部解决
- `status| blocked-internal` : 等待团队决策
- `status| blocked-on-hold` : 主动暂停
- `status| blocked-help-wanted` : 需外部协助

### 进行中

- `status| progress-planning` : 规划中
- `status| progress-fixing` : 处理中
- `status| progress-waiting-for-pr` : 等待 pr
- `status| progress-needs-review` : 待代码审核

### 待发布

- `status| prerelease-note-worthy` : 需写发布说明
- `status| prerelease-port-back` : 需向后兼容
- `status| prerelease-port-forward` : 需向前移植
```

---

### **四、终态标签** `close|` (``#6A737D` 中性灰)

_标记问题最终状态_

- `close| stackoverflow` : 更适合在 stackoverflow.com 上提问的问题
- `close| discussion` : 更适合作为讨论
- `close| external`: 跨项目问题
- `close| invalid` : 无效问题
- `close| duplicate` : 重复问题
- `close| declined` : 建议未采纳
- `close| superseded` : 已被替代
- `close| resolved` : 已解决
- `close| wontfix` : 暂不修复

---

```yaml
- type|
    - bug: A general bug
    - question: A question
    - regression: A regression from a previous release
    - task: A general task
    - enhancement: A general enhancement
    - docs: A documentation update
    - epic: An issue tracking a large piece of work that will be split into smaller issues
    - dependency-upgrade: A dependency upgrade
- module|
    - code: Issues related to the code
    - testing: Issues related to testing
    - config: Issues related to the configuration theme
    - performance: Issues related to general performance
    - plugins: Issues related to plugins
    - observability: Issues related to observability
    - containers: Testcontainers, Docker Compose and Buildpack features
- status|
    - waiting-for-triage: An issue we've not yet triaged
    - waiting-reproduction: We need a minimal reproducible example to continue
    - waiting-feedback: We need additional information before we can continue
    - blocked-external: An issue that's blocked on an external project change
    - blocked-internal: An issue we'd like to discuss as a team to make progress
    - blocked-on-hold: We can't start working on this issue yet
    - blocked-help-wanted: An issue that can only be worked on by brand new contributors
    - progress-planning: An issue we're planning to work on
    - progress-fixing: An issue we're actively working on
    - progress-waiting-for-pr: An issue that a contributor can help us with
    - progress-needs-review: An issue that needs a pr review
    - prerelease-note-worthy: A noteworthy issue to call out in the release notes
    - prerelease-port-back: An issue tracking the back-port of a change made in a later branch
    - prerelease-port-forward: An issue tracking the forward-port of a change made in an earlier branch
- close|
    - stackoverflow: A question that's better suited to stackoverflow.com
    - discussion: A question that's better suited to a discussion
    - external: An issue that's not relevant to the project
    - invalid: An issue that we don't feel is valid
    - duplicate: An issue that's a duplicate of another issue
    - declined: A suggestion or change that we don't feel we should currently apply
    - superseded: An issue that has been superseded by another
    - resolved: An issue that has been resolved
    - wontfix: An issue that we don't plan to fix
```
