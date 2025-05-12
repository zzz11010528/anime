<template>
  <div class="cart-container">
    <el-card class="cart-card">
      <template #header>
        <div class="cart-header">
          <h2>我的购物车</h2>
          <el-button type="primary" @click="goToShopping" v-if="cartItems.length === 0">
            去购物
          </el-button>
        </div>
      </template>

      <div v-if="cartLoading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <el-empty v-else-if="cartItems.length === 0" description="购物车空空如也" />

      <div v-else class="cart-content">
        <div class="cart-table">
          <el-table
            ref="cartTableRef"
            :data="cartItems"
            style="width: 100%"
            row-key="id"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />

            <el-table-column label="商品信息" min-width="400">
              <template #default="{ row }">
                <div class="product-info">
                  <el-image :src="formatImageUrl(row.productImage)" :alt="row.productName" class="product-image" @click="goToProduct(row.productId)" />
                  <div class="product-details">
                    <div class="product-name" @click="goToProduct(row.productId)">{{ row.productName }}</div>
                    <div class="product-attrs" v-if="row.attrs">
                      <el-tag v-for="(attr, index) in row.attrs" :key="index" size="small">
                        {{ attr.name }}: {{ attr.value }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <div class="product-price">¥{{ formatPrice(row.price) }}</div>
              </template>
            </el-table-column>

            <el-table-column label="数量" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="row.stock"
                  size="small"
                  @change="(value) => handleQuantityChange(row.id, value)"
                />
              </template>
            </el-table-column>

            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                <div class="product-subtotal">¥{{ formatPrice(row.price * row.quantity) }}</div>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="danger" text @click="handleRemove(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="cart-footer">
          <div class="cart-actions">
            <el-checkbox v-model="selectAll" @change="handleSelectAllChange">全选</el-checkbox>
            <el-button type="danger" text @click="handleBatchRemove" :disabled="selectedItems.length === 0">
              删除选中
            </el-button>
            <el-button type="primary" text @click="handleClearInvalid" v-if="hasInvalidItems">
              清除失效商品
            </el-button>
          </div>

          <div class="cart-summary">
            <div class="summary-item">
              已选择 <span class="highlight">{{ selectedCount }}</span> 件商品
            </div>
            <div class="summary-item">
              合计: <span class="highlight price">¥{{ formatPrice(totalPrice) }}</span>
            </div>
            <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedCount === 0">
              去结算
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 猜你喜欢 -->
    <el-card class="recommend-card" v-if="recommendProducts.length > 0">
      <template #header>
        <div class="card-header">
          <span>猜你喜欢</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col v-for="product in recommendProducts" :key="product.id" :xs="12" :sm="8" :md="6" :lg="4">
          <product-card :product="product" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { debounce } from 'lodash-es'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '../../stores/cart'
import { getHotProducts } from '../../api/product'
import ProductCard from '../../components/ProductCard.vue'
import { formatImageUrl } from '../../utils/image'

const router = useRouter()
const cartStore = useCartStore()
const cartTableRef = ref(null)

// 状态
const selectedItems = ref([])
const selectAll = ref(false)
const recommendProducts = ref([])

// 计算属性
const cartItems = computed(() => cartStore.cartItems)
const cartLoading = computed(() => cartStore.cartLoading)
const selectedCount = computed(() => selectedItems.value.length)
const totalPrice = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0)
})
const hasInvalidItems = computed(() => {
  return cartItems.value.some(item => item.stock <= 0 || item.status === 0)
})

// 方法
function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

function goToShopping() {
  router.push('/product')
}

function goToProduct(productId) {
  router.push(`/product/${productId}`)
}

async function handleQuantityChange(id, quantity) {
  try {
    await cartStore.updateItemQuantity(id, quantity)
  } catch (error) {
    ElMessage.error(error.message || '更新数量失败')
  }
}

// 防抖函数，避免频繁发送请求
const updateSelectionDebounced = debounce(async (items) => {
  // 获取当前选中的商品ID
  const selectedIds = items.map(item => item.id)

  // 更新每个商品的选中状态
  const updatePromises = []

  for (const item of cartItems.value) {
    const isSelected = selectedIds.includes(item.id)
    // 如果选中状态发生变化，则更新后端
    if ((isSelected && item.selected === 0) || (!isSelected && item.selected === 1)) {
      updatePromises.push(cartStore.updateItemSelected(item.id, isSelected ? 1 : 0))
    }
  }

  if (updatePromises.length > 0) {
    try {
      await Promise.all(updatePromises)
      // 更新完成后重新获取购物车列表
      await cartStore.fetchCartList()

      // 重新设置表格选中状态
      if (cartTableRef.value) {
        // 先清除所有选中
        cartTableRef.value.clearSelection()

        // 根据后端返回的数据设置选中状态
        cartItems.value.forEach(row => {
          if (row.selected === 1) {
            cartTableRef.value.toggleRowSelection(row, true)
          }
        })

        // 更新selectedItems数组
        selectedItems.value = cartItems.value.filter(item => item.selected === 1)
      }
    } catch (error) {
      console.error('更新选中状态失败:', error)
      ElMessage.error('更新选中状态失败')
    }
  }
}, 300)

function handleSelectionChange(items) {
  // 更新选中的商品列表
  selectedItems.value = items

  // 更新全选状态
  selectAll.value = items.length === cartItems.value.length && cartItems.value.length > 0

  // 使用防抖函数更新选中状态
  updateSelectionDebounced(items)

  // 打印调试信息
  console.log('选中商品数量:', items.length)
  console.log('选中商品总价:', items.reduce((total, item) => total + item.price * item.quantity, 0))
}

async function handleSelectAllChange(val) {
  try {
    await cartStore.selectAll(val ? 1 : 0)
    // 重新获取购物车列表
    await cartStore.fetchCartList()

    // 手动更新表格选中状态
    if (cartTableRef.value) {
      // 先清除所有选中
      cartTableRef.value.clearSelection()

      // 如果是全选，则选中所有行
      if (val) {
        cartItems.value.forEach(row => {
          if (row.selected === 1) {
            cartTableRef.value.toggleRowSelection(row, true)
          }
        })
      }

      // 更新selectedItems数组
      if (val) {
        // 全选时，将所有selected=1的商品加入selectedItems
        selectedItems.value = cartItems.value.filter(item => item.selected === 1)
      } else {
        // 取消全选时，清空selectedItems
        selectedItems.value = []
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
    // 恢复复选框状态
    selectAll.value = !val
  }
}

async function handleRemove(id) {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 删除商品
      await cartStore.removeCartItem(id)

      // 从选中的商品中移除
      selectedItems.value = selectedItems.value.filter(item => item.id !== id)

      // 更新全选状态
      if (cartItems.value.length > 0) {
        selectAll.value = selectedItems.value.length === cartItems.value.length - 1
      } else {
        selectAll.value = false
      }

      ElMessage.success('删除成功')
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

async function handleBatchRemove() {
  if (selectedItems.value.length === 0) return

  ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.value.length} 件商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 保存要删除的商品ID
      const itemsToDelete = [...selectedItems.value]

      // 删除选中的商品
      const promises = itemsToDelete.map(item => cartStore.removeCartItem(item.id))
      await Promise.all(promises)

      // 清空选中的商品
      selectedItems.value = []

      // 更新全选状态
      selectAll.value = false

      ElMessage.success('删除成功')
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error(error.message || '删除失败')

      // 出错时重新获取购物车列表
      await cartStore.fetchCartList()
    }
  }).catch(() => {})
}

async function handleClearInvalid() {
  const invalidItems = cartItems.value.filter(item => item.stock <= 0 || item.status === 0)
  if (invalidItems.length === 0) return

  ElMessageBox.confirm(`确定要清除 ${invalidItems.length} 件失效商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 保存要删除的商品ID
      const itemsToDelete = [...invalidItems]

      // 删除失效商品
      const promises = itemsToDelete.map(item => cartStore.removeCartItem(item.id))
      await Promise.all(promises)

      // 从选中的商品中移除已删除的商品
      const invalidIds = itemsToDelete.map(item => item.id)
      selectedItems.value = selectedItems.value.filter(item => !invalidIds.includes(item.id))

      // 更新全选状态
      if (cartItems.value.length > invalidItems.length) {
        selectAll.value = selectedItems.value.length === cartItems.value.length - invalidItems.length
      } else {
        selectAll.value = false
      }

      ElMessage.success('清除成功')
    } catch (error) {
      console.error('清除失效商品失败:', error)
      ElMessage.error(error.message || '清除失败')

      // 出错时重新获取购物车列表
      await cartStore.fetchCartList()
    }
  }).catch(() => {})
}

function handleCheckout() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  // 跳转到订单确认页面
  router.push({
    path: '/order/confirm',
    query: {
      from: 'cart'
    }
  })
}

// 获取推荐商品
async function fetchRecommendProducts() {
  try {
    const res = await getHotProducts({ limit: 6 })
    recommendProducts.value = res.data.hotProducts || []
  } catch (error) {
    console.error('获取推荐商品失败:', error)
  }
}

// 生命周期钩子
onMounted(async () => {
  await cartStore.fetchCartList()
  fetchRecommendProducts()

  // 设置初始选中状态
  setTimeout(() => {
    if (cartTableRef.value && cartItems.value.length > 0) {
      // 选中已选中的商品
      cartItems.value.forEach(row => {
        if (row.selected === 1) {
          cartTableRef.value.toggleRowSelection(row, true)
        }
      })

      // 更新全选状态
      const selectedCount = cartItems.value.filter(item => item.selected === 1).length
      selectAll.value = selectedCount === cartItems.value.length && cartItems.value.length > 0
    }
  }, 100) // 延迟一点时间，确保表格已经渲染完成
})
</script>

<style lang="scss" scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;

  .cart-card {
    margin-bottom: 20px;

    .cart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        font-size: 20px;
      }
    }

    .loading {
      padding: 20px 0;
    }

    .cart-content {
      .product-info {
        display: flex;
        align-items: center;

        .product-image {
          width: 80px;
          height: 80px;
          object-fit: cover;
          margin-right: 15px;
          cursor: pointer;
        }

        .product-details {
          .product-name {
            font-size: 14px;
            margin-bottom: 8px;
            cursor: pointer;

            &:hover {
              color: #409EFF;
            }
          }

          .product-attrs {
            .el-tag {
              margin-right: 5px;
              margin-bottom: 5px;
            }
          }
        }
      }

      .product-price,
      .product-subtotal {
        font-weight: bold;
      }

      .product-subtotal {
        color: #f56c6c;
      }

      .cart-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #eee;

        .cart-actions {
          display: flex;
          align-items: center;
          gap: 15px;
        }

        .cart-summary {
          display: flex;
          align-items: center;
          gap: 20px;

          .summary-item {
            .highlight {
              font-weight: bold;
              color: #f56c6c;

              &.price {
                font-size: 20px;
              }
            }
          }
        }
      }
    }
  }

  .recommend-card {
    .card-header {
      font-size: 18px;
      font-weight: bold;
    }
  }
}

@media (max-width: 768px) {
  .cart-container {
    .cart-card {
      .cart-content {
        .cart-footer {
          flex-direction: column;
          gap: 15px;

          .cart-actions,
          .cart-summary {
            width: 100%;
            justify-content: space-between;
          }
        }
      }
    }
  }
}
</style>
