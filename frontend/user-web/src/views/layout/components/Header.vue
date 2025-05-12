<template>
  <div class="header-container">
    <div class="header-content">
      <div class="logo" @click="goHome">
        <img src="../../../assets/images/logo.jpg" alt="Logo">
        <span>动漫社区</span>
      </div>

      <div class="nav-menu">
        <el-menu
          :default-active="activeIndex"
          mode="horizontal"
          router
          background-color="transparent"
          text-color="#333"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/product">商城</el-menu-item>
          <el-menu-item index="/community">社区</el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品"
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div v-if="isLoggedIn" class="user-actions">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
            <el-button type="primary" text @click="goToMessage">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>

          <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge">
            <el-button type="primary" text @click="goToCart">
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-avatar">
              <el-avatar :src="avatarUrl" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div v-else class="login-actions">
          <el-button type="primary" @click="goToLogin">登录</el-button>
          <el-button @click="goToRegister">注册</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Bell, ShoppingCart } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { useCartStore } from '../../../stores/cart'
import { getUnreadMessageCount, getUnreadNotificationCount } from '../../../api/message'
import { formatImageUrl } from '../../../utils/image'
import defaultAvatar from '../../../assets/images/default-avatar.png'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

// 状态
const searchKeyword = ref('')
const unreadCount = ref(0)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const cartCount = computed(() => cartStore.cartCount)
const activeIndex = computed(() => {
  const path = route.path
  if (path.startsWith('/home')) return '/home'
  if (path.startsWith('/product')) return '/product'
  if (path.startsWith('/community')) return '/community'
  return ''
})

// 格式化头像URL
const avatarUrl = computed(() => {
  if (!userInfo.value.avatar) return defaultAvatar
  return formatImageUrl(userInfo.value.avatar)
})

// 方法
function goHome() {
  router.push('/')
}

function goToLogin() {
  router.push('/login')
}

function goToRegister() {
  router.push('/register')
}

function goToCart() {
  router.push('/cart')
}

function goToMessage() {
  router.push('/message')
}

function handleSearch() {
  if (!searchKeyword.value.trim()) return

  // 根据当前页面决定搜索类型
  if (activeIndex.value === '/product') {
    router.push({
      path: '/product',
      query: { keyword: searchKeyword.value }
    })
  } else if (activeIndex.value === '/community') {
    router.push({
      path: '/community',
      query: { keyword: searchKeyword.value }
    })
  } else {
    router.push({
      path: '/product',
      query: { keyword: searchKeyword.value }
    })
  }

  searchKeyword.value = ''
}

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/order')
      break
    case 'logout':
      userStore.logoutAction()
      router.push('/')
      break
  }
}

// 获取未读消息数量
async function fetchUnreadCount() {
  if (!isLoggedIn.value) return

  try {
    const [messageRes, notificationRes] = await Promise.all([
      getUnreadMessageCount(),
      getUnreadNotificationCount()
    ])

    unreadCount.value = (messageRes.data || 0) + (notificationRes.data || 0)
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

// 获取购物车数量
async function fetchCartCount() {
  if (!isLoggedIn.value) return

  try {
    await cartStore.fetchCartList()
  } catch (error) {
    console.error('获取购物车数量失败:', error)
  }
}

// 生命周期钩子
onMounted(() => {
  fetchUnreadCount()
  fetchCartCount()
})
</script>

<style lang="scss" scoped>
.header-container {
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 70px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        transform: scale(1.05);
      }

      img {
        height: 45px;
        margin-right: 12px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      span {
        font-size: 22px;
        font-weight: 700;
        background: linear-gradient(to right, #409EFF, #36D1DC);
        -webkit-background-clip: text;
        background-clip: text;
        -webkit-text-fill-color: transparent;
        color: transparent;
      }
    }

    .nav-menu {
      flex: 1;
      margin-left: 50px;

      :deep(.el-menu) {
        border-bottom: none;

        .el-menu-item {
          font-size: 16px;
          font-weight: 500;
          height: 70px;
          line-height: 70px;

          &.is-active {
            font-weight: 600;
            color: #409EFF !important;
            border-bottom: 3px solid #409EFF;
          }

          &:hover {
            background-color: rgba(64, 158, 255, 0.05);
          }
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;

      .search-box {
        width: 220px;
        margin-right: 25px;

        :deep(.el-input__inner) {
          border-radius: 20px;
          padding-left: 15px;
          transition: all 0.3s;

          &:focus {
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
          }
        }

        .search-icon {
          cursor: pointer;
          color: #409EFF;
          font-size: 18px;
        }
      }

      .user-actions {
        display: flex;
        align-items: center;

        .message-badge,
        .cart-badge {
          margin-right: 20px;

          :deep(.el-button) {
            font-size: 20px;

            &:hover {
              color: #409EFF;
            }
          }
        }

        .user-avatar {
          cursor: pointer;
          transition: transform 0.3s;

          &:hover {
            transform: scale(1.1);
          }

          :deep(.el-avatar) {
            border: 2px solid #fff;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
        }
      }

      .login-actions {
        display: flex;
        gap: 12px;

        .el-button {
          padding: 10px 20px;
          border-radius: 20px;
          font-weight: 500;

          &--primary {
            background: linear-gradient(to right, #409EFF, #36D1DC);
            border: none;
            box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 6px 12px rgba(64, 158, 255, 0.4);
            }
          }
        }
      }
    }
  }
}
</style>
