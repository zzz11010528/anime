<template>
  <div class="merchant-audit-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商家认证审核</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="公司名称">
            <el-input v-model="searchForm.companyName" placeholder="请输入公司名称" clearable />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="searchForm.contactName" placeholder="请输入联系人" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="审核中" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
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
        :data="certificationList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="companyName" label="公司名称" width="180" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column label="营业执照" width="120">
          <template #default="scope">
            <el-image
              :src="scope.row.businessLicense.startsWith('http') ? scope.row.businessLicense : `/api${scope.row.businessLicense}`"
              :preview-src-list="[scope.row.businessLicense.startsWith('http') ? scope.row.businessLicense : `/api${scope.row.businessLicense}`]"
              fit="cover"
              style="width: 80px; height: 80px"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.certificationStatus === 0" type="warning">审核中</el-tag>
            <el-tag v-else-if="scope.row.certificationStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="scope.row.certificationStatus === 2" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" width="180" />
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button
              v-if="scope.row.certificationStatus === 0"
              type="success"
              link
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.certificationStatus === 0"
              type="danger"
              link
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
            <el-button type="primary" link @click="handleDetail(scope.row)">详情</el-button>
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

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="400px"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectFormRules"
        label-width="100px"
      >
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReject">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="认证详情"
      width="600px"
    >
      <div v-if="currentCertification" class="certification-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentCertification.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentCertification.username }}</el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ currentCertification.companyName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentCertification.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentCertification.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ currentCertification.contactEmail }}</el-descriptions-item>
          <el-descriptions-item label="公司地址" :span="2">{{ currentCertification.companyAddress }}</el-descriptions-item>
          <el-descriptions-item label="公司简介" :span="2">{{ currentCertification.companyDescription }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentCertification.certificationStatus === 0" type="warning">审核中</el-tag>
            <el-tag v-else-if="currentCertification.certificationStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="currentCertification.certificationStatus === 2" type="danger">已拒绝</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentCertification.createdAt }}</el-descriptions-item>
          <el-descriptions-item v-if="currentCertification.certificationStatus === 2" label="拒绝原因" :span="2">
            {{ currentCertification.rejectReason }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="image-container">
          <div class="image-item">
            <div class="image-title">营业执照</div>
            <el-image
              :src="currentCertification.businessLicense.startsWith('http') ? currentCertification.businessLicense : `/api${currentCertification.businessLicense}`"
              :preview-src-list="[currentCertification.businessLicense.startsWith('http') ? currentCertification.businessLicense : `/api${currentCertification.businessLicense}`]"
              fit="cover"
              style="width: 200px; height: 200px"
            />
          </div>
          <div class="image-item" v-if="currentCertification.idCardFront">
            <div class="image-title">身份证正面</div>
            <el-image
              :src="currentCertification.idCardFront.startsWith('http') ? currentCertification.idCardFront : `/api${currentCertification.idCardFront}`"
              :preview-src-list="[currentCertification.idCardFront.startsWith('http') ? currentCertification.idCardFront : `/api${currentCertification.idCardFront}`]"
              fit="cover"
              style="width: 200px; height: 200px"
            />
          </div>
          <div class="image-item" v-if="currentCertification.idCardBack">
            <div class="image-title">身份证反面</div>
            <el-image
              :src="currentCertification.idCardBack.startsWith('http') ? currentCertification.idCardBack : `/api${currentCertification.idCardBack}`"
              :preview-src-list="[currentCertification.idCardBack.startsWith('http') ? currentCertification.idCardBack : `/api${currentCertification.idCardBack}`]"
              fit="cover"
              style="width: 200px; height: 200px"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCertificationList, approveCertification, rejectCertification } from '../../api/merchant'

// 状态
const loading = ref(false)
const certificationList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const rejectDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentCertification = ref(null)

// 表单引用
const rejectFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  companyName: '',
  contactName: '',
  status: ''
})

// 拒绝表单
const rejectForm = reactive({
  id: null,
  rejectReason: ''
})

// 表单校验规则
const rejectFormRules = {
  rejectReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

// 方法
function fetchCertificationList() {
  loading.value = true
  getCertificationList({
    page: currentPage.value,
    size: pageSize.value,
    companyName: searchForm.companyName || undefined,
    contactName: searchForm.contactName || undefined,
    status: searchForm.status !== '' ? searchForm.status : undefined
  }).then(res => {
    certificationList.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchCertificationList()
}

function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchCertificationList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchCertificationList()
}

function handleApprove(row) {
  ElMessageBox.confirm(`确定要通过 ${row.companyName} 的认证申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    approveCertification(row.id).then(() => {
      ElMessage.success('审核通过成功')
      fetchCertificationList()
    })
  }).catch(() => {})
}

function handleReject(row) {
  rejectForm.id = row.id
  rejectForm.rejectReason = ''
  rejectDialogVisible.value = true
}

function handleDetail(row) {
  currentCertification.value = row
  detailDialogVisible.value = true
}

function submitReject() {
  rejectFormRef.value.validate(valid => {
    if (valid) {
      rejectCertification(rejectForm.id, rejectForm.rejectReason).then(() => {
        ElMessage.success('拒绝成功')
        rejectDialogVisible.value = false
        fetchCertificationList()
      })
    }
  })
}

// 生命周期钩子
onMounted(() => {
  fetchCertificationList()
})
</script>

<style lang="scss" scoped>
.merchant-audit-container {
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

  .certification-detail {
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
  }
}
</style>
