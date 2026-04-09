<template>
  <el-dialog
    v-model="visible"
    :title="activeTab === 'login' ? '登 录' : '注 册'"
    width="420px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <!-- 标签切换 -->
    <el-tabs v-model="activeTab" class="auth-tabs">
      <!-- 登录标签 -->
      <el-tab-pane label="登 录" name="login">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="邮箱或手机号" prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="请输入邮箱或手机号"
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-footer">
            <el-link type="primary" href="#" disabled>忘记密码?</el-link>
          </div>
        </el-form>
      </el-tab-pane>

      <!-- 注册标签 -->
      <el-tab-pane label="注 册" name="register">
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
          @keyup.enter="handleRegister"
        >
          <el-form-item label="用户名（可选）" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="3-20个字符"
              maxlength="20"
              clearable
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              clearable
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item label="手机号（可选）" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="11位手机号"
              maxlength="11"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              clearable
              @keyup.enter="handleRegister"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="handleClose">再看看</el-button>
      <el-button
        type="primary"
        @click="activeTab === 'login' ? handleLogin() : handleRegister()"
        :loading="isLoading"
      >
        {{ activeTab === 'login' ? '登 录' : '注 册' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { login, register } from '@/api/user'
import { useAppStore } from '@/stores/app'
import { isValidEmail, isValidPhone, isValidUsername } from '@/utils/validators'
import type { LoginPayload, RegisterPayload } from '@/types/user'

// Props & Emits
const { modelValue } = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  update: [modelValue: boolean]
  success: []
}>()

// Store
const appStore = useAppStore()

// Refs
const loginFormRef = ref<InstanceType<typeof ElForm>>()
const registerFormRef = ref<InstanceType<typeof ElForm>>()

// 本地状态
const activeTab = ref<'login' | 'register'>('login')
const isLoading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

// 验证规则
const loginRules = {
  account: [
    {
      required: true,
      message: '邮箱或手机号不能为空',
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: '密码不能为空',
      trigger: 'blur'
    },
    {
      min: 6,
      message: '密码至少6个字符',
      trigger: 'blur'
    }
  ]
}

const registerRules = {
  username: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) {
          // 用户名可选
          callback()
        } else if (!isValidUsername(value)) {
          callback(new Error('用户名格式不正确（3-20个字符，仅支持字母、数字、下划线）'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      required: true,
      message: '邮箱不能为空',
      trigger: 'blur'
    },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value && !isValidEmail(value)) {
          callback(new Error('邮箱格式不正确'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) {
          // 手机号可选
          callback()
        } else if (!isValidPhone(value)) {
          callback(new Error('手机号格式不正确'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: '密码不能为空',
      trigger: 'blur'
    },
    {
      min: 6,
      message: '密码至少6个字符',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: '请确认密码',
      trigger: 'blur'
    },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// Computed
const visible = computed({
  get: () => modelValue,
  set: (val) => emit('update:modelValue', val)
})

// Methods
async function handleLogin() {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()

    isLoading.value = true
    const payload: LoginPayload = {
      account: loginForm.account,
      password: loginForm.password
    }

    const response = await login(payload)
    appStore.setUser(response, response.token)

    ElMessage.success('登录成功')
    visible.value = false
    emit('success')

    // 重置表单
    resetLoginForm()
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('登录失败，请检查输入')
    }
  } finally {
    isLoading.value = false
  }
}

async function handleRegister() {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()

    isLoading.value = true
    const payload: RegisterPayload = {
      username: registerForm.username || undefined,
      password: registerForm.password,
      email: registerForm.email,
      phone: registerForm.phone || undefined
    }

    await register(payload)
    ElMessage.success('注册成功，请登录')

    // 切换到登录标签
    activeTab.value = 'login'
    resetRegisterForm()

    // 自动填入邮箱
    loginForm.account = registerForm.email
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('注册失败，请重试')
    }
  } finally {
    isLoading.value = false
  }
}

function handleClose() {
  visible.value = false
}

function resetLoginForm() {
  loginForm.account = ''
  loginForm.password = ''
  loginFormRef.value?.clearValidate()
}

function resetRegisterForm() {
  registerForm.username = ''
  registerForm.email = ''
  registerForm.phone = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerFormRef.value?.clearValidate()
}

// 监听标签切换
watch(activeTab, (newTab) => {
  if (newTab === 'login') {
    resetLoginForm()
  } else {
    resetRegisterForm()
  }
})
</script>

<style scoped>
.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

:deep(.el-dialog__footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
