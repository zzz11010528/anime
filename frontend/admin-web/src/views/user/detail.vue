<template>
  <div class="user-detail-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户详情</span>
          <el-button type="primary" size="small" @click="goBack">返回列表</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="用户ID">{{ userInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号码">{{ userInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱">{{ userInfo.email }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(userInfo.status)">{{ getStatusText(userInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作">
          <el-button 
            :type="userInfo.status === 1 ? 'danger' : 'success'" 
            size="small" 
            @click="handleStatusChange"
          >
            {{ userInfo.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="warning" size="small" @click="handleResetPassword">重置密码</el-button>
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="avatar-container" v-if="userInfo.avatar">
        <h3>用户头像</h3>
        <el-avatar :src="userInfo.avatar" :size="100"></el-avatar>
      </div>
    </el-card>
    
    <!-- 用户统计信息 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>用户统计</span>
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
            <div class="stat-title">消费总额</div>
            <div class="stat-value">¥{{ formatPrice(statistics.totalAmount) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">帖子数量</div>
            <div class="stat-value">{{ statistics.postCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-title">收藏数量</div>
            <div class="stat-value">{{ statistics.collectionCount || 0 }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 最近订单 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>最近订单</span>
          <el-button type="primary" link @click="viewAllOrders">查看全部</el-button>
        </div>
      </template>
      
      <el-table :data="recentOrders" style="width: 100%" v-loading="ordersLoading">
        <el-table-column prop="id" label="订单ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="totalAmount" label="订单金额" width="100">
          <template #default="scope">
            ¥{{ formatPrice(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="scope">
            <el-tag :type="getOrderStatusType(scope.row.status)">
              {{ getOrderStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" link @click="viewOrderDetail(scope.row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 最近帖子 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>最近帖子</span>
          <el-button type="primary" link @click="viewAllPosts">查看全部</el-button>
        </div>
      </template>
      
      <el-table :data="recentPosts" style="width: 100%" v-loading="postsLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column prop="commentCount" label="评论数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getPostStatusType(scope.row.status)">
              {{ getPostStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" link @click="viewPostDetail(scope.row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="500px"
    >
      <el-form :model="resetPasswordForm" label-width="100px">
        <el-form-item label="新密码" prop="password" :rules="[{ required: true, message: '请输入新密码', trigger: 'blur' }]">
          <el-input 
            v-model="resetPasswordForm.password" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" :rules="[{ required: true, message: '请确认密码', trigger: 'blur' }]">
          <el-input 
            v-model="resetPasswordForm.confirmPassword" 
            type="password" 
            placeholder="请确认密码"
            show-password
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmResetPassword">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserDetail, updateUserStatus, resetUserPassword } from '../../api/user'
import { getOrderList } from '../../api/order'
import { getPostList } from '../../api/post'

const route = useRoute()
const router = useRouter()
const userId = route.params.id

// 数据
const loading = ref(false)
const ordersLoading = ref(false)
const postsLoading = ref(false)
const userInfo = ref({})
const statistics = ref({})
const recentOrders = ref([])
const recentPosts = ref([])

// 重置密码相关
const resetPasswordDialogVisible = ref(false)
const resetPasswordForm = reactive({
  password: '',
  confirmPassword: ''
})

// 方法
function goBack() {
  router.push('/user/list')
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

function getOrderStatusType(status) {
  if (status === 0) return 'info'     // 待付款
  if (status === 1) return 'warning'  // 待发货
  if (status === 2) return 'primary'  // 待收货
  if (status === 3) return 'success'  // 已完成
  if (status === 4) return 'danger'   // 已取消
  if (status === 5) return 'warning'  // 退款中
  if (status === 6) return 'info'     // 已退款
  return 'info'
}

function getOrderStatusText(status) {
  if (status === 0) return '待付款'
  if (status === 1) return '待发货'
  if (status === 2) return '待收货'
  if (status === 3) return '已完成'
  if (status === 4) return '已取消'
  if (status === 5) return '退款中'
  if (status === 6) return '已退款'
  return '未知'
}

function getPostStatusType(status) {
  if (status === 0) return 'warning'  // 待审核
  if (status === 1) return 'success'  // 已发布
  if (status === 2) return 'danger'   // 已拒绝
  return 'info'
}

function getPostStatusText(status) {
  if (status === 0) return '待审核'
  if (status === 1) return '已发布'
  if (status === 2) return '已拒绝'
  return '未知'
}

async function fetchUserDetail() {
  loading.value = true
  try {
    const res = await getUserDetail(userId)
    userInfo.value = res.data.userInfo || {}
    statistics.value = res.data.statistics || {}
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

async function fetchRecentOrders() {
  ordersLoading.value = true
  try {
    const res = await getOrderList({ userId, page: 1, size: 5 })
    recentOrders.value = res.data.records || []
  } catch (error) {
    console.error('获取最近订单失败:', error)
  } finally {
    ordersLoading.value = false
  }
}

async function fetchRecentPosts() {
  postsLoading.value = true
  try {
    const res = await getPostList({ userId, page: 1, size: 5 })
    recentPosts.value = res.data.records || []
  } catch (error) {
    console.error('获取最近帖子失败:', error)
  } finally {
    postsLoading.value = false
  }
}

async function handleStatusChange() {
  const newStatus = userInfo.value.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户"${userInfo.value.nickname || userInfo.value.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateUserStatus(userId, newStatus)
    ElMessage.success(`${actionText}成功`)
    userInfo.value.status = newStatus
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}用户失败:`, error)
      ElMessage.error(`${actionText}失败`)
    }
  }
}

function handleResetPassword() {
  resetPasswordForm.password = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

async function confirmResetPassword() {
  if (!resetPasswordForm.password) {
    ElMessage.warning('请输入新密码')
    return
  }
  
  if (resetPasswordForm.password !== resetPasswordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    await resetUserPassword(userId, resetPasswordForm.password)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error) {
    console.error('密码重置失败:', error)
    ElMessage.error('密码重置失败')
  }
}

function viewAllOrders() {
  router.push({ path: '/order/list', query: { userId } })
}

function viewOrderDetail(orderId) {
  router.push(`/order/detail/${orderId}`)
}

function viewAllPosts() {
  router.push({ path: '/content/community', query: { userId } })
}

function viewPostDetail(postId) {
  router.push(`/content/post/detail/${postId}`)
}

// 生命周期钩子
onMounted(() => {
  fetchUserDetail()
  fetchRecentOrders()
  fetchRecentPosts()
})
</script>

<style lang="scss" scoped>
.user-detail-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .avatar-container {
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
