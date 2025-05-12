<template>
  <div class="dashboard-container">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <div class="welcome-info">
        <div class="merchant-avatar">
          <el-avatar :src="merchantInfo.logo || defaultAvatar" :size="60"></el-avatar>
        </div>
        <div class="welcome-text">
          <h2>欢迎回来，{{ merchantInfo.merchantName }}</h2>
          <p>今天是 {{ currentDate }}，{{ getGreeting() }}</p>
        </div>
      </div>
      <div class="merchant-status">
        <el-tag :type="getStatusType(merchantInfo.status)">{{ getStatusText(merchantInfo.status) }}</el-tag>
        <span class="expire-info" v-if="merchantInfo.expireDate">
          到期时间：{{ formatDate(merchantInfo.expireDate) }}
        </span>
      </div>
    </div>
    
    <!-- 数据概览 -->
    <el-row :gutter="20" class="data-overview">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">今日订单</span>
            <el-icon class="data-icon" color="#409EFF"><ShoppingCart /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">{{ todayData.orderCount }}</div>
            <div class="data-compare" :class="getCompareClass(todayData.orderCompare)">
              <el-icon><component :is="getCompareIcon(todayData.orderCompare)" /></el-icon>
              <span>{{ Math.abs(todayData.orderCompare) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">今日销售额</span>
            <el-icon class="data-icon" color="#67C23A"><Money /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">¥{{ formatPrice(todayData.salesAmount) }}</div>
            <div class="data-compare" :class="getCompareClass(todayData.salesCompare)">
              <el-icon><component :is="getCompareIcon(todayData.salesCompare)" /></el-icon>
              <span>{{ Math.abs(todayData.salesCompare) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">今日访客</span>
            <el-icon class="data-icon" color="#E6A23C"><View /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">{{ todayData.visitorCount }}</div>
            <div class="data-compare" :class="getCompareClass(todayData.visitorCompare)">
              <el-icon><component :is="getCompareIcon(todayData.visitorCompare)" /></el-icon>
              <span>{{ Math.abs(todayData.visitorCompare) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">转化率</span>
            <el-icon class="data-icon" color="#F56C6C"><DataAnalysis /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">{{ todayData.conversionRate }}%</div>
            <div class="data-compare" :class="getCompareClass(todayData.conversionCompare)">
              <el-icon><component :is="getCompareIcon(todayData.conversionCompare)" /></el-icon>
              <span>{{ Math.abs(todayData.conversionCompare) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 待办事项 -->
    <el-card class="todo-card">
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
          <el-button type="primary" text @click="refreshTodoList">刷新</el-button>
        </div>
      </template>
      
      <el-row :gutter="20" class="todo-list">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, index) in todoList" :key="index">
          <div class="todo-item" @click="handleTodoClick(item)">
            <el-badge :value="item.count" :type="item.type" class="todo-badge" />
            <div class="todo-info">
              <div class="todo-title">{{ item.title }}</div>
              <div class="todo-desc">{{ item.description }}</div>
            </div>
            <el-icon class="todo-arrow"><ArrowRight /></el-icon>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 销售趋势 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
              <div class="chart-actions">
                <el-radio-group v-model="salesChartPeriod" size="small" @change="fetchSalesData">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="year">本年</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          
          <div class="chart-container">
            <div ref="salesChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>商品销售占比</span>
            </div>
          </template>
          
          <div class="chart-container">
            <div ref="productChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最新订单 -->
    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <span>最新订单</span>
          <el-button type="primary" text @click="goToOrderList">查看全部</el-button>
        </div>
      </template>
      
      <el-table :data="latestOrders" style="width: 100%" v-loading="orderLoading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ formatPrice(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">{{ getOrderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" text @click="viewOrderDetail(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 系统公告 -->
    <el-card class="notice-card">
      <template #header>
        <div class="card-header">
          <span>系统公告</span>
        </div>
      </template>
      
      <div v-if="notices.length === 0" class="empty-notice">
        <el-empty description="暂无公告" />
      </div>
      
      <div v-else class="notice-list">
        <div v-for="(notice, index) in notices" :key="index" class="notice-item">
          <div class="notice-title">
            <el-tag v-if="notice.isImportant" type="danger" size="small">重要</el-tag>
            <span>{{ notice.title }}</span>
          </div>
          <div class="notice-content">{{ notice.content }}</div>
          <div class="notice-footer">
            <span class="notice-time">{{ formatDateTime(notice.createdAt) }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ShoppingCart, Money, View, DataAnalysis, 
  ArrowRight, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import { useMerchantStore } from '../../stores/merchant'
import * as echarts from 'echarts/core'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import defaultAvatar from '../../assets/images/default-avatar.png'

// 注册 ECharts 组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  LineChart,
  PieChart,
  CanvasRenderer
])

const router = useRouter()
const merchantStore = useMerchantStore()

// 状态
const salesChartRef = ref(null)
const productChartRef = ref(null)
const salesChartPeriod = ref('week')
const orderLoading = ref(false)

// 图表实例
let salesChart = null
let productChart = null

// 今日数据
const todayData = reactive({
  orderCount: 0,
  orderCompare: 0,
  salesAmount: 0,
  salesCompare: 0,
  visitorCount: 0,
  visitorCompare: 0,
  conversionRate: 0,
  conversionCompare: 0
})

// 待办事项
const todoList = ref([])

// 最新订单
const latestOrders = ref([])

// 系统公告
const notices = ref([])

// 计算属性
const merchantInfo = computed(() => merchantStore.merchantInfo)
const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

// 方法
function getGreeting() {
  const hour = new Date().getHours()
  if (hour < 6) {
    return '凌晨好'
  } else if (hour < 9) {
    return '早上好'
  } else if (hour < 12) {
    return '上午好'
  } else if (hour < 14) {
    return '中午好'
  } else if (hour < 17) {
    return '下午好'
  } else if (hour < 19) {
    return '傍晚好'
  } else {
    return '晚上好'
  }
}

function getStatusType(status) {
  switch (status) {
    case 1: return 'success' // 正常营业
    case 2: return 'warning' // 审核中
    case 3: return 'danger'  // 已冻结
    case 4: return 'info'    // 已过期
    default: return 'info'
  }
}

function getStatusText(status) {
  switch (status) {
    case 1: return '正常营业'
    case 2: return '审核中'
    case 3: return '已冻结'
    case 4: return '已过期'
    default: return '未知状态'
  }
}

function getCompareClass(value) {
  return value >= 0 ? 'up' : 'down'
}

function getCompareIcon(value) {
  return value >= 0 ? 'ArrowUp' : 'ArrowDown'
}

function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${formatDate(d)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getOrderStatusType(status) {
  switch (status) {
    case 1: return 'warning' // 待付款
    case 2: return 'primary' // 待发货
    case 3: return 'success' // 待收货
    case 4: return ''        // 已完成
    case 5: return 'info'    // 已取消
    case 6: return 'danger'  // 退款中
    case 7: return 'info'    // 已退款
    default: return 'info'
  }
}

function getOrderStatusText(status) {
  switch (status) {
    case 1: return '待付款'
    case 2: return '待发货'
    case 3: return '待收货'
    case 4: return '已完成'
    case 5: return '已取消'
    case 6: return '退款中'
    case 7: return '已退款'
    default: return '未知状态'
  }
}

// 获取今日数据
async function fetchTodayData() {
  // TODO: 实现获取今日数据
  // 模拟数据
  todayData.orderCount = 32
  todayData.orderCompare = 12.5
  todayData.salesAmount = 5280.50
  todayData.salesCompare = 8.3
  todayData.visitorCount = 156
  todayData.visitorCompare = -5.2
  todayData.conversionRate = 20.5
  todayData.conversionCompare = 3.8
}

// 获取待办事项
async function fetchTodoList() {
  // TODO: 实现获取待办事项
  // 模拟数据
  todoList.value = [
    { title: '待发货', count: 5, type: 'danger', description: '有5个订单等待发货', path: '/order/list?status=2' },
    { title: '待处理退款', count: 2, type: 'warning', description: '有2个退款申请等待处理', path: '/order/after-sale?status=1' },
    { title: '库存预警', count: 3, type: 'danger', description: '有3个商品库存不足', path: '/product/list?stock=low' },
    { title: '待回复消息', count: 8, type: 'primary', description: '有8条消息等待回复', path: '/message' }
  ]
}

// 刷新待办事项
function refreshTodoList() {
  fetchTodoList()
}

// 处理待办点击
function handleTodoClick(item) {
  router.push(item.path)
}

// 获取销售数据
async function fetchSalesData() {
  // TODO: 实现获取销售数据
  // 模拟数据
  let xData = []
  let yData = []
  
  if (salesChartPeriod.value === 'week') {
    xData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    yData = [1200, 1350, 980, 1400, 1800, 2100, 1750]
  } else if (salesChartPeriod.value === 'month') {
    xData = Array.from({ length: 30 }, (_, i) => `${i + 1}日`)
    yData = Array.from({ length: 30 }, () => Math.floor(Math.random() * 2000 + 500))
  } else {
    xData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    yData = [15000, 12000, 18000, 20000, 17000, 22000, 25000, 28000, 24000, 26000, 30000, 32000]
  }
  
  // 更新图表
  if (salesChart) {
    salesChart.setOption({
      xAxis: {
        data: xData
      },
      series: [{
        data: yData
      }]
    })
  }
}

// 获取商品销售占比
async function fetchProductData() {
  // TODO: 实现获取商品销售占比
  // 模拟数据
  const data = [
    { value: 40, name: '手办模型' },
    { value: 25, name: '服饰' },
    { value: 15, name: '文具' },
    { value: 10, name: '海报挂画' },
    { value: 10, name: '其他周边' }
  ]
  
  // 更新图表
  if (productChart) {
    productChart.setOption({
      series: [{
        data: data
      }]
    })
  }
}

// 获取最新订单
async function fetchLatestOrders() {
  orderLoading.value = true
  try {
    // TODO: 实现获取最新订单
    // 模拟数据
    latestOrders.value = [
      { id: 1, orderNo: 'DD20230710001', customerName: '张三', totalAmount: 299.00, status: 2, createdAt: '2023-07-10 15:30:00' },
      { id: 2, orderNo: 'DD20230710002', customerName: '李四', totalAmount: 158.50, status: 1, createdAt: '2023-07-10 14:25:00' },
      { id: 3, orderNo: 'DD20230710003', customerName: '王五', totalAmount: 520.00, status: 3, createdAt: '2023-07-10 13:10:00' },
      { id: 4, orderNo: 'DD20230710004', customerName: '赵六', totalAmount: 99.90, status: 4, createdAt: '2023-07-10 11:45:00' },
      { id: 5, orderNo: 'DD20230710005', customerName: '钱七', totalAmount: 1280.00, status: 2, createdAt: '2023-07-10 10:20:00' }
    ]
  } finally {
    orderLoading.value = false
  }
}

// 获取系统公告
async function fetchNotices() {
  // TODO: 实现获取系统公告
  // 模拟数据
  notices.value = [
    { 
      title: '平台升级通知', 
      content: '系统将于2023年7月15日凌晨2:00-4:00进行升级维护，期间商家后台将无法访问，请提前做好准备。', 
      isImportant: true, 
      createdAt: '2023-07-09 10:00:00' 
    },
    { 
      title: '新功能上线通知', 
      content: '商家后台新增了数据分析功能，可以更直观地查看店铺销售情况和客户分析，欢迎体验使用。', 
      isImportant: false, 
      createdAt: '2023-07-08 14:30:00' 
    }
  ]
}

// 查看订单详情
function viewOrderDetail(orderId) {
  router.push(`/order/detail/${orderId}`)
}

// 跳转到订单列表
function goToOrderList() {
  router.push('/order/list')
}

// 初始化销售趋势图表
function initSalesChart() {
  if (salesChart) {
    salesChart.dispose()
  }
  
  salesChart = echarts.init(salesChartRef.value)
  
  const option = {
    title: {
      text: '销售趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: [],
        areaStyle: {
          opacity: 0.3
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  salesChart.setOption(option)
  
  // 获取销售数据
  fetchSalesData()
}

// 初始化商品销售占比图表
function initProductChart() {
  if (productChart) {
    productChart.dispose()
  }
  
  productChart = echarts.init(productChartRef.value)
  
  const option = {
    title: {
      text: '商品销售占比',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '销售占比',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: []
      }
    ]
  }
  
  productChart.setOption(option)
  
  // 获取商品销售占比数据
  fetchProductData()
}

// 处理窗口大小变化
function handleResize() {
  salesChart && salesChart.resize()
  productChart && productChart.resize()
}

// 生命周期钩子
onMounted(() => {
  fetchTodayData()
  fetchTodoList()
  fetchLatestOrders()
  fetchNotices()
  
  // 初始化图表
  setTimeout(() => {
    initSalesChart()
    initProductChart()
  }, 0)
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  .welcome-section {
    background-color: #fff;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .welcome-info {
      display: flex;
      align-items: center;
      
      .merchant-avatar {
        margin-right: 15px;
      }
      
      .welcome-text {
        h2 {
          margin: 0 0 5px 0;
          font-size: 18px;
        }
        
        p {
          margin: 0;
          color: #666;
        }
      }
    }
    
    .merchant-status {
      text-align: right;
      
      .expire-info {
        display: block;
        margin-top: 5px;
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .data-overview {
    margin-bottom: 20px;
    
    .data-card {
      height: 120px;
      margin-bottom: 20px;
      
      .data-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        
        .data-title {
          font-size: 14px;
          color: #666;
        }
        
        .data-icon {
          font-size: 24px;
        }
      }
      
      .data-content {
        display: flex;
        align-items: baseline;
        
        .data-value {
          font-size: 24px;
          font-weight: bold;
          margin-right: 10px;
        }
        
        .data-compare {
          display: flex;
          align-items: center;
          font-size: 12px;
          
          &.up {
            color: #67C23A;
          }
          
          &.down {
            color: #F56C6C;
          }
          
          .el-icon {
            margin-right: 2px;
          }
        }
      }
    }
  }
  
  .todo-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .todo-list {
      .todo-item {
        background-color: #f5f7fa;
        border-radius: 4px;
        padding: 15px;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background-color: #ecf5ff;
        }
        
        .todo-badge {
          margin-right: 15px;
        }
        
        .todo-info {
          flex: 1;
          
          .todo-title {
            font-weight: bold;
            margin-bottom: 5px;
          }
          
          .todo-desc {
            font-size: 12px;
            color: #666;
          }
        }
        
        .todo-arrow {
          color: #909399;
        }
      }
    }
  }
  
  .chart-section {
    margin-bottom: 20px;
    
    .chart-card {
      margin-bottom: 20px;
      
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
    }
  }
  
  .order-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .notice-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .empty-notice {
      padding: 20px 0;
    }
    
    .notice-list {
      .notice-item {
        padding: 15px 0;
        border-bottom: 1px solid #eee;
        
        &:last-child {
          border-bottom: none;
        }
        
        .notice-title {
          font-weight: bold;
          margin-bottom: 10px;
          
          .el-tag {
            margin-right: 5px;
          }
        }
        
        .notice-content {
          color: #666;
          margin-bottom: 10px;
          line-height: 1.5;
        }
        
        .notice-footer {
          font-size: 12px;
          color: #999;
          text-align: right;
        }
      }
    }
  }
}
</style>
