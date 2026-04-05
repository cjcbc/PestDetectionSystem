<template>
  <div class="detection-page">
    <h2>病虫害智能识别</h2>

    <el-card class="upload-card">
      <div
        class="upload-zone"
        :class="{ 'is-dragover': isDragover }"
        @drop="handleDrop"
        @dragover.prevent
        @dragenter.prevent="isDragover = true"
        @dragleave="isDragover = false"
        @click="triggerFileInput"
      >
        <el-icon :size="48" color="#67c23a"><UploadFilled /></el-icon>
        <p class="upload-text">拖拽图片到此处，或 <span class="link-text">点击选择</span></p>
        <p class="upload-hint">支持 JPG、PNG 格式，文件大小不超过 10MB</p>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleFileSelect"
        />
      </div>

      <div v-if="previewUrl" class="preview-section">
        <img :src="previewUrl" alt="预览图片" class="preview-image" />
        <el-button type="danger" size="small" @click="clearSelection">清除</el-button>
      </div>

      <div class="action-bar">
        <el-button
          type="primary"
          size="large"
          :loading="isLoading"
          :disabled="!selectedFile"
          @click="handleDetect"
        >
          {{ isLoading ? '识别中...' : '开始识别' }}
        </el-button>
      </div>
    </el-card>

    <el-card v-if="detectResult" class="result-card">
      <template #header>
        <div class="card-header">
          <span>识别结果</span>
          <el-button type="success" size="small" @click="handleAskAI">
            <el-icon><ChatDotRound /></el-icon>
            追问 AI
          </el-button>
        </div>
      </template>

      <div class="result-content">
        <div class="confidence-circle">
          <svg width="120" height="120" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="54" fill="none" stroke="#e5e9f0" stroke-width="8" />
            <circle
              cx="60"
              cy="60"
              r="54"
              fill="none"
              :stroke="getStatusCircleColor(detectResult.status)"
              stroke-width="8"
              stroke-dasharray="339.292"
              :stroke-dashoffset="339.292 * (1 - confidence / 100)"
              stroke-linecap="round"
              transform="rotate(-90 60 60)"
            />
            <text x="60" y="55" text-anchor="middle" class="confidence-value">{{ confidence }}%</text>
            <text x="60" y="75" text-anchor="middle" class="confidence-label">置信度</text>
          </svg>
        </div>

        <div class="result-info">
          <div class="info-item">
            <span class="info-label">识别状态：</span>
            <el-tag :type="getStatusTagType(detectResult.status)">{{ getStatusLabel(detectResult.status) }}</el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">病虫害名称：</span>
            <span class="info-value">{{ detectResult.topLabel }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">识别时间：</span>
            <span class="info-value">{{ formatTime(detectResult.createdTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">文件名称：</span>
            <span class="info-value">{{ detectResult.imageName }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>识别历史</span>
          <el-button size="small" @click="loadRecords">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table
        :data="paginatedRecords"
        stripe
        v-loading="isLoadingRecords"
        empty-text="暂无识别记录"
      >
        <el-table-column label="识别时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="topLabel" label="识别结果" min-width="220" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="120">
          <template #default="{ row }">
            <el-tag :type="getConfidenceType(row.confidence)">
              {{ (row.confidence * 100).toFixed(2) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewRecord(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="records.length"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Refresh, UploadFilled } from '@element-plus/icons-vue'
import { detect, getDetectRecords } from '@/api/detect'
import type { DetectResult } from '@/types/detect'
import { fileToBase64, isImageFile } from '@/utils/image'

const router = useRouter()

const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const previewUrl = ref('')
const isDragover = ref(false)

const isLoading = ref(false)
const detectResult = ref<DetectResult | null>(null)

const records = ref<DetectResult[]>([])
const isLoadingRecords = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const confidence = computed(() => {
  if (!detectResult.value) return 0
  return Math.round(detectResult.value.confidence * 100)
})

const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return records.value.slice(start, start + pageSize.value)
})

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    processFile(file)
  }
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  isDragover.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) {
    processFile(file)
  }
}

function processFile(file: File) {
  if (!isImageFile(file)) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }

  selectedFile.value = file
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = URL.createObjectURL(file)
}

function clearSelection() {
  selectedFile.value = null
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

async function handleDetect() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  try {
    isLoading.value = true
    const base64 = await fileToBase64(selectedFile.value)
    const result = await detect({ imageBase64: base64 })
    detectResult.value = result
    ElMessage.success(`识别完成：${getStatusLabel(result.status)}`)
    await loadRecords()
    router.push({ path: `/detection/${result.id}` })
  } catch {
    ElMessage.error('识别失败，请重试')
  } finally {
    isLoading.value = false
  }
}

async function loadRecords() {
  try {
    isLoadingRecords.value = true
    records.value = await getDetectRecords()
  } catch {
    ElMessage.error('加载历史记录失败')
  } finally {
    isLoadingRecords.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
}

function viewRecord(record: DetectResult) {
  router.push({ path: `/detection/${record.id}` })
}

function handleAskAI() {
  if (!detectResult.value) return
  router.push({
    path: '/chat',
    query: {
      detectionId: detectResult.value.id.toString(),
      detectionLabel: detectResult.value.topLabel
    }
  })
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

function getConfidenceType(confidence: number): 'success' | 'warning' | 'danger' {
  const percent = confidence * 100
  if (percent >= 80) return 'success'
  if (percent >= 60) return 'warning'
  return 'danger'
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

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.detection-page {
  padding: var(--spacing-lg);
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-primary);
}

.upload-card,
.result-card,
.history-card {
  margin-bottom: var(--spacing-lg);
}

.upload-zone {
  border: 2px dashed var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-xl);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: var(--color-bg-secondary);
}

.upload-zone:hover,
.upload-zone.is-dragover {
  border-color: var(--color-primary);
  background-color: rgba(103, 194, 58, 0.05);
}

.upload-text {
  margin-top: var(--spacing-md);
  font-size: 16px;
  color: var(--color-text-primary);
}

.link-text {
  color: var(--color-primary);
  font-weight: 500;
}

.upload-hint {
  margin-top: var(--spacing-sm);
  font-size: 12px;
  color: var(--color-text-secondary);
}

.preview-section {
  margin-top: var(--spacing-lg);
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.action-bar {
  margin-top: var(--spacing-lg);
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-content {
  display: flex;
  gap: var(--spacing-xl);
  align-items: center;
}

.confidence-circle {
  flex-shrink: 0;
}

.confidence-value {
  font-size: 24px;
  font-weight: bold;
  fill: var(--color-primary);
}

.confidence-label {
  font-size: 12px;
  fill: var(--color-text-secondary);
}

.result-info {
  flex: 1;
}

.info-item {
  margin-bottom: var(--spacing-md);
  font-size: 14px;
}

.info-label {
  color: var(--color-text-secondary);
  margin-right: var(--spacing-sm);
}

.info-value {
  color: var(--color-text-primary);
  font-weight: 500;
}

.pagination-wrapper {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .detection-page {
    padding: var(--spacing-md);
  }

  .result-content {
    flex-direction: column;
  }

  .upload-zone {
    padding: var(--spacing-lg);
  }

  .upload-text {
    font-size: 14px;
  }
}
</style>
