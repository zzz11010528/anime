<template>
  <div class="order-list-container">
    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <h2>我的订单</h2>
          <el-radio-group v-model="filterStatus" size="small" @change="handleStatusChange">
            <el-radio-button :label="-1">全部</el-radio-button>
            <el-radio-button :label="0">待付款</el-radio-button>
            <el-radio-button :label="1">待发货</el-radio-button>
            <el-radio-button :label="2">待收货</el-radio-button>
            <el-radio-button :label="3">已完成</el-radio-button>
            <el-radio-button :label="4">已取消</el-radio-button>
            <el-radio-button :label="5">申请退款</el-radio-button>
            <el-radio-button :label="6">已退款</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <el-empty v-else-if="orders.length === 0" description="暂无订单" />

      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-header">
            <div class="order-info">
              <span class="order-id">订单号：{{ order.orderNo }}</span>
              <span class="order-time">下单时间：{{ formatTime(order.createdAt) }}</span>
            </div>
            <div class="order-status">
              <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </div>

          <div class="order-content">
            <!-- 如果有订单项，则显示订单项 -->
            <div v-if="order.orderItems && order.orderItems.length > 0">
              <div v-for="item in order.orderItems" :key="item.id" class="order-product" @click="goToProduct(item.productId)">
                <el-image :src="formatImageUrl(item.productImage)" :alt="item.productName" class="product-image" />
                <div class="product-info">
                  <div class="product-name">{{ item.productName }}</div>
                  <div class="product-attrs" v-if="item.attrs">
                    <span v-for="(attr, index) in item.attrs" :key="index">{{ attr.name }}: {{ attr.value }}</span>
                  </div>
                </div>
                <div class="product-price">¥{{ formatPrice(item.price) }}</div>
                <div class="product-quantity">x{{ item.quantity }}</div>
                <div class="product-subtotal">¥{{ formatPrice(item.price * item.quantity) }}</div>
              </div>
            </div>
            <!-- 如果没有订单项，则显示简单信息 -->
            <div v-else class="order-product-simple">
              <div class="product-info">
                <div class="product-name">订单号: {{ order.orderNo }}</div>
                <div class="product-price">总金额: ¥{{ formatPrice(order.totalAmount) }}</div>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <div class="order-total">
              共 <span class="count">{{ getTotalQuantity(order) }}</span> 件商品，
              合计：<span class="price">¥{{ formatPrice(order.totalAmount) }}</span>
              <span v-if="order.shippingFee">（含运费 ¥{{ formatPrice(order.shippingFee) }}）</span>
            </div>

            <div class="order-actions">
              <el-button v-if="order.status === 0" type="primary" @click="handlePay(order)">支付宝支付</el-button>
              <el-button v-if="order.status === 0" type="danger" @click="handleCancel(order)">取消订单</el-button>
              <el-button v-if="order.status === 2" type="success" @click="handleReceive(order)">确认收货</el-button>
              <el-button v-if="order.status === 3" type="warning" @click="handleReview(order)">评价</el-button>
              <el-button v-if="[1, 2].includes(order.status)" type="danger" @click="handleRefund(order)">申请退款</el-button>
              <el-button v-if="order.status === 5" type="info" disabled>退款处理中</el-button>
              <el-button type="info" @click="goToOrderDetail(order.id)">订单详情</el-button>
            </div>
          </div>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, cancelOrder, payOrder, receiveOrder, applyRefund } from '../../api/order'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()

// 状态
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filterStatus = ref(-1)

// 监听路由参数变化
watch(
  () => route.query,
  (query) => {
    // 从URL参数更新筛选条件
    filterStatus.value = Number(query.status || -1)

    // 更新分页
    page.value = Number(query.page || 1)
    size.value = Number(query.size || 10)

    // 获取订单列表
    fetchOrders()
  },
  { immediate: true }
)

// 方法
async function fetchOrders() {
  loading.value = true

  try {
    // 构建请求参数
    const params = {
      page: page.value,
      size: size.value
    }

    if (filterStatus.value >= 0) {
      params.status = filterStatus.value
    }

    const res = await getOrderList(params)
    orders.value = res.data.records || []
    total.value = res.data.total || 0

    // 处理订单项数据
    orders.value.forEach(order => {
      if (!order.orderItems) {
        order.orderItems = []
      }

      // 确保每个订单项都有正确的图片URL
      order.orderItems.forEach(item => {
        if (item && item.productImage) {
          item.productImage = formatImageUrl(item.productImage)
        }
      })
    })
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理状态变化
function handleStatusChange() {
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 处理分页大小变化
function handleSizeChange() {
  updateRouteQuery()
}

// 处理页码变化
function handleCurrentChange() {
  updateRouteQuery()
}

// 更新路由查询参数
function updateRouteQuery() {
  const query = {}

  if (filterStatus.value >= 0) {
    query.status = filterStatus.value
  }

  if (page.value > 1) {
    query.page = page.value
  }

  if (size.value !== 10) {
    query.size = size.value
  }

  router.push({ path: '/order', query })
}

// 获取订单状态文本
function getStatusText(status) {
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

// 获取订单状态类型
function getStatusType(status) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return ''
    case 4: return 'info'
    case 5: return 'danger'
    case 6: return 'info'
    default: return 'info'
  }
}

// 获取订单商品总数量
function getTotalQuantity(order) {
  if (!order.orderItems || order.orderItems.length === 0) {
    return 1; // 默认显示1件商品
  }
  return order.orderItems.reduce((total, item) => total + item.quantity, 0)
}

// 格式化价格
function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''

  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 跳转到商品详情
function goToProduct(productId) {
  router.push(`/product/${productId}`)
}

// 跳转到订单详情
function goToOrderDetail(orderId) {
  router.push(`/order/${orderId}`)
}

// 处理支付
async function handlePay(order) {
  try {
    // 调用支付接口获取支付链接
    const res = await payOrder(order.id, 1)
    if (res.data) {
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
      ElMessage.error('获取支付链接失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '支付失败')
  }
}

// 处理取消订单
async function handleCancel(order) {
  try {
    ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await cancelOrder(order.id)
      ElMessage.success('订单已取消')
      fetchOrders()
    })
  } catch (error) {
    ElMessage.error(error.message || '取消订单失败')
  }
}

// 处理确认收货
async function handleReceive(order) {
  try {
    ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await receiveOrder(order.id)
      ElMessage.success('确认收货成功')
      fetchOrders()
    })
  } catch (error) {
    ElMessage.error(error.message || '确认收货失败')
  }
}

// 处理评价
function handleReview(order) {
  router.push({
    path: `/order/${order.id}`,
    query: { review: 'true' }
  })
}

// 处理申请退款
async function handleRefund(order) {
  try {
    ElMessageBox.prompt('请输入退款原因', '申请退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入退款原因'
    }).then(async ({ value }) => {
      await applyRefund(order.id, value)
      ElMessage.success('退款申请已提交')
      fetchOrders()
    })
  } catch (error) {
    ElMessage.error(error.message || '申请退款失败')
  }
}

// 生命周期钩子
onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.order-list-container {
  max-width: 1200px;
  margin: 0 auto;

  .order-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        font-size: 20px;
      }
    }

    .loading {
      padding: 20px 0;
    }

    .order-list {
      .order-item {
        background-color: #f8f8f8;
        border-radius: 4px;
        margin-bottom: 20px;
        overflow: hidden;

        .order-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px;
          background-color: #fff;
          border-bottom: 1px solid #eee;

          .order-info {
            .order-id {
              margin-right: 20px;
              font-weight: bold;
            }

            .order-time {
              color: #666;
            }
          }
        }

        .order-content {
          background-color: #fff;

          .order-product {
            display: flex;
            align-items: center;
            padding: 15px;
            border-bottom: 1px solid #f5f5f5;
            cursor: pointer;

            &:hover {
              background-color: #f9f9f9;
            }

            .product-image {
              width: 80px;
              height: 80px;
              object-fit: cover;
              margin-right: 15px;
            }

            .product-info {
              flex: 1;

              .product-name {
                font-size: 14px;
                margin-bottom: 5px;
              }

              .product-attrs {
                font-size: 12px;
                color: #999;

                span {
                  margin-right: 10px;
                }
              }
            }

            .product-price,
            .product-quantity,
            .product-subtotal {
              margin-left: 15px;
              text-align: center;
              width: 80px;
            }

            .product-subtotal {
              font-weight: bold;
              color: #f56c6c;
            }
          }

          .order-product-simple {
            display: flex;
            align-items: center;
            padding: 15px;
            border-bottom: 1px solid #f5f5f5;

            .product-info {
              flex: 1;

              .product-name {
                font-size: 14px;
                margin-bottom: 5px;
              }

              .product-price {
                font-weight: bold;
                color: #f56c6c;
              }
            }
          }
        }

        .order-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px;
          background-color: #fff;

          .order-total {
            .count {
              color: #f56c6c;
              font-weight: bold;
            }

            .price {
              color: #f56c6c;
              font-size: 18px;
              font-weight: bold;
            }
          }

          .order-actions {
            display: flex;
            gap: 10px;
          }
        }
      }

      .pagination {
        margin-top: 20px;
        text-align: center;
      }
    }
  }
}

@media (max-width: 768px) {
  .order-list-container {
    .order-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;

        h2 {
          margin-bottom: 10px;
        }
      }

      .order-list {
        .order-item {
          .order-header {
            flex-direction: column;
            align-items: flex-start;

            .order-info {
              margin-bottom: 10px;
            }
          }

          .order-content {
            .order-product {
              flex-wrap: wrap;

              .product-image {
                margin-bottom: 10px;
              }

              .product-info {
                width: 100%;
                margin-bottom: 10px;
              }

              .product-price,
              .product-quantity,
              .product-subtotal {
                margin-left: 0;
                width: 33.33%;
              }
            }
          }

          .order-footer {
            flex-direction: column;

            .order-total {
              margin-bottom: 10px;
            }

            .order-actions {
              width: 100%;
              flex-wrap: wrap;
              justify-content: flex-end;
            }
          }
        }
      }
    }
  }
}
</style>
