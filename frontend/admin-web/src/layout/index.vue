<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
      <div class="logo-container">
        <img src="../assets/images/logo.jpg" alt="Logo" class="logo">
        <h1 v-if="!isCollapsed" class="title">管理员后台</h1>
      </div>

      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :unique-opened="true"
          :collapse-transition="false"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>控制台</template>
          </el-menu-item>

          <el-sub-menu index="/merchant">
            <template #title>
              <el-icon><Shop /></el-icon>
              <span>商家管理</span>
            </template>
            <el-menu-item index="/merchant/list">商家列表</el-menu-item>
            <el-menu-item index="/merchant/audit">商家审核</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/user/list">用户列表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/product">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/product/list">商品列表</el-menu-item>
            <el-menu-item index="/product/category">分类管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/order">
            <template #title>
              <el-icon><List /></el-icon>
              <span>订单管理</span>
            </template>
            <el-menu-item index="/order/list">订单列表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/community">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>社区管理</span>
            </template>
            <el-menu-item index="/community/category">分类管理</el-menu-item>
            <el-menu-item index="/community/topic">话题管理</el-menu-item>
            <el-menu-item index="/community/post">帖子管理</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/ip/list">
            <el-icon><Promotion /></el-icon>
            <template #title>IP管理</template>
          </el-menu-item>


        </el-menu>
      </el-scrollbar>
    </div>

    <!-- 主要内容区 -->
    <div class="main-container">
      <!-- 头部导航 -->
      <div class="navbar">
        <div class="left-area">
          <el-icon class="fold-btn" @click="toggleSidebar">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
          <breadcrumb />
        </div>

        <div class="right-area">
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="action-item" @click="toggleFullScreen">
              <FullScreen v-if="!isFullscreen" />
              <el-icon v-else><i class="el-icon-close"></i></el-icon>
            </el-icon>
          </el-tooltip>



          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-container">
              <el-avatar :src="avatarSrc" :size="30"></el-avatar>
              <span class="admin-name">{{ adminInfo.name }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled, Shop, User, Goods, List, Document, Promotion,
  Fold, Expand, FullScreen, ArrowDown
} from '@element-plus/icons-vue'
import { useAdminStore } from '../stores/admin'
import Breadcrumb from '../components/Breadcrumb.vue'
import defaultAvatar from '../assets/images/default-avatar.png'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

// 状态
const isCollapsed = ref(false)
const isFullscreen = ref(false)

const cachedViews = ref(['Dashboard'])

// 计算属性
const activeMenu = computed(() => route.path)
const adminInfo = computed(() => adminStore.adminInfo)

// 处理头像URL，确保包含/api前缀
const avatarSrc = computed(() => {
  if (!adminInfo.value.avatar) return defaultAvatar

  // 确保头像URL包含/api前缀
  if (adminInfo.value.avatar.startsWith('http')) {
    return adminInfo.value.avatar
  } else if (adminInfo.value.avatar.startsWith('/api')) {
    return adminInfo.value.avatar
  } else {
    return `/api${adminInfo.value.avatar}`
  }
})

// 方法
function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

function toggleFullScreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}



function handleCommand(command) {
  if (command === 'profile') {
    router.push('/system/profile')
  } else if (command === 'password') {
    router.push('/system/password')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      adminStore.logoutAction()
      router.push('/login')
    }).catch(() => {})
  }
}

// 监听全屏变化
function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}



// 生命周期钩子
onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;

  .sidebar-container {
    width: 210px;
    height: 100%;
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;

    &.is-collapsed {
      width: 64px;
    }

    .logo-container {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 15px;
      background-color: #263445;

      .logo {
        width: 32px;
        height: 32px;
      }

      .title {
        margin-left: 10px;
        color: #fff;
        font-size: 16px;
        font-weight: bold;
        white-space: nowrap;
      }
    }

    .el-menu {
      border-right: none;
    }
  }

  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .navbar {
      height: 60px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 15px;
      background-color: #fff;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

      .left-area {
        display: flex;
        align-items: center;

        .fold-btn {
          font-size: 20px;
          cursor: pointer;
          margin-right: 15px;
        }
      }

      .right-area {
        display: flex;
        align-items: center;

        .action-item {
          font-size: 20px;
          cursor: pointer;
          margin-right: 20px;
        }

        .avatar-container {
          display: flex;
          align-items: center;
          cursor: pointer;

          .admin-name {
            margin: 0 5px;
            font-size: 14px;
          }
        }
      }
    }

    .app-main {
      flex: 1;
      padding: 15px;
      overflow-y: auto;
      background-color: #f0f2f5;
    }
  }
}

// 路由切换动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>
