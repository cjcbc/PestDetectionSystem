export interface WarningItem {
  id: string
  title: string
  content: string
  region?: string
  pestName?: string
  severity: 1 | 2 | 3
  status: 0 | 1
  publishTime: number
  publisherId: string
  publisherName: string
  viewCount: number
  isRead: boolean
  readTime: number | null
  createdTime: number
}

export interface WarningPageResult {
  total: number
  pageNum: number
  pageSize: number
  pages: number
  list: WarningItem[]
}

export interface CreateWarningPayload {
  title: string
  content: string
  region?: string
  pestName?: string
  severity: 1 | 2 | 3
  status: 0 | 1
}

export interface WarningUnreadSummary {
  id: string
  title: string
  severity: 1 | 2 | 3
  publishTime: number
}

export interface WarningUnreadCountResult {
  unreadCount: number
  warnings: WarningUnreadSummary[]
}

export interface WarningReadResult {
  warningId: string
  userId: string
  readTime: number
}
