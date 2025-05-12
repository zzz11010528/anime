<template>
  <div class="product-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
          </el-form-item>
          <el-form-item label="商家ID">
            <el-input v-model="searchForm.userId" placeholder="请输入商家ID" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select style="width: 150px;" v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
              <el-option
                v-for="category in categoryOptions"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="IP">
            <el-select style="width: 150px;" v-model="searchForm.ipId" placeholder="请选择IP" clearable>
              <el-option
                v-for="ip in ipOptions"
                :key="ip.id"
                :label="ip.name"
                :value="ip.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item  label="状态">
            <el-select style="width: 150px;" v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="下架" :value="0" />
              <el-option label="上架" :value="1" />
              <el-option label="审核中" :value="2" />
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
        :data="productList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <el-image
              :src="scope.row.mainImage"
              :preview-src-list="productList.map(item => item.mainImage).filter(Boolean)"
              :initial-index="getImageIndex(scope.row)"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px; cursor: pointer; transition: all 0.3s;"
              preview-teleported
              class="product-image"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" width="180" show-overflow-tooltip />
        <el-table-column prop="userId" label="商家ID" width="80" />
        <el-table-column prop="username" label="商家名称" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="ipName" label="IP" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="认证商家" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isCertified === 1" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">下架</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">上架</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning">审核中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 2"
              type="success"
              link
              @click="handleAudit(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 2"
              type="danger"
              link
              @click="handleAudit(scope.row, 0)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="scope.row.status !== 2"
              type="primary"
              link
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 0 ? '上架' : '下架' }}
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

    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商品详情"
      width="800px"
    >
      <div v-if="currentProduct" class="product-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品ID">{{ currentProduct.id }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ currentProduct.name }}</el-descriptions-item>
          <el-descriptions-item label="商家ID">{{ currentProduct.userId }}</el-descriptions-item>
          <el-descriptions-item label="商家名称">{{ currentProduct.username }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentProduct.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ currentProduct.ipName }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ currentProduct.price }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ currentProduct.stock }}</el-descriptions-item>
          <el-descriptions-item label="销量">{{ currentProduct.sales }}</el-descriptions-item>
          <el-descriptions-item label="收藏数">{{ currentProduct.collectCount }}</el-descriptions-item>
          <el-descriptions-item label="认证商家">
            <el-tag v-if="currentProduct.isCertified === 1" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentProduct.status === 0" type="info">下架</el-tag>
            <el-tag v-else-if="currentProduct.status === 1" type="success">上架</el-tag>
            <el-tag v-else-if="currentProduct.status === 2" type="warning">审核中</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentProduct.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentProduct.updatedAt }}</el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">{{ currentProduct.description }}</el-descriptions-item>
        </el-descriptions>

        <div class="image-container">
          <div class="image-title">商品主图</div>
          <el-image
            :src="currentProduct.mainImage"
            :preview-src-list="[currentProduct.mainImage]"
            fit="cover"
            style="width: 200px; height: 200px"
            preview-teleported
          />
        </div>

        <div class="image-container" v-if="currentProduct.images && currentProduct.images.length > 0">
          <div class="image-title">商品图片</div>
          <div class="image-list">
            <div v-for="(image, index) in currentProduct.images" :key="index" class="image-item">
              <el-image
                :src="image.imageUrl"
                :preview-src-list="currentProduct.images.map(img => img.imageUrl)"
                :initial-index="index"
                fit="cover"
                style="width: 120px; height: 120px"
                preview-teleported
              />
            </div>
          </div>
        </div>

        <div class="detail-content" v-if="currentProduct.detail">
          <div class="detail-title">商品详情</div>
          <div v-html="currentProduct.detail.content"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, getProductDetail, auditProduct, updateProductStatus } from '../../api/product'
import { getCategoryList } from '../../api/category'
import { getIpList } from '../../api/ip'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()

// 状态
const loading = ref(false)
const productList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentProduct = ref(null)
const categoryOptions = ref([])
const ipOptions = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  userId: route.query.merchantId || '',
  categoryId: '',
  ipId: '',
  status: ''
})

// 方法
function fetchProductList() {
  loading.value = true
  getProductList({
    page: currentPage.value,
    size: pageSize.value,
    keyword: searchForm.name || undefined,
    userId: searchForm.userId || undefined,
    categoryId: searchForm.categoryId || undefined,
    ipId: searchForm.ipId || undefined,
    status: searchForm.status !== '' ? searchForm.status : undefined
  }).then(res => {
    // 处理图片URL，添加/api前缀
    const records = res.data.records
    records.forEach(item => {
      if (item.mainImage) {
        item.mainImage = formatImageUrl(item.mainImage)
      }
    })
    productList.value = records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

function fetchCategoryList() {
  getCategoryList().then(res => {
    categoryOptions.value = res.data
  })
}

function fetchIpList() {
  getIpList().then(res => {
    ipOptions.value = res.data
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchProductList()
}

function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    if (key !== 'userId' || !route.query.merchantId) {
      searchForm[key] = ''
    }
  })
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchProductList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchProductList()
}

// 获取图片在预览列表中的索引
function getImageIndex(row) {
  if (!row.mainImage) return 0
  const validImages = productList.value.map(item => item.mainImage).filter(Boolean)
  return validImages.indexOf(row.mainImage)
}

function handleDetail(row) {
  getProductDetail(row.id).then(res => {
    const product = res.data

    // 处理主图URL
    if (product.mainImage) {
      product.mainImage = formatImageUrl(product.mainImage)
    }

    // 处理商品图片URL
    if (product.images && product.images.length > 0) {
      product.images.forEach(image => {
        if (image.imageUrl) {
          image.imageUrl = formatImageUrl(image.imageUrl)
        }
      })
    }

    currentProduct.value = product
    detailDialogVisible.value = true
  })
}

function handleAudit(row, status) {
  const statusText = status === 1 ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${statusText}商品 ${row.name} 的审核吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    auditProduct(row.id, status).then(() => {
      ElMessage.success(`审核${statusText}成功`)
      fetchProductList()
    })
  }).catch(() => {})
}

function handleStatusChange(row) {
  const newStatus = row.status === 0 ? 1 : 0
  const statusText = newStatus === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确定要${statusText}商品 ${row.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    updateProductStatus(row.id, newStatus).then(() => {
      ElMessage.success(`${statusText}成功`)
      fetchProductList()
    })
  }).catch(() => {})
}

// 监听路由参数变化
watch(() => route.query.merchantId, (newVal) => {
  if (newVal) {
    searchForm.userId = newVal
    handleSearch()
  }
})

// 生命周期钩子
onMounted(() => {
  fetchProductList()
  fetchCategoryList()
  fetchIpList()
})
</script>

<style lang="scss" scoped>
.product-list-container {
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

  :deep(.product-image) {
    &:hover {
      transform: scale(1.05);
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
  }

  .product-detail {
    .image-container {
      margin-top: 20px;

      .image-title {
        font-weight: bold;
        margin-bottom: 10px;
        font-size: 16px;
        color: #333;
      }

      .image-list {
        display: flex;
        flex-wrap: wrap;

        .image-item {
          margin-right: 15px;
          margin-bottom: 15px;
          border-radius: 4px;
          overflow: hidden;
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          transition: all 0.3s;

          &:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
          }
        }
      }
    }

    .detail-content {
      margin-top: 20px;

      .detail-title {
        font-weight: bold;
        margin-bottom: 10px;
      }
    }
  }
}
</style>
