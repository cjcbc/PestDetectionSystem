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

          <el-form-item label="验证码" prop="verificationCode">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.verificationCode"
                placeholder="请输入验证码"
                maxlength="4"
                clearable
                @keyup.enter="handleLogin"
              />
              <div class="captcha-actions">
                <button
                  class="captcha-image-button"
                  type="button"
                  :disabled="loginCaptcha.loading"
                  @click="loadVerificationCode('login')"
                >
                  <img v-if="loginCaptcha.image" :src="loginCaptcha.image" alt="验证码" />
                  <span v-else>刷新</span>
                </button>
                <button
                  class="captcha-refresh-link"
                  type="button"
                  :disabled="loginCaptcha.loading"
                  @click="loadVerificationCode('login')"
                >
                  看不清？点击刷新
                </button>
              </div>
            </div>
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

          <el-form-item label="验证码" prop="verificationCode">
            <div class="captcha-row">
              <el-input
                v-model="registerForm.verificationCode"
                placeholder="请输入验证码"
                maxlength="4"
                clearable
                @keyup.enter="handleRegister"
              />
              <div class="captcha-actions">
                <button
                  class="captcha-image-button"
                  type="button"
                  :disabled="registerCaptcha.loading"
                  @click="loadVerificationCode('register')"
                >
                  <img v-if="registerCaptcha.image" :src="registerCaptcha.image" alt="验证码" />
                  <span v-else>刷新</span>
                </button>
                <button
                  class="captcha-refresh-link"
                  type="button"
                  :disabled="registerCaptcha.loading"
                  @click="loadVerificationCode('register')"
                >
                  看不清？点击刷新
                </button>
              </div>
            </div>
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
import { getVerificationCode, login, register } from '@/api/user'
import { useAppStore } from '@/stores/app'
import { isValidEmail, isValidPhone, isValidUsername } from '@/utils/validators'
import type { LoginPayload, RegisterPayload } from '@/types/user'

// Props & Emits
const { modelValue } = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [modelValue: boolean]
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
  password: '',
  verificationCodeId: '',
  verificationCode: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  verificationCodeId: '',
  verificationCode: ''
})

const loginCaptcha = reactive({
  image: '',
  loading: false
})

const registerCaptcha = reactive({
  image: '',
  loading: false
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
  ],
  verificationCode: [
    {
      required: true,
      message: '验证码不能为空',
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
  ],
  verificationCode: [
    {
      required: true,
      message: '验证码不能为空',
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
async function loadVerificationCode(target: 'login' | 'register') {
  const captcha = target === 'login' ? loginCaptcha : registerCaptcha
  const form = target === 'login' ? loginForm : registerForm
  captcha.loading = true
  try {
    const response = await getVerificationCode()
    form.verificationCodeId = response.verificationCodeId
    form.verificationCode = ''
    captcha.image = response.image
  } catch {
    ElMessage.error('验证码加载失败')
  } finally {
    captcha.loading = false
  }
}

async function handleLogin() {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()

    isLoading.value = true
    const payload: LoginPayload = {
      account: loginForm.account,
      password: loginForm.password,
      verificationCodeId: loginForm.verificationCodeId,
      verificationCode: loginForm.verificationCode
    }

    const response = await login(payload)
    appStore.setUser(response, response.token)

    ElMessage.success('登录成功')
    visible.value = false
    emit('success')

    // 重置表单
    resetLoginForm()
  } catch {
    await loadVerificationCode('login')
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
      phone: registerForm.phone || undefined,
      verificationCodeId: registerForm.verificationCodeId,
      verificationCode: registerForm.verificationCode
    }

    await register(payload)
    ElMessage.success('注册成功，请登录')
    const registeredEmail = registerForm.email

    // 切换到登录标签
    activeTab.value = 'login'
    resetRegisterForm()

    // 自动填入邮箱
    loginForm.account = registeredEmail
    await loadVerificationCode('login')
  } catch {
    await loadVerificationCode('register')
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
  loginForm.verificationCodeId = ''
  loginForm.verificationCode = ''
  loginCaptcha.image = ''
  loginFormRef.value?.clearValidate()
}

function resetRegisterForm() {
  registerForm.username = ''
  registerForm.email = ''
  registerForm.phone = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.verificationCodeId = ''
  registerForm.verificationCode = ''
  registerCaptcha.image = ''
  registerFormRef.value?.clearValidate()
}

// 监听标签切换
watch(activeTab, async (newTab) => {
  if (newTab === 'login') {
    resetRegisterForm()
  } else {
    resetLoginForm()
  }
  if (visible.value) {
    await loadVerificationCode(newTab)
  }
})

watch(visible, async (newVisible) => {
  if (newVisible) {
    await loadVerificationCode(activeTab.value)
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

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 124px;
  gap: 10px;
  align-items: start;
}

.captcha-actions {
  display: grid;
  gap: 4px;
}

.captcha-image-button {
  width: 100%;
  height: 34px;
  padding: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: #fff;
  overflow: hidden;
  cursor: pointer;
}

.captcha-image-button:disabled {
  cursor: wait;
  opacity: 0.7;
}

.captcha-image-button img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-refresh-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  font-size: 12px;
  line-height: 16px;
  text-align: left;
}

.captcha-refresh-link:disabled {
  cursor: wait;
  opacity: 0.7;
}

:deep(.el-dialog__footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
