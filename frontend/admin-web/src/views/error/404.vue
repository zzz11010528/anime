<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
  
  .not-found-content {
    text-align: center;
    
    .error-code {
      font-size: 120px;
      color: #409EFF;
      margin: 0;
      line-height: 1.2;
    }
    
    .error-message {
      font-size: 24px;
      color: #606266;
      margin: 20px 0 30px;
    }
  }
}
</style>
