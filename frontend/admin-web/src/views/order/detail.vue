<template>
  <div class="order-detail-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button type="primary" size="small" @click="goBack">返回列表</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="订单ID">{{ orderInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ orderInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ orderInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ orderInfo.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ formatPrice(orderInfo.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPayTypeText(orderInfo.payType) }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getOrderStatusType(orderInfo.status)">{{ getOrderStatusText(orderInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(orderInfo.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间" v-if="orderInfo.payTime">{{ formatDate(orderInfo.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="发货时间" v-if="orderInfo.shipTime">{{ formatDate(orderInfo.shipTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" v-if="orderInfo.finishTime">{{ formatDate(orderInfo.finishTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消时间" v-if="orderInfo.cancelTime">{{ formatDate(orderInfo.cancelTime) }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 收货信息 -->
      <div class="section-container">
        <h3>收货信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="收货人">{{ orderInfo.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderInfo.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ orderInfo.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 物流信息 -->
      <div class="section-container" v-if="orderInfo.status >= 2">
        <h3>物流信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="物流公司">{{ orderInfo.expressCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ orderInfo.expressNo || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 订单商品 -->
      <div class="section-container">
        <h3>订单商品</h3>
        <el-table :data="orderInfo.items || []" style="width: 100%">
          <el-table-column label="商品图片" width="100">
            <template #default="scope">
              <el-image 
                :src="scope.row.productImage" 
                fit="cover"
                style="width: 80px; height: 80px"
              ></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
          <el-table-column prop="productPrice" label="单价" width="100">
            <template #default="scope">
              ¥{{ formatPrice(scope.row.productPrice) }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="scope">
              ¥{{ formatPrice(scope.row.productPrice * scope.row.quantity) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 退款信息 -->
      <div class="section-container" v-if="orderInfo.status === 5 || orderInfo.status === 6">
        <h3>退款信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="退款原因">{{ orderInfo.refundReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ formatPrice(orderInfo.refundAmount) }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(orderInfo.refundTime) }}</el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="orderInfo.status === 5 ? 'warning' : 'success'">
              {{ orderInfo.status === 5 ? '退款中' : '已退款' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作" v-if="orderInfo.status === 5">
            <el-button type="success" size="small" @click="handleRefund(1)">同意退款</el-button>
            <el-button type="danger" size="small" @click="handleRefund(2)">拒绝退款</el-button>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
    
    <!-- 拒绝退款对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝退款"
      width="500px"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" prop="reason" :rules="[{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]">
          <el-input 
            v-model="rejectForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入拒绝原因"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, handleRefund } from '../../api/order'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

// 数据
const loading = ref(false)
const orderInfo = ref({})

// 拒绝退款相关
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  reason: ''
})

// 方法
function goBack() {
  router.push('/order/list')
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

function formatPrice(price) {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

function getPayTypeText(payType) {
  if (payType === 1) return '支付宝'
  if (payType === 2) return '微信'
  if (payType === 3) return '银行卡'
  return '未知'
}

function getOrderStatusType(status) {
  if (status === 0) return 'info'     // 待付款
  if (status === 1) return 'warning'  // 待发货
  if (status === 2) return 'primary'  // 待收货
  if (status === 3) return 'success'  // 已完成
  if (status === 4) return 'danger'   // 已取消
  if (status === 5) return 'warning'  // 退款中
  if (status === 6) return 'info'     // 已退款
  return 'info'
}

function getOrderStatusText(status) {
  if (status === 0) return '待付款'
  if (status === 1) return '待发货'
  if (status === 2) return '待收货'
  if (status === 3) return '已完成'
  if (status === 4) return '已取消'
  if (status === 5) return '退款中'
  if (status === 6) return '已退款'
  return '未知'
}

async function fetchOrderDetail() {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId)
    orderInfo.value = res.data || {}
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

function handleRefund(status) {
  if (status === 1) {
    ElMessageBox.confirm('确定要同意退款吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      processRefund(status)
    }).catch(() => {})
  } else {
    rejectForm.reason = ''
    rejectDialogVisible.value = true
  }
}

function confirmReject() {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  processRefund(2, rejectForm.reason)
}

async function processRefund(status, reason = '') {
  try {
    await handleRefund(orderId, status, reason)
    ElMessage.success(status === 1 ? '退款已同意' : '退款已拒绝')
    rejectDialogVisible.value = false
    fetchOrderDetail()
  } catch (error) {
    console.error('处理退款失败:', error)
    ElMessage.error('处理退款失败')
  }
}

// 生命周期钩子
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style lang="scss" scoped>
.order-detail-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .section-container {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
      font-weight: bold;
    }
  }
}
</style>
