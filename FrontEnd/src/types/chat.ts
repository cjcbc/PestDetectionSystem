export type ChatRole = 'user' | 'assistant'

export interface ChatSession {
  id: number
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
  id: number
  sessionId: number
  role: ChatRole
  content: string
  model: string | null
  promptTokens: number
  completionTokens: number
  totalTokens: number
  createdTime: number
}

export interface SendMessagePayload {
  sessionId: number
  message: string
  detectionId?: number
}

export interface ChatReply {
  sessionId: number
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
