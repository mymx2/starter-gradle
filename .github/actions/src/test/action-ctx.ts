import type {
  EnvContext,
  GithubContext,
  JobContext,
  RunnerContext,
  StepContext,
  StrategyContext,
} from '../../action-lang.d.ts'

export const githubCtx = {} as GithubContext
export const envCtx = {} as Record<string, string> & EnvContext
export const varsCtx = {} as Record<string, string>
export const jobCtx = {} as JobContext
export const jobsCtx = {} as Record<string, JobContext>
export const stepsCtx = {} as Record<string, StepContext>
export const runnerCtx = {} as RunnerContext
export const secretsCtx = {} as Record<string, string>
export const strategy = {} as StrategyContext
// eslint-disable-next-line ts/no-explicit-any
export const matrixCtx = {} as Record<string, any>
export const needsCtx = {} as Record<string, { outputs: Record<string, string>, result: string }>
export const inputsCtx = {} as Record<string, string | number | boolean>
