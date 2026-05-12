export interface SseStreamCallbacks {
  onDelta: (text: string) => void
  onReasoning: (text: string) => void
  onDone: () => void
  onError: (err: string) => void
}

export type SseDispatchResult = 'continue' | 'stop'

export function dispatchSseEvent(
  currentEvent: string,
  fullData: string,
  callbacks: SseStreamCallbacks
): SseDispatchResult {
  if (currentEvent === 'done') {
    callbacks.onDone()
    return 'stop'
  }

  if (currentEvent === 'error') {
    callbacks.onError(fullData)
    return 'stop'
  }

  if (currentEvent === 'reasoning') {
    if (fullData) {
      callbacks.onReasoning(fullData)
    }
    return 'continue'
  }

  if (fullData) {
    callbacks.onDelta(fullData)
  }
  return 'continue'
}
