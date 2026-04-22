# 🌾 病虫害识别与农业论坛系统 - 前端开发指南

## 📚 文档总览

本文件夹包含 **6 个分阶段的完整开发指南**，涵盖从基础架构到管理后台的所有前端功能模块。

### 文档列表

| 序号 | 阶段 | 文档 | 优先级 | 工期 | 重点 |
|-----|------|------|--------|------|------|
| 1️⃣ | 基础架构与认证 | [STAGE_1_FOUNDATION.md](./STAGE_1_FOUNDATION.md) | P0 | 2-3 天 | 阻塞所有后续功能 |
| 2️⃣ | 图片识别诊断 | [STAGE_2_DETECTION.md](./STAGE_2_DETECTION.md) | P0 | 2-3 天 | 核心功能，无依赖 |
| 3️⃣ | AI 对话系统 | [STAGE_3_CHAT.md](./STAGE_3_CHAT.md) | P1 | 3-4 天 | 多会话管理，消息交互 |
| 4️⃣ | 用户中心 | [STAGE_4_USER_CENTER.md](./STAGE_4_USER_CENTER.md) | P1 | 1-2 天 | 个人资料，头像上传 |
| 5️⃣ | 社区论坛 | [STAGE_5_FORUM.md](./STAGE_5_FORUM.md) | P2 | 4-5 天 | 富文本编辑，评论交互 |
| 6️⃣ | 管理后台 | [STAGE_6_ADMIN.md](./STAGE_6_ADMIN.md) | P3 | 2-3 天 | 权限控制，内容管理 |
| 7️⃣ | 论坛互动增强与预警中心 | [STAGE_7_INTERACTION_AND_WARNING.md](./STAGE_7_INTERACTION_AND_WARNING.md) | P1 | 4-6 天 | 评论、点赞、预警中心、后台预警联动 |

---

## 🎯 快速开始

### 推荐开发顺序

```
第一轮：并行开发
├─ STAGE_1 基础架构（必须先完成）→ 为其他章节提供基础

第二轮：核心功能并行
├─ STAGE_2 图片识别（无依赖，快速完成）
├─ STAGE_4 用户中心（无依赖，快速完成）
└─ 同时开始 STAGE_3 对话（依赖 STAGE_1）

第三轮：扩展功能
├─ STAGE_5 论坛（依赖 STAGE_1）
└─ STAGE_6 管理（依赖 STAGE_1）

第四轮：联调与体验补强
├─ STAGE_7 论坛互动增强（依赖 STAGE_5 占位页）
└─ STAGE_7 预警中心（依赖 STAGE_6 管理入口）
```

### 每个阶段的验证清单

每份文档都包含 **验收标准** 部分，完成后按清单逐项验证：

```
- [ ] 功能实现完整
- [ ] TypeScript 无报错
- [ ] 页面响应式良好
- [ ] API 对接成功
- [ ] 用户反馈提示完善
```

---

## 🏗️ 技术栈概览

### 核心框架
- **Vue 3** - 最新语法（`<script setup>`）
- **TypeScript** - 全项目类型安全
- **Vite** - 快速开发和构建
- **Element Plus** - UI 组件库

### 状态与请求
- **Pinia** - 全局状态管理
- **Axios** - HTTP 请求客户端
- **Vue Router 4** - 路由库

### 功能库（新增）
- **markdown-it** - Markdown 解析（对话、论坛）
- **highlight.js** - 代码高亮
- **wangeditor** - 富文本编辑器（论坛发帖）
- **dayjs** - 时间格式化
- **clipboard** - 复制到剪贴板

### 安装命令

```bash
# 基础依赖已安装，新增：
npm install markdown-it highlight.js wangeditor dayjs clipboard
```

---

## 📋 项目文件结构

开发完成后的完整结构：

```
FrontEnd/
├── DEVELOPMENT_GUIDES/          # 本文件夹（开发指南）
│   ├── README.md
│   ├── STAGE_1_FOUNDATION.md
│   ├── STAGE_2_DETECTION.md
│   ├── STAGE_3_CHAT.md
│   ├── STAGE_4_USER_CENTER.md
│   ├── STAGE_5_FORUM.md
│   ├── STAGE_6_ADMIN.md
│   └── STAGE_7_INTERACTION_AND_WARNING.md
│
├── src/
│   ├── api/
│   │   ├── request.ts            # Axios 实例 + 拦截器
│   │   ├── user.ts               # 用户接口
│   │   ├── detect.ts             # 识别接口
│   │   ├── chat.ts               # 对话接口
│   │   ├── forum.ts              # 论坛接口
│   │   └── admin.ts              # 管理接口
│   │
│   ├── stores/
│   │   ├── app.ts                # 全局状态
│   │   ├── chat.ts               # 对话状态
│   │   ├── detection.ts          # 识别状态
│   │   └── forum.ts              # 论坛状态
│   │
│   ├── router/
│   │   └── index.ts              # 路由定义 + 守卫
│   │
│   ├── views/
│   │   ├── Login.vue             # 登录页
│   │   ├── Register.vue          # 注册页
│   │   ├── Home.vue              # 首页
│   │   ├── Detection.vue         # 识别页
│   │   ├── Chat.vue              # 对话首页
│   │   ├── ChatDetail.vue        # 聊天页
│   │   ├── UserProfile.vue       # 用户中心
│   │   ├── Forum.vue             # 论坛首页
│   │   ├── PostDetail.vue        # 帖子详情
│   │   ├── CreatePost.vue        # 发布帖子
│   │   ├── 404.vue               # 404 页
│   │   └── admin/                # 管理后台
│   │       ├── AdminUsers.vue
│   │       ├── AdminContent.vue
│   │       └── AdminAlerts.vue
│   │
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Header.vue
│   │   │   └── Sidebar.vue
│   │   ├── common/
│   │   │   ├── ImageUpload.vue
│   │   │   ├── RichEditor.vue
│   │   │   └── MarkdownRenderer.vue
│   │   └── ...其他组件
│   │
│   ├── utils/
│   │   ├── auth.ts               # Token 管理
│   │   ├── image.ts              # 图片处理
│   │   ├── validators.ts         # 表单校验
│   │   ├── format.ts             # 格式化工具
│   │   └── message.ts            # 提示消息
│   │
│   ├── types/
│   │   ├── api.ts                # API 通用类型
│   │   ├── user.ts               # 用户类型
│   │   ├── detect.ts             # 识别类型
│   │   ├── chat.ts               # 对话类型
│   │   └── forum.ts              # 论坛类型
│   │
│   ├── styles/
│   │   ├── variables.css         # CSS 变量
│   │   ├── global.css            # 全局样式
│   │   ├── detection.css
│   │   ├── chat.css
│   │   ├── user.css
│   │   └── forum.css
│   │
│   ├── App.vue
│   └── main.ts
│
├── package.json
├── tsconfig.json
├── vite.config.ts
└── ...其他配置文件
```

---

## 🔄 阶段间依赖关系

```
STAGE_1 基础架构（API + 认证 + 状态）
    ↓
    ├─→ STAGE_2 识别（独立）
    ├─→ STAGE_4 用户中心（独立）
    │
    ├─→ STAGE_3 对话（依赖 1）
    ├─→ STAGE_5 论坛（依赖 1）
  ├─→ STAGE_6 管理（依赖 1）
  └─→ STAGE_7 互动增强与预警中心（依赖 5 和 6）
```

**说明**：
- STAGE_1 是基础，其他章节都依赖它
- STAGE_2、STAGE_4 可与 STAGE_1 并行
- STAGE_3、STAGE_5、STAGE_6 需在 STAGE_1 完成后开始
- STAGE_7 适合作为论坛与管理端接口完成后的联调阶段

---

## 💡 通用开发规范

### TypeScript 类型定义

每个功能模块创建对应的 `types/` 文件：

```typescript
// 示例：src/types/detect.ts
export interface DetectResult {
  id: number;
  topLabel: string;
  confidence: number;
  createdTime: number;
}
```

### API 模块规范

每个 API 模块需要定义接口和实现函数：

```typescript
// 示例：src/api/detect.ts
import { http } from './request'
import type { DetectResult } from '@/types/detect'

export function detect(payload: any): Promise<DetectResult> {
  return http.post('/detect', payload)
}
```

### 组件编写规范

使用 `<script setup>` 语法：

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'

const count = ref(0)
const doubled = computed(() => count.value * 2)

function increment() {
  count.value++
}
</script>
```

### 命名约定

| 类型 | 规范 | 示例 |
|-----|------|------|
| 文件（组件） | PascalCase | `UserProfile.vue` |
| 文件（工具/api） | kebab-case | `image.ts` |
| 变量 | camelCase | `userInfo` |
| 常量 | UPPER_SNAKE_CASE | `MAX_FILE_SIZE` |
| 接口 | PascalCase + I/VO/DTO | `UserVO`, `DetectDTO` |

---

## 🚀 快速参考

### 常用命令

```bash
# 开发服务器
npm run dev

# 构建生产版本
npm run build

# 类型检查
npx vue-tsc -b

# 预览生产版本
npm run preview
```

### 常用 VS Code 扩展

推荐安装：
- Volar（Vue 语言支持）
- TypeScript Vue Plugin
- ESLint
- Prettier

### API 调用示例

```typescript
import { detect } from '@/api/detect'
import { getMessages } from '@/api/chat'
import { updateUsername } from '@/api/user'

// 使用 async/await
async function handleDetect(file: File) {
  try {
    const base64 = await fileToBase64(file)
    const result = await detect({ imageBase64: base64 })
    console.log(result)
  } catch (error) {
    ElMessage.error('识别失败')
  }
}
```

### 状态管理示例

```typescript
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

// 读取状态
console.log(appStore.userInfo)
console.log(appStore.isLoggedIn)

// 修改状态
appStore.setUser(userInfo, token)
appStore.logout()
```

### 路由跳转示例

```typescript
import { useRouter } from 'vue-router'

const router = useRouter()

// 跳转
router.push({ name: 'ChatDetail', params: { sessionId: 1 } })

// 返回
router.back()
```

---

## 📌 重要提醒

### ⚠️ 常见陷阱

1. **Token 管理**：登出时必须完全清除 token 和 userInfo
2. **API 错误处理**：认证失败（401）、权限不足（403）需特殊处理
3. **Base64 前缀**：识别功能需确保不包含 `data:image/` 前缀
4. **时间戳单位**：后端毫秒级，需用 dayjs 格式化
5. **跨域请求**：确认后端已配置 CORS

### ✅ 质量检查清单

在提交代码前：

- [ ] 无 TypeScript 报错（`npx vue-tsc -b`）
- [ ] 页面在手机端可用（responsive）
- [ ] 所有用户交互有提示（ElMessage）
- [ ] 网络错误有重试机制
- [ ] Token 过期自动重新登录
- [ ] Console 无异常警告

---

## 🤝 获取帮助

### 查找特定功能的文档

- **需要实现登录功能** → 查看 STAGE_1
- **开发图片识别页面** → 查看 STAGE_2
- **实现聊天对话** → 查看 STAGE_3
- **编辑个人资料** → 查看 STAGE_4
- **发布论坛帖子** → 查看 STAGE_5
- **搭建管理后台** → 查看 STAGE_6

### 常见问题速查

| 问题 | 查看部分 |
|-----|--------|
| 如何添加 API 接口？ | STAGE_1 - API 请求层 |
| 如何添加国际化？ | 各 STAGE 的完善建议 |
| 如何优化长列表性能？ | STAGE_3、STAGE_5 |
| 如何处理图片上传？ | STAGE_2、STAGE_4 |
| 如何实现权限检查？ | STAGE_1 - 路由守卫 |

---

## 📈 开发进度追踪

### 推荐项目管理方式

```
Week 1: STAGE_1（基础架构）- 100%
Week 2: STAGE_2 + STAGE_4（核心功能） - 100%
Week 3: STAGE_3（对话系统）- 100%
Week 4: STAGE_5（论坛）- 100%
Week 5: STAGE_6（管理后台） + 测试优化
```

---

## 📞 反馈和建议

本文档基于项目规划，如在开发过程中发现：
- 文档遗漏或错误
- 需求变更
- 技术方案调整

请及时更新本文档，确保开发指南始终准确。

---

**最后更新**：2026-04-05 | 版本：1.0
