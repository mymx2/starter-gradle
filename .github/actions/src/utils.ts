import { join, resolve } from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'
import Debug from 'debug'
import fs from 'node:fs'

/**
 * 是否win环境
 */
export const isWin = process.platform === 'win32'
/**
 * 用户目录
 */
export const home = isWin ? process.env.USERPROFILE : process.env.HOME
/**
 * src目录
 */
const __srcDir = fileURLToPath(new URL('..', import.meta.url))
/**
 * 项目目录
 */
export const DIR_ROOT = resolve(__srcDir, '.')
/**
 * 文件目录
 * @param relativePath 相对目录
 */
export function useRootDir(relativePath: string) {
  return join(DIR_ROOT, relativePath)
}
export const gLog = Debug('trace')
gLog.enabled = process.env.DEBUG === '*'
export function initEnv() {
  const envPath = useRootDir('.env.local')
  if (!fs.existsSync(envPath)) return
  fs.readFileSync(envPath, 'utf-8')
    .split('\n')
    .map(line => line.trim())
    .filter(line => line && !line.startsWith('#') && line.includes('='))
    .forEach(line => {
      const [key, ...rest] = line.split('=')
      let value = rest.join('=').trim()
      // 去掉双/单引号
      if (
        (value.startsWith('"') && value.endsWith('"')) ||
        (value.startsWith("'") && value.endsWith("'"))
      ) {
        value = value.slice(1, -1)
      }
      process.env[key.trim()] = value
    })
  // console.log('🌱 环境变量加载完成')
}
