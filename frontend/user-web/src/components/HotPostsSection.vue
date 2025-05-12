<template>
  <div class="hot-posts-section">
    <div class="section-header">
      <h3>热门帖子</h3>
      <el-button type="text" @click="navigateTo('/community')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="1" animated v-for="i in 5" :key="i" style="margin-bottom: 12px;" />
    </div>

    <div v-else-if="posts.length === 0" class="empty">
      <el-empty description="暂无热门帖子" :image-size="60" />
    </div>

    <div v-else class="post-list">
      <hot-post-card
        v-for="(post, index) in posts"
        :key="post.id || post.postId"
        :post="post"
        :index="index"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { getHotPosts } from '../api/community'
import { getUserProfile } from '../api/profile'
import HotPostCard from './HotPostCard.vue'

const props = defineProps({
  limit: {
    type: Number,
    default: 5
  }
})

const router = useRouter()
const posts = ref([])
const loading = ref(false)

// 方法
function navigateTo(path) {
  router.push(path)
}

async function fetchHotPosts() {
  loading.value = true
  try {
    const res = await getHotPosts({ limit: props.limit })
    const hotPosts = res.data.hotPosts || []

    // 获取用户信息
    const userPromises = []
    const userMap = new Map()

    // 收集所有不同的用户ID
    const userIds = [...new Set(hotPosts.map(post => post.userId))]

    // 为每个帖子添加用户信息
    for (const post of hotPosts) {
      // 添加默认用户信息
      post.username = '用户' + post.userId
      post.nickname = '用户' + post.userId
      post.avatar = ''

      // 从后端获取用户信息
      userPromises.push(
        getUserInfo(post.userId)
          .then(userInfo => {
            if (userInfo) {
              userMap.set(post.userId, userInfo)
            }
          })
          .catch(err => {
            console.error(`获取用户${post.userId}信息失败:`, err)
          })
      )
    }

    // 等待所有用户信息获取完成
    if (userPromises.length > 0) {
      await Promise.all(userPromises)

      // 更新帖子的用户信息
      for (const post of hotPosts) {
        const userInfo = userMap.get(post.userId)
        if (userInfo) {
          post.username = userInfo.username || post.username
          post.nickname = userInfo.nickname || post.nickname
          post.avatar = userInfo.avatar || post.avatar
        }
      }
    }

    posts.value = hotPosts
  } catch (error) {
    console.error('获取热门帖子失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取用户信息
async function getUserInfo(userId) {
  try {
    const res = await getUserProfile(userId)
    return res.data
  } catch (error) {
    console.error(`获取用户${userId}信息失败:`, error)
    return null
  }
}

// 生命周期钩子
onMounted(() => {
  fetchHotPosts()
})
</script>

<style lang="scss" scoped>
.hot-posts-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      margin: 0;
      position: relative;
      padding-left: 12px;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 16px;
        background-color: #409EFF;
        border-radius: 3px;
      }
    }
  }

  .loading {
    padding: 10px 0;
  }

  .empty {
    padding: 20px 0;
    display: flex;
    justify-content: center;
  }

  .post-list {
    max-height: 400px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: #ddd;
      border-radius: 3px;

      &:hover {
        background-color: #ccc;
      }
    }
  }
}
</style>
