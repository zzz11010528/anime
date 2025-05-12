<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="../../assets/images/logo.jpg" alt="Logo" class="logo">
        <h2>动漫周边商城 - 管理员后台</h2>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password size="large">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin" size="large">登录</el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>© 2025 动漫周边商城 - 管理员后台</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAdminStore } from '../../stores/admin'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

// 状态
const loginFormRef = ref(null)
const loading = ref(false)

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 方法
function handleLogin() {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await adminStore.loginAction({
        username: loginForm.username,
        password: loginForm.password
      })

      ElMessage.success('登录成功')

      // 跳转到重定向页面或首页
      const redirect = route.query.redirect || '/'
      router.replace(redirect)
    } catch (error) {
      ElMessage.error(error.message || '登录失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 生命周期钩子
onMounted(() => {
  // 如果已经登录，直接跳转到首页
  if (adminStore.isLoggedIn) {
    router.replace('/')
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  background-size: cover;
  background-position: center;

  .login-box {
    width: 420px;
    padding: 40px;
    background-color: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
    }

    .login-header {
      text-align: center;
      margin-bottom: 40px;

      .logo {
        width: 100px;
        height: 100px;
        margin-bottom: 20px;
        border-radius: 50%;
        object-fit: cover;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        border: 3px solid #fff;
      }

      h2 {
        font-size: 24px;
        color: #333;
        font-weight: 600;
        margin: 0;
      }
    }

    .login-form {
      .el-form-item {
        margin-bottom: 25px;
      }

      .el-input {
        :deep(.el-input__wrapper) {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
          border-radius: 8px;
          padding: 0 15px;
        }

        :deep(.el-input__prefix) {
          margin-right: 10px;
          color: #409EFF;
        }
      }



      .login-button {
        width: 100%;
        height: 50px;
        font-size: 16px;
        font-weight: 500;
        border-radius: 8px;
        background: linear-gradient(to right, #409EFF, #007bff);
        border: none;
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 15px rgba(64, 158, 255, 0.5);
        }
      }
    }

    .login-footer {
      text-align: center;
      margin-top: 30px;
      font-size: 14px;
      color: #909399;
    }
  }
}

@media (max-width: 480px) {
  .login-container {
    .login-box {
      width: 90%;
      padding: 20px;
    }
  }
}
</style>
