<template>
  <div class="login-shell">
    <!-- 左侧品牌沉浸区 -->
    <aside class="login-hero">
      <div class="login-hero__overlay"></div>
      <div class="login-hero__content">
        <span class="login-hero__badge">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" width="28" height="28">
              <path d="M24 4C16 4 8 10 8 20c0 10 10 18 16 24 6-6 16-14 16-24C40 10 32 4 24 4z" fill="rgba(255,255,255,0.2)"/>
              <path d="M24 12c-3 0-8 3-8 9 0 5 5 11 8 14 3-3 8-9 8-14 0-6-5-9-8-9z" fill="#fff"/>
              <path d="M19 22c3-4 7-5 10-3" stroke="rgba(16,163,127,0.7)" stroke-width="2" stroke-linecap="round"/>
              <circle cx="33" cy="17" r="2.5" fill="rgba(255,255,255,0.7)"/>
            </svg>
          </span>
        <h1 class="login-hero__title">智慧农业<br/>守护每一片田</h1>
        <p class="login-hero__desc">基于深度学习的病虫害智能识别，结合 AI 问答与社区交流，为农业生产提供全方位技术支撑。</p>
        <div class="login-hero__features">
          <div class="login-hero__feature">
            <div class="feature-dot"></div>
            <span>病虫害图像识别</span>
          </div>
          <div class="login-hero__feature">
            <div class="feature-dot"></div>
            <span>AI 专家在线问答</span>
          </div>
          <div class="login-hero__feature">
            <div class="feature-dot"></div>
            <span>农业社区经验分享</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧表单区 -->
    <main class="login-form-area">
      <div class="login-form-wrap">
        <div class="login-form-header">
          <h2 class="login-form-title">{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
          <p class="login-form-sub">{{ activeTab === 'login' ? '登录以获取完整功能体验' : '注册后即可使用全部功能' }}</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs">
          <!-- 登录 -->
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
                  size="large"
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
                  size="large"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>

              <div class="login-options">
                <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
              </div>

              <el-form-item label="验证码" prop="verificationCode">
                <div class="captcha-row">
                  <el-input
                    v-model="loginForm.verificationCode"
                    placeholder="请输入验证码"
                    maxlength="4"
                    clearable
                    size="large"
                    @keyup.enter="handleLogin"
                  />
                  <div class="captcha-actions">
                    <button
                      class="captcha-image-button"
                      type="button"
                      :disabled="loginCaptcha.loading || captchaLimit.isLimited.value"
                      @click="loadVerificationCode('login')"
                    >
                      <img v-if="loginCaptcha.image" :src="loginCaptcha.image" alt="验证码" />
                      <span v-else>刷新</span>
                    </button>
                    <button
                      class="captcha-refresh-link"
                      type="button"
                      :disabled="loginCaptcha.loading || captchaLimit.isLimited.value"
                      @click="loadVerificationCode('login')"
                    >
                      {{ captchaButtonText }}
                    </button>
                  </div>
                </div>
              </el-form-item>

              <el-button
                type="primary"
                class="login-submit"
                :loading="loginLoading"
                :disabled="loginLimit.isLimited.value"
                @click="handleLogin"
              >
                {{ loginButtonText }}
              </el-button>
            </el-form>
          </el-tab-pane>

          <!-- 注册 -->
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
                  size="large"
                />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱"
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item label="手机号（可选）" prop="phone">
                <el-input
                  v-model="registerForm.phone"
                  placeholder="11位手机号"
                  maxlength="11"
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码（至少6位）"
                  show-password
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  show-password
                  clearable
                  size="large"
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
                    size="large"
                    @keyup.enter="handleRegister"
                  />
                  <div class="captcha-actions">
                    <button
                      class="captcha-image-button"
                      type="button"
                      :disabled="registerCaptcha.loading || captchaLimit.isLimited.value"
                      @click="loadVerificationCode('register')"
                    >
                      <img v-if="registerCaptcha.image" :src="registerCaptcha.image" alt="验证码" />
                      <span v-else>刷新</span>
                    </button>
                    <button
                      class="captcha-refresh-link"
                      type="button"
                      :disabled="registerCaptcha.loading || captchaLimit.isLimited.value"
                      @click="loadVerificationCode('register')"
                    >
                      {{ captchaButtonText }}
                    </button>
                  </div>
                </div>
              </el-form-item>

              <el-button
                type="primary"
                class="login-submit"
                :loading="registerLoading"
                :disabled="registerLimit.isLimited.value"
                @click="handleRegister"
              >
                {{ registerButtonText }}
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { getVerificationCode, login, register } from '@/api/user'
import { useAppStore } from '@/stores/app'
import { RATE_LIMIT_KEYS, useRateLimitCountdown } from '@/composables/useRateLimit'
import { isValidEmail, isValidPhone, isValidUsername } from '@/utils/validators'
import { clearRememberedLogin, getRememberedLogin, setRememberedLogin } from '@/utils/auth'
import type { LoginPayload, RegisterPayload } from '@/types/user'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()

const activeTab = ref<'login' | 'register'>('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const rememberPassword = ref(false)

const loginLimit = useRateLimitCountdown(RATE_LIMIT_KEYS.login)
const registerLimit = useRateLimitCountdown(RATE_LIMIT_KEYS.register)
const captchaLimit = useRateLimitCountdown(RATE_LIMIT_KEYS.verificationCode)

const captchaButtonText = computed(() =>
  captchaLimit.isLimited.value ? `请稍候 ${captchaLimit.remainingSeconds.value}s` : '看不清？点击刷新'
)
const loginButtonText = computed(() =>
  loginLimit.isLimited.value ? `请稍候 ${loginLimit.remainingSeconds.value}s` : '登 录'
)
const registerButtonText = computed(() =>
  registerLimit.isLimited.value ? `请稍候 ${registerLimit.remainingSeconds.value}s` : '注 册'
)

const loginFormRef = ref<InstanceType<typeof ElForm>>()
const registerFormRef = ref<InstanceType<typeof ElForm>>()

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

const loginRules = {
  account: [
    { required: true, message: '邮箱或手机号不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '验证码不能为空', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) { callback(); return }
        if (!isValidUsername(value)) {
          callback(new Error('用户名格式不正确（3-20个字符，仅支持字母、数字、下划线）'))
        } else { callback() }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value && !isValidEmail(value)) {
          callback(new Error('邮箱格式不正确'))
        } else { callback() }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) { callback(); return }
        if (!isValidPhone(value)) {
          callback(new Error('手机号格式不正确'))
        } else { callback() }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else { callback() }
      },
      trigger: 'blur'
    }
  ],
  verificationCode: [
    { required: true, message: '验证码不能为空', trigger: 'blur' }
  ]
}

async function loadVerificationCode(target: 'login' | 'register') {
  const captcha = target === 'login' ? loginCaptcha : registerCaptcha
  const form = target === 'login' ? loginForm : registerForm
  if (captchaLimit.isLimited.value) return

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
  if (loginLimit.isLimited.value) return

  try {
    await loginFormRef.value.validate()
  } catch { return }

  loginLoading.value = true
  try {
    const payload: LoginPayload = {
      account: loginForm.account,
      password: loginForm.password,
      verificationCodeId: loginForm.verificationCodeId,
      verificationCode: loginForm.verificationCode
    }
    const response = await login(payload)
    appStore.setUser(response, response.token)
    if (rememberPassword.value) {
      setRememberedLogin({ account: loginForm.account, password: loginForm.password })
    } else {
      clearRememberedLogin()
    }
    appStore.setPendingMessage('success', '登录成功')
    const redirect = route.query.redirect as string
    if (redirect && redirect.startsWith('/')) {
      await router.push(redirect)
    } else {
      await router.push('/')
    }
  } catch {
    await loadVerificationCode('login')
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  if (!registerFormRef.value) return
  if (registerLimit.isLimited.value) return

  try {
    await registerFormRef.value.validate()
  } catch { return }

  registerLoading.value = true
  try {
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
    activeTab.value = 'login'
    resetRegisterForm()
    loginForm.account = registeredEmail
    await loadVerificationCode('login')
  } catch {
    await loadVerificationCode('register')
  } finally {
    registerLoading.value = false
  }
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

function resetLoginForm() {
  loginForm.account = ''
  loginForm.password = ''
  loginForm.verificationCodeId = ''
  loginForm.verificationCode = ''
  loginCaptcha.image = ''
  loginFormRef.value?.clearValidate()
}

function loadRememberedLogin() {
  const rememberedLogin = getRememberedLogin()
  if (!rememberedLogin) return

  loginForm.account = rememberedLogin.account
  loginForm.password = rememberedLogin.password
  rememberPassword.value = true
}

watch(activeTab, async (newTab) => {
  if (newTab === 'login') {
    resetRegisterForm()
  } else {
    resetLoginForm()
  }
  await loadVerificationCode(activeTab.value)
})

onMounted(() => {
  loadRememberedLogin()
  loadVerificationCode(activeTab.value)
})
</script>

<style scoped>
.login-shell {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
}

/* ====== 左侧品牌区 ====== */
.login-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg, #0d3b1e 0%, #14532d 40%, #1a7a3a 80%, #4ade80 100%);
  overflow: hidden;
}

.login-hero__overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(74, 222, 128, 0.2), transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(255, 255, 255, 0.08), transparent 40%);
  pointer-events: none;
}

.login-hero__content {
  position: relative;
  z-index: 1;
  padding: 60px 48px;
  color: #fff;
  max-width: 480px;
}

.login-hero__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 900;
  letter-spacing: 0.08em;
  margin-bottom: 32px;
}

.login-hero__title {
  font-family: var(--font-display);
  font-size: clamp(32px, 4vw, 44px);
  font-weight: 900;
  line-height: 1.2;
  margin-bottom: 20px;
  letter-spacing: -0.02em;
}

.login-hero__desc {
  font-size: 16px;
  line-height: 1.7;
  opacity: 0.85;
  margin-bottom: 40px;
  max-width: 400px;
}

.login-hero__features {
  display: grid;
  gap: 16px;
}

.login-hero__feature {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 500;
  opacity: 0.9;
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary-light);
  box-shadow: 0 0 12px rgba(74, 222, 128, 0.5);
  flex-shrink: 0;
}

/* ====== 右侧表单区 ====== */
.login-form-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background:
    radial-gradient(circle at top right, rgba(240, 253, 244, 0.7), transparent 40%),
    linear-gradient(180deg, #fefefe 0%, #f9fafb 100%);
}

.login-form-wrap {
  width: 100%;
  max-width: 420px;
}

.login-form-header {
  margin-bottom: 36px;
}

.login-form-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 900;
  color: var(--color-primary-dark);
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.login-form-sub {
  margin: 0;
  font-size: 15px;
  color: var(--color-text-secondary);
}

/* Tabs styling */
.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--color-border);
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-tertiary);
  transition: color var(--transition-fast);
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

.login-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
  height: 3px;
  border-radius: 3px;
}

/* Input override */
.login-tabs :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  transition: box-shadow var(--transition-fast);
}

.login-tabs :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-gray-400) inset;
}

.login-tabs :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 3px rgba(26, 122, 58, 0.12) inset !important;
}

.login-tabs :deep(.el-input__inner) {
  height: 44px;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 124px;
  gap: 10px;
  align-items: start;
}

.login-options {
  display: flex;
  justify-content: flex-start;
  margin: -8px 0 16px;
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

.login-submit {
  width: 100%;
  height: 48px;
  margin-top: 12px;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.04em;
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast);
}

.login-submit:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-brand);
}

.login-submit:active {
  transform: translateY(0);
}

/* ====== 响应式：小屏隐藏左侧 ====== */
@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-hero {
    display: none;
  }

  .login-form-area {
    padding: 40px 24px;
    min-height: 100vh;
  }
}

@media (max-width: 640px) {
  .login-form-area {
    padding: 32px 16px;
  }

  .login-form-title {
    font-size: 26px;
  }
}
</style>
