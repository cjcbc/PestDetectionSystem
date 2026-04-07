import request from './request'
import type {
  ChatMessage,
  ChatQuota,
  ChatReply,
  ChatSession,
  CreateSessionPayload,
  SendMessagePayload
} from '@/types/chat'

export function createSession(payload: CreateSessionPayload): Promise<ChatSession> {
  return request.post('/chat/session', payload)
}

export function getSessions(): Promise<ChatSession[]> {
  return request.get('/chat/sessions')
}

export function deleteSession(sessionId: number): Promise<string> {
  return request.delete(`/chat/session/${sessionId}`)
}

export function getMessages(sessionId: number): Promise<ChatMessage[]> {
  return request.get(`/chat/session/${sessionId}/messages`)
}

export function sendMessage(payload: SendMessagePayload): Promise<ChatReply> {
  return request.post('/chat/send', payload)
}

export function getQuota(): Promise<ChatQuota> {
  return request.get('/chat/quota')
}
