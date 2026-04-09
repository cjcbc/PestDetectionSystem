export type ChatRole = 'user' | 'assistant'

export interface ChatSession {
  id: string
  title: string
  scene: string
  messageCount: number
  lastMessageAt: number
  createdTime: number
  updatedTime: number
}

export interface CreateSessionPayload {
  title?: string
  scene?: string
}

export interface ChatMessage {
  id: string
  sessionId: string
  detectionId?: string | null
  role: ChatRole
  content: string
  model: string | null
  promptTokens: number
  completionTokens: number
  totalTokens: number
  createdTime: number
}

export interface SendMessagePayload {
  sessionId: string
  message: string
  detectionId?: string
}

export interface ChatReply {
  sessionId: string
  answer: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

export interface ChatQuota {
  quotaDate: string
  requestCount: number
  inputTokens: number
  outputTokens: number
}
