<template>
  <div class="product-edit-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑商品' : '添加商品' }}</span>
        </div>
      </template>

      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="120px"
        class="product-form"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="productForm.categoryId"
            :options="categoryOptions"
            :props="{
              checkStrictly: true,
              value: 'id',
              label: 'name',
              children: 'children'
            }"
            placeholder="请选择商品分类"
            clearable
          />
        </el-form-item>

        <el-form-item label="IP" prop="ipId">
          <el-select v-model="productForm.ipId" placeholder="请选择IP" clearable>
            <el-option
              v-for="ip in ipOptions"
              :key="ip.id"
              :label="ip.name"
              :value="ip.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="productForm.price"
            :precision="2"
            :step="0.1"
            :min="0"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="商品库存" prop="stock">
          <el-input-number
            v-model="productForm.stock"
            :min="0"
            :step="1"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item label="商品主图" prop="mainImage">
          <el-upload
            class="upload-container"
            action="/api/file/upload/product"
            :headers="uploadHeaders"
            :on-success="handleMainImageSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :limit="1"
            :file-list="mainImageList"
          >
            <el-button type="primary">上传主图</el-button>
            <template #tip>
              <div class="el-upload__tip">请上传商品主图，JPG/PNG格式，大小不超过5MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品图片" prop="images">
          <el-upload
            class="upload-container"
            action="/api/file/upload/product"
            :headers="uploadHeaders"
            :on-success="handleImageSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :limit="9"
            :file-list="imagesList"
            multiple
            list-type="picture-card"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-dialog v-model="dialogVisible">
            <img w-full :src="dialogImageUrl" alt="Preview Image" />
          </el-dialog>
        </el-form-item>

        <el-form-item label="商品详情" prop="detail">
          <el-input
            v-model="productForm.detail.content"
            type="textarea"
            :rows="10"
            placeholder="请输入商品详情"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">{{ isEdit ? '保存修改' : '立即创建' }}</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useMerchantStore } from '../../stores/merchant'
import { getProductDetail, addProduct, updateProduct, getCategoryList, getIpList } from '../../api/product'

const route = useRoute()
const router = useRouter()
const merchantStore = useMerchantStore()

// 状态
const productFormRef = ref(null)
const isEdit = computed(() => !!route.params.id)
const categoryOptions = ref([])
const ipOptions = ref([])
const mainImageList = ref([])
const imagesList = ref([])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')

// 表单数据
const productForm = reactive({
  id: null,
  name: '',
  categoryId: null,
  ipId: null,
  price: 0,
  stock: 0,
  description: '',
  mainImage: '',
  images: [],
  detail: {
    content: ''
  }
})

// 表单验证规则
const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
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
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 200, message: '长度在 10 到 200 个字符', trigger: 'blur' }
  ],
  mainImage: [
    { required: true, message: '请上传商品主图', trigger: 'change' }
  ]
}

// 上传相关
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${merchantStore.token}`
  }
})

// 方法
async function fetchCategoryList() {
  try {
    const res = await getCategoryList()
    categoryOptions.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

async function fetchIpList() {
  try {
    const res = await getIpList()
    ipOptions.value = res.data
  } catch (error) {
    console.error('获取IP列表失败:', error)
    ElMessage.error('获取IP列表失败')
  }
}

async function fetchProductDetail() {
  if (!isEdit.value) return

  try {
    const res = await getProductDetail(route.params.id)
    const productData = res.data

    // 填充表单数据
    Object.keys(productForm).forEach(key => {
      if (key === 'detail') {
        if (productData.detail) {
          productForm.detail.content = productData.detail.content
        }
      } else if (key === 'images') {
        if (productData.images && productData.images.length > 0) {
          productForm.images = productData.images.map(img => img.imageUrl)
          // 设置图片列表
          imagesList.value = productData.images.map(img => ({
            name: `图片${img.sort}`,
            url: img.imageUrl
          }))
        }
      } else {
        productForm[key] = productData[key]
      }
    })

    // 设置主图
    if (productData.mainImage) {
      mainImageList.value = [{
        name: '主图',
        url: productData.mainImage
      }]
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
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

function handleMainImageSuccess(response) {
  productForm.mainImage = response.data
  ElMessage.success('主图上传成功')
}

function handleImageSuccess(response) {
  productForm.images.push(response.data)
  ElMessage.success('图片上传成功')
}

function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

function handlePictureCardPreview(file) {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

function handleRemove(file) {
  // 从 images 数组中移除
  const index = productForm.images.indexOf(file.url || file.response.data)
  if (index !== -1) {
    productForm.images.splice(index, 1)
  }
}

function submitForm() {
  productFormRef.value.validate(async valid => {
    if (valid) {
      try {
        // 准备提交的数据
        const submitData = { ...productForm }

        // 处理图片数据
        submitData.images = productForm.images.map((url, index) => ({
          imageUrl: url,
          sort: index + 1
        }))

        if (isEdit.value) {
          await updateProduct(submitData)
          ElMessage.success('商品更新成功')
        } else {
          await addProduct(submitData)
          ElMessage.success('商品添加成功')
        }

        // 返回列表页
        router.push('/product/list')
      } catch (error) {
        console.error('提交商品失败:', error)
        ElMessage.error(error.message || '提交失败')
      }
    }
  })
}

function cancel() {
  ElMessageBox.confirm('确定要取消编辑吗？未保存的数据将丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/product/list')
  }).catch(() => {})
}

// 生命周期钩子
onMounted(async () => {
  await Promise.all([
    fetchCategoryList(),
    fetchIpList()
  ])

  await fetchProductDetail()
})
</script>

<style lang="scss" scoped>
.product-edit-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .product-form {
    max-width: 800px;
    margin: 0 auto;

    .upload-container {
      .el-upload__tip {
        line-height: 1.2;
        padding: 8px 0;
      }
    }

    .editor-container {
      border: 1px solid #dcdfe6;
      border-radius: 4px;

      .editor {
        min-height: 300px;
      }
    }
  }
}
</style>
