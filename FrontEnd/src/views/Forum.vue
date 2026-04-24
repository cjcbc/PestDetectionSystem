<template>
  <div class="forum-container">
    <div class="forum-header">
      <div>
        <h1>农业社区论坛</h1>
        <p>查看最新农业资讯，交流病虫害防治经验</p>
      </div>
      <el-button type="primary" @click="goCreate">发布帖子</el-button>
    </div>

    <el-card class="filter-card filter-card--sticky">
      <div class="filters">
        <el-radio-group v-model="category" @change="handleFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="资讯">资讯</el-radio-button>
          <el-radio-button label="预警">预警</el-radio-button>
          <el-radio-button label="技巧">技巧</el-radio-button>
          <el-radio-button label="其他">其他</el-radio-button>
        </el-radio-group>

        <el-select v-model="sortBy" style="width: 140px" @change="handleFilterChange">
          <el-option label="最新" value="newest" />
          <el-option label="热度" value="hottest" />
        </el-select>

        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索标题/内容"
            clearable
            @keyup.enter="handleFilterChange"
          />
          <el-button type="primary" @click="handleFilterChange">搜索</el-button>
        </div>
      </div>
    </el-card>

    <el-empty v-if="!loading && posts.length === 0" description="暂无帖子，快来发布第一条内容" />

    <div v-loading="loading" class="post-list">
      <el-card v-for="post in posts" :key="post.id" class="post-card" :class="getPostCategoryClass(post.category)" @click="goDetail(post.id)">
        <div class="post-main">
          <div class="post-meta-top">
            <el-tag effect="plain" :class="'category-tag--' + getPostCategoryKey(post.category)">{{ post.category || '未分类' }}</el-tag>
            <span>{{ formatTime(post.createdTime) }}</span>
          </div>

          <h3>{{ post.title }}</h3>
          <p class="summary">{{ post.summary || getContentPreview(post.content) }}</p>

          <div class="post-meta-bottom">
            <span class="meta-item"><el-icon><User /></el-icon> {{ post.author }}</span>
            <span class="meta-item"><el-icon><View /></el-icon> {{ post.viewCount }}</span>
            <span class="meta-item"><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
            <span class="meta-item"><el-icon><ChatLineSquare /></el-icon> {{ post.commentCount }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <div class="pager" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :page-size="pageSize"
        :current-page="page"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPosts } from '@/api/forum'
import type { ForumPost } from '@/types/forum'
import { User, View, Star, ChatLineSquare } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const posts = ref<ForumPost[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 10

const category = ref('all')
const sortBy = ref<'newest' | 'hottest'>('newest')
const keyword = ref('')

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPosts(page.value, pageSize, category.value, keyword.value, sortBy.value)
    posts.value = res.list
    total.value = res.total
  } catch (error) {
    console.error(error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  fetchPosts()
}

function handleFilterChange() {
  page.value = 1
  fetchPosts()
}

function goDetail(postId: string) {
  router.push(`/forum/post/${postId}`)
}

function goCreate() {
  router.push('/forum/create')
}

function formatTime(time: number) {
  return new Date(time).toLocaleString('zh-CN')
}

function getContentPreview(content: string) {
  const plain = content.replace(/<[^>]*>/g, '').trim()
  return plain.length > 100 ? `${plain.slice(0, 100)}...` : plain
}

function getPostCategoryKey(category: string | undefined): string {
  if (!category) return 'other'
  const map: Record<string, string> = { '资讯': 'info', '预警': 'alert', '技巧': 'tips' }
  return map[category] || 'other'
}

function getPostCategoryClass(category: string | undefined): string {
  return 'post-card--' + getPostCategoryKey(category)
}

onMounted(fetchPosts)
</script>

<style scoped>
.forum-container {
  padding: var(--spacing-lg);
  display: grid;
  gap: 16px;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.forum-header h1 {
  margin-bottom: 8px;
}

.forum-header p {
  margin: 0;
  color: var(--color-text-secondary);
}

.filter-card--sticky {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
}

.filters {
  display: grid;
  gap: 12px;
}

.search-box {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.post-list {
  display: grid;
  gap: 12px;
}

.post-card {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
}

/* Category-specific color coding */
.post-card--info {
  border-left: 4px solid #3b82f6;
}

.post-card--alert {
  border-left: 4px solid #ef4444;
}

.post-card--tips {
  border-left: 4px solid var(--color-primary);
}

.post-card--other {
  border-left: 4px solid var(--color-gray-400);
}

.post-card:hover {
  border-color: var(--el-color-primary-light-5);
  transform: translateX(4px);
}

.post-card--info:hover {
  border-color: #3b82f6;
  border-left-color: #3b82f6;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.12);
}

.post-card--alert:hover {
  border-color: #ef4444;
  border-left-color: #ef4444;
  box-shadow: 0 2px 12px rgba(239, 68, 68, 0.12);
}

.post-card--tips:hover {
  border-color: var(--color-primary);
  border-left-color: var(--color-primary);
  box-shadow: var(--shadow-brand);
}

.post-card--other:hover {
  box-shadow: var(--shadow-md);
}

/* Category tag colors */
:deep(.category-tag--info) {
  --el-tag-bg-color: rgba(59, 130, 246, 0.1);
  --el-tag-border-color: rgba(59, 130, 246, 0.2);
  --el-tag-text-color: #1d4ed8;
}

:deep(.category-tag--alert) {
  --el-tag-bg-color: rgba(239, 68, 68, 0.1);
  --el-tag-border-color: rgba(239, 68, 68, 0.2);
  --el-tag-text-color: #b91c1c;
}

:deep(.category-tag--tips) {
  --el-tag-bg-color: var(--color-bg-brand);
  --el-tag-border-color: var(--color-border-brand);
  --el-tag-text-color: var(--color-primary-dark);
}

:deep(.category-tag--other) {
  --el-tag-bg-color: var(--color-gray-100);
  --el-tag-border-color: var(--color-gray-300);
  --el-tag-text-color: var(--color-gray-600);
}

.post-main {
  display: grid;
  gap: 10px;
}

.post-meta-top,
.post-meta-bottom {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.post-main h3 {
  margin: 0;
}

.summary {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .forum-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
