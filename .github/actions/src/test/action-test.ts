import console from 'node:console'
import process from 'node:process'
import { Octokit } from 'octokit'
import { gLog } from '../utils.ts'

export function octokit() {
  return new Octokit({ auth: process.env.GITHUB_TOKEN })
}

export function ghAuth() {
  gLog('获取当前用户信息\n-----------------------------------')
  octokit().rest.users.getAuthenticated().then((result) => {
    console.log(result)
    gLog('-----------------------------------')
  })
}

export const fns = [
  ghAuth,
]
