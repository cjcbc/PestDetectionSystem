<template>
  <div class="chat-page">
    <!-- 未登录状态提示 -->
    <div v-if="!userIsLoggedIn" class="auth-required-overlay">
      <el-empty description="AI 对话功能需要登录">
        <el-button type="primary" @click="goToLogin">立即登录</el-button>
      </el-empty>
    </div>

    <!-- 已登录的完整功能 -->
    <div v-else class="chat-shell">
      <aside class="chat-sidebar">
        <div class="chat-sidebar__header">
          <el-button type="primary" size="large" style="width: 100%" @click="openCreateDialog()">
            新建会话
          </el-button>
        </div>

        <div v-loading="chatStore.isSessionsLoading" class="chat-sidebar__list">
          <el-empty v-if="!chatStore.sessions.length" description="暂无会话，先新建一个吧" />

          <button
            v-for="session in chatStore.sessions"
            :key="session.id"
            type="button"
            class="chat-session-item"
            :class="{ 'is-active': session.id === activeSessionId }"
            @click="goToSession(session.id)"
          >
            <div>
              <h3 class="chat-session-item__title">{{ session.title || '新对话' }}</h3>
              <p class="chat-session-item__meta">
                {{ formatSessionTime(session.lastMessageAt) }} · {{ session.messageCount }} 条消息
              </p>
            </div>

            <el-popconfirm
              title="确认删除这个会话？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDeleteSession(session.id)"
            >
              <template #reference>
                <el-button text type="danger" @click.stop>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </button>
        </div>
      </aside>

      <section class="chat-main">
        <div class="chat-main__empty">
          <p class="chat-empty__eyebrow">AI Chat</p>
          <h1 class="chat-empty__title">欢迎来到农业 AI 顾问</h1>
          <p class="chat-empty__desc">
            你可以围绕病虫害识别、田间管理、农药使用和作物养护发起咨询。后端已核对，当前聊天接口支持会话管理、历史消息和额度统计。
          </p>

          <div class="chat-empty__actions">
            <button type="button" class="chat-entry-card" @click="openCreateDialog(detectionQuestion)">
              <h3 class="chat-entry-card__title">
                {{ detectionLabel ? `围绕 ${detectionLabel} 开始咨询` : '创建一个空白会话' }}
              </h3>
              <p class="chat-entry-card__desc">
                {{ detectionLabel ? '检测页传入的问题会作为首条消息自动发送。' : '先建会话，再进入对话页面继续提问。' }}
              </p>
            </button>

            <button type="button" class="chat-entry-card" @click="openCreateDialog(examplePrompts[0])">
              <h3 class="chat-entry-card__title">快速开始</h3>
              <p class="chat-entry-card__desc">直接带入示例问题创建会话并开始聊天。</p>
            </button>
          </div>

          <div class="chat-empty__prompts">
            <button
              v-for="prompt in promptCards"
              :key="prompt.title"
              type="button"
              class="chat-prompt-card"
              @click="createAndOpenWithMessage(prompt.message)"
            >
              <div>
                <h3 class="chat-prompt-card__title">{{ prompt.title }}</h3>
                <p class="chat-prompt-card__desc">{{ prompt.desc }}</p>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </div>
      </section>
    </div>

    <el-dialog v-model="dialogVisible" title="创建新会话" width="420px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="50" placeholder="留空则由首条消息自动生成" />
        </el-form-item>
        <el-form-item label="场景">
          <el-select v-model="form.scene" style="width: 100%">
            <el-option label="农业专家" value="expert" />
          </el-select>
        </el-form-item>
        <el-form-item label="首条消息">
          <el-input
            v-model="draftMessage"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            placeholder="可选。填写后会在创建成功后自动发送。"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreateSession">
          创建并进入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { isLoggedIn } from '@/utils/auth'
import { useChatStore } from '@/stores/chat'
import { formatSessionTime } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const chatStore = useChatStore()

const userIsLoggedIn = computed(() => isLoggedIn())

const dialogVisible = ref(false)
const submitting = ref(false)
const draftMessage = ref('')
const form = reactive({
  title: '',
  scene: 'expert'
})

const examplePrompts = [
  '水稻常见病虫害有哪些？',
  '如何防治稻瘟病？',
  '有机肥和化肥的区别是什么？'
]

const promptCards = [
  {
    title: '病虫害诊断',
    desc: '快速了解水稻、小麦、玉米等常见病虫害及识别要点。',
    message: examplePrompts[0]
  },
  {
    title: '防治方案',
    desc: '询问预防措施、推荐流程和田间管理建议。',
    message: examplePrompts[1]
  },
  {
    title: '种植管理',
    desc: '围绕施肥、灌溉和增产管理获取专业建议。',
    message: examplePrompts[2]
  }
]

const activeSessionId = computed(() => {
  const raw = route.params.sessionId
  if (!raw) {
    return null
  }

  return String(raw)
})

const detectionLabel = computed(() => String(route.query.detectionLabel || '').trim())
const detectionQuestion = computed(() => String(route.query.question || '').trim())
const detectionId = computed(() => {
  const raw = String(route.query.detectionId || '').trim()
  return raw && Number.isFinite(Number(raw)) ? raw : undefined
})

function openCreateDialog(message = '') {
  draftMessage.value = message
  dialogVisible.value = true
}

async function createAndOpenWithMessage(message: string) {
  draftMessage.value = message
  await handleCreateSession()
}

async function handleCreateSession() {
  // 检查是否登录
  if (!userIsLoggedIn.value) {
    ElMessage.warning('对话功能需要登录')
    router.push({ name: 'Login' })
    return
  }

  submitting.value = true

  try {
    const session = await chatStore.createNewSession({
      title: form.title.trim() || undefined,
      scene: form.scene
    })

    dialogVisible.value = false

    const query = {
      ...(detectionId.value ? { detectionId: String(detectionId.value) } : {}),
      ...(detectionLabel.value ? { detectionLabel: detectionLabel.value } : {})
    }

    await router.push({
      name: 'ChatDetail',
      params: { sessionId: session.id },
      query
    })

    if (draftMessage.value.trim()) {
      await chatStore.sendMsg(draftMessage.value.trim(), detectionId.value)
      draftMessage.value = ''
    }

    form.title = ''
    form.scene = 'expert'
  } catch {
    ElMessage.error('创建会话失败')
  } finally {
    submitting.value = false
  }
}

function goToSession(sessionId: string) {
  router.push({
    name: 'ChatDetail',
    params: { sessionId },
    query: route.query
  })
}

async function handleDeleteSession(sessionId: string) {
  // 检查是否登录
  if (!userIsLoggedIn.value) {
    ElMessage.warning('对话功能需要登录')
    router.push({ name: 'Login' })
    return
  }

  try {
    await chatStore.removeSession(sessionId)
    ElMessage.success('会话已删除')
  } catch {
    ElMessage.error('删除会话失败')
  }
}

function goToLogin() {
  router.push({ name: 'Login' })
}

onMounted(async () => {
  // 只在登录状态下才加载会话和额度信息
  if (userIsLoggedIn.value) {
    await Promise.all([chatStore.fetchSessions(), chatStore.fetchQuota()])
  }
})
</script>

<style scoped>
.auth-required-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.05) 0%, rgba(64, 158, 255, 0.05) 100%);
  border-radius: 8px;
  margin: 20px;
  padding: 40px;
}
</style>
