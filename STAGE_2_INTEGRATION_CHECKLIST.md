# 阶段二（STAGE_2_DETECTION）前后端联调检查报告

**检查日期**：2026年4月5日  
**检查范围**：病虫害识别模块（图片上传、AI识别、结果展示、历史记录）

---

## 一、后端实现现状

### ✅ 已完成的后端功能

**控制器**：`PestDetectionController`
- [x] `POST /api/detect` - 病虫害识别
- [x] `GET /api/detect/records` - 获取历史记录

**服务实现**：`PestDetectionServiceImpl`
- [x] 图片保存到本地（`pest-images` 目录）
- [x] 调用 Flask 模型服务进行识别
- [x] 插入识别记录到数据库
- [x] 根据置信度设置 status（>= 0.7 为 1，< 0.7 为 0）
- [x] 获取用户的识别历史（仅返回 status=1 的记录）

**数据模型**：
- [x] `PestDetectionDTO` - 请求参数（imageBase64）
- [x] `PestDetectionVO` - 响应体
- [x] `PestDetection` 实体类

---

## 二、前端实现现状

### ❌ 缺失的前端功能

| 模块 | 文件 | 状态 | 优先级 |
|------|------|------|--------|
| **API 模块** | `src/api/detect.ts` | ❌ 不存在 | P0 |
| **工具函数** | `src/utils/image.ts` | ❌ 不存在 | P0 |
| **类型定义** | `src/types/detect.ts` | ❌ 不存在 | P0 |
| **页面实现** | `src/views/Detection.vue` | ⚠️ 占位符 | P0 |
| **状态管理** | `src/stores/detection.ts` | ❌ 不存在 | P1 |

**Detection.vue 当前状态**：
- 仅有占位符框架
- 无上传功能
- 无识别逻辑
- 无结果展示
- 无历史记录表格

---

## 三、前后端联调检查

### 🔴 关键问题

#### 问题 1：数据类型不匹配
**严重程度**：🔴 **高**

**问题描述**：
- 后端返回 `confidence` 类型为 `BigDecimal`
- 前端期望 `number`（用于计算百分比）

**后端响应示例**：
```json
{
  "confidence": 0.98    // 或可能序列化为 "0.98" (String)
}
```

**前端期望**：
```typescript
// src/types/detect.ts
export interface DetectResult {
  confidence: number;  // 0-1 之间的浮点数
}
```

**解决方案**：
- ✅ 后端自动序列化：Jackson 会将 BigDecimal 转为 JSON 数字
- ✅ 前端按浮点数处理：`Math.round(confidence * 100)` 得到百分比

**联调状态**：⚠️ 可能出现精度问题

---

#### 问题 2：置信度阈值逻辑差异
**严重程度**：🟡 **中**

**问题描述**：
- 后端：置信度 < 0.7 时，status = 0，不会在 getRecords 中返回
- 前端：期望返回所有识别结果，然后前端决定是否显示

**后端代码**：
```java
detection.setStatus(confidence >= CONFIDENCE_THRESHOLD ? 1 : 0);
```

**getRecords 查询**：
```java
wrapper.eq(PestDetection::getStatus, 1)
```

**影响**：
- 低置信度识别无法在历史记录中查看 ⚠️

**解决方案**：
1. **方案 A**（推荐）：后端仍存储，但 getRecords 支持过滤参数
   ```java
   // 加入过滤参数，如 includeAll=true
   @GetMapping("/records")
   public Result<List<PestDetectionVO>> getRecords(
     @RequestParam(defaultValue = "false") boolean includeAll
   )
   ```

2. **方案 B**：前端仅显示高置信度结果，设计上告知用户

**联调状态**：⚠️ 需要沟通确认需求

---

#### 问题 3：图片名称来源不一致
**严重程度**：🔵 **低**

**问题描述**：
- 后端返回 `imageName = flaskResult.get("image_name")` 或 "unknown"
- 前端可能期望返回上传时的文件名

**后端代码**：
```java
String imageName = (String) flaskResult.getOrDefault("image_name", "unknown");
```

**联调状态**：✅ 可接受

---

### 🟢 兼容的部分

#### API 路由正确
- 后端：`POST /api/detect`（含 context-path）
- 设置：Spring Boot context-path = `/api`
- 实际路径：`/detect`（在 controller 层）
- 完整 URL：`http://localhost:8888/api/detect` ✅

#### 请求参数格式正确
```json
{
  "imageBase64": "<大型 base64 字符串，不含 data:image 前缀>"
}
```
✅ 与前端计划完全一致

#### 响应格式一致
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1743840000000,
    "userId": 123456789,
    "imageName": "unknown",
    "topLabel": "brown_spot",
    "confidence": 0.98,
    "createdTime": 1743840000000
  }
}
```
✅ 与阶段一的统一格式完全一致

---

## 四、前端实现 TODO 清单

### 🟠 必做项（P0）

#### 1. 创建 API 模块 - `src/api/detect.ts`
```typescript
import request from './request'
import type { DetectResult, DetectPayload } from '@/types/detect'

/**
 * 发送图片识别请求
 */
export function detect(payload: DetectPayload): Promise<DetectResult> {
  return request.post('/detect', payload)
}

/**
 * 获取当前用户的识别历史记录
 */
export function getDetectRecords(includeAll?: boolean): Promise<DetectResult[]> {
  return request.get('/detect/records', {
    params: { includeAll: includeAll ?? false }
  })
}
```

**关键点**：
- ✅ POST 到 `/detect`（request.ts 会自动加 baseURL）
- ✅ GET `/detect/records` 
- ✅ 支持 includeAll 参数（可选，需后端支持）

---

#### 2. 创建类型定义 - `src/types/detect.ts`
```typescript
export interface DetectPayload {
  imageBase64: string;  // 不含 'data:image/...;base64,' 前缀
}

export interface DetectResult {
  id: number;
  userId: number;
  imageName: string;        // 文件名
  topLabel: string;         // 病害标签，如 'brown_spot'
  confidence: number;       // 0-1 的置信度
  createdTime: number;      // 毫秒级时间戳
}

export interface ImageDimensions {
  width: number;
  height: number;
}
```

**注意**：
- ⚠️ `confidence` 为 number，后端序列化 BigDecimal 时 Jackson 自动转换

---

#### 3. 创建图片工具函数 - `src/utils/image.ts`
```typescript
/**
 * 将 File 对象转换为 Base64 字符串（不含前缀）
 */
export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result as string
      // 去掉 'data:image/...;base64,' 前缀
      const base64 = result.split(',')[1]
      resolve(base64)
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

/**
 * 验证文件是否为图片
 */
export function isImageFile(file: File): boolean {
  return file.type.startsWith('image/')
}

/**
 * 获取图片的宽高信息
 */
export function getImageDimensions(file: File): Promise<{ width: number; height: number }> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve({ width: img.width, height: img.height })
    }
    
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('Failed to load image'))
    }
    
    img.src = url
  })
}

/**
 * 压缩图片（可选）
 */
export function compressImage(file: File, quality: number = 0.8): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    
    reader.onload = (event) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        canvas.width = img.width
        canvas.height = img.height
        const ctx = canvas.getContext('2d')
        ctx?.drawImage(img, 0, 0)
        
        canvas.toBlob(
          (blob) => {
            if (blob) resolve(blob)
            else reject(new Error('Compression failed'))
          },
          'image/jpeg',
          quality
        )
      }
      img.onerror = reject
      img.src = event.target?.result as string
    }
    
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}
```

---

#### 4. 实现 Detection.vue 页面
**需要的功能**：
- [ ] 拖拽上传区域
- [ ] 点击选择文件
- [ ] 图片预览
- [ ] 识别按钮和加载态
- [ ] 识别结果卡片（圆形进度条 + 病害名 + 时间 + 操作按钮）
- [ ] 历史记录表格
  - [ ] 表格列：时间、病害名、置信度、操作
  - [ ] 分页（每页 10 条）
  - [ ] 空态提示
  - [ ] 按时间倒序

**代码框架**：
```vue
<template>
  <div class="detection-page">
    <!-- 上传区 -->
    <div class="upload-zone">
      <!-- 拖拽上传 -->
      <div class="drop-area" @drop="handleDrop" @dragover.prevent @dragenter.prevent>
        <p>拖拽图片到此处或点击选择</p>
      </div>
      <input type="file" ref="fileInput" @change="handleFileSelect" accept="image/*" />
    </div>

    <!-- 图片预览 -->
    <div v-if="previewUrl" class="preview">
      <img :src="previewUrl" :alt="selectedFile?.name" />
    </div>

    <!-- 识别按钮 -->
    <el-button 
      @click="handleDetect" 
      :loading="isLoading"
      type="primary">
      识别诊断
    </el-button>

    <!-- 识别结果 -->
    <div v-if="detectResult" class="result-card">
      <!-- 圆形进度条 + 病害信息 -->
    </div>

    <!-- 历史记录表格 -->
    <el-table
      :data="paginatedRecords"
      stripe>
      <el-table-column prop="createdTime" label="识别时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="topLabel" label="病害名称" width="150" />
      <el-table-column prop="confidence" label="置信度" width="120">
        <template #default="{ row }">
          {{ (row.confidence * 100).toFixed(2) }}%
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { detect, getDetectRecords } from '@/api/detect'
import { fileToBase64, isImageFile } from '@/utils/image'
import type { DetectResult } from '@/types/detect'

// State
const selectedFile = ref<File | null>(null)
const previewUrl = ref<string>('')
const isLoading = ref(false)
const detectResult = ref<DetectResult | null>(null)
const records = ref<DetectResult[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

// Computed
const confidence = computed(() => {
  if (!detectResult.value) return 0
  return Math.round(detectResult.value.confidence * 100)
})

const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return records.value.slice(start, end)
})

// Methods
async function handleFileSelect(file?: File) {
  const targetFile = file || (fileInput.value?.files?.[0] as File)
  
  if (!targetFile) return
  
  if (!isImageFile(targetFile)) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  selectedFile.value = targetFile
  previewUrl.value = URL.createObjectURL(targetFile)
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  const files = e.dataTransfer?.files
  if (files?.length) {
    handleFileSelect(files[0] as File)
  }
}

async function handleDetect() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  try {
    isLoading.value = true
    const base64 = await fileToBase64(selectedFile.value)
    const result = await detect({ imageBase64: base64 })
    detectResult.value = result
    ElMessage.success('识别完成')
    
    // 刷新历史记录
    await loadRecords()
  } catch (error) {
    ElMessage.error('识别失败，请重试')
  } finally {
    isLoading.value = false
  }
}

async function loadRecords() {
  try {
    records.value = await getDetectRecords()
  } catch (error) {
    ElMessage.error('加载历史记录失败')
  }
}

function formatTime(timestamp: number): string {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

// Lifecycle
onMounted(() => {
  loadRecords()
})
</script>
```

---

### 🟡 可选项（P1）

#### 5. 创建状态管理 - `src/stores/detection.ts`
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDetectRecords } from '@/api/detect'
import type { DetectResult } from '@/types/detect'

export const useDetectionStore = defineStore('detection', () => {
  const records = ref<DetectResult[]>([])
  const lastResult = ref<DetectResult | null>(null)
  const isLoading = ref(false)

  async function fetchRecords() {
    isLoading.value = true
    try {
      records.value = await getDetectRecords()
    } finally {
      isLoading.value = false
    }
  }

  function setLastResult(result: DetectResult) {
    lastResult.value = result
  }

  return { records, lastResult, isLoading, fetchRecords, setLastResult }
})
```

---

## 五、联调检查清单

### 前置条件
- [ ] **后端已启动**：`http://localhost:8888`
- [ ] **Flask 模型服务已启动**：（后端依赖）
- [ ] **前端已启动**：`http://localhost:3000`
- [ ] **用户已登录**：登录页 → 获取 token

### 接口测试

#### ✅ POST /api/detect
**预期行为**：
1. 上传图片 Base64
2. 后端保存图片到 `pest-images` 目录
3. 调用 Flask 模型识别
4. 返回识别结果

**测试命令**：
```bash
curl -X POST http://localhost:8888/api/detect \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "imageBase64": "<base64-string-without-prefix>"
  }'
```

**预期响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1743840000000,
    "userId": 123456789,
    "imageName": "unknown",
    "topLabel": "brown_spot",
    "confidence": 0.98,
    "createdTime": 1743840000000
  }
}
```

#### ✅ GET /api/detect/records
**预期行为**：
1. 获取当前用户的识别历史
2. 仅返回 status=1 的记录（置信度 >= 0.7）
3. 按时间倒序排列

**测试命令**：
```bash
curl -X GET http://localhost:8888/api/detect/records \
  -H "Authorization: Bearer <token>"
```

**预期响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1743840000000,
      "userId": 123456789,
      "imageName": "unknown",
      "topLabel": "brown_spot",
      "confidence": 0.98,
      "createdTime": 1743840000000
    }
  ]
}
```

---

## 六、已知注意事项

### 🔴 高优先级问题

1. **置信度阈值**（需确认）
   - 当前后端过滤 status < 0.7 的记录
   - 前端是否需要看到低置信度结果？
   - 建议：后端支持 `includeAll` 参数让前端自己决定

2. **Python Flask 模型服务**
   - 后端代码假设 Flask 服务运行在某个地址
   - 需要确认 Flask 服务是否正确部署和配置

### 🟡 中等优先级问题

3. **图片大小限制**
   - Base64 可能很大，建议限制文件大小（10MB）
   - 考虑前端压缩大图片

4. **成功标准**（置信度）
   - 0.7 是硬编码的，无法动态调整
   - 可考虑配置化

### 🟢 低优先级问题

5. **病害名称国际化**
   - `topLabel` 返回英文（如 `brown_spot`）
   - 考虑前端是否需要中文映射

---

## 七、实现建议

### 分阶段实现

**第一阶段（核心）**：
1. ✅ 完成 `detect.ts` API 模块
2. ✅ 完成 `image.ts` 工具函数
3. ✅ 完成 `detect.ts` 类型定义
4. ✅ 实现 Detection.vue 的上传和识别功能
5. ✅ 测试与后端的联调

**第二阶段（完善）**：
1. 添加历史记录表格分页
2. 实现状态管理（detection.ts）
3. 优化图片压缩逻辑
4. 添加错误重试机制

### 测试用例

| 功能 | 测试步骤 | 预期结果 |
|------|---------|--------|
| 图片上传 | 1. 选择图片 2. 显示预览 | 预览图正确显示 ✅ |
| Base64 转换 | 调用 fileToBase64() | 返回不含前缀的 Base64 ✅ |
| API 调用 | 发送请求到 /api/detect | 返回识别结果 ✅ |
| 结果展示 | 解析 detectResult | 显示病害名和置信度百分比 ✅ |
| 历史记录 | 调用 getDetectRecords | 表格显示历史记录 ✅ |

---

## 八、总结

### ✅ 后端准备就绪
- 控制器、服务、持久层完整实现
- 与前端 API 契约一致
- 支持图片识别和历史查询

### ⚠️ 前端需要补充
| 模块 | 优先级 | 工作量 |
|------|--------|--------|
| API 模块 | P0 | 1h |
| 类型定义 | P0 | 0.5h |
| 工具函数 | P0 | 1h |
| Detection.vue 实现 | P0 | 2-3h |
| 样式和优化 | P1 | 1h |
| 状态管理 | P1 | 0.5h |

### 🎯 下一步行动
1. [ ] 创建所有缺失的前端文件
2. [ ] 实现 Detection.vue 的核心功能
3. [ ] 本地测试图片上传和识别流程
4. [ ] 与后端进行联调测试
5. [ ] 解决置信度阈值问题（后端协议）

---

**当前进度**：🔴 **未开始实现**  
**预计完成时间**：2-3 天  
**下一里程碑**：STAGE_2 完成并通过联调测试
