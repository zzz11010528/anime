<template>
  <div class="hot-post-card" @click="goToDetail">
    <div class="post-rank" :class="getRankClass(index)">{{ index + 1 }}</div>
    <div class="post-content">
      <div class="post-title">{{ post.title }}</div>
      <div class="post-meta">
        <div class="post-author">
          <el-avatar :src="post.avatar ? formatImageUrl(post.avatar) : ''" :size="24"></el-avatar>
          <span>{{ post.nickname || post.username || '用户' + post.userId }}</span>
        </div>
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
            <span>{{ post.collectCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'
import { formatImageUrl } from '../utils/image'

const props = defineProps({
  post: {
    type: Object,
    required: true
  },
  index: {
    type: Number,
    required: true
  }
})

const router = useRouter()

// 方法
function goToDetail() {
  router.push(`/post/${props.post.id || props.post.postId}`)
}

function getRankClass(index) {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return ''
}
</script>

<style lang="scss" scoped>
.hot-post-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-color: #e6f7ff;
  }
  
  .post-rank {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background-color: #eee;
    color: #666;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: bold;
    margin-right: 12px;
    flex-shrink: 0;
    
    &.rank-first {
      background-color: #f56c6c;
      color: #fff;
    }
    
    &.rank-second {
      background-color: #e6a23c;
      color: #fff;
    }
    
    &.rank-third {
      background-color: #409eff;
      color: #fff;
    }
  }
  
  .post-content {
    flex: 1;
    overflow: hidden;
    
    .post-title {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .post-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .post-author {
        display: flex;
        align-items: center;
        font-size: 12px;
        color: #666;
        
        .el-avatar {
          margin-right: 6px;
        }
      }
      
      .post-stats {
        display: flex;
        gap: 10px;
        
        .stat-item {
          display: flex;
          align-items: center;
          font-size: 12px;
          color: #999;
          
          .el-icon {
            margin-right: 4px;
            font-size: 14px;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .hot-post-card {
    .post-content {
      .post-meta {
        flex-direction: column;
        align-items: flex-start;
        
        .post-author {
          margin-bottom: 6px;
        }
      }
    }
  }
}
</style>
