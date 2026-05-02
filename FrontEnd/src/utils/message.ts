import { ElMessage, type MessageHandler } from 'element-plus'

type MessageType = 'success' | 'warning' | 'info' | 'error'

const recentMessages = new Map<string, number>()
const DEFAULT_DEDUPE_TIME = 1500

export function showMessage(type: MessageType, message: string): MessageHandler | undefined {
  const now = Date.now()
  const key = `${type}:${message}`
  const lastShownAt = recentMessages.get(key) ?? 0

  if (now - lastShownAt < DEFAULT_DEDUPE_TIME) {
    return
  }

  recentMessages.set(key, now)
  return ElMessage({
    type,
    message
  })
}

export function showSuccess(message: string) {
  return showMessage('success', message)
}

export function showWarning(message: string) {
  return showMessage('warning', message)
}

export function showInfo(message: string) {
  return showMessage('info', message)
}

export function showError(message: string) {
  return showMessage('error', message)
}
