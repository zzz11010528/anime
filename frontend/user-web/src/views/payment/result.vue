<template>
  <div class="payment-result-container">
    <el-card class="result-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <template v-else>
        <div class="result-icon">
          <el-icon v-if="status === 'success'" class="success-icon"><CircleCheckFilled /></el-icon>
          <el-icon v-else-if="status === 'fail'" class="fail-icon"><CircleCloseFilled /></el-icon>
          <el-icon v-else class="warning-icon"><WarningFilled /></el-icon>
        </div>
        
        <div class="result-title">
          <h2 v-if="status === 'success'">支付成功</h2>
          <h2 v-else-if="status === 'fail'">支付失败</h2>
          <h2 v-else>支付异常</h2>
        </div>
        
        <div class="result-message">
          <p v-if="status === 'success'">您的订单已支付成功，感谢您的购买！</p>
          <p v-else-if="status === 'fail'">支付验证失败，请稍后重试或联系客服。</p>
          <p v-else>支付过程中发生异常，请稍后重试或联系客服。</p>
        </div>
        
        <div class="result-info" v-if="status === 'success' && orderNo">
          <p>订单号：{{ orderNo }}</p>
        </div>
        
        <div class="result-actions">
          <el-button v-if="status === 'success' && orderNo" type="primary" @click="goToOrderDetail">
            查看订单详情
          </el-button>
          <el-button @click="goToOrderList">
            返回订单列表
          </el-button>
          <el-button v-if="status !== 'success'" type="primary" @click="goToHome">
            返回首页
          </el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheckFilled, CircleCloseFilled, WarningFilled } from '@element-plus/icons-vue'
import { getOrderDetail } from '../../api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const status = ref('')
const orderNo = ref('')

// 获取订单详情
async function fetchOrderDetail() {
  if (!orderNo.value) return
  
  try {
    await getOrderDetail(orderNo.value)
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

// 跳转到订单详情页
function goToOrderDetail() {
  router.push(`/order/${orderNo.value}`)
}

// 跳转到订单列表页
function goToOrderList() {
  router.push('/order')
}

// 跳转到首页
function goToHome() {
  router.push('/')
}

onMounted(async () => {
  // 从URL参数中获取支付状态和订单号
  status.value = route.query.status || 'error'
  orderNo.value = route.query.orderNo || ''
  
  if (status.value === 'success' && orderNo.value) {
    try {
      // 获取订单详情，确认支付状态
      await fetchOrderDetail()
      ElMessage.success('支付成功')
    } catch (error) {
      console.error('获取订单详情失败:', error)
    }
  }
  
  loading.value = false
})
</script>

<style lang="scss" scoped>
.payment-result-container {
  max-width: 600px;
  margin: 50px auto;
  
  .result-card {
    padding: 20px;
    text-align: center;
    
    .loading-container {
      padding: 40px 20px;
    }
    
    .result-icon {
      font-size: 80px;
      margin-bottom: 20px;
      
      .success-icon {
        color: #67c23a;
      }
      
      .fail-icon {
        color: #f56c6c;
      }
      
      .warning-icon {
        color: #e6a23c;
      }
    }
    
    .result-title {
      margin-bottom: 20px;
      
      h2 {
        font-size: 24px;
        font-weight: 500;
      }
    }
    
    .result-message {
      margin-bottom: 30px;
      color: #606266;
      
      p {
        font-size: 16px;
        line-height: 1.5;
      }
    }
    
    .result-info {
      margin-bottom: 30px;
      padding: 15px;
      background-color: #f8f9fa;
      border-radius: 4px;
      
      p {
        margin: 5px 0;
        color: #606266;
      }
    }
    
    .result-actions {
      display: flex;
      justify-content: center;
      gap: 15px;
    }
  }
}
</style>
