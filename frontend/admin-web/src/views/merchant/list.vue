<template>
  <div class="merchant-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商家列表</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable />
          </el-form-item>
          <el-form-item label="公司名称">
            <el-input v-model="searchForm.companyName" placeholder="请输入公司名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="禁用" :value="0" />
              <el-option label="正常" :value="1" />
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
        :data="merchantList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="companyName" label="公司名称" width="180" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-avatar :src="getAvatarSrc(scope.row.avatar)" :size="40">
              <img src="../../assets/images/default-avatar.png" />
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column prop="certifiedAt" label="认证时间" width="180" />
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button type="primary" link @click="handleDetail(scope.row)">详情</el-button>
              <el-button type="primary" link @click="handleProducts(scope.row)">商品</el-button>
              <el-button type="primary" link @click="handleOrders(scope.row)">订单</el-button>
            </div>
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商家详情"
      width="600px"
    >
      <div v-if="currentMerchant" class="merchant-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentMerchant.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentMerchant.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentMerchant.nickname }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentMerchant.email }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentMerchant.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentMerchant.status === 1 ? 'success' : 'danger'">
              {{ currentMerchant.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ currentMerchant.companyName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentMerchant.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ currentMerchant.contactEmail }}</el-descriptions-item>
          <el-descriptions-item label="公司地址" :span="2">{{ currentMerchant.companyAddress }}</el-descriptions-item>
          <el-descriptions-item label="公司简介" :span="2">{{ currentMerchant.companyDescription }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentMerchant.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="认证时间">{{ currentMerchant.certifiedAt }}</el-descriptions-item>
        </el-descriptions>

        <div class="image-container">
          <div class="image-item">
            <div class="image-title">营业执照</div>
            <el-image
              :src="getAvatarSrc(currentMerchant.businessLicense)"
              :preview-src-list="[getAvatarSrc(currentMerchant.businessLicense)]"
              fit="cover"
              style="width: 200px; height: 200px"
            />
          </div>
        </div>

        <div class="stat-container">
          <div class="stat-title">商家统计</div>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">商品数量</div>
                <div class="stat-value">{{ currentMerchant.productCount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">订单数量</div>
                <div class="stat-value">{{ currentMerchant.orderCount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">销售额</div>
                <div class="stat-value">¥{{ currentMerchant.salesAmount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">评分</div>
                <div class="stat-value">
                  <el-rate
                    v-model="currentMerchant.rating"
                    disabled
                    show-score
                    text-color="#ff9900"
                  />
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMerchantList, getMerchantDetail, updateMerchantStatus } from '../../api/merchant'
import defaultAvatar from '../../assets/images/default-avatar.png'

const router = useRouter()

// 状态
const loading = ref(false)
const merchantList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentMerchant = ref(null)

// 搜索表单
const searchForm = reactive({
  username: '',
  nickname: '',
  companyName: '',
  status: ''
})

// 方法
function fetchMerchantList() {
  loading.value = true
  getMerchantList({
    page: currentPage.value,
    size: pageSize.value,
    username: searchForm.username || undefined,
    nickname: searchForm.nickname || undefined,
    companyName: searchForm.companyName || undefined,
    status: searchForm.status !== '' ? searchForm.status : undefined
  }).then(res => {
    merchantList.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchMerchantList()
}

function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchMerchantList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchMerchantList()
}

function handleStatusChange(row) {
  updateMerchantStatus(row.id, row.status).then(() => {
    ElMessage.success(`商家状态已${row.status === 1 ? '启用' : '禁用'}`)
  }).catch(() => {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  })
}

function handleDetail(row) {
  getMerchantDetail(row.id).then(res => {
    currentMerchant.value = res.data
    detailDialogVisible.value = true
  })
}

function handleProducts(row) {
  router.push({
    path: '/product/list',
    query: { merchantId: row.id }
  })
}

function handleOrders(row) {
  router.push({
    path: '/order/list',
    query: { merchantId: row.id }
  })
}

// 处理头像URL，确保包含/api前缀
function getAvatarSrc(avatar) {
  if (!avatar) return defaultAvatar

  // 确保头像URL包含/api前缀
  if (avatar.startsWith('http')) {
    return avatar
  } else if (avatar.startsWith('/api')) {
    return avatar
  } else {
    return `/api${avatar}`
  }
}

// 生命周期钩子
onMounted(() => {
  fetchMerchantList()
})
</script>

<style lang="scss" scoped>
.merchant-list-container {
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

  .operation-buttons {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    white-space: nowrap;

    .el-button {
      margin-right: 5px;
      padding: 4px 8px;
    }
  }

  .merchant-detail {
    .image-container {
      display: flex;
      flex-wrap: wrap;
      margin-top: 20px;

      .image-item {
        margin-right: 20px;
        margin-bottom: 20px;

        .image-title {
          margin-bottom: 10px;
          font-weight: bold;
        }
      }
    }

    .stat-container {
      margin-top: 20px;

      .stat-title {
        font-weight: bold;
        margin-bottom: 15px;
        font-size: 16px;
      }

      .stat-item {
        background-color: #f5f7fa;
        padding: 15px;
        border-radius: 4px;
        text-align: center;

        .stat-label {
          color: #909399;
          margin-bottom: 10px;
        }

        .stat-value {
          font-size: 20px;
          font-weight: bold;
          color: #409EFF;
        }
      }
    }
  }
}
</style>
