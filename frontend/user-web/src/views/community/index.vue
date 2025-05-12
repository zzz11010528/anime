<template>
  <div class="community-container">
    <!-- 顶部横幅 - 热门帖子 -->
    <div class="community-banner">
      <el-carousel height="200px" indicator-position="none">
        <el-carousel-item v-for="(banner, index) in banners" :key="index" @click="goToPostDetail(banner.postId)">
          <div class="banner-content" :style="{ backgroundImage: `url(${banner.imageUrl})` }">
            <div class="banner-text">
              <h2>{{ banner.title }}</h2>
              <p>{{ banner.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="community-main">
      <!-- 左侧边栏 -->
      <div class="community-sidebar">
        <el-card class="sidebar-card">
          <div class="user-info" v-if="isLoggedIn">
            <el-avatar :src="userAvatarUrl" :size="60"></el-avatar>
            <div class="user-name">{{ userInfo.nickname || userInfo.username }}</div>
            <el-button type="primary" @click="goToPublish">发布帖子</el-button>
          </div>

          <div class="login-prompt" v-else>
            <el-avatar :size="60" :src="defaultAvatar"></el-avatar>
            <div class="prompt-text">登录后才能发布帖子和参与讨论</div>
            <el-button type="primary" @click="goToLogin">立即登录</el-button>
          </div>
        </el-card>

        <el-card class="sidebar-card">
          <template #header>
            <div class="card-header">
              <span>社区分类</span>
            </div>
          </template>

          <div class="category-list">
            <div
              v-for="category in categories"
              :key="category.id"
              class="category-item"
              :class="{ active: filterParams.categoryId === category.id }"
              @click="handleCategoryClick(category.id)"
            >
              <el-icon v-if="category.icon"><component :is="category.icon" /></el-icon>
              <span>{{ category.name }}</span>
              <span class="count">({{ category.postCount || 0 }})</span>
            </div>
          </div>
        </el-card>

        <el-card class="sidebar-card">
          <template #header>
            <div class="card-header">
              <span>热门话题</span>
            </div>
          </template>

          <div class="topic-list">
            <div
              v-for="topic in topics"
              :key="topic.id"
              class="topic-item"
              :class="{ active: filterParams.topicId === topic.id }"
              @click="handleTopicClick(topic.id)"
            >
              <span class="topic-name"># {{ topic.name }}</span>
              <span class="topic-count">{{ topic.postCount || 0 }}篇</span>
            </div>
          </div>
        </el-card>

        <!-- 热门帖子 -->
        <hot-posts-section :limit="5" />
      </div>

      <!-- 右侧内容区 -->
      <div class="community-content">
        <!-- 用户信息卡片 -->
        <div v-if="targetUser" class="user-profile-section">
          <UserInfoCard :userInfo="targetUser" />
          <el-card class="user-filter-card">
            <div class="filter-header">
              <h3>{{ targetUser.nickname || targetUser.username }}的帖子</h3>
              <el-button type="primary" text @click="clearUserFilter">
                返回全部帖子
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 筛选卡片 -->
        <el-card class="filter-card" v-else>
          <div class="filter-header">
            <div class="filter-tabs">
              <div
                class="filter-tab"
                :class="{ active: filterParams.orderBy === 'newest' }"
                @click="handleOrderChange('newest')"
              >
                最新
              </div>
              <div
                class="filter-tab"
                :class="{ active: filterParams.orderBy === 'hottest' }"
                @click="handleOrderChange('hottest')"
              >
                最热
              </div>
              <div
                class="filter-tab"
                :class="{ active: filterParams.orderBy === 'recommended' }"
                @click="handleOrderChange('recommended')"
              >
                推荐
              </div>
            </div>

            <div class="filter-search">
              <el-input
                v-model="filterParams.keyword"
                placeholder="搜索帖子"
                clearable
                @keyup.enter="handleSearch"
              >
                <template #suffix>
                  <el-button :icon="Search" circle @click="handleSearch" />
                </template>
              </el-input>
            </div>
          </div>

          <div class="filter-tags" v-if="hasActiveFilters">
            <el-tag
              v-if="activeCategoryName"
              closable
              @close="handleCategoryClick(null)"
            >
              分类: {{ activeCategoryName }}
            </el-tag>

            <el-tag
              v-if="activeTopicName"
              closable
              @close="handleTopicClick(null)"
            >
              话题: {{ activeTopicName }}
            </el-tag>

            <el-tag
              v-if="filterParams.keyword"
              closable
              @close="clearKeyword"
            >
              关键词: {{ filterParams.keyword }}
            </el-tag>
          </div>
        </el-card>

        <div class="post-list">
          <div v-if="loading" class="loading">
            <el-skeleton :rows="3" animated v-for="i in 3" :key="i" style="margin-bottom: 20px;" />
          </div>

          <el-empty v-else-if="posts.length === 0" description="暂无帖子" />

          <div v-else>
            <div v-for="post in posts" :key="post.id" class="post-item">
              <post-card :post="post" @click="goToPostDetail(post.id)" />
            </div>

            <div class="pagination">
              <el-pagination
                v-model:current-page="page"
                v-model:page-size="size"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布帖子对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布帖子"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入标题" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="publishForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="话题" prop="topicIds">
          <el-select
            v-model="publishForm.topicIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择话题（最多3个）"
            :max-collapse-tags="2"
          >
            <el-option
              v-for="topic in topics"
              :key="topic.id"
              :label="topic.name"
              :value="topic.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            placeholder="请输入内容"
            rows="6"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
            action="/api/file/upload/post"
            list-type="picture-card"
            :limit="9"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPost" :loading="publishing">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getPostList, getCategoryList, getTopicList, publishPost, getHotPosts } from '../../api/community'
import { getUserProfile } from '../../api/profile'
import PostCard from '../../components/PostCard.vue'
import HotPostsSection from '../../components/HotPostsSection.vue'
import UserInfoCard from '../../components/UserInfoCard.vue'
import { formatImageUrl } from '../../utils/image'
import defaultAvatar from '../../assets/images/default-avatar.png'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const banners = ref([])
const categories = ref([])
const topics = ref([])
const posts = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const targetUser = ref(null)
const targetUserLoading = ref(false)

// 筛选参数
const filterParams = reactive({
  keyword: '',
  categoryId: null,
  topicId: null,
  orderBy: 'newest'
})

// 发布帖子相关
const publishDialogVisible = ref(false)
const publishFormRef = ref(null)
const publishing = ref(false)
const publishForm = reactive({
  title: '',
  categoryId: null,
  topicIds: [],
  content: '',
  images: []
})
const publishRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 5, max: 2000, message: '内容长度在 5 到 2000 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)

// 格式化头像URL
const userAvatarUrl = computed(() => {
  if (!userInfo.value.avatar) return defaultAvatar
  return formatImageUrl(userInfo.value.avatar)
})

const activeCategoryName = computed(() => {
  if (!filterParams.categoryId) return ''
  const category = categories.value.find(item => item.id === filterParams.categoryId)
  return category ? category.name : ''
})

const activeTopicName = computed(() => {
  if (!filterParams.topicId) return ''
  const topic = topics.value.find(item => item.id === filterParams.topicId)
  return topic ? topic.name : ''
})

const hasActiveFilters = computed(() => {
  return filterParams.categoryId || filterParams.topicId || filterParams.keyword
})

// 监听路由参数变化
watch(
  () => route.query,
  (query) => {
    // 从URL参数更新筛选条件
    filterParams.keyword = query.keyword || ''
    filterParams.categoryId = query.categoryId ? Number(query.categoryId) : null
    filterParams.topicId = query.topicId ? Number(query.topicId) : null
    filterParams.orderBy = query.orderBy || 'newest'

    // 更新分页
    page.value = Number(query.page) || 1
    size.value = Number(query.size) || 10

    // 如果有用户ID参数，获取用户资料
    if (query.userId) {
      fetchUserProfile(Number(query.userId))
    } else {
      targetUser.value = null
    }

    // 获取帖子列表
    fetchPosts()
  },
  { immediate: true }
)

// 获取用户资料
async function fetchUserProfile(userId) {
  targetUserLoading.value = true
  try {
    const res = await getUserProfile(userId)
    targetUser.value = res.data
  } catch (error) {
    console.error('获取用户资料失败:', error)
    ElMessage.error('获取用户资料失败')
    targetUser.value = null
  } finally {
    targetUserLoading.value = false
  }
}

// 方法
async function fetchPosts() {
  loading.value = true

  try {
    // 构建请求参数
    const params = {
      page: page.value,
      size: size.value,
      keyword: filterParams.keyword,
      categoryId: filterParams.categoryId,
      topicId: filterParams.topicId
    }

    // 如果有用户ID参数，添加到请求参数中
    if (route.query.userId) {
      params.userId = Number(route.query.userId)
    }

    // 处理排序
    if (filterParams.orderBy === 'newest') {
      params.orderBy = 'created_at'
      params.orderType = 'desc'
    } else if (filterParams.orderBy === 'hottest') {
      params.orderBy = 'view_count'
      params.orderType = 'desc'
    } else if (filterParams.orderBy === 'recommended') {
      params.recommended = 1
    }

    const res = await getPostList(params)
    posts.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取分类列表
async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取话题列表
async function fetchTopics() {
  try {
    const res = await getTopicList()
    topics.value = res.data
  } catch (error) {
    console.error('获取话题列表失败:', error)
  }
}

// 处理分类点击
function handleCategoryClick(categoryId) {
  filterParams.categoryId = categoryId
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 处理话题点击
function handleTopicClick(topicId) {
  filterParams.topicId = topicId
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 处理排序方式变化
function handleOrderChange(orderBy) {
  filterParams.orderBy = orderBy
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 处理搜索
function handleSearch() {
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 清除关键词
function clearKeyword() {
  filterParams.keyword = ''
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 清除用户筛选
function clearUserFilter() {
  // 移除用户ID参数，保留其他筛选条件
  const query = { ...route.query }
  delete query.userId
  router.push({ path: '/community', query })
}

// 处理分页大小变化
function handleSizeChange() {
  updateRouteQuery()
}

// 处理页码变化
function handleCurrentChange() {
  updateRouteQuery()
}

// 更新路由查询参数
function updateRouteQuery() {
  const query = {}

  if (filterParams.keyword) {
    query.keyword = filterParams.keyword
  }

  if (filterParams.categoryId) {
    query.categoryId = filterParams.categoryId
  }

  if (filterParams.topicId) {
    query.topicId = filterParams.topicId
  }

  if (filterParams.orderBy !== 'newest') {
    query.orderBy = filterParams.orderBy
  }

  if (page.value > 1) {
    query.page = page.value
  }

  if (size.value !== 10) {
    query.size = size.value
  }

  router.push({ path: '/community', query })
}

// 跳转到帖子详情
function goToPostDetail(postId) {
  router.push(`/post/${postId}`)
}

// 跳转到登录页
function goToLogin() {
  router.push({
    path: '/login',
    query: { redirect: route.fullPath }
  })
}

// 跳转到发布帖子页面
function goToPublish() {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  router.push('/post/publish')
}

// 处理图片上传成功
function handleImageSuccess(res, file) {
  publishForm.images.push(res.data)
}

// 处理图片移除
function handleImageRemove(file, fileList) {
  const index = publishForm.images.indexOf(file.response?.data)
  if (index !== -1) {
    publishForm.images.splice(index, 1)
  }
}

// 图片上传前验证
function beforeImageUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return (isJPG || isPNG) && isLt2M
}

// 提交发布帖子
function submitPost() {
  publishFormRef.value.validate(async (valid) => {
    if (!valid) return

    publishing.value = true
    try {
      await publishPost({
        title: publishForm.title,
        categoryId: publishForm.categoryId,
        topicIds: publishForm.topicIds,
        content: publishForm.content,
        images: publishForm.images
      })

      ElMessage.success('发布成功')
      publishDialogVisible.value = false

      // 重置表单
      publishForm.title = ''
      publishForm.categoryId = null
      publishForm.topicIds = []
      publishForm.content = ''
      publishForm.images = []

      // 刷新帖子列表
      fetchPosts()
    } catch (error) {
      ElMessage.error(error.message || '发布失败')
    } finally {
      publishing.value = false
    }
  })
}

// 用户统计数据已移除

// 获取热门帖子作为轮播图
async function fetchHotPostsForBanner() {
  try {
    const res = await getHotPosts({ limit: 3 })
    const hotPosts = res.data.hotPosts || []

    // 将热门帖子转换为轮播图格式
    banners.value = hotPosts.map(post => ({
      postId: post.postId || post.id,
      title: post.title,
      description: `热度: ${post.viewCount}浏览 ${post.commentCount}评论 ${post.likeCount}点赞`,
      imageUrl: 'https://via.placeholder.com/1200x200' // 默认背景图
    }))
  } catch (error) {
    console.error('获取热门帖子作为轮播图失败:', error)
  }
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchTopics()
  fetchHotPostsForBanner()
})
</script>

<style lang="scss" scoped>
.community-container {
  max-width: 1200px;
  margin: 0 auto;

  .community-banner {
    margin-bottom: 20px;

    .banner-content {
      width: 100%;
      height: 100%;
      background-size: cover;
      background-position: center;
      position: relative;
      cursor: pointer;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(to right, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.4));
      }

      .banner-text {
        position: absolute;
        top: 50%;
        left: 50px;
        transform: translateY(-50%);
        color: #fff;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
        max-width: 70%;

        h2 {
          font-size: 28px;
          margin-bottom: 15px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        p {
          font-size: 16px;
          opacity: 0.9;
        }
      }
    }
  }

  .community-main {
    display: flex;
    gap: 20px;

    .community-sidebar {
      width: 280px;
      flex-shrink: 0;

      .sidebar-card {
        margin-bottom: 20px;

        .user-info {
          text-align: center;
          padding: 10px 0;

          .el-avatar {
            margin-bottom: 10px;
          }

          .user-name {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 15px;
          }
        }

        .login-prompt {
          text-align: center;
          padding: 20px 0;

          .el-avatar {
            margin-bottom: 15px;
          }

          .prompt-text {
            margin-bottom: 15px;
            color: #666;
          }
        }

        .card-header {
          font-weight: bold;
        }

        .category-list {
          .category-item {
            display: flex;
            align-items: center;
            padding: 10px 0;
            cursor: pointer;
            transition: all 0.3s;

            &:not(:last-child) {
              border-bottom: 1px solid #f0f0f0;
            }

            .el-icon {
              margin-right: 8px;
              font-size: 16px;
            }

            .count {
              margin-left: auto;
              color: #999;
              font-size: 12px;
            }

            &:hover {
              color: #409EFF;
            }

            &.active {
              color: #409EFF;
              font-weight: bold;
            }
          }
        }

        .topic-list {
          .topic-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 0;
            cursor: pointer;
            transition: all 0.3s;

            &:not(:last-child) {
              border-bottom: 1px solid #f0f0f0;
            }

            .topic-name {
              font-size: 14px;
            }

            .topic-count {
              color: #999;
              font-size: 12px;
            }

            &:hover {
              color: #409EFF;
            }

            &.active {
              color: #409EFF;
              font-weight: bold;
            }
          }
        }
      }
    }

    .community-content {
      flex: 1;

      .user-profile-section {
        margin-bottom: 20px;

        .user-filter-card {
          margin-top: 15px;

          .filter-header {
            display: flex;
            justify-content: space-between;
            align-items: center;

            h3 {
              margin: 0;
              font-size: 16px;
            }
          }
        }
      }

      .filter-card {
        margin-bottom: 20px;

        .filter-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .filter-tabs {
            display: flex;

            .filter-tab {
              padding: 0 15px;
              height: 40px;
              line-height: 40px;
              cursor: pointer;
              position: relative;

              &.active {
                color: #409EFF;
                font-weight: bold;

                &::after {
                  content: '';
                  position: absolute;
                  bottom: 0;
                  left: 50%;
                  transform: translateX(-50%);
                  width: 20px;
                  height: 2px;
                  background-color: #409EFF;
                }
              }
            }
          }

          .filter-search {
            width: 250px;
          }
        }

        .filter-tags {
          margin-top: 15px;

          .el-tag {
            margin-right: 10px;
          }
        }
      }

      .post-list {
        .post-item {
          margin-bottom: 20px;
        }

        .pagination {
          margin-top: 30px;
          text-align: center;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .community-container {
    .community-main {
      flex-direction: column;

      .community-sidebar {
        width: 100%;
      }
    }
  }
}
</style>
