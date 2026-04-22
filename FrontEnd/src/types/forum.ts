export interface ForumPost {
  id: string
  userId: string
  author: string
  title: string
  category: string
  summary?: string
  content: string
  coverImage?: string
  viewCount: number
  likeCount: number
  commentCount: number
  isLiked: boolean
  createdTime: number
  updatedTime: number
}

export interface ForumCommentReply {
  id: string
  userId: string
  username: string
  userImage?: string
  content: string
  likeCount: number
  isLiked: boolean
  createdTime: number
}

export interface ForumComment {
  id: string
  userId: string
  username: string
  userImage?: string
  content: string
  likeCount: number
  replyCount: number
  isLiked: boolean
  createdTime: number
  replies: ForumCommentReply[]
}

export interface ForumListResult<T> {
  total: number
  pageNum: number
  pageSize: number
  pages: number
  list: T[]
}

export interface CreatePostPayload {
  title: string
  summary?: string
  content: string
  coverImage?: string
  category?: string
  tags?: string
}

export interface CreateCommentPayload {
  content: string
  parentId?: string | null
}

export interface ToggleLikeResult {
  liked: boolean
  likeCount: number
}
