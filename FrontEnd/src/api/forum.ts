import request from './request'
import type {
  CreateCommentPayload,
  CreatePostPayload,
  ForumComment,
  ForumListResult,
  ForumPost,
  ToggleLikeResult
} from '@/types/forum'

export function getPosts(
  page = 1,
  pageSize = 10,
  category?: string,
  keyword?: string,
  sortBy: 'newest' | 'hottest' = 'newest'
): Promise<ForumListResult<ForumPost>> {
  return request.get('/article', {
    params: {
      page,
      pageSize,
      category,
      keyword,
      sortBy
    }
  })
}

export function getPostDetail(postId: string): Promise<ForumPost> {
  return request.get(`/article/${postId}`)
}

export function createPost(payload: CreatePostPayload): Promise<ForumPost> {
  return request.post('/article', payload)
}

export function toggleLikePost(postId: string): Promise<ToggleLikeResult> {
  return request.post(`/article/${postId}/like`)
}

export function getComments(
  postId: string,
  page = 1,
  pageSize = 20,
  sortBy: 'newest' | 'hottest' = 'newest'
): Promise<ForumListResult<ForumComment>> {
  return request.get(`/article/${postId}/comments`, {
    params: { page, pageSize, sortBy }
  })
}

export function createComment(postId: string, payload: CreateCommentPayload): Promise<ForumComment> {
  return request.post(`/article/${postId}/comments`, payload)
}

export function toggleLikeComment(commentId: string): Promise<{ commentId: string; liked: boolean; likeCount: number }> {
  return request.post(`/comment/${commentId}/like`)
}

export function deleteComment(commentId: string): Promise<null> {
  return request.delete(`/comment/${commentId}`)
}
