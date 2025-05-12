<template>
  <div class="group-buying-detail-container">
    <el-card v-if="loading" class="loading-card">
      <el-skeleton :rows="10" animated />
    </el-card>

    <div v-else-if="!groupBuying.id" class="not-found">
      <el-empty description="拼团活动不存在或已结束" />
      <el-button type="primary" @click="goBack">返回首页</el-button>
    </div>

    <template v-else>
      <!-- 拼团商品信息 -->
      <el-card class="product-card">
        <div class="product-header">
          <div class="product-image">
            <el-image :src="formatImageUrl(groupBuying.productImage)" fit="cover" />
          </div>
          <div class="product-info">
            <h1 class="product-name">{{ groupBuying.productName }}</h1>
            <div class="price-info">
              <div class="group-price">拼团价: ¥{{ formatPrice(groupBuying.groupPrice) }}</div>
              <div class="original-price">原价: ¥{{ formatPrice(groupBuying.productPrice) }}</div>
            </div>
            <div class="group-rules">
              <div class="rule-item">
                <span class="label">拼团人数:</span>
                <span class="value">{{ groupBuying.minGroupSize }}人成团</span>
              </div>
              <div class="rule-item">
                <span class="label">活动时间:</span>
                <span class="value">{{ formatDateTime(groupBuying.startTime) }} 至 {{ formatDateTime(groupBuying.endTime) }}</span>
              </div>
              <div class="rule-item">
                <span class="label">拼团状态:</span>
                <span class="value" :class="getStatusClass(groupBuying.status)">{{ getStatusText(groupBuying.status) }}</span>
              </div>
            </div>
            <div class="action-buttons">
              <el-button type="primary" size="large" @click="handleCreateGroup" :disabled="groupBuying.status !== 1">发起拼团</el-button>
              <el-button type="default" size="large" @click="goToProduct">查看商品详情</el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 正在拼团列表 -->
      <el-card class="grouping-card" v-if="groupingOrders.length > 0">
        <template #header>
          <div class="card-header">
            <h2>正在拼团</h2>
          </div>
        </template>
        <div class="grouping-list">
          <div v-for="order in groupingOrders" :key="order.id" class="grouping-item">
            <div class="leader-info">
              <el-avatar
                :src="order.leaderAvatar ? formatImageUrl(order.leaderAvatar) : defaultAvatar"
                :size="50">
              </el-avatar>
              <div class="leader-name">{{ order.leaderNickname || order.leaderUsername || '用户' + order.leaderUserId }}</div>
            </div>
            <div class="group-progress">
              <div class="progress-text">
                已有 <span class="highlight">{{ order.currentSize }}</span> 人参团，
                还差 <span class="highlight">{{ groupBuying.minGroupSize - order.currentSize }}</span> 人成团
              </div>
              <el-progress :percentage="(order.currentSize / groupBuying.minGroupSize) * 100" :format="() => ''" />
            </div>
            <div class="group-expire">
              <el-countdown :value="new Date(order.expireTime).getTime()" format="剩余 DD 天 HH:mm:ss" />
            </div>
            <el-button
              type="danger"
              @click="handleJoinGroup(order.id)"
              :disabled="isUserInGroup(order) || isUserLeader(order)">
              {{ isUserLeader(order) ? '我是团长' : isUserInGroup(order) ? '已参团' : '参加拼团' }}
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 商品详情 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <h2>商品详情</h2>
          </div>
        </template>
        <div class="product-detail" v-html="productDetail"></div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGroupBuyingDetail, createGroup, joinGroup } from '../../api/group'
import { getProductDetail } from '../../api/product'
import { formatImageUrl } from '../../utils/image'
import { useUserStore } from '../../stores/user'
import defaultAvatar from '../../assets/images/default-avatar.png'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const groupBuying = ref({})
const groupingOrders = ref([])
const productDetail = ref('')
const loading = ref(true)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUserId = computed(() => userStore.userInfo.id)

// 方法
function formatPrice(price) {
  if (!price || isNaN(price)) return '0.00'
  return parseFloat(price).toFixed(2)
}

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getStatusText(status) {
  switch (status) {
    case 0: return '已结束'
    case 1: return '进行中'
    case 2: return '未开始'
    default: return '未知状态'
  }
}

function getStatusClass(status) {
  switch (status) {
    case 0: return 'status-ended'
    case 1: return 'status-active'
    case 2: return 'status-pending'
    default: return ''
  }
}

function isUserInGroup(order) {
  // 检查当前用户是否已经在这个拼团中
  return order.participants && order.participants.some(p => p.userId === currentUserId.value)
}

function isUserLeader(order) {
  // 检查当前用户是否是团长
  return order.leaderUserId === currentUserId.value
}

function goBack() {
  router.push('/')
}

function goToProduct() {
  router.push(`/product/${groupBuying.value.productId}`)
}

async function fetchGroupBuyingDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getGroupBuyingDetail(id)
    groupBuying.value = res.data

    // 获取正在拼团的订单
    if (res.data.groupingOrders) {
      groupingOrders.value = res.data.groupingOrders
    }

    // 获取商品详情
    if (res.data.productId) {
      const productRes = await getProductDetail(res.data.productId)
      productDetail.value = productRes.data.detail || '暂无详情'
    }
  } catch (error) {
    console.error('获取拼团详情失败:', error)
    ElMessage.error('获取拼团详情失败')
  } finally {
    loading.value = false
  }
}

async function handleCreateGroup() {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('请先登录后再发起拼团', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }

  try {
    // 创建拼团订单
    const res = await createGroup(groupBuying.value.id)
    const groupOrderId = res.data

    // 跳转到拼团订单页面
    ElMessage.success('发起拼团成功，请完成支付')
    router.push(`/group-order/${groupOrderId}`)
  } catch (error) {
    console.error('发起拼团失败:', error)
    ElMessage.error(error.message || '发起拼团失败')
  }
}

async function handleJoinGroup(groupOrderId) {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('请先登录后再参加拼团', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }

  try {
    // 加入拼团
    const res = await joinGroup(groupOrderId)

    // 跳转到拼团订单页面
    ElMessage.success('参加拼团成功，请完成支付')
    router.push(`/group-order/${groupOrderId}`)
  } catch (error) {
    console.error('参加拼团失败:', error)
    ElMessage.error(error.message || '参加拼团失败')
  }
}

// 生命周期钩子
onMounted(() => {
  fetchGroupBuyingDetail()
})
</script>

<style lang="scss" scoped>
.group-buying-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .loading-card,
  .not-found {
    padding: 40px;
    text-align: center;
  }

  .product-card {
    margin-bottom: 20px;

    .product-header {
      display: flex;
      gap: 30px;

      .product-image {
        width: 400px;
        height: 400px;
        flex-shrink: 0;

        .el-image {
          width: 100%;
          height: 100%;
          border-radius: 8px;
        }
      }

      .product-info {
        flex: 1;

        .product-name {
          font-size: 24px;
          margin-bottom: 20px;
        }

        .price-info {
          margin-bottom: 20px;

          .group-price {
            font-size: 28px;
            color: #f56c6c;
            font-weight: bold;
            margin-bottom: 5px;
          }

          .original-price {
            font-size: 16px;
            color: #999;
            text-decoration: line-through;
          }
        }

        .group-rules {
          margin-bottom: 30px;

          .rule-item {
            margin-bottom: 10px;
            font-size: 16px;

            .label {
              color: #666;
              margin-right: 10px;
            }

            .value {
              &.status-active {
                color: #67c23a;
              }

              &.status-ended {
                color: #909399;
              }

              &.status-pending {
                color: #e6a23c;
              }
            }
          }
        }

        .action-buttons {
          display: flex;
          gap: 15px;
        }
      }
    }
  }

  .grouping-card {
    margin-bottom: 20px;

    .grouping-list {
      .grouping-item {
        display: flex;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #eee;

        &:last-child {
          border-bottom: none;
        }

        .leader-info {
          display: flex;
          flex-direction: column;
          align-items: center;
          width: 100px;
          margin-right: 20px;

          .leader-name {
            margin-top: 8px;
            font-size: 14px;
            text-align: center;
            width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .group-progress {
          flex: 1;
          margin-right: 20px;

          .progress-text {
            margin-bottom: 10px;
            font-size: 14px;

            .highlight {
              color: #f56c6c;
              font-weight: bold;
            }
          }
        }

        .group-expire {
          margin-right: 20px;
          font-size: 14px;
          color: #f56c6c;
        }
      }
    }
  }

  .detail-card {
    .product-detail {
      img {
        max-width: 100%;
      }
    }
  }
}

@media (max-width: 768px) {
  .group-buying-detail-container {
    .product-card {
      .product-header {
        flex-direction: column;

        .product-image {
          width: 100%;
          height: auto;
          margin-bottom: 20px;
        }
      }
    }

    .grouping-card {
      .grouping-list {
        .grouping-item {
          flex-direction: column;
          align-items: flex-start;
          gap: 15px;

          .leader-info {
            flex-direction: row;
            width: 100%;
            margin-right: 0;

            .leader-name {
              margin-top: 0;
              margin-left: 10px;
            }
          }

          .group-progress,
          .group-expire {
            width: 100%;
            margin-right: 0;
          }
        }
      }
    }
  }
}
</style>
