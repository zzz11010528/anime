<template>
  <div class="home-container">
    <!-- 轮播图 - 热门商品 -->
    <div class="banner-section">
      <el-carousel height="500px" indicator-position="outside" arrow="always" :interval="5000">
        <el-carousel-item v-for="(product, i) in hotProducts.slice(0, 3)" :key="product.productId || product.id">
          <div class="banner-slide">
            <img :src="formatImageUrl(product.productImage || product.mainImage)" :alt="product.productName || product.name" class="banner-image">
            <div class="banner-content">
              <div class="banner-tag">热门推荐</div>
              <h2>{{ product.productName || product.name }}</h2>
              <p>{{ product.description || `热销商品，已售 ${product.sales} 件` }}</p>
              <div class="banner-price">¥{{ formatPrice(product.price) }}</div>
              <el-button type="primary" size="large" @click="goToProduct(product.productId || product.id)">立即查看</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 服务保障 -->
    <div class="service-section">
      <div class="service-item">
        <el-icon><Van /></el-icon>
        <span>全场包邮</span>
      </div>
      <div class="service-item">
        <el-icon><GoodsFilled /></el-icon>
        <span>正品保障</span>
      </div>
      <div class="service-item">
        <el-icon><RefreshRight /></el-icon>
        <span>7天无理由退换</span>
      </div>
      <div class="service-item">
        <el-icon><Service /></el-icon>
        <span>24小时客服</span>
      </div>
    </div>



    <!-- 拼团活动 -->
    <div class="group-buying-section" v-if="groupBuyings.length > 0">
      <div class="section-title">
        <h2>拼团活动</h2>
        <el-button type="text" @click="navigateTo('/group-buying')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
      </div>

      <el-row :gutter="24">
        <el-col v-for="groupBuying in groupBuyings" :key="groupBuying.id" :xs="24" :sm="12" :md="8" :lg="6">
          <group-buying-card :group-buying="groupBuying" />
        </el-col>
      </el-row>
    </div>

    <!-- 热门商品 -->
    <div class="hot-products-section">
      <div class="section-title">
        <h2>热门商品</h2>
        <el-button type="text" @click="navigateTo('/product')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
      </div>

      <el-row :gutter="24">
        <el-col v-for="product in hotProducts" :key="product.id || product.productId" :xs="24" :sm="12" :md="8" :lg="6">
          <product-card :product="product" />
        </el-col>
      </el-row>
    </div>

    <!-- IP专区 -->
    <div class="ip-section">
      <div class="section-title">
        <h2>IP专区</h2>
        <el-button type="text" @click="navigateTo('/product')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
      </div>

      <div class="ip-grid">
        <div v-for="ip in ipList" :key="ip.id" class="ip-item" @click="navigateToIp(ip.id)">
          <div class="ip-image">
            <img :src="formatImageUrl(ip.logo)" :alt="ip.name">
          </div>
          <div class="ip-info">
            <div class="ip-name">{{ ip.name }}</div>
            <div class="ip-desc">{{ ip.description || '点击查看相关商品' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门帖子 -->
    <hot-posts-section :limit="6" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, Goods, Handbag, Document, House, More, ShoppingBag, Notebook,
  Van, GoodsFilled, RefreshRight, Service
} from '@element-plus/icons-vue'
import { getCategoryList, getHotProducts } from '../../api/product'
import { getIpList } from '../../api/ip'
import { getActiveGroupBuyings } from '../../api/group'
import ProductCard from '../../components/ProductCard.vue'
import GroupBuyingCard from '../../components/GroupBuyingCard.vue'
import HotPostsSection from '../../components/HotPostsSection.vue'
import { formatImageUrl } from '../../utils/image'

const router = useRouter()

// 状态
const categories = ref([])
const hotProducts = ref([])
const ipList = ref([])
const groupBuyings = ref([])

// 方法
function navigateTo(path) {
  router.push(path)
}

function goToProduct(productId) {
  router.push(`/product/${productId}`)
}

function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

function navigateToCategory(categoryId) {
  router.push({
    path: '/product',
    query: { categoryId }
  })
}

function navigateToIp(ipId) {
  router.push({
    path: '/product',
    query: { ipId }
  })
}

// 根据分类名称返回对应的图标组件
function getCategoryIcon(categoryName) {
  const iconMap = {
    '手办': Goods,
    '服装': ShoppingBag,
    '文具': Document,
    '家居': House,
    '其他': More,
    'PVC手办': Goods,
    'Q版手办': Handbag,
    '可动手办': Goods,
    'T恤': ShoppingBag,
    '卫衣': ShoppingBag,
    '帽子': More,
    '文件夹': Document,
    '笔记本': Notebook,
    '钢笔': Document,
    '抱枕': House,
    '杯子': House,
    '挂画': House
    // 添加更多分类映射
  }

  // 如果找不到对应的图标，返回默认图标
  return iconMap[categoryName] || More
}

// 获取分类列表
async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取IP列表
async function fetchIpList() {
  try {
    const res = await getIpList()
    ipList.value = res.data
  } catch (error) {
    console.error('获取IP列表失败:', error)
  }
}

// 获取热门商品
async function fetchHotProducts() {
  try {
    const res = await getHotProducts({ limit: 8 })
    hotProducts.value = res.data.hotProducts || []
    console.log('热门商品数据:', hotProducts.value)
  } catch (error) {
    console.error('获取热门商品失败:', error)
  }
}

// 获取拼团活动
async function fetchGroupBuyings() {
  try {
    const res = await getActiveGroupBuyings({ limit: 4 })
    groupBuyings.value = res.data || []
    console.log('拼团活动数据:', groupBuyings.value)
  } catch (error) {
    console.error('获取拼团活动失败:', error)
  }
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchIpList()
  fetchHotProducts()
  fetchGroupBuyings()
})
</script>

<style lang="scss" scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;

  .banner-section {
    margin-bottom: 30px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);

    .banner-slide {
      position: relative;
      height: 100%;
      width: 100%;
    }

    .banner-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s;
    }

    .banner-content {
      position: absolute;
      top: 50%;
      left: 60px;
      transform: translateY(-50%);
      color: #fff;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
      max-width: 500px;
      z-index: 2;
      background-color: rgba(0, 0, 0, 0.5);
      padding: 30px;
      border-radius: 15px;
      backdrop-filter: blur(5px);

      h2 {
        font-size: 36px;
        margin-bottom: 15px;
        font-weight: 700;
        letter-spacing: 1px;
      }

      p {
        font-size: 18px;
        margin-bottom: 15px;
        line-height: 1.5;
      }

      .banner-price {
        font-size: 28px;
        font-weight: 700;
        color: #ff6b6b;
        margin-bottom: 20px;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
      }

      .el-button {
        padding: 12px 24px;
        font-size: 16px;
        border-radius: 30px;
        font-weight: 600;
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
        transition: transform 0.3s, box-shadow 0.3s;

        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 6px 16px rgba(64, 158, 255, 0.5);
        }
      }
    }
  }

  .service-section {
    display: flex;
    justify-content: space-between;
    margin-bottom: 40px;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .service-item {
      display: flex;
      align-items: center;
      font-size: 16px;
      color: #606266;

      .el-icon {
        font-size: 24px;
        margin-right: 8px;
        color: #409EFF;
      }
    }
  }

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;

    h2 {
      font-size: 26px;
      position: relative;
      padding-left: 18px;
      font-weight: 600;
      color: #333;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 6px;
        height: 24px;
        background: linear-gradient(to bottom, #409EFF, #36D1DC);
        border-radius: 3px;
      }
    }

    .el-button {
      font-size: 15px;
      transition: color 0.3s;

      &:hover {
        color: #36D1DC;
      }
    }
  }

  .category-section {
    margin-bottom: 50px;
    background-color: #f9fafc;
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

    .category-list {
      display: flex;
      flex-wrap: wrap;
      gap: 25px;

      .category-item {
        width: calc(20% - 20px);
        text-align: center;
        cursor: pointer;
        transition: all 0.3s;
        padding: 15px 10px;
        border-radius: 10px;
        background-color: #fff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);

          .category-icon {
            background: linear-gradient(135deg, #409EFF, #36D1DC);

            .el-icon {
              color: #fff;
            }
          }
        }

        .category-icon {
          width: 70px;
          height: 70px;
          margin: 0 auto 12px;
          background-color: #f0f9ff;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.3s;
          box-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);

          .el-icon {
            font-size: 32px;
            color: #409EFF;
            transition: color 0.3s;
          }
        }

        .category-name {
          font-size: 15px;
          font-weight: 500;
          margin-top: 5px;
          color: #333;
        }
      }
    }
  }

  .hot-products-section {
    margin-bottom: 50px;

    .el-row {
      margin: 0 -10px;

      .el-col {
        padding: 0 10px;
        margin-bottom: 20px;
      }
    }
  }

  .ip-section {
    margin-bottom: 40px;
    background-color: #f9fafc;
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

    .ip-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 20px;

      .ip-item {
        background-color: #fff;
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

          .ip-image img {
            transform: scale(1.05);
          }
        }

        .ip-image {
          height: 160px;
          overflow: hidden;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s;
          }
        }

        .ip-info {
          padding: 16px;

          .ip-name {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 8px;
            color: #303133;
          }

          .ip-desc {
            font-size: 14px;
            color: #909399;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
          }
        }
      }
    }
  }

  .hot-posts-section {
    margin-bottom: 50px;

    .el-row {
      margin: 0 -10px;

      .el-col {
        padding: 0 10px;
        margin-bottom: 20px;
      }
    }
  }
}

@media (max-width: 768px) {
  .home-container {
    .banner-section {
      .banner-content {
        left: 5%;
        max-width: 90%;

        h2 {
          font-size: 24px;
        }

        p {
          font-size: 14px;
        }

        .banner-price {
          font-size: 20px;
        }
      }
    }

    .service-section {
      flex-wrap: wrap;

      .service-item {
        width: 50%;
        margin-bottom: 16px;
      }
    }

    .category-section {
      .category-list {
        .category-item {
          width: calc(33.33% - 14px);
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .home-container {
    .banner-section {
      .banner-content {
        left: 20px;
        padding: 20px;

        h2 {
          font-size: 20px;
        }

        p {
          font-size: 14px;
        }

        .banner-price {
          font-size: 18px;
        }
      }
    }

    .category-section {
      .category-list {
        .category-item {
          width: calc(50% - 10px);
        }
      }
    }

    .ip-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
