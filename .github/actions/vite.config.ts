import { defineConfig } from 'vite-plus'

const ignorePatterns = ['pnpm-workspace.yaml', '**/*-lock.*', '__*']

export default defineConfig({
  lint: {
    options: {
      typeAware: true,
      typeCheck: true,
    },
    ignorePatterns: [...ignorePatterns],
  },
  fmt: {
    singleQuote: true,
    semi: false,
    arrowParens: 'avoid',
    ignorePatterns: [...ignorePatterns],
  },
  staged: {
    '*': 'vp check --fix --no-error-on-unmatched-pattern',
  },
  run: {
    cache: true,
  },
  test: {
    passWithNoTests: true,
  },
  resolve: {
    tsconfigPaths: true,
  },
  pack: {
    entry: ['index.ts'],
    dts: true,
  },
})
