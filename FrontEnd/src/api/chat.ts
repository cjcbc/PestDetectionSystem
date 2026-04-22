import request from './request'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type {
  ChatMessage,
  ChatQuota,
  ChatReply,
  ChatSession,
  CreateSessionPayload,
  SendMessagePayload
} from '@/types/chat'

let isStreamHandling401 = false

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api'

export function createSession(payload: CreateSessionPayload): Promise<ChatSession> {
  return request.post('/chat/session', payload)
}

export function getSessions(): Promise<ChatSession[]> {
  return request.get('/chat/sessions')
}

export function deleteSession(sessionId: string): Promise<string> {
  return request.delete(`/chat/session/${sessionId}`)
}

export function getMessages(sessionId: string): Promise<ChatMessage[]> {
  return request.get(`/chat/session/${sessionId}/messages`)
}

export function sendMessage(payload: SendMessagePayload): Promise<ChatReply> {
  return request.post('/chat/send', payload)
}

export function getQuota(): Promise<ChatQuota> {
  return request.get('/chat/quota')
}

/**
 * 流式发送消息 - 使用 fetch + SSE
 */
export async function sendMessageStream(
  payload: SendMessagePayload,
  onDelta: (text: string) => void,
  onDone: () => void,
  onError: (err: string) => void
): Promise<void> {
  const token = localStorage.getItem('token')

  const response = await fetch(`${API_BASE_URL}/chat/send/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload)
  })

  if (!response.ok) {
    if (response.status === 401) {
      if (!isStreamHandling401) {
        isStreamHandling401 = true
        const appStore = useAppStore()
        appStore.logout()
        ElMessage.error('登录过期')
        router.push({ name: 'Home' })
        setTimeout(() => { isStreamHandling401 = false }, 3000)
      }
      return
    }
    const text = await response.text()
    onError(`请求失败: ${response.status} ${text}`)
    return
  }

  const reader = response.body?.getReader()
  if (!reader) {
    onError('浏览器不支持流式读取')
    return
  }

  const decoder = new TextDecoder()
  let buffer = ''

  // SSE 解析状态
  let currentEvent = ''
  let currentDataLines: string[] = []

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    // 按行拆分 SSE
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      // SSE 规范：空行表示一个事件结束
      if (line.trim() === '') {
        if (currentDataLines.length > 0) {
          // 多个 data: 行合并时用 \n 连接（SSE 规范）
          const fullData = currentDataLines.join('\n')

          if (currentEvent === 'done') {
            onDone()
            return
          } else if (currentEvent === 'error') {
            onError(fullData)
            return
          } else {
            // delta 或默认事件
            if (fullData) {
              onDelta(fullData)
            }
          }
        }
        // 重置当前事件
        currentEvent = ''
        currentDataLines = []
        continue
      }

      if (line.startsWith('event:')) {
        currentEvent = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        // data: 后面的部分（保留原始内容，可能为空表示换行）
        currentDataLines.push(line.slice(5))
      }
    }
  }

  // 处理 buffer 中残留的最后一个事件
  if (currentDataLines.length > 0) {
    const fullData = currentDataLines.join('\n')
    if (currentEvent === 'done') {
      onDone()
      return
    }
    if (fullData) {
      onDelta(fullData)
    }
  }

  // 如果流结束但没有收到 done 事件
  onDone()
}
