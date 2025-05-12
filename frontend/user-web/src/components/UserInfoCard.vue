<template>
  <el-card class="user-info-card">
    <div class="user-header">
      <el-avatar :src="userInfo.avatar ? formatImageUrl(userInfo.avatar) : defaultAvatar" :size="80"></el-avatar>
      <div class="user-name">{{ userInfo.nickname || userInfo.username }}</div>
    </div>
    
    <div class="user-stats">
      <div class="stat-item">
        <div class="stat-value">{{ userInfo.postCount || 0 }}</div>
        <div class="stat-label">帖子</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ userInfo.commentCount || 0 }}</div>
        <div class="stat-label">评论</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ userInfo.likeCount || 0 }}</div>
        <div class="stat-label">获赞</div>
      </div>
    </div>
    
    <div class="user-actions" v-if="!isSelf && isLoggedIn">
      <el-button type="primary" @click="handleSendMessage">
        <el-icon><Message /></el-icon>
        发送私信
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { formatImageUrl } from '../utils/image'
import defaultAvatar from '../assets/images/default-avatar.png'

const props = defineProps({
  userInfo: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const userStore = useUserStore()

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUser = computed(() => userStore.userInfo)
const isSelf = computed(() => currentUser.value.id === props.userInfo.id)

// 方法
function handleSendMessage() {
  router.push({
    path: '/message',
    query: { toUserId: props.userInfo.id }
  })
}
</script>

<style lang="scss" scoped>
.user-info-card {
  margin-bottom: 20px;
  
  .user-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    
    .el-avatar {
      margin-bottom: 15px;
    }
    
    .user-name {
      font-size: 18px;
      font-weight: bold;
    }
  }
  
  .user-stats {
    display: flex;
    justify-content: space-around;
    padding: 15px 0;
    border-top: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        font-size: 18px;
        font-weight: bold;
        color: #409EFF;
      }
      
      .stat-label {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }
  
  .user-actions {
    padding: 15px 0;
    text-align: center;
  }
}
</style>
