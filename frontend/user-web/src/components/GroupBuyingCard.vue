<template>
  <el-card class="group-buying-card" shadow="hover">
    <div class="group-buying-content">
      <div class="product-image">
        <el-image
          :src="formatImageUrl(groupBuying.productImage)"
          fit="cover"
          @click="goToDetail"
        />
        <div class="group-tag">拼团</div>
      </div>
      <div class="product-info">
        <div class="product-name" @click="goToDetail">{{ groupBuying.productName }}</div>
        <div class="price-info">
          <div class="group-price">¥{{ formatPrice(groupBuying.groupPrice) }}</div>
          <div class="original-price">¥{{ formatPrice(groupBuying.productPrice) }}</div>
        </div>
        <div class="group-info">
          <span>{{ groupBuying.minGroupSize }}人团</span>
          <span>已有{{ groupBuying.groupedCount }}人成团</span>
        </div>
        <el-button type="primary" size="small" @click="goToDetail">去拼团</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { formatImageUrl } from '../utils/image'

const props = defineProps({
  groupBuying: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 方法
function formatPrice(price) {
  if (!price || isNaN(price)) return '0.00'
  return parseFloat(price).toFixed(2)
}

function goToDetail() {
  router.push(`/group-buying/${props.groupBuying.id}`)
}
</script>

<style lang="scss" scoped>
.group-buying-card {
  margin-bottom: 20px;
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-5px);
  }

  .group-buying-content {
    display: flex;
    flex-direction: column;

    .product-image {
      position: relative;
      width: 100%;
      height: 200px;
      overflow: hidden;
      cursor: pointer;

      .el-image {
        width: 100%;
        height: 100%;
        transition: transform 0.3s;

        &:hover {
          transform: scale(1.05);
        }
      }

      .group-tag {
        position: absolute;
        top: 10px;
        left: 10px;
        background-color: #f56c6c;
        color: white;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
      }
    }

    .product-info {
      padding: 10px 0;

      .product-name {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        cursor: pointer;

        &:hover {
          color: #409EFF;
        }
      }

      .price-info {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .group-price {
          font-size: 18px;
          font-weight: bold;
          color: #f56c6c;
          margin-right: 8px;
        }

        .original-price {
          font-size: 14px;
          color: #999;
          text-decoration: line-through;
        }
      }

      .group-info {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #666;
        margin-bottom: 10px;
      }
    }
  }
}
</style>
