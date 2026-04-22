<template>
  <div class="create-post-container">
    <el-page-header content="发布帖子" @back="goBack" />

    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="100"
            show-word-limit
            placeholder="请输入帖子标题（5-100字）"
          />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="选择分类" style="width: 220px">
            <el-option label="资讯" value="资讯" />
            <el-option label="预警" value="预警" />
            <el-option label="技巧" value="技巧" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="可选，填写摘要便于列表展示"
          />
        </el-form-item>

        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="14"
            maxlength="20000"
            show-word-limit
            placeholder="请输入帖子正文，支持基础 HTML"
          />
        </el-form-item>

        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="可选，输入封面图片 URL" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="可选，多个标签用英文逗号分隔" />
        </el-form-item>

        <div class="actions">
          <el-button @click="saveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createPost } from '@/api/forum'
import type { CreatePostPayload } from '@/types/forum'

const router = useRouter()

const DRAFT_KEY = 'forum_create_post_draft'

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<CreatePostPayload>({
  title: '',
  category: '资讯',
  summary: '',
  content: '',
  coverImage: '',
  tags: ''
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应为 5-100 字', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [
    { required: true, message: '请输入正文', trigger: 'blur' },
    { min: 10, message: '正文至少 10 字', trigger: 'blur' }
  ]
}

restoreDraft()

function restoreDraft() {
  const raw = localStorage.getItem(DRAFT_KEY)
  if (!raw) return
  try {
    const draft = JSON.parse(raw) as Partial<CreatePostPayload>
    Object.assign(form, draft)
  } catch (error) {
    console.error(error)
  }
}

function saveDraft() {
  localStorage.setItem(DRAFT_KEY, JSON.stringify(form))
  ElMessage.success('草稿已保存')
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()

  submitting.value = true
  try {
    const post = await createPost(form)
    localStorage.removeItem(DRAFT_KEY)
    ElMessage.success('发布成功')
    router.push(`/forum/post/${post.id}`)
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/forum')
}
</script>

<style scoped>
.create-post-container {
  padding: var(--spacing-lg);
  display: grid;
  gap: 16px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
