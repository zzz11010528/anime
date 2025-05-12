<template>
  <div class="product-detail-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品详情</span>
          <el-button type="primary" size="small" @click="goBack">返回列表</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="商品ID">{{ productInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ productInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="商品分类">{{ productInfo.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ productInfo.ipName }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ productInfo.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ formatPrice(productInfo.price) }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ productInfo.stock }}</el-descriptions-item>
        <el-descriptions-item label="销量">{{ productInfo.sales }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(productInfo.status)">{{ getStatusText(productInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(productInfo.auditStatus)">{{ getAuditStatusText(productInfo.auditStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(productInfo.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(productInfo.updatedAt) }}</el-descriptions-item>
        <el-descriptions-item label="操作" :span="2">
          <el-button 
            v-if="productInfo.auditStatus === 0" 
            type="success" 
            size="small" 
            @click="handleAudit(1)"
          >
            通过审核
          </el-button>
          <el-button 
            v-if="productInfo.auditStatus === 0" 
            type="danger" 
            size="small" 
            @click="handleAudit(0)"
          >
            拒绝审核
          </el-button>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 商品图片 -->
      <div class="image-container" v-if="productInfo.mainImage">
        <h3>商品主图</h3>
        <el-image 
          :src="productInfo.mainImage" 
          :preview-src-list="[productInfo.mainImage]"
          fit="contain"
          style="max-width: 100%; max-height: 300px;"
        ></el-image>
      </div>
      
      <!-- 商品详情图片 -->
      <div class="image-container" v-if="productInfo.detailImages && productInfo.detailImages.length > 0">
        <h3>商品详情图</h3>
        <div class="detail-images">
          <el-image 
            v-for="(image, index) in productInfo.detailImages" 
            :key="index"
            :src="image" 
            :preview-src-list="productInfo.detailImages"
            fit="contain"
            style="max-width: 100%; max-height: 300px; margin: 10px;"
          ></el-image>
        </div>
      </div>
      
      <!-- 商品详情 -->
      <div class="detail-content" v-if="productInfo.detail">
        <h3>商品详情</h3>
        <div v-html="productInfo.detail"></div>
      </div>
      
      <!-- 商品规格 -->
      <div class="specs-container" v-if="productInfo.specs && productInfo.specs.length > 0">
        <h3>商品规格</h3>
        <el-table :data="productInfo.specs" border style="width: 100%">
          <el-table-column prop="name" label="规格名称" />
          <el-table-column prop="value" label="规格值" />
        </el-table>
      </div>
    </el-card>
    
    <!-- 销售统计 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>销售统计</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">总销量</div>
            <div class="stat-value">{{ productInfo.sales || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">总销售额</div>
            <div class="stat-value">¥{{ formatPrice(productInfo.sales * productInfo.price) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">浏览量</div>
            <div class="stat-value">{{ productInfo.viewCount || 0 }}</div>
          </div>
        </el-col>
      </el-row>
      
      <div class="chart-container" v-if="productInfo.id">
        <h3>销售趋势</h3>
        <div ref="salesChartRef" class="chart"></div>
      </div>
    </el-card>
    
    <!-- 评价列表 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>商品评价</span>
        </div>
      </template>
      
      <el-table :data="reviews" style="width: 100%" v-loading="reviewsLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评价时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="danger" link @click="handleDeleteReview(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="empty-block" v-if="reviews.length === 0 && !reviewsLoading">
        <el-empty description="暂无评价" />
      </div>
    </el-card>
    
    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { getProductDetail, auditProduct } from '../../api/product'
import { getProductReviews, deleteProductReview } from '../../api/review'
import { getProductSalesTrend } from '../../api/stat'

const route = useRoute()
const router = useRouter()
const productId = route.params.id

// 数据
const loading = ref(false)
const reviewsLoading = ref(false)
const productInfo = ref({})
const reviews = ref([])
const salesChartRef = ref(null)
let salesChart = null

// 拒绝审核相关
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  reason: ''
})

// 方法
function goBack() {
  router.push('/product/list')
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

function getStatusType(status) {
  return status === 1 ? 'success' : 'info'
}

function getStatusText(status) {
  return status === 1 ? '上架中' : '已下架'
}

function getAuditStatusType(status) {
  if (status === 0) return 'warning'  // 待审核
  if (status === 1) return 'success'  // 已通过
  if (status === 2) return 'danger'   // 已拒绝
  return 'info'
}

function getAuditStatusText(status) {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已拒绝'
  return '未知'
}

async function fetchProductDetail() {
  loading.value = true
  try {
    const res = await getProductDetail(productId)
    productInfo.value = res.data || {}
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

async function fetchProductReviews() {
  reviewsLoading.value = true
  try {
    const res = await getProductReviews({ productId, page: 1, size: 10 })
    reviews.value = res.data.records || []
  } catch (error) {
    console.error('获取商品评价失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

function handleAudit(status) {
  if (status === 1) {
    ElMessageBox.confirm('确定要通过该商品的审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      auditProduct(productId, status).then(() => {
        ElMessage.success('审核通过成功')
        productInfo.value.auditStatus = 1
      }).catch(error => {
        console.error('审核失败:', error)
        ElMessage.error('审核失败')
      })
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
  
  auditProduct(productId, 0, rejectForm.reason).then(() => {
    ElMessage.success('审核拒绝成功')
    productInfo.value.auditStatus = 2
    rejectDialogVisible.value = false
  }).catch(error => {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  })
}

async function handleDeleteReview(row) {
  try {
    await ElMessageBox.confirm(`确定要删除该评价吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteProductReview(row.id)
    ElMessage.success('删除成功')
    fetchProductReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

function initSalesChart() {
  if (salesChartRef.value) {
    salesChart = echarts.init(salesChartRef.value)
    fetchSalesTrend()
  }
}

function fetchSalesTrend() {
  getProductSalesTrend(productId).then(res => {
    const { dates, sales } = res.data
    salesChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: sales,
          type: 'line',
          smooth: true,
          areaStyle: {}
        }
      ]
    })
  }).catch(error => {
    console.error('获取销售趋势失败:', error)
  })
}

// 生命周期钩子
onMounted(() => {
  fetchProductDetail()
  fetchProductReviews()
  
  nextTick(() => {
    initSalesChart()
    
    window.addEventListener('resize', () => {
      salesChart && salesChart.resize()
    })
  })
})
</script>

<style lang="scss" scoped>
.product-detail-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .image-container {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
    }
    
    .detail-images {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-start;
    }
  }
  
  .detail-content {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
    }
  }
  
  .specs-container {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
    }
  }
  
  .stat-card {
    background-color: #f5f7fa;
    border-radius: 4px;
    padding: 20px;
    text-align: center;
    
    .stat-title {
      font-size: 14px;
      color: #606266;
      margin-bottom: 10px;
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
  }
  
  .chart-container {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
    }
    
    .chart {
      height: 300px;
      width: 100%;
    }
  }
  
  .empty-block {
    margin: 20px 0;
  }
}
</style>
