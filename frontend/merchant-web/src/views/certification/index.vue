<template>
  <div class="certification-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商家认证</span>
        </div>
      </template>
      
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
            <el-descriptions-item label="公司地址" :span="2">{{ certificationInfo.companyAddress }}</el-descriptions-item>
            <el-descriptions-item label="公司简介" :span="2">{{ certificationInfo.companyDescription }}</el-descriptions-item>
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
                :src="certificationInfo.businessLicense" 
                :preview-src-list="[certificationInfo.businessLicense]"
                fit="cover"
                style="width: 200px; height: 200px"
              />
            </div>
            <div class="image-item" v-if="certificationInfo.idCardFront">
              <div class="image-title">身份证正面</div>
              <el-image 
                :src="certificationInfo.idCardFront" 
                :preview-src-list="[certificationInfo.idCardFront]"
                fit="cover"
                style="width: 200px; height: 200px"
              />
            </div>
            <div class="image-item" v-if="certificationInfo.idCardBack">
              <div class="image-title">身份证反面</div>
              <el-image 
                :src="certificationInfo.idCardBack" 
                :preview-src-list="[certificationInfo.idCardBack]"
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
          
          <el-form-item label="公司地址" prop="companyAddress">
            <el-input v-model="certificationForm.companyAddress" placeholder="请输入公司地址" />
          </el-form-item>
          
          <el-form-item label="公司简介" prop="companyDescription">
            <el-input
              v-model="certificationForm.companyDescription"
              type="textarea"
              :rows="4"
              placeholder="请输入公司简介"
            />
          </el-form-item>
          
          <el-form-item label="营业执照" prop="businessLicense">
            <el-upload
              class="upload-container"
              action="/api/upload"
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
          
          <el-form-item label="身份证正面" prop="idCardFront">
            <el-upload
              class="upload-container"
              action="/api/upload"
              :headers="uploadHeaders"
              :on-success="handleIdCardFrontSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :limit="1"
              :file-list="idCardFrontList"
            >
              <el-button type="primary">上传身份证正面</el-button>
              <template #tip>
                <div class="el-upload__tip">请上传清晰的身份证正面照片，JPG/PNG格式，大小不超过5MB</div>
              </template>
            </el-upload>
          </el-form-item>
          
          <el-form-item label="身份证反面" prop="idCardBack">
            <el-upload
              class="upload-container"
              action="/api/upload"
              :headers="uploadHeaders"
              :on-success="handleIdCardBackSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :limit="1"
              :file-list="idCardBackList"
            >
              <el-button type="primary">上传身份证反面</el-button>
              <template #tip>
                <div class="el-upload__tip">请上传清晰的身份证反面照片，JPG/PNG格式，大小不超过5MB</div>
              </template>
            </el-upload>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitCertification">提交认证</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMerchantStore } from '../../stores/merchant'
import { getCertificationInfo, applyCertification } from '../../api/certification'

const merchantStore = useMerchantStore()

// 状态
const certificationInfo = ref(null)
const showForm = ref(false)
const certificationFormRef = ref(null)
const businessLicenseList = ref([])
const idCardFrontList = ref([])
const idCardBackList = ref([])

// 表单数据
const certificationForm = reactive({
  companyName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  companyAddress: '',
  companyDescription: '',
  businessLicense: '',
  idCardFront: '',
  idCardBack: ''
})

// 表单验证规则
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
  companyAddress: [
    { required: true, message: '请输入公司地址', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  companyDescription: [
    { required: true, message: '请输入公司简介', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  businessLicense: [
    { required: true, message: '请上传营业执照', trigger: 'change' }
  ],
  idCardFront: [
    { required: true, message: '请上传身份证正面', trigger: 'change' }
  ],
  idCardBack: [
    { required: true, message: '请上传身份证反面', trigger: 'change' }
  ]
}

// 上传相关
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${merchantStore.token}`
  }
})

// 方法
async function fetchCertificationInfo() {
  try {
    const res = await getCertificationInfo()
    certificationInfo.value = res.data
    
    // 如果已经有认证信息，填充表单
    if (certificationInfo.value && certificationInfo.value.certificationStatus === 2) {
      Object.keys(certificationForm).forEach(key => {
        if (certificationInfo.value[key]) {
          certificationForm[key] = certificationInfo.value[key]
        }
      })
      
      // 设置文件列表
      if (certificationInfo.value.businessLicense) {
        businessLicenseList.value = [{
          name: '营业执照',
          url: certificationInfo.value.businessLicense
        }]
      }
      
      if (certificationInfo.value.idCardFront) {
        idCardFrontList.value = [{
          name: '身份证正面',
          url: certificationInfo.value.idCardFront
        }]
      }
      
      if (certificationInfo.value.idCardBack) {
        idCardBackList.value = [{
          name: '身份证反面',
          url: certificationInfo.value.idCardBack
        }]
      }
    }
  } catch (error) {
    console.error('获取认证信息失败:', error)
  }
}

function showApplyForm() {
  showForm.value = true
}

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

function handleBusinessLicenseSuccess(response, file) {
  certificationForm.businessLicense = response.data
  ElMessage.success('营业执照上传成功')
}

function handleIdCardFrontSuccess(response, file) {
  certificationForm.idCardFront = response.data
  ElMessage.success('身份证正面上传成功')
}

function handleIdCardBackSuccess(response, file) {
  certificationForm.idCardBack = response.data
  ElMessage.success('身份证反面上传成功')
}

function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

function submitCertification() {
  certificationFormRef.value.validate(async valid => {
    if (valid) {
      try {
        await applyCertification(certificationForm)
        ElMessage.success('认证申请提交成功，请等待审核')
        showForm.value = false
        fetchCertificationInfo()
      } catch (error) {
        console.error('提交认证申请失败:', error)
        ElMessage.error(error.message || '提交认证申请失败')
      }
    }
  })
}

function resetForm() {
  certificationFormRef.value.resetFields()
  businessLicenseList.value = []
  idCardFrontList.value = []
  idCardBackList.value = []
}

// 生命周期钩子
onMounted(() => {
  fetchCertificationInfo()
})
</script>

<style lang="scss" scoped>
.certification-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
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
</style>
