<template>
  <div class="group-order-container">
    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <h2>拼团订单</h2>
          <el-button type="primary" @click="goBack">返回</el-button>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="!groupOrder.id" class="not-found">
        <el-empty description="订单不存在或已被取消" />
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>

      <template v-else>
        <!-- 拼团信息 -->
        <div class="group-info">
          <h3>拼团信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="拼团ID">{{ groupOrder.id }}</el-descriptions-item>
            <el-descriptions-item label="拼团状态">
              <el-tag :type="groupOrder.status === 0 ? 'warning' : groupOrder.status === 1 ? 'success' : 'info'">
                {{ groupOrder.status === 0 ? '组团中' : groupOrder.status === 1 ? '已成团' : '已解散' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="团长">
              <div class="user-info">
                <el-avatar
                  :src="groupOrder.leaderAvatar ? formatImageUrl(groupOrder.leaderAvatar) : defaultAvatar"
                  :size="30">
                </el-avatar>
                <span>{{ groupOrder.leaderNickname || groupOrder.leaderUsername || '用户' + groupOrder.leaderUserId }}</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="当前人数">{{ groupOrder.currentSize }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(groupOrder.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">
              <div class="expire-time">
                {{ formatDateTime(groupOrder.expireTime) }}
                <el-countdown
                  v-if="groupOrder.status === 0"
                  :value="new Date(groupOrder.expireTime).getTime()"
                  format="剩余 DD 天 HH:mm:ss"
                />
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 商品信息 -->
        <div class="product-info" v-if="groupBuying">
          <h3>商品信息</h3>
          <el-card shadow="never" class="product-card">
            <div class="product-header">
              <el-image :src="formatImageUrl(groupBuying.productImage)" class="product-image" fit="cover" />
              <div class="product-detail">
                <h4 class="product-name">{{ groupBuying.productName }}</h4>
                <div class="price-info">
                  <div class="group-price">拼团价: ¥{{ formatPrice(groupBuying.groupPrice) }}</div>
                  <div class="original-price">原价: ¥{{ formatPrice(groupBuying.productPrice) }}</div>
                </div>
                <div class="group-rule">
                  <span>{{ groupBuying.minGroupSize }}人成团</span>
                  <span v-if="groupOrder.status === 0">
                    还差 <span class="highlight">{{ groupBuying.minGroupSize - groupOrder.currentSize }}</span> 人成团
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 参与用户 -->
        <div class="participants" v-if="groupOrder.orders && groupOrder.orders.length > 0">
          <h3>参与用户</h3>
          <div class="participant-list">
            <div v-for="(order, index) in groupOrder.orders" :key="index" class="participant-item">
              <el-avatar
                :src="order.avatar ? formatImageUrl(order.avatar) : defaultAvatar"
                :size="50">
              </el-avatar>
              <div class="participant-name">{{ order.nickname || order.username || '用户' + order.userId }}</div>
              <div class="participant-status">
                <el-tag :type="order.status >= 1 ? 'success' : 'warning'">
                  {{ order.status >= 1 ? '已支付' : '未支付' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的订单 -->
        <div class="my-order" v-if="myOrder">
          <h3>我的订单</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ myOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getOrderStatusType(myOrder.status)">
                {{ getOrderStatusText(myOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付金额">¥{{ formatPrice(myOrder.payAmount) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(myOrder.createdAt) }}</el-descriptions-item>
          </el-descriptions>

          <div class="order-actions">
            <el-button
              v-if="myOrder.status === 0"
              type="primary"
              @click="handlePay"
            >
              立即支付
            </el-button>
            <el-button
              v-if="myOrder.status === 0"
              type="danger"
              @click="handleCancel"
            >
              取消订单
            </el-button>
            <el-button
              v-if="myOrder.status >= 1 && myOrder.status <= 2"
              type="info"
              @click="viewOrderDetail"
            >
              查看订单详情
            </el-button>
          </div>
        </div>

        <!-- 邀请好友 -->
        <div class="invite-friends" v-if="groupOrder.status === 0">
          <h3>邀请好友</h3>
          <div class="invite-content">
            <p>分享链接邀请好友参团，还差 {{ groupBuying.minGroupSize - groupOrder.currentSize }} 人成团</p>
            <el-input
              v-model="shareLink"
              readonly
              class="share-link"
            >
              <template #append>
                <el-button @click="copyShareLink">复制链接</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGroupOrderDetail } from '../../api/group'
import { getGroupBuyingDetail } from '../../api/group'
import { cancelOrder, payOrder } from '../../api/order'
import { formatImageUrl } from '../../utils/image'
import { useUserStore } from '../../stores/user'
import defaultAvatar from '../../assets/images/default-avatar.png'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(true)
const groupOrder = ref({})
const groupBuying = ref(null)
const myOrder = ref(null)
const shareLink = ref('')

// 计算属性
const currentUserId = computed(() => userStore.userInfo.id)

// 方法
function formatPrice(price) {
  if (!price || isNaN(price)) return '0.00'
  return parseFloat(price).toFixed(2)
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getOrderStatusType(status) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'success'
    case 4: return 'info'
    case 5: return 'danger'
    case 6: return 'info'
    default: return 'info'
  }
}

function getOrderStatusText(status) {
  switch (status) {
    case 0: return '待付款'
    case 1: return '待发货'
    case 2: return '待收货'
    case 3: return '已完成'
    case 4: return '已取消'
    case 5: return '申请退款'
    case 6: return '已退款'
    default: return '未知状态'
  }
}

function goBack() {
  router.go(-1)
}

async function fetchGroupOrderDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getGroupOrderDetail(id)
    console.log('拼团订单详情API响应:', res)

    if (res.data) {
      // 处理拼团订单数据
      const orderData = res.data

      // 确保所有必要的字段都存在
      if (!orderData.orders) {
        orderData.orders = []
      }

      groupOrder.value = orderData

      // 查找当前用户的订单
      if (groupOrder.value.orders && groupOrder.value.orders.length > 0) {
        myOrder.value = groupOrder.value.orders.find(order => order.userId === currentUserId.value)

        // 确保订单金额是数字
        if (myOrder.value && myOrder.value.payAmount) {
          myOrder.value.payAmount = parseFloat(myOrder.value.payAmount)
        }
      }

      // 获取拼团活动详情
      if (groupOrder.value.groupBuyingId) {
        const groupBuyingRes = await getGroupBuyingDetail(groupOrder.value.groupBuyingId)
        console.log('拼团活动详情API响应:', groupBuyingRes)

        if (groupBuyingRes.data) {
          // 处理拼团活动数据
          const groupBuyingData = groupBuyingRes.data

          // 确保价格字段是数字
          if (groupBuyingData.groupPrice) {
            groupBuyingData.groupPrice = parseFloat(groupBuyingData.groupPrice)
          }

          if (groupBuyingData.productPrice) {
            groupBuyingData.productPrice = parseFloat(groupBuyingData.productPrice)
          }

          // 确保图片URL正确
          if (groupBuyingData.productImage) {
            groupBuyingData.productImage = formatImageUrl(groupBuyingData.productImage)
          }

          groupBuying.value = groupBuyingData
        }
      }

      // 生成分享链接
      shareLink.value = `${window.location.origin}/group-buying/${groupOrder.value.groupBuyingId}?groupOrderId=${groupOrder.value.id}`
    }
  } catch (error) {
    console.error('获取拼团订单详情失败:', error)
    ElMessage.error('获取拼团订单详情失败')
  } finally {
    loading.value = false
  }
}

function copyShareLink() {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

function handlePay() {
  if (!myOrder.value) return

  payOrder(myOrder.value.id, 1).then(res => {
    // 如果返回的是支付链接或HTML表单，则处理
    if (res.data && typeof res.data === 'string') {
      if (res.data.includes('<form')) {
        // 创建一个临时div来存放支付表单
        const div = document.createElement('div')
        div.innerHTML = res.data
        document.body.appendChild(div)

        // 提交表单，跳转到支付宝支付页面
        document.forms[0].submit()

        // 移除临时div
        setTimeout(() => {
          document.body.removeChild(div)
        }, 1000)
      } else {
        // 如果是URL，直接打开
        window.open(res.data, '_blank')
      }
    } else {
      ElMessage.success('支付成功')
      fetchGroupOrderDetail()
    }
  }).catch(err => {
    console.error('支付失败:', err)
    ElMessage.error(err.message || '支付失败')
  })
}

function handleCancel() {
  if (!myOrder.value) return

  ElMessageBox.confirm('确定要取消订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cancelOrder(myOrder.value.id).then(() => {
      ElMessage.success('订单已取消')
      fetchGroupOrderDetail()
    }).catch(err => {
      console.error('取消订单失败:', err)
      ElMessage.error(err.message || '取消订单失败')
    })
  }).catch(() => {})
}

function viewOrderDetail() {
  if (!myOrder.value) return
  router.push(`/order/${myOrder.value.id}`)
}

// 生命周期钩子
onMounted(() => {
  fetchGroupOrderDetail()
})
</script>

<style lang="scss" scoped>
.group-order-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;

  .order-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .loading, .not-found {
      padding: 40px;
      text-align: center;
    }

    h3 {
      margin: 20px 0 15px;
      font-size: 18px;
      font-weight: bold;
    }

    .group-info, .my-order, .invite-friends {
      margin-bottom: 30px;
    }

    .product-card {
      border: 1px solid #ebeef5;

      .product-header {
        display: flex;

        .product-image {
          width: 100px;
          height: 100px;
          margin-right: 15px;
          border-radius: 4px;
        }

        .product-detail {
          flex: 1;

          .product-name {
            font-size: 16px;
            margin-bottom: 10px;
          }

          .price-info {
            margin-bottom: 10px;

            .group-price {
              color: #f56c6c;
              font-weight: bold;
              font-size: 18px;
            }

            .original-price {
              color: #999;
              text-decoration: line-through;
              margin-left: 10px;
            }
          }

          .group-rule {
            color: #666;

            .highlight {
              color: #f56c6c;
              font-weight: bold;
            }
          }
        }
      }
    }

    .participants {
      margin-bottom: 30px;

      .participant-list {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;

        .participant-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          width: 80px;

          .participant-name {
            margin: 8px 0;
            font-size: 14px;
            text-align: center;
            width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }

    .order-actions {
      margin-top: 20px;
      display: flex;
      gap: 10px;
    }

    .invite-content {
      background-color: #f8f8f8;
      padding: 15px;
      border-radius: 4px;

      p {
        margin-bottom: 15px;
      }

      .share-link {
        margin-bottom: 10px;
      }
    }

    .user-info {
      display: flex;
      align-items: center;

      .el-avatar {
        margin-right: 8px;
      }
    }

    .expire-time {
      display: flex;
      flex-direction: column;
    }
  }
}

@media (max-width: 768px) {
  .group-order-container {
    padding: 10px;

    .product-card {
      .product-header {
        flex-direction: column;

        .product-image {
          width: 100%;
          height: auto;
          margin-right: 0;
          margin-bottom: 15px;
        }
      }
    }
  }
}
</style>
