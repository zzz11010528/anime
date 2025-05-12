<template>
  <div class="product-list-container">
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true" class="filter-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable style="width: 220px;" />
        </el-form-item>

        <el-form-item label="商品分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable style="width: 180px;">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="IP">
          <el-select v-model="queryParams.ipId" placeholder="请选择IP" clearable style="width: 180px;">
            <el-option
              v-for="item in ipOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 180px;">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
            <el-option label="审核中" :value="2" />
            <el-option label="审核失败" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="价格区间">
          <el-input-number
            v-model="queryParams.minPrice"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="最低价"
            style="width: 130px"
          />
          <span class="price-separator">-</span>
          <el-input-number
            v-model="queryParams.maxPrice"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="最高价"
            style="width: 130px"
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
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增商品
          </el-button>
        </div>

        <!-- 移除导入导出功能 -->
        <div class="right-actions">
          <el-button type="primary" plain @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 商品列表 -->
      <el-table
        v-loading="loading"
        :data="productList"
        @selection-change="handleSelectionChange"
        border
        style="width: 100%"
      >


        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.mainImage)"
              :preview-src-list="[getImageUrl(row.mainImage)]"
              fit="cover"
              style="width: 60px; height: 60px"
              :preview-teleported="true"
            />
          </template>
        </el-table-column>

        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />

        <el-table-column prop="categoryName" label="分类" width="120" />

        <el-table-column prop="ipName" label="IP" width="120" />

        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <div class="price-info">
              <div class="current-price">¥{{ formatPrice(row.price) }}</div>
              <div v-if="row.originalPrice" class="original-price">¥{{ formatPrice(row.originalPrice) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存" width="80" />

        <el-table-column prop="sales" label="销量" width="80" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handlePublish(row)"
            >
              上架
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handleUnpublish(row)"
            >
              下架
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
    </el-card>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus
} from '@element-plus/icons-vue'
import {
  getProductList, deleteProduct, publishProduct, unpublishProduct
} from '../../api/product'
import { getCategoryList, getIpList } from '../../api/product'

const router = useRouter()

// 状态
const loading = ref(false)
const productList = ref([])
const total = ref(0)
// 已不再使用
// const selectedProducts = ref([])
const categoryOptions = ref([])
const ipOptions = ref([])

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  categoryId: null,
  ipId: null,
  status: null,
  minPrice: null,
  maxPrice: null
})



// 方法
function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getStatusType(status) {
  switch (status) {
    case 0: return 'info'     // 下架
    case 1: return 'success'  // 上架
    case 2: return 'warning'  // 审核中
    case 3: return 'danger'   // 审核失败
    default: return 'info'
  }
}

function getStatusText(status) {
  switch (status) {
    case 0: return '下架'
    case 1: return '上架'
    case 2: return '审核中'
    case 3: return '审核失败'
    default: return '未知'
  }
}

// 获取商品列表
async function getProducts() {
  loading.value = true
  try {
    const res = await getProductList(queryParams)
    productList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categoryOptions.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取IP列表
async function fetchIps() {
  try {
    const res = await getIpList()
    ipOptions.value = res.data
  } catch (error) {
    console.error('获取IP列表失败:', error)
  }
}

// 搜索
function handleQuery() {
  queryParams.page = 1
  getProducts()
}

// 重置
function resetQuery() {
  queryParams.name = ''
  queryParams.categoryId = null
  queryParams.ipId = null
  queryParams.status = null
  queryParams.minPrice = null
  queryParams.maxPrice = null
  handleQuery()
}

// 选择变化 - 已不再使用
function handleSelectionChange(selection) {
  // 保留函数但不再使用
}

// 分页大小变化
function handleSizeChange() {
  getProducts()
}

// 页码变化
function handleCurrentChange() {
  getProducts()
}

// 新增商品
function handleAdd() {
  router.push('/product/add')
}

// 编辑商品
function handleEdit(row) {
  router.push(`/product/edit/${row.id}`)
}

// 查看商品
function handleView(row) {
  router.push(`/product/detail/${row.id}`)
}

// 删除商品
function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除商品"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      console.log('删除商品ID:', row.id);
      await deleteProduct(row.id)
      ElMessage.success('删除成功')
      getProducts()
    } catch (error) {
      console.error('删除商品失败:', error);
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

// 上架商品
async function handlePublish(row) {
  try {
    await publishProduct(row.id)
    ElMessage.success('上架成功')
    getProducts()
  } catch (error) {
    ElMessage.error(error.message || '上架失败')
  }
}

// 下架商品
async function handleUnpublish(row) {
  try {
    await unpublishProduct(row.id)
    ElMessage.success('下架成功')
    getProducts()
  } catch (error) {
    ElMessage.error(error.message || '下架失败')
  }
}

// 刷新列表
function handleRefresh() {
  getProducts()
  ElMessage.success('刷新成功')
}

// 获取图片URL（添加API前缀）
function getImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) {
    return url;
  } else {
    return `/api${url}`;
  }
}

// 生命周期钩子
onMounted(() => {
  // 确保localStorage中有userId
  if (!localStorage.getItem('userId')) {
    const userInfo = JSON.parse(localStorage.getItem('merchantInfo') || '{}');
    if (userInfo.id) {
      localStorage.setItem('userId', userInfo.id);
    }
  }

  getProducts()
  fetchCategories()
  fetchIps()
})
</script>

<style lang="scss" scoped>
.product-list-container {
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

      .right-actions {
        display: flex;
        gap: 10px;
      }
    }

    .price-info {
      .current-price {
        font-weight: bold;
        color: #f56c6c;
      }

      .original-price {
        font-size: 12px;
        color: #999;
        text-decoration: line-through;
      }
    }

    .pagination {
      margin-top: 20px;
      text-align: right;
    }
  }


}
</style>
