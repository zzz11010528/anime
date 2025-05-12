<template>
  <div class="product-add-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑商品' : '添加商品' }}</h2>
        </div>
      </template>

      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="120px"
        class="product-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品名称" prop="name">
                <el-input v-model="productForm.name" placeholder="请输入商品名称" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="商品分类" prop="categoryId">
                <el-cascader
                  v-model="productForm.categoryId"
                  :options="categoryOptions"
                  :props="categoryProps"
                  placeholder="请选择商品分类"
                  style="width: 100%"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="IP" prop="ipId">
                <el-select v-model="productForm.ipId" placeholder="请选择IP" clearable style="width: 100%">
                  <el-option
                    v-for="item in ipOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="商品描述" prop="description">
                <el-input v-model="productForm.description" placeholder="请输入商品描述" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品主图" prop="mainImage">
                <el-upload
                  class="avatar-uploader"
                  action="/api/file/upload/product"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleMainImageSuccess"
                  :before-upload="beforeImageUpload"
                >
                  <img v-if="productForm.mainImage" :src="getImageUrl(productForm.mainImage)" class="avatar" />
                  <div v-else class="avatar-uploader-icon">
                    <el-icon><Plus /></el-icon>
                    <div class="upload-text">上传主图</div>
                  </div>
                </el-upload>
                <div class="upload-tip">建议尺寸：800x800px，大小不超过2MB</div>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="商品图片" prop="images">
                <el-upload
                  action="/api/file/upload/product"
                  :headers="uploadHeaders"
                  list-type="picture-card"
                  :file-list="fileList"
                  :on-success="handleImageSuccess"
                  :on-remove="handleImageRemove"
                  :before-upload="beforeImageUpload"
                  :preview-teleported="true"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <div class="upload-tip">最多上传5张图片，每张不超过2MB</div>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 价格库存 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Money /></el-icon>
            <span>价格库存</span>
          </div>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品价格" prop="price">
                <el-input-number
                  v-model="productForm.price"
                  :min="0"
                  :precision="2"
                  :step="10"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="库存" prop="stock">
                <el-input-number
                  v-model="productForm.stock"
                  :min="0"
                  :precision="0"
                  :step="10"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品状态">
                <el-radio-group v-model="productForm.status">
                  <el-radio :label="1">上架</el-radio>
                  <el-radio :label="0">下架</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 商品详情 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>商品详情</span>
          </div>

          <el-form-item label="详情内容" prop="detail">
            <el-input
              v-model="productForm.detail"
              type="textarea"
              :rows="6"
              placeholder="请输入商品详情"
            />
          </el-form-item>
        </div>

        <!-- 表单操作 -->
        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">{{ isEdit ? '保存修改' : '立即创建' }}</el-button>
          <el-button v-if="!isEdit" type="info" @click="saveDraft">保存草稿</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, InfoFilled, Money, Document } from '@element-plus/icons-vue'
import {
  addProduct, updateProduct, getProductDetail,
  getCategoryTreeVO, getIpList
} from '../../api/product'

const route = useRoute()
const router = useRouter()

// 状态
const productFormRef = ref(null)
const submitting = ref(false)
const categoryOptions = ref([])
const ipOptions = ref([])
const fileList = ref([])

// 分类级联选择器配置
const categoryProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: true,
  emitPath: false
}

// 表单数据
const productForm = reactive({
  name: '',
  categoryId: null,
  ipId: null,
  description: '',
  mainImage: '',
  images: [],
  price: 0,
  stock: 0,
  status: 1,
  detail: ''
})

// 表单验证规则
const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  description: [
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ],
  mainImage: [
    { required: true, message: '请上传商品主图', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入商品详情', trigger: 'blur' }
  ]
}

// 计算属性
const isEdit = computed(() => !!route.params.id)
const uploadHeaders = computed(() => {
  return {
    Authorization: localStorage.getItem('token')
  }
})

// 获取图片URL（添加API前缀）
function getImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) {
    return url;
  } else {
    return `/api${url}`;
  }
}

// 获取商品详情
async function getProduct() {
  try {
    const res = await getProductDetail(route.params.id)
    const product = res.data

    // 填充表单
    Object.keys(productForm).forEach(key => {
      if (product[key] !== undefined) {
        productForm[key] = product[key]
      }
    })

    // 处理图片列表
    if (product.images && product.images.length > 0) {
      // 设置文件列表用于显示
      fileList.value = product.images.map((img, index) => ({
        name: `image_${index}`,
        url: getImageUrl(img.imageUrl)
      }))

      // 确保productForm.images中的数据格式正确
      productForm.images = product.images.map(img => ({
        imageUrl: img.imageUrl
      }))
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
    router.push('/product/list')
  }
}

// 获取分类列表
async function fetchCategories() {
  try {
    const res = await getCategoryTreeVO()
    categoryOptions.value = res.data
    console.log('分类树数据:', res.data)
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 获取IP列表
async function fetchIps() {
  try {
    const res = await getIpList()
    ipOptions.value = res.data
    console.log('IP数据:', res.data)
  } catch (error) {
    console.error('获取IP列表失败:', error)
    ElMessage.error('获取IP列表失败')
  }
}

// 处理主图上传成功
function handleMainImageSuccess(res) {
  productForm.mainImage = res.data
}

// 处理图片上传成功
function handleImageSuccess(res) {
  // 添加图片对象，而不是直接添加URL字符串
  productForm.images.push({
    imageUrl: res.data
  })
}

// 处理图片移除
function handleImageRemove(file) {
  // 获取图片URL
  const imageUrl = file.url || file.response?.data
  // 查找包含该URL的图片对象
  const index = productForm.images.findIndex(img => img.imageUrl === imageUrl)
  if (index !== -1) {
    productForm.images.splice(index, 1)
  }
}

// 图片上传前校验
function beforeImageUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return (isJPG || isPNG) && isLt2M
}

// 提交表单
function submitForm() {
  productFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 准备提交的数据
      const submitData = { ...productForm }

      // 添加用户ID
      submitData.userId = localStorage.getItem('userId')

      // 将detail字段转换为content字段
      submitData.content = submitData.detail
      delete submitData.detail

      if (isEdit.value) {
        // 确保ID是数字类型
        const productId = parseInt(route.params.id);
        console.log('更新商品ID:', productId);
        console.log('更新商品数据:', submitData);
        await updateProduct(productId, submitData)
        ElMessage.success('商品更新成功')
      } else {
        await addProduct(submitData)
        ElMessage.success('商品添加成功')
      }
      router.push('/product/list')
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 保存草稿
function saveDraft() {
  productForm.status = 0 // 设置为下架状态
  submitForm()
}

// 返回
function goBack() {
  router.push('/product/list')
}

// 生命周期钩子
onMounted(() => {
  // 确保localStorage中有userId
  if (!localStorage.getItem('userId')) {
    const userInfo = JSON.parse(localStorage.getItem('merchantInfo') || '{}');
    if (userInfo.id) {
      localStorage.setItem('userId', userInfo.id);
    }
  }

  fetchCategories()
  fetchIps()

  if (isEdit.value) {
    getProduct()
  }
})
</script>

<style lang="scss" scoped>
.product-add-container {
  padding: 20px;

  .form-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #333;
      }
    }

    .product-form {
      padding: 10px;

      .form-section {
        margin-bottom: 30px;
        background-color: #f9f9f9;
        border-radius: 8px;
        padding: 20px;

        .section-title {
          display: flex;
          align-items: center;
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 1px solid #eaeaea;
          color: #409EFF;

          .el-icon {
            margin-right: 8px;
            font-size: 20px;
          }
        }

        .upload-tip {
          font-size: 12px;
          color: #909399;
          margin-top: 5px;
        }

        .avatar-uploader {
          .avatar {
            width: 150px;
            height: 150px;
            display: block;
            object-fit: cover;
            border-radius: 4px;
          }

          .avatar-uploader-icon {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            font-size: 28px;
            color: #8c939d;
            width: 150px;
            height: 150px;
            border: 1px dashed #d9d9d9;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              border-color: #409EFF;
              color: #409EFF;
            }

            .upload-text {
              font-size: 14px;
              margin-top: 8px;
            }
          }
        }
      }

      .form-actions {
        display: flex;
        justify-content: center;
        gap: 15px;
        margin-top: 30px;
      }
    }
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;

  &:hover {
    box-shadow: 0 0 0 1px #c0c4cc inset;
  }

  &.is-focus {
    box-shadow: 0 0 0 1px #409EFF inset;
  }
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-upload--picture-card) {
  --el-upload-picture-card-size: 120px;
  border-radius: 4px;

  &:hover {
    border-color: #409EFF;
  }
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  border-radius: 4px;
}
</style>