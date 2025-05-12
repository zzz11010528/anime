<template>
  <div class="group-buying-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>拼团管理</h2>
          <el-button type="primary" @click="handleAddGroup">创建拼团</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.keyword" placeholder="请输入商品名称" clearable />
          </el-form-item>
          <el-form-item label="拼团状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="未开始" :value="2" />
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="0" />
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
        :data="groupList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品信息" min-width="250">
          <template #default="scope">
            <div class="product-info">
              <el-image
                :src="formatImageUrl(scope.row.productImage)"
                :preview-src-list="[formatImageUrl(scope.row.productImage)]"
                fit="cover"
                class="product-image"
              />
              <div class="product-detail">
                <div class="product-name">{{ scope.row.productName }}</div>
                <div class="product-price">
                  <span class="original-price">原价: ¥{{ scope.row.productPrice }}</span>
                  <span class="group-price">拼团价: ¥{{ scope.row.groupPrice }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="拼团规则" width="180">
          <template #default="scope">
            <div>
              <div>{{ scope.row.minGroupSize }}-{{ scope.row.maxGroupSize }}人成团</div>
              <div>已成团: {{ scope.row.groupedCount || 0 }}个</div>
              <div>拼团中: {{ scope.row.groupingCount || 0 }}个</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="240">
          <template #default="scope">
            <div>
              <div>开始: {{ formatDate(scope.row.startTime) }}</div>
              <div>结束: {{ formatDate(scope.row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 2"
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status !== 0"
              type="danger"
              size="small"
              @click="handleEnd(scope.row)"
            >
              结束
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewOrders(scope.row)"
            >
              查看订单
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 创建/编辑拼团对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '创建拼团' : '编辑拼团'"
      width="600px"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupRules"
        label-width="100px"
      >
        <el-form-item label="选择商品" prop="productId">
          <el-select
            v-model="groupForm.productId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入商品名称搜索"
            :remote-method="searchProducts"
            :loading="productLoading"
            @change="handleProductChange"
          >
            <el-option
              v-for="item in productOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <div class="product-option">
                <el-image
                  :src="formatImageUrl(item.mainImage)"
                  class="option-image"
                  style="width: 30px; height: 30px; object-fit: cover;"
                />
                <span>{{ item.name }}</span>
                <span class="option-price">¥{{ item.price }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="拼团价格" prop="groupPrice">
          <el-input-number
            v-model="groupForm.groupPrice"
            :min="0.01"
            :precision="2"
            :step="0.1"
            style="width: 100%"
            placeholder="请输入拼团价格"
          />
        </el-form-item>
        <el-form-item label="成团人数" required>
          <div class="group-size-container">
            <el-form-item prop="minGroupSize" class="group-size-item">
              <el-input-number
                v-model="groupForm.minGroupSize"
                :min="2"
                :max="groupForm.maxGroupSize"
                :step="1"
                controls-position="right"
                placeholder="最小人数"
              />
            </el-form-item>
            <span class="separator">-</span>
            <el-form-item prop="maxGroupSize" class="group-size-item">
              <el-input-number
                v-model="groupForm.maxGroupSize"
                :min="groupForm.minGroupSize"
                :step="1"
                controls-position="right"
                placeholder="最大人数"
              />
            </el-form-item>
          </div>
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange" required>
          <el-date-picker
            v-model="groupForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGroupForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看订单对话框 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="拼团订单"
      width="800px"
    >
      <el-tabs v-model="orderTabActive">
        <el-tab-pane label="拼团中" name="grouping">
          <el-table :data="groupingOrders" border style="width: 100%">
            <el-table-column prop="id" label="拼团ID" width="80" />
            <el-table-column label="团长" width="150">
              <template #default="scope">
                <div class="user-info">
                  <el-avatar
                    :src="scope.row.leaderAvatar ? formatImageUrl(scope.row.leaderAvatar) : defaultAvatar"
                    :size="30">
                  </el-avatar>
                  <span class="username">{{ scope.row.leaderNickname || scope.row.leaderUsername || '用户' + scope.row.leaderUserId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="currentSize" label="当前人数" width="100" />
            <el-table-column label="过期时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.expireTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewGroupDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="已成团" name="grouped">
          <el-table :data="groupedOrders" border style="width: 100%">
            <el-table-column prop="id" label="拼团ID" width="80" />
            <el-table-column label="团长" width="150">
              <template #default="scope">
                <div class="user-info">
                  <el-avatar
                    :src="scope.row.leaderAvatar ? formatImageUrl(scope.row.leaderAvatar) : defaultAvatar"
                    :size="30">
                  </el-avatar>
                  <span class="username">{{ scope.row.leaderNickname || scope.row.leaderUsername || '用户' + scope.row.leaderUserId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="currentSize" label="成团人数" width="100" />
            <el-table-column label="创建时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="成团时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewGroupDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 拼团详情对话框 -->
    <el-dialog
      v-model="groupDetailDialogVisible"
      title="拼团详情"
      width="800px"
    >
      <div v-if="currentGroupOrder" class="group-detail">
        <div class="group-info">
          <h3>拼团信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="拼团ID">{{ currentGroupOrder.id }}</el-descriptions-item>
            <el-descriptions-item label="拼团状态">
              <el-tag :type="currentGroupOrder.status === 0 ? 'warning' : currentGroupOrder.status === 1 ? 'success' : 'info'">
                {{ currentGroupOrder.status === 0 ? '组团中' : currentGroupOrder.status === 1 ? '已成团' : '已解散' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="团长">
              <div class="user-info">
                <el-avatar
                  :src="currentGroupOrder.leaderAvatar ? formatImageUrl(currentGroupOrder.leaderAvatar) : defaultAvatar"
                  :size="30">
                </el-avatar>
                <span>{{ currentGroupOrder.leaderNickname || currentGroupOrder.leaderUsername || '用户' + currentGroupOrder.leaderUserId }}</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="当前人数">{{ currentGroupOrder.currentSize }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(currentGroupOrder.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ formatDateTime(currentGroupOrder.expireTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="order-list" v-if="currentGroupOrder.orders && currentGroupOrder.orders.length > 0">
          <h3>参与订单</h3>
          <el-table :data="currentGroupOrder.orders" border style="width: 100%">
            <el-table-column prop="orderNo" label="订单编号" width="180" />
            <el-table-column label="用户" width="150">
              <template #default="scope">
                <div class="user-info">
                  <span>{{ scope.row.nickname || scope.row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="payAmount" label="支付金额" width="100">
              <template #default="scope">
                ¥{{ formatPrice(scope.row.payAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="支付状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status >= 1 ? 'success' : 'warning'">
                  {{ scope.row.status >= 1 ? '已支付' : '未支付' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else class="empty-orders">
          <el-empty description="暂无订单数据" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList } from '../../api/product'
import {
  getGroupBuyingList,
  createGroupBuying,
  updateGroupBuying,
  endGroupBuying,
  getGroupingOrders,
  getGroupedOrders,
  getGroupOrderDetail
} from '../../api/group'
import { formatImageUrl } from '../../utils/image'
import defaultAvatar from '../../assets/images/default-avatar.png'

// 状态
const loading = ref(false)
const groupList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref('add')
const groupFormRef = ref(null)
const productLoading = ref(false)
const productOptions = ref([])
const orderDialogVisible = ref(false)
const orderTabActive = ref('grouping')
const groupingOrders = ref([])
const groupedOrders = ref([])
const groupDetailDialogVisible = ref(false)
const currentGroupOrder = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 拼团表单
const groupForm = reactive({
  id: null,
  productId: null,
  productName: '',
  productPrice: 0,
  groupPrice: 0,
  minGroupSize: 2,
  maxGroupSize: 5,
  timeRange: []
})

// 表单验证规则
const groupRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  groupPrice: [{ required: true, message: '请输入拼团价格', trigger: 'blur' }],
  minGroupSize: [{ required: true, message: '请输入最小成团人数', trigger: 'blur' }],
  maxGroupSize: [{ required: true, message: '请输入最大成团人数', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  fetchGroupList()
})

// 获取拼团列表
function fetchGroupList() {
  loading.value = true
  getGroupBuyingList({
    keyword: searchForm.keyword,
    status: searchForm.status,
    page: currentPage.value,
    size: pageSize.value
  }).then(res => {
    // 处理图片URL
    const records = res.data.records
    records.forEach(item => {
      if (item.productImage) {
        console.log('拼团列表原始图片URL:', item.productImage)
        item.productImage = formatImageUrl(item.productImage)
        console.log('拼团列表处理后图片URL:', item.productImage)
      }
    })
    groupList.value = records
    total.value = res.data.total
  }).catch(err => {
    console.error('获取拼团列表失败:', err)
  }).finally(() => {
    loading.value = false
  })
}

// 搜索商品
async function searchProducts(query) {
  if (query) {
    productLoading.value = true
    try {
      const res = await getProductList({
        keyword: query,
        status: 1,
        page: 1,
        size: 20
      })
      // 处理图片URL
      const records = res.data.records
      records.forEach(item => {
        if (item.mainImage) {
          console.log('原始图片URL:', item.mainImage)
          item.mainImage = formatImageUrl(item.mainImage)
          console.log('处理后图片URL:', item.mainImage)
        }
      })
      productOptions.value = records
    } catch (error) {
      console.error('搜索商品失败:', error)
    } finally {
      productLoading.value = false
    }
  } else {
    productOptions.value = []
  }
}

// 处理商品选择变化
function handleProductChange(productId) {
  const product = productOptions.value.find(item => item.id === productId)
  if (product) {
    groupForm.productName = product.name
    groupForm.productPrice = product.price
    // 默认拼团价为原价的8折
    groupForm.groupPrice = Math.floor(product.price * 0.8 * 100) / 100
  }
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化日期时间（别名，保持与模板一致）
const formatDateTime = formatDate

// 格式化价格
function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

// 获取状态类型
function getStatusType(status) {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'warning'
    default: return 'info'
  }
}

// 获取状态文本
function getStatusText(status) {
  switch (status) {
    case 0: return '已结束'
    case 1: return '进行中'
    case 2: return '未开始'
    default: return '未知'
  }
}

// 处理搜索
function handleSearch() {
  currentPage.value = 1
  fetchGroupList()
}

// 重置搜索
function resetSearch() {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

// 处理分页大小变化
function handleSizeChange(val) {
  pageSize.value = val
  fetchGroupList()
}

// 处理页码变化
function handleCurrentChange(val) {
  currentPage.value = val
  fetchGroupList()
}

// 处理添加拼团
function handleAddGroup() {
  dialogType.value = 'add'
  resetGroupForm()
  dialogVisible.value = true
}

// 处理编辑拼团
function handleEdit(row) {
  dialogType.value = 'edit'
  resetGroupForm()

  // 填充表单数据
  groupForm.id = row.id
  groupForm.productId = row.productId
  groupForm.productName = row.productName
  groupForm.productPrice = row.productPrice
  groupForm.groupPrice = row.groupPrice
  groupForm.minGroupSize = row.minGroupSize
  groupForm.maxGroupSize = row.maxGroupSize
  groupForm.timeRange = [row.startTime, row.endTime]

  dialogVisible.value = true
}

// 重置拼团表单
function resetGroupForm() {
  groupForm.id = null
  groupForm.productId = null
  groupForm.productName = ''
  groupForm.productPrice = 0
  groupForm.groupPrice = 0
  groupForm.minGroupSize = 2
  groupForm.maxGroupSize = 5
  groupForm.timeRange = []

  if (groupFormRef.value) {
    groupFormRef.value.resetFields()
  }
}

// 提交拼团表单
function submitGroupForm() {
  groupFormRef.value.validate(valid => {
    if (valid) {
      // 构建提交数据
      const submitData = {
        id: groupForm.id,
        productId: groupForm.productId,
        groupPrice: groupForm.groupPrice,
        minGroupSize: groupForm.minGroupSize,
        maxGroupSize: groupForm.maxGroupSize,
        startTime: groupForm.timeRange[0],
        endTime: groupForm.timeRange[1]
      }

      if (dialogType.value === 'add') {
        // 创建拼团
        createGroupBuying(submitData).then(() => {
          ElMessage.success('创建成功')
          dialogVisible.value = false
          fetchGroupList()
        }).catch(err => {
          console.error('创建拼团失败:', err)
        })
      } else {
        // 更新拼团
        updateGroupBuying(submitData).then(() => {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          fetchGroupList()
        }).catch(err => {
          console.error('更新拼团失败:', err)
        })
      }
    }
  })
}

// 处理结束拼团
function handleEnd(row) {
  ElMessageBox.confirm('确定要结束该拼团活动吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    endGroupBuying(row.id).then(() => {
      ElMessage.success('拼团活动已结束')
      fetchGroupList()
    }).catch(err => {
      console.error('结束拼团失败:', err)
    })
  }).catch(() => {})
}

// 处理查看订单
function handleViewOrders(row) {
  // 获取拼团中的订单
  getGroupingOrders(row.id).then(res => {
    groupingOrders.value = res.data
  }).catch(err => {
    console.error('获取拼团中订单失败:', err)
  })

  // 获取已成团的订单
  getGroupedOrders(row.id).then(res => {
    groupedOrders.value = res.data
  }).catch(err => {
    console.error('获取已成团订单失败:', err)
  })

  orderDialogVisible.value = true
}

// 查看拼团详情
function viewGroupDetail(row) {
  console.log('查看拼团详情:', row)

  // 获取拼团订单详情
  getGroupOrderDetail(row.id).then(res => {
    currentGroupOrder.value = res.data
    groupDetailDialogVisible.value = true
  }).catch(err => {
    console.error('获取拼团订单详情失败:', err)
    ElMessage.error('获取拼团订单详情失败')
  })
}
</script>

<style lang="scss" scoped>
.group-buying-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-container {
    margin-bottom: 20px;
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
        font-weight: bold;
        margin-bottom: 5px;
      }

      .product-price {
        .original-price {
          color: #999;
          text-decoration: line-through;
          margin-right: 10px;
        }

        .group-price {
          color: #f56c6c;
          font-weight: bold;
        }
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }

  .product-option {
    display: flex;
    align-items: center;

    .option-image {
      width: 30px;
      height: 30px;
      margin-right: 10px;
    }

    .option-price {
      margin-left: auto;
      color: #f56c6c;
    }
  }

  .group-size-container {
    display: flex;
    align-items: center;

    .group-size-item {
      margin-bottom: 0;
    }

    .separator {
      margin: 0 10px;
    }
  }

  .user-info {
    display: flex;
    align-items: center;

    .el-avatar {
      margin-right: 8px;
    }

    .username {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .group-detail {
    .group-info {
      margin-bottom: 20px;

      h3 {
        margin-bottom: 15px;
      }
    }

    .order-list {
      h3 {
        margin-bottom: 15px;
      }
    }

    .empty-orders {
      padding: 20px 0;
    }
  }
}
</style>
