<template>
  <div class="user-profile-container">
    <div class="profile-header">
      <h2>个人信息</h2>
      <p class="subtitle">管理您的账户信息</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：用户信息卡片 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="user-info-card" shadow="hover">
          <div class="avatar-section">
            <div class="avatar-wrapper" :class="{ 'edit-mode': isEditMode }">
              <img
                v-if="userInfo?.image"
                :src="userInfo.image"
                :alt="currentUserName"
                class="avatar"
              />
              <div v-else class="avatar-placeholder">
                <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="60" cy="60" r="60" fill="url(#default-avatar-profile)"/>
                  <circle cx="60" cy="44" r="18" fill="#fff" opacity="0.9"/>
                  <ellipse cx="60" cy="92" rx="30" ry="20" fill="#fff" opacity="0.9"/>
                  <defs><linearGradient id="default-avatar-profile" x1="0" y1="0" x2="120" y2="120"><stop stop-color="#67c23a"/><stop offset="1" stop-color="#409eff"/></linearGradient></defs>
                </svg>
              </div>
              <div v-if="isEditMode" class="upload-overlay">
                <label for="avatar-input" class="upload-btn">
                  <el-icon><Camera /></el-icon>
                  <span>更换</span>
                </label>
                <input
                  id="avatar-input"
                  type="file"
                  accept="image/*"
                  @change="handleAvatarChange"
                  style="display: none"
                />
              </div>
            </div>
            <div v-if="isUploading" class="upload-progress">
              <el-progress type="circle" :percentage="uploadProgress" :width="80" />
            </div>
          </div>

          <div class="user-nickname">
            <span class="nickname">{{ currentUserName }}</span>
          </div>

          <div class="user-role-badge">
            <span class="role-tag" :class="isAdminUser ? 'role-admin' : 'role-user'">
              {{ isAdminUser ? '管理员' : '普通用户' }}
            </span>
          </div>

          <div class="user-info-content">
            <div class="info-item">
              <span class="label">
                <el-icon><User /></el-icon>
                用户名
              </span>
              <span class="value">{{ userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">
                <el-icon><Male v-if="userInfo?.sex === 1" /><Female v-else-if="userInfo?.sex === 0" /><QuestionFilled v-else /></el-icon>
                性别
              </span>
              <span class="value">{{ sexToText(userInfo?.sex) }}</span>
            </div>
            <div class="info-item">
              <span class="label">
                <el-icon><Message /></el-icon>
                邮箱
              </span>
              <span class="value">{{ userInfo?.email || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <span class="label">
                <el-icon><Phone /></el-icon>
                手机
              </span>
              <span class="value">{{ userInfo?.phone ? maskPhone(userInfo.phone) : '未绑定' }}</span>
            </div>
          </div>

          <div class="card-actions">
            <el-button type="primary" @click="enterEditMode" v-if="!isEditMode" class="action-btn">
              <el-icon><Edit /></el-icon>
              修改信息
            </el-button>
            <el-button @click="showChangePasswordModal = true" :disabled="isEditMode" class="action-btn">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧 -->
      <el-col :xs="24" :sm="24" :md="16">
        <!-- 编辑表单 -->
        <el-card class="edit-form-card" shadow="hover" v-if="isEditMode">
          <template #header>
            <div class="card-header">
              <el-icon><Edit /></el-icon>
              <span>编辑个人信息</span>
            </div>
          </template>

          <div class="form-section">
            <div class="section-title">
              <el-icon><User /></el-icon>
              <span>用户名</span>
            </div>
            <div class="form-group">
              <el-input
                v-model="editForm.username"
                placeholder="请输入新用户名（3-20个字符）"
                maxlength="20"
                clearable
                :class="{ 'is-error': usernameError }"
              >
                <template #suffix>
                  <span class="char-count">{{ editForm.username.length }}/20</span>
                </template>
              </el-input>
              <span v-if="usernameError" class="field-error">
                <el-icon><Warning /></el-icon>
                {{ usernameError }}
              </span>
            </div>
          </div>

          <el-divider>
            <el-icon><DArrowRight /></el-icon>
          </el-divider>

          <div class="form-section">
            <div class="section-title">
              <el-icon><Male v-if="editForm.sex === 1" /><Female v-else-if="editForm.sex === 0" /><QuestionFilled v-else /></el-icon>
              <span>性别</span>
            </div>
            <div class="form-group">
              <el-radio-group v-model="editForm.sex" size="large">
                <el-radio-button :value="0">
                  <el-icon><Female /></el-icon> 女
                </el-radio-button>
                <el-radio-button :value="1">
                  <el-icon><Male /></el-icon> 男
                </el-radio-button>
                <el-radio-button :value="2">
                  <el-icon><QuestionFilled /></el-icon> 未知
                </el-radio-button>
              </el-radio-group>
              <span v-if="sexError" class="field-error">
                <el-icon><Warning /></el-icon>
                {{ sexError }}
              </span>
            </div>
          </div>

          <el-divider>
            <el-icon><DArrowRight /></el-icon>
          </el-divider>

          <div class="form-section">
            <div class="section-title">
              <el-icon><Message /></el-icon>
              <span>联系方式</span>
            </div>
            <div class="form-group">
              <div class="field-row">
                <div class="field">
                  <label>邮箱</label>
                  <el-input
                    v-model="editForm.email"
                    placeholder="请输入邮箱"
                    type="email"
                    clearable
                    :class="{ 'is-error': emailError }"
                  >
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                  </el-input>
                  <span v-if="emailError" class="field-error">
                    <el-icon><Warning /></el-icon>
                    {{ emailError }}
                  </span>
                </div>
                <div class="field">
                  <label>手机号</label>
                  <el-input
                    v-model="editForm.phone"
                    placeholder="请输入手机号"
                    clearable
                    :class="{ 'is-error': phoneError }"
                  >
                    <template #prefix>
                      <el-icon><Phone /></el-icon>
                    </template>
                  </el-input>
                  <span v-if="phoneError" class="field-error">
                    <el-icon><Warning /></el-icon>
                    {{ phoneError }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="form-actions-bottom">
            <el-button type="primary" size="large" @click="saveAllChanges" :loading="isSaving">
              <el-icon><Check /></el-icon>
              保存修改
            </el-button>
            <el-button size="large" @click="cancelEdit">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
            <span v-if="generalError" class="error-msg">
              <el-icon><Warning /></el-icon>
              {{ generalError }}
            </span>
          </div>
        </el-card>

        <!-- 普通用户：最近检测 + 快捷操作 -->
        <div v-else-if="!isAdminUser" class="right-content">
          <el-card class="recent-detections-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Compass /></el-icon>
                <span>最近检测</span>
              </div>
            </template>
            <div v-if="isLoadingDetections" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <div v-else-if="recentDetections.length === 0" class="empty-state">
              <el-icon class="empty-icon"><Document /></el-icon>
              <p>暂无检测记录</p>
              <el-button type="primary" size="small" @click="router.push('/detection')">
                去检测
              </el-button>
            </div>
            <div v-else class="detection-list">
              <div
                v-for="item in recentDetections"
                :key="item.id"
                class="detection-item"
                @click="router.push(`/detection/${item.id}`)"
              >
                <div class="detection-info">
                  <span class="pest-name">{{ item.topLabel }}</span>
                  <span class="confidence">置信度 {{ (item.confidence * 100).toFixed(1) }}%</span>
                </div>
                <div class="detection-meta">
                  <span class="time">{{ formatTime(item.createdTime) }}</span>
                  <el-icon class="arrow"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </el-card>

          <el-card class="quick-actions-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><ArrowRight /></el-icon>
                <span>快捷操作</span>
              </div>
            </template>
            <div class="action-grid">
              <div class="action-item" @click="router.push('/detection')">
                <div class="action-icon detection">
                  <el-icon><Compass /></el-icon>
                </div>
                <span class="action-label">病虫识别</span>
              </div>
              <div class="action-item" @click="router.push('/chat')">
                <div class="action-icon chat">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <span class="action-label">AI 问答</span>
              </div>
              <div class="action-item" @click="router.push('/forum')">
                <div class="action-icon forum">
                  <el-icon><Document /></el-icon>
                </div>
                <span class="action-label">交流论坛</span>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 管理员：管理后台入口 -->
        <div v-else class="right-content">
          <el-card class="admin-shortcuts-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Setting /></el-icon>
                <span>管理功能</span>
              </div>
            </template>
            <div class="admin-grid">
              <div class="admin-item" @click="router.push('/admin/users')">
                <div class="admin-icon users">
                  <el-icon><User /></el-icon>
                </div>
                <span class="admin-label">用户管理</span>
                <span class="admin-desc">管理用户账户与权限</span>
              </div>
              <div class="admin-item" @click="router.push('/admin/content')">
                <div class="admin-icon content">
                  <el-icon><Document /></el-icon>
                </div>
                <span class="admin-label">内容管理</span>
                <span class="admin-desc">审核论坛帖子与评论</span>
              </div>
              <div class="admin-item" @click="router.push('/admin/alerts')">
                <div class="admin-icon alerts">
                  <el-icon><Bell /></el-icon>
                </div>
                <span class="admin-label">告警管理</span>
                <span class="admin-desc">查看系统安全告警</span>
              </div>
              <div class="admin-item" @click="router.push('/admin/dashboard')">
                <div class="admin-icon dashboard">
                  <el-icon><DataLine /></el-icon>
                </div>
                <span class="admin-label">数据概览</span>
                <span class="admin-desc">查看系统运行状态</span>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- 修改密码对话框 -->
    <ChangePasswordModal
      v-model="showChangePasswordModal"
      :is-admin="isAdmin()"
      @success="handlePasswordChangeSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Male,
  Female,
  QuestionFilled,
  Message,
  Phone,
  Camera,
  Edit,
  Lock,
  Check,
  Close,
  Warning,
  DArrowRight,
  Compass,
  ChatDotRound,
  Document,
  ArrowRight,
  Loading,
  Setting,
  Bell,
  DataLine
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import {
  getUserInfo,
  getAdminInfo,
  updateUsername,
  updateSex,
  updateAvatar,
  bindPhoneEmail
} from '@/api/user'
import { getDetectRecords } from '@/api/detect'
import { isAdmin } from '@/utils/auth'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'
import {
  isValidUsername,
  isValidEmail,
  isValidPhone,
  isFileSizeValid,
  maskPhone,
  sexToText
} from '@/utils/validators'
import type { DetectResult } from '@/types/detect'

const router = useRouter()
const appStore = useAppStore()

const showChangePasswordModal = ref(false)
const isEditMode = ref(false)

const userInfo = computed(() => appStore.userInfo)
const isAdminUser = computed(() => isAdmin())
const currentUserName = computed(() => userInfo.value?.username || '用户')

const editForm = reactive({
  username: '',
  sex: 2 as 0 | 1 | 2,
  email: '',
  phone: ''
})

const originForm = reactive({
  username: '',
  sex: 2 as 0 | 1 | 2,
  email: '',
  phone: ''
})

const isSaving = ref(false)
const isUploading = ref(false)
const uploadProgress = ref(0)

const usernameError = ref('')
const sexError = ref('')
const emailError = ref('')
const phoneError = ref('')
const generalError = ref('')

const recentDetections = ref<DetectResult[]>([])
const isLoadingDetections = ref(false)

function formatTime(timestamp: number): string {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return `${date.getMonth() + 1}-${date.getDate()}`
}

async function loadRecentDetections() {
  try {
    isLoadingDetections.value = true
    const records = await getDetectRecords()
    recentDetections.value = records.slice(0, 4)
  } catch (error) {
    console.error('加载检测记录失败:', error)
  } finally {
    isLoadingDetections.value = false
  }
}

function enterEditMode() {
  editForm.username = userInfo.value?.username || ''
  editForm.sex = userInfo.value?.sex ?? 2
  editForm.email = userInfo.value?.email || ''
  editForm.phone = userInfo.value?.phone || ''
  originForm.username = userInfo.value?.username || ''
  originForm.sex = userInfo.value?.sex ?? 2
  originForm.email = userInfo.value?.email || ''
  originForm.phone = userInfo.value?.phone || ''
  isEditMode.value = true
}

function cancelEdit() {
  isEditMode.value = false
  clearAllErrors()
  editForm.username = originForm.username
  editForm.sex = originForm.sex
  editForm.email = originForm.email
  editForm.phone = originForm.phone
}

function clearAllErrors() {
  usernameError.value = ''
  sexError.value = ''
  emailError.value = ''
  phoneError.value = ''
  generalError.value = ''
}

onMounted(() => {
  loadUserInfo()
  loadRecentDetections()
})

async function loadUserInfo() {
  try {
    const info = isAdmin() ? await getAdminInfo() : await getUserInfo()
    appStore.setUserInfo(info)
    editForm.username = info.username
    editForm.sex = info.sex
    editForm.email = info.email || ''
    editForm.phone = info.phone || ''
    originForm.username = info.username
    originForm.sex = info.sex
    originForm.email = info.email || ''
    originForm.phone = info.phone || ''
  } catch (error) {
    ElMessage.error('加载用户信息失败')
    console.error(error)
  }
}

async function saveAllChanges() {
  clearAllErrors()
  const isUsernameChanged = editForm.username !== originForm.username
  const isSexChanged = editForm.sex !== originForm.sex
  const isEmailChanged = editForm.email !== originForm.email
  const isPhoneChanged = editForm.phone !== originForm.phone

  if (!isUsernameChanged && !isSexChanged && !isEmailChanged && !isPhoneChanged) {
    generalError.value = '请先修改内容'
    return
  }

  if (isUsernameChanged) {
    if (!editForm.username.trim()) {
      usernameError.value = '用户名不能为空'
      return
    }
    if (!isValidUsername(editForm.username)) {
      usernameError.value = '用户名格式不正确（3-20个字符，仅支持字母、数字、下划线）'
      return
    }
  }

  if (isEmailChanged && editForm.email && !isValidEmail(editForm.email)) {
    emailError.value = '邮箱格式不正确'
    return
  }

  if (isPhoneChanged && editForm.phone && !isValidPhone(editForm.phone)) {
    phoneError.value = '手机号格式不正确'
    return
  }

  if ((isEmailChanged || isPhoneChanged) && !editForm.email.trim() && !editForm.phone.trim()) {
    generalError.value = '邮箱和手机号不能同时为空'
    return
  }

  try {
    isSaving.value = true
    if (isUsernameChanged) await updateUsername(editForm.username)
    if (isSexChanged) await updateSex(editForm.sex)
    if (isEmailChanged || isPhoneChanged) {
      let bindType: 'PHONE' | 'EMAIL' | 'BOTH' = 'BOTH'
      if (isEmailChanged && !isPhoneChanged) bindType = 'EMAIL'
      else if (isPhoneChanged && !isEmailChanged) bindType = 'PHONE'
      await bindPhoneEmail({
        phone: editForm.phone || undefined,
        email: editForm.email || undefined,
        bindType
      })
    }
    await loadUserInfo()
    originForm.username = editForm.username
    originForm.sex = editForm.sex
    originForm.email = editForm.email
    originForm.phone = editForm.phone
    isEditMode.value = false
    ElMessage.success('信息修改成功')
  } catch (error: any) {
    const errorMsg = error?.response?.data?.message || '修改失败'
    generalError.value = errorMsg
    ElMessage.error(errorMsg)
  } finally {
    isSaving.value = false
  }
}

async function handleAvatarChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const target = event.target as HTMLInputElement
  target.value = ''
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (!isFileSizeValid(file, 5)) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  try {
    isUploading.value = true
    uploadProgress.value = 0
    const progressInterval = setInterval(() => {
      uploadProgress.value = Math.min(uploadProgress.value + Math.random() * 30, 90)
    }, 300)
    await updateAvatar(file)
    clearInterval(progressInterval)
    uploadProgress.value = 100
    await loadUserInfo()
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '上传失败')
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

function handlePasswordChangeSuccess() {
  ElMessage.success('密码修改成功，请重新登录')
}
</script>

<style scoped>
.user-profile-container {
  padding: 32px 24px;
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
}

.profile-header {
  margin-bottom: 32px;
  text-align: center;
}

.profile-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.profile-header .subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

.user-info-card {
  position: sticky;
  top: 20px;
  border-radius: 16px;
  overflow: hidden;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.avatar-wrapper.edit-mode {
  width: 140px;
  height: 140px;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--el-color-primary-light-5);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.avatar-wrapper:hover .avatar {
  transform: scale(1.02);
  border-color: var(--el-color-primary);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4px solid var(--el-border-color-lighter, #e4e8ed);
  overflow: hidden;
}

.avatar-placeholder svg {
  width: 100%;
  height: 100%;
  display: block;
}

.upload-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  border-radius: 0 0 50% 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-wrapper:hover .upload-overlay {
  opacity: 1;
}

.upload-btn {
  color: white;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.upload-progress {
  position: absolute;
  bottom: -50px;
  left: 50%;
  transform: translateX(-50%);
}

.user-nickname {
  text-align: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.user-nickname .nickname {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.user-role-badge {
  display: flex;
  justify-content: center;
  margin: 8px 0;
}

.role-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.role-tag.role-admin {
  background: rgba(167, 139, 250, 0.2);
  color: #7c3aed;
  border: 1px solid rgba(167, 139, 250, 0.4);
}

.role-tag.role-user {
  background: rgba(52, 211, 153, 0.15);
  color: #059669;
  border: 1px solid rgba(52, 211, 153, 0.3);
}

.user-info-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-regular);
  font-size: 14px;
  font-weight: 500;
}

.info-item .label .el-icon {
  font-size: 16px;
  color: var(--el-color-primary-light-3);
}

.info-item .value {
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-weight: 500;
}

.card-actions {
  display: flex;
  flex-direction: row;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.card-actions .action-btn {
  flex: 1;
  border-radius: 8px;
}

.card-actions .action-btn:first-child {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #fff;
}

.card-actions .action-btn:first-child:hover {
  background: var(--el-color-primary-light-1);
  border-color: var(--el-color-primary-light-1);
}

.edit-form-card {
  border-radius: 16px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

.edit-form-card .card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.edit-form-card .card-header .el-icon {
  font-size: 20px;
  color: var(--el-color-primary);
}

.form-section {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
}

.section-title .el-icon {
  font-size: 18px;
  color: var(--el-color-primary);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group :deep(.el-input),
.form-group :deep(.el-select),
.form-group :deep(.el-radio-group) {
  width: 100%;
}

.form-group :deep(.el-input.is-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset;
}

.char-count {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.form-actions-bottom {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.form-actions-bottom .el-button {
  border-radius: 8px;
  padding: 12px 28px;
}

.form-actions-bottom .el-button--primary {
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  border: none;
}

.form-actions-bottom .el-button--primary:hover {
  background: linear-gradient(135deg, var(--el-color-primary-light-3) 0%, var(--el-color-primary) 100%);
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-color-danger);
}

.field-error {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-color-danger);
  animation: shake 0.3s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

.field-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

@media (min-width: 768px) {
  .field-row {
    grid-template-columns: 1fr 1fr;
  }
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.right-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.recent-detections-card,
.quick-actions-card {
  border-radius: 16px;
}

.recent-detections-card .card-header,
.quick-actions-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.recent-detections-card .card-header .el-icon,
.quick-actions-card .card-header .el-icon {
  color: var(--el-color-primary);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  gap: 12px;
  color: var(--el-text-color-secondary);
}

.empty-icon {
  font-size: 48px;
  color: var(--el-text-color-placeholder);
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.detection-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detection-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.detection-item:hover {
  background: var(--el-color-primary-light-9);
  transform: translateX(4px);
}

.detection-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pest-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.confidence {
  font-size: 12px;
  color: var(--el-color-success);
}

.detection-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.arrow {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 12px;
  background: var(--el-fill-color-light);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: var(--el-color-primary-light-9);
  transform: translateY(-4px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.action-icon.detection {
  background: linear-gradient(135deg, var(--el-color-success) 0%, var(--el-color-success-light-3) 100%);
  color: #fff;
}

.action-icon.chat {
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  color: #fff;
}

.action-icon.forum {
  background: linear-gradient(135deg, var(--el-color-warning) 0%, var(--el-color-warning-light-3) 100%);
  color: #fff;
}

.action-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.admin-shortcuts-card {
  border-radius: 16px;
}

.admin-shortcuts-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.admin-shortcuts-card .card-header .el-icon {
  color: var(--el-color-primary);
}

.admin-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.admin-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 16px;
  background: var(--el-fill-color-light);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.admin-item:hover {
  background: var(--el-color-primary-light-9);
  transform: translateY(-4px);
}

.admin-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.admin-icon.users {
  background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%);
}

.admin-icon.content {
  background: linear-gradient(135deg, #059669 0%, #34d399 100%);
}

.admin-icon.alerts {
  background: linear-gradient(135deg, #dc2626 0%, #f87171 100%);
}

.admin-icon.dashboard {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
}

.admin-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.admin-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: 20px 16px;
  }

  .profile-header h2 {
    font-size: 24px;
  }

  .user-info-card {
    position: static;
    margin-bottom: 20px;
  }

  .right-content {
    gap: 16px;
  }

  .action-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .action-item {
    padding: 16px 8px;
  }

  .action-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .action-label {
    font-size: 12px;
  }

  .form-section {
    margin-bottom: 16px;
  }

  .form-actions-bottom {
    flex-direction: column;
    align-items: stretch;
  }

  .form-actions-bottom .el-button {
    width: 100%;
  }

  .admin-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .admin-item {
    padding: 16px 12px;
  }

  .admin-icon {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }

  .admin-label {
    font-size: 13px;
  }

  .admin-desc {
    font-size: 11px;
  }
}
</style>
