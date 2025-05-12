<template>
  <div class="user-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="right">
            <el-button type="primary" @click="handleAdd">添加用户</el-button>
          </div>
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
          <el-form-item label="角色">
            <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
              <el-option label="普通用户" :value="0" />
              <el-option label="商家" :value="1" />
              <el-option label="管理员" :value="2" />
            </el-select>
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
        :data="userList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-avatar :src="getAvatarSrc(scope.row.avatar)" :size="40">
              <img src="../../assets/images/default-avatar.png" />
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 0" type="info">普通用户</el-tag>
            <el-tag v-else-if="scope.row.role === 1" type="success">商家</el-tag>
            <el-tag v-else-if="scope.row.role === 2" type="danger">管理员</el-tag>
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
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="primary" link @click="handleResetPassword(scope.row)">重置密码</el-button>
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

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="普通用户" :value="0" />
            <el-option label="商家" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="userForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="禁用"
          />
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
      width="400px"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordFormRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" show-password />
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
import { getUserList, addUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '../../api/user'
import defaultAvatar from '../../assets/images/default-avatar.png'

// 状态
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const resetPasswordDialogVisible = ref(false)
const currentUserId = ref(null)

// 表单引用
const userFormRef = ref(null)
const resetPasswordFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  username: '',
  nickname: '',
  role: '',
  status: ''
})

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  role: 0,
  status: 1
})

// 重置密码表单
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单校验规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 重置密码表单校验规则
const resetPasswordFormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
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
function fetchUserList() {
  loading.value = true
  getUserList({
    page: currentPage.value,
    size: pageSize.value,
    username: searchForm.username || undefined,
    nickname: searchForm.nickname || undefined,
    role: searchForm.role !== '' ? searchForm.role : undefined,
    status: searchForm.status !== '' ? searchForm.status : undefined
  }).then(res => {
    userList.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchUserList()
}

function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchUserList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchUserList()
}

function handleAdd() {
  dialogType.value = 'add'
  Object.keys(userForm).forEach(key => {
    userForm[key] = key === 'role' ? 0 : key === 'status' ? 1 : ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogType.value = 'edit'
  Object.keys(userForm).forEach(key => {
    if (key !== 'password') {
      userForm[key] = row[key]
    }
  })
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteUser(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchUserList()
    })
  }).catch(() => {})
}

function handleStatusChange(row) {
  updateUserStatus(row.id, row.status).then(() => {
    ElMessage.success(`用户状态已${row.status === 1 ? '启用' : '禁用'}`)
  }).catch(() => {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  })
}

function handleResetPassword(row) {
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  currentUserId.value = row.id
  resetPasswordDialogVisible.value = true
}

function submitForm() {
  userFormRef.value.validate(valid => {
    if (valid) {
      if (dialogType.value === 'add') {
        addUser(userForm).then(() => {
          ElMessage.success('添加成功')
          dialogVisible.value = false
          fetchUserList()
        })
      } else {
        updateUser(userForm).then(() => {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          fetchUserList()
        })
      }
    }
  })
}

function submitResetPassword() {
  resetPasswordFormRef.value.validate(valid => {
    if (valid) {
      resetUserPassword(currentUserId.value, resetPasswordForm.newPassword).then(() => {
        ElMessage.success('密码重置成功')
        resetPasswordDialogVisible.value = false
      })
    }
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
  fetchUserList()
})
</script>

<style lang="scss" scoped>
.user-list-container {
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
}
</style>
