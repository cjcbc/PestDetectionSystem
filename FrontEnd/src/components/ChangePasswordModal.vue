<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="450px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入原密码"
          show-password
          clearable
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码（至少8个字符）"
          show-password
          clearable
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          clearable
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { changeUserPassword, changeAdminPassword } from '@/api/user'
import type { ChangePasswordPayload } from '@/types/user'

interface Props {
  modelValue: boolean
  isAdmin?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  isAdmin: false
})

const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePasswordMatch = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入确认密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    { validator: validatePasswordMatch, trigger: 'blur' }
  ]
}

const visible = ref(false)

const handleClose = () => {
  emit('update:modelValue', false)
  formRef.value?.clearValidate()
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    loading.value = true
    const payload: ChangePasswordPayload = {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    }

    if (props.isAdmin) {
      await changeAdminPassword(payload)
    } else {
      await changeUserPassword(payload)
    }

    ElMessage.success('密码修改成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('密码修改失败')
    }
  } finally {
    loading.value = false
  }
}

// 监听 props 中的 modelValue 变化
import { watch } from 'vue'
watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal
  }
)

// 监听 visible 变化，同步到 props
watch(visible, (newVal) => {
  if (!newVal) {
    handleClose()
  }
})
</script>
