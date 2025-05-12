<template>
  <div class="admin-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>管理员管理</span>
          <el-button type="primary" @click="handleAdd">添加管理员</el-button>
        </div>
      </template>
      
      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="adminList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'danger' : 'success'" 
              link 
              @click="handleStatusChange(scope.row)"
              :disabled="scope.row.id === 1"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="warning" link @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(scope.row)"
              :disabled="scope.row.id === 1"
            >
              删除
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
    
    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑管理员' : '添加管理员'"
      width="500px"
    >
      <el-form :model="adminForm" label-width="100px" :rules="rules" ref="adminFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="adminForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="adminForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
          <el-input v-model="adminForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="adminForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="adminForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="adminForm.status">
            <el-radio :label="1">正常</el-radio>
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
    
    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="500px"
    >
      <el-form :model="resetPasswordForm" label-width="100px" :rules="resetPasswordRules" ref="resetPasswordFormRef">
        <el-form-item label="用户名">{{ resetPasswordForm.username }}</el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPasswordForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPassword">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminList, addAdmin, updateAdmin, deleteAdmin, updateAdminStatus, resetPassword } from '../../api/admin'

// 状态
const loading = ref(false)
const adminList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const resetPasswordDialogVisible = ref(false)

// 表单引用
const adminFormRef = ref(null)
const resetPasswordFormRef = ref(null)

// 管理员表单
const adminForm = reactive({
  id: null,
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  status: 1
})

// 重置密码表单
const resetPasswordForm = reactive({
  id: null,
  username: '',
  password: '',
  confirmPassword: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== adminForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 重置密码校验规则
const resetPasswordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
function fetchAdminList() {
  loading.value = true
  getAdminList({
    page: currentPage.value,
    size: pageSize.value
  }).then(res => {
    adminList.value = res.data.records || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchAdminList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchAdminList()
}

function resetForm() {
  adminForm.id = null
  adminForm.username = ''
  adminForm.nickname = ''
  adminForm.password = ''
  adminForm.confirmPassword = ''
  adminForm.email = ''
  adminForm.phone = ''
  adminForm.status = 1
  
  if (adminFormRef.value) {
    adminFormRef.value.resetFields()
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
  
  adminForm.id = row.id
  adminForm.username = row.username
  adminForm.nickname = row.nickname
  adminForm.email = row.email
  adminForm.phone = row.phone
  adminForm.status = row.status
  
  dialogVisible.value = true
}

function handleStatusChange(row) {
  if (row.id === 1) {
    ElMessage.warning('不能修改超级管理员状态')
    return
  }
  
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${statusText}管理员 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    updateAdminStatus(row.id, newStatus).then(() => {
      ElMessage.success(`${statusText}成功`)
      row.status = newStatus
    })
  }).catch(() => {})
}

function handleDelete(row) {
  if (row.id === 1) {
    ElMessage.warning('不能删除超级管理员')
    return
  }
  
  ElMessageBox.confirm(`确定要删除管理员 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteAdmin(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchAdminList()
    })
  }).catch(() => {})
}

function handleResetPassword(row) {
  resetPasswordForm.id = row.id
  resetPasswordForm.username = row.username
  resetPasswordForm.password = ''
  resetPasswordForm.confirmPassword = ''
  
  resetPasswordDialogVisible.value = true
}

function submitForm() {
  adminFormRef.value.validate(valid => {
    if (!valid) return
    
    if (isEdit.value) {
      updateAdmin(adminForm).then(() => {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchAdminList()
      })
    } else {
      addAdmin(adminForm).then(() => {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchAdminList()
      })
    }
  })
}

function submitResetPassword() {
  resetPasswordFormRef.value.validate(valid => {
    if (!valid) return
    
    resetPassword(resetPasswordForm.id, resetPasswordForm.password).then(() => {
      ElMessage.success('密码重置成功')
      resetPasswordDialogVisible.value = false
    })
  })
}

// 生命周期钩子
onMounted(() => {
  fetchAdminList()
})
</script>

<style lang="scss" scoped>
.admin-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
