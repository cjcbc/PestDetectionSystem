<template>
  <div class="post-detail-container">
    <el-button text @click="goBack">返回论坛</el-button>

    <el-card v-loading="postLoading">
      <template v-if="post">
        <div class="post-header">
          <h1>{{ post.title }}</h1>
          <div class="meta">
            <el-tag effect="plain">{{ post.category || '未分类' }}</el-tag>
            <span>作者：{{ post.author }}</span>
            <span>{{ formatTime(post.createdTime) }}</span>
            <span>浏览 {{ post.viewCount }}</span>
          </div>
          <div class="actions">
            <el-button :type="post.isLiked ? 'primary' : 'default'" @click="handleLikePost">
              {{ post.isLiked ? '已点赞' : '点赞' }} ({{ post.likeCount }})
            </el-button>
          </div>
        </div>
        <div class="post-content" v-html="post.content" />
      </template>
    </el-card>

    <el-card>
      <template #header>
        <div class="comment-head">
          <span>评论区</span>
          <el-select v-model="commentSortBy" style="width: 140px" @change="fetchComments">
            <el-option label="最新" value="newest" />
            <el-option label="最热" value="hottest" />
          </el-select>
        </div>
      </template>

      <div class="comment-editor">
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="4"
          maxlength="1000"
          show-word-limit
          placeholder="写下你的评论..."
        />
        <div class="editor-actions">
          <el-button type="primary" @click="submitComment">发布评论</el-button>
        </div>
      </div>

      <div v-loading="commentLoading" class="comment-list">
        <el-empty v-if="!commentLoading && comments.length === 0" description="暂无评论" />

        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-main">
            <div class="comment-top">
              <span class="user">{{ comment.username }}</span>
              <span>{{ formatTime(comment.createdTime) }}</span>
            </div>
            <p>{{ comment.content }}</p>
            <div class="comment-actions">
              <el-button text @click="toggleCommentLike(comment.id)">
                {{ comment.isLiked ? '取消赞' : '点赞' }} ({{ comment.likeCount }})
              </el-button>
              <el-button text @click="showReply(comment.id)">回复</el-button>
              <el-button
                v-if="canDelete(comment.userId)"
                text
                type="danger"
                @click="removeComment(comment.id)"
              >
                删除
              </el-button>
            </div>
          </div>

          <div v-if="activeReplyId === comment.id" class="reply-editor">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="3"
              maxlength="1000"
              show-word-limit
              placeholder="输入回复内容"
            />
            <div class="editor-actions">
              <el-button type="primary" @click="submitReply(comment.id)">发送回复</el-button>
              <el-button @click="cancelReply">取消</el-button>
            </div>
          </div>

          <div v-if="comment.replies.length > 0" class="reply-list">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <div class="comment-top">
                <span class="user">{{ reply.username }}</span>
                <span>{{ formatTime(reply.createdTime) }}</span>
              </div>
              <p>{{ reply.content }}</p>
              <div class="comment-actions">
                <el-button text @click="toggleCommentLike(reply.id)">
                  {{ reply.isLiked ? '取消赞' : '点赞' }} ({{ reply.likeCount }})
                </el-button>
                <el-button
                  v-if="canDelete(reply.userId)"
                  text
                  type="danger"
                  @click="removeComment(reply.id)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createComment,
  deleteComment,
  getComments,
  getPostDetail,
  toggleLikeComment,
  toggleLikePost
} from '@/api/forum'
import type { ForumComment, ForumPost } from '@/types/forum'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const postId = computed(() => String(route.params.id || ''))

const post = ref<ForumPost | null>(null)
const comments = ref<ForumComment[]>([])

const postLoading = ref(false)
const commentLoading = ref(false)

const commentText = ref('')
const commentSortBy = ref<'newest' | 'hottest'>('newest')

const activeReplyId = ref<string | null>(null)
const replyText = ref('')

const currentUserId = computed(() => appStore.userInfo?.id || '')
const isAdmin = computed(() => appStore.userInfo?.role === 0)

async function fetchPost() {
  postLoading.value = true
  try {
    post.value = await getPostDetail(postId.value)
  } catch (error) {
    console.error(error)
    ElMessage.error('加载帖子详情失败')
  } finally {
    postLoading.value = false
  }
}

async function fetchComments() {
  commentLoading.value = true
  try {
    const res = await getComments(postId.value, 1, 20, commentSortBy.value)
    comments.value = res.list
  } catch (error) {
    console.error(error)
    ElMessage.error('加载评论失败')
  } finally {
    commentLoading.value = false
  }
}

async function handleLikePost() {
  if (!appStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  try {
    const res = await toggleLikePost(postId.value)
    if (post.value) {
      post.value.isLiked = res.liked
      post.value.likeCount = res.likeCount
    }
  } catch (error) {
    console.error(error)
  }
}

async function submitComment() {
  if (!appStore.isLoggedIn) {
    ElMessage.warning('请先登录后评论')
    return
  }
  if (!commentText.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  try {
    await createComment(postId.value, { content: commentText.value.trim(), parentId: null })
    commentText.value = ''
    ElMessage.success('评论发布成功')
    await fetchComments()
    await fetchPost()
  } catch (error) {
    console.error(error)
  }
}

function showReply(commentId: string) {
  if (!appStore.isLoggedIn) {
    ElMessage.warning('请先登录后回复')
    return
  }
  activeReplyId.value = commentId
  replyText.value = ''
}

function cancelReply() {
  activeReplyId.value = null
  replyText.value = ''
}

async function submitReply(parentId: string) {
  if (!replyText.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  try {
    await createComment(postId.value, { content: replyText.value.trim(), parentId })
    ElMessage.success('回复成功')
    cancelReply()
    await fetchComments()
  } catch (error) {
    console.error(error)
  }
}

async function toggleCommentLike(commentId: string) {
  if (!appStore.isLoggedIn) {
    ElMessage.warning('请先登录后点赞')
    return
  }
  try {
    await toggleLikeComment(commentId)
    await fetchComments()
  } catch (error) {
    console.error(error)
  }
}

async function removeComment(commentId: string) {
  try {
    await deleteComment(commentId)
    ElMessage.success('评论已删除')
    await fetchComments()
    await fetchPost()
  } catch (error) {
    console.error(error)
  }
}

function canDelete(userId: string) {
  return isAdmin.value || userId === currentUserId.value
}

function formatTime(time: number) {
  return new Date(time).toLocaleString('zh-CN')
}

function goBack() {
  router.push('/forum')
}

onMounted(async () => {
  await fetchPost()
  await fetchComments()
})
</script>

<style scoped>
.post-detail-container {
  padding: var(--spacing-lg);
  display: grid;
  gap: 16px;
}

.post-header {
  display: grid;
  gap: 12px;
}

.post-header h1 {
  margin: 0;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.post-content {
  margin-top: 20px;
  line-height: 1.8;
}

.comment-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-editor {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
}

.editor-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.comment-list {
  display: grid;
  gap: 14px;
}

.comment-item {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px;
  display: grid;
  gap: 10px;
}

.comment-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.comment-top .user {
  color: var(--color-text-primary);
  font-weight: 600;
}

.comment-main p,
.reply-item p {
  margin: 8px 0;
  line-height: 1.7;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.reply-list {
  display: grid;
  gap: 10px;
  border-left: 3px solid #e5e7eb;
  padding-left: 12px;
}

.reply-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 10px;
}

.reply-editor {
  display: grid;
  gap: 8px;
}
</style>
