<template>
  <div class="comment-card" @click="goToPostDetail">
    <div class="comment-header">
      <div class="user-info">
        <el-avatar :src="comment.avatar ? formatImageUrl(comment.avatar) : ''" :size="40"></el-avatar>
        <div class="user-name">
          <div class="nickname">{{ comment.nickname || comment.username || '用户' + comment.userId }}</div>
          <div class="time">{{ formatTime(comment.createdAt) }}</div>
        </div>
      </div>
    </div>

    <div class="comment-content">
      <div class="comment-text">{{ comment.content }}</div>
      <div class="post-info">
        <span>回复帖子：</span>
        <span class="post-title">{{ postTitle }}</span>
      </div>
    </div>

    <div class="comment-footer">
      <div class="comment-stats">
        <div class="stat-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>回复</span>
        </div>
        <div class="stat-item">
          <el-icon><Check /></el-icon>
          <span>{{ comment.likeCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Check } from '@element-plus/icons-vue'
import { formatImageUrl } from '../utils/image'
import { getPostDetail } from '../api/community'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const postTitle = ref('')

// 方法
function goToPostDetail() {
  router.push(`/post/${props.comment.postId}`)
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

// 获取帖子标题
async function fetchPostTitle() {
  if (!props.comment.postId) return

  try {
    const res = await getPostDetail(props.comment.postId)
    postTitle.value = res.data.title
  } catch (error) {
    console.error('获取帖子标题失败:', error)
    postTitle.value = '未知帖子'
  }
}

onMounted(() => {
  fetchPostTitle()
})
</script>

<style lang="scss" scoped>
.comment-card {
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

  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .user-info {
      display: flex;
      align-items: center;

      .user-name {
        margin-left: 10px;

        .nickname {
          font-size: 14px;
          font-weight: bold;
          color: #333;
        }

        .time {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }

  .comment-content {
    margin-bottom: 12px;

    .comment-text {
      font-size: 14px;
      color: #333;
      margin-bottom: 10px;
      line-height: 1.5;
    }

    .post-info {
      font-size: 12px;
      color: #666;
      background-color: #f9f9f9;
      padding: 8px 12px;
      border-radius: 4px;
      
      .post-title {
        color: #409EFF;
        font-weight: 500;
      }
    }
  }

  .comment-footer {
    .comment-stats {
      display: flex;
      justify-content: flex-end;

      .stat-item {
        display: flex;
        align-items: center;
        color: #999;
        font-size: 12px;
        margin-left: 15px;

        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
}
</style>
