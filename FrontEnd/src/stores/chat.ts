import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createSession,
  deleteSession,
  getMessages,
  getQuota,
  getSessions,
  sendMessageStream
} from '@/api/chat'
import type {
  ChatMessage,
  ChatQuota,
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
  /** 流式输出中的中间状态 — 不触发 messages 响应式更新 */
  const streamingContent = ref('')
  const isStreaming = ref(false)
  /** 流式期间用户消息的临时 ID */
  const streamingUserMsgId = ref<string | null>(null)

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

    // 添加用户消息（一次性修改数组）
    const tempUserMessage: Partial<ChatMessage> & { id: string; role: 'user'; content: string; createdTime: number } = {
      id: `temp-user-${Date.now()}`,
      role: 'user',
      content,
      createdTime: Date.now()
    }
    messages.value = [...messages.value, tempUserMessage as ChatMessage]

    // 记录流式消息 ID & 清空流式缓冲
    streamingUserMsgId.value = tempUserMessage.id
    streamingContent.value = ''
    isSending.value = true
    isStreaming.value = true

    try {
      await sendMessageStream(
        {
          sessionId: currentSessionId.value,
          message: content,
          detectionId
        },
        // onDelta: 仅更新 streamingContent，不修改 messages
        (delta: string) => {
          streamingContent.value += delta
        },
        // onDone: 流结束 — 用真实数据替换临时消息
        async () => {
          isStreaming.value = false
          isSending.value = false
          streamingUserMsgId.value = null
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
          streamingUserMsgId.value = null
          // 移除临时用户消息
          messages.value = messages.value.filter(
            (m) => m.id !== tempUserMessage.id
          )
          throw new Error(err)
        }
      )
    } catch {
      isStreaming.value = false
      isSending.value = false
      streamingUserMsgId.value = null
      messages.value = messages.value.filter(
        (m) => m.id !== tempUserMessage.id
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
    streamingUserMsgId,
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
