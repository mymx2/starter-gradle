import { defineConfig } from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  lang: "zh-CN",
  base: "/",
  title: "Starter Gradle Doc",
  description: "A VitePress Site",
  head: [["link", { rel: "icon", href: "/favicon.ico" }]],
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "Home", link: "/" },
      { text: "Examples", link: "/examples/markdown-examples" },
      { text: "Engineering", link: "/engineering/perf-skip-quality-decoupling" },
    ],

    sidebar: [
      {
        text: "Examples",
        items: [
          { text: "Markdown Examples", link: "/examples/markdown-examples" },
          { text: "Runtime API Examples", link: "/examples/api-examples" },
          { text: "Learn CURL", link: "/examples/learn-curl" },
        ],
      },
      {
        text: "Engineering",
        items: [
          {
            text: "构建性能优化（SKIP 门控）",
            link: "/engineering/perf-skip-quality-decoupling",
          },
        ],
      },
    ],

    socialLinks: [{ icon: "github", link: "https://github.com/vuejs/vitepress" }],
  },
});
