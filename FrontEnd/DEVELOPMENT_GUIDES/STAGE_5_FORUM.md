# 阶段五：社区论坛系统

## 🎯 阶段目标
实现农业社区论坛功能，包括资讯浏览、帖子发布、评论互动、点赞等完整的社交功能。

**依赖**：阶段一 | **工作量**：4-5 天 | **优先级**：P2

---

## 📋 核心需求

### 1. 论坛 API 接口

**文件**：`src/api/forum.ts`

```typescript
// ===== 帖子相关 =====

export interface ForumPost {
  id: number;
  userId: number;
  author: string;
  title: string;
  category: string;           // 资讯、预警、技巧等
  content: string;            // HTML 或 Markdown
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;           // 当前用户是否已点赞
  createdTime: number;
  updatedTime: number;
}

export interface CreatePostPayload {
  title: string;
  category: string;
  content: string;
  status?: 'draft' | 'published';  // 草稿或发布
}

export function getPosts(page: number = 1, category?: string, search?: string): Promise<{ data: ForumPost[]; total: number }>

export function getPostDetail(postId: number): Promise<ForumPost>

export function createPost(payload: CreatePostPayload): Promise<ForumPost>

export function updatePost(postId: number, payload: Partial<CreatePostPayload>): Promise<ForumPost>

export function deletePost(postId: number): Promise<{ code: 200; message: string }>

export function likePost(postId: number): Promise<{ liked: boolean }>

// ===== 评论相关 =====

export interface ForumComment {
  id: number;
  postId: number;
  userId: number;
  author: string;
  parentId: number | null;    // 父评论 ID，用于回复
  content: string;
  likeCount: number;
  isLiked: boolean;
  createdTime: number;
  updatedTime: number;
}

export function getComments(postId: number): Promise<ForumComment[]>

export function createComment(postId: number, payload: { content: string; parentId?: number }): Promise<ForumComment>

export function deleteComment(commentId: number): Promise<{ code: 200; message: string }>

export function likeComment(commentId: number): Promise<{ liked: boolean }>

// ===== 预警相关 =====

export interface AlertInfo {
  id: number;
  title: string;
  content: string;
  severity: 'low' | 'medium' | 'high';  // 低、中、高
  province?: string;
  createdTime: number;
}

export function getAlerts(): Promise<AlertInfo[]>
```

**后端 API 规范**（待实现）：
```
GET /api/forum/posts?page=1&category=all&search=...
POST /api/forum/posts
GET /api/forum/posts/:postId
PATCH /api/forum/posts/:postId
DELETE /api/forum/posts/:postId
POST /api/forum/posts/:postId/like

GET /api/forum/posts/:postId/comments
POST /api/forum/posts/:postId/comments
DELETE /api/forum/comments/:commentId
POST /api/forum/comments/:commentId/like

GET /api/forum/alerts
```

---

### 2. Forum 论坛首页

**文件**：`src/views/Forum.vue`

**功能需求**：

#### 顶部导航和筛选
- 资讯、预警、技巧等分类标签（可点击筛选）
- 搜索框（搜索帖子标题和内容）
- 排序选项：最新、热度、评论数

#### 预警信息横幅（可选）
- 显示最新的高风险预警
- 红色警告风格
- 点击可跳转到预警详情

#### 帖子列表
- 卡片式展示，每个帖子包含：
  - 帖子标题（可点击进入详情）
  - 摘要（截断长文本，显示前 100 字）
  - 作者名和发布时间
  - 分类标签
  - 浏览数、点赞数、评论数
  - 若图片较多可显示缩略图（可选）
- 分页加载或无限滚动
- 无数据时显示空态提示

#### 右侧快速操作栏（可选）
- "发布帖子"按钮
- "我的帖子"链接
- 热门话题推荐

---

### 3. PostDetail 帖子详情页

**文件**：`src/views/PostDetail.vue`

**功能需求**：

#### 帖子内容区
- 标题、作者、发布时间、浏览数
- 分类标签
- 富文本内容展示（HTML 或 Markdown）
- 点赞按钮和点赞数（心形按钮，可切换状态）

#### 评论系统
- 评论输入框
  - 顶部：支持 Markdown 或富文本
  - 预览功能（可选）
  - 发布按钮
- 评论列表
  - 评论卡片：头像、作者、发布时间、内容
  - 评论点赞功能
  - 回复按钮（显示评论树形结构）
  - 删除按钮（仅评论作者可见）
- 评论分页（每页 10 条）
- 评论排序：最新 / 热度

#### 页面侧栏（可选）
- 作者信息卡片（头像、名称、粉丝数、关注按钮）
- 相关帖子推荐（同分类的其他热门帖子）

---

### 4. CreatePost 发布帖子页

**文件**：`src/views/CreatePost.vue`

**功能需求**：

#### 表单区
- 标题输入框
  - 校验：非空，长度 5-100
  - 实时字数统计
- 分类选择下拉框
  - 资讯、预警、技巧、其他等
- 内容编辑器
  - 使用 `wangeditor` 富文本编辑器
  - 支持格式：
    - 加粗、斜体、下划线
    - 标题、列表、引用、代码块
    - 表格、链接、图片
  - 自动保存草稿（本地 localStorage）
  - 实时预览（可选）

#### 操作按钮
- "发布"按钮
- "保存草稿"按钮
- "预览"按钮（打开预览对话框）
- "返回"按钮

#### 确认对话框
- 发布前确认分类和标题
- 发布成功后跳转到帖子详情页

---

### 5. Pinia 论坛状态管理

**文件**：`src/stores/forum.ts`

```typescript
export const useForumStore = defineStore('forum', () => {
  // State
  const posts = ref<ForumPost[]>([])
  const currentPost = ref<ForumPost | null>(null)
  const comments = ref<ForumComment[]>([])
  const isLoading = ref(false)
  const currentCategory = ref('all')
  const currentPage = ref(1)
  const totalPosts = ref(0)

  // Actions
  async function fetchPosts(page = 1, category = 'all', search = '') {
    isLoading.value = true
    try {
      const result = await getPosts(page, category, search)
      posts.value = result.data
      totalPosts.value = result.total
      currentPage.value = page
      currentCategory.value = category
    } finally {
      isLoading.value = false
    }
  }

  async function fetchPostDetail(postId: number) {
    try {
      currentPost.value = await getPostDetail(postId)
      comments.value = await getComments(postId)
    } catch (error) {
      ElMessage.error('加载帖子失败')
    }
  }

  async function publishPost(payload: CreatePostPayload) {
    try {
      const post = await createPost(payload)
      posts.value.unshift(post)
      return post
    } catch (error) {
      ElMessage.error('发布失败')
      throw error
    }
  }

  async function toggleLikePost(postId: number) {
    try {
      await likePost(postId)
      if (currentPost.value?.id === postId) {
        currentPost.value.isLiked = !currentPost.value.isLiked
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  return {
    posts, currentPost, comments, isLoading, currentCategory, currentPage, totalPosts,
    fetchPosts, fetchPostDetail, publishPost, toggleLikePost
  }
})
```

---

### 6. 富文本编辑器集成

**文件**：`src/components/RichEditor.vue`

**需要实现**：
- 使用 `wangeditor` 库
- Props: `modelValue: string`，`placeholder?: string`
- Emits: `update:modelValue`
- 支持工具栏：
  - 基础格式（加粗、斜体、下划线）
  - 标题、列表、引用
  - 代码块、表格、链接、图片
- 自动保存到 localStorage（可选，使用 debounce）

**示意**：
```vue
<script setup lang="ts">
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css.css'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ 'update:modelValue': [string] }>()

const editorRef = ref(null)
const mode = ref('default') // 或 'simple'

const toolbarConfig = {
  excludeKeys: ['uploadVideo']  // 排除不需要的工具
}

const editorConfig = {
  placeholder: props.placeholder || '输入内容...',
  autoFocus: false,
  readOnly: false
}

function handleChange() {
  const html = editorRef.value?.getHtml()
  emit('update:modelValue', html || '')
}
</script>
```

---

### 7. 评论组件（树形结构）

**文件**：`src/components/CommentTree.vue`

**需要实现**：
- 递归组件支持嵌套回复
- Props: `comments: ForumComment[]`
- 显示评论缩进
- 支持点赞、删除、回复操作
- 输入框用于回复（使用 v-if 显示/隐藏）

---

## 🏗️ 项目结构

```
src/
├── api/
│   └── forum.ts                    ✅ 论坛 API 模块
├── views/
│   ├── Forum.vue                   ✅ 论坛首页
│   ├── PostDetail.vue              ✅ 帖子详情页
│   └── CreatePost.vue              ✅ 发布帖子页
├── components/
│   ├── RichEditor.vue              ✅ 富文本编辑器
│   └── CommentTree.vue             ✅ 评论树形组件
├── stores/
│   └── forum.ts                    ✅ 论坛状态
├── types/
│   └── forum.ts                    ✅ 论坛类型定义
└── styles/
    └── forum.css                   ✅ 论坛样式
```

---

## 📝 验收标准

- [ ] Forum 首页可访问（`/forum`），列表加载正确
- [ ] 分类筛选功能可用
- [ ] 搜索功能可用
- [ ] 帖子卡片显示完整信息
- [ ] 点击帖子进入详情页
- [ ] PostDetail 页面加载帖子和评论正确
- [ ] 点赞功能可用（心形按钮切换）
- [ ] 评论树形结构显示正确
- [ ] 评论输入框可用，发送成功
- [ ] CreatePost 页面富文本编辑器可用
- [ ] 草稿自动保存到 localStorage
- [ ] 发布帖子成功，跳转到详情页
- [ ] 删除功能可用（仅作者）
- [ ] 页面响应式良好

---

## ⚠️ 常见陷阱

1. **内容安全**：富文本编辑器输出的 HTML 需要验证，避免 XSS（可用 DOMPurify）
2. **评论树形**：深层回复可能导致性能问题，考虑限制深度或虚拟滚动
3. **图片上传**：wangeditor 图片上传需配置后端接口
4. **草稿管理**：localStorage 容量有限，避免保存过多内容
5. **实时更新**：点赞、评论数需要立即更新 UI

---

## 🤝 与其他阶段接口

- Home 页面可显示最新帖子或预警信息
- User 页面可显示"我的帖子"统计
- Detection 页面可集成"分享到论坛"功能
