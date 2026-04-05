# 阶段三：AI 对话系统

## 🎯 阶段目标
实现多会话管理、消息对话、历史查询、额度显示等完整的 AI 咨询功能。

**依赖**：阶段一 | **工作量**：3-4 天 | **优先级**：P1

---

## 📋 核心需求

### 1. 对话 API 接口

**文件**：`src/api/chat.ts`

```typescript
// ===== 会话相关 =====

export interface ChatSession {
  id: number;
  title: string;
  scene: string;
  messageCount: number;
  lastMessageAt: number;
  createdTime: number;
  updatedTime: number;
}

export interface CreateSessionPayload {
  title?: string;      // 默认 "新对话"
  scene?: string;      // 默认 "expert"
}

export function createSession(payload: CreateSessionPayload): Promise<ChatSession>

export function getSessions(): Promise<ChatSession[]>

export function deleteSession(sessionId: number): Promise<{ code: 200; message: string }>

// ===== 消息相关 =====

export interface ChatMessage {
  id: number;
  sessionId: number;
  role: 'user' | 'assistant';
  content: string;
  model: string | null;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  createdTime: number;
}

export function getMessages(sessionId: number): Promise<ChatMessage[]>

export interface SendMessagePayload {
  sessionId: number;
  message: string;
  detectionId?: number;
}

export interface ChatReply {
  sessionId: number;
  answer: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
}

export function sendMessage(payload: SendMessagePayload): Promise<ChatReply>

// ===== 额度相关 =====

export interface ChatQuota {
  quotaDate: string;        // "2026-04-05"
  requestCount: number;     // 今日请求数
  inputTokens: number;
  outputTokens: number;
}

export function getQuota(): Promise<ChatQuota>
```

**后端 API 规范**：
```
POST /api/chat/session
GET /api/chat/sessions
DELETE /api/chat/session/{sessionId}
GET /api/chat/session/{sessionId}/messages
POST /api/chat/send
GET /api/chat/quota
```

---

### 2. Chat 首页（会话列表）

**文件**：`src/views/Chat.vue`

**功能需求**：

#### 左侧会话列表面板
- 顶部"新建会话"按钮
- 会话列表（最新会话在上）
- 每个会话显示：
  - 标题（截断超长文本）
  - 最后消息时间（今天显示 HH:MM，其他日期显示日期）
- 当前会话高亮
- 鼠标悬停时显示删除按钮 or 右键菜单

#### 右侧内容区（无会话时）
- 欢迎标题："欢迎来到农业 AI 顾问"
- 快速开始卡片，包含示例问题如：
  - "水稻常见病虫害有哪些？"
  - "如何防治稻瘟病？"
  - "有机肥和化肥的区别是什么？"
- 点击示例问题自动创建新会话并进入聊天

#### 创建会话对话框
- 标题输入框（可选，默认为空）
- 场景选择下拉框（可选，默认 "expert"）
- 取消和创建按钮

---

### 3. ChatDetail 聊天页面

**文件**：`src/views/ChatDetail.vue`

**功能需求**：

#### 消息列表
- 用户消息在左侧，背景色 ID（浅灰或浅绿）
- AI 消息在右侧，背景色不同
- 消息时间戳显示
- AI 消息支持 Markdown 渲染：
  - 加粗、斜体、代码块、列表、表格
  - 代码块带语言高亮
- AI 消息支持复制、重新生成（可选）
- 加载动画：AI 回复中时显示 "AI 在思考..."
- 长消息列表：支持虚拟滚动（可选，用 vue-virtual-scroller）

#### 额度显示
- 显示当日额度统计
- 格式：`请求 15/50 | 输入 Token 3200 | 输出 Token 1800`
- 超过限制时提示（如 "已达到每日请求上限"）

#### 消息输入框
- 多行输入（textarea）
- Shift+Enter 换行，Enter 发送
- 发送按钮
- 发送前校验：消息不能为空，控制最大长度（如 2000 字）
- 发送中禁用输入框和发送按钮，显示 "停止" 按钮（可选）

#### 页面顶部操作栏
- 返回按钮（返回会话列表）
- 会话标题展示
- 删除会话按钮（确认对话框）

---

### 4. Pinia 对话状态管理

**文件**：`src/stores/chat.ts`

```typescript
export const useChatStore = defineStore('chat', () => {
  // State
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<number | null>(null)
  const messages = ref<ChatMessage[]>([])
  const quota = ref<ChatQuota | null>(null)
  const isLoading = ref(false)

  // Computed
  const currentSession = computed(() => {
    return sessions.value.find(s => s.id === currentSessionId.value)
  })

  // Actions
  async function fetchSessions() {
    const data = await getSessions()
    sessions.value = data
  }

  async function createNewSession(payload: CreateSessionPayload) {
    const session = await createSession(payload)
    sessions.value.unshift(session)
    return session
  }

  async function loadMessages(sessionId: number) {
    currentSessionId.value = sessionId
    const data = await getMessages(sessionId)
    messages.value = data
  }

  async function sendMsg(content: string) {
    if (!currentSessionId.value) return
    
    isLoading.value = true
    try {
      const reply = await sendMessage({
        sessionId: currentSessionId.value,
        message: content
      })
      
      // 刷新消息和会话
      await loadMessages(currentSessionId.value)
      await fetchQuota()
    } finally {
      isLoading.value = false
    }
  }

  async function fetchQuota() {
    quota.value = await getQuota()
  }

  async function removeSession(sessionId: number) {
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
    }
  }

  return {
    sessions, currentSessionId, messages, quota, isLoading,
    currentSession,
    fetchSessions, createNewSession, loadMessages, sendMsg, fetchQuota, removeSession
  }
})
```

---

### 5. Markdown 渲染组件

**文件**：`src/components/MarkdownRenderer.vue`

**需要实现**：
- 使用 `markdown-it` 库解析 Markdown
- 使用 `highlight.js` 高亮代码块
- 支持 HTML 渲染（注意安全性）
- Props: `content: string`

**示意**：
```vue
<template>
  <div class="markdown-content" v-html="renderedHtml" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const props = defineProps<{ content: string }>()

const md = new MarkdownIt({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  }
})

const renderedHtml = computed(() => md.render(props.content))
</script>
```

---

### 6. 时间格式化工具

**文件**：`src/utils/format.ts`

```typescript
import dayjs from 'dayjs'

/**
 * 格式化消息时间戳
 * 今天显示时间，其他日期显示日期
 */
export function formatMessageTime(timestamp: number): string {
  const date = dayjs(timestamp)
  const today = dayjs()
  if (date.format('YYYY-MM-DD') === today.format('YYYY-MM-DD')) {
    return date.format('HH:mm')
  }
  return date.format('MM-DD HH:mm')
}

/**
 * 格式化会话时间
 */
export function formatSessionTime(timestamp: number): string {
  const date = dayjs(timestamp)
  const today = dayjs()
  if (date.format('YYYY-MM-DD') === today.format('YYYY-MM-DD')) {
    return date.format('HH:mm')
  }
  return date.format('MM-DD')
}
```

---

### 7. 样式和交互

**需要实现的样式**：
- 消息气泡：user 左浅灰 / assistant 右浅绿
- 消息时间：小号灰色，居中或靠边
- Markdown 内容：支持代码块、列表、表格样式
- 输入框：占满宽度，支持自动高度调整
- 额度条：进度条或纯文本总结
- 加载动画：可用骨架屏或脉冲效果

---

## 🏗️ 项目结构

```
src/
├── api/
│   └── chat.ts                     ✅ 对话 API 模块
├── views/
│   ├── Chat.vue                    ✅ 会话列表页
│   └── ChatDetail.vue              ✅ 聊天详情页
├── components/
│   ├── MarkdownRenderer.vue        ✅ Markdown 渲染
│   └── ChatMessageItem.vue         ✅ 消息气泡组件（可选）
├── stores/
│   └── chat.ts                     ✅ 对话状态
├── utils/
│   └── format.ts                   ✅ 格式化工具
├── types/
│   └── chat.ts                     ✅ 对话类型定义
└── styles/
    └── chat.css                    ✅ 对话样式
```

---

## 📝 验收标准

- [ ] Chat 首页可访问，会话列表加载正确
- [ ] 新建会话对话框可用
- [ ] 创建会话成功并进入聊天页
- [ ] 点击会话进入对应的聊天页面
- [ ] 消息列表显示正确（user/AI 区分）
- [ ] 消息时间戳格式正确
- [ ] AI 消息的 Markdown 渲染正确（加粗、代码块等）
- [ ] 消息输入框可用，Shift+Enter 换行正常
- [ ] 发送消息成功，新消息出现在列表
- [ ] 加载中显示"AI 在思考..."
- [ ] 额度显示准确（请求数/50、Token 数）
- [ ] 删除会话成功，列表更新
- [ ] 页面响应式良好

---

## ⚠️ 常见陷阱

1. **消息去重**：避免重复显示同一条消息
2. **消息顺序**：确保消息按时间倒序显示
3. **滚动位置**：新消息到达时自动滚动到底部（需特殊处理）
4. **HTML 安全**：使用 `v-html` 时确保内容安全，避免 XSS（可用 DOMPurify）
5. **Token 限制**：显示当日 Token 消费上限提示

---

## 🤝 与其他阶段接口

- Detection 页面可集成"请教 AI"按钮，传递 detectionId 到对话
- Home 页面可显示最近会话的快速链接
