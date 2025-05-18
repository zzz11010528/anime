<template>
  <div class="product-form-container">
    <el-card class="product-form-card">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑商品' : '添加商品' }}</h2>
        </div>
      </template>

      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
        class="product-form"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择商品分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="IP" prop="ipId">
          <el-select v-model="productForm.ipId" placeholder="请选择IP（可选）" clearable style="width: 100%">
            <el-option
              v-for="ip in ipOptions"
              :key="ip.id"
              :label="ip.name"
              :value="ip.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" :min="0.01" :step="0.01" style="width: 100%"></el-input-number>
        </el-form-item>

        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="1" :step="1" style="width: 100%"></el-input-number>
        </el-form-item>

        <el-form-item label="主图" prop="mainImage">
          <el-upload
            class="product-image-uploader"
            action="/api/file/upload/product"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleMainImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="productForm.mainImage" :src="formatImageUrl(productForm.mainImage)" class="product-image" />
            <el-icon v-else class="product-image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="详情图片" prop="images">
          <el-upload
            class="product-images-uploader"
            action="/api/file/upload/product"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleDetailImageSuccess"
            :on-remove="handleDetailImageRemove"
            :before-upload="beforeImageUpload"
            :file-list="fileList"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" rows="4" placeholder="请输入商品描述"></el-input>
        </el-form-item>

        <el-form-item label="商品详情" prop="content">
          <el-input v-model="productForm.content" type="textarea" rows="6" placeholder="请输入商品详情"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">{{ isEdit ? '保存修改' : '发布商品' }}</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryList, getProductDetail, addProduct, updateProduct } from '../../api/seller'
import { getIpList } from '../../api/ip'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()
const productFormRef = ref(null)
const loading = ref(false)
const categories = ref([])
const ipOptions = ref([])
const fileList = ref([])

// 判断是否是编辑模式
const isEdit = computed(() => route.name === 'product-edit')
const productId = computed(() => isEdit.value ? parseInt(route.params.id) : null)

// 上传图片的请求头
const uploadHeaders = computed(() => {
  return {
    'Authorization': localStorage.getItem('token')
  }
})

// 商品表单
const productForm = reactive({
  name: '',
  categoryId: '',
  ipId: null, // 添加IP字段
  price: 0,
  stock: 1,
  mainImage: '',
  images: [],
  description: '',
  content: '' // 使用content字段替代detail字段，与后端保持一致
})

// 表单验证规则
const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度应在2-50个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ],
  mainImage: [
    { required: true, message: '请上传商品主图', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入商品详情', trigger: 'blur' }
  ]
}

// 获取商品分类
async function fetchCategories() {
  try {
    const res = await getCategoryList()
    if (res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('获取商品分类失败:', error)
    ElMessage.error('获取商品分类失败')
  }
}

// 获取IP列表
async function fetchIpList() {
  try {
    const res = await getIpList()
    if (res.data) {
      ipOptions.value = res.data
    }
  } catch (error) {
    console.error('获取IP列表失败:', error)
    ElMessage.error('获取IP列表失败')
  }
}

// 获取商品详情（编辑模式）
async function fetchProductDetail() {
  if (!isEdit.value || !productId.value) return

  try {
    loading.value = true
    const res = await getProductDetail(productId.value)
    if (res.data) {
      const product = res.data
      productForm.name = product.name
      productForm.categoryId = product.categoryId
      productForm.ipId = product.ipId
      productForm.price = product.price
      productForm.stock = product.stock
      productForm.mainImage = product.mainImage

      // 处理商品图片
      if (product.images && Array.isArray(product.images)) {
        // 如果images是数组对象，提取imageUrl
        productForm.images = product.images.map(img => img.imageUrl)

        // 设置fileList用于显示已上传的图片
        fileList.value = product.images.map((img, index) => ({
          name: `image_${index}`,
          url: formatImageUrl(img.imageUrl),
          response: { data: img.imageUrl }
        }))
      } else if (typeof product.images === 'string' && product.images) {
        // 如果images是字符串，按逗号分割
        const imageUrls = product.images.split(',')
        productForm.images = imageUrls

        // 设置fileList用于显示已上传的图片
        fileList.value = imageUrls.map((url, index) => ({
          name: `image_${index}`,
          url: formatImageUrl(url),
          response: { data: url }
        }))
      } else {
        productForm.images = []
        fileList.value = []
      }

      productForm.description = product.description
      productForm.content = product.detail || product.content

      console.log('商品详情加载成功:', product)
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 图片上传相关方法
function beforeImageUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

function handleMainImageSuccess(res) {
  if (res.code === 200 && res.data) {
    productForm.mainImage = res.data
  } else {
    ElMessage.error('上传失败')
  }
}

function handleDetailImageSuccess(res, file) {
  if (res.code === 200 && res.data) {
    productForm.images.push(res.data)

    // 确保fileList中也有这个新上传的文件
    // Element Plus会自动处理fileList，但我们在这里确保一下
    const existingFile = fileList.value.find(item =>
      (item.response && item.response.data === res.data) || item.url === formatImageUrl(res.data)
    )

    if (!existingFile) {
      fileList.value.push({
        name: file.name,
        url: formatImageUrl(res.data),
        response: { data: res.data }
      })
    }
  } else {
    ElMessage.error('上传失败')
  }
}

function handleDetailImageRemove(file) {
  // 获取要删除的图片URL
  const imageUrl = file.response ? file.response.data : file.url

  // 从productForm.images中删除
  const index = productForm.images.indexOf(imageUrl)
  if (index !== -1) {
    productForm.images.splice(index, 1)
  }

  // 从fileList中删除
  const fileIndex = fileList.value.findIndex(item =>
    (item.response && item.response.data === imageUrl) || item.url === imageUrl
  )
  if (fileIndex !== -1) {
    fileList.value.splice(fileIndex, 1)
  }
}

// 提交表单
async function submitForm() {
  if (!productFormRef.value) return

  await productFormRef.value.validate(async (valid) => {
    if (!valid) {
      return false
    }

    try {
      loading.value = true

      // 处理图片数组
      const formData = { ...productForm }
      if (formData.images && formData.images.length > 0) {
        // 将图片URL数组转换为后端期望的图片对象数组格式
        formData.images = formData.images.map((url, index) => ({
          imageUrl: url,
          sort: index + 1
        }))
      } else {
        formData.images = []
      }

      // content字段已经直接使用，不需要转换

      if (isEdit.value) {
        // 编辑模式
        formData.id = productId.value
        await updateProduct(formData)
        ElMessage.success('商品更新成功')
      } else {
        // 添加模式
        await addProduct(formData)
        ElMessage.success('商品发布成功')
      }

      // 返回商品列表
      router.push('/my-products')
    } catch (error) {
      console.error('提交商品失败:', error)
      ElMessage.error('操作失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
function resetForm() {
  if (!productFormRef.value) return

  productFormRef.value.resetFields()
  if (!isEdit.value) {
    productForm.mainImage = ''
    productForm.images = []
    fileList.value = []
  } else {
    fetchProductDetail()
  }
}

// 返回
function goBack() {
  router.push('/my-products')
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchIpList()
  if (isEdit.value) {
    fetchProductDetail()
  }
})
</script>

<style lang="scss" scoped>
.product-form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;

  .product-form-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .product-form {
      margin-top: 20px;
    }

    .product-image-uploader {
      .product-image {
        width: 178px;
        height: 178px;
        display: block;
      }

      .product-image-uploader-icon {
        font-size: 28px;
        color: #8c939d;
        width: 178px;
        height: 178px;
        line-height: 178px;
        text-align: center;
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
      }
    }
  }
}
</style>
