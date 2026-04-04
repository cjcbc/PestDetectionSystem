<template>
  <div class="about">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>关于页面</span>
        </div>
      </template>
      <p>这是一个 Vue3 + Vite + TypeScript 项目</p>
      <p>使用的技术栈：</p>
      <ul>
        <li>Vue 3 - 渐进式 JavaScript 框架</li>
        <li>Vite - 下一代前端构建工具</li>
        <li>TypeScript - JavaScript 的超集</li>
        <li>Vue Router - 官方路由管理器</li>
        <li>Pinia - 官方状态管理库</li>
        <li>Axios - HTTP 客户端</li>
        <li>Element Plus - Vue 3 组件库</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.about {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

ul {
  margin: 15px 0;
  padding-left: 20px;
}

li {
  margin: 8px 0;
  line-height: 1.6;
}
</style>
