import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Starter Gradle",
  description: "Gradle 构建系统模板 - 提供约定插件、质量检查和 CI/CD 自动化支持",
  
  lang: 'zh-CN',
  lastUpdated: true,
  
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['meta', { name: 'theme-color', content: '#3eaf7c' }],
  ],

  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    logo: {
      src: '/favicon.ico',
      width: 24,
      height: 24
    },

    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/introduction' },
      { text: '示例', link: '/examples/markdown-examples' },
      { text: 'GitHub', link: 'https://github.com/mymx2/starter-gradle' },
    ],

    sidebar: [
      {
        text: '开始',
        items: [
          { text: '快速开始', link: '/guide/quickstart' },
          { text: '项目介绍', link: '/guide/introduction' },
        ]
      },
      {
        text: '深入指南',
        items: [
          { text: '配置指南', link: '/guide/configuration' },
          { text: '插件详解', link: '/guide/plugins' },
          { text: '最佳实践', link: '/guide/best-practices' },
          { text: '故障排除', link: '/guide/troubleshooting' },
        ]
      },
      {
        text: '示例',
        items: [
          { text: 'Markdown 示例', link: '/examples/markdown-examples' },
          { text: 'API 示例', link: '/examples/api-examples' },
        ]
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/mymx2/starter-gradle' },
    ],

    footer: {
      message: '基于 Apache 2.0 许可证发布',
      copyright: 'Copyright © 2024 mymx2'
    },

    editLink: {
      pattern: 'https://github.com/mymx2/starter-gradle/edit/main/docs/:path',
      text: '在 GitHub 上编辑此页面'
    },

    search: {
      provider: 'local',
      options: {
        locales: {
          zh: {
            translations: {
              button: {
                buttonText: '搜索',
                buttonAriaLabel: '搜索文档'
              },
              modal: {
                noResultsText: '无法找到相关结果',
                resetButtonTitle: '清除查询条件',
                footer: {
                  selectText: '选择',
                  navigateText: '切换'
                }
              }
            }
          }
        }
      }
    }
  },

  markdown: {
    theme: {
      light: 'github-light',
      dark: 'github-dark'
    },
    
    lineNumbers: true,
  }
})
