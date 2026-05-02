<template>
  <div class="admin-alerts-container">
    <h1>预警管理</h1>

    <el-card class="create-card">
      <template #header>发布预警</template>
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="地区">
              <el-input v-model="form.region" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="病虫害">
              <el-input v-model="form.pestName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="严重等级">
              <el-select v-model="form.severity" style="width: 100%">
                <el-option :value="1" label="低" />
                <el-option :value="2" label="中" />
                <el-option :value="3" label="高" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <div class="actions">
          <el-button type="primary" :loading="submitting" @click="submit">发布预警</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="list-header">
          <span>预警列表</span>
          <div class="filters">
            <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px" @change="reload">
              <el-option :value="1" label="上架" />
              <el-option :value="0" label="下架" />
            </el-select>
            <el-select v-model="query.severity" clearable placeholder="等级" style="width: 120px" @change="reload">
              <el-option :value="1" label="低" />
              <el-option :value="2" label="中" />
              <el-option :value="3" label="高" />
            </el-select>
            <el-input v-model="query.keyword" clearable placeholder="关键字" style="width: 220px" @keyup.enter="reload" />
            <el-button type="primary" @click="reload">筛选</el-button>
          </div>
        </div>
      </template>

      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="region" label="地区" min-width="120" />
        <el-table-column prop="pestName" label="病虫害" min-width="140" />
        <el-table-column label="等级" width="90">
          <template #default="{ row }">
            <el-tag :type="severityTagType(row.severity)">{{ severityText(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column label="发布时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          @current-change="changePage"
        />
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑预警" width="640px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="地区">
              <el-input v-model="editForm.region" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="病虫害">
              <el-input v-model="editForm.pestName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="严重等级">
              <el-select v-model="editForm.severity" style="width: 100%">
                <el-option :value="1" label="低" />
                <el-option :value="2" label="中" />
                <el-option :value="3" label="高" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editing" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createWarning,
  deleteWarning,
  getWarningList,
  toggleWarningStatus,
  updateWarning
} from '@/api/warning'
import type { CreateWarningPayload, WarningItem } from '@/types/warning'
import { isMessageHandled } from '@/api/request'

const loading = ref(false)
const submitting = ref(false)
const editing = ref(false)
const rows = ref<WarningItem[]>([])
const total = ref(0)
const editDialogVisible = ref(false)
const editingId = ref('')

const query = reactive({
  page: 1,
  pageSize: 10,
  status: undefined as 0 | 1 | undefined,
  severity: undefined as 1 | 2 | 3 | undefined,
  keyword: ''
})

const form = reactive<CreateWarningPayload>({
  title: '',
  content: '',
  region: '',
  pestName: '',
  severity: 2,
  status: 1
})

const editForm = reactive<CreateWarningPayload>({
  title: '',
  content: '',
  region: '',
  pestName: '',
  severity: 2,
  status: 1
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getWarningList({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status,
      severity: query.severity,
      keyword: query.keyword || undefined
    })
    rows.value = res.list
    total.value = res.total
  } catch (error) {
    console.error(error)
    if (!isMessageHandled(error)) {
      ElMessage.error('加载预警失败')
    }
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }

  submitting.value = true
  try {
    await createWarning({
      ...form,
      title: form.title.trim(),
      content: form.content.trim(),
      region: form.region?.trim() || undefined,
      pestName: form.pestName?.trim() || undefined
    })
    ElMessage.success('预警发布成功')
    form.title = ''
    form.content = ''
    form.region = ''
    form.pestName = ''
    form.severity = 2
    form.status = 1
    await fetchList()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

function openEdit(row: WarningItem) {
  editingId.value = row.id
  editForm.title = row.title
  editForm.content = row.content
  editForm.region = row.region || ''
  editForm.pestName = row.pestName || ''
  editForm.severity = row.severity
  editForm.status = row.status
  editDialogVisible.value = true
}

async function submitEdit() {
  if (!editForm.title.trim() || !editForm.content.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }

  editing.value = true
  try {
    await updateWarning(editingId.value, {
      ...editForm,
      title: editForm.title.trim(),
      content: editForm.content.trim(),
      region: editForm.region?.trim() || undefined,
      pestName: editForm.pestName?.trim() || undefined
    })
    ElMessage.success('预警更新成功')
    editDialogVisible.value = false
    await fetchList()
  } catch (error) {
    console.error(error)
  } finally {
    editing.value = false
  }
}

async function handleToggleStatus(row: WarningItem) {
  try {
    await toggleWarningStatus(row.id)
    ElMessage.success(row.status === 1 ? '预警已下架' : '预警已上架')
    await fetchList()
  } catch (error) {
    console.error(error)
  }
}

async function handleDelete(row: WarningItem) {
  try {
    await ElMessageBox.confirm(`确认删除预警“${row.title}”吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteWarning(row.id)
    ElMessage.success('预警已删除')
    await fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

function reload() {
  query.page = 1
  fetchList()
}

function changePage(p: number) {
  query.page = p
  fetchList()
}

function severityText(value: 1 | 2 | 3) {
  if (value === 3) return '高'
  if (value === 2) return '中'
  return '低'
}

function severityTagType(value: 1 | 2 | 3): 'danger' | 'warning' | 'success' {
  if (value === 3) return 'danger'
  if (value === 2) return 'warning'
  return 'success'
}

function formatTime(time: number) {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(fetchList)
</script>

<style scoped>
.admin-alerts-container {
  padding: var(--spacing-lg);
  display: grid;
  gap: 16px;
}

h1 {
  margin: 0;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: center;
}
</style>
