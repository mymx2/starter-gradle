import console from 'node:console'
import process from 'node:process'

import * as main from './src/main/index.ts'
import * as test from './src/test/index.ts'

const testEnv = process.env.ENV === 'test'

if (!testEnv) {
  globalize(...main.default)
}
else {
  globalize(...test.default)
}

// eslint-disable-next-line ts/no-unsafe-function-type
function globalize(...fns: Function[]) {
  // @ts-expect-error any
  if (!globalThis.__methods__) {
    // @ts-expect-error any
    globalThis.__methods__ = {}
  }
  // @ts-expect-error any
  const globalMethods = globalThis.__methods__

  fns.forEach((fn) => {
    if (fn.name) {
      globalMethods[fn.name] = fn
    }
  })
}

const methodName = process.argv[2]
const args = process.argv.slice(3)
// @ts-expect-error any
const globalMethods = globalThis.__methods__

// eslint-disable-next-line ts/no-unsafe-function-type
let fnToCall: Function | undefined
if (methodName) {
  fnToCall = globalMethods?.[methodName]
}
else {
  fnToCall = globalMethods?.main
}

// 如果都不存在，报错
if (!fnToCall || typeof fnToCall !== 'function') {
  console.error(
    `❌ 方法 "${methodName || 'main'}" 不存在！可用方法: ${Object.keys(globalMethods || {}).join(', ')}`,
  )
  process.exit(1)
}
// eslint-disable-next-line antfu/no-top-level-await
await fnToCall(args)
