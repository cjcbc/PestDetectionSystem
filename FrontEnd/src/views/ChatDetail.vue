<template>
  <div class="chat-detail-page">
    <section class="chat-detail-panel">
      <header class="chat-detail-panel__header">
        <div style="display: flex; align-items: center; gap: 12px">
          <el-button text @click="router.push('/chat')">返回</el-button>
          <div class="chat-detail-panel__title">
            <h2>{{ sessionTitle }}</h2>
            <p>{{ detectionLabel ? `已关联识别结果：${detectionLabel}` : '农业专家场景' }}</p>
          </div>
        </div>

        <el-popconfirm
          title="删除当前会话？"
          confirm-button-text="删除"
          cancel-button-text="取消"
          @confirm="handleDeleteCurrentSession"
        >
          <template #reference>
            <el-button type="danger" plain>删除会话</el-button>
          </template>
        </el-popconfirm>
      </header>

      <div class="chat-quota" :class="{ 'is-danger': isQuotaExceeded }">
        <p class="chat-quota__text">
          {{ quotaText }}
        </p>
      </div>

      <div ref="messageContainer" v-loading="chatStore.isMessagesLoading" class="chat-messages">
        <div class="chat-messages__list">
          <el-empty v-if="!chatStore.messages.length && !chatStore.isMessagesLoading" description="先发一条消息，开始本次咨询" />

          <article
            v-for="message in chatStore.messages"
            :key="message.id"
            class="chat-message-item"
            :class="message.role === 'user' ? 'is-user' : 'is-assistant'"
          >
            <div class="chat-message-item__bubble">
              <div v-if="message.role === 'assistant'">
                <MarkdownRenderer :content="message.content" />
              </div>
              <p v-else class="chat-message-item__plain">{{ message.content }}</p>
              <p class="chat-message-item__time">{{ formatMessageTime(message.createdTime) }}</p>
            </div>
          </article>

          <article v-if="chatStore.isSending" class="chat-message-item is-assistant">
            <div class="chat-message-item__bubble">
              <p class="chat-thinking">AI 在思考...</p>
            </div>
          </article>
        </div>
      </div>

      <div class="chat-input">
        <div class="chat-input__box">
          <el-input
            v-model="draft"
            class="chat-input__textarea"
            type="textarea"
            :rows="4"
            :maxlength="2000"
            resize="none"
            placeholder="输入你的问题，Enter 发送，Shift+Enter 换行"
            :disabled="chatStore.isSending"
            @keydown="handleKeydown"
          />
          <el-button type="primary" size="large" :loading="chatStore.isSending" @click="handleSend">
            发送
          </el-button>
        </div>
        <p class="chat-input__hint">消息不能为空，最长 2000 字。发送后会自动刷新会话和额度统计。</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { formatMessageTime } from '@/utils/format'

const MarkdownRenderer = defineAsyncComponent(() => import('@/components/MarkdownRenderer.vue'))

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()

const draft = ref('')
const messageContainer = ref<HTMLElement | null>(null)

const sessionId = computed(() => Number(route.params.sessionId))
const detectionLabel = computed(() => String(route.query.detectionLabel || '').trim())
const detectionId = computed(() => {
  const raw = Number(route.query.detectionId)
  return Number.isFinite(raw) ? raw : undefined
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

function scrollToBottom() {
  nextTick(() => {
    const container = messageContainer.value
    if (!container) {
      return
    }

    const target = container.scrollHeight
    container.scrollTo({ top: target, behavior: 'auto' })
    requestAnimationFrame(() => {
      container.scrollTo({ top: container.scrollHeight, behavior: 'auto' })
    })
  })
}

async function loadCurrentSession() {
  if (!Number.isFinite(sessionId.value)) {
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
  } catch {
    ElMessage.error('加载会话失败')
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
  } catch {
    ElMessage.error('删除会话失败')
  }
}

watch(
  () => route.params.sessionId,
  () => {
    void loadCurrentSession()
  }
)

watch(
  () => [chatStore.messages.length, chatStore.isSending],
  async () => {
    await nextTick()
    scrollToBottom()
  },
  { deep: false }
)

onMounted(() => {
  draft.value = String(route.query.question || '').trim()
  void loadCurrentSession()
})
</script>
