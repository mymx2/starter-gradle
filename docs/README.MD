# Starter-Gradle官方文档

站点采用 [VitePress](https://vitepress.dev/zh/guide/getting-started) 构建，使用 [**Vite+**](https://viteplus.dev/guide/)
创建及管理项目。

## Vite+

`vp` 是一个全局命令行工具，负责整个开发生命周期。

* 使用 `vp help` 查看所有命令
* 使用 `vp <command> --help` 查看具体命令说明

---

## 项目相关命令

```shell
vp up # 更新依赖到最新版本
vp fmt # 格式化代码
vp check # 检查项目错误
vp check --fix # 检查并修复项目错误
vp run dev # 启动开发环境
vp run preview # 预览项目
vp run build # 构建项目
```

---

## ⚠️ 常见坑（Common Pitfalls）

* **不要直接使用包管理器**
  不要直接使用 pnpm、npm 或 Yarn，Vite+ 已统一处理。

* **运行脚本的区别**

  * `vp dev` → 永远是 Vite 开发服务器
  * `vp run dev` → 执行 package.json 中的 dev 脚本（本项目）

---
