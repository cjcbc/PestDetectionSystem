<template>
  <div class="register-container">
    <el-card class="register-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">注册账户</span>
          <p>创建一个新账号后即可进入系统栏目页</p>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" type="email" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            type="password"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
          注册
        </el-button>

        <p class="login-link">
          已有账号？
          <router-link to="/login">点击登录</router-link>
        </p>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }

  if (value.length < 6) {
    callback(new Error('密码长度不能少于 6 位'))
    return
  }

  callback()
}

const validateConfirmPassword = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void
) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }

  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
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
  phone: [{ required: false, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
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
      phone: form.phone
    })

    ElMessage.success('注册成功，请登录')
    router.push({ name: 'Login' })
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at top, rgba(46, 204, 113, 0.22), transparent 35%),
    linear-gradient(180deg, #f4fbf5 0%, #ffffff 100%);
}

.register-card {
  width: 100%;
  max-width: 460px;
  border: none;
  border-radius: 24px;
  box-shadow: 0 24px 48px rgba(21, 128, 61, 0.12);
}

.card-header {
  text-align: center;
}

.card-header p {
  margin: 8px 0 0;
  color: var(--color-text-secondary);
}

.title {
  font-size: 26px;
  font-weight: 700;
  color: #166534;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
}

.login-link {
  margin: 18px 0 0;
  text-align: center;
  color: var(--color-text-secondary);
}
</style>
