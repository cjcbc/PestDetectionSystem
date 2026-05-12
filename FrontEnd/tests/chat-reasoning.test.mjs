import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import ts from 'typescript'

const sourcePath = new URL('../src/utils/chatReasoning.ts', import.meta.url)
const source = await readFile(sourcePath, 'utf8')
const compiled = ts.transpileModule(source, {
  compilerOptions: {
    module: ts.ModuleKind.ES2020,
    target: ts.ScriptTarget.ES2020,
    strict: true
  }
}).outputText

const moduleUrl = `data:text/javascript;base64,${Buffer.from(compiled).toString('base64')}`
const { splitReasoningContent } = await import(moduleUrl)

assert.deepEqual(
  splitReasoningContent('<think>先分析叶片斑点</think>\n\n这是回答', null),
  {
    reasoning: '先分析叶片斑点',
    content: '这是回答'
  }
)

assert.deepEqual(
  splitReasoningContent('<think>旧推理</think>\n\n这是回答', '数据库推理'),
  {
    reasoning: '数据库推理',
    content: '这是回答'
  }
)

assert.deepEqual(
  splitReasoningContent('普通回答', null),
  {
    reasoning: '',
    content: '普通回答'
  }
)

assert.deepEqual(
  splitReasoningContent('', '仅推理内容'),
  {
    reasoning: '仅推理内容',
    content: ''
  }
)

assert.deepEqual(
  splitReasoningContent('推理过程\n\n正式回答', '推理过程'),
  {
    reasoning: '推理过程',
    content: '正式回答'
  }
)

console.log('chat reasoning compatibility checks passed')
