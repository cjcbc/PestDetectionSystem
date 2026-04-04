<template>
  <div class="home">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用 Vue3 项目</span>
        </div>
      </template>
      <p>这是一个基于 Vue3 + Vite + TypeScript 的项目模板</p>
      <p>当前计数: {{ appStore.count }}</p>
      <el-button type="primary" @click="appStore.increment">
        点击增加
      </el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<style scoped>
.home {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.welcome-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

p {
  margin: 10px 0;
  line-height: 1.6;
}
</style>
