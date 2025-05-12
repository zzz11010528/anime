<template>
  <div class="product-card" @click="goToDetail">
    <div class="product-image">
      <img :src="formatImageUrl(product.productImage || product.mainImage)" :alt="product.productName || product.name">
      <div v-if="product.isCertified === 1" class="certified-badge">
        <el-icon><Check /></el-icon>
        <span>官方认证</span>
      </div>
    </div>
    <div class="product-info">
      <div class="product-name">{{ product.productName || product.name }}</div>
      <div class="product-price">
        <span class="price">¥{{ formatPrice(product.price) }}</span>
        <span class="sales">已售 {{ product.sales }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Check } from '@element-plus/icons-vue'
import { formatImageUrl } from '../utils/image'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 方法
function goToDetail() {
  router.push(`/product/${props.product.productId || props.product.id}`)
  console.log('跳转到商品详情:', props.product)
}

function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}
</script>

<style lang="scss" scoped>
.product-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  margin-bottom: 20px;
  cursor: pointer;
  border: 1px solid #f0f0f0;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #e6f7ff;

    .product-image img {
      transform: scale(1.05);
    }
  }

  .product-image {
    position: relative;
    height: 200px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s ease;
    }

    .certified-badge {
      position: absolute;
      top: 10px;
      right: 10px;
      background-color: rgba(64, 158, 255, 0.9);
      color: #fff;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
      display: flex;
      align-items: center;

      .el-icon {
        margin-right: 4px;
      }
    }
  }

  .product-info {
    padding: 12px;

    .product-name {
      font-size: 14px;
      color: #333;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      height: 42px;
    }

    .product-price {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price {
        font-size: 16px;
        color: #f56c6c;
        font-weight: bold;
      }

      .sales {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
