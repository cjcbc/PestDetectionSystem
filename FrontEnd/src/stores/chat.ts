import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createSession,
  deleteSession,
  getMessages,
  getQuota,
  getSessions,
  sendMessage
} from '@/api/chat'
import type {
  ChatMessage,
  ChatQuota,
  ChatSession,
  CreateSessionPayload
} from '@/types/chat'

function dedupeMessages(messages: ChatMessage[]) {
  const seen = new Set<number>()
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
  const currentSessionId = ref<number | null>(null)
  const messages = ref<ChatMessage[]>([])
  const quota = ref<ChatQuota | null>(null)
  const isSessionsLoading = ref(false)
  const isMessagesLoading = ref(false)
  const isSending = ref(false)

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

  async function loadMessages(sessionId: number) {
    currentSessionId.value = sessionId
    isMessagesLoading.value = true
    try {
      const data = await getMessages(sessionId)
      messages.value = dedupeMessages(data).sort((a, b) => a.createdTime - b.createdTime)
    } finally {
      isMessagesLoading.value = false
    }
  }

  async function sendMsg(content: string, detectionId?: number) {
    if (!currentSessionId.value) {
      return
    }

    isSending.value = true
    try {
      await sendMessage({
        sessionId: currentSessionId.value,
        message: content,
        detectionId
      })

      await Promise.all([
        loadMessages(currentSessionId.value),
        fetchSessions(),
        fetchQuota()
      ])
    } finally {
      isSending.value = false
    }
  }

  async function removeSession(sessionId: number) {
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
