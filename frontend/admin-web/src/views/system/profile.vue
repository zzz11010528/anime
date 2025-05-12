<template>
  <div class="profile-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>

      <div class="profile-content">
        <div class="avatar-container">
          <el-avatar :src="avatarSrc" :size="100"></el-avatar>
          <el-upload
            class="avatar-uploader"
            action="/api/file/upload/avatar"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="rules"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input v-model="adminInfo.username" disabled></el-input>
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称"></el-input>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱"></el-input>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号"></el-input>
          </el-form-item>

          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
              <el-radio :label="0">保密</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="profileForm.bio"
              type="textarea"
              :rows="3"
              placeholder="请输入个人简介"
            ></el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="loading">保存</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '../../stores/admin'
import { updateAdminInfo } from '../../api/admin'
import defaultAvatar from '../../assets/images/default-avatar.png'

const adminStore = useAdminStore()
const adminInfo = computed(() => adminStore.adminInfo)

// 计算属性
const avatarSrc = computed(() => {
  if (!adminInfo.value.avatar) return defaultAvatar

  // 确保头像URL包含/api前缀
  if (adminInfo.value.avatar.startsWith('http')) {
    return adminInfo.value.avatar
  } else if (adminInfo.value.avatar.startsWith('/api')) {
    return adminInfo.value.avatar
  } else {
    return `/api${adminInfo.value.avatar}`
  }
})

// 状态
const profileFormRef = ref(null)
const loading = ref(false)

// 表单数据
const profileForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  bio: ''
})

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  bio: [
    { max: 500, message: '个人简介不能超过500个字符', trigger: 'blur' }
  ]
}

// 上传相关
const uploadHeaders = {
  Authorization: localStorage.getItem('adminToken') || ''
}

// 方法
function initForm() {
  profileForm.nickname = adminInfo.value.nickname || ''
  profileForm.email = adminInfo.value.email || ''
  profileForm.phone = adminInfo.value.phone || ''
  profileForm.gender = adminInfo.value.gender || 0
  profileForm.bio = adminInfo.value.bio || ''
}

function submitForm() {
  profileFormRef.value.validate(valid => {
    if (!valid) return

    loading.value = true
    updateAdminInfo(profileForm)
      .then(() => {
        ElMessage.success('个人信息更新成功')
        adminStore.getAdminInfo() // 刷新管理员信息
      })
      .catch(error => {
        console.error('更新个人信息失败:', error)
        ElMessage.error(error.response?.data?.msg || '更新失败，请重试')
      })
      .finally(() => {
        loading.value = false
      })
  })
}

function resetForm() {
  profileFormRef.value.resetFields()
  initForm()
}

function handleAvatarSuccess(res) {
  if (res.code === 200) {
    // 确保头像URL包含/api前缀
    let avatarUrl = res.data
    if (avatarUrl && !avatarUrl.startsWith('/api') && !avatarUrl.startsWith('http')) {
      avatarUrl = `/api${avatarUrl}`
    }

    updateAdminInfo({ avatar: avatarUrl })
      .then(() => {
        adminStore.getAdminInfo() // 刷新管理员信息
        ElMessage.success('头像更新成功')
      })
      .catch(error => {
        console.error('更新头像失败:', error)
        ElMessage.error('更新头像失败，请重试')
      })
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
    return false
  }

  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }

  return true
}



// 生命周期钩子
onMounted(() => {
  initForm()
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;

  .profile-content {
    display: flex;
    flex-direction: column;

    .avatar-container {
      margin-bottom: 20px;
      text-align: center;

      .el-avatar {
        margin-bottom: 10px;
      }
    }

    .profile-form {
      width: 100%;
      max-width: 500px;
      margin: 0 auto;
    }
  }
}
</style>
