<template>
  <div class="seller-orders-container">
    <el-card class="seller-orders-card">
      <template #header>
        <div class="card-header">
          <h2>卖家订单</h2>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable></el-input>
          </el-form-item>
          <el-form-item label="买家信息">
            <el-input v-model="searchForm.customerInfo" placeholder="买家昵称/手机号" clearable></el-input>
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select style="width: 150px;" v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="全部" value=""></el-option>
              <el-option label="待付款" :value="0"></el-option>
              <el-option label="待发货" :value="1"></el-option>
              <el-option label="待收货" :value="2"></el-option>
              <el-option label="已完成" :value="3"></el-option>
              <el-option label="已取消" :value="4"></el-option>
              <el-option label="退款中" :value="5"></el-option>
              <el-option label="已退款" :value="6"></el-option>
              <el-option label="退款拒绝" :value="7"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="orders.length > 0" class="orders-table">
        <el-table :data="orders" border style="width: 100%">
          <el-table-column prop="orderNo" label="订单编号" min-width="180"></el-table-column>
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <div v-if="scope.row.orderItems && scope.row.orderItems.length > 0" class="order-items">
                <div v-for="item in scope.row.orderItems" :key="item.id" class="order-item">
                  <el-image
                    :src="formatImageUrl(item.productImage)"
                    class="product-image"
                    fit="cover"
                  ></el-image>
                  <div class="product-detail">
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-price">
                      <span class="price">¥{{ formatPrice(item.price) }}</span>
                      <span class="quantity">x{{ item.quantity }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="买家" width="120">
            <template #default="scope">
              {{ scope.row.buyerName }}
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="订单金额" width="100">
            <template #default="scope">
              ¥{{ formatPrice(scope.row.totalAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="订单状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button type="primary" link @click="viewOrder(scope.row.id)">详情</el-button>
              <el-button 
                v-if="scope.row.status === 1" 
                type="success" 
                link 
                @click="shipOrder(scope.row.id)"
              >
                发货
              </el-button>
              <el-button 
                v-if="scope.row.status === 5" 
                type="danger" 
                link 
                @click="handleRefund(scope.row.id)"
              >
                处理退款
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <el-empty v-else description="暂无订单数据"></el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSellerOrders, shipOrder as apiShipOrder, handleRefund as apiHandleRefund } from '../../api/seller'
import { formatImageUrl } from '../../utils/image'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  customerInfo: '',
  status: ''
})

// 获取订单列表
async function fetchOrders() {
  try {
    loading.value = true
    const res = await getSellerOrders({
      page: page.value,
      size: size.value,
      orderNo: searchForm.orderNo,
      customerInfo: searchForm.customerInfo,
      status: searchForm.status
    })

    if (res.data) {
      orders.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
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

// 分页处理
function handleSizeChange() {
  page.value = 1
  fetchOrders()
}

function handleCurrentChange() {
  fetchOrders()
}

// 搜索
function search() {
  page.value = 1
  fetchOrders()
}

// 重置搜索
function resetSearch() {
  searchForm.orderNo = ''
  searchForm.customerInfo = ''
  searchForm.status = ''
  page.value = 1
  fetchOrders()
}

// 订单操作
function viewOrder(id) {
  router.push(`/seller-orders/${id}`)
}

async function shipOrder(id) {
  try {
    await ElMessageBox.confirm('确认发货该订单?', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await apiShipOrder(id)
    ElMessage.success('发货成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败，请重试')
    }
  }
}

async function handleRefund(id) {
  try {
    const { value: action } = await ElMessageBox.prompt(
      '请输入拒绝原因（同意退款可不填）',
      '处理退款申请',
      {
        confirmButtonText: '同意退款',
        cancelButtonText: '拒绝退款',
        inputPlaceholder: '拒绝原因...',
        showCancelButton: true,
        distinguishCancelAndClose: true
      }
    )
    
    // 同意退款
    await apiHandleRefund(id, 1)
    ElMessage.success('已同意退款')
    fetchOrders()
  } catch (error) {
    if (error === 'cancel') {
      // 用户点击了"拒绝退款"
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
        await apiHandleRefund(id, 2, reason)
        ElMessage.success('已拒绝退款')
        fetchOrders()
      }
    } else if (error !== 'close') {
      console.error('处理退款失败:', error)
      ElMessage.error('处理退款失败，请重试')
    }
  }
}

// 生命周期钩子
onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.seller-orders-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 0;

  .seller-orders-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .search-bar {
      margin-bottom: 20px;
    }

    .loading-container {
      padding: 20px 0;
    }

    .orders-table {
      .order-items {
        .order-item {
          display: flex;
          align-items: center;
          margin-bottom: 10px;

          &:last-child {
            margin-bottom: 0;
          }

          .product-image {
            width: 50px;
            height: 50px;
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
              -webkit-line-clamp: 1;
              -webkit-box-orient: vertical;
            }

            .product-price {
              .price {
                color: #f56c6c;
                font-weight: bold;
                margin-right: 5px;
              }

              .quantity {
                color: #999;
              }
            }
          }
        }
      }

      .pagination {
        margin-top: 20px;
        text-align: right;
      }
    }
  }
}
</style>
