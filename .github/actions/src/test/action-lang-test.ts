import console from 'node:console'
import { data, Evaluator, Lexer, Parser } from '@actions/expressions'
import { contains } from '@actions/expressions/funcs/contains'
import { parseWorkflow } from '@actions/workflow-parser'

function actionExpressionTest() {
  const lexer = new Lexer('\'monalisa\' == context.name')
  const lr = lexer.lex()

  const parser = new Parser(lr.tokens, ['context'], [])
  const expr = parser.parse()

  const evaluator = new Evaluator(expr, new data.Dictionary([{
    key: 'context',
    value: new data.Dictionary([{
      key: 'name',
      value: new data.StringData('monalisa'),
    }]),
  }]))
  const result = evaluator.evaluate()

  console.log(result.coerceString()) // true
}

function expressionDemo() {
  //  contains: contains,
  //  endswith: endswith,
  //  format: format,
  //  fromjson: fromjson,
  //  join: join,
  //  startswith: startswith,
  //  tojson: tojson
  console.log(contains('Hello world', 'llo'))
  // join(github.event.issue.labels.*.name, ', ')
}

export function actionWorkflowParserTest() {
  const result = parseWorkflow(
    {
      name: 'test.yaml',
      content: `on: push
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    steps:
                      - run: echo 'hello'`,
    },
  )
  console.log(result.context)
  console.log(result.value)
}

export const fns = [
  actionExpressionTest,
  expressionDemo,
  actionWorkflowParserTest,
]
