import console from 'node:console'

import { initEnv, useRootDir } from '../utils.ts'
import { fns as actionCtxTestFns } from './action-ctx-test.ts'
import { fns as actionTestFns } from './action-test.ts'

initEnv()

function main(args: string[]) {
  const filePath = useRootDir('index.ts')
  console.log(`> ${filePath}`)
  console.log('> 欢迎使用 Main 方法！')
  if (args.length > 0) {
    console.log(`> 接收到 ${args.length} 个参数: ${args.join(', ')}`)
  }
}

export default [
  main,
  ...actionTestFns,
  ...actionCtxTestFns,
]
