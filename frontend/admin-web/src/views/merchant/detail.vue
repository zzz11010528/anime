<template>
  <div class="merchant-detail-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商家详情</span>
          <el-button type="primary" size="small" @click="goBack">返回列表</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="商家ID">{{ merchantInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ merchantInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ merchantInfo.merchantName || merchantInfo.nickname }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ merchantInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱">{{ merchantInfo.email }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(merchantInfo.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(merchantInfo.status)">{{ getStatusText(merchantInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作">
          <el-button 
            :type="merchantInfo.status === 1 ? 'danger' : 'success'" 
            size="small" 
            @click="handleStatusChange"
          >
            {{ merchantInfo.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 认证信息 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>认证信息</span>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="公司名称">{{ certificationInfo.companyName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ certificationInfo.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ certificationInfo.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ certificationInfo.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag :type="getCertStatusType(certificationInfo.certificationStatus)">
            {{ getCertStatusText(certificationInfo.certificationStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="认证时间" v-if="certificationInfo.certificationStatus === 1">
          {{ formatDate(certificationInfo.updatedAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" v-if="certificationInfo.certificationStatus === 2">
          {{ certificationInfo.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="license-container" v-if="certificationInfo.businessLicense">
        <h3>营业执照</h3>
        <el-image 
          :src="certificationInfo.businessLicense" 
          :preview-src-list="[certificationInfo.businessLicense]"
          fit="contain"
          style="max-width: 100%; max-height: 300px;"
        ></el-image>
      </div>
    </el-card>
    
    <!-- 商品统计 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>商品统计</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">商品总数</div>
            <div class="stat-value">{{ statistics.productCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">在售商品</div>
            <div class="stat-value">{{ statistics.onSaleCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-title">总销量</div>
            <div class="stat-value">{{ statistics.totalSales || 0 }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 订单统计 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>订单统计</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">订单总数</div>
            <div class="stat-value">{{ statistics.orderCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">待发货</div>
            <div class="stat-value">{{ statistics.pendingShipCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">已完成</div>
            <div class="stat-value">{{ statistics.completedCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">总销售额</div>
            <div class="stat-value">¥{{ formatPrice(statistics.totalAmount) }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantDetail, updateMerchantStatus } from '../../api/merchant'

const route = useRoute()
const router = useRouter()
const merchantId = route.params.id

// 数据
const loading = ref(false)
const merchantInfo = ref({})
const certificationInfo = ref({})
const statistics = ref({})

// 方法
function goBack() {
  router.push('/merchant/list')
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
  return status === 1 ? 'success' : 'danger'
}

function getStatusText(status) {
  return status === 1 ? '正常' : '禁用'
}

function getCertStatusType(status) {
  if (status === 0) return 'info'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

function getCertStatusText(status) {
  if (status === 0) return '审核中'
  if (status === 1) return '已认证'
  if (status === 2) return '未通过'
  return '未知'
}

async function fetchMerchantDetail() {
  loading.value = true
  try {
    const res = await getMerchantDetail(merchantId)
    merchantInfo.value = res.data.merchantInfo || {}
    certificationInfo.value = res.data.certificationInfo || {}
    statistics.value = res.data.statistics || {}
  } catch (error) {
    console.error('获取商家详情失败:', error)
    ElMessage.error('获取商家详情失败')
  } finally {
    loading.value = false
  }
}

async function handleStatusChange() {
  const newStatus = merchantInfo.value.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该商家吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateMerchantStatus(merchantId, newStatus)
    ElMessage.success(`${actionText}成功`)
    merchantInfo.value.status = newStatus
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}商家失败:`, error)
      ElMessage.error(`${actionText}失败`)
    }
  }
}

// 生命周期钩子
onMounted(() => {
  fetchMerchantDetail()
})
</script>

<style lang="scss" scoped>
.merchant-detail-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .license-container {
    margin-top: 20px;
    text-align: center;
    
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
}
</style>
