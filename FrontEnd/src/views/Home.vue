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
          v-for="(item, index) in sections"
          :key="item.title"
          class="entry-card"
          :class="'entry-card--' + index"
          :style="{ animationDelay: `${index * 80}ms` }"
          @click="router.push(item.to)"
        >
          <div class="entry-card__top">
            <div class="entry-card__icon" :style="{ background: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <span class="entry-card__tag" :style="{ background: item.tagBg, color: item.tagColor }">{{ item.tag }}</span>
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
    color: 'linear-gradient(135deg, #14532d 0%, #4ade80 100%)',
    tagBg: 'rgba(22, 101, 52, 0.1)',
    tagColor: '#166534'
  },
  {
    title: 'AI问答',
    description: '与智能农业助手对话，获取关于病虫害防治、种植技术的专业建议和指导。',
    to: '/chat',
    tag: '在线咨询',
    icon: ChatDotRound,
    color: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
    tagBg: 'rgba(30, 58, 138, 0.1)',
    tagColor: '#1e3a8a'
  },
  {
    title: '交流论坛',
    description: '与其他农民、专家交流经验，分享防治方案，讨论种植技术问题。',
    to: '/forum',
    tag: '社区交流',
    icon: Files,
    color: 'linear-gradient(135deg, #7c2d12 0%, #ea580c 100%)',
    tagBg: 'rgba(124, 45, 18, 0.1)',
    tagColor: '#9a3412'
  },
  {
    title: '个人中心',
    description: '管理个人信息、查看识别历史、收藏知识文章、跟进咨询记录。',
    to: '/user-profile',
    tag: '个人管理',
    icon: User,
    color: 'linear-gradient(135deg, #5b21b6 0%, #a855f7 100%)',
    tagBg: 'rgba(91, 33, 182, 0.1)',
    tagColor: '#5b21b6'
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
  border-radius: var(--radius-2xl);
  background:
    linear-gradient(135deg, rgba(20, 83, 45, 0.96) 0%, rgba(26, 122, 58, 0.9) 50%, rgba(74, 222, 128, 0.85) 100%);
  color: #fff;
  box-shadow: var(--shadow-brand-lg);
}

.hero__eyebrow {
  display: none;
}

.hero h1 {
  margin-bottom: 28px;
  font-size: clamp(32px, 4vw, 48px);
  line-height: 1.15;
  font-family: var(--font-display);
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
  border-radius: var(--radius-lg);
  color: #fff;
  font-size: 22px;
  box-shadow: var(--shadow-md);
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
  border-radius: var(--radius-2xl);
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(17, 24, 39, 0.06);
  box-shadow: var(--shadow-md);
  transition:
    transform var(--transition-normal),
    box-shadow var(--transition-normal),
    border-color var(--transition-fast);
  cursor: pointer;
  animation: cardFadeIn 0.5s ease-out backwards;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.entry-card:hover {
  transform: translateY(-6px);
}

.entry-card--0:hover {
  border-color: rgba(74, 222, 128, 0.4);
  box-shadow: 0 12px 28px rgba(74, 222, 128, 0.18);
}

.entry-card--1:hover {
  border-color: rgba(59, 130, 246, 0.35);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.18);
}

.entry-card--2:hover {
  border-color: rgba(234, 88, 12, 0.35);
  box-shadow: 0 12px 28px rgba(234, 88, 12, 0.18);
}

.entry-card--3:hover {
  border-color: rgba(168, 85, 247, 0.35);
  box-shadow: 0 12px 28px rgba(168, 85, 247, 0.18);
}

.entry-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.entry-card__tag {
  padding: 6px 12px;
  border-radius: var(--radius-full);
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
  color: var(--color-primary-dark);
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
