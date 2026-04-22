import request from './request'
import type {
  CreateWarningPayload,
  WarningItem,
  WarningPageResult,
  WarningReadResult,
  WarningUnreadCountResult
} from '@/types/warning'

export function getWarningList(params: {
  page?: number
  pageSize?: number
  status?: 0 | 1
  severity?: 1 | 2 | 3
  region?: string
  keyword?: string
}): Promise<WarningPageResult> {
  return request.get('/warning', { params })
}

export function createWarning(payload: CreateWarningPayload): Promise<WarningItem> {
  return request.post('/warning', payload)
}

export function updateWarning(warningId: string, payload: Partial<CreateWarningPayload>): Promise<WarningItem> {
  return request.patch(`/warning/${warningId}`, payload)
}

export function toggleWarningStatus(warningId: string): Promise<WarningItem> {
  return request.patch(`/warning/${warningId}/status-toggle`)
}

export function deleteWarning(warningId: string): Promise<string> {
  return request.delete(`/warning/${warningId}`)
}

export function getWarningDetail(warningId: string): Promise<WarningItem> {
  return request.get(`/warning/${warningId}`)
}

export function markWarningRead(warningId: string): Promise<WarningReadResult> {
  return request.post(`/warning/${warningId}/read`)
}

export function getUnreadWarningCount(): Promise<WarningUnreadCountResult> {
  return request.get('/warning/unread/count')
}
