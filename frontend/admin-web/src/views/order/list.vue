<template>
  <div class="order-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="用户ID">
            <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
          </el-form-item>
          <el-form-item label="商家ID">
            <el-input v-model="searchForm.sellerId" placeholder="请输入商家ID" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待付款" :value="0" />
              <el-option label="待发货" :value="1" />
              <el-option label="待收货" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="申请退款" :value="5" />
              <el-option label="退款成功" :value="6" />
              <el-option label="退款拒绝" :value="7" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="orderList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="用户信息" width="120">
          <template #default="scope">
            {{ scope.row.username }} ({{ scope.row.nickname }})
          </template>
        </el-table-column>
        <el-table-column prop="sellerId" label="商家ID" width="80" />
        <el-table-column label="商家信息" width="120">
          <template #default="scope">
            {{ scope.row.sellerUsername }} ({{ scope.row.sellerNickname }})
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="支付金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.payAmount }}
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.payType === 1" type="success">支付宝</el-tag>
            <el-tag v-else-if="scope.row.payType === 2" type="primary">微信</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">待付款</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="warning">待发货</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="primary">待收货</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="success">已完成</el-tag>
            <el-tag v-else-if="scope.row.status === 4" type="info">已取消</el-tag>
            <el-tag v-else-if="scope.row.status === 5" type="danger">申请退款</el-tag>
            <el-tag v-else-if="scope.row.status === 6" type="danger">退款成功</el-tag>
            <el-tag v-else-if="scope.row.status === 7" type="warning">退款拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" width="180">
          <template #default="scope">
            {{ scope.row.shippingName }} / {{ scope.row.shippingPhone }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="scope">
            <el-button type="primary" link @click="handleDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 5"
              type="success"
              link
              @click="handleRefund(scope.row, 1)"
            >
              同意退款
            </el-button>
            <el-button
              v-if="scope.row.status === 5"
              type="danger"
              link
              @click="handleRefund(scope.row, 2)"
            >
              拒绝退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
    >
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID">{{ currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentOrder.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentOrder.username }} ({{ currentOrder.nickname }})</el-descriptions-item>
          <el-descriptions-item label="商家ID">{{ currentOrder.sellerId }}</el-descriptions-item>
          <el-descriptions-item label="商家名称">{{ currentOrder.sellerUsername }} ({{ currentOrder.sellerNickname }})</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ currentOrder.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">
            <el-tag v-if="currentOrder.payType === 1" type="success">支付宝</el-tag>
            <el-tag v-else-if="currentOrder.payType === 2" type="primary">微信</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag v-if="currentOrder.status === 0" type="info">待付款</el-tag>
            <el-tag v-else-if="currentOrder.status === 1" type="warning">待发货</el-tag>
            <el-tag v-else-if="currentOrder.status === 2" type="primary">待收货</el-tag>
            <el-tag v-else-if="currentOrder.status === 3" type="success">已完成</el-tag>
            <el-tag v-else-if="currentOrder.status === 4" type="info">已取消</el-tag>
            <el-tag v-else-if="currentOrder.status === 5" type="danger">申请退款</el-tag>
            <el-tag v-else-if="currentOrder.status === 6" type="danger">退款成功</el-tag>
            <el-tag v-else-if="currentOrder.status === 7" type="warning">退款拒绝</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.shippingName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.shippingPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentOrder.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.paymentTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ currentOrder.shippingTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货时间">{{ currentOrder.receiveTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关闭时间">{{ currentOrder.closeTime || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.refund" label="退款原因" :span="2">
            {{ currentOrder.refund.reason }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="order-items" v-if="currentOrder.items && currentOrder.items.length > 0">
          <div class="items-title">订单商品</div>
          <el-table :data="currentOrder.items" border style="width: 100%">
            <el-table-column label="商品图片" width="100">
              <template #default="scope">
                <el-image
                  :src="getImageUrl(scope.row.productImage)"
                  :preview-src-list="[getImageUrl(scope.row.productImage)]"
                  fit="cover"
                  style="width: 60px; height: 60px"
                />
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="price" label="单价">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="totalPrice" label="小计">
              <template #default="scope">
                ¥{{ scope.row.totalPrice }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="payment-info" v-if="currentOrder.payment">
          <div class="payment-title">支付信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="支付流水号">{{ currentOrder.payment.paymentNo }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">
              <el-tag v-if="currentOrder.payment.paymentType === 'alipay'" type="success">支付宝</el-tag>
              <el-tag v-else-if="currentOrder.payment.paymentType === 'wechat'" type="primary">微信</el-tag>
              <span v-else>{{ currentOrder.payment.paymentType }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="支付金额">¥{{ currentOrder.payment.amount }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag v-if="currentOrder.payment.status === 0" type="info">未支付</el-tag>
              <el-tag v-else-if="currentOrder.payment.status === 1" type="success">已支付</el-tag>
              <el-tag v-else-if="currentOrder.payment.status === 2" type="danger">支付失败</el-tag>
              <el-tag v-else-if="currentOrder.payment.status === 3" type="warning">已退款</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentOrder.payment.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ currentOrder.payment.updatedAt }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="refund-info" v-if="currentOrder.refund">
          <div class="refund-title">退款信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="退款单号">{{ currentOrder.refund.refundNo }}</el-descriptions-item>
            <el-descriptions-item label="退款金额">¥{{ currentOrder.refund.refundAmount }}</el-descriptions-item>
            <el-descriptions-item label="退款原因">{{ currentOrder.refund.reason }}</el-descriptions-item>
            <el-descriptions-item label="退款状态">
              <el-tag v-if="currentOrder.refund.status === 0" type="info">处理中</el-tag>
              <el-tag v-else-if="currentOrder.refund.status === 1" type="success">已退款</el-tag>
              <el-tag v-else-if="currentOrder.refund.status === 2" type="danger">已拒绝</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ currentOrder.refund.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="处理时间">{{ currentOrder.refund.updatedAt }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, processRefund } from '../../api/order'

const route = useRoute()

// 状态
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  userId: '',
  sellerId: route.query.merchantId || '',
  status: ''
})

// 方法
function fetchOrderList() {
  loading.value = true

  // 构建查询参数
  const params = {
    current: currentPage.value,
    size: pageSize.value
  };

  // 添加可选查询参数
  if (searchForm.orderNo) params.orderNo = searchForm.orderNo;
  if (searchForm.userId) params.userId = searchForm.userId;
  if (searchForm.sellerId) params.sellerId = searchForm.sellerId;
  if (searchForm.status !== '') params.status = searchForm.status;

  getOrderList(params).then(res => {

    // 检查响应数据结构
    if (res.data && Array.isArray(res.data)) {
      // 直接返回数组
      orderList.value = res.data
      total.value = res.data.length
    } else if (res.data && res.data.records) {
      // 分页格式
      orderList.value = res.data.records
      total.value = res.data.total
    } else if (res.data) {
      // 其他格式
      orderList.value = res.data
      total.value = 0
    } else {
      orderList.value = []
      total.value = 0
    }
  }).catch(error => {
    ElMessage.error('获取订单列表失败')
    orderList.value = []
    total.value = 0
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchOrderList()
}

function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    if (key !== 'sellerId' || !route.query.merchantId) {
      searchForm[key] = ''
    }
  })
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchOrderList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchOrderList()
}

function handleDetail(row) {
  getOrderDetail(row.id).then(res => {
    currentOrder.value = res.data
    detailDialogVisible.value = true
  }).catch(error => {
    ElMessage.error('获取订单详情失败')
  })
}

function handleRefund(row, status) {
  const statusText = status === 1 ? '同意' : '拒绝'
  let reason = '';

  if (status === 2) {
    // 拒绝退款需要输入原因
    ElMessageBox.prompt(`请输入拒绝退款的原因`, '拒绝退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '原因不能为空'
    }).then(({ value }) => {
      reason = value;
      confirmRefund(row, status, reason);
    }).catch(() => {});
  } else {
    // 同意退款直接确认
    ElMessageBox.confirm(`确定要${statusText}订单 ${row.orderNo} 的退款申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      confirmRefund(row, status);
    }).catch(() => {});
  }
}

function confirmRefund(row, status, reason) {
  const statusText = status === 1 ? '同意' : '拒绝';
  processRefund(row.id, status, reason).then(() => {
    ElMessage.success(`${statusText}退款成功`)
    fetchOrderList()
  }).catch(error => {
    ElMessage.error(error.response?.data?.msg || `${statusText}退款失败`)
  })
}

// 处理图片URL
function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/api')) return url
  return `/api${url}`
}

// 监听路由参数变化
watch(() => route.query.merchantId, (newVal) => {
  if (newVal) {
    searchForm.sellerId = newVal
    handleSearch()
  }
})

// 生命周期钩子
onMounted(() => {
  fetchOrderList()
})
</script>

<style lang="scss" scoped>
.order-list-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-container {
    margin-bottom: 20px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .order-detail {
    .order-items {
      margin-top: 20px;

      .items-title {
        font-weight: bold;
        margin-bottom: 10px;
      }
    }

    .payment-info, .refund-info {
      margin-top: 20px;

      .payment-title, .refund-title {
        font-weight: bold;
        margin-bottom: 10px;
      }
    }
  }
}
</style>
