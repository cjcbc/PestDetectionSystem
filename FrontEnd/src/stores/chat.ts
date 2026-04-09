import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createSession,
  deleteSession,
  getMessages,
  getQuota,
  getSessions,
  sendMessage,
  sendMessageStream
} from '@/api/chat'
import type {
  ChatMessage,
  ChatQuota,
  ChatReply,
  ChatSession,
  CreateSessionPayload
} from '@/types/chat'

function dedupeMessages(messages: ChatMessage[]) {
  const seen = new Set<string>()
  return messages.filter((message) => {
    if (seen.has(message.id)) {
      return false
    }
    seen.add(message.id)
    return true
  })
}

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<string | null>(null)
  const messages = ref<ChatMessage[]>([])
  const quota = ref<ChatQuota | null>(null)
  const isSessionsLoading = ref(false)
  const isMessagesLoading = ref(false)
  const isSending = ref(false)
  /** 流式输出中的中间状态 */
  const streamingContent = ref('')
  const isStreaming = ref(false)

  const currentSession = computed(() =>
    sessions.value.find((session) => session.id === currentSessionId.value) ?? null
  )

  async function fetchSessions() {
    isSessionsLoading.value = true
    try {
      const data = await getSessions()
      sessions.value = [...data].sort((a, b) => b.lastMessageAt - a.lastMessageAt)
    } finally {
      isSessionsLoading.value = false
    }
  }

  async function fetchQuota() {
    quota.value = await getQuota()
  }

  async function createNewSession(payload: CreateSessionPayload) {
    const session = await createSession(payload)
    sessions.value = [session, ...sessions.value.filter((item) => item.id !== session.id)]
    currentSessionId.value = session.id
    messages.value = []
    return session
  }

  async function loadMessages(sessionId: string) {
    currentSessionId.value = sessionId
    isMessagesLoading.value = true
    try {
      const data = await getMessages(sessionId)
      messages.value = dedupeMessages(data).sort((a, b) => a.createdTime - b.createdTime)
    } finally {
      isMessagesLoading.value = false
    }
  }

  async function sendMsg(content: string, detectionId?: string) {
    if (!currentSessionId.value) {
      return
    }

    // 添加临时用户消息
    const tempUserMessage: ChatMessage = {
      id: `temp-user-${Date.now()}`,
      role: 'user',
      content,
      createdTime: Date.now()
    }
    messages.value = [...messages.value, tempUserMessage]

    // 添加临时 AI 消息占位（流式内容会实时写入）
    const tempAiMessage: ChatMessage = {
      id: `temp-ai-${Date.now()}`,
      role: 'assistant',
      content: '',
      createdTime: Date.now()
    }
    messages.value = [...messages.value, tempAiMessage]

    isSending.value = true
    isStreaming.value = true
    streamingContent.value = ''

    try {
      await sendMessageStream(
        {
          sessionId: currentSessionId.value,
          message: content,
          detectionId
        },
        // onDelta: 增量文本
        (delta: string) => {
          streamingContent.value += delta
          // 实时更新临时 AI 消息内容
          const idx = messages.value.findIndex((m) => m.id === tempAiMessage.id)
          if (idx !== -1) {
            messages.value[idx] = {
              ...messages.value[idx],
              content: streamingContent.value
            }
          }
        },
        // onDone: 流结束
        async () => {
          isStreaming.value = false
          isSending.value = false
          // 重新加载真实消息（含数据库 ID）
          await Promise.all([
            loadMessages(currentSessionId.value!),
            fetchSessions(),
            fetchQuota()
          ])
        },
        // onError
        (err: string) => {
          isStreaming.value = false
          isSending.value = false
          // 移除临时消息
          messages.value = messages.value.filter(
            (m) => m.id !== tempUserMessage.id && m.id !== tempAiMessage.id
          )
          throw new Error(err)
        }
      )
    } catch {
      isStreaming.value = false
      isSending.value = false
      messages.value = messages.value.filter(
        (m) => m.id !== tempUserMessage.id && m.id !== tempAiMessage.id
      )
      throw new Error('发送消息失败')
    }
  }

  async function removeSession(sessionId: string) {
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter((session) => session.id !== sessionId)

    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
    }
  }

  function clearCurrentSession() {
    currentSessionId.value = null
    messages.value = []
  }

  return {
    sessions,
    currentSessionId,
    messages,
    quota,
    isSessionsLoading,
    isMessagesLoading,
    isSending,
    isStreaming,
    streamingContent,
    currentSession,
    fetchSessions,
    fetchQuota,
    createNewSession,
    loadMessages,
    sendMsg,
    removeSession,
    clearCurrentSession
  }
})
