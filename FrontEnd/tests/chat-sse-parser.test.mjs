import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { pathToFileURL } from 'node:url'
import ts from 'typescript'

const sourcePath = new URL('../src/api/chat-sse.ts', import.meta.url)
const source = await readFile(sourcePath, 'utf8')
const compiled = ts.transpileModule(source, {
  compilerOptions: {
    module: ts.ModuleKind.ES2020,
    target: ts.ScriptTarget.ES2020,
    strict: true
  }
}).outputText

const moduleUrl = `data:text/javascript;base64,${Buffer.from(compiled).toString('base64')}`
const { dispatchSseEvent } = await import(moduleUrl)

const calls = []
const result = dispatchSseEvent('reasoning', '分析叶片斑点', {
  onDelta: (text) => calls.push(['delta', text]),
  onReasoning: (text) => calls.push(['reasoning', text]),
  onDone: () => calls.push(['done']),
  onError: (err) => calls.push(['error', err])
})

assert.equal(result, 'continue')
assert.deepEqual(calls, [['reasoning', '分析叶片斑点']])

console.log('chat SSE parser checks passed')
