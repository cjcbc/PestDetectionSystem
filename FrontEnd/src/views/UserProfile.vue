<template>
  <div class="user-profile-container">
    <el-row :gutter="24">
      <!-- 左侧：用户信息卡片 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="user-info-card">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <img 
                v-if="userInfo?.image" 
                :src="userInfo.image" 
                alt="头像"
                class="avatar"
              />
              <div v-else class="avatar-placeholder">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="upload-overlay">
                <label for="avatar-input" class="upload-btn">
                  <i class="el-icon-camera"></i>
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
              <el-progress type="circle" :percentage="uploadProgress"></el-progress>
            </div>
          </div>

          <div class="user-info-content">
            <div class="info-item">
              <span class="label">用户名</span>
              <span class="value">{{ userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别</span>
              <span class="value">{{ sexToText(userInfo?.sex) }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ userInfo?.email || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机</span>
              <span class="value">{{ userInfo?.phone ? maskPhone(userInfo.phone) : '未绑定' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：编辑表单 -->
      <el-col :xs="24" :sm="24" :md="16">
        <el-card class="edit-form-card">
          <template #header>
            <div class="card-header">
              <span>编辑个人信息</span>
            </div>
          </template>

          <!-- 用户名编辑 -->
          <div class="form-section">
            <div class="section-title">
              <i class="el-icon-edit"></i>
              <span>用户名</span>
            </div>
            <div class="form-group">
              <el-input 
                v-model="editForm.username"
                placeholder="请输入新用户名（3-20个字符）"
                maxlength="20"
                clearable
              >
                <template #suffix>
                  <span class="char-count">{{ editForm.username.length }}/20</span>
                </template>
              </el-input>
              <span v-if="usernameError" class="field-error">{{ usernameError }}</span>
            </div>
          </div>

          <el-divider></el-divider>

          <!-- 性别编辑 -->
          <div class="form-section">
            <div class="section-title">
              <i class="el-icon-edit"></i>
              <span>性别</span>
            </div>
            <div class="form-group">
              <el-select v-model="editForm.sex" placeholder="请选择性别" clearable>
                <el-option label="女" :value="0"></el-option>
                <el-option label="男" :value="1"></el-option>
                <el-option label="未知" :value="2"></el-option>
              </el-select>
              <span v-if="sexError" class="field-error">{{ sexError }}</span>
            </div>
          </div>

          <el-divider></el-divider>

          <!-- 邮箱和手机编辑 -->
          <div class="form-section">
            <div class="section-title">
              <i class="el-icon-edit"></i>
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
                  ></el-input>
                  <span v-if="emailError" class="field-error">{{ emailError }}</span>
                </div>
              </div>
              <div class="field-row">
                <div class="field">
                  <label>手机号</label>
                  <el-input 
                    v-model="editForm.phone"
                    placeholder="请输入手机号"
                    clearable
                  ></el-input>
                  <span v-if="phoneError" class="field-error">{{ phoneError }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 统一保存按钮 -->
          <div class="form-actions-bottom">
            <el-button type="primary" size="large" @click="saveAllChanges" :loading="isSaving">
              保存修改
            </el-button>
            <el-button @click="showChangePasswordModal = true">
              修改密码
            </el-button>
            <span v-if="generalError" class="error-msg">{{ generalError }}</span>
          </div>
        </el-card>
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
import { useAppStore } from '@/stores/app'
import { 
  getUserInfo, 
  getAdminInfo,
  updateUsername, 
  updateSex, 
  updateAvatar, 
  bindPhoneEmail 
} from '@/api/user'
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

const appStore = useAppStore()

// 修改密码对话框
const showChangePasswordModal = ref(false)

// 用户信息
const userInfo = computed(() => appStore.userInfo)

// 编辑表单
const editForm = reactive({
  username: '',
  sex: 2 as 0 | 1 | 2,
  email: '',
  phone: ''
})

// 原始表单值（用于追踪变更）
const originForm = reactive({
  username: '',
  sex: 2 as 0 | 1 | 2,
  email: '',
  phone: ''
})

// 保存状态
const isSaving = ref(false)

const isUploading = ref(false)
const uploadProgress = ref(0)

// 错误信息
const usernameError = ref('')
const sexError = ref('')
const emailError = ref('')
const phoneError = ref('')
const generalError = ref('')

// 初始化
onMounted(() => {
  loadUserInfo()
})

async function loadUserInfo() {
  try {
    // 根据用户角色调用不同的接口
    const info = isAdmin() ? await getAdminInfo() : await getUserInfo()
    appStore.setUserInfo(info)
    
    editForm.username = info.username
    editForm.sex = info.sex
    editForm.email = info.email || ''
    editForm.phone = info.phone || ''
    
    // 更新原始值
    originForm.username = info.username
    originForm.sex = info.sex
    originForm.email = info.email || ''
    originForm.phone = info.phone || ''
  } catch (error) {
    ElMessage.error('加载用户信息失败')
    console.error(error)
  }
}

// 统一保存所有修改
async function saveAllChanges() {
  // 清空所有错误
  usernameError.value = ''
  sexError.value = ''
  emailError.value = ''
  phoneError.value = ''
  generalError.value = ''

  // 检查是否有任何修改
  const isUsernameChanged = editForm.username !== originForm.username
  const isSexChanged = editForm.sex !== originForm.sex
  const isEmailChanged = editForm.email !== originForm.email
  const isPhoneChanged = editForm.phone !== originForm.phone

  if (!isUsernameChanged && !isSexChanged && !isEmailChanged && !isPhoneChanged) {
    generalError.value = '请先修改内容'
    return
  }

  // 验证用户名
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

  // 验证邮箱
  if (isEmailChanged && editForm.email && !isValidEmail(editForm.email)) {
    emailError.value = '邮箱格式不正确'
    return
  }

  // 验证手机号
  if (isPhoneChanged && editForm.phone && !isValidPhone(editForm.phone)) {
    phoneError.value = '手机号格式不正确'
    return
  }

  // 如果email和phone都被修改了，需要至少有一个值
  if ((isEmailChanged || isPhoneChanged) && !editForm.email.trim() && !editForm.phone.trim()) {
    generalError.value = '邮箱和手机号不能同时为空'
    return
  }

  try {
    isSaving.value = true

    // 分别调用相应的接口
    if (isUsernameChanged) {
      await updateUsername(editForm.username)
    }

    if (isSexChanged) {
      await updateSex(editForm.sex)
    }

    if (isEmailChanged || isPhoneChanged) {
      // 判断bindType
      let bindType: 'PHONE' | 'EMAIL' | 'BOTH' = 'BOTH'
      
      if (isEmailChanged && !isPhoneChanged) {
        // 只修改了邮箱
        bindType = 'EMAIL'
      } else if (isPhoneChanged && !isEmailChanged) {
        // 只修改了手机号
        bindType = 'PHONE'
      } else if (isEmailChanged && isPhoneChanged) {
        // 两个都修改了
        bindType = 'BOTH'
      }

      await bindPhoneEmail({
        phone: editForm.phone || undefined,
        email: editForm.email || undefined,
        bindType
      })
    }

    // 刷新用户信息
    await loadUserInfo()
    ElMessage.success('信息修改成功')
  } catch (error: any) {
    const errorMsg = error?.response?.data?.message || '修改失败'
    generalError.value = errorMsg
    ElMessage.error(errorMsg)
  } finally {
    isSaving.value = false
  }
}

// 上传头像
async function handleAvatarChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  // 清空错误
  const target = event.target as HTMLInputElement
  target.value = ''

  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }

  // 校验文件大小
  if (!isFileSizeValid(file, 5)) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  // 上传
  try {
    isUploading.value = true
    uploadProgress.value = 0
    
    // 模拟进度
    const progressInterval = setInterval(() => {
      uploadProgress.value = Math.min(uploadProgress.value + Math.random() * 30, 90)
    }, 300)
    
    await updateAvatar(file)
    clearInterval(progressInterval)
    uploadProgress.value = 100
    
    // 刷新用户信息
    await loadUserInfo()
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '上传失败')
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

// 处理密码修改成功
function handlePasswordChangeSuccess() {
  ElMessage.success('密码修改成功，请重新登录')
  // 可选：清除登录状态并跳转到登录页面
  // appStore.logout()
  // router.push('/login')
}
</script>

<style scoped>
.user-profile-container {
  padding: var(--spacing-lg, 24px);
  min-height: 100vh;
  background: var(--el-bg-color, #f5f7fa);
}

/* 用户信息卡片 */
.user-info-card {
  position: sticky;
  top: 20px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  position: relative;
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  width: 128px;
  height: 128px;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--el-color-primary, #409eff);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--el-bg-color-page, #f5f7fa);
  border: 3px solid var(--el-border-color, #dcdfe6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: var(--el-text-color-placeholder, #a8abb2);
}

.upload-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0 0 50% 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .upload-overlay {
  opacity: 1;
}

.upload-btn {
  color: white;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.upload-progress {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 20px;
}

.user-info-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.label {
  color: var(--el-text-color-regular, #606266);
  font-weight: 500;
}

.value {
  color: var(--el-text-color-primary, #303133);
  word-break: break-all;
}

/* 编辑表单卡片 */
.edit-form-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: var(--el-text-color-primary, #303133);
}

.section-title i {
  font-size: 18px;
  color: var(--el-color-primary, #409eff);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-group :deep(.el-input),
.form-group :deep(.el-select) {
  width: 100%;
}

.char-count {
  font-size: 12px;
  color: var(--el-text-color-placeholder, #a8abb2);
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-actions .el-button {
  min-width: 80px;
}

.form-actions-bottom {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color, #dcdfe6);
}

.error-msg {
  font-size: 12px;
  color: var(--el-color-danger, #f56c6c);
}

.field-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
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
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-regular, #606266);
}

.field-error {
  font-size: 12px;
  color: var(--el-color-danger, #f56c6c);
}

/* 响应式 */
@media (max-width: 768px) {
  .user-profile-container {
    padding: var(--spacing-md, 16px);
  }

  .user-info-card {
    position: static;
    margin-bottom: 16px;
  }

  .form-section {
    margin-bottom: 16px;
  }
}
</style>
