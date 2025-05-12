<template>
  <div class="data-overview-container">
    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <div class="filter-header">
        <div class="date-filter">
          <el-radio-group v-model="dateType" size="large" @change="handleDateTypeChange">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="yesterday">昨日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>
          
          <el-date-picker
            v-if="dateType === 'custom'"
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            @change="handleDateRangeChange"
          />
        </div>
        
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
      
      <div class="date-info">
        <span>统计时间：{{ formatDateRange }}</span>
        <span class="update-time">数据更新时间：{{ formatDateTime(updateTime) }}</span>
      </div>
    </el-card>
    
    <!-- 核心指标 -->
    <el-row :gutter="20" class="data-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span class="card-title">销售额</span>
            <el-tooltip content="统计周期内的商品销售总额" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="card-value">¥{{ formatPrice(coreData.salesAmount) }}</div>
          <div class="card-compare">
            <span>
              较上期：
              <span :class="getCompareClass(coreData.salesCompare)">
                <el-icon><component :is="getCompareIcon(coreData.salesCompare)" /></el-icon>
                {{ Math.abs(coreData.salesCompare) }}%
              </span>
            </span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span class="card-title">订单数</span>
            <el-tooltip content="统计周期内的订单总数" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="card-value">{{ coreData.orderCount }}</div>
          <div class="card-compare">
            <span>
              较上期：
              <span :class="getCompareClass(coreData.orderCompare)">
                <el-icon><component :is="getCompareIcon(coreData.orderCompare)" /></el-icon>
                {{ Math.abs(coreData.orderCompare) }}%
              </span>
            </span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span class="card-title">客单价</span>
            <el-tooltip content="平均每个订单的金额" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="card-value">¥{{ formatPrice(coreData.averageOrderAmount) }}</div>
          <div class="card-compare">
            <span>
              较上期：
              <span :class="getCompareClass(coreData.averageOrderCompare)">
                <el-icon><component :is="getCompareIcon(coreData.averageOrderCompare)" /></el-icon>
                {{ Math.abs(coreData.averageOrderCompare) }}%
              </span>
            </span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span class="card-title">访客数</span>
            <el-tooltip content="统计周期内的店铺访客数" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="card-value">{{ coreData.visitorCount }}</div>
          <div class="card-compare">
            <span>
              较上期：
              <span :class="getCompareClass(coreData.visitorCompare)">
                <el-icon><component :is="getCompareIcon(coreData.visitorCompare)" /></el-icon>
                {{ Math.abs(coreData.visitorCompare) }}%
              </span>
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 销售趋势 -->
    <el-card class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>销售趋势</span>
          <el-radio-group v-model="salesChartType" size="small" @change="updateSalesChart">
            <el-radio-button label="amount">销售额</el-radio-button>
            <el-radio-button label="order">订单数</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      
      <div class="chart-container" ref="salesChartRef"></div>
    </el-card>
    
    <!-- 商品销售排行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>商品销售排行</span>
              <el-radio-group v-model="productRankType" size="small" @change="updateProductRank">
                <el-radio-button label="amount">按销售额</el-radio-button>
                <el-radio-button label="quantity">按销量</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <el-table :data="productRankData" style="width: 100%" :max-height="400">
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column label="商品图片" width="80">
              <template #default="{ row }">
                <el-image
                  :src="row.image"
                  :preview-src-list="[row.image]"
                  fit="cover"
                  style="width: 50px; height: 50px"
                />
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
            <el-table-column :prop="productRankType === 'amount' ? 'amount' : 'quantity'" :label="productRankType === 'amount' ? '销售额' : '销量'">
              <template #default="{ row }">
                {{ productRankType === 'amount' ? '¥' + formatPrice(row.amount) : row.quantity }}
              </template>
            </el-table-column>
            <el-table-column label="占比" width="120">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.percentage"
                  :color="getProgressColor(row.percentage)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>销售分布</span>
              <el-radio-group v-model="distributionType" size="small" @change="updateDistributionChart">
                <el-radio-button label="category">按分类</el-radio-button>
                <el-radio-button label="ip">按IP</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <div class="chart-container" ref="distributionChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 订单分析 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>订单状态分布</span>
            </div>
          </template>
          
          <div class="chart-container" ref="orderStatusChartRef"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>支付方式分布</span>
            </div>
          </template>
          
          <div class="chart-container" ref="paymentChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { Refresh, QuestionFilled, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { 
  getOverviewData, getSalesTrend, getProductRank, 
  getDistributionData, getOrderStatusData, getPaymentData 
} from '../../api/data'

// 注册 ECharts 组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  LineChart,
  BarChart,
  PieChart,
  CanvasRenderer
])

// 状态
const dateType = ref('today')
const dateRange = ref([])
const updateTime = ref(new Date())
const coreData = reactive({
  salesAmount: 0,
  salesCompare: 0,
  orderCount: 0,
  orderCompare: 0,
  averageOrderAmount: 0,
  averageOrderCompare: 0,
  visitorCount: 0,
  visitorCompare: 0
})
const salesChartType = ref('amount')
const productRankType = ref('amount')
const distributionType = ref('category')
const productRankData = ref([])

// 图表引用
const salesChartRef = ref(null)
const distributionChartRef = ref(null)
const orderStatusChartRef = ref(null)
const paymentChartRef = ref(null)

// 图表实例
let salesChart = null
let distributionChart = null
let orderStatusChart = null
let paymentChart = null

// 日期快捷选项
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

// 计算属性
const formatDateRange = computed(() => {
  if (dateType.value === 'today') {
    return '今日（' + formatDate(new Date()) + '）'
  } else if (dateType.value === 'yesterday') {
    const yesterday = new Date()
    yesterday.setDate(yesterday.getDate() - 1)
    return '昨日（' + formatDate(yesterday) + '）'
  } else if (dateType.value === 'week') {
    return '本周'
  } else if (dateType.value === 'month') {
    return '本月'
  } else if (dateType.value === 'custom' && dateRange.value && dateRange.value.length === 2) {
    return dateRange.value[0] + ' 至 ' + dateRange.value[1]
  }
  return ''
})

// 方法
function formatPrice(price) {
  return parseFloat(price || 0).toFixed(2)
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

function getCompareClass(value) {
  return value >= 0 ? 'up' : 'down'
}

function getCompareIcon(value) {
  return value >= 0 ? 'ArrowUp' : 'ArrowDown'
}

function getProgressColor(percentage) {
  if (percentage < 30) return '#909399'
  if (percentage < 70) return '#409EFF'
  return '#67C23A'
}

// 处理日期类型变化
function handleDateTypeChange() {
  if (dateType.value !== 'custom') {
    fetchData()
  }
}

// 处理日期范围变化
function handleDateRangeChange() {
  fetchData()
}

// 刷新数据
function refreshData() {
  fetchData()
}

// 获取数据
async function fetchData() {
  try {
    // 获取概览数据
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null
    }
    
    const res = await getOverviewData(params)
    Object.assign(coreData, res.data)
    updateTime.value = new Date()
    
    // 获取其他数据
    fetchSalesTrend()
    fetchProductRank()
    fetchDistributionData()
    fetchOrderStatusData()
    fetchPaymentData()
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

// 获取销售趋势
async function fetchSalesTrend() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      dataType: salesChartType.value
    }
    
    const res = await getSalesTrend(params)
    updateSalesChart(res.data)
  } catch (error) {
    console.error('获取销售趋势失败:', error)
  }
}

// 获取商品排行
async function fetchProductRank() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      rankType: productRankType.value
    }
    
    const res = await getProductRank(params)
    productRankData.value = res.data
  } catch (error) {
    console.error('获取商品排行失败:', error)
  }
}

// 获取分布数据
async function fetchDistributionData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      distributionType: distributionType.value
    }
    
    const res = await getDistributionData(params)
    updateDistributionChart(res.data)
  } catch (error) {
    console.error('获取分布数据失败:', error)
  }
}

// 获取订单状态数据
async function fetchOrderStatusData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null
    }
    
    const res = await getOrderStatusData(params)
    updateOrderStatusChart(res.data)
  } catch (error) {
    console.error('获取订单状态数据失败:', error)
  }
}

// 获取支付方式数据
async function fetchPaymentData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null
    }
    
    const res = await getPaymentData(params)
    updatePaymentChart(res.data)
  } catch (error) {
    console.error('获取支付方式数据失败:', error)
  }
}

// 更新销售趋势图表
function updateSalesChart(data) {
  if (!salesChart) {
    salesChart = echarts.init(salesChartRef.value)
  }
  
  const option = {
    title: {
      text: salesChartType.value === 'amount' ? '销售额趋势' : '订单数趋势'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const param = params[0]
        if (salesChartType.value === 'amount') {
          return param.name + '<br/>' + param.seriesName + ': ¥' + formatPrice(param.value)
        } else {
          return param.name + '<br/>' + param.seriesName + ': ' + param.value
        }
      }
    },
    xAxis: {
      type: 'category',
      data: data.xAxis,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: function(value) {
          if (salesChartType.value === 'amount') {
            return '¥' + formatPrice(value)
          } else {
            return value
          }
        }
      }
    },
    series: [
      {
        name: salesChartType.value === 'amount' ? '销售额' : '订单数',
        type: 'line',
        data: data.series,
        smooth: true,
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
}

// 更新分布图表
function updateDistributionChart(data) {
  if (!distributionChart) {
    distributionChart = echarts.init(distributionChartRef.value)
  }
  
  const option = {
    title: {
      text: distributionType.value === 'category' ? '分类销售分布' : 'IP销售分布',
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
        name: '销售额',
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
        data: data
      }
    ]
  }
  
  distributionChart.setOption(option)
}

// 更新订单状态图表
function updateOrderStatusChart(data) {
  if (!orderStatusChart) {
    orderStatusChart = echarts.init(orderStatusChartRef.value)
  }
  
  const option = {
    title: {
      text: '订单状态分布',
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
        name: '订单数',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  orderStatusChart.setOption(option)
}

// 更新支付方式图表
function updatePaymentChart(data) {
  if (!paymentChart) {
    paymentChart = echarts.init(paymentChartRef.value)
  }
  
  const option = {
    title: {
      text: '支付方式分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: data.map(item => item.value)
      }
    ]
  }
  
  paymentChart.setOption(option)
}

// 处理窗口大小变化
function handleResize() {
  salesChart && salesChart.resize()
  distributionChart && distributionChart.resize()
  orderStatusChart && orderStatusChart.resize()
  paymentChart && paymentChart.resize()
}

// 生命周期钩子
onMounted(() => {
  fetchData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  salesChart && salesChart.dispose()
  distributionChart && distributionChart.dispose()
  orderStatusChart && orderStatusChart.dispose()
  paymentChart && paymentChart.dispose()
})
</script>

<style lang="scss" scoped>
.data-overview-container {
  .filter-card {
    margin-bottom: 20px;
    
    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .date-filter {
        display: flex;
        align-items: center;
        gap: 15px;
      }
    }
    
    .date-info {
      color: #666;
      
      .update-time {
        margin-left: 20px;
        color: #999;
      }
    }
  }
  
  .data-cards {
    margin-bottom: 20px;
    
    .data-card {
      margin-bottom: 20px;
      
      .card-header {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        
        .card-title {
          font-size: 14px;
          margin-right: 5px;
        }
        
        .el-icon {
          font-size: 14px;
          color: #909399;
        }
      }
      
      .card-value {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 10px;
      }
      
      .card-compare {
        font-size: 12px;
        color: #666;
        
        .up {
          color: #67C23A;
        }
        
        .down {
          color: #F56C6C;
        }
      }
    }
  }
  
  .chart-card {
    margin-bottom: 20px;
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .chart-container {
      height: 400px;
    }
  }
  
  .chart-row {
    margin-bottom: 20px;
  }
}
</style>
