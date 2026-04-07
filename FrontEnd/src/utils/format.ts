import dayjs from 'dayjs'

export function formatMessageTime(timestamp: number): string {
  const date = dayjs(timestamp)
  const today = dayjs()

  if (date.format('YYYY-MM-DD') === today.format('YYYY-MM-DD')) {
    return date.format('HH:mm')
  }

  return date.format('MM-DD HH:mm')
}

export function formatSessionTime(timestamp: number): string {
  const date = dayjs(timestamp)
  const today = dayjs()

  if (date.format('YYYY-MM-DD') === today.format('YYYY-MM-DD')) {
    return date.format('HH:mm')
  }

  return date.format('MM-DD')
}
