<template>
  <div class="post-card" @click="goToDetail">
    <div class="post-header">
      <div class="user-info">
        <el-avatar
          :src="post.avatar ? formatImageUrl(post.avatar) : ''"
          :size="40"
          @click.stop="goToUserPosts"
          class="clickable-avatar"
        ></el-avatar>
        <div class="user-name" @click.stop="goToUserPosts">
          <div class="nickname">{{ post.nickname || post.username || '用户' + post.userId }}</div>
          <div class="time">{{ formatTime(post.createdAt) }}</div>
        </div>
      </div>
      <div v-if="post.ipName" class="ip-tag">{{ post.ipName }}</div>
    </div>

    <div class="post-content">
      <div class="post-title">
        {{ post.title }}
        <el-tag v-if="post.status === 2" type="warning" size="small" class="status-tag">审核中</el-tag>
        <el-tag v-else-if="post.status === 0" type="danger" size="small" class="status-tag">已拒绝</el-tag>
      </div>
      <div v-if="post.content" class="post-text">{{ post.content }}</div>
      <div v-if="post.images && post.images.length > 0" class="post-images">
        <img v-for="(image, index) in post.images.slice(0, 3)" :key="index" :src="formatImageUrl(image.imageUrl)" alt="Post Image">
        <div v-if="post.images.length > 3" class="more-images">+{{ post.images.length - 3 }}</div>
      </div>
    </div>

    <div class="post-footer">
      <div class="post-stats">
        <div class="stat-item">
          <el-icon><View /></el-icon>
          <span>{{ post.viewCount }}</span>
        </div>
        <div class="stat-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ post.commentCount }}</span>
        </div>
        <div class="stat-item">
          <el-icon><Star /></el-icon>
          <span>{{ post.collectCount }}</span>
        </div>
        <div class="stat-item">
          <el-icon><Check /></el-icon>
          <span>{{ post.likeCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { View, ChatDotRound, Star, Check } from '@element-plus/icons-vue'
import { formatImageUrl } from '../utils/image'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 方法
function goToDetail() {
  router.push(`/post/${props.post.id || props.post.postId}`)
}

function goToUserPosts() {
  if (props.post.userId) {
    router.push({
      path: '/community',
      query: { userId: props.post.userId }
    })
  }
}

function formatTime(time) {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }

  // 小于1天
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }

  // 小于1周
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }

  // 格式化日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.post-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  margin-bottom: 20px;
  padding: 20px;
  cursor: pointer;
  border: 1px solid #f0f0f0;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #e6f7ff;
  }

  .post-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .user-info {
      display: flex;
      align-items: center;

      .clickable-avatar {
        cursor: pointer;
        transition: transform 0.2s;

        &:hover {
          transform: scale(1.05);
        }
      }

      .user-name {
        margin-left: 10px;
        cursor: pointer;

        &:hover {
          .nickname {
            color: #409EFF;
          }
        }

        .nickname {
          font-size: 14px;
          font-weight: bold;
          color: #333;
          transition: color 0.2s;
        }

        .time {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .ip-tag {
      background-color: #f0f9ff;
      color: #409EFF;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
    }
  }

  .post-content {
    margin-bottom: 12px;

    .post-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
      margin-bottom: 8px;
      display: flex;
      align-items: center;

      .status-tag {
        margin-left: 8px;
        font-size: 12px;
        font-weight: normal;
      }
    }

    .post-text {
      font-size: 14px;
      color: #666;
      margin-bottom: 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      line-clamp: 3;
      -webkit-box-orient: vertical;
    }

    .post-images {
      display: flex;
      gap: 5px;
      position: relative;

      img {
        width: calc(33.33% - 4px);
        height: 100px;
        object-fit: cover;
        border-radius: 4px;
      }

      .more-images {
        position: absolute;
        right: 5px;
        bottom: 5px;
        background-color: rgba(0, 0, 0, 0.5);
        color: #fff;
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 12px;
      }
    }
  }

  .post-footer {
    .post-stats {
      display: flex;
      justify-content: space-around;

      .stat-item {
        display: flex;
        align-items: center;
        color: #999;
        font-size: 12px;

        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
}
</style>
