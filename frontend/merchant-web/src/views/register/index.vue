<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <img src="../../assets/images/logo.jpg" alt="Logo" class="logo">
        <h2>商家注册</h2>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
        class="register-form"
      >
        <div class="form-container">
          <!-- 左侧：基本信息 -->
          <div class="form-column">
            <div class="column-title">基本账户信息</div>

            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="registerForm.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </div>

          <!-- 右侧：商家认证信息 -->
          <div class="form-column">
            <div class="column-title">商家认证信息</div>

            <el-form-item label="公司名称" prop="companyName">
              <el-input v-model="registerForm.companyName" placeholder="请输入公司名称" />
            </el-form-item>

            <el-form-item label="联系人姓名" prop="contactName">
              <el-input v-model="registerForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>

            <el-form-item label="联系人电话" prop="contactPhone">
              <el-input v-model="registerForm.contactPhone" placeholder="请输入联系人电话" />
            </el-form-item>

            <el-form-item label="联系人邮箱" prop="contactEmail">
              <el-input v-model="registerForm.contactEmail" placeholder="请输入联系人邮箱" />
            </el-form-item>

            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="registerForm.bio"
                type="textarea"
                :rows="4"
                placeholder="请输入个人简介"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 底部协议和按钮 -->
        <div class="form-footer">
          <el-form-item label="协议" prop="agreement">
            <el-checkbox v-model="registerForm.agreement">
              我已阅读并同意《商家入驻协议》
            </el-checkbox>
          </el-form-item>

          <el-form-item class="form-buttons">
            <el-button type="primary" @click="submitRegister" :loading="submitting" class="register-button">注册</el-button>
            <el-button @click="goToLogin">返回登录</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/user'

const router = useRouter()

// 状态
const registerFormRef = ref(null)
const submitting = ref(false)

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: '',
  companyName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  bio: '',
  agreement: false
})

// 表单验证规则
const validatePass = (_, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (_, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  nickname: [
    { required: false, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  companyName: [
    { required: true, message: '请输入公司名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  contactEmail: [
    { required: false, message: '请输入联系人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  bio: [
    { required: false, message: '请输入个人简介', trigger: 'blur' },
    { max: 500, message: '长度不能超过500个字符', trigger: 'blur' }
  ],
  agreement: [
    {
      required: true,
      message: '请阅读并同意协议',
      trigger: 'change',
      validator: (_, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意协议'))
        } else {
          callback()
        }
      }
    }
  ]
}

// 返回登录页
function goToLogin() {
  router.push('/login')
}

// 提交注册
async function submitRegister() {
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      console.log('开始注册...')

      // 构建用户注册数据
      const userData = {
        username: registerForm.username,
        password: registerForm.password,
        nickname: registerForm.nickname || registerForm.username,
        phone: registerForm.phone,
        email: registerForm.email,
        bio: registerForm.bio
        // 不设置角色，使用后端默认值（普通用户）
      }

      console.log('注册数据:', userData)

      try {
        // 注册用户
        const response = await register(userData)
        console.log('注册响应:', response)
      } catch (error) {
        console.error('注册请求错误:', error)
        throw error
      }

      // 构建商家认证数据
      const certificationData = {
        companyName: registerForm.companyName,
        contactName: registerForm.contactName,
        contactPhone: registerForm.contactPhone,
        contactEmail: registerForm.contactEmail || registerForm.email
      }

      // 保存认证信息到本地存储，登录后提交认证申请
      localStorage.setItem('certificationData', JSON.stringify(certificationData))

      ElMessage({
        message: '注册成功，请登录后在店铺设置中完成商家认证',
        type: 'success',
        duration: 5000
      })

      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage({
        message: error.message || '注册失败，请重试',
        type: 'error',
        duration: 5000
      })
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  padding: 40px 0;

  .register-box {
    width: 900px;
    padding: 40px;
    background-color: rgba(255, 255, 255, 0.98);
    border-radius: 16px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 15px 50px rgba(0, 0, 0, 0.25);
    }

    .register-header {
      text-align: center;
      margin-bottom: 30px;

      .logo {
        width: 100px;
        height: 100px;
        margin-bottom: 20px;
        border-radius: 50%;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
        object-fit: cover;
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

    .register-form {
      width: 100%;

      .form-container {
        display: flex;
        gap: 30px;
        margin-bottom: 20px;

        .form-column {
          flex: 1;
          padding: 20px;
          background-color: #f9fafc;
          border-radius: 12px;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

          .column-title {
            font-size: 18px;
            font-weight: 600;
            color: #333;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #e6e8eb;
          }
        }
      }

      .form-footer {
        margin-top: 20px;
        padding: 20px;
        background-color: #f9fafc;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

        .form-buttons {
          margin-bottom: 0;
          display: flex;
          justify-content: center;
        }
      }

      :deep(.el-form-item) {
        margin-bottom: 20px;
      }

      :deep(.el-form-item__label) {
        font-weight: 500;
        color: #333;
        padding-right: 15px;
      }

      :deep(.el-input__wrapper) {
        padding: 12px 15px;
        box-shadow: 0 0 0 1px #dcdfe6 inset;
        transition: all 0.3s ease;
        width: 100%;
        border-radius: 8px;

        &:hover {
          box-shadow: 0 0 0 1px #c0c4cc inset;
        }

        &.is-focus {
          box-shadow: 0 0 0 1px #409eff inset;
        }
      }

      :deep(.el-textarea__inner) {
        padding: 12px 15px;
        min-height: 80px;
        border-radius: 8px;
      }

      :deep(.el-form-item__content) {
        display: flex;
        justify-content: flex-start;
        flex-wrap: wrap;
        width: 100%;
      }

      :deep(.el-input) {
        width: 100%;
      }

      .register-button {
        width: 140px;
        padding: 12px 15px;
        font-size: 16px;
        border-radius: 8px;
        background: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);
        border: none;
        transition: all 0.3s ease;
        margin-right: 15px;
        font-weight: 500;

        &:hover {
          opacity: 0.9;
          transform: translateY(-2px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
}

@media (max-width: 992px) {
  .register-container {
    padding: 20px;

    .register-box {
      width: 95%;
      padding: 30px 20px;

      .register-form {
        .form-container {
          flex-direction: column;
          gap: 20px;
        }
      }
    }
  }
}
</style>
