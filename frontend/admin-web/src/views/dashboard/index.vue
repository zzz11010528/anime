<template>
  <div class="dashboard-container">
    <!-- 数据概览 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="box-card">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">用户总数</div>
              <div class="stat-value">{{ overview.userCount || 0 }}</div>
              <div class="stat-desc">
                <span class="stat-change" :class="{ 'up': overview.userIncrease > 0, 'down': overview.userIncrease < 0 }">
                  {{ overview.userIncrease > 0 ? '+' : '' }}{{ overview.userIncrease || 0 }}
                </span>
                较昨日
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">订单总数</div>
              <div class="stat-value">{{ overview.orderCount || 0 }}</div>
              <div class="stat-desc">
                <span class="stat-change" :class="{ 'up': overview.orderIncrease > 0, 'down': overview.orderIncrease < 0 }">
                  {{ overview.orderIncrease > 0 ? '+' : '' }}{{ overview.orderIncrease || 0 }}
                </span>
                较昨日
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">销售总额</div>
              <div class="stat-value">¥{{ overview.salesAmount || 0 }}</div>
              <div class="stat-desc">
                <span class="stat-change" :class="{ 'up': overview.salesIncrease > 0, 'down': overview.salesIncrease < 0 }">
                  {{ overview.salesIncrease > 0 ? '+' : '' }}{{ overview.salesIncrease || 0 }}
                </span>
                较昨日
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">帖子总数</div>
              <div class="stat-value">{{ overview.postCount || 0 }}</div>
              <div class="stat-desc">
                <span class="stat-change" :class="{ 'up': overview.postIncrease > 0, 'down': overview.postIncrease < 0 }">
                  {{ overview.postIncrease > 0 ? '+' : '' }}{{ overview.postIncrease || 0 }}
                </span>
                较昨日
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活跃用户 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>活跃用户</span>
              <el-button type="primary" link @click="refreshActiveUsers">刷新</el-button>
            </div>
          </template>
          <div class="hot-list">
            <el-table :data="activeUsers" style="width: 100%">
              <el-table-column label="排名" width="80">
                <template #default="scope">
                  <div class="rank-item">
                    <span class="rank-number" :class="{ 'top3': scope.$index < 3 }">{{ scope.$index + 1 }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="nickname" label="昵称" />
              <el-table-column prop="activityCount" label="活动次数" width="100" />
              <el-table-column prop="lastActiveTime" label="最后活动时间" width="180" />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门数据 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>热门商品</span>
              <el-button type="primary" link @click="refreshHotProducts">刷新</el-button>
            </div>
          </template>
          <div class="hot-list">
            <el-table :data="hotProducts" style="width: 100%">
              <el-table-column label="排名" width="80">
                <template #default="scope">
                  <div class="rank-item">
                    <span class="rank-number" :class="{ 'top3': scope.$index < 3 }">{{ scope.$index + 1 }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="商品图片" width="80">
                <template #default="scope">
                  <el-image
                    v-if="scope.row.productImage"
                    :src="scope.row.productImage"
                    fit="cover"
                    style="width: 50px; height: 50px"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
              <el-table-column prop="sales" label="销量" width="80" />
              <el-table-column prop="amount" label="销售额" width="100">
                <template #default="scope">
                  ¥{{ scope.row.amount }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>热门帖子</span>
              <el-button type="primary" link @click="refreshHotPosts">刷新</el-button>
            </div>
          </template>
          <div class="hot-list">
            <el-table :data="hotPosts" style="width: 100%">
              <el-table-column label="排名" width="80">
                <template #default="scope">
                  <div class="rank-item">
                    <span class="rank-number" :class="{ 'top3': scope.$index < 3 }">{{ scope.$index + 1 }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" show-overflow-tooltip />
              <el-table-column prop="viewCount" label="浏览量" width="100" />
              <el-table-column prop="likeCount" label="点赞数" width="100" />
              <el-table-column prop="commentCount" label="评论数" width="100" />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品销售统计 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>商品销售统计</span>
              <div>
                <el-date-picker
                  v-model="salesDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :shortcuts="dateShortcuts"
                />
                <el-button type="primary" @click="fetchProductSalesStat" style="margin-left: 10px;">查询</el-button>
              </div>
            </div>
          </template>
          <div class="sales-stat">
            <el-descriptions :column="4" border>
              <el-descriptions-item label="总销售额">¥{{ productSalesStat.totalAmount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="总订单数">{{ productSalesStat.totalOrders || 0 }}</el-descriptions-item>
              <el-descriptions-item label="总销量">{{ productSalesStat.totalSales || 0 }}</el-descriptions-item>
              <el-descriptions-item label="平均客单价">¥{{ productSalesStat.averageOrderAmount || 0 }}</el-descriptions-item>
            </el-descriptions>

            <div class="sales-table" style="margin-top: 20px;">
              <el-table :data="productSalesStat.products || []" style="width: 100%">
                <el-table-column prop="productName" label="商品名称" />
                <el-table-column prop="sales" label="销量" width="100" />
                <el-table-column prop="amount" label="销售额" width="120">
                  <template #default="scope">
                    ¥{{ scope.row.amount }}
                  </template>
                </el-table-column>
                <el-table-column prop="orderCount" label="订单数" width="100" />
              </el-table>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { User, ShoppingCart, Money, ChatDotRound } from '@element-plus/icons-vue'
import { getSystemOverview, getHotProducts, getHotPosts, getActiveUsers, getProductSalesStat } from '../../api/stat'

// 数据概览
const overview = reactive({
  userCount: 0,
  userIncrease: 0,
  orderCount: 0,
  orderIncrease: 0,
  salesAmount: 0,
  salesIncrease: 0,
  postCount: 0,
  postIncrease: 0
})

// 热门数据
const hotProducts = ref([])
const hotPosts = ref([])
const activeUsers = ref([])

// 商品销售统计
const productSalesStat = reactive({
  totalAmount: 0,
  totalOrders: 0,
  totalSales: 0,
  averageOrderAmount: 0,
  products: []
})

// 日期选择器
const salesDateRange = ref([])
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 方法
function fetchSystemOverview() {
  getSystemOverview().then(res => {
    Object.assign(overview, res.data)
  }).catch(err => {
    console.error('获取系统概览失败:', err)
  })
}

function fetchHotProducts() {
  getHotProducts({ limit: 10 }).then(res => {
    hotProducts.value = res.data.hotProducts || []
  }).catch(err => {
    console.error('获取热门商品失败:', err)
  })
}

function fetchHotPosts() {
  getHotPosts({ limit: 10 }).then(res => {
    hotPosts.value = res.data.hotPosts || []
  }).catch(err => {
    console.error('获取热门帖子失败:', err)
  })
}

function fetchActiveUsers() {
  getActiveUsers({ limit: 10 }).then(res => {
    activeUsers.value = res.data.activeUsers || []
  }).catch(err => {
    console.error('获取活跃用户失败:', err)
  })
}

function fetchProductSalesStat() {
  const params = {}
  if (salesDateRange.value && salesDateRange.value.length === 2) {
    params.startDate = salesDateRange.value[0]
    params.endDate = salesDateRange.value[1]
  }

  getProductSalesStat(params).then(res => {
    Object.assign(productSalesStat, res.data)
  }).catch(err => {
    console.error('获取商品销售统计失败:', err)
  })
}

function refreshHotProducts() {
  fetchHotProducts()
}

function refreshHotPosts() {
  fetchHotPosts()
}

function refreshActiveUsers() {
  fetchActiveUsers()
}

// 生命周期钩子
onMounted(() => {
  fetchSystemOverview()
  fetchHotProducts()
  fetchHotPosts()
  fetchActiveUsers()

  // 设置默认日期范围为最近30天
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
  salesDateRange.value = [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0]
  ]

  // 获取商品销售统计
  fetchProductSalesStat()
})

onBeforeUnmount(() => {
  // 清理工作
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;

  .stat-card {
    display: flex;
    align-items: center;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background-color: #ecf5ff;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-right: 15px;

      .el-icon {
        font-size: 30px;
        color: #409EFF;
      }
    }

    .stat-info {
      flex: 1;

      .stat-title {
        font-size: 14px;
        color: #909399;
        margin-bottom: 5px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 5px;
      }

      .stat-desc {
        font-size: 12px;
        color: #909399;

        .stat-change {
          &.up {
            color: #67C23A;
          }

          &.down {
            color: #F56C6C;
          }
        }
      }
    }
  }

  .chart-row {
    margin-top: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chart-container {
    height: 300px;

    .chart {
      width: 100%;
      height: 100%;
    }
  }

  .hot-list {
    .rank-item {
      display: flex;
      align-items: center;

      .rank-number {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background-color: #909399;
        color: #fff;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 14px;

        &.top3 {
          background-color: #E6A23C;
        }
      }
    }
  }
}
</style>
