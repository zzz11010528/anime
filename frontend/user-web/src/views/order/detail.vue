<template>
  <div class="order-detail-container">
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" animated>
        <template #template>
          <div style="padding: 20px;">
            <el-skeleton-item variant="h1" style="width: 50%;" />
            <el-skeleton-item variant="text" style="margin-top: 20px; width: 100%;" />
            <el-skeleton-item variant="text" style="width: 100%;" />
            <el-skeleton-item variant="text" style="width: 80%;" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <div v-else-if="!order.id" class="not-found">
      <el-empty description="订单不存在" />
      <el-button type="primary" @click="goBack">返回订单列表</el-button>
    </div>

    <template v-else>
      <!-- 订单状态 -->
      <el-card class="status-card">
        <div class="status-header">
          <div class="status-info">
            <el-tag :type="getStatusType(order.status)" size="large">{{ getStatusText(order.status) }}</el-tag>
            <div class="status-desc">{{ getStatusDescription(order.status) }}</div>
          </div>

          <div class="status-actions">
            <el-button v-if="order.status === 0" type="primary" @click="handlePay">支付宝支付</el-button>
            <el-button v-if="order.status === 0" type="danger" @click="handleCancel">取消订单</el-button>
            <el-button v-if="order.status === 2" type="success" @click="handleReceive">确认收货</el-button>
            <el-button v-if="order.status === 3" type="warning" @click="showReviewDialog" :disabled="allItemsReviewed">{{ allItemsReviewed ? '已评价' : '评价' }}</el-button>
            <el-button v-if="[1, 2].includes(order.status)" type="danger" @click="handleRefund">申请退款</el-button>
            <el-button v-if="order.status === 5" type="info" disabled>退款处理中</el-button>
          </div>
        </div>

        <el-steps :active="getStatusStep(order.status)" finish-status="success" simple style="margin-top: 20px;">
          <el-step title="提交订单" :icon="Document" />
          <el-step title="付款成功" :icon="Wallet" />
          <el-step title="商家发货" :icon="Van" />
          <el-step title="确认收货" :icon="Box" />
          <el-step title="交易完成" :icon="CircleCheck" />
        </el-steps>
      </el-card>

      <!-- 订单信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(order.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPaymentMethod(order.payType) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ order.paymentTime ? formatTime(order.paymentTime) : '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ order.shippingTime ? formatTime(order.shippingTime) : '未发货' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ order.receiveTime ? formatTime(order.receiveTime) : '未完成' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 收货信息 -->
      <el-card class="address-card">
        <template #header>
          <div class="card-header">
            <span>收货信息</span>
          </div>
        </template>

        <div class="address-info">
          <div class="info-item">
            <span class="label">收货人：</span>
            <span class="value">{{ order.shippingName || '暂无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话：</span>
            <span class="value">{{ order.shippingPhone || '暂无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">收货地址：</span>
            <span class="value">{{ order.shippingAddress || '暂无' }}</span>
          </div>
        </div>
      </el-card>

      <!-- 商品信息 -->
      <el-card class="product-card">
        <template #header>
          <div class="card-header">
            <span>商品信息</span>
          </div>
        </template>

        <el-table :data="order.orderItems || []" style="width: 100%">
          <el-table-column label="商品信息" min-width="400">
            <template #default="{ row }">
              <div class="product-info">
                <el-image :src="formatImageUrl(row.productImage)" :alt="row.productName" class="product-image" @click="goToProduct(row.productId)" />
                <div class="product-details">
                  <div class="product-name" @click="goToProduct(row.productId)">{{ row.productName }}</div>
                  <div class="product-attrs" v-if="row.attrs">
                    <span v-for="(attr, index) in row.attrs" :key="index">{{ attr.name }}: {{ attr.value }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" prop="price">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="100" prop="quantity" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥{{ formatPrice(row.price * row.quantity) }}</template>
          </el-table-column>
          <el-table-column label="评价状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.reviewed === 1" type="success">已评价</el-tag>
              <el-tag v-else type="info">未评价</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 金额信息 -->
      <el-card class="amount-card">
        <template #header>
          <div class="card-header">
            <span>金额信息</span>
          </div>
        </template>

        <div class="amount-info">
          <div class="info-item">
            <span class="label">商品总额：</span>
            <span class="value">¥{{ formatPrice(order.totalAmount) }}</span>
          </div>
          <div class="info-item">
            <span class="label">运费：</span>
            <span class="value">¥{{ formatPrice(0) }}</span>
          </div>
          <div class="info-item">
            <span class="label">优惠金额：</span>
            <span class="value">-¥{{ formatPrice(0) }}</span>
          </div>
          <div class="info-item total">
            <span class="label">实付金额：</span>
            <span class="value price">¥{{ formatPrice(order.payAmount) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 物流信息 -->
      <el-card class="logistics-card" v-if="order.status >= 2">
        <template #header>
          <div class="card-header">
            <span>物流信息</span>
          </div>
        </template>

        <div class="logistics-info">
          <div class="info-item">
            <span class="label">物流公司：</span>
            <span class="value">{{ order.shippingCompany || '暂无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">物流单号：</span>
            <span class="value">{{ order.trackingNumber || '暂无' }}</span>
            <el-button v-if="order.trackingNumber" type="primary" link @click="trackLogistics">查看物流</el-button>
          </div>
        </div>

        <div class="logistics-timeline" v-if="logistics.length > 0">
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in logistics"
              :key="index"
              :timestamp="activity.time"
              :type="index === 0 ? 'primary' : ''"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>
    </template>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="商品评价"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="review-form">
        <div v-if="unreviewedItems.length === 0" class="no-items">
          <el-empty description="所有商品都已评价" />
        </div>
        <div v-else v-for="item in unreviewedItems" :key="item.id" class="review-item">
          <div class="product-info">
            <el-image :src="formatImageUrl(item.productImage)" :alt="item.productName" class="product-image" />
            <div class="product-name">{{ item.productName }}</div>
          </div>

          <div class="review-content">
            <div class="rating">
              <span class="label">评分：</span>
              <el-rate v-model="item.rating" allow-half />
            </div>

            <div class="content">
              <span class="label">评价内容：</span>
              <el-input
                v-model="item.reviewContent"
                type="textarea"
                :rows="3"
                placeholder="请输入评价内容"
                maxlength="500"
                show-word-limit
              />
            </div>

            <div class="images">
              <span class="label">上传图片：</span>
              <el-upload
                action="/api/file/upload/review"
                list-type="picture-card"
                :limit="5"
                :on-success="(res) => handleImageSuccess(res, item)"
                :on-remove="(file) => handleImageRemove(file, item)"
                :before-upload="beforeImageUpload"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitReview"
            :loading="reviewing"
            :disabled="unreviewedItems.length === 0"
          >
            {{ unreviewedItems.length === 0 ? '已全部评价' : '提交评价' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Wallet, Van, Box, CircleCheck, Plus } from '@element-plus/icons-vue'
import { getOrderDetail, cancelOrder, payOrder, receiveOrder, applyRefund } from '../../api/order'
import { publishReview } from '../../api/review'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()

// 状态
const order = ref({})
const loading = ref(true)
const logistics = ref([])
const reviewDialogVisible = ref(false)
const reviewing = ref(false)

// 计算属性
const isReviewMode = computed(() => route.query.review === 'true')

// 判断是否所有商品都已评价
const allItemsReviewed = computed(() => {
  if (!order.value.orderItems || order.value.orderItems.length === 0) {
    return false
  }
  return order.value.orderItems.every(item => item.reviewed === 1)
})

// 获取未评价的商品列表
const unreviewedItems = computed(() => {
  if (!order.value.orderItems) {
    return []
  }
  return order.value.orderItems.filter(item => item.reviewed !== 1)
})

// 方法
async function fetchOrderDetail() {
  loading.value = true
  try {
    const orderId = route.params.id
    const res = await getOrderDetail(orderId)
    order.value = res.data

    // 初始化评价数据
    if (order.value.orderItems) {
      order.value.orderItems.forEach(item => {
        item.rating = 5
        item.reviewContent = ''
        item.reviewImages = []
      })
    }

    // 获取物流信息
    if (order.value.status >= 2 && order.value.trackingNumber) {
      fetchLogistics()
    }

    // 如果是评价模式，自动打开评价对话框
    if (isReviewMode.value && order.value.status === 4) {
      showReviewDialog()
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    order.value = {}
  } finally {
    loading.value = false
  }
}

// 获取物流信息
async function fetchLogistics() {
  // TODO: 实现获取物流信息
  logistics.value = [
    { time: '2023-07-10 15:30:00', content: '已签收，签收人：本人' },
    { time: '2023-07-10 10:15:00', content: '快递已到达，正在派送中' },
    { time: '2023-07-09 18:20:00', content: '已到达【北京海淀区中关村营业部】' },
    { time: '2023-07-08 08:30:00', content: '已发货，从【上海仓库】发出' }
  ]
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

// 获取订单状态描述
function getStatusDescription(status) {
  switch (status) {
    case 0: return '请在24小时内完成支付，超时订单将自动取消'
    case 1: return '商家正在处理您的订单，请耐心等待'
    case 2: return '商品已发货，请注意查收'
    case 3: return '交易已完成，感谢您的购买'
    case 4: return '订单已取消'
    case 5: return '退款申请正在处理中，请耐心等待'
    case 6: return '退款已完成，资金将原路退回'
    default: return ''
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

// 获取订单状态步骤
function getStatusStep(status) {
  switch (status) {
    case 0: return 0
    case 1: return 1
    case 2: return 2
    case 3: return 4
    case 4: return 0
    case 5: return 1
    case 6: return 1
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

// 格式化价格
function formatPrice(price) {
  if (!price || isNaN(price)) return '0.00'
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

// 返回订单列表
function goBack() {
  router.push('/order')
}

// 查看物流
function trackLogistics() {
  // TODO: 实现查看物流功能
  ElMessage.info('物流查询功能开发中')
}

// 处理支付
async function handlePay() {
  try {
    // 调用支付接口获取支付链接
    const res = await payOrder(order.value.id, 1)
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
async function handleCancel() {
  try {
    ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await cancelOrder(order.value.id)
      ElMessage.success('订单已取消')
      fetchOrderDetail()
    })
  } catch (error) {
    ElMessage.error(error.message || '取消订单失败')
  }
}

// 处理确认收货
async function handleReceive() {
  try {
    ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await receiveOrder(order.value.id)
      ElMessage.success('确认收货成功')
      fetchOrderDetail()
    })
  } catch (error) {
    ElMessage.error(error.message || '确认收货失败')
  }
}

// 处理申请退款
async function handleRefund() {
  try {
    ElMessageBox.prompt('请输入退款原因', '申请退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入退款原因'
    }).then(async ({ value }) => {
      await applyRefund(order.value.id, value)
      ElMessage.success('退款申请已提交')
      fetchOrderDetail()
    })
  } catch (error) {
    ElMessage.error(error.message || '申请退款失败')
  }
}

// 显示评价对话框
function showReviewDialog() {
  reviewDialogVisible.value = true
}

// 处理图片上传成功
function handleImageSuccess(res, item) {
  if (!item.reviewImages) {
    item.reviewImages = []
  }
  // 确保我们获取到了正确的图片URL
  const imageUrl = res.data
  if (imageUrl) {
    item.reviewImages.push(imageUrl)
  } else {
    console.error('上传图片失败，未获取到图片URL', res)
    ElMessage.error('上传图片失败')
  }
}

// 处理图片移除
function handleImageRemove(file, item) {
  const index = item.reviewImages.indexOf(file.response?.data)
  if (index !== -1) {
    item.reviewImages.splice(index, 1)
  }
}

// 图片上传前验证
function beforeImageUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return (isJPG || isPNG) && isLt2M
}

// 提交评价
async function submitReview() {
  // 获取未评价的商品
  const items = unreviewedItems.value

  // 如果没有未评价的商品，直接关闭对话框
  if (items.length === 0) {
    reviewDialogVisible.value = false
    return
  }

  // 检查是否所有未评价商品都已填写评价内容
  const invalidItems = items.filter(item => !item.reviewContent?.trim())
  if (invalidItems.length > 0) {
    ElMessage.warning('请填写所有商品的评价内容')
    return
  }

  reviewing.value = true
  try {
    let successCount = 0
    let duplicateCount = 0

    // 为每个未评价的商品提交评价
    for (const item of items) {
      try {
        // 调用评价API
        const res = await publishReview({
          review: {
            productId: item.productId,
            orderId: order.value.id,
            orderItemId: item.id,
            content: item.reviewContent,
            rating: item.rating || 5
          },
          imageUrls: item.reviewImages || []
        })
        console.log('评价提交成功:', res)
        successCount++

        // 更新商品的评价状态
        item.reviewed = 1
      } catch (itemError) {
        console.error('单个商品评价失败:', itemError)

        // 检查是否是重复评价错误
        if (itemError.message && itemError.message.includes('已经评价')) {
          duplicateCount++
          // 更新商品的评价状态
          item.reviewed = 1
          // 继续处理下一个商品，不中断整个流程
        } else {
          // 其他错误则抛出，中断整个评价流程
          throw itemError
        }
      }
    }

    // 根据评价结果显示不同的消息
    if (successCount > 0 && duplicateCount === 0) {
      ElMessage.success('评价提交成功')
    } else if (successCount > 0 && duplicateCount > 0) {
      ElMessage.warning(`部分商品评价成功，${duplicateCount}个商品已经评价过`)
    } else if (successCount === 0 && duplicateCount > 0) {
      ElMessage.warning('所有商品都已经评价过')
    }

    reviewDialogVisible.value = false

    // 刷新订单详情
    fetchOrderDetail()
  } catch (error) {
    console.error('评价提交失败:', error)
    ElMessage.error(error.message || '评价提交失败')
  } finally {
    reviewing.value = false
  }
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

  .loading, .not-found {
    padding: 40px 0;
    text-align: center;
  }

  .status-card,
  .info-card,
  .address-card,
  .product-card,
  .amount-card,
  .logistics-card {
    margin-bottom: 20px;

    .card-header {
      font-weight: bold;
    }

    .status-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .status-info {
        .status-desc {
          margin-top: 10px;
          color: #666;
        }
      }

      .status-actions {
        display: flex;
        gap: 10px;
      }
    }

    .address-info,
    .amount-info,
    .logistics-info {
      .info-item {
        margin-bottom: 10px;

        .label {
          color: #666;
          margin-right: 10px;
        }

        &.total {
          font-weight: bold;

          .price {
            color: #f56c6c;
            font-size: 18px;
          }
        }
      }
    }

    .product-info {
      display: flex;
      align-items: center;

      .product-image {
        width: 60px;
        height: 60px;
        object-fit: cover;
        margin-right: 15px;
        cursor: pointer;
      }

      .product-details {
        .product-name {
          font-size: 14px;
          margin-bottom: 5px;
          cursor: pointer;

          &:hover {
            color: #409EFF;
          }
        }

        .product-attrs {
          font-size: 12px;
          color: #999;

          span {
            margin-right: 10px;
          }
        }
      }
    }

    .logistics-timeline {
      margin-top: 20px;
    }
  }

  .review-form {
    .no-items {
      padding: 30px 0;
      text-align: center;
    }

    .review-item {
      display: flex;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #eee;

      &:last-child {
        margin-bottom: 0;
        padding-bottom: 0;
        border-bottom: none;
      }

      .product-info {
        width: 150px;
        text-align: center;

        .product-image {
          width: 80px;
          height: 80px;
          object-fit: cover;
          margin-bottom: 10px;
        }

        .product-name {
          font-size: 12px;
        }
      }

      .review-content {
        flex: 1;

        .rating,
        .content,
        .images {
          margin-bottom: 15px;

          .label {
            display: block;
            margin-bottom: 5px;
            color: #666;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .order-detail-container {
    .status-card {
      .status-header {
        flex-direction: column;
        align-items: flex-start;

        .status-info {
          margin-bottom: 15px;
        }
      }
    }

    .review-form {
      .review-item {
        flex-direction: column;

        .product-info {
          width: 100%;
          display: flex;
          align-items: center;
          margin-bottom: 15px;

          .product-image {
            margin-bottom: 0;
            margin-right: 10px;
          }

          .product-name {
            text-align: left;
          }
        }
      }
    }
  }
}
</style>
