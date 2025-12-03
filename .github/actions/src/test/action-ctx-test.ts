import type {
  IssueCommentEvent,
  IssuesLabeledEvent,
  PullRequestOpenedEvent,
  WorkflowRunCompletedEvent,
} from '@octokit/webhooks-definitions/schema.d.ts'

import console from 'node:console'
import { context } from '@actions/github'
import glob from '@actions/glob'
import { match, minimatch } from 'minimatch'
import { githubCtx as github } from './action-ctx.ts'
import { octokit } from './action-test.ts'

function patternMatchDemo() {
  const branchesMatch = minimatch('releases/beta/3-alpha', 'releases/**')
  const branchesMatchNegative = minimatch('releases/beta/3-alpha', '!releases/**/*-alpha')
  const tagsMatch = minimatch('v1.9.1', 'v1.*')
  const pathsMatch = match([
    '.github/actions/package.json',
    '.github/actions/index.ts',
    '.github/actions/src/utils.ts',
    '.github/renovate.json5',
  ], '.github/**/*.ts')
  console.log('branchesMatch:', branchesMatch)
  console.log('branchesMatchNegative:', branchesMatchNegative)
  console.log('tagsMatch:', tagsMatch)
  console.log('pathsMatch:', pathsMatch)
}

async function globMatchDemo() {
  const patterns = ['**/*.ts', '**/*.yml'].map(p => `src/${p}`)
  const globber = await glob.create(patterns.join('\n'))
  const matchFiles = await globber.glob()
  console.log('files', matchFiles)
  const searchPaths = globber.getSearchPaths()
  console.log('searchPaths', searchPaths)
}

async function ctxDemo() {
  const workflowRun = github.event.workflow_run as WorkflowRunCompletedEvent
  console.log(
    workflowRun.workflow.name,
    workflowRun.workflow_run.id,
  )
  const workflow = github.workflow
  if (workflow === 'Issue Labeled') {
    const event = github.event.issues as IssuesLabeledEvent
    console.log(
      event.label?.name,
      event.issue.number,
      event.issue.user.login,
    )
  }
  if (workflow === 'PR Checks') {
    const issueComment = github.event_name === 'issue_comment'
    if (issueComment) {
      const event = github.event.issue_comment as IssueCommentEvent
      console.log(
        event.comment.body,
        event.issue.pull_request,
        event.comment.author_association,
      )
    }
  }
  if (workflow === 'PR Limit') {
    const event = github.event.pull_request as PullRequestOpenedEvent
    console.log(
      event.pull_request.head.repo.full_name,
      github.repository,
      event.pull_request.user.login,
    )
  }
  if (workflow === 'Fix Renovate') {
    const event = github.event.issue_comment as IssueCommentEvent
    console.log(
      event.comment.body,
      event.issue.pull_request,
      event.comment.author_association,
      event.issue.pull_request?.html_url,
      event.issue.number,
    )
    const { data: pr } = await octokit().rest.pulls.get({
      owner: context.repo.owner,
      repo: context.repo.repo,
      pull_number: context.issue.number,
    })
    console.log({
      num: context.issue.number,
      branchName: pr.head.ref,
      commit: pr.head.sha,
      committer: pr.head.user.login,
      repo: pr.head.repo.full_name,
    })
    console.log(
      pr.user.login,
    )
  }
}

export const fns = [
  patternMatchDemo,
  globMatchDemo,
  ctxDemo,
]
