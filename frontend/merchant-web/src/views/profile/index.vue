<template>
  <div class="profile-container">
    <div class="profile-layout">
      <!-- 左侧导航 -->
      <div class="profile-sidebar">
        <el-menu
          :default-active="activeTab"
          class="profile-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="basic">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
          <el-menu-item index="certification">
            <el-icon><Medal /></el-icon>
            <span>商家认证</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <h2>
                {{
                  activeTab === 'basic' ? '个人信息' :
                  activeTab === 'password' ? '修改密码' :
                  '商家认证'
                }}
              </h2>
            </div>
          </template>

          <!-- 个人信息 -->
          <div v-if="activeTab === 'basic'">
            <el-form
              ref="basicFormRef"
              :model="basicForm"
              :rules="basicRules"
              label-width="120px"
              class="profile-form"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="basicForm.username" disabled />
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="basicForm.nickname" placeholder="请输入昵称" />
              </el-form-item>

              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  action="/api/file/upload/avatar"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <img v-if="basicForm.avatar" :src="formatImageUrl(basicForm.avatar)" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="basicForm.email" placeholder="请输入邮箱" />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="basicForm.phone" placeholder="请输入手机号" />
              </el-form-item>

              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="basicForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                  <el-radio :label="0">保密</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="个人简介" prop="bio">
                <el-input
                  v-model="basicForm.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入个人简介"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updateBasicInfo" :loading="submitting">保存修改</el-button>
                <el-button @click="resetBasicForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 修改密码 -->
          <div v-if="activeTab === 'password'">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="120px"
              class="profile-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入原密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updatePassword" :loading="submitting">修改密码</el-button>
                <el-button @click="resetPasswordForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 商家认证 -->
          <div v-if="activeTab === 'certification'">
            <!-- 认证状态展示 -->
            <div v-if="certificationInfo" class="certification-status">
              <el-alert
                v-if="certificationInfo.certificationStatus === 0"
                title="您的认证申请正在审核中，请耐心等待"
                type="warning"
                :closable="false"
                show-icon
              />
              <el-alert
                v-else-if="certificationInfo.certificationStatus === 1"
                title="恭喜您，认证已通过！您可以开始使用商家功能"
                type="success"
                :closable="false"
                show-icon
              />
              <el-alert
                v-else-if="certificationInfo.certificationStatus === 2"
                title="很抱歉，您的认证申请被拒绝"
                type="error"
                :closable="false"
                show-icon
              >
                <div class="reject-reason">
                  拒绝原因：{{ certificationInfo.rejectReason || '未提供拒绝原因' }}
                </div>
              </el-alert>

              <div class="certification-info" v-if="certificationInfo.certificationStatus !== null">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="公司名称">{{ certificationInfo.companyName }}</el-descriptions-item>
                  <el-descriptions-item label="联系人">{{ certificationInfo.contactName }}</el-descriptions-item>
                  <el-descriptions-item label="联系电话">{{ certificationInfo.contactPhone }}</el-descriptions-item>
                  <el-descriptions-item label="联系邮箱">{{ certificationInfo.contactEmail }}</el-descriptions-item>
                  <el-descriptions-item label="申请时间">{{ certificationInfo.createdAt }}</el-descriptions-item>
                  <el-descriptions-item label="认证状态">
                    <el-tag v-if="certificationInfo.certificationStatus === 0" type="warning">审核中</el-tag>
                    <el-tag v-else-if="certificationInfo.certificationStatus === 1" type="success">已通过</el-tag>
                    <el-tag v-else-if="certificationInfo.certificationStatus === 2" type="danger">已拒绝</el-tag>
                  </el-descriptions-item>
                </el-descriptions>

                <div class="certification-images">
                  <div class="image-item">
                    <div class="image-title">营业执照</div>
                    <el-image
                      :src="formatImageUrl(certificationInfo.businessLicense)"
                      :preview-src-list="[formatImageUrl(certificationInfo.businessLicense)]"
                      fit="cover"
                      style="width: 200px; height: 200px"
                    />
                  </div>
                </div>

                <div class="action-buttons" v-if="certificationInfo.certificationStatus === 2">
                  <el-button type="primary" @click="showApplyForm">重新申请</el-button>
                </div>
              </div>
            </div>

            <!-- 认证申请表单 -->
            <div v-if="!certificationInfo || (certificationInfo.certificationStatus === 2 && showForm)" class="certification-form">
              <el-form
                ref="certificationFormRef"
                :model="certificationForm"
                :rules="certificationRules"
                label-width="120px"
              >
                <el-form-item label="公司名称" prop="companyName">
                  <el-input v-model="certificationForm.companyName" placeholder="请输入公司名称" />
                </el-form-item>

                <el-form-item label="联系人" prop="contactName">
                  <el-input v-model="certificationForm.contactName" placeholder="请输入联系人姓名" />
                </el-form-item>

                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="certificationForm.contactPhone" placeholder="请输入联系电话" />
                </el-form-item>

                <el-form-item label="联系邮箱" prop="contactEmail">
                  <el-input v-model="certificationForm.contactEmail" placeholder="请输入联系邮箱" />
                </el-form-item>

                <el-form-item label="营业执照" prop="businessLicense">
                  <el-upload
                    class="upload-container"
                    action="/api/file/upload/post"
                    :headers="uploadHeaders"
                    :on-success="handleBusinessLicenseSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    :limit="1"
                    :file-list="businessLicenseList"
                  >
                    <el-button type="primary">上传营业执照</el-button>
                    <template #tip>
                      <div class="el-upload__tip">请上传清晰的营业执照照片，JPG/PNG格式，大小不超过5MB</div>
                    </template>
                  </el-upload>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="submitCertification">提交认证</el-button>
                  <el-button @click="resetCertificationForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Lock, Medal } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, updatePassword as updatePwd } from '../../api/user'
import { getCertificationInfo, applyCertification } from '../../api/certification'
import { getUserCertificationInfo, applyUserCertification } from '../../api/userCertification'
import { useMerchantStore } from '../../stores/merchant'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()
const merchantStore = useMerchantStore()

// 状态
const activeTab = ref('basic')
const basicFormRef = ref(null)
const passwordFormRef = ref(null)
const certificationFormRef = ref(null)
const submitting = ref(false)
const certificationInfo = ref(null)
const showForm = ref(false)
const businessLicenseList = ref([])

// 表单数据
const basicForm = reactive({
  username: '',
  nickname: '',
  avatar: '',
  email: '',
  phone: '',
  gender: 0,
  bio: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const certificationForm = reactive({
  companyName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  businessLicense: ''
  // 移除了不在数据库中的字段
})

// 表单验证规则
const basicRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  bio: [
    { max: 500, message: '长度不能超过 500 个字符', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const certificationRules = {
  companyName: [
    { required: true, message: '请输入公司名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { required: true, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  businessLicense: [
    { required: true, message: '请上传营业执照', trigger: 'change' }
  ]
}

// 计算属性
const uploadHeaders = computed(() => {
  return {
    'satoken': localStorage.getItem('token')
  }
})

// 方法
// 菜单选择
function handleMenuSelect(key) {
  activeTab.value = key
}

// 获取用户信息
async function fetchUserInfo() {
  try {
    const res = await getUserInfo()
    const userInfo = res.data

    // 填充基本信息表单
    basicForm.username = userInfo.username
    basicForm.nickname = userInfo.nickname || ''
    basicForm.avatar = userInfo.avatar || ''
    basicForm.email = userInfo.email || ''
    basicForm.phone = userInfo.phone || ''
    basicForm.gender = userInfo.gender || 0
    basicForm.bio = userInfo.bio || ''

    // 更新 store 中的用户信息
    merchantStore.setMerchantInfo(userInfo)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 获取认证信息
async function fetchCertificationInfo() {
  // 检查用户角色
  const userRole = merchantStore.merchantInfo.role

  try {
    let res;

    if (userRole === 1) {
      // 如果是商家角色，使用商家认证接口
      res = await getCertificationInfo();
    } else {
      // 如果是普通用户，使用普通用户认证接口
      try {
        res = await getUserCertificationInfo();
      } catch (error) {
        // 如果普通用户接口失败，直接显示申请表单
        console.log('普通用户获取认证信息失败，显示申请表单');
        certificationInfo.value = null;
        showForm.value = true;
        return;
      }
    }

    certificationInfo.value = res.data;

    // 如果已经有认证信息
    if (certificationInfo.value) {
      // 更新商家信息中的认证状态
      merchantStore.setMerchantInfo({
        certificationStatus: certificationInfo.value.certificationStatus
      });

      console.log('更新认证状态:', certificationInfo.value.certificationStatus);
      console.log('当前商家信息:', merchantStore.merchantInfo);

      // 如果是被拒绝的认证，填充表单以便重新申请
      if (certificationInfo.value.certificationStatus === 2) {
        Object.keys(certificationForm).forEach(key => {
          if (certificationInfo.value[key]) {
            certificationForm[key] = certificationInfo.value[key];
          }
        });

        // 设置文件列表
        if (certificationInfo.value.businessLicense) {
          businessLicenseList.value = [{
            name: '营业执照',
            url: certificationInfo.value.businessLicense
          }];
        }
      }
    } else {
      // 如果没有认证信息，显示申请表单
      certificationInfo.value = null;
      showForm.value = true;
    }
  } catch (error) {
    console.error('获取认证信息失败:', error);

    // 如果是403错误，说明用户没有权限，显示申请表单
    if (error.response && error.response.status === 403) {
      certificationInfo.value = null;
      showForm.value = true;
    } else {
      ElMessage.error('获取认证信息失败，请刷新页面重试');
    }
  }
}

// 更新基本信息
function updateBasicInfo() {
  basicFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await updateUserInfo({
        nickname: basicForm.nickname,
        avatar: basicForm.avatar,
        email: basicForm.email,
        phone: basicForm.phone,
        gender: basicForm.gender,
        bio: basicForm.bio
      })

      ElMessage.success('个人信息更新成功')

      // 更新 store 中的用户信息
      merchantStore.setMerchantInfo({
        nickname: basicForm.nickname,
        avatar: basicForm.avatar,
        email: basicForm.email,
        phone: basicForm.phone,
        gender: basicForm.gender,
        bio: basicForm.bio
      })
    } catch (error) {
      console.error('更新个人信息失败:', error)
      ElMessage.error('更新个人信息失败')
    } finally {
      submitting.value = false
    }
  })
}

// 修改密码
function updatePassword() {
  passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await updatePwd({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })

      ElMessage.success('密码修改成功，请重新登录')

      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''

      // 退出登录
      setTimeout(() => {
        merchantStore.logoutAction()
        router.push('/login')
      }, 1500)
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error('修改密码失败')
    } finally {
      submitting.value = false
    }
  })
}

// 显示申请表单
function showApplyForm() {
  showForm.value = true
}

// 提交认证
function submitCertification() {
  certificationFormRef.value.validate(async valid => {
    if (valid) {
      try {
        // 显示确认对话框
        ElMessageBox.confirm(
          '提交认证申请后，需要等待管理员审核，审核通过后才能使用商家功能。确定要提交吗？',
          '提交认证',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(async () => {
          try {
            // 检查是否有必要的字段
            if (!certificationForm.companyName) {
              ElMessage.error('请输入公司名称')
              return
            }
            if (!certificationForm.contactName) {
              ElMessage.error('请输入联系人姓名')
              return
            }
            if (!certificationForm.contactPhone) {
              ElMessage.error('请输入联系电话')
              return
            }
            if (!certificationForm.contactEmail) {
              ElMessage.error('请输入联系邮箱')
              return
            }
            if (!certificationForm.businessLicense) {
              ElMessage.error('请上传营业执照')
              return
            }

            // 尝试提交认证申请
            try {
              // 根据用户角色选择不同的API
              const userRole = merchantStore.merchantInfo.role;

              if (userRole === 1) {
                // 商家角色使用商家认证接口
                await applyCertification(certificationForm);
              } else {
                // 普通用户使用普通用户认证接口
                await applyUserCertification(certificationForm);
              }

              ElMessage.success('认证申请提交成功，请等待审核');
              showForm.value = false;

              // 更新用户信息，确保角色信息是最新的
              await merchantStore.fetchUserInfo();

              // 手动更新认证状态为"审核中"(0)
              merchantStore.setMerchantInfo({
                certificationStatus: 0
              });

              console.log('提交认证后更新状态为审核中');
              console.log('当前商家信息:', merchantStore.merchantInfo);

              // 重新获取认证信息
              fetchCertificationInfo();

              // 刷新页面以确保状态更新
              setTimeout(() => {
                window.location.reload();
              }, 1000);
            } catch (error) {
              console.error('提交认证申请失败:', error);

              // 如果是403错误，说明用户没有权限访问该接口
              if (error.response && error.response.status === 403) {
                // 显示更详细的错误信息
                ElMessageBox.alert(
                  '由于系统权限限制，您的认证申请无法直接提交。请将以下信息发送给管理员，由管理员帮您完成认证：<br><br>' +
                  `公司名称：${certificationForm.companyName}<br>` +
                  `联系人：${certificationForm.contactName}<br>` +
                  `联系电话：${certificationForm.contactPhone}<br>` +
                  `联系邮箱：${certificationForm.contactEmail}<br>` +
                  '营业执照：请将营业执照图片单独发送给管理员',
                  '认证申请提交失败',
                  {
                    dangerouslyUseHTMLString: true,
                    confirmButtonText: '我知道了',
                    type: 'warning'
                  }
                );

                // 保存认证信息到本地存储，以便下次打开页面时自动填充
                localStorage.setItem('certificationData', JSON.stringify(certificationForm));
              } else {
                ElMessage.error(error.message || '提交认证申请失败，请稍后再试');
              }
            }
          } catch (error) {
            console.error('提交认证申请失败:', error)
            ElMessage.error(error.message || '提交认证申请失败，请稍后再试')
          }
        }).catch(() => {
          // 用户取消提交
          ElMessage.info('已取消提交')
        })
      } catch (error) {
        console.error('提交认证申请失败:', error)
        ElMessage.error(error.message || '提交认证申请失败')
      }
    }
  })
}

// 头像上传成功
function handleAvatarSuccess(res) {
  basicForm.avatar = res.data
}

// 头像上传前校验
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

// 通用上传前校验
function beforeUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
    return false
  }

  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }

  return true
}

// 营业执照上传成功
function handleBusinessLicenseSuccess(response) {
  certificationForm.businessLicense = response.data
  ElMessage.success('营业执照上传成功')
}

// 上传失败
function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

// 重置表单
function resetBasicForm() {
  fetchUserInfo()
}

function resetPasswordForm() {
  passwordFormRef.value.resetFields()
}

function resetCertificationForm() {
  certificationFormRef.value.resetFields()
  businessLicenseList.value = []
}

// 根据URL参数切换选项卡
function checkTabFromQuery() {
  const { tab } = route.query
  if (tab && ['basic', 'password', 'certification'].includes(tab)) {
    activeTab.value = tab
  }
}

// 检查本地存储的认证数据
function checkLocalCertificationData() {
  const certificationDataStr = localStorage.getItem('certificationData')
  if (certificationDataStr) {
    try {
      const certData = JSON.parse(certificationDataStr)
      // 填充认证表单
      if (certData.companyName) certificationForm.companyName = certData.companyName
      if (certData.contactName) certificationForm.contactName = certData.contactName
      if (certData.contactPhone) certificationForm.contactPhone = certData.contactPhone
      if (certData.contactEmail) certificationForm.contactEmail = certData.contactEmail

      // 显示认证表单
      showForm.value = true

      // 清除本地存储的认证数据
      localStorage.removeItem('certificationData')

      // 提示用户
      ElMessage.info('已自动填充您之前保存的认证信息，请上传营业执照并提交认证申请')
    } catch (error) {
      console.error('解析认证数据失败:', error)
    }
  }
}

// 生命周期钩子
onMounted(async () => {
  // 先获取用户信息
  await fetchUserInfo();

  // 然后获取认证信息
  await fetchCertificationInfo();

  // 检查URL参数
  checkTabFromQuery();

  // 检查本地存储的认证数据
  checkLocalCertificationData();

  // 打印当前状态
  console.log('页面加载完成，当前认证状态:', merchantStore.merchantInfo.certificationStatus);
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;

  .profile-layout {
    display: flex;
    gap: 20px;

    .profile-sidebar {
      width: 200px;
      flex-shrink: 0;

      .profile-menu {
        border-radius: 4px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }
    }

    .profile-content {
      flex: 1;

      .profile-card {
        .card-header {
          h2 {
            margin: 0;
            font-size: 20px;
          }
        }

        .profile-form {
          max-width: 600px;
          margin: 20px auto;

          .avatar-uploader {
            .avatar {
              width: 100px;
              height: 100px;
              display: block;
              border-radius: 50%;
              object-fit: cover;
            }

            .avatar-uploader-icon {
              font-size: 28px;
              color: #8c939d;
              width: 100px;
              height: 100px;
              line-height: 100px;
              text-align: center;
              border: 1px dashed #d9d9d9;
              border-radius: 50%;
            }
          }
        }

        // 认证相关样式
        .certification-status {
          .reject-reason {
            margin-top: 10px;
            color: #F56C6C;
          }

          .certification-info {
            margin-top: 20px;

            .certification-images {
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

            .action-buttons {
              margin-top: 20px;
              display: flex;
              justify-content: center;
            }
          }
        }

        .certification-form {
          margin-top: 20px;

          .upload-container {
            .el-upload__tip {
              line-height: 1.2;
              padding: 8px 0;
            }
          }
        }
      }
    }
  }
}
</style>
