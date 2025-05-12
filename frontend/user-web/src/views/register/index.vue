<template>
  <div class="register-container">
    <div class="register-content">
      <div class="register-left">
        <div class="register-banner">
          <img src="../../assets/images/logo.jpg" alt="Logo" class="logo">
          <h1 class="site-title">动漫社区</h1>
          <p class="site-slogan">加入我们，探索动漫的无限可能</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><ShoppingCart /></el-icon>
              <span>购买精美周边</span>
            </div>
            <div class="feature-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>参与社区讨论</span>
            </div>
            <div class="feature-item">
              <el-icon><Star /></el-icon>
              <span>收藏喜爱作品</span>
            </div>
          </div>
          <div class="login-link">
            已有账号？<el-button type="text" @click="goToLogin" class="login-btn">立即登录</el-button>
          </div>
        </div>
      </div>

      <div class="register-right">
        <div class="register-box">
          <h2 class="register-title">创建账号</h2>

          <el-steps :active="active" finish-status="success" class="register-steps">
            <el-step title="账号信息" />
            <el-step title="个人资料" />
            <el-step title="完成注册" />
          </el-steps>

          <!-- 步骤一：填写账号信息 -->
          <el-form
            v-if="active === 0"
            ref="accountFormRef"
            :model="registerForm"
            :rules="accountRules"
            label-position="top"
            class="register-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                size="large"
                :prefix-icon="Lock"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                show-password
                size="large"
                :prefix-icon="Lock"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="nextStep" size="large" round class="next-button">下一步</el-button>
            </el-form-item>
          </el-form>

          <!-- 步骤二：设置个人资料 -->
          <el-form
            v-if="active === 1"
            ref="profileFormRef"
            :model="registerForm"
            :rules="profileRules"
            label-position="top"
            class="register-form"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="请输入昵称"
                size="large"
                :prefix-icon="UserFilled"
              />
            </el-form-item>

            <el-form-item label="性别">
              <el-radio-group v-model="registerForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action="/api/file/upload/avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :headers="uploadHeaders"
              >
                <img v-if="registerForm.avatar" :src="formatImageUrl(registerForm.avatar)" class="avatar" />
                <div v-else class="avatar-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传头像</span>
                </div>
              </el-upload>
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input
                v-model="registerForm.bio"
                type="textarea"
                placeholder="请输入个人简介"
                :rows="4"
              />
            </el-form-item>

            <div class="form-actions">
              <el-button @click="active = 0" size="large" class="prev-button">上一步</el-button>
              <el-button type="primary" @click="submitRegister" :loading="loading" size="large" round class="submit-button">注册</el-button>
            </div>
          </el-form>

          <!-- 步骤三：注册成功 -->
          <div v-if="active === 2" class="success-step">
            <el-result
              icon="success"
              title="注册成功"
              sub-title="您已成功注册动漫社区账号，现在可以开始您的动漫之旅了！"
            >
              <template #extra>
                <el-button type="primary" @click="goToLogin" size="large" round>立即登录</el-button>
                <el-button @click="goToHome" size="large">返回首页</el-button>
              </template>
            </el-result>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, User, UserFilled, Lock, Phone, Message, ShoppingCart, ChatDotRound, Star } from '@element-plus/icons-vue'
import { register } from '../../api/user'
import { formatImageUrl } from '../../utils/image'

const router = useRouter()

// 状态
const active = ref(0)
const accountFormRef = ref(null)
const profileFormRef = ref(null)
const loading = ref(false)
const uploadHeaders = {
  // 如果需要认证头部，可以在这里添加
}

const registerForm = reactive({
  username: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  gender: 0,
  avatar: '',
  bio: ''
})

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      profileFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const accountRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 方法
function nextStep() {
  accountFormRef.value.validate((valid) => {
    if (valid) {
      active.value = 1
    }
  })
}

async function submitRegister() {
  profileFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // 构建注册数据
      const registerData = {
        username: registerForm.username,
        password: registerForm.password,
        phone: registerForm.phone,
        email: registerForm.email,
        nickname: registerForm.nickname,
        gender: registerForm.gender,
        avatar: registerForm.avatar,
        bio: registerForm.bio
      }

      const res = await register(registerData)
      if (res.code === 200) {
        ElMessage.success('注册成功')
        active.value = 2
      } else {
        ElMessage.error(res.message || '注册失败，请重试')
      }
    } catch (error) {
      console.error('注册错误:', error)
      // 尝试从错误对象中提取详细信息
      let errorMsg = '注册失败，请重试'
      if (error.response && error.response.data) {
        errorMsg = error.response.data.message || errorMsg
      } else if (error.message) {
        errorMsg = error.message
      }
      ElMessage.error(errorMsg)
    } finally {
      loading.value = false
    }
  })
}

function goToLogin() {
  router.push('/login')
}

function goToHome() {
  router.push('/')
}

function handleAvatarSuccess(res) {
  // 确保头像URL包含API前缀
  if (res.data && res.code === 200) {
    registerForm.avatar = res.data
    console.log('头像上传成功:', registerForm.avatar)
  } else {
    ElMessage.error(res.message || '头像上传失败')
  }
}

function beforeAvatarUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return (isJPG || isPNG) && isLt2M
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  padding: 40px 0;

  .register-content {
    display: flex;
    width: 1000px;
    min-height: 700px;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);

    .register-left {
      width: 40%;
      background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: url('../../assets/images/pattern.png');
        opacity: 0.1;
      }

      .register-banner {
        position: relative;
        z-index: 2;
        text-align: center;

        .logo {
          width: 100px;
          height: 100px;
          object-fit: cover;
          border-radius: 50%;
          margin-bottom: 20px;
          border: 4px solid rgba(255, 255, 255, 0.2);
        }

        .site-title {
          font-size: 32px;
          font-weight: 700;
          margin-bottom: 16px;
          text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
        }

        .site-slogan {
          font-size: 16px;
          margin-bottom: 40px;
          opacity: 0.9;
        }

        .features {
          display: flex;
          flex-direction: column;
          gap: 20px;
          margin-bottom: 40px;

          .feature-item {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 16px;

            .el-icon {
              font-size: 20px;
              background: rgba(255, 255, 255, 0.2);
              border-radius: 50%;
              padding: 8px;
              display: flex;
            }
          }
        }

        .login-link {
          font-size: 14px;

          .login-btn {
            color: white;
            font-weight: 600;
            text-decoration: underline;

            &:hover {
              opacity: 0.8;
            }
          }
        }
      }
    }

    .register-right {
      width: 60%;
      background-color: white;
      padding: 40px;
      overflow-y: auto;

      .register-box {
        max-width: 500px;
        margin: 0 auto;

        .register-title {
          font-size: 28px;
          font-weight: 600;
          color: #333;
          margin-bottom: 30px;
          text-align: center;
        }

        .register-steps {
          margin-bottom: 30px;
        }

        .register-form {
          .avatar-uploader {
            display: flex;
            justify-content: center;
            margin-bottom: 20px;

            .avatar {
              width: 120px;
              height: 120px;
              border-radius: 50%;
              object-fit: cover;
              box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
            }

            .avatar-placeholder {
              width: 120px;
              height: 120px;
              border-radius: 50%;
              background-color: #f5f7fa;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              cursor: pointer;
              transition: all 0.3s;
              border: 2px dashed #dcdfe6;

              .el-icon {
                font-size: 24px;
                color: #909399;
                margin-bottom: 8px;
              }

              span {
                font-size: 14px;
                color: #909399;
              }

              &:hover {
                border-color: #409EFF;
                background-color: #ecf5ff;

                .el-icon, span {
                  color: #409EFF;
                }
              }
            }
          }

          .form-actions {
            display: flex;
            justify-content: space-between;
            margin-top: 20px;

            .prev-button, .submit-button {
              width: 48%;
            }

            .submit-button {
              background: linear-gradient(to right, #6a11cb, #2575fc);
              border: none;

              &:hover {
                background: linear-gradient(to right, #5a0fc0, #1e68e6);
              }
            }
          }

          .next-button {
            width: 100%;
            margin-top: 20px;
            background: linear-gradient(to right, #6a11cb, #2575fc);
            border: none;

            &:hover {
              background: linear-gradient(to right, #5a0fc0, #1e68e6);
            }
          }
        }

        .success-step {
          padding: 40px 0;
        }
      }
    }
  }
}

@media (max-width: 992px) {
  .register-container {
    .register-content {
      width: 90%;
      flex-direction: column;

      .register-left {
        width: 100%;
        padding: 30px;
      }

      .register-right {
        width: 100%;
      }
    }
  }
}

@media (max-width: 576px) {
  .register-container {
    padding: 20px 0;

    .register-content {
      width: 95%;
      border-radius: 10px;

      .register-left {
        padding: 20px;

        .register-banner {
          .logo {
            width: 80px;
            height: 80px;
          }

          .site-title {
            font-size: 24px;
          }

          .site-slogan {
            font-size: 14px;
            margin-bottom: 20px;
          }

          .features {
            gap: 10px;
            margin-bottom: 20px;

            .feature-item {
              font-size: 14px;

              .el-icon {
                font-size: 16px;
                padding: 6px;
              }
            }
          }
        }
      }

      .register-right {
        padding: 20px;

        .register-box {
          .register-title {
            font-size: 24px;
            margin-bottom: 20px;
          }

          .register-form {
            .form-actions {
              flex-direction: column;
              gap: 10px;

              .prev-button, .submit-button {
                width: 100%;
              }
            }
          }
        }
      }
    }
  }
}</style>
