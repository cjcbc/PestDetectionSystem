import request from './request'
import type { DetectPayload, DetectResult } from '@/types/detect'

export function detect(payload: DetectPayload): Promise<DetectResult> {
  return request.post('/detect', payload)
}

export function getDetectRecords(): Promise<DetectResult[]> {
  return request.get('/detect/records')
}
