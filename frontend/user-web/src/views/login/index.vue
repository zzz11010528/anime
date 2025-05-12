<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="login-banner">
          <h1 class="site-title">动漫社区</h1>
          <p class="site-slogan">发现、分享、交流你的动漫热爱</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><ShoppingCart /></el-icon>
              <span>海量动漫周边</span>
            </div>
            <div class="feature-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>活跃社区讨论</span>
            </div>
            <div class="feature-item">
              <el-icon><Star /></el-icon>
              <span>精选IP资源</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="login-box">
          <div class="login-header">
            <img src="../../assets/images/logo.jpg" alt="Logo" class="logo">
            <h2>欢迎回来</h2>
            <p class="subtitle">登录您的账号以继续</p>
          </div>

          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名/手机号/邮箱"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                show-password
                size="large"
                :prefix-icon="Lock"
              />
            </el-form-item>

            <div class="login-options">
              <el-button type="text" @click="forgotPassword" class="forgot-password">忘记密码？</el-button>
            </div>

            <el-form-item>
              <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin" size="large" round>登录</el-button>
            </el-form-item>

            <div class="register-link">
              还没有账号？<el-button type="text" @click="goToRegister" class="register-btn">立即注册</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ShoppingCart, ChatDotRound, Star } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 状态
const loginFormRef = ref(null)
const loginForm = reactive({
  username: '',
  password: ''
})
const rememberMe = ref(false)
const loading = ref(false)

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
      await userStore.loginAction(loginForm.username, loginForm.password)
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

function goToRegister() {
  router.push('/register')
}

function forgotPassword() {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  overflow: hidden;

  .login-content {
    display: flex;
    width: 900px;
    height: 600px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);

    .login-left {
      flex: 1;
      background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: -50%;
        left: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 60%);
        z-index: 1;
      }

      .login-banner {
        position: relative;
        z-index: 2;
        text-align: center;

        .site-title {
          font-size: 36px;
          font-weight: 700;
          margin-bottom: 16px;
          text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
        }

        .site-slogan {
          font-size: 18px;
          margin-bottom: 40px;
          opacity: 0.9;
        }

        .features {
          display: flex;
          flex-direction: column;
          gap: 20px;

          .feature-item {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 16px;

            .el-icon {
              font-size: 24px;
              background: rgba(255, 255, 255, 0.2);
              border-radius: 50%;
              padding: 8px;
              display: flex;
            }
          }
        }
      }
    }

    .login-right {
      flex: 1;
      background-color: white;
      display: flex;
      align-items: center;
      justify-content: center;

      .login-box {
        width: 80%;
        max-width: 360px;

        .login-header {
          text-align: center;
          margin-bottom: 30px;

          .logo {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 50%;
            margin-bottom: 20px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
          }

          h2 {
            font-size: 28px;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
          }

          .subtitle {
            font-size: 16px;
            color: #666;
          }
        }

        .login-form {
          .login-options {
            display: flex;
            justify-content: flex-end;
            margin-bottom: 20px;

            .forgot-password {
              color: #666;
              font-size: 14px;

              &:hover {
                color: #409EFF;
              }
            }
          }

          .login-button {
            width: 100%;
            height: 50px;
            font-size: 16px;
            background: linear-gradient(to right, #6a11cb, #2575fc);
            border: none;

            &:hover {
              background: linear-gradient(to right, #5a0fc0, #1e68e6);
            }
          }

          .register-link {
            text-align: center;
            margin-top: 20px;
            font-size: 14px;
            color: #666;

            .register-btn {
              color: #6a11cb;
              font-weight: 600;

              &:hover {
                color: #2575fc;
              }
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .login-container {
    .login-content {
      width: 90%;
      height: auto;
      flex-direction: column;

      .login-left {
        display: none;
      }

      .login-right {
        padding: 40px 20px;

        .login-box {
          width: 100%;
        }
      }
    }
  }
}
</style>
