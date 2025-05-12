<template>
  <div class="ip-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>IP管理</span>
          <el-button type="primary" @click="handleAdd">添加IP</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="IP名称">
            <el-input v-model="searchForm.name" placeholder="请输入IP名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
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
        :data="ipList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="IP图标" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.logo"
              :src="scope.row.logo ? UPLOAD_CONFIG.getFullUrl(scope.row.logo) : ''"
              :preview-src-list="allIpList.filter(item => item.logo).map(item => UPLOAD_CONFIG.getFullUrl(item.logo))"
              :initial-index="getImageIndex(scope.row)"
              fit="cover"
              style="width: 60px; height: 60px"
              preview-teleported
            />
            <div v-else style="width: 60px; height: 60px; display: flex; justify-content: center; align-items: center; background-color: #f5f7fa;">
              <el-icon style="font-size: 20px; color: #909399;"><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="IP名称" width="180" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 添加/编辑IP对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑IP' : '添加IP'"
      width="500px"
    >
      <el-form :model="ipForm" label-width="100px" :rules="rules" ref="ipFormRef">
        <el-form-item label="IP名称" prop="name">
          <el-input v-model="ipForm.name" placeholder="请输入IP名称" />
        </el-form-item>
        <el-form-item label="IP图标" prop="logo">
          <el-upload
            class="avatar-uploader"
            action="/api/admin/file/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleLogoSuccess"
            :before-upload="beforeLogoUpload"
            :data="{type: 'ip'}"
          >
            <el-image
              v-if="ipForm.logo"
              :src="ipForm.logo"
              class="avatar"
              :preview-src-list="[ipForm.logo]"
              preview-teleported
            />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="ipForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入IP描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ipForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'
import { getIpDetail, addIp, updateIp, deleteIp, updateIpStatus } from '../../api/ip'
import { getAllIps } from '../../api/admin-community'
import { UPLOAD_CONFIG } from '../../config'

// 状态
const loading = ref(false)
const allIpList = ref([]) // 所有IP列表
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)

// 分页后的IP列表
const ipList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return allIpList.value.slice(start, end)
})

// 表单引用
const ipFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// IP表单
const ipForm = reactive({
  id: null,
  name: '',
  logo: '',
  description: '',
  status: 1
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入IP名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  logo: [
    { required: true, message: '请上传IP图标', trigger: 'change' }
  ]
}

// 上传相关
const uploadHeaders = {
  Authorization: localStorage.getItem('adminToken') || ''
}

// 获取图片在预览列表中的索引
const getImageIndex = (row) => {
  if (!row.logo) return 0
  const items = allIpList.value.filter(item => item.logo)
  const currentFullUrl = UPLOAD_CONFIG.getFullUrl(row.logo)
  const index = items.findIndex(item => UPLOAD_CONFIG.getFullUrl(item.logo) === currentFullUrl)
  return index >= 0 ? index : 0
}

// 方法
function fetchIpList() {
  loading.value = true
  getAllIps().then(res => {
    // 直接使用返回的数组
    let filteredList = res.data || []

    // 过滤数据
    if (searchForm.name) {
      filteredList = filteredList.filter(item => item.name.includes(searchForm.name))
    }
    if (searchForm.status !== '') {
      filteredList = filteredList.filter(item => item.status === parseInt(searchForm.status))
    }

    // 处理图片URL
    filteredList.forEach(item => {
      if (item.logo && !item.logo.startsWith('http')) {
        // 不修改原始数据，只在前端显示时添加完整URL
        item._fullLogoUrl = UPLOAD_CONFIG.getFullUrl(item.logo)
      }
    })

    // 设置数据和总数
    allIpList.value = filteredList
    total.value = filteredList.length

    // 重置到第一页
    if (currentPage.value > 1 && ipList.value.length === 0) {
      currentPage.value = 1
    }
  }).catch(error => {
    console.error('获取IP列表失败:', error)
    ElMessage.error('获取IP列表失败')
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchIpList()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.status = ''
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  // 不需要重新获取数据，只需要更新页面显示
}

function handleCurrentChange(val) {
  currentPage.value = val
  // 不需要重新获取数据，只需要更新页面显示
}

function resetForm() {
  ipForm.id = null
  ipForm.name = ''
  ipForm.logo = ''
  ipForm.description = ''
  ipForm.status = 1

  if (ipFormRef.value) {
    ipFormRef.value.resetFields()
  }
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  isEdit.value = true

  getIpDetail(row.id).then(res => {
    const ipData = { ...res.data }

    // 确保logo URL是完整的
    if (ipData.logo && !ipData.logo.startsWith('http')) {
      ipData.logo = UPLOAD_CONFIG.getFullUrl(ipData.logo)
    }

    Object.assign(ipForm, ipData)
    dialogVisible.value = true
  })
}

function handleStatusChange(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  ElMessageBox.confirm(`确定要${statusText}IP "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    updateIpStatus(row.id, newStatus).then(() => {
      ElMessage.success(`${statusText}成功`)
      // 更新本地数据，避免重新请求
      row.status = newStatus
    }).catch(error => {
      console.error(`${statusText}失败:`, error)
      ElMessage.error(`${statusText}失败: ${error.response?.data?.msg || '未知错误'}`)
    })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除IP "${row.name}" 吗？删除后可能影响关联的商品和帖子。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteIp(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchIpList()
    }).catch(error => {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.msg || '删除失败，可能存在关联数据')
    })
  }).catch(() => {})
}

function handleLogoSuccess(res) {
  if (res.code === 200) {
    // 使用全局配置获取完整URL
    ipForm.logo = UPLOAD_CONFIG.getFullUrl(res.data)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function beforeLogoUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传图标只能是图片格式!')
    return false
  }

  if (!isLt2M) {
    ElMessage.error('上传图标大小不能超过 2MB!')
    return false
  }

  return true
}

function submitForm() {
  ipFormRef.value.validate(valid => {
    if (!valid) return

    // 创建一个新对象，避免修改原始表单数据
    const formData = { ...ipForm }

    // 如果logo是完整URL，提取相对路径
    if (formData.logo && formData.logo.includes('://')) {
      // 从完整URL中提取相对路径
      const apiUrlIndex = formData.logo.indexOf('/api')
      if (apiUrlIndex !== -1) {
        formData.logo = formData.logo.substring(apiUrlIndex + 4) // 去掉 '/api' 前缀
      }
    }

    if (isEdit.value) {
      updateIp(formData).then(() => {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchIpList()
      }).catch(error => {
        console.error('更新失败:', error)
        ElMessage.error(error.response?.data?.msg || '更新失败')
      })
    } else {
      addIp(formData).then(() => {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchIpList()
      }).catch(error => {
        console.error('添加失败:', error)
        ElMessage.error(error.response?.data?.msg || '添加失败')
      })
    }
  })
}

// 生命周期钩子
onMounted(() => {
  fetchIpList()
})
</script>

<style lang="scss" scoped>
.ip-list-container {
  padding: 20px;

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

  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);

      &:hover {
        border-color: var(--el-color-primary);
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      text-align: center;
      line-height: 100px;
    }

    .avatar {
      width: 100px;
      height: 100px;
      display: block;
      object-fit: cover;
    }

    :deep(.el-image) {
      width: 100px;
      height: 100px;
      display: block;
    }
  }
}
</style>
