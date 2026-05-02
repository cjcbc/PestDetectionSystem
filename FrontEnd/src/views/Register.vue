<template>
  <div class="register-shell">
    <!-- 左侧品牌沉浸区 - 与登录页共用视觉 -->
    <aside class="register-hero">
      <div class="register-hero__overlay"></div>
      <div class="register-hero__content">
        <span class="register-hero__badge">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" width="28" height="28">
              <path d="M24 4C16 4 8 10 8 20c0 10 10 18 16 24 6-6 16-14 16-24C40 10 32 4 24 4z" fill="rgba(255,255,255,0.2)"/>
              <path d="M24 12c-3 0-8 3-8 9 0 5 5 11 8 14 3-3 8-9 8-14 0-6-5-9-8-9z" fill="#fff"/>
              <path d="M19 22c3-4 7-5 10-3" stroke="rgba(16,163,127,0.7)" stroke-width="2" stroke-linecap="round"/>
              <circle cx="33" cy="17" r="2.5" fill="rgba(255,255,255,0.7)"/>
            </svg>
          </span>
        <h1 class="register-hero__title">加入我们<br/>共建智慧农业</h1>
        <p class="register-hero__desc">注册后即可使用病虫害识别、AI 问答、交流论坛等全部功能。</p>
        <div class="register-hero__features">
          <div class="register-hero__feature">
            <div class="feature-dot"></div>
            <span>病虫害图像识别</span>
          </div>
          <div class="register-hero__feature">
            <div class="feature-dot"></div>
            <span>AI 专家在线问答</span>
          </div>
          <div class="register-hero__feature">
            <div class="feature-dot"></div>
            <span>农业社区经验分享</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧表单区 -->
    <main class="register-form-area">
      <div class="register-form-wrap">
        <div class="register-form-header">
          <h2 class="register-form-title">创建账号</h2>
          <p class="register-form-sub">填写以下信息完成注册</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleRegister"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="3-20个字符的字母、数字或下划线"
              maxlength="20"
              clearable
              size="large"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              type="email"
              clearable
              size="large"
            />
          </el-form-item>

          <el-form-item label="手机号（可选）" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="11位手机号"
              maxlength="11"
              clearable
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              placeholder="请输入密码（至少6位）"
              type="password"
              show-password
              clearable
              size="large"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              placeholder="请再次输入密码"
              type="password"
              show-password
              clearable
              size="large"
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="verificationCode">
            <div class="captcha-row">
              <el-input
                v-model="form.verificationCode"
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
                  :disabled="captcha.loading"
                  @click="loadVerificationCode"
                >
                  <img v-if="captcha.image" :src="captcha.image" alt="验证码" />
                  <span v-else>刷新</span>
                </button>
                <button
                  class="captcha-refresh-link"
                  type="button"
                  :disabled="captcha.loading"
                  @click="loadVerificationCode"
                >
                  看不清？点击刷新
                </button>
              </div>
            </div>
          </el-form-item>

          <el-button
            type="primary"
            class="register-submit"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form>

        <p class="register-link">
          已有账号？
          <router-link to="/login">点击登录</router-link>
        </p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getVerificationCode, register } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  verificationCodeId: '',
  verificationCode: ''
})

const captcha = reactive({
  image: '',
  loading: false
})

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void
) => {
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: false, trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '验证码不能为空', trigger: 'blur' }
  ]
}

async function loadVerificationCode() {
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

async function handleRegister() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      email: form.email,
      phone: form.phone,
      verificationCodeId: form.verificationCodeId,
      verificationCode: form.verificationCode
    })

    ElMessage.success('注册成功，请登录')
    router.push({ name: 'Login' })
  } catch (error) {
    console.error('注册失败:', error)
    await loadVerificationCode()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadVerificationCode()
})
</script>

<style scoped>
.register-shell {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
}

/* ====== 左侧品牌区 ====== */
.register-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg, #0d3b1e 0%, #14532d 40%, #1a7a3a 80%, #4ade80 100%);
  overflow: hidden;
}

.register-hero__overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(74, 222, 128, 0.2), transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(255, 255, 255, 0.08), transparent 40%);
  pointer-events: none;
}

.register-hero__content {
  position: relative;
  z-index: 1;
  padding: 60px 48px;
  color: #fff;
  max-width: 480px;
}

.register-hero__badge {
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

.register-hero__title {
  font-family: var(--font-display);
  font-size: clamp(32px, 4vw, 44px);
  font-weight: 900;
  line-height: 1.2;
  margin-bottom: 20px;
  letter-spacing: -0.02em;
}

.register-hero__desc {
  font-size: 16px;
  line-height: 1.7;
  opacity: 0.85;
  margin-bottom: 40px;
  max-width: 400px;
}

.register-hero__features {
  display: grid;
  gap: 16px;
}

.register-hero__feature {
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
.register-form-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background:
    radial-gradient(circle at top right, rgba(240, 253, 244, 0.7), transparent 40%),
    linear-gradient(180deg, #fefefe 0%, #f9fafb 100%);
}

.register-form-wrap {
  width: 100%;
  max-width: 420px;
}

.register-form-header {
  margin-bottom: 36px;
}

.register-form-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 900;
  color: var(--color-primary-dark);
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.register-form-sub {
  margin: 0;
  font-size: 15px;
  color: var(--color-text-secondary);
}

/* Input overrides */
.register-form-area :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  transition: box-shadow var(--transition-fast);
}

.register-form-area :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-gray-400) inset;
}

.register-form-area :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 3px rgba(26, 122, 58, 0.12) inset !important;
}

.register-form-area :deep(.el-input__inner) {
  height: 44px;
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

.register-submit {
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

.register-submit:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-brand);
}

.register-submit:active {
  transform: translateY(0);
}

.register-link {
  margin: 20px 0 0;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.register-link a {
  color: var(--color-primary);
  font-weight: 600;
}

/* ====== 响应式 ====== */
@media (max-width: 960px) {
  .register-shell {
    grid-template-columns: 1fr;
  }

  .register-hero {
    display: none;
  }

  .register-form-area {
    padding: 40px 24px;
    min-height: 100vh;
  }
}

@media (max-width: 640px) {
  .register-form-area {
    padding: 32px 16px;
  }

  .register-form-title {
    font-size: 26px;
  }
}
</style>
