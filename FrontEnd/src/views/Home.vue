<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero__content">
        <h1>农业病虫害识别与交流平台</h1>
        <div class="hero__actions">
          <el-button type="primary" size="large" @click="router.push('/detection')">
            进入病虫识别
          </el-button>
          <el-button size="large" plain @click="router.push('/forum')">
            查看交流论坛
          </el-button>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section__title">
        <h2>栏目入口</h2>
        <p>点击卡片即可进入对应栏目页</p>
      </div>

      <div class="grid">
        <article
          v-for="item in sections"
          :key="item.title"
          class="entry-card"
          @click="router.push(item.to)"
        >
          <div class="entry-card__top">
            <div class="entry-card__icon" :style="{ background: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <span class="entry-card__tag">{{ item.tag }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
          <button class="entry-card__button" type="button">
            进入栏目
            <el-icon><ArrowRight /></el-icon>
          </button>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  ChatDotRound,
  Crop,
  Files,
  User
} from '@element-plus/icons-vue'

const router = useRouter()

const sections = [
  {
    title: '病虫识别',
    description: '上传作物图片并进行病虫害识别诊断，实时获取专业诊断结果与防治建议。',
    to: '/detection',
    tag: '识别服务',
    icon: Crop,
    color: 'linear-gradient(135deg, #166534 0%, #4ade80 100%)'
  },
  {
    title: 'AI问答',
    description: '与智能农业助手对话，获取关于病虫害防治、种植技术的专业建议和指导。',
    to: '/chat',
    tag: '在线咨询',
    icon: ChatDotRound,
    color: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)'
  },
  {
    title: '交流论坛',
    description: '与其他农民、专家交流经验，分享防治方案，讨论种植技术问题。',
    to: '/forum',
    tag: '社区交流',
    icon: Files,
    color: 'linear-gradient(135deg, #7c2d12 0%, #ea580c 100%)'
  },
  {
    title: '个人中心',
    description: '管理个人信息、查看识别历史、收藏知识文章、跟进咨询记录。',
    to: '/user-profile',
    tag: '个人管理',
    icon: User,
    color: 'linear-gradient(135deg, #5b21b6 0%, #a855f7 100%)'
  }
]
</script>

<style scoped>
.home-page {
  display: grid;
  gap: 28px;
}

.hero {
  display: grid;
  gap: 24px;
  align-items: stretch;
}

.hero__content {
  padding: 48px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(20, 83, 45, 0.96) 0%, rgba(34, 197, 94, 0.88) 100%),
    linear-gradient(180deg, #14532d 0%, #22c55e 100%);
  color: #fff;
  box-shadow: 0 28px 60px rgba(20, 83, 45, 0.18);
}

.hero__eyebrow {
  display: none;
}

.hero h1 {
  margin-bottom: 28px;
  font-size: clamp(32px, 4vw, 48px);
  line-height: 1.1;
}

.hero__desc {
  display: none;
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.status-card {
  display: none;
}

.entry-card__icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  color: #fff;
  font-size: 22px;
  box-shadow: 0 12px 24px rgba(17, 24, 39, 0.12);
}

.section {
  padding: 8px 0;
}

.section__title {
  margin-bottom: 18px;
}

.section__title p {
  margin: 0;
  color: var(--color-text-secondary);
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.entry-card {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(17, 24, 39, 0.06);
  box-shadow: 0 18px 36px rgba(17, 24, 39, 0.06);
  transition:
    transform var(--transition-normal),
    box-shadow var(--transition-normal),
    border-color var(--transition-fast);
  cursor: pointer;
}

.entry-card:hover {
  transform: translateY(-6px);
  border-color: rgba(34, 197, 94, 0.25);
  box-shadow: 0 24px 40px rgba(21, 128, 61, 0.12);
}

.entry-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.entry-card__tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: #eff6f0;
  color: #166534;
  font-size: 12px;
  font-weight: 700;
}

.entry-card h3 {
  margin-bottom: 12px;
}

.entry-card p {
  min-height: 72px;
  color: var(--color-text-secondary);
}

.entry-card__button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  background: transparent;
  color: #166534;
  font-size: 15px;
  font-weight: 700;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .hero__content,
  .status-card,
  .entry-card {
    border-radius: 22px;
  }

  .hero__content {
    padding: 24px;
  }

  .entry-card {
    padding: 20px;
  }
}
</style>
