<template>
  <div class="order-detail-container">
    <el-card class="order-detail-card">
      <template #header>
        <div class="card-header">
          <h2>订单详情</h2>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="order" class="order-detail">
        <div class="order-status">
          <el-steps :active="getStatusStep(order.status)" finish-status="success">
            <el-step title="待付款"></el-step>
            <el-step title="待发货"></el-step>
            <el-step title="待收货"></el-step>
            <el-step title="已完成"></el-step>
          </el-steps>
        </div>

        <div class="order-info">
          <el-descriptions title="订单信息" :column="1" border>
            <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag  :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(order.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间" v-if="order.paymentTime">{{ formatDateTime(order.paymentTime) }}</el-descriptions-item>
            <el-descriptions-item label="发货时间" v-if="order.shippingTime">{{ formatDateTime(order.shippingTime) }}</el-descriptions-item>
            <el-descriptions-item label="完成时间" v-if="order.completionTime">{{ formatDateTime(order.completionTime) }}</el-descriptions-item>
            <el-descriptions-item label="取消时间" v-if="order.cancelTime">{{ formatDateTime(order.cancelTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="buyer-info">
          <el-descriptions title="买家信息" :column="1" border>
            <el-descriptions-item label="买家ID">{{ order.userId || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="买家昵称">{{ order.nickname || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="收货人">{{ order.shippingName || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ order.shippingPhone || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ order.shippingAddress || '暂无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="order-items">
          <h3>商品信息</h3>
          <el-table :data="order.orderItems" border style="width: 100%">
            <el-table-column label="商品信息" min-width="300">
              <template #default="scope">
                <div class="product-info">
                  <el-image
                    :src="formatImageUrl(scope.row.productImage)"
                    class="product-image"
                    fit="cover"
                  ></el-image>
                  <div class="product-detail">
                    <div class="product-name">{{ scope.row.productName }}</div>
                    <div class="product-price">¥{{ formatPrice(scope.row.price) }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
            <el-table-column label="小计" width="120">
              <template #default="scope">
                ¥{{ formatPrice(scope.row.totalPrice || (scope.row.price * scope.row.quantity)) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="order-payment">
          <el-descriptions title="支付信息" :column="1" border>
            <el-descriptions-item label="商品总额">¥{{ formatPrice(order.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="运费">¥{{ formatPrice(0) }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">
              <span class="payment-amount">¥{{ formatPrice(order.status > 0 ? order.payAmount : order.totalAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ getPaymentMethod(order.payType) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="order-actions">
          <el-button
            v-if="order.status === 1"
            type="primary"
            @click="handleShip"
            :loading="actionLoading"
          >
            发货
          </el-button>
          <el-button
            v-if="order.status === 5"
            type="success"
            @click="handleRefund(1)"
            :loading="actionLoading"
          >
            同意退款
          </el-button>
          <el-button
            v-if="order.status === 5"
            type="danger"
            @click="handleRefund(2)"
            :loading="actionLoading"
          >
            拒绝退款
          </el-button>
        </div>
      </div>

      <el-empty v-else description="订单不存在或已被删除"></el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, shipOrder, handleRefund as apiHandleRefund } from '../../api/seller'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const actionLoading = ref(false)
const order = ref(null)

// 获取订单详情
async function fetchOrderDetail() {
  const orderId = route.params.id
  if (!orderId) {
    ElMessage.error('订单ID不能为空')
    return
  }

  try {
    loading.value = true
    const res = await getOrderDetail(orderId)
    if (res.data) {
      order.value = res.data
      console.log('订单详情:', order.value)

      // 确保金额字段有正确的值
      if (order.value.totalAmount === null || order.value.totalAmount === undefined) {
        // 如果总金额为空，尝试从订单项计算
        if (order.value.orderItems && Array.isArray(order.value.orderItems)) {
          order.value.totalAmount = order.value.orderItems.reduce((sum, item) => {
            return sum + (item.price || 0) * (item.quantity || 1)
          }, 0)
        } else {
          order.value.totalAmount = 0
        }
      }

      // 确保支付金额有正确的值
      if (order.value.payAmount === null || order.value.payAmount === undefined || order.value.payAmount === 0) {
        // 如果订单已支付（状态为1或以上），但支付金额为0，则使用总金额
        if (order.value.status >= 1) {
          order.value.payAmount = order.value.totalAmount
        } else {
          order.value.payAmount = order.value.totalAmount
        }
      }

      // 确保订单项数据的完整性
      if (order.value.orderItems && Array.isArray(order.value.orderItems)) {
        order.value.orderItems.forEach(item => {
          // 确保数值字段有正确的值
          if (item.quantity === null || item.quantity === undefined) item.quantity = 1
          if (item.price === null || item.price === undefined) item.price = 0
          if (item.totalPrice === null || item.totalPrice === undefined) item.totalPrice = item.price * item.quantity
        })
      }
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 格式化价格
function formatPrice(price) {
  if (!price || isNaN(price)) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 格式化日期时间
function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态文本
function getStatusText(status) {
  switch (status) {
    case 0: return '待付款'
    case 1: return '待发货'
    case 2: return '待收货'
    case 3: return '已完成'
    case 4: return '已取消'
    case 5: return '退款中'
    case 6: return '已退款'
    case 7: return '退款拒绝'
    default: return '未知'
  }
}

// 获取状态类型
function getStatusType(status) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'success'
    case 4: return 'info'
    case 5: return 'danger'
    case 6: return 'info'
    case 7: return 'danger'
    default: return 'info'
  }
}

// 获取状态步骤
function getStatusStep(status) {
  switch (status) {
    case 0: return 0
    case 1: return 1
    case 2: return 2
    case 3: return 3
    case 4: return 0
    case 5: return 1
    case 6: return 1
    case 7: return 1
    default: return 0
  }
}

// 获取支付方式
function getPaymentMethod(payType) {
  switch (payType) {
    case 1: return '支付宝'
    case 2: return '微信支付'
    case 3: return '银行卡'
    default: return '未支付'
  }
}

// 发货
async function handleShip() {
  if (!order.value || !order.value.id) return

  try {
    actionLoading.value = true
    await ElMessageBox.confirm('确认发货该订单?', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await shipOrder(order.value.id)
    ElMessage.success('发货成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败，请重试')
    }
  } finally {
    actionLoading.value = false
  }
}

// 处理退款
async function handleRefund(action) {
  if (!order.value || !order.value.id) return

  try {
    actionLoading.value = true

    if (action === 1) {
      // 同意退款
      await ElMessageBox.confirm('确认同意退款?', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await apiHandleRefund(order.value.id, 1)
      ElMessage.success('已同意退款')
    } else {
      // 拒绝退款
      const { value: reason } = await ElMessageBox.prompt(
        '请输入拒绝原因',
        '拒绝退款',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          inputValidator: (value) => {
            if (!value) {
              return '拒绝原因不能为空'
            }
            return true
          }
        }
      )

      if (reason) {
        await apiHandleRefund(order.value.id, 2, reason)
        ElMessage.success('已拒绝退款')
      }
    }

    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('处理退款失败:', error)
      ElMessage.error('处理退款失败，请重试')
    }
  } finally {
    actionLoading.value = false
  }
}

// 返回列表
function goBack() {
  router.push('/seller-orders')
}

// 生命周期钩子
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style lang="scss" scoped>
.order-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px 0;

  .order-detail-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .loading-container {
      padding: 20px 0;
    }

    .order-detail {
      .order-status {
        margin-bottom: 30px;
      }

      .order-info,
      .buyer-info,
      .order-payment {
        margin-bottom: 30px;
      }

      .order-items {
        margin-bottom: 30px;

        h3 {
          margin-bottom: 15px;
        }

        .product-info {
          display: flex;
          align-items: center;

          .product-image {
            width: 60px;
            height: 60px;
            margin-right: 10px;
            border-radius: 4px;
          }

          .product-detail {
            .product-name {
              font-size: 14px;
              margin-bottom: 5px;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
            }

            .product-price {
              color: #f56c6c;
              font-weight: bold;
            }
          }
        }
      }

      .payment-amount {
        color: #f56c6c;
        font-size: 18px;
        font-weight: bold;
      }

      .order-actions {
        margin-top: 30px;
        text-align: center;
      }
    }
  }
}
</style>
