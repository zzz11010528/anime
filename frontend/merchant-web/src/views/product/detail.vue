<template>
  <div class="product-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>商品详情</h2>
          <div class="header-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="handleEdit">编辑</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="商品ID">{{ product.id }}</el-descriptions-item>
        <el-descriptions-item label="商品状态">
          <el-tag :type="getStatusType(product.status)">{{ getStatusText(product.status) }}</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="商品名称" :span="2">{{ product.name }}</el-descriptions-item>

        <el-descriptions-item label="商品分类">{{ product.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ product.ipName }}</el-descriptions-item>

        <el-descriptions-item label="商品描述" :span="2">{{ product.description }}</el-descriptions-item>

        <el-descriptions-item label="商品价格">¥{{ formatPrice(product.price) }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ product.stock }}</el-descriptions-item>

        <el-descriptions-item label="销量">{{ product.sales }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag v-if="product.isCertified === 1" type="success">已认证</el-tag>
          <el-tag v-else type="info">未认证</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="商家ID">{{ product.userId }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ product.username }}</el-descriptions-item>

        <el-descriptions-item label="创建时间">{{ formatDateTime(product.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(product.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 商品图片 -->
      <div class="image-section">
        <div class="section-title">商品图片</div>

        <div class="main-image">
          <div class="image-title">主图</div>
          <el-image
            :src="getImageUrl(product.mainImage)"
            :preview-src-list="[getImageUrl(product.mainImage)]"
            fit="contain"
            style="width: 200px; height: 200px"
          />
        </div>

        <div class="image-list">
          <div class="image-title">图片列表</div>
          <div class="images">
            <el-image
              v-for="(image, index) in product.images"
              :key="index"
              :src="getImageUrl(image.imageUrl)"
              :preview-src-list="product.images.map(img => getImageUrl(img.imageUrl))"
              fit="contain"
              style="width: 100px; height: 100px; margin-right: 10px; margin-bottom: 10px"
            />
          </div>
        </div>
      </div>

      <!-- 商品详情 -->
      <div class="detail-section">
        <div class="section-title">商品详情</div>

        <div class="detail-content">
          <div class="detail-text" v-html="product.detail"></div>
        </div>
      </div>


    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '../../api/product'

const route = useRoute()
const router = useRouter()

// 状态
const product = reactive({})

// 方法
function formatPrice(price) {
  return parseFloat(price || 0).toFixed(2)
}

// 获取图片URL
function getImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) {
    return url;
  } else {
    return `/api${url}`;
  }
}

function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getStatusType(status) {
  switch (status) {
    case 0: return 'info'     // 下架
    case 1: return 'success'  // 上架
    case 2: return 'warning'  // 审核中
    case 3: return 'danger'   // 审核失败
    default: return 'info'
  }
}

function getStatusText(status) {
  switch (status) {
    case 0: return '下架'
    case 1: return '上架'
    case 2: return '审核中'
    case 3: return '审核失败'
    default: return '未知'
  }
}

// 获取商品详情
async function getProduct() {
  try {
    const res = await getProductDetail(route.params.id)
    Object.assign(product, res.data)
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
    router.push('/product/list')
  }
}



// 返回
function goBack() {
  router.push('/product/list')
}

// 编辑
function handleEdit() {
  const productId = route.params.id;
  console.log('编辑商品ID:', productId);
  router.push(`/product/edit/${productId}`)
}

// 生命周期钩子
onMounted(() => {
  getProduct()
})
</script>

<style lang="scss" scoped>
.product-detail-container {
  .detail-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        font-size: 20px;
      }
    }

    .section-title {
      font-size: 16px;
      font-weight: bold;
      margin: 30px 0 20px;
      padding-bottom: 10px;
      border-bottom: 1px solid #eee;
    }

    .image-section {
      .main-image {
        margin-bottom: 20px;

        .image-title {
          font-weight: bold;
          margin-bottom: 10px;
        }
      }

      .image-list {
        .image-title {
          font-weight: bold;
          margin-bottom: 10px;
        }

        .images {
          display: flex;
          flex-wrap: wrap;
        }
      }
    }

    .detail-section {
      .detail-content {
        .detail-text {
          white-space: pre-wrap;
          line-height: 1.6;
          margin-bottom: 20px;
        }
      }
    }


  }
}
</style>
