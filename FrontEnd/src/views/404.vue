<template>
  <div class="not-found">
    <div class="not-found-content">
      <h1 class="status-code">404</h1>
      <h2 class="message">页面未找到</h2>
      <p class="description">抱歉，您访问的页面不存在或已被删除</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="goHome">返回首页</el-button>
        <el-button size="large" @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push({ name: 'Home' })
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-white) 100%);
}

.not-found-content {
  text-align: center;
  padding: var(--spacing-lg);
}

.status-code {
  font-size: 120px;
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  margin: 0;
  line-height: 1;
}

.message {
  font-size: var(--font-size-2xl);
  color: var(--color-text-primary);
  margin: var(--spacing-lg) 0;
}

.description {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xl);
}

.actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}

@media (max-width: 768px) {
  .status-code {
    font-size: 80px;
  }

  .message {
    font-size: var(--font-size-xl);
  }

  .actions {
    flex-direction: column;
  }
}
</style>
