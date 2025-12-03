// eslint doc: https://eslint.org/docs/latest/rules/
// typescript-eslint doc: https://typescript-eslint.io/rules/

// @ts-expect-error antfu
import eslintAntfu from '@antfu/eslint-config'

export default eslintAntfu(
  {
    unocss: true,
    overrides: {
      javascript: {
        'no-console': 'off', // 禁止使用console
        'eqeqeq': ['error', 'always', { null: 'ignore' }], // 要求使用 === 和 !==
      },
      typescript: {
        'ts/no-explicit-any': 'error',
        'unused-imports/no-unused-vars': 'off',
      },
    },
    formatters: {
      /**
       * Format Markdown files
       * Supports Prettier and dprint
       * By default uses Prettier
       */
      markdown: 'prettier',
    },
  },
  {
    rules: {
    },
  },
  {
    ignores: [
      '**/__*',
      '**/__*/**',
    ],
  },
)
