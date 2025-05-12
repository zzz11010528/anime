<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="../../assets/images/logo.jpg" alt="Logo" class="logo">
        <h2>商家管理中心</h2>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        还没有商家账号？<el-button type="text" @click="goToRegister">立即注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useMerchantStore } from '../../stores/merchant'

const router = useRouter()
const route = useRoute()
const merchantStore = useMerchantStore()

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

// 登录方法
function handleLogin() {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // 登录并获取用户信息
      await merchantStore.loginAction({
        username: loginForm.username,
        password: loginForm.password
      })

      ElMessage.success('登录成功')

      // 检查认证状态
      const merchantInfo = merchantStore.merchantInfo

      // 如果是商家且认证状态为审核中或被拒绝，跳转到认证页面
      if (merchantInfo.role === 1) {
        if (merchantInfo.certificationStatus === 0) {
          ElMessage.warning('您的商家认证正在审核中，请耐心等待')
          setTimeout(() => {
            router.replace('/profile?tab=certification')
          }, 1000)
          return
        } else if (merchantInfo.certificationStatus === 2) {
          ElMessage.error('您的商家认证被拒绝，请重新提交认证申请')
          setTimeout(() => {
            router.replace('/profile?tab=certification')
          }, 1000)
          return
        }
      }

      // 如果是普通用户，需要引导去认证
      if (merchantInfo.role === 0) {
        ElMessage.info('您需要完成商家认证才能使用商家功能')
        setTimeout(() => {
          router.replace('/profile?tab=certification')
        }, 1000)
        return
      }

      // 检查是否有保存的认证数据
      const certificationData = localStorage.getItem('certificationData')
      if (certificationData) {
        ElMessage.info('检测到您有未提交的商家认证信息，即将跳转到认证页面')
        // 跳转到认证页面
        setTimeout(() => {
          router.replace('/profile?tab=certification')
        }, 1000)
        // 清除本地存储的认证数据，避免重复提示
        localStorage.removeItem('certificationData')
      } else {
        // 跳转到重定向页面或首页
        const redirect = route.query.redirect || '/'
        router.replace(redirect)
      }
    } catch (error) {
      ElMessage.error(error.message || '登录失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 跳转到注册页面
function goToRegister() {
  router.push('/register')
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;

  .login-box {
    width: 450px;
    padding: 50px;
    background-color: rgba(255, 255, 255, 0.98);
    border-radius: 16px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: -50px;
      right: -50px;
      width: 150px;
      height: 150px;
      background: linear-gradient(135deg, rgba(79, 172, 254, 0.1), rgba(0, 242, 254, 0.1));
      border-radius: 50%;
      z-index: 0;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: -80px;
      left: -80px;
      width: 200px;
      height: 200px;
      background: linear-gradient(135deg, rgba(118, 75, 162, 0.1), rgba(102, 126, 234, 0.1));
      border-radius: 50%;
      z-index: 0;
    }

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 15px 50px rgba(0, 0, 0, 0.25);
    }

    .login-header {
      text-align: center;
      margin-bottom: 40px;
      position: relative;
      z-index: 1;

      .logo {
        width: 100px;
        height: 100px;
        margin-bottom: 20px;
        border-radius: 50%;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
        object-fit: cover;
        border: 4px solid rgba(255, 255, 255, 0.8);
      }

      h2 {
        font-size: 32px;
        color: #333;
        font-weight: 600;
        margin: 0;
        background: linear-gradient(to right, #4facfe, #00f2fe);
        -webkit-background-clip: text;
        background-clip: text;
        -webkit-text-fill-color: transparent;
        color: transparent;
      }
    }

    .login-form {
      margin-bottom: 20px;
      position: relative;
      z-index: 1;

      :deep(.el-form-item) {
        margin-bottom: 25px;
      }

      :deep(.el-input__wrapper) {
        padding: 14px 16px;
        box-shadow: 0 0 0 1px #dcdfe6 inset;
        transition: all 0.3s ease;
        border-radius: 8px;

        &:hover {
          box-shadow: 0 0 0 1px #c0c4cc inset;
        }

        &.is-focus {
          box-shadow: 0 0 0 1px #409eff inset;
        }
      }

      :deep(.el-input__prefix) {
        margin-right: 12px;

        .el-icon {
          font-size: 18px;
          color: #909399;
        }
      }

      .login-button {
        width: 100%;
        padding: 14px 16px;
        font-size: 16px;
        margin-top: 15px;
        border-radius: 8px;
        background: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);
        border: none;
        transition: all 0.3s ease;
        font-weight: 500;

        &:hover {
          opacity: 0.9;
          transform: translateY(-2px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
        }
      }
    }

    .register-link {
      text-align: center;
      margin-top: 30px;
      font-size: 15px;
      color: #666;
      position: relative;
      z-index: 1;

      .el-button {
        padding: 0 4px;
        font-size: 15px;
        font-weight: 500;
        background: linear-gradient(to right, #4facfe, #00f2fe);
        -webkit-background-clip: text;
        background-clip: text;
        -webkit-text-fill-color: transparent;
        color: transparent;

        &:hover {
          opacity: 0.8;
          transform: translateY(-1px);
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .login-container {
    .login-box {
      width: 90%;
      padding: 40px 25px;
    }
  }
}
</style>
