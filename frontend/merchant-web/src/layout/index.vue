<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
      <div class="logo-container">
        <img src="../assets/images/logo.jpg" alt="Logo" class="logo">
        <h1 v-if="!isCollapsed" class="title">商家管理中心</h1>
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
          <el-sub-menu index="/product" :disabled="!isCertified">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
              <el-tooltip v-if="!isCertified" content="请先完成商家认证" placement="right">
                <el-icon class="lock-icon"><Lock /></el-icon>
              </el-tooltip>
            </template>
            <el-menu-item index="/product/list" :disabled="!isCertified">商品列表</el-menu-item>
            <el-menu-item index="/product/add" :disabled="!isCertified">添加商品</el-menu-item>
            <el-menu-item index="/product/group" :disabled="!isCertified">拼团管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/order" :disabled="!isCertified">
            <template #title>
              <el-icon><List /></el-icon>
              <span>订单管理</span>
              <el-tooltip v-if="!isCertified" content="请先完成商家认证" placement="right">
                <el-icon class="lock-icon"><Lock /></el-icon>
              </el-tooltip>
            </template>
            <el-menu-item index="/order/list" :disabled="!isCertified">订单列表</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/message" :disabled="!isCertified">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>
              私信管理
              <el-tooltip v-if="!isCertified" content="请先完成商家认证" placement="right">
                <el-icon class="lock-icon"><Lock /></el-icon>
              </el-tooltip>
            </template>
          </el-menu-item>



          <el-menu-item index="/profile">
            <el-icon><Setting /></el-icon>
            <template #title>店铺设置</template>
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
            <i :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
          </el-icon>
          <breadcrumb />
        </div>

        <div class="right-area">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-container">
              <el-avatar :src="avatarSrc" :size="30"></el-avatar>
              <span class="merchant-name">{{ merchantInfo.merchantName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">店铺设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 认证提示 -->
      <div v-if="!isCertified" class="certification-alert">
        <el-alert
          :title="getAlertTitle()"
          :type="getAlertType()"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="alert-content">
              <span>认证状态：{{ certificationStatusText }}</span>
              <div>
                <el-button type="primary" size="small" @click="goToCertification">
                  {{ certificationActionText }}
                </el-button>
                <el-button type="info" size="small" @click="refreshCertificationStatus">
                  刷新状态
                </el-button>
              </div>
            </div>
          </template>
        </el-alert>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Goods, List, ChatDotRound, Setting, ArrowDown, Lock
} from '@element-plus/icons-vue'
import { useMerchantStore } from '../stores/merchant'
import { getCertificationInfo } from '../api/certification'
import Breadcrumb from '../components/Breadcrumb.vue'
import defaultAvatar from '../assets/images/default-avatar.png'
import { formatImageUrl } from '../utils/image'

const route = useRoute()
const router = useRouter()
const merchantStore = useMerchantStore()

// 状态
const isCollapsed = ref(false)
const cachedViews = ref(['ProductList'])

// 计算属性
const activeMenu = computed(() => route.path)
const merchantInfo = computed(() => merchantStore.merchantInfo)

// 处理头像URL
const avatarSrc = computed(() => {
  if (!merchantInfo.value.avatar) return defaultAvatar
  return formatImageUrl(merchantInfo.value.avatar)
})

// 是否已认证
const isCertified = computed(() => {
  // 只有当角色为商家(1)且认证状态为已通过(1)时才算已认证
  return merchantInfo.value.role === 1 && merchantInfo.value.certificationStatus === 1
})

// 认证状态文本
const certificationStatusText = computed(() => {
  const status = merchantInfo.value.certificationStatus
  switch (status) {
    case 0:
      return '审核中'
    case 1:
      return '已认证'
    case 2:
      return '认证被拒绝'
    default:
      return '未认证'
  }
})

// 认证操作文本
const certificationActionText = computed(() => {
  const status = merchantInfo.value.certificationStatus
  switch (status) {
    case 0:
      return '查看详情'
    case 1:
      return '查看详情'
    case 2:
      return '重新认证'
    default:
      return '立即认证'
  }
})

// 方法
function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')  // 店铺设置页面
  } else if (command === 'logout') {
    handleLogout()
  }
}

// 处理退出登录
function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 调用store中的logoutAction函数，它会处理跳转到登录页面
    merchantStore.logoutAction()
  }).catch(() => {})
}

// 跳转到认证页面
function goToCertification() {
  router.push({
    path: '/profile',
    query: { tab: 'certification' }
  })
}

// 获取提示标题
function getAlertTitle() {
  const status = merchantInfo.value.certificationStatus
  switch (status) {
    case 0:
      return '您的商家认证正在审核中，请耐心等待'
    case 1:
      return '恭喜您，商家认证已通过！您可以使用所有商家功能'
    case 2:
      return '很抱歉，您的商家认证申请被拒绝，请重新提交'
    default:
      return '您尚未完成商家认证，请先完成认证才能使用商家功能'
  }
}

// 获取提示类型
function getAlertType() {
  const status = merchantInfo.value.certificationStatus
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'error'
    default:
      return 'warning'
  }
}

// 刷新认证状态
async function refreshCertificationStatus() {
  try {
    // 先获取最新的用户信息
    await merchantStore.fetchUserInfo();

    // 如果是商家角色，获取认证信息
    if (merchantStore.merchantInfo.role === 1) {
      const res = await getCertificationInfo();
      if (res.data) {
        // 更新认证状态
        merchantStore.setMerchantInfo({
          certificationStatus: res.data.certificationStatus
        });

        console.log('布局组件刷新认证状态:', res.data.certificationStatus);
      }
    }
  } catch (error) {
    console.error('刷新认证状态失败:', error);
  }
}

// 监听路由变化，刷新认证状态
watch(() => route.path, () => {
  refreshCertificationStatus();
});

// 页面加载时刷新认证状态
onMounted(() => {
  refreshCertificationStatus();
});
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

      .lock-icon {
        margin-left: 5px;
        color: #F56C6C;
        font-size: 14px;
      }
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

        .logout-btn, .exit-btn {
          font-size: 20px;
          margin-right: 20px;
          padding: 0;
          height: auto;

          .el-icon {
            font-size: 20px;
          }
        }

        .logout-btn {
          color: #409EFF;

          &:hover {
            color: #66b1ff;
          }
        }

        .exit-btn {
          color: #F56C6C;

          &:hover {
            color: #f78989;
          }
        }

        .avatar-container {
          display: flex;
          align-items: center;
          cursor: pointer;

          .merchant-name {
            margin: 0 5px;
            font-size: 14px;
          }
        }
      }
    }

    .certification-alert {
      padding: 10px 15px 0;

      .alert-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 5px;
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
