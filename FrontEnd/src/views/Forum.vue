<template>
  <div class="forum-container">
    <div class="forum-header">
      <div>
        <h1>农业社区论坛</h1>
        <p>查看最新农业资讯，交流病虫害防治经验</p>
      </div>
      <el-button type="primary" @click="goCreate">发布帖子</el-button>
    </div>

    <el-card class="filter-card">
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
      <el-card v-for="post in posts" :key="post.id" class="post-card" @click="goDetail(post.id)">
        <div class="post-main">
          <div class="post-meta-top">
            <el-tag effect="plain">{{ post.category || '未分类' }}</el-tag>
            <span>{{ formatTime(post.createdTime) }}</span>
          </div>

          <h3>{{ post.title }}</h3>
          <p class="summary">{{ post.summary || getContentPreview(post.content) }}</p>

          <div class="post-meta-bottom">
            <span>作者：{{ post.author }}</span>
            <span>浏览 {{ post.viewCount }}</span>
            <span>点赞 {{ post.likeCount }}</span>
            <span>评论 {{ post.commentCount }}</span>
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
}

.post-card:hover {
  border-color: var(--el-color-primary-light-5);
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
