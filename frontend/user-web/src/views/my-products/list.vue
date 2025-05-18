<template>
  <div class="my-products-container">
    <el-card class="my-products-card">
      <template #header>
        <div class="card-header">
          <h2>我的商品</h2>
          <el-button type="primary" @click="addProduct">发布新商品</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.keyword" placeholder="请输入商品名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="商品状态">
            <el-select style="width: 150px;" v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="上架" :value="1"></el-option>
              <el-option label="下架" :value="0"></el-option>
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

      <div v-else-if="products.length > 0" class="products-table">
        <el-table :data="products" border style="width: 100%">
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <div class="product-info">
                <el-image
                  :src="formatImageUrl(scope.row.mainImage)"
                  class="product-image"
                  fit="cover"
                ></el-image>
                <div class="product-detail">
                  <div class="product-name">{{ scope.row.name }}</div>
                  <div class="product-price">¥{{ formatPrice(scope.row.price) }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="120"></el-table-column>
          <el-table-column prop="stock" label="库存" width="80"></el-table-column>
          <el-table-column prop="sales" label="销量" width="80"></el-table-column>
          <el-table-column label="状态" width="100">
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
              <el-button type="primary" link @click="viewProduct(scope.row.id)">查看</el-button>
              <el-button type="success" link @click="editProduct(scope.row.id)">编辑</el-button>
              <el-button 
                :type="scope.row.status === 1 ? 'warning' : 'success'" 
                link 
                @click="toggleStatus(scope.row)"
              >
                {{ scope.row.status === 1 ? '下架' : '上架' }}
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

      <el-empty v-else description="暂无商品数据"></el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyProducts, updateProductStatus } from '../../api/seller'
import { formatImageUrl } from '../../utils/image'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 获取商品列表
async function fetchProducts() {
  try {
    loading.value = true
    const res = await getMyProducts({
      page: page.value,
      size: size.value,
      keyword: searchForm.keyword,
      status: searchForm.status
    })

    if (res.data) {
      products.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
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
    case 0: return '下架'
    case 1: return '上架'
    case 2: return '审核中'
    case 3: return '审核失败'
    case -1: return '已删除'
    default: return '未知'
  }
}

// 获取状态类型
function getStatusType(status) {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    case -1: return 'danger'
    default: return 'info'
  }
}

// 分页处理
function handleSizeChange() {
  page.value = 1
  fetchProducts()
}

function handleCurrentChange() {
  fetchProducts()
}

// 搜索
function search() {
  page.value = 1
  fetchProducts()
}

// 重置搜索
function resetSearch() {
  searchForm.keyword = ''
  searchForm.status = ''
  page.value = 1
  fetchProducts()
}

// 商品操作
function addProduct() {
  router.push('/my-products/add')
}

function viewProduct(id) {
  router.push(`/product/${id}`)
}

function editProduct(id) {
  router.push(`/my-products/edit/${id}`)
}

async function toggleStatus(product) {
  try {
    const newStatus = product.status === 1 ? 0 : 1
    const actionText = newStatus === 1 ? '上架' : '下架'
    
    await ElMessageBox.confirm(`确认${actionText}该商品?`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateProductStatus(product.id, newStatus)
    ElMessage.success(`商品已${actionText}`)
    fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新商品状态失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 生命周期钩子
onMounted(() => {
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.my-products-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 0;

  .my-products-card {
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

    .products-table {
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

      .pagination {
        margin-top: 20px;
        text-align: right;
      }
    }
  }
}
</style>
