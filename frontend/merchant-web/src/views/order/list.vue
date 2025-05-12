<template>
  <div class="order-list-container">
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true" class="filter-form">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>

        <el-form-item label="客户信息">
          <el-input v-model="queryParams.customerInfo" placeholder="姓名/手机号" clearable />
        </el-form-item>

        <el-form-item label="订单状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="item in orderStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="下单时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>

        <el-form-item label="订单金额">
          <el-input-number
            v-model="queryParams.minAmount"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="最低金额"
            style="width: 120px"
          />
          <span class="price-separator">-</span>
          <el-input-number
            v-model="queryParams.maxAmount"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="最高金额"
            style="width: 120px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left-actions">
          <!-- 左侧操作区域 -->
        </div>

        <div class="right-actions">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="table">表格视图</el-radio-button>
            <el-radio-button label="card">卡片视图</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-if="viewMode === 'table'">
        <el-table
          v-loading="loading"
          :data="orderList"
          @selection-change="handleSelectionChange"
          border
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />

          <el-table-column prop="orderNo" label="订单号" width="180" />

          <el-table-column label="客户信息" width="150">
            <template #default="{ row }">
              <div>{{ row.nickname || row.username }}</div>
              <div class="text-muted">{{ row.shippingPhone }}</div>
            </template>
          </el-table-column>

          <el-table-column label="商品信息" min-width="250">
            <template #default="{ row }">
              <div v-for="item in row.orderItems" :key="item.id" class="order-item">
                <el-image
                  :src="formatImageUrl(item.productImage)"
                  :preview-src-list="[formatImageUrl(item.productImage)]"
                  fit="cover"
                  style="width: 40px; height: 40px; margin-right: 10px"
                />
                <div class="item-info">
                  <div class="item-name">{{ item.productName }}</div>
                </div>
                <div class="item-price">
                  <div>¥{{ formatPrice(item.price) }}</div>
                  <div>x{{ item.quantity }}</div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="订单金额" width="120">
            <template #default="{ row }">
              <div>¥{{ formatPrice(row.payAmount) }}</div>
              <div class="text-muted">{{ getPaymentMethod(row.payType) }}</div>
            </template>
          </el-table-column>

          <el-table-column label="订单状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getOrderStatusType(row.status)">{{ getOrderStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="下单时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleDetail(row)">详情</el-button>

              <el-button
                v-if="row.status === 1"
                type="success"
                link
                @click="handleShip(row)"
              >
                发货
              </el-button>



              <el-button
                v-if="row.status === 5"
                type="danger"
                link
                @click="handleRefund(row)"
              >
                处理退款
              </el-button>


            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <!-- 卡片视图 -->
      <div v-else class="card-view">
        <el-empty v-if="orderList.length === 0" description="暂无订单" />

        <el-row :gutter="20" v-else>
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="order in orderList" :key="order.id">
            <el-card class="order-card" shadow="hover">
              <template #header>
                <div class="order-card-header">
                  <div class="order-no">订单号：{{ order.orderNo }}</div>
                  <el-tag :type="getOrderStatusType(order.status)">{{ getOrderStatusText(order.status) }}</el-tag>
                </div>
              </template>

              <div class="order-card-content">
                <div class="customer-info">
                  <div class="info-label">客户信息：</div>
                  <div class="info-value">{{ order.nickname || order.username }} {{ order.shippingPhone }}</div>
                </div>

                <div class="order-items">
                  <div v-for="item in order.orderItems" :key="item.id" class="card-order-item">
                    <el-image
                      :src="formatImageUrl(item.productImage)"
                      :preview-src-list="[formatImageUrl(item.productImage)]"
                      fit="cover"
                      style="width: 40px; height: 40px; margin-right: 10px"
                    />
                    <div class="item-info">
                      <div class="item-name">{{ item.productName }}</div>
                    </div>
                    <div class="item-price">
                      <div>¥{{ formatPrice(item.price) }}</div>
                      <div>x{{ item.quantity }}</div>
                    </div>
                  </div>
                </div>

                <div class="order-amount">
                  <div class="info-label">订单金额：</div>
                  <div class="info-value">¥{{ formatPrice(order.payAmount) }}</div>
                </div>

                <div class="order-time">
                  <div class="info-label">下单时间：</div>
                  <div class="info-value">{{ formatDateTime(order.createdAt) }}</div>
                </div>

                <div class="order-actions">
                  <el-button type="primary" size="small" @click="handleDetail(order)">详情</el-button>

                  <el-button
                    v-if="order.status === 1"
                    type="success"
                    size="small"
                    @click="handleShip(order)"
                  >
                    发货
                  </el-button>



                  <el-button
                    v-if="order.status === 5"
                    type="danger"
                    size="small"
                    @click="handleRefund(order)"
                  >
                    处理退款
                  </el-button>


                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh
} from '@element-plus/icons-vue'
import {
  getSellerOrders
} from '../../api/order'
import { formatImageUrl } from '../../utils/image'

const router = useRouter()

// 状态
const loading = ref(false)
const submitting = ref(false)
const orderList = ref([])
const total = ref(0)
const selectedOrders = ref([])
const viewMode = ref('table')
const dateRange = ref([])
const shipDialogVisible = ref(false)
const refundDialogVisible = ref(false)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  customerInfo: '',
  status: null,
  startDate: null,
  endDate: null,
  minAmount: null,
  maxAmount: null
})

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

// 订单状态选项
const orderStatusOptions = [
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '申请退款', value: 5 },
  { label: '已退款', value: 6 }
]

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

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = null
    queryParams.endDate = null
  }
})

// 方法
function formatPrice(price) {
  return parseFloat(price || 0).toFixed(2)
}

function formatDateTime(date) {
  if (!date) return ''
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

// 获取支付方式文本
function getPaymentMethod(payType) {
  switch (payType) {
    case 1: return '支付宝'
    case 2: return '微信支付'
    case 3: return '银行卡'
    default: return '未知方式'
  }
}

// 获取订单列表
async function getOrders() {
  loading.value = true

  // 构建查询参数
  const params = {
    page: queryParams.page,
    size: queryParams.size,
    orderNo: queryParams.orderNo,
    customerInfo: queryParams.customerInfo,
    status: queryParams.status,
    startDate: queryParams.startDate,
    endDate: queryParams.endDate,
    minAmount: queryParams.minAmount,
    maxAmount: queryParams.maxAmount
  };

  try {
    const res = await getSellerOrders(params)
    orderList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleQuery() {
  queryParams.page = 1
  getOrders()
}

// 重置
function resetQuery() {
  queryParams.orderNo = ''
  queryParams.customerInfo = ''
  queryParams.status = null
  dateRange.value = []
  queryParams.startDate = null
  queryParams.endDate = null
  queryParams.minAmount = null
  queryParams.maxAmount = null
  handleQuery()
}

// 选择变化
function handleSelectionChange(selection) {
  selectedOrders.value = selection
}

// 分页大小变化
function handleSizeChange() {
  getOrders()
}

// 页码变化
function handleCurrentChange() {
  getOrders()
}

// 查看订单详情
function handleDetail(row) {
  router.push(`/order/detail/${row.id}`)
}

// 发货
function handleShip(row) {
  shipForm.orderId = row.id
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
        getOrders()
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
function handleRefund(row) {
  refundForm.orderId = row.id
  refundForm.amount = row.payAmount
  refundForm.status = 1
  refundForm.rejectReason = ''
  refundDialogVisible.value = true
}

// 确认退款
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
        getOrders()
      })
      .catch((error) => {
        ElMessage.error(error.message || '处理退款申请失败')
      })
      .finally(() => {
        submitting.value = false
      })
  })
}



// 生命周期钩子
onMounted(() => {
  getOrders()
})
</script>

<style lang="scss" scoped>
.order-list-container {
  .filter-card {
    margin-bottom: 20px;

    .filter-form {
      .price-separator {
        margin: 0 5px;
      }
    }
  }

  .action-card {
    .action-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15px;

      .left-actions {
        display: flex;
        gap: 10px;
      }
    }

    .text-muted {
      font-size: 12px;
      color: #999;
    }

    .order-item {
      display: flex;
      align-items: center;
      padding: 5px 0;
      border-bottom: 1px dashed #eee;

      &:last-child {
        border-bottom: none;
      }

      .item-info {
        flex: 1;
        overflow: hidden;

        .item-name {
          font-size: 14px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .item-spec {
          font-size: 12px;
          color: #999;
        }
      }

      .item-price {
        text-align: right;
        font-size: 12px;
      }
    }

    .pagination {
      margin-top: 20px;
      text-align: right;
    }

    .card-view {
      .order-card {
        margin-bottom: 20px;

        .order-card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .order-no {
            font-size: 14px;
            font-weight: bold;
          }
        }

        .order-card-content {
          .customer-info,
          .order-amount,
          .order-time {
            display: flex;
            margin-bottom: 10px;

            .info-label {
              width: 80px;
              color: #666;
            }

            .info-value {
              flex: 1;
            }
          }

          .order-items {
            margin: 10px 0;

            .card-order-item {
              display: flex;
              align-items: center;
              padding: 5px 0;
              border-bottom: 1px dashed #eee;

              &:last-child {
                border-bottom: none;
              }

              .item-info {
                flex: 1;
                overflow: hidden;

                .item-name {
                  font-size: 14px;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                }

                .item-spec {
                  font-size: 12px;
                  color: #999;
                }
              }

              .item-price {
                text-align: right;
                font-size: 12px;
              }
            }
          }

          .order-actions {
            margin-top: 15px;
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
          }
        }
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
