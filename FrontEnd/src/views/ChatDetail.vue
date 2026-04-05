<template>
  <div class="chat-detail-container">
    <h1>对话详情</h1>
    <el-card>
      <p>对话详情页面 - 开发中 (sessionId: {{ $route.params.sessionId }})</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.chat-detail-container {
  padding: var(--spacing-lg);
}

h1 {
  margin-bottom: var(--spacing-lg);
}
</style>
