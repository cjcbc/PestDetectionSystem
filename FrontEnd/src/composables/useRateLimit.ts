import { computed, onBeforeUnmount, ref } from 'vue'

export const RATE_LIMIT_SECONDS = 60

export const RATE_LIMIT_KEYS = {
  verificationCode: '/user/verification-code',
  login: '/user/login',
  register: '/user/register',
  detect: '/detect',
  chatSend: '/chat/send',
  chatStream: '/chat/send/stream'
} as const

const limitedUntilByKey = ref<Record<string, number>>({})

export function normalizeRateLimitKey(url?: string): string {
  if (!url) return ''
  const path = url.split('?')[0]
  if (!path.startsWith('http')) return path

  return new URL(path).pathname.replace(/^\/api/, '')
}

export function setRateLimited(key: string, seconds = RATE_LIMIT_SECONDS) {
  if (!key || seconds <= 0) return

  limitedUntilByKey.value = {
    ...limitedUntilByKey.value,
    [normalizeRateLimitKey(key)]: Date.now() + seconds * 1000
  }
}

export function getRateLimitRemaining(key: string): number {
  const limitedUntil = limitedUntilByKey.value[normalizeRateLimitKey(key)] ?? 0
  return Math.max(0, Math.ceil((limitedUntil - Date.now()) / 1000))
}

export function useRateLimitCountdown(key: string) {
  const now = ref(Date.now())
  const timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)

  const remainingSeconds = computed(() => {
    now.value
    return getRateLimitRemaining(key)
  })
  const isLimited = computed(() => remainingSeconds.value > 0)

  onBeforeUnmount(() => {
    window.clearInterval(timer)
  })

  return {
    remainingSeconds,
    isLimited
  }
}
