<template>
  <div class="chatgpt-page">
    <!-- 未登录状态提示 -->
    <div v-if="!userIsLoggedIn" class="auth-required-overlay">
      <el-empty description="AI 对话功能需要登录">
        <el-button type="primary" @click="goToLogin">立即登录</el-button>
      </el-empty>
    </div>

    <!-- 已登录的完整功能 -->
    <template v-else>
      <!-- 固定顶栏 -->
      <header class="chatgpt-topbar">
        <button class="chatgpt-back-btn" @click="router.push('/chat')" title="返回会话列表">
          <el-icon :size="18"><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>
        <div class="chatgpt-topbar__center">
          <h2 class="chatgpt-topbar__title">{{ sessionTitle }}</h2>
          <span class="chatgpt-topbar__sub">{{ detectionLabel ? `已关联：${detectionLabel}` : '农业专家场景' }}</span>
        </div>
        <div class="chatgpt-topbar__right">
          <span class="chatgpt-quota" :class="{ 'is-danger': isQuotaExceeded }">{{ quotaText }}</span>
          <el-popconfirm
            title="删除当前会话？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDeleteCurrentSession"
          >
            <template #reference>
              <el-button type="danger" text size="small" @click.stop>
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </header>

      <!-- 消息区域 -->
      <div ref="messageContainer" v-loading="chatStore.isMessagesLoading" class="chatgpt-messages">
        <div class="chatgpt-messages__inner">
          <div v-if="!chatStore.messages.length && !chatStore.isMessagesLoading" class="chatgpt-empty">
            <div class="chatgpt-empty__icon">🌾</div>
            <h3>开始你的农业咨询</h3>
            <p>向 AI 专家提问，获取病虫害识别与田间管理建议</p>
          </div>

          <template v-for="message in chatStore.messages" :key="message.id">
            <!-- 用户消息 -->
            <div v-if="message.role === 'user'" class="chatgpt-msg chatgpt-msg--user">
              <div class="chatgpt-msg__row">
                <div class="chatgpt-msg__content">
                  <p class="chatgpt-msg__text">{{ message.content }}</p>
                </div>
                <div class="chatgpt-msg__avatar chatgpt-msg__avatar--user">
                  <el-icon :size="18"><User /></el-icon>
                </div>
              </div>
              <p class="chatgpt-msg__time chatgpt-msg__time--right">{{ formatMessageTime(message.createdTime) }}</p>
            </div>

            <!-- AI 消息 -->
            <div v-else class="chatgpt-msg chatgpt-msg--assistant">
              <div class="chatgpt-msg__row">
                <div class="chatgpt-msg__avatar chatgpt-msg__avatar--ai">
                  <span>AI</span>
                </div>
                <div class="chatgpt-msg__content">
                  <MarkdownRenderer
                    :content="message.content"
                    :is-streaming="false"
                  />
                </div>
              </div>
              <p v-if="!message.id.startsWith('temp-')" class="chatgpt-msg__time">{{ formatMessageTime(message.createdTime) }}</p>
            </div>
          </template>

          <!-- 流式 AI 消息（独立渲染，不走 messages 数组） -->
          <div v-if="chatStore.isStreaming && chatStore.streamingContent" class="chatgpt-msg chatgpt-msg--assistant">
            <div class="chatgpt-msg__row">
              <div class="chatgpt-msg__avatar chatgpt-msg__avatar--ai">
                <span>AI</span>
              </div>
              <div class="chatgpt-msg__content">
                <MarkdownRenderer
                  :content="chatStore.streamingContent"
                  :is-streaming="true"
                />
              </div>
            </div>
          </div>

          <!-- 思考中占位（流式开始但尚无内容） -->
          <div v-else-if="chatStore.isStreaming && !chatStore.streamingContent" class="chatgpt-msg chatgpt-msg--assistant">
            <div class="chatgpt-msg__row">
              <div class="chatgpt-msg__avatar chatgpt-msg__avatar--ai">
                <span>AI</span>
              </div>
              <div class="chatgpt-msg__content">
                <span class="chatgpt-thinking-inline">
                  <span class="chatgpt-thinking__dot"></span>
                  <span class="chatgpt-thinking__dot"></span>
                  <span class="chatgpt-thinking__dot"></span>
                </span>
              </div>
            </div>
          </div>

          <!-- 底部锚点 -->
          <div ref="messagesEnd"></div>
        </div>
      </div>

      <!-- 固定底部输入框 -->
      <div class="chatgpt-input-area">
        <div class="chatgpt-input-area__inner">
          <div class="chatgpt-input-box">
            <el-input
              v-model="draft"
              class="chatgpt-input-box__textarea"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 6 }"
              :maxlength="2000"
              resize="none"
              placeholder="输入消息... (Enter 发送，Shift+Enter 换行)"
              :disabled="chatStore.isSending"
              @keydown="handleKeydown"
            />
            <button
              class="chatgpt-send-btn"
              :class="{ 'is-active': draft.trim().length > 0 }"
              :disabled="chatStore.isSending || !draft.trim()"
              @click="handleSend"
            >
              <el-icon :size="20"><Promotion /></el-icon>
            </button>
          </div>
          <p class="chatgpt-input-hint">AI 可能产生不准确的信息，建议在实际操作前进行验证。</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete, User, Promotion } from '@element-plus/icons-vue'
import { isLoggedIn } from '@/utils/auth'
import { useChatStore } from '@/stores/chat'
import { isMessageHandled } from '@/api/request'
import { formatMessageTime } from '@/utils/format'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()

const userIsLoggedIn = computed(() => isLoggedIn())

const draft = ref('')
const messageContainer = ref<HTMLElement | null>(null)
const messagesEnd = ref<HTMLElement | null>(null)

const sessionId = computed(() => String(route.params.sessionId || ''))
const detectionLabel = computed(() => String(route.query.detectionLabel || '').trim())
const detectionId = computed(() => {
  const raw = String(route.query.detectionId || '').trim()
  return raw && Number.isFinite(Number(raw)) ? raw : undefined
})

const sessionTitle = computed(() => chatStore.currentSession?.title || '会话详情')

const quotaText = computed(() => {
  const quota = chatStore.quota
  if (!quota) {
    return '正在加载额度统计...'
  }

  return `请求 ${quota.requestCount}/50 | 输入 Token ${quota.inputTokens} | 输出 Token ${quota.outputTokens}`
})

const isQuotaExceeded = computed(() => (chatStore.quota?.requestCount || 0) >= 50)

function scrollToBottom(smooth = false) {
  nextTick(() => {
    const anchor = messagesEnd.value
    if (anchor) {
      anchor.scrollIntoView({ behavior: smooth ? 'smooth' : 'instant', block: 'end' })
    } else {
      const container = messageContainer.value
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    }
  })
}

async function loadCurrentSession() {
  if (!sessionId.value || !/^\d+$/.test(sessionId.value)) {
    ElMessage.error('无效的会话 ID')
    router.push('/chat')
    return
  }

  try {
    await Promise.all([
      chatStore.fetchSessions(),
      chatStore.loadMessages(sessionId.value),
      chatStore.fetchQuota()
    ])
    scrollToBottom()
  } catch (error) {
    if (!isMessageHandled(error)) {
      ElMessage.error('加载会话失败')
    }
    router.push('/chat')
  }
}

async function handleSend() {
  const content = draft.value.trim()
  if (!content) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (content.length > 2000) {
    ElMessage.warning('消息长度不能超过 2000 字')
    return
  }

  try {
    const extraDetectionId = chatStore.messages.length === 0 ? detectionId.value : undefined
    draft.value = ''
    await chatStore.sendMsg(content, extraDetectionId)
    scrollToBottom()
  } catch {
    draft.value = content
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter' || event.shiftKey) {
    return
  }

  event.preventDefault()
  void handleSend()
}

async function handleDeleteCurrentSession() {
  try {
    await chatStore.removeSession(sessionId.value)
    ElMessage.success('会话已删除')
    router.push('/chat')
  } catch (error) {
    if (!isMessageHandled(error)) {
      ElMessage.error('删除会话失败')
    }
  }
}

watch(
  () => route.params.sessionId,
  () => {
    void loadCurrentSession()
  }
)

// Throttled scroll-to-bottom during streaming
let scrollTimer: ReturnType<typeof setTimeout> | 0 = 0
let lastScrollTime = 0
const SCROLL_THROTTLE_MS = 200

function throttledScrollToBottom() {
  const now = Date.now()
  const remaining = SCROLL_THROTTLE_MS - (now - lastScrollTime)
  if (remaining <= 0) {
    lastScrollTime = now
    scrollToBottom(true)
    return
  }
  if (!scrollTimer) {
    scrollTimer = window.setTimeout(() => {
      scrollTimer = 0
      lastScrollTime = Date.now()
      scrollToBottom(true)
    }, remaining)
  }
}

watch(
  () => chatStore.streamingContent,
  () => {
    throttledScrollToBottom()
  }
)

watch(
  () => chatStore.messages.length,
  () => {
    scrollToBottom()
  }
)

watch(
  () => chatStore.isSending,
  (val) => {
    if (!val) {
      nextTick(() => scrollToBottom(true))
    }
  }
)

function goToLogin() {
  router.push({ name: 'Login' })
}

onMounted(() => {
  draft.value = String(route.query.question || '').trim()
  void loadCurrentSession()
})
</script>

<style scoped>
.auth-required-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.05) 0%, rgba(64, 158, 255, 0.05) 100%);
  border-radius: 8px;
  margin: 20px;
  padding: 40px;
}

/* ===== 页面布局 ===== */
.chatgpt-page {
  display: flex;
  flex-direction: column;
  /* 减去顶部导航栏(78px) + shell-main padding(28px+40px) */
  height: calc(100vh - 146px);
  background: #f7f7f8;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
}

/* ===== 顶栏 ===== */
.chatgpt-topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.chatgpt-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  background: #fff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.chatgpt-back-btn:hover {
  background: #f5f5f5;
  border-color: #d0d0d0;
}

.chatgpt-topbar__center {
  flex: 1;
  min-width: 0;
  text-align: center;
}

.chatgpt-topbar__title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chatgpt-topbar__sub {
  font-size: 12px;
  color: #999;
}

.chatgpt-topbar__right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.chatgpt-quota {
  font-size: 12px;
  color: #888;
  white-space: nowrap;
}

.chatgpt-quota.is-danger {
  color: #ef4444;
  font-weight: 500;
}

/* ===== 消息区域 ===== */
.chatgpt-messages {
  flex: 1;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.chatgpt-messages__inner {
  max-width: 768px;
  margin: 0 auto;
  padding: 24px 16px 32px;
}

/* 空状态 */
.chatgpt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  text-align: center;
  color: #999;
}

.chatgpt-empty__icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.chatgpt-empty h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.chatgpt-empty p {
  margin: 0;
  font-size: 14px;
  color: #888;
}

/* ===== 消息气泡 ===== */
.chatgpt-msg {
  margin-bottom: 24px;
  contain: layout style;
}

.chatgpt-msg__row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.chatgpt-msg--user .chatgpt-msg__row {
  justify-content: flex-end;
}

.chatgpt-msg__avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 2px;
}

.chatgpt-msg__avatar--ai {
  background: linear-gradient(135deg, #10a37f, #1a7f64);
  color: #fff;
}

.chatgpt-msg__avatar--user {
  background: #1a1a1a;
  color: #fff;
}

.chatgpt-msg__content {
  max-width: calc(100% - 60px);
  line-height: 1.7;
  font-size: 15px;
  color: #1a1a1a;
  contain: layout style;
}

/* 用户消息样式 */
.chatgpt-msg--user .chatgpt-msg__content {
  background: #1a1a1a;
  color: #fff;
  padding: 10px 16px;
  border-radius: 18px 18px 4px 18px;
  max-width: 70%;
}

/* AI消息样式 */
.chatgpt-msg--assistant .chatgpt-msg__content {
  background: #fff;
  padding: 14px 18px;
  border-radius: 4px 18px 18px 18px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.chatgpt-msg__text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.chatgpt-msg__time {
  margin: 6px 0 0 44px;
  font-size: 11px;
  color: #bbb;
}

.chatgpt-msg__time--right {
  text-align: right;
  margin-right: 44px;
  margin-left: 0;
}

/* ===== 思考动画 ===== */
.chatgpt-thinking-inline {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 0;
}

.chatgpt-thinking {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 0;
}

.chatgpt-thinking__dot {
  width: 8px;
  height: 8px;
  background: #10a37f;
  border-radius: 50%;
  animation: chatgpt-bounce 1.4s infinite ease-in-out both;
}

.chatgpt-thinking__dot:nth-child(1) {
  animation-delay: -0.32s;
}

.chatgpt-thinking__dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes chatgpt-bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ===== 底部输入区域 ===== */
.chatgpt-input-area {
  position: sticky;
  bottom: 0;
  z-index: 20;
  padding: 16px 16px 20px;
  background: linear-gradient(to top, #f7f7f8 80%, transparent);
  flex-shrink: 0;
}

.chatgpt-input-area__inner {
  max-width: 768px;
  margin: 0 auto;
}

.chatgpt-input-box {
  display: flex;
  align-items: flex-end;
  gap: 0;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  padding: 8px 8px 8px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.chatgpt-input-box:focus-within {
  border-color: #10a37f;
  box-shadow: 0 2px 16px rgba(16, 163, 127, 0.12);
}

.chatgpt-input-box__textarea {
  flex: 1;
}

.chatgpt-input-box__textarea :deep(.el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
  padding: 6px 0;
  font-size: 15px;
  line-height: 1.5;
  resize: none;
  max-height: 200px;
}

.chatgpt-send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 10px;
  background: #d9d9d9;
  color: #fff;
  cursor: not-allowed;
  transition: all 0.2s;
  flex-shrink: 0;
}

.chatgpt-send-btn.is-active {
  background: #1a1a1a;
  cursor: pointer;
}

.chatgpt-send-btn.is-active:hover {
  background: #333;
}

.chatgpt-input-hint {
  margin: 8px 0 0;
  text-align: center;
  font-size: 12px;
  color: #bbb;
}

/* ===== Markdown 覆盖 ===== */
.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content) {
  color: #1a1a1a;
  line-height: 1.7;
}

.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content p) {
  margin: 8px 0;
}

.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content pre) {
  background: #1e1e1e;
  border-radius: 10px;
  padding: 14px 16px;
  margin: 12px 0;
}

.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content code) {
  font-size: 13px;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  color: #d63384;
}

.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content ul),
.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.chatgpt-msg--assistant .chatgpt-msg__content :deep(.markdown-content blockquote) {
  border-left: 3px solid #10a37f;
  margin: 8px 0;
  padding-left: 12px;
  color: #666;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .chatgpt-topbar {
    padding: 8px 12px;
  }

  .chatgpt-topbar__center {
    display: none;
  }

  .chatgpt-messages__inner {
    padding: 16px 8px 24px;
  }

  .chatgpt-msg--user .chatgpt-msg__content {
    max-width: 85%;
  }

  .chatgpt-input-area {
    padding: 12px 8px 16px;
  }

  .chatgpt-input-box {
    border-radius: 12px;
  }
}
</style>
