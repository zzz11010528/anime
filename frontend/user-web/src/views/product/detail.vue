<template>
  <div class="product-detail-container">
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" animated>
        <template #template>
          <div style="display: flex; gap: 20px;">
            <el-skeleton-item variant="image" style="width: 400px; height: 400px;" />
            <div style="flex: 1;">
              <el-skeleton-item variant="h1" style="width: 50%;" />
              <el-skeleton-item variant="text" style="margin-top: 20px; width: 80%;" />
              <el-skeleton-item variant="text" style="width: 60%;" />
              <el-skeleton-item variant="text" style="width: 70%;" />
              <el-skeleton-item variant="text" style="width: 40%;" />
              <el-skeleton-item variant="button" style="margin-top: 40px; width: 200px; height: 40px;" />
            </div>
          </div>
        </template>
      </el-skeleton>
    </div>

    <div v-else-if="!product.id" class="not-found">
      <el-empty description="商品不存在或已下架" />
      <el-button type="primary" @click="goBack">返回商品列表</el-button>
    </div>

    <template v-else>
      <!-- 商品基本信息 -->
      <el-card class="product-info-card">
        <div class="product-info">
          <div class="product-gallery">
            <div class="main-image-container">
              <el-image
                :src="currentImage"
                :alt="product.name"
                class="main-product-image"
                :preview-src-list="productImages"
                :initial-index="currentImageIndex"
                fit="contain"
              />
            </div>
            <div class="thumbnail-container" v-if="productImages.length > 1">
              <div
                v-for="(image, index) in productImages"
                :key="index"
                class="thumbnail-item"
                :class="{ 'active': currentImageIndex === index }"
                @click="currentImageIndex = index"
              >
                <el-image
                  :src="image"
                  :alt="`${product.name} - 缩略图 ${index + 1}`"
                  fit="cover"
                />
              </div>
            </div>
          </div>

          <div class="product-details">
            <div class="product-title">
              <h1>{{ product.name }}</h1>
              <div v-if="product.isCertified === 1" class="certified-badge">
                <el-icon><Check /></el-icon>
                <span>官方认证</span>
              </div>
            </div>

            <div class="product-price">
              <div class="current-price">¥{{ formatPrice(product.price) }}</div>
              <div v-if="product.originalPrice" class="original-price">¥{{ formatPrice(product.originalPrice) }}</div>
            </div>

            <div class="product-stats">
              <div class="stat-item">
                <span class="label">销量：</span>
                <span class="value">{{ product.sales }}</span>
              </div>
              <div class="stat-item">
                <span class="label">库存：</span>
                <span class="value">{{ product.stock }}</span>
              </div>
              <div class="stat-item">
                <span class="label">IP：</span>
                <span class="value">{{ product.ipName || '无' }}</span>
              </div>
            </div>

            <div class="product-tags">
              <el-tag v-if="product.categoryName" size="small">{{ product.categoryName }}</el-tag>
              <el-tag v-if="product.isNew === 1" type="success" size="small">新品</el-tag>
              <el-tag v-if="product.isHot === 1" type="danger" size="small">热销</el-tag>
              <el-tag v-if="product.isRecommend === 1" type="warning" size="small">推荐</el-tag>
            </div>

            <div class="product-quantity">
              <span class="label">数量：</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="product.stock > 0 ? product.stock : 1"
                :disabled="product.stock <= 0"
              />
            </div>

            <div class="product-actions">
              <el-button type="primary" size="large" @click="handleAddToCart" :disabled="product.stock <= 0">
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button type="danger" size="large" @click="buyNow" :disabled="product.stock <= 0">
                立即购买
              </el-button>
              <el-button :type="productCollected ? 'warning' : 'default'" size="large" @click="toggleCollect">
                <el-icon><Star /></el-icon>
                {{ productCollected ? '已收藏' : '收藏' }}
              </el-button>
            </div>

            <div class="merchant-info">
              <div class="merchant-title">
                <span>商家信息</span>
              </div>
              <div class="merchant-content">
                <el-avatar :src="product.avatar ? formatImageUrl(product.avatar) : defaultAvatar" :size="40"></el-avatar>
                <div class="merchant-name">{{ product.nickname || product.username }}</div>
                <el-button type="primary" text @click="contactMerchant">联系商家</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 商品详情 -->
      <el-card class="product-detail-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="product-description" v-html="product.description"></div>
          </el-tab-pane>

          <el-tab-pane label="用户评价" name="reviews">
            <el-empty v-if="reviews.length === 0" description="暂无评价" />
            <div v-else class="review-list">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="user-info">
                    <el-avatar :src="review.avatar ? formatImageUrl(review.avatar) : defaultAvatar" :size="40"></el-avatar>
                    <div class="user-name">{{ review.nickname || review.username }}</div>
                  </div>
                  <div class="review-rating">
                    <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
                  </div>
                </div>
                <div class="review-content">{{ review.content }}</div>
                <div class="review-images" v-if="review.images && review.images.length > 0 && getPreviewImages(review).length > 0">
                  <el-image
                    v-for="(image, index) in getPreviewImages(review)"
                    :key="index"
                    :src="image"
                    :preview-src-list="getPreviewImages(review)"
                    :initial-index="index"
                    preview-teleported
                    fit="cover"
                    class="review-image"
                  />
                </div>
                <div class="review-footer">
                  <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                </div>
              </div>

              <div class="pagination">
                <el-pagination
                  v-model:current-page="reviewPage"
                  v-model:page-size="reviewSize"
                  :page-sizes="[5, 10, 20]"
                  layout="total, sizes, prev, pager, next"
                  :total="reviewTotal"
                  @size-change="handleReviewSizeChange"
                  @current-change="handleReviewCurrentChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <!-- 相关商品推荐 -->
      <el-card class="related-products-card">
        <template #header>
          <div class="card-header">
            <span>相关商品推荐</span>
          </div>
        </template>

        <el-empty v-if="relatedProducts.length === 0" description="暂无相关商品" />
        <el-row v-else :gutter="20">
          <el-col v-for="item in relatedProducts" :key="item.id" :xs="12" :sm="8" :md="6" :lg="4">
            <product-card :product="item" />
          </el-col>
        </el-row>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, ShoppingCart, Star } from '@element-plus/icons-vue'
import { getProductDetail } from '../../api/product'
// import { addToCart } from '../../api/cart'
import { collect, uncollect, checkIsCollected as isCollected } from '../../api/community'
import { getProductReviews } from '../../api/review'
import { useUserStore } from '../../stores/user'
import { useCartStore } from '../../stores/cart'
import ProductCard from '../../components/ProductCard.vue'
import defaultAvatar from '../../assets/images/default-avatar.png'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 状态
const product = ref({})
const loading = ref(true)
const quantity = ref(1)
const activeTab = ref('detail') // 只有'detail'和'reviews'两个选项卡
const productCollected = ref(false)
const currentImageIndex = ref(0)

// 评价相关
const reviews = ref([])
const reviewPage = ref(1)
const reviewSize = ref(5)
const reviewTotal = ref(0)

// 相关商品
const relatedProducts = ref([])

// 计算属性
const productImages = computed(() => {
  if (!product.value.images) return [formatImageUrl(product.value.mainImage)]
  return [
    formatImageUrl(product.value.mainImage),
    ...product.value.images.map(img => formatImageUrl(img.imageUrl))
  ]
})

const currentImage = computed(() => {
  if (productImages.value.length === 0) return ''
  return productImages.value[currentImageIndex.value]
})

const isLoggedIn = computed(() => userStore.isLoggedIn)

// 方法
async function fetchProductDetail() {
  loading.value = true
  try {
    const productId = route.params.id
    const res = await getProductDetail(productId)
    product.value = res.data

    // 确保库存不为负数
    if (product.value.stock < 0) {
      product.value.stock = 0
    }

    // 设置初始数量
    quantity.value = 1

    // 获取收藏状态
    if (isLoggedIn.value) {
      checkCollectionStatus()
    }

    // 获取评价
    fetchReviews()

    // 获取相关商品
    fetchRelatedProducts()
  } catch (error) {
    console.error('获取商品详情失败:', error)
    product.value = {}
  } finally {
    loading.value = false
  }
}

// 检查收藏状态
async function checkCollectionStatus() {
  try {
    const res = await isCollected(product.value.id, 1) // 1表示商品
    productCollected.value = res.data
  } catch (error) {
    console.error('获取收藏状态失败:', error)
  }
}

// 切换收藏状态
async function toggleCollect() {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('请先登录后再收藏', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }

  try {
    if (productCollected.value) {
      await uncollect(product.value.id, 1)
      productCollected.value = false
      ElMessage.success('取消收藏成功')
    } else {
      await collect(product.value.id, 1)
      productCollected.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 加入购物车
async function handleAddToCart() {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('请先登录后再添加购物车', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }

  try {
    await cartStore.addCartItem(product.value.id, quantity.value)
    ElMessage.success('添加购物车成功')
  } catch (error) {
    console.error('添加购物车失败:', error)
    ElMessage.error(error.message || '添加购物车失败')
  }
}

// 立即购买
function buyNow() {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('请先登录后再购买', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }

  // 跳转到确认订单页面
  router.push({
    path: '/order/confirm',
    query: {
      productId: product.value.id,
      quantity: quantity.value
    }
  })
}

// 联系商家
function contactMerchant() {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('请先登录后再联系商家', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }

  // 跳转到消息页面
  router.push({
    path: '/message',
    query: {
      toUserId: product.value.userId
    }
  })
}

// 获取评价
async function fetchReviews() {
  try {
    const res = await getProductReviews({
      productId: route.params.id,
      page: reviewPage.value,
      size: reviewSize.value
    })

    // 获取评价列表
    const reviewList = res.data.records || []

    // 确保每个评价都有images数组
    reviewList.forEach(review => {
      if (!review.images) {
        review.images = []
      }
    })

    reviews.value = reviewList
    reviewTotal.value = res.data.total || 0
  } catch (error) {
    console.error('获取商品评价失败:', error)
    reviews.value = []
    reviewTotal.value = 0
  }
}

// 获取相关商品
async function fetchRelatedProducts() {
  // TODO: 实现获取相关商品
  relatedProducts.value = []
}



// 返回商品列表
function goBack() {
  router.push('/product')
}

// 格式化价格
function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''

  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取评价图片预览列表
function getPreviewImages(review) {
  if (!review.images || review.images.length === 0) {
    return []
  }

  // 过滤掉无效的图片URL，并确保每个图片URL都经过格式化处理
  return review.images
    .filter(img => img && typeof img === 'string' && img.trim() !== '')
    .map(img => formatImageUrl(img))
    .filter(url => url && url.trim() !== '')
}

// 评价分页处理
function handleReviewSizeChange() {
  fetchReviews()
}

function handleReviewCurrentChange() {
  fetchReviews()
}

// 生命周期钩子
onMounted(() => {
  fetchProductDetail()
})
</script>

<style lang="scss" scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;

  .loading, .not-found {
    padding: 40px 0;
    text-align: center;
  }

  .product-info-card {
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    border-radius: 8px;
    overflow: hidden;

    :deep(.el-card__body) {
      padding: 25px;
    }

    .product-info {
      display: flex;
      gap: 30px;

      .product-gallery {
        width: 450px;
        display: flex;
        flex-direction: column;
        gap: 15px;

        .main-image-container {
          width: 100%;
          height: 400px;
          border: 1px solid #ebeef5;
          border-radius: 8px;
          overflow: hidden;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #f8f9fa;

          .main-product-image {
            width: 100%;
            height: 100%;
            object-fit: contain;
            cursor: pointer;
            transition: transform 0.3s;

            &:hover {
              transform: scale(1.02);
            }
          }
        }

        .thumbnail-container {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;

          .thumbnail-item {
            width: 80px;
            height: 80px;
            border: 2px solid #ebeef5;
            border-radius: 4px;
            overflow: hidden;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              border-color: #409EFF;
              transform: translateY(-2px);
            }

            &.active {
              border-color: #409EFF;
              box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
            }

            .el-image {
              width: 100%;
              height: 100%;
            }
          }
        }
      }

      .product-details {
        flex: 1;

        .product-title {
          display: flex;
          align-items: center;
          margin-bottom: 20px;

          h1 {
            margin: 0;
            font-size: 24px;
            color: #333;
          }

          .certified-badge {
            display: flex;
            align-items: center;
            background-color: #409EFF;
            color: #fff;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            margin-left: 10px;

            .el-icon {
              margin-right: 4px;
            }
          }
        }

        .product-price {
          display: flex;
          align-items: baseline;
          margin-bottom: 20px;

          .current-price {
            font-size: 28px;
            color: #f56c6c;
            font-weight: bold;
          }

          .original-price {
            margin-left: 10px;
            font-size: 16px;
            color: #999;
            text-decoration: line-through;
          }
        }

        .product-stats {
          display: flex;
          margin-bottom: 20px;

          .stat-item {
            margin-right: 20px;

            .label {
              color: #666;
            }

            .value {
              color: #333;
            }
          }
        }

        .product-tags {
          margin-bottom: 20px;

          .el-tag {
            margin-right: 10px;
          }
        }

        .product-quantity {
          display: flex;
          align-items: center;
          margin-bottom: 30px;

          .label {
            margin-right: 10px;
            color: #666;
          }
        }

        .product-actions {
          display: flex;
          gap: 15px;
          margin-bottom: 30px;
        }

        .merchant-info {
          border-top: 1px solid #eee;
          padding-top: 20px;

          .merchant-title {
            margin-bottom: 15px;
            font-size: 16px;
            font-weight: bold;
          }

          .merchant-content {
            display: flex;
            align-items: center;

            .merchant-name {
              margin: 0 15px;
              font-size: 14px;
            }
          }
        }
      }
    }
  }

  .product-detail-card {
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    border-radius: 8px;
    overflow: hidden;

    :deep(.el-tabs__header) {
      margin-bottom: 0;
      background-color: #f8f9fa;
      padding: 0 20px;
    }

    :deep(.el-tabs__nav) {
      border: none;
    }

    :deep(.el-tabs__item) {
      height: 60px;
      line-height: 60px;
      font-size: 16px;
      font-weight: 500;
      color: #606266;

      &.is-active {
        color: #409EFF;
        font-weight: 600;
      }

      &:hover {
        color: #409EFF;
      }
    }

    :deep(.el-tabs__active-bar) {
      height: 3px;
      border-radius: 3px;
    }

    .product-description {
      padding: 30px;
      line-height: 1.8;
      color: #333;
      font-size: 15px;

      :deep(img) {
        max-width: 100%;
        border-radius: 8px;
        margin: 15px 0;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      :deep(p) {
        margin-bottom: 15px;
      }
    }

    .review-list {
      padding: 20px 30px;

      .review-item {
        padding: 20px;
        border-bottom: 1px solid #eee;
        margin-bottom: 15px;
        border-radius: 8px;
        transition: all 0.3s;
        background-color: #fff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);

        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
          transform: translateY(-2px);
        }

        &:last-child {
          border-bottom: none;
        }

        .review-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;

          .user-info {
            display: flex;
            align-items: center;

            .user-name {
              margin-left: 10px;
              font-weight: bold;
              color: #333;
            }
          }

          .review-rating {
            :deep(.el-rate__icon) {
              margin-right: 4px;
              font-size: 18px;
            }

            :deep(.el-rate__text) {
              font-size: 14px;
              font-weight: bold;
            }
          }
        }

        .review-content {
          margin-bottom: 15px;
          line-height: 1.6;
          color: #333;
          font-size: 15px;
          padding: 0 5px;
        }

        .review-images {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;
          margin-bottom: 15px;

          .review-image {
            width: 100px;
            height: 100px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s;
            object-fit: cover;
            cursor: pointer;

            &:hover {
              transform: scale(1.05);
            }
          }

          :deep(.el-image-viewer__wrapper) {
            z-index: 2050;
          }
        }

        .review-footer {
          color: #999;
          font-size: 13px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding-top: 10px;
        }
      }

      .pagination {
        margin-top: 30px;
        text-align: center;
        padding-bottom: 10px;
      }
    }
  }

  .related-products-card {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    border-radius: 8px;
    overflow: hidden;

    :deep(.el-card__header) {
      background-color: #f8f9fa;
      padding: 15px 20px;
      border-bottom: 1px solid #ebeef5;
    }

    .card-header {
      font-size: 18px;
      font-weight: bold;
      color: #333;
    }

    :deep(.el-card__body) {
      padding: 25px;
    }

    :deep(.el-row) {
      margin-bottom: -20px;

      .el-col {
        margin-bottom: 20px;
      }
    }
  }
}

@media (max-width: 768px) {
  .product-detail-container {
    padding: 0 15px;

    .product-info-card {
      .product-info {
        flex-direction: column;

        .product-gallery {
          width: 100%;

          .main-image-container {
            height: 300px;
          }

          .thumbnail-container {
            justify-content: center;

            .thumbnail-item {
              width: 60px;
              height: 60px;
            }
          }
        }

        .product-details {
          .product-title {
            h1 {
              font-size: 20px;
            }
          }

          .product-price {
            .current-price {
              font-size: 24px;
            }
          }

          .product-actions {
            flex-wrap: wrap;

            .el-button {
              flex: 1;
              min-width: 120px;
            }
          }
        }
      }
    }

    .product-detail-card {
      :deep(.el-tabs__item) {
        height: 50px;
        line-height: 50px;
        font-size: 14px;
      }

      .product-description {
        padding: 20px 15px;
      }

      .review-list {
        padding: 15px;

        .review-item {
          padding: 15px;

          .review-images {
            gap: 8px;

            .review-image {
              width: 80px;
              height: 80px;
            }
          }
        }
      }
    }
  }
}
</style>
