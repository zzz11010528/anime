<template>
  <div class="product-list-container">
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-section">
        <div class="filter-item">
          <div class="filter-label">分类：</div>
          <div class="filter-options">
            <el-radio-group v-model="filterParams.categoryId" @change="handleFilterChange">
              <el-radio :label="null">全部</el-radio>
              <el-radio v-for="category in categories" :key="category.id" :label="category.id">{{ category.name }}</el-radio>
            </el-radio-group>
          </div>
        </div>

        <div class="filter-item">
          <div class="filter-label">IP：</div>
          <div class="filter-options">
            <el-radio-group v-model="filterParams.ipId" @change="handleFilterChange">
              <el-radio :label="null">全部</el-radio>
              <el-radio v-for="ip in ipList" :key="ip.id" :label="ip.id">{{ ip.name }}</el-radio>
            </el-radio-group>
          </div>
        </div>

        <div class="filter-item">
          <div class="filter-label">价格：</div>
          <div class="filter-options">
            <el-radio-group v-model="filterParams.priceRange" @change="handleFilterChange">
              <el-radio :label="null">全部</el-radio>
              <el-radio :label="'0-50'">0-50元</el-radio>
              <el-radio :label="'50-100'">50-100元</el-radio>
              <el-radio :label="'100-200'">100-200元</el-radio>
              <el-radio :label="'200-500'">200-500元</el-radio>
              <el-radio :label="'500-'">500元以上</el-radio>
            </el-radio-group>
          </div>
        </div>

        <div class="filter-item">
          <div class="filter-label">排序：</div>
          <div class="filter-options">
            <el-radio-group v-model="filterParams.orderBy" @change="handleFilterChange">
              <el-radio :label="'default'">默认</el-radio>
              <el-radio :label="'sales'">销量</el-radio>
              <el-radio :label="'price-asc'">价格从低到高</el-radio>
              <el-radio :label="'price-desc'">价格从高到低</el-radio>
              <el-radio :label="'newest'">最新上架</el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>

      <div class="search-section">
        <el-input
          v-model="filterParams.keyword"
          placeholder="搜索商品"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #suffix>
            <el-button :icon="Search" circle @click="handleSearch" />
          </template>
        </el-input>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <div class="product-list">
      <el-empty v-if="products.length === 0" description="暂无商品" />

      <el-row v-else :gutter="20">
        <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <product-card :product="product" />
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getProductList, getCategoryList } from '../../api/product'
import { getIpList } from '../../api/ip'
import ProductCard from '../../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

// 状态
const products = ref([])
const categories = ref([])
const ipList = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)
const loading = ref(false)

// 筛选参数
const filterParams = reactive({
  keyword: '',
  categoryId: null,
  ipId: null,
  priceRange: null,
  orderBy: 'default'
})

// 监听路由参数变化
watch(
  () => route.query,
  (query) => {
    // 从URL参数更新筛选条件
    filterParams.keyword = query.keyword || ''
    filterParams.categoryId = query.categoryId ? Number(query.categoryId) : null
    filterParams.ipId = query.ipId ? Number(query.ipId) : null
    filterParams.priceRange = query.priceRange || null
    filterParams.orderBy = query.orderBy || 'default'

    // 更新分页
    page.value = Number(query.page) || 1
    size.value = Number(query.size) || 12

    // 获取商品列表
    fetchProducts()
  },
  { immediate: true }
)

// 方法
async function fetchProducts() {
  loading.value = true

  try {
    // 构建请求参数
    const params = {
      page: page.value,
      size: size.value,
      keyword: filterParams.keyword,
      categoryId: filterParams.categoryId,
      ipId: filterParams.ipId
    }

    // 处理价格范围
    if (filterParams.priceRange) {
      const [min, max] = filterParams.priceRange.split('-')
      params.minPrice = min
      if (max) {
        params.maxPrice = max
      }
    }

    // 处理排序
    if (filterParams.orderBy === 'sales') {
      params.orderBy = 'sales'
      params.orderType = 'desc'
    } else if (filterParams.orderBy === 'price-asc') {
      params.orderBy = 'price'
      params.orderType = 'asc'
    } else if (filterParams.orderBy === 'price-desc') {
      params.orderBy = 'price'
      params.orderType = 'desc'
    } else if (filterParams.orderBy === 'newest') {
      params.orderBy = 'created_at'
      params.orderType = 'desc'
    }

    const res = await getProductList(params)
    products.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
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

// 处理筛选条件变化
function handleFilterChange() {
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 处理搜索
function handleSearch() {
  page.value = 1 // 重置页码
  updateRouteQuery()
}

// 处理分页大小变化
function handleSizeChange() {
  updateRouteQuery()
}

// 处理页码变化
function handleCurrentChange() {
  updateRouteQuery()
}

// 更新路由查询参数
function updateRouteQuery() {
  const query = {}

  if (filterParams.keyword) {
    query.keyword = filterParams.keyword
  }

  if (filterParams.categoryId) {
    query.categoryId = filterParams.categoryId
  }

  if (filterParams.ipId) {
    query.ipId = filterParams.ipId
  }

  if (filterParams.priceRange) {
    query.priceRange = filterParams.priceRange
  }

  if (filterParams.orderBy !== 'default') {
    query.orderBy = filterParams.orderBy
  }

  if (page.value > 1) {
    query.page = page.value
  }

  if (size.value !== 12) {
    query.size = size.value
  }

  router.push({ path: '/product', query })
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchIpList()
})
</script>

<style lang="scss" scoped>
.product-list-container {
  max-width: 1200px;
  margin: 0 auto;

  .filter-card {
    margin-bottom: 20px;

    .filter-section {
      .filter-item {
        display: flex;
        margin-bottom: 15px;

        .filter-label {
          width: 60px;
          font-weight: bold;
          color: #333;
          line-height: 32px;
        }

        .filter-options {
          flex: 1;

          .el-radio {
            margin-right: 15px;
            margin-bottom: 10px;
          }
        }
      }
    }

    .search-section {
      display: flex;
      justify-content: flex-end;
      margin-top: 15px;

      .el-input {
        width: 300px;
      }
    }
  }

  .product-list {
    .pagination {
      margin-top: 30px;
      text-align: center;
    }
  }
}

@media (max-width: 768px) {
  .product-list-container {
    .filter-card {
      .filter-section {
        .filter-item {
          flex-direction: column;

          .filter-label {
            width: 100%;
            margin-bottom: 5px;
          }
        }
      }

      .search-section {
        .el-input {
          width: 100%;
        }
      }
    }
  }
}
</style>
