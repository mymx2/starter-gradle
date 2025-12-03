import type { Debugger } from 'debug'
import { exec as ex, spawn } from 'node:child_process'
import process from 'node:process'
import { promisify } from 'node:util'
import Debug from 'debug'

const exec = promisify(ex)

export class SimpleGit {
  private readonly _cwd: string
  private readonly _shell: string | undefined
  private readonly _log: Debugger

  constructor(
    cwd = process.cwd(),
    shell = process.env.SHELL,
    log = Debug('simple-git'),
  ) {
    this._cwd = cwd
    this._shell = shell
    this._log = log
  }

  public get shell() {
    return this._shell
  }

  public get cwd() {
    return this._cwd
  }

  async gitVersion() {
    return await exec('git -v', { cwd: this._cwd, shell: this._shell, encoding: 'utf-8' })
  }

  private async git(...args: string[]) {
    const command = `git ${args.join(' ')}`
    this._log(`In: ${command}`)
    const child = spawn('git', args, { cwd: this._cwd, shell: this._shell })
    return new Promise<string>((resolve, reject) => {
      let stdout = ''
      let stderr = ''
      child.stdout.on('data', (data) => {
        stdout += data
        this._log(`StdOut: ${stdout}`)
      })
      child.stderr.on('data', (data) => {
        stderr += data
        this._log(`StdErr: ${stderr}`)
      })
      child.on('close', (code) => {
        if (code === 0) {
          resolve(stdout)
        }
        else {
          this._log(`Error, in: ${command}`)
          this._log(`Error, out: ${stderr}`)
          reject(new Error(stderr))
        }
      })
    })
  }

  run = this.git

  private _curriedCommand(...name: string[]) {
    return (...args: string[]) => {
      return this.git(...name, ...args)
    }
  }

  async init(options: {
    cwd?: string
    name?: string
    email?: string
    gpgsign?: boolean
  } = {}) {
    const cwd = options.cwd ?? '.'
    await this.run('init', cwd)
    if (options.name) {
      await this.run('config', 'user.name', options.name)
    }
    if (options.email) {
      await this.run('config', 'user.email', options.email)
    }
    const gpg = options.gpgsign === undefined ? false : options.gpgsign
    await this.run('config', 'user.gpgsign', gpg.toString())
  }

  checkout = this._curriedCommand('checkout')

  checkoutBranch = this._curriedCommand('checkout', '-b')

  checkoutLocalBranch = this._curriedCommand('checkout', '-b')

  async merge(head: string) {
    await this.run('merge', head)
    return await this.lastCommit()
  }

  async mergeFromTo(from: string, to: string) {
    await this.run('checkout', to)
    await this.run('merge', from)
  }

  add = this._curriedCommand('add')

  async lastCommit() {
    const out = await this.run(
      'log',
      `-n 1`,
      `--pretty=format:'{"commit": "%H",  "abbreviated_commit": "%h",  "tree": "%T", "abbreviated_tree": "%t", "parent": "%P", "abbreviated_parent": "%p", "refs": "%D", "date": "%aD"}'`,
    )

    const logs = out
      .split('\n')
      .map(s => s.trim().substring(1, s.length - 1))
      .map(s => JSON.parse(s) as unknown)
      .map(s => ({
        // @ts-expect-error - hash
        hash: s.commit as string,
      }))
    return logs[0].hash
  }

  async commit(message: string) {
    await this.run('commit', '-m', message)
    return await this.lastCommit()
  }
}

export const git = new SimpleGit()
