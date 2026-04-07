<template>
  <div class="detail-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/detection' }">病虫害识别</el-breadcrumb-item>
      <el-breadcrumb-item>识别详情</el-breadcrumb-item>
    </el-breadcrumb>

    <h2>识别结果详情</h2>

    <div v-loading="loading" class="detail-content">
      <template v-if="record">
        <el-card class="image-card">
          <template #header>
            <div class="card-header">
              <span>标注图片</span>
              <el-button type="primary" size="small" @click="downloadImage">
                <el-icon><Download /></el-icon>
                下载图片
              </el-button>
            </div>
          </template>
          <div class="image-wrapper">
            <img
              :src="getImageUrl(record.imageName)"
              alt="识别图片"
              class="detail-image"
              @error="handleImageError"
            />
          </div>
        </el-card>

        <el-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>识别信息</span>
              <el-button type="success" size="small" @click="handleAskAI">
                <el-icon><ChatDotRound /></el-icon>
                问 AI
              </el-button>
            </div>
          </template>

          <div class="result-grid">
            <div class="confidence-section">
              <div class="confidence-circle">
                <svg width="160" height="160" viewBox="0 0 160 160">
                  <circle cx="80" cy="80" r="70" fill="none" stroke="#e5e9f0" stroke-width="10" />
                  <circle
                    cx="80"
                    cy="80"
                    r="70"
                    fill="none"
                    :stroke="getStatusCircleColor(record.status)"
                    stroke-width="10"
                    stroke-dasharray="439.823"
                    :stroke-dashoffset="439.823 * (1 - record.confidence)"
                    stroke-linecap="round"
                    transform="rotate(-90 80 80)"
                  />
                  <text x="80" y="75" text-anchor="middle" class="confidence-value">{{ confidencePercent }}%</text>
                  <text x="80" y="100" text-anchor="middle" class="confidence-label">置信度</text>
                </svg>
              </div>
              <el-tag :type="getStatusTagType(record.status)" size="large" class="confidence-tag">
                {{ getStatusLabel(record.status) }}
              </el-tag>
            </div>

            <div class="info-section">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="识别状态">
                  <el-tag :type="getStatusTagType(record.status)">{{ getStatusLabel(record.status) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="病虫害名称">
                  <span class="highlight-text">{{ record.topLabel }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="识别时间">
                  {{ formatTime(record.createdTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="记录 ID">
                  {{ record.id }}
                </el-descriptions-item>
                <el-descriptions-item label="文件名称">
                  {{ record.imageName }}
                </el-descriptions-item>
                <el-descriptions-item label="用户 ID">
                  {{ record.userId }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>

        <el-card class="suggestion-card">
          <template #header>
            <span>诊断建议</span>
          </template>
          <div class="suggestion-content">
            <el-alert
              title="智能诊断建议"
              :description="getSuggestion(record.topLabel, record.status)"
              type="info"
              :closable="false"
              show-icon
            />
            <el-divider />
            <p class="suggestion-tip">
              <el-icon><InfoFilled /></el-icon>
              可以继续点击“问 AI”，获取更详细的防治建议和用药方案。
            </p>
          </div>
        </el-card>

        <div class="action-buttons">
          <el-button size="large" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
          <el-button type="primary" size="large" @click="handleAskAI">
            <el-icon><ChatDotRound /></el-icon>
            问 AI
          </el-button>
          <el-button type="success" size="large" @click="startNewDetection">
            <el-icon><Plus /></el-icon>
            新的识别
          </el-button>
        </div>
      </template>

      <el-empty v-else description="未找到识别记录" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound, Download, InfoFilled, Plus } from '@element-plus/icons-vue'
import { getDetectRecords } from '@/api/detect'
import type { DetectResult } from '@/types/detect'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const record = ref<DetectResult | null>(null)

const confidencePercent = computed(() => {
  if (!record.value) return 0
  return Math.round(record.value.confidence * 100)
})

async function loadRecord() {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('记录 ID 不存在')
    router.push('/detection')
    return
  }

  try {
    loading.value = true
    const records = await getDetectRecords()
    const found = records.find((item) => item.id.toString() === id)

    if (found) {
      record.value = found
    } else {
      ElMessage.warning('未找到该识别记录')
      router.push('/detection')
    }
  } catch {
    ElMessage.error('加载记录失败')
    router.push('/detection')
  } finally {
    loading.value = false
  }
}

function handleAskAI() {
  if (!record.value) return
  router.push({
    path: '/chat',
    query: {
      detectionId: record.value.id.toString(),
      detectionLabel: record.value.topLabel,
      question: `请结合这次识别结果，分析${record.value.topLabel}的症状特征、可能成因和防治建议。`
    }
  })
}

function goBack() {
  router.push('/detection')
}

function startNewDetection() {
  router.push('/detection')
}

function getImageUrl(imageName: string): string {
  if (!imageName) return ''
  if (imageName.startsWith('http')) return imageName

  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api'
  const apiBase = baseURL.replace(/\/$/, '')
  return `${apiBase}/images/${encodeURIComponent(imageName)}`
}

function handleImageError() {
  ElMessage.warning('图片加载失败')
}

function downloadImage() {
  if (!record.value) return
  window.open(getImageUrl(record.value.imageName), '_blank')
}

function getStatusLabel(status: number): string {
  if (status === 1) return '高置信度'
  if (status === 0) return '低置信度'
  return '未检测到'
}

function getStatusTagType(status: number): 'success' | 'warning' | 'info' {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  return 'info'
}

function getStatusCircleColor(status: number): string {
  if (status === 1) return '#67c23a'
  if (status === 0) return '#e6a23c'
  return '#909399'
}

function getSuggestion(label: string, status: number): string {
  if (status === -1) {
    return '本次未检测到明确病虫害目标。建议更换更清晰的图片、调整拍摄角度，或补充叶片/果实局部特写后重新识别。'
  }
  if (status === 0) {
    return '本次识别到了疑似病虫害，但置信度偏低。建议结合田间实际情况复核，并重新上传更清晰、光照更稳定的图片进一步确认。'
  }

  const name = label.includes(' ') ? label.split(' ').slice(-1)[0] : label
  return `检测到“${name}”。建议及时隔离受影响植株，清理病叶病果，并根据具体病害类型采取针对性的防治措施。`
}

function formatTime(timestamp: number): string {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

onMounted(() => {
  loadRecord()
})
</script>

<style scoped>
.detail-page {
  padding: var(--spacing-lg);
  max-width: 1200px;
  margin: 0 auto;
}

.el-breadcrumb {
  margin-bottom: var(--spacing-md);
}

h2 {
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-primary);
}

.detail-content {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-card,
.result-card,
.suggestion-card {
  margin-bottom: var(--spacing-lg);
}

.image-wrapper {
  text-align: center;
  background-color: var(--color-bg-secondary);
  padding: var(--spacing-lg);
  border-radius: 8px;
}

.detail-image {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.result-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: var(--spacing-xl);
  align-items: start;
}

.confidence-section {
  text-align: center;
}

.confidence-circle {
  margin-bottom: var(--spacing-md);
}

.confidence-value {
  font-size: 32px;
  font-weight: bold;
  fill: var(--color-primary);
}

.confidence-label {
  font-size: 14px;
  fill: var(--color-text-secondary);
}

.confidence-tag {
  margin-top: var(--spacing-sm);
}

.info-section {
  width: 100%;
}

.highlight-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
}

.suggestion-content {
  padding: var(--spacing-sm);
}

.suggestion-tip {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-text-secondary);
  font-size: 14px;
  margin: 0;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg) 0;
}

@media (max-width: 768px) {
  .detail-page {
    padding: var(--spacing-md);
  }

  .result-grid {
    grid-template-columns: 1fr;
  }

  .confidence-circle svg {
    width: 120px;
    height: 120px;
  }

  .confidence-value {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
