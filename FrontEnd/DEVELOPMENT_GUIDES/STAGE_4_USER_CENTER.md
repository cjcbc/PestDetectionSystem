# 阶段四：用户中心

## 🎯 阶段目标
实现用户资料查看、头像上传、个人信息编辑、账号绑定等功能。

**依赖**：阶段一 | **工作量**：1-2 天 | **优先级**：P1

---

## 📋 核心需求

### 1. 用户中心 API 接口

**文件**：`src/api/user.ts`（扩展阶段一的接口）

已实现：`login()`、`register()`、`getUserInfo()`

新增函数：
```typescript
/**
 * 修改用户名
 */
export function updateUsername(username: string): Promise<UserVO>

/**
 * 修改性别
 * @param sex 0=女, 1=男, 2=未知
 */
export function updateSex(sex: 0 | 1 | 2): Promise<UserVO>

/**
 * 上传头像
 * @param file 图片文件，仅支持 image/* 类型，≤ 5MB
 */
export function uploadAvatar(file: File): Promise<{ code: 200; message: string }>

/**
 * 绑定手机或邮箱
 */
export interface BindPayload {
  phone?: string;
  email?: string;
  bindType: 'PHONE' | 'EMAIL' | 'BOTH';
}

export function bindPhoneEmail(payload: BindPayload): Promise<{ code: 200; message: string }>

/**
 * 获取用户信息（刷新）
 */
export function fetchUserInfo(): Promise<UserVO>
```

**后端 API 规范**：
```
PATCH /api/user/username?username=new_name
PATCH /api/user/sex?sex=1
POST /api/user/avatar (multipart/form-data, file)
PATCH /api/user/bind (JSON body)
GET /api/user/info
```

---

### 2. 用户信息类型定义

**文件**：`src/types/user.ts`（已有，确保完整）

```typescript
export interface UserVO {
  id: string;
  role: 0 | 1;           // 0=管理员, 1=普通用户
  username: string;
  email: string;
  phone: string;
  sex: 0 | 1 | 2;        // 0=女, 1=男, 2=未知
  image: string;         // Base64 Data URI
  status: number;        // 用户状态
  flag: number;
  token?: string;        // 登录时包含
}

export interface BindPayload {
  phone?: string;
  email?: string;
  bindType: 'PHONE' | 'EMAIL' | 'BOTH';
}
```

---

### 3. UserProfile 页面

**文件**：`src/views/UserProfile.vue`

**功能需求**：

#### 用户信息卡片（展示区）
- 左侧大头像（圆形，128x128px）
- 右侧用户信息：
  - 用户名（点击可编辑）
  - 性别（0/1/2 转换为中文）
  - 邮箱（脱敏显示或完整显示）
  - 手机（脱敏显示，如 138****0000）
  - 注册时间（格式化）
  - 编辑按钮

#### 编辑表单区
- 用户名输入框
  - 校验：长度 3-20，仅 letters/digits/underscore
  - 验证：不能重复（可选）
- 性别选择下拉框：女 / 男 / 未知
- 邮箱输入框
  - 校验：邮箱格式
  - 需绑定时调用绑定 API
- 手机输入框
  - 校验：11 位数字，13/14/15/16/17/18/19 开头
  - 需绑定时调用绑定 API
- 各字段独立保存按钮
- 修改成功提示（ElMessage.success）
- 修改失败显示错误信息

#### 头像上传和管理
- 点击头像或上传按钮打开文件选择器
- 文件验证：
  - 仅接受 image/* 类型
  - 文件大小 ≤ 5MB
  - 验证失败显示错误码提示（40050/40051/40052）
- 上传前预览（optional：可简单显示，也可支持裁剪）
- 上传中显示 loading
- 上传成功后：
  - 更新本地展示的头像
  - 更新全局 store 的用户信息
  - 显示成功提示
- 上传失败显示错误

---

### 4. 表单校验工具

**文件**：`src/utils/validators.ts`（已有，确保完整）

```typescript
/**
 * 校验邮箱格式
 */
export function isValidEmail(email: string): boolean {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

/**
 * 校验手机号（中国）
 */
export function isValidPhone(phone: string): boolean {
  const re = /^1[3-9]\d{9}$/
  return re.test(phone)
}

/**
 * 校验用户名
 */
export function isValidUsername(username: string): boolean {
  // 长度 3-20，仅允许字母、数字、下划线
  const re = /^[a-zA-Z0-9_]{3,20}$/
  return re.test(username)
}

/**
 * 校验文件大小
 */
export function isFileSizeValid(file: File, maxSizeMB: number = 5): boolean {
  return file.size <= maxSizeMB * 1024 * 1024
}

/**
 * 脱敏手机号
 */
export function maskPhone(phone: string): string {
  return phone.slice(0, 3) + '****' + phone.slice(7)
}
```

---

### 5. 页面逻辑和状态

**需要在 UserProfile.vue 中实现**：
```typescript
import { ref, reactive, onMounted, computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'

const appStore = useAppStore()

// 用户信息
const userInfo = computed(() => appStore.userInfo)

// 编辑表单
const editForm = reactive({
  username: '',
  sex: 2,
  email: '',
  phone: ''
})

// 编辑状态
const isEditingUsername = ref(false)
const isEditingEmail = ref(false)
const isEditingPhone = ref(false)
const isUploading = ref(false)

// 初始化
onMounted(() => {
  loadUserInfo()
})

async function loadUserInfo() {
  try {
    const info = await fetchUserInfo()
    appStore.setUserInfo(info)
    
    editForm.username = info.username
    editForm.sex = info.sex
    editForm.email = info.email
    editForm.phone = info.phone
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

// 保存用户名
async function saveUsername() {
  if (!isValidUsername(editForm.username)) {
    ElMessage.error('用户名格式不正确')
    return
  }
  
  try {
    const info = await updateUsername(editForm.username)
    appStore.setUserInfo(info)
    ElMessage.success('用户名修改成功')
    isEditingUsername.value = false
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

// 保存性别
async function saveSex() {
  try {
    const info = await updateSex(editForm.sex)
    appStore.setUserInfo(info)
    ElMessage.success('性别修改成功')
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

// 上传头像
async function handleAvatarChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  // 校验
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }

  if (!isFileSizeValid(file, 5)) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  // 上传
  try {
    isUploading.value = true
    await uploadAvatar(file)
    
    // 刷新用户信息
    await loadUserInfo()
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    isUploading.value = false
  }
}

// 绑定邮箱或手机
async function bindContact() {
  const payload = {
    phone: editForm.phone || undefined,
    email: editForm.email || undefined,
    bindType: 'BOTH'
  }

  // 至少选一个
  if (!payload.phone && !payload.email) {
    ElMessage.warning('请至少输入邮箱或手机')
    return
  }

  try {
    await bindPhoneEmail(payload)
    ElMessage.success('绑定成功')
    await loadUserInfo()
  } catch (error) {
    ElMessage.error('绑定失败')
  }
}
```

---

### 6. 样式和主题

**需要实现的样式**：
- 头像：圆形，128x128px，有边框或阴影
- 卡片：圆角 8px，浅色背景，轻微阴影
- 表单：标签左对齐，输入框占满右侧
- 按钮：绿色主题，hover 状态变深
- 响应式：移动端编辑字段纵向排列

---

## 🏗️ 项目结构

```
src/
├── api/
│   └── user.ts                     ✅ 用户 API 模块
├── views/
│   └── UserProfile.vue             ✅ 用户中心页
├── utils/
│   ├── validators.ts               ✅ 表单校验工具
│   └── image.ts                    ✅ 图片处理（已有）
├── types/
│   └── user.ts                     ✅ 用户类型定义
└── styles/
    └── user.css                    ✅ 用户中心样式
```

---

## 📝 验收标准

- [ ] UserProfile 页面可访问（`/user`）
- [ ] 用户信息正确加载并展示
- [ ] 头像正确显示（Base64 转 Data URI）
- [ ] 用户名编辑和保存成功
- [ ] 性别修改成功
- [ ] 邮箱和手机信息显示或编辑正确
- [ ] 头像上传成功，页面头像立即更新
- [ ] 文件验证生效（非图片、超过 5MB 被拒绝）
- [ ] 上传失败显示错误信息
- [ ] 页面响应式良好

---

## ⚠️ 常见陷阱

1. **Base64 转 Data URI**：确保 Base64 前缀格式正确（`data:image/jpeg;base64,...`）
2. **头像更新**：上传后需同时更新本地和全局 store
3. **表单校验**：逐字段校验，避免混乱提示
4. **并发编辑**：多字段编辑时避免状态冲突

---

## 🤝 与其他阶段接口

- 检测页面可添加"编辑资料"快捷链接到此页面
- 对话页面的用户菜单可链接到此页面
