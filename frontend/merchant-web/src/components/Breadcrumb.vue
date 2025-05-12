<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index" :to="item.path">
      {{ item.meta.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const breadcrumbs = ref([])

// 生成面包屑导航
const getBreadcrumbs = () => {
  // 过滤没有 meta.title 的路由
  const matched = route.matched.filter(item => item.meta && item.meta.title)

  // 如果第一个不是首页，添加首页
  if (matched.length > 0 && matched[0].path !== '/product/list') {
    matched.unshift({
      path: '/product/list',
      meta: { title: '首页' }
    })
  }

  breadcrumbs.value = matched
}

// 监听路由变化
watch(
  () => route.path,
  () => getBreadcrumbs(),
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.el-breadcrumb {
  font-size: 14px;
  line-height: 1;
}
</style>
