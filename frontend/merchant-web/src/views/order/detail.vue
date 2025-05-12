<template>
  <div class="order-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>订单详情</h2>
          <div class="header-actions">
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getOrderStatusType(order.status)">{{ getOrderStatusText(order.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 订单进度 -->
      <div class="progress-section">
        <div class="section-title">订单进度</div>

        <el-steps :active="getOrderProgress(order.status)" finish-status="success" align-center>
          <el-step title="提交订单" :description="formatDateTime(order.createdAt)" />
          <el-step title="付款成功" :description="formatDateTime(order.payTime)" />
          <el-step title="商品发货" :description="formatDateTime(order.shipTime)" />
          <el-step title="交易完成" :description="formatDateTime(order.completeTime)" />
        </el-steps>
      </div>

      <!-- 商品信息 -->
      <div class="product-section">
        <div class="section-title">商品信息</div>

        <el-table :data="order.orderItems" border style="width: 100%">
          <el-table-column label="商品图片" width="80">
            <template #default="{ row }">
              <el-image
                :src="formatImageUrl(row.productImage)"
                :preview-src-list="[formatImageUrl(row.productImage)]"
                fit="cover"
                style="width: 60px; height: 60px"
              />
            </template>
          </el-table-column>

          <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />

          <el-table-column label="单价" width="100">
            <template #default="{ row }">
              ¥{{ formatPrice(row.price) }}
            </template>
          </el-table-column>

          <el-table-column prop="quantity" label="数量" width="80" />

          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ formatPrice(row.price * row.quantity) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 订单金额 -->
      <div class="amount-section">
        <div class="section-title">订单金额</div>

        <div class="amount-info">
          <div class="amount-item total">
            <span class="item-label">订单总额：</span>
            <span class="item-value">¥{{ formatPrice(order.totalAmount) }}</span>
          </div>

          <div class="amount-item total">
            <span class="item-label">实付金额：</span>
            <span class="item-value">¥{{ formatPrice(order.payAmount) }}</span>
          </div>

          <div class="amount-item">
            <span class="item-label">支付方式：</span>
            <span class="item-value">{{ getPaymentMethod(order.payType) }}</span>
          </div>

          <div class="amount-item">
            <span class="item-label">支付时间：</span>
            <span class="item-value">{{ formatDateTime(order.paymentTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="address-section">
        <div class="section-title">收货信息</div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="收货人">{{ order.shippingName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.shippingPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ order.shippingAddress }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 物流信息 -->
      <div class="logistics-section" v-if="order.status >= 3">
        <div class="section-title">物流信息</div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="物流公司">{{ getLogisticsCompanyName(order.logisticsCompany) }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ order.trackingNumber }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ formatDateTime(order.shipTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="logistics-timeline">
          <div class="timeline-title">物流跟踪</div>

          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in logisticsInfo"
              :key="index"
              :timestamp="activity.time"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <!-- 订单备注 -->
      <div class="remark-section">
        <div class="section-title">订单备注</div>

        <el-input
          v-model="order.remark"
          type="textarea"
          :rows="3"
          placeholder="暂无备注"
          readonly
        />
      </div>

      <!-- 订单操作 -->
      <div class="action-section">
        <div class="section-title">订单操作</div>

        <div class="action-buttons">
          <el-button
            v-if="order.status === 1"
            type="primary"
            @click="handleShip"
          >
            发货
          </el-button>



          <el-button
            v-if="order.status === 5"
            type="danger"
            @click="handleRefund"
          >
            处理退款
          </el-button>

          <el-button
            v-if="order.status === 0"
            type="info"
            @click="handleCancel"
          >
            取消订单
          </el-button>

          <el-button type="success" @click="handleRemark">修改备注</el-button>
        </div>
      </div>


    </el-card>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="500px">
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="100px">
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option
              v-for="item in logisticsOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="物流单号" prop="trackingNumber">
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入物流单号" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="shipForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmShip" :loading="submitting">确认发货</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退款处理对话框 -->
    <el-dialog v-model="refundDialogVisible" title="处理退款申请" width="500px">
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="100px">
        <el-form-item label="退款金额">
          <span>¥{{ formatPrice(refundForm.amount) }}</span>
        </el-form-item>

        <el-form-item label="处理结果" prop="status">
          <el-radio-group v-model="refundForm.status">
            <el-radio :label="1">同意退款</el-radio>
            <el-radio :label="2">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="refundForm.status === 2" label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="refundForm.rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRefund" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 备注对话框 -->
    <el-dialog v-model="remarkDialogVisible" title="修改备注" width="500px">
      <el-form :model="remarkForm" ref="remarkFormRef" label-width="100px">
        <el-form-item label="订单备注">
          <el-input
            v-model="remarkForm.remark"
            type="textarea"
            :rows="5"
            placeholder="请输入订单备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="remarkDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRemark" :loading="submitting">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail } from '../../api/order'
import { formatImageUrl } from '../../utils/image'
// 不再导入cancelOrder

const route = useRoute()
const router = useRouter()

// 状态
const order = reactive({})
const logisticsInfo = ref([])
const submitting = ref(false)
const shipDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const remarkDialogVisible = ref(false)

// 发货表单
const shipFormRef = ref(null)
const shipForm = reactive({
  orderId: null,
  logisticsCompany: '',
  trackingNumber: '',
  remark: ''
})

// 退款表单
const refundFormRef = ref(null)
const refundForm = reactive({
  orderId: null,
  amount: 0,
  status: 1,
  rejectReason: ''
})

// 备注表单
const remarkFormRef = ref(null)
const remarkForm = reactive({
  orderId: null,
  remark: ''
})

// 表单验证规则
const shipRules = {
  logisticsCompany: [
    { required: true, message: '请选择物流公司', trigger: 'change' }
  ],
  trackingNumber: [
    { required: true, message: '请输入物流单号', trigger: 'blur' }
  ]
}

const refundRules = {
  status: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ],
  rejectReason: [
    {
      validator: (_, value, callback) => {
        if (refundForm.status === 2 && (!value || value.trim() === '')) {
          callback(new Error('请输入拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 物流公司选项
const logisticsOptions = [
  { label: '顺丰速运', value: 'SF' },
  { label: '中通快递', value: 'ZTO' },
  { label: '圆通速递', value: 'YTO' },
  { label: '韵达速递', value: 'YD' },
  { label: '申通快递', value: 'STO' },
  { label: '百世快递', value: 'HTKY' },
  { label: '邮政快递', value: 'YZPY' },
  { label: 'EMS', value: 'EMS' },
  { label: '京东物流', value: 'JD' },
  { label: '天天快递', value: 'TTKDEX' }
]

// 方法
function formatPrice(price) {
  return parseFloat(price || 0).toFixed(2)
}

function formatDateTime(date) {
  if (!date) return '--'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getOrderStatusType(status) {
  switch (status) {
    case 0: return 'warning'  // 待付款
    case 1: return 'primary'  // 待发货
    case 2: return 'success'  // 待收货
    case 3: return ''         // 已完成
    case 4: return 'info'     // 已取消
    case 5: return 'danger'   // 申请退款
    case 6: return 'info'     // 已退款
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

function getOrderProgress(status) {
  if (status === 0) return 1
  if (status === 1) return 2
  if (status === 2) return 3
  if (status === 3) return 4
  if (status === 4) return 1
  if (status === 5 || status === 6) return 2
  return 0
}

function getLogisticsCompanyName(code) {
  const company = logisticsOptions.find(item => item.value === code)
  return company ? company.label : code
}

// 获取支付方式文本
function getPaymentMethod(payType) {
  switch (payType) {
    case 1: return '支付宝'
    case 2: return '微信支付'
    case 3: return '银行卡'
    default: return '未知方式'
  }
}

// 获取订单详情
async function getOrder() {
  try {
    const res = await getOrderDetail(route.params.id)
    Object.assign(order, res.data)
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    router.push('/order/list')
  }
}



// 获取物流信息 - 简化版，不调用API
// 注释掉未使用的函数
// function fetchLogisticsInfo() {
//   // 不调用API，使用空数组
//   logisticsInfo.value = []
// }

// 返回
function goBack() {
  router.push('/order/list')
}



// 发货
function handleShip() {
  shipForm.orderId = order.id
  shipForm.logisticsCompany = ''
  shipForm.trackingNumber = ''
  shipForm.remark = ''
  shipDialogVisible.value = true
}

// 确认发货
function confirmShip() {
  shipFormRef.value.validate((valid) => {
    if (!valid) return

    submitting.value = true

    // 直接发送发货请求，简化为只传递订单ID
    fetch(`/api/order/ship?id=${shipForm.orderId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('发货失败')
        }
        return response.json()
      })
      .then(() => {
        ElMessage.success('发货成功')
        shipDialogVisible.value = false
        getOrder()
      })
      .catch((error) => {
        ElMessage.error(error.message || '发货失败')
      })
      .finally(() => {
        submitting.value = false
      })
  })
}



// 处理退款申请
function handleRefund() {
  refundForm.orderId = order.id
  refundForm.amount = order.payAmount
  refundForm.status = 1
  refundForm.rejectReason = ''
  refundDialogVisible.value = true
}

// 确认退款处理
function confirmRefund() {
  refundFormRef.value.validate((valid) => {
    if (!valid) return

    submitting.value = true

    // 调用处理退款API - 使用URL参数而非请求体
    const params = new URLSearchParams()
    params.append('id', refundForm.orderId)
    params.append('status', refundForm.status)

    // 如果是拒绝退款，添加拒绝原因
    if (refundForm.status === 2 && refundForm.rejectReason) {
      params.append('reason', refundForm.rejectReason)
    }

    // 发送请求
    fetch('/api/order/refund/handle', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: params
    })
      .then(response => response.json())
      .then(data => {
        if (data.code !== 200) {
          throw new Error(data.message || '处理退款申请失败')
        }
        ElMessage.success('处理退款申请成功')
        refundDialogVisible.value = false
        getOrder()
      })
      .catch((error) => {
        ElMessage.error(error.message || '处理退款申请失败')
      })
      .finally(() => {
        submitting.value = false
      })
  })
}

// 取消订单 - 简化版，只显示提示
function handleCancel() {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 使用fetch直接调用API，而不是使用cancelOrder函数
    fetch(`/api/order/cancel?id=${order.id}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
      .then(response => response.json())
      .then(data => {
        if (data.code === 200) {
          ElMessage.success('订单取消成功')
          getOrder() // 刷新订单数据
        } else {
          throw new Error(data.message || '取消订单失败')
        }
      })
      .catch(error => {
        ElMessage.error(error.message || '取消订单失败')
      })
  }).catch(() => {})
}

// 修改备注 - 简化版，只显示对话框
function handleRemark() {
  remarkForm.orderId = order.id
  remarkForm.remark = order.remark || ''
  remarkDialogVisible.value = true
}

// 确认修改备注 - 简化版，只显示提示
function confirmRemark() {
  ElMessage.info('该功能暂未实现')
  remarkDialogVisible.value = false
}

// 生命周期钩子
onMounted(() => {
  getOrder()
})
</script>

<style lang="scss" scoped>
.order-detail-container {
  .detail-card {
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

    .section-title {
      font-size: 16px;
      font-weight: bold;
      margin: 30px 0 20px;
      padding-bottom: 10px;
      border-bottom: 1px solid #eee;
    }

    .progress-section {
      margin-top: 20px;
    }

    .amount-section {
      .amount-info {
        background-color: #f8f8f8;
        padding: 20px;
        border-radius: 4px;

        .amount-item {
          display: flex;
          justify-content: flex-end;
          margin-bottom: 10px;

          &:last-child {
            margin-bottom: 0;
          }

          &.total {
            font-size: 18px;
            font-weight: bold;
            color: #f56c6c;
            border-top: 1px dashed #ddd;
            padding-top: 10px;
            margin-top: 10px;
          }

          .item-label {
            width: 100px;
            text-align: right;
            margin-right: 10px;
          }
        }
      }
    }

    .logistics-section {
      .logistics-timeline {
        margin-top: 20px;

        .timeline-title {
          font-weight: bold;
          margin-bottom: 15px;
        }
      }
    }

    .action-section {
      .action-buttons {
        display: flex;
        gap: 10px;
      }
    }
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }
}
</style>
