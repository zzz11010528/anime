<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <el-empty description="页面不存在" :image-size="200"></el-empty>
      <h1>404</h1>
      <h2>页面不存在</h2>
      <p>抱歉，您访问的页面不存在或已被删除</p>
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
  background-color: #f5f7fa;

  .not-found-content {
    text-align: center;
    padding: 40px;

    .not-found-image {
      width: 200px;
      margin-bottom: 20px;
    }

    h1 {
      font-size: 72px;
      color: #409EFF;
      margin: 0;
    }

    h2 {
      font-size: 24px;
      color: #333;
      margin: 10px 0;
    }

    p {
      font-size: 16px;
      color: #666;
      margin-bottom: 30px;
    }
  }
}
</style>
