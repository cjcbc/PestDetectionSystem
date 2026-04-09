<template>
  <div class="markdown-renderer">
    <!-- 流式思考中：展开显示浅色文字 -->
    <div v-if="isThinking" class="think-streaming">
      <div class="think-streaming__header">
        <svg class="think-streaming__icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
        <span>思考中...</span>
        <span class="think-streaming__dots">
          <span></span><span></span><span></span>
        </span>
      </div>
      <div class="think-streaming__body" v-html="thinkHtml" />
    </div>

    <!-- 思考完成后折叠 -->
    <details v-else-if="thinkContent" class="think-collapse">
      <summary class="think-collapse__summary">
        <svg class="think-collapse__icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
        <span>已思考完成 · 点击查看</span>
        <svg class="think-collapse__arrow" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
      </summary>
      <div class="think-collapse__body" v-html="thinkHtml" />
    </details>

    <!-- 正文内容 -->
    <div v-if="mainContent" class="markdown-content" v-html="mainHtml" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import bash from 'highlight.js/lib/languages/bash'
import css from 'highlight.js/lib/languages/css'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import markdown from 'highlight.js/lib/languages/markdown'
import plaintext from 'highlight.js/lib/languages/plaintext'
import python from 'highlight.js/lib/languages/python'
import sql from 'highlight.js/lib/languages/sql'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import 'highlight.js/styles/github.css'

const props = withDefaults(defineProps<{
  content: string
  isStreaming?: boolean
}>(), {
  isStreaming: false
})

hljs.registerLanguage('bash', bash)
hljs.registerLanguage('css', css)
hljs.registerLanguage('java', java)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('json', json)
hljs.registerLanguage('markdown', markdown)
hljs.registerLanguage('md', markdown)
hljs.registerLanguage('plaintext', plaintext)
hljs.registerLanguage('text', plaintext)
hljs.registerLanguage('python', python)
hljs.registerLanguage('py', python)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)

const md = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true,
  highlight(code: string, language: string) {
    const validLanguage = language && hljs.getLanguage(language) ? language : undefined
    const highlighted = validLanguage
      ? hljs.highlight(code, { language: validLanguage }).value
      : hljs.highlightAuto(code).value

    return `<pre class="hljs"><code>${highlighted}</code></pre>`
  }
})

/**
 * 分离 <think>...</think> 和正文
 * 支持流式场景：<think> 可能还未闭合
 */
function parseThinkBlock(raw: string, streaming: boolean): { think: string; main: string; thinkDone: boolean } {
  // 完整闭合的 think 块
  const closedRegex = /^<think>([\s\S]*?)<\/think>\s*/
  const closedMatch = raw.match(closedRegex)
  if (closedMatch) {
    return {
      think: closedMatch[1].trim(),
      main: raw.slice(closedMatch[0].length).trim(),
      thinkDone: true
    }
  }

  // 流式场景：还在 think 中（有 <think> 但没有 </think>）
  const openRegex = /^<think>([\s\S]*)$/
  const openMatch = raw.match(openRegex)
  if (openMatch && streaming) {
    return {
      think: openMatch[1].trim(),
      main: '',
      thinkDone: false
    }
  }

  return { think: '', main: raw, thinkDone: true }
}

const parsed = computed(() => parseThinkBlock(props.content || '', props.isStreaming))

const thinkContent = computed(() => parsed.value.think)
const mainContent = computed(() => parsed.value.main)
const isThinking = computed(() => props.isStreaming && thinkContent.value && !parsed.value.thinkDone)

const thinkHtml = computed(() => {
  if (!thinkContent.value) return ''
  return DOMPurify.sanitize(md.render(thinkContent.value))
})

const mainHtml = computed(() => {
  if (!mainContent.value) return ''
  return DOMPurify.sanitize(md.render(mainContent.value))
})
</script>

<style scoped>
.markdown-renderer {
  width: 100%;
}

/* ===== 流式思考中状态 ===== */
.think-streaming {
  margin-bottom: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  overflow: hidden;
  background: #fafbfc;
}

.think-streaming__header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #10a37f;
  border-bottom: 1px solid #ebebeb;
}

.think-streaming__icon {
  flex-shrink: 0;
  color: #10a37f;
}

.think-streaming__dots {
  display: inline-flex;
  gap: 3px;
  margin-left: 2px;
}

.think-streaming__dots span {
  width: 4px;
  height: 4px;
  background: #10a37f;
  border-radius: 50%;
  animation: think-dot-pulse 1.4s infinite ease-in-out both;
}

.think-streaming__dots span:nth-child(1) { animation-delay: -0.32s; }
.think-streaming__dots span:nth-child(2) { animation-delay: -0.16s; }
.think-streaming__dots span:nth-child(3) { animation-delay: 0s; }

@keyframes think-dot-pulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.think-streaming__body {
  padding: 8px 14px 12px;
  font-size: 13px;
  line-height: 1.65;
  color: #aaa;
  max-height: 200px;
  overflow-y: auto;
}

.think-streaming__body :deep(p) {
  margin: 4px 0;
}

/* ===== 思考完成折叠 ===== */
.think-collapse {
  margin-bottom: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  overflow: hidden;
  background: #fafafa;
}

.think-collapse[open] {
  background: #f5f5f5;
}

.think-collapse__summary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #888;
  cursor: pointer;
  user-select: none;
  list-style: none;
}

.think-collapse__summary::-webkit-details-marker {
  display: none;
}

.think-collapse__summary::marker {
  display: none;
  content: '';
}

.think-collapse__icon {
  flex-shrink: 0;
  color: #aaa;
}

.think-collapse__arrow {
  margin-left: auto;
  flex-shrink: 0;
  transition: transform 0.2s;
}

.think-collapse[open] .think-collapse__arrow {
  transform: rotate(180deg);
}

.think-collapse__summary:hover {
  color: #666;
}

.think-collapse__body {
  padding: 4px 14px 12px;
  font-size: 13px;
  line-height: 1.65;
  color: #777;
  border-top: 1px solid #ebebeb;
}

.think-collapse__body :deep(p) {
  margin: 4px 0;
}

.think-collapse__body :deep(ul),
.think-collapse__body :deep(ol) {
  padding-left: 18px;
  margin: 4px 0;
}
</style>
