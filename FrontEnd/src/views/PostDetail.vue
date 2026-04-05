<template>
  <div class="post-detail-container">
    <h1>帖子详情</h1>
    <el-card>
      <p>帖子详情页面 - 开发中 (postId: {{ $route.params.id }})</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.post-detail-container {
  padding: var(--spacing-lg);
}

h1 {
  margin-bottom: var(--spacing-lg);
}
</style>
