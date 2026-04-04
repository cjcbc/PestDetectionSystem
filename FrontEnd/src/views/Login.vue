<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2 class="login-title">系统登录</h2>
        <p class="login-subtitle">欢迎使用病虫害检测系统</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="密码登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            size="large"
            class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>
            <el-form-item>
              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="账号注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            size="large"
            class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次确认密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

// 登录表单
const loginFormRef = ref<FormInstance>()
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

// 注册表单
const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const registerRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      // 模拟登录请求
      setTimeout(() => {
        loading.value = false
        localStorage.setItem('token', 'dummy-token') // 模拟保存 token
        ElMessage.success('登录成功')
        router.push('/')
      }, 1000)
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      // 模拟注册请求
      setTimeout(() => {
        loading.value = false
        ElMessage.success('注册成功，请登录')
        activeTab.value = 'login'
        loginForm.username = registerForm.username
        registerFormRef.value?.resetFields()
      }, 1000)
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  background-image: url('data:image/svg+xml,%3Csvg width="60" height="60" viewBox="0 0 60 60" xmlns="http://www.w3.org/2000/svg"%3E%3Cg fill="none" fill-rule="evenodd"%3E%3Cg fill="%239C92AC" fill-opacity="0.05"%3E%3Cpath d="M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z"/%3E%3C/g%3E%3C/g%3E%3C/svg%3E');
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  margin: 0 0 12px;
  font-size: 28px;
  font-weight: 600;
  color: #1f2f3d;
  letter-spacing: 1px;
}

.login-subtitle {
  margin: 0;
  font-size: 15px;
  color: #909399;
}

.login-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: #ebeef5;
  }
  :deep(.el-tabs__item) {
    font-size: 16px;
    height: 50px;
    line-height: 50px;
    color: #606266;
    font-weight: 500;
  }
  :deep(.el-tabs__item.is-active) {
    color: var(--el-color-primary);
    font-weight: 600;
  }
  :deep(.el-tabs__active-bar) {
    height: 3px;
    border-radius: 3px;
  }
}

.login-form {
  margin-top: 32px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 1px 15px;
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

:deep(.el-input__inner) {
  height: 44px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  margin-top: -4px;
}

.submit-btn {
  width: 100%;
  font-size: 16px;
  height: 44px;
  border-radius: 6px;
  font-weight: 500;
  letter-spacing: 2px;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
}
</style>
