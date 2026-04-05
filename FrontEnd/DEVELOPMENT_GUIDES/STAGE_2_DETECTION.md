# 阶段二：图片识别诊断模块

## 🎯 阶段目标
实现植物图片上传、AI 识别、结果展示、历史记录查询功能。

**依赖**：阶段一（基础架构）| **工作量**：2-3 天 | **优先级**：P0

---

## 📋 核心需求

### 1. 图片识别 API 模块

**文件**：`src/api/detect.ts`

**需要实现的函数**：
```typescript
export interface DetectResult {
  id: number;
  userId: number;
  imageName: string;
  topLabel: string;           // 病害/虫害名称，如 'brown_spot'
  confidence: number;         // 0-1 的置信度
  createdTime: number;        // 毫秒级时间戳
}

export interface DetectPayload {
  imageBase64: string;        // 不含 'data:image/...;base64,' 前缀
}

// 发送图片识别请求
export function detect(payload: DetectPayload): Promise<DetectResult>

// 获取当前用户的识别历史记录
export function getDetectRecords(): Promise<DetectResult[]>
```

**后端 API 规范**：
- `POST /api/detect` - 识别图片
- `GET /api/detect/records` - 获取历史

---

### 2. Detection 页面组件

**文件**：`src/views/Detection.vue`

**功能需求**：

#### 上传区域
- 拖拽上传支持
- 点击打开文件选择器
- 显示选中图片的预览
- 文件校验：
  - 仅接受 image/* 类型
  - 文件大小限制（可设为 10MB）
- 上传按钮和加载态

#### 识别结果展示
- 输入框下方显示结果卡片
- 卡片内容：
  - 大型圆形进度条，显示置信度（百分比）
  - 病害/虫害名称（中文，需后端支持或前端映射）
  - 识别时间戳（格式：2026-04-05 14:30:00）
  - "查看历史"或"继续诊断"按钮
- 识别中时显示加载动画
- 识别失败时显示错误提示
- 识别成功显示一个“追问”选项，预留接口转到ai对话

#### 历史记录列表
- 表格展示（使用 Element Plus Table）
- 列：
  - 识别时间（时间戳格式化）
  - 病害名称
  - 置信度（百分比格式）
  - 操作（查看原图、删除等，可选）
- 分页：每页 10 条，支持翻页
- 无数据时显示空态提示
- 列表按时间倒序排列（新的在顶部）

---

### 3. 图片处理和 Base64 转换

**文件**：`src/utils/image.ts`

**需要实现的工具函数**：
```typescript
/**
 * 将 File 对象转换为 Base64 字符串（不含 data:image/... 前缀）
 */
export function fileToBase64(file: File): Promise<string>

/**
 * 验证文件是否为图片
 */
export function isImageFile(file: File): boolean

/**
 * 压缩图片（可选，用于优化文件大小）
 */
export function compressImage(file: File, quality: number = 0.8): Promise<Blob>

/**
 * 获取图片的宽高信息
 */
export function getImageDimensions(file: File): Promise<{ width: number; height: number }>
```

**实现要点**：
- 使用 FileReader API 转换 Base64
- 校验文件类型使用 MIME type 或 magic number
- Base64 输出时去掉 `data:image/jpeg;base64,` 前缀

---

### 4. Detection 页面状态和逻辑

**需要在组件中实现**：
```typescript
// Reactive data
const selectedFile = ref<File | null>(null)
const previewUrl = ref<string>('')  // 图片预览地址
const isLoading = ref(false)        // 识别中
const detectResult = ref<DetectResult | null>(null)
const records = ref<DetectResult[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

// Computed
const confidence = computed(() => {
  if (!detectResult.value) return 0
  return Math.round(detectResult.value.confidence * 100)
})

// Methods
async function handleFileSelect(file: File) {
  // 校验文件
  if (!isImageFile(file)) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 生成预览
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
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

async function loadRecords(page: number = 1) {
  try {
    const data = await getDetectRecords()
    records.value = data
  } catch (error) {
    ElMessage.error('加载历史记录失败')
  }
}

// Lifecycle
onMounted(() => {
  loadRecords()
})
```

---

### 5. 类型定义文件

**文件**：`src/types/detect.ts`

```typescript
export interface DetectResult {
  id: number;
  userId: number;
  imageName: string;
  topLabel: string;
  confidence: number;
  createdTime: number;
}

export interface DetectPayload {
  imageBase64: string;
}

export interface ImageDimensions {
  width: number;
  height: number;
}
```

---

### 6. Pinia 状态管理（可选）

**文件**：`src/stores/detection.ts`

如果需要跨页面共享识别历史，可创建：
```typescript
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

  return { records, lastResult, isLoading, fetchRecords }
})
```

---

### 7. 样式和主题

**需要实现的样式**：
- 上传区域：虚线边框、拖拽状态下的高亮效果
- 结果卡片：圆形进度条（可用 SVG 或 CSS）、绿色主题
- 表格：Element Plus 默认样式，可自定义表头背景为浅绿
- 响应式：移动端上传区完整可见，表格可横向滚动

---

## 🏗️ 项目结构

```
src/
├── api/
│   └── detect.ts                   ✅ 识别 API 模块
├── views/
│   └── Detection.vue               ✅ 识别页面
├── stores/
│   └── detection.ts                ⏳ 识别状态（可选）
├── utils/
│   └── image.ts                    ✅ 图片处理工具
├── types/
│   └── detect.ts                   ✅ 识别相关类型
└── styles/
    └── detection.css               ✅ 识别页面样式
```

---

## 📝 验收标准

- [ ] Detection 页面可访问（`/detection`）
- [ ] 拖拽上传可用，点击选择文件可用
- [ ] 图片预览正确显示
- [ ] 文件校验生效（非图片文件被拒绝）
- [ ] 识别按钮可点击，触发 API 请求
- [ ] Base64 转换正确，后端能识别
- [ ] 识别结果正确显示（病害名、置信度、时间）
- [ ] 加载中显示 loading 动画
- [ ] 识别失败显示错误提示
- [ ] 历史记录列表可加载和翻页
- [ ] 时间戳正确格式化（2026-04-05 14:30:00）
- [ ] 页面响应式良好（手机上可用）

---

## ⚠️ 常见陷阱

1. **Base64 前缀**：确保发送给后端的 Base64 不含 `data:image/` 前缀
2. **文件读取**：FileReader 是异步的，需要用 Promise 包装
3. **图片预览**：使用 `URL.createObjectURL()` 后记得清理（`URL.revokeObjectURL()`）
4. **大文件上传**：超过 10MB 的文件会很慢，考虑压缩或限制尺寸
5. **时间戳单位**：后端返回毫秒级时间戳，JavaScript new Date() 也是毫秒级

---

## 🤝 与其他阶段的接口

- 本阶段可独立完成
- Home 页面已添加快速识别卡片，链接到此页面
- User 页面可添加"我的识别记录"快捷链接
