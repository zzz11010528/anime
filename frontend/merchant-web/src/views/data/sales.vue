<template>
  <div class="sales-analysis-container">
    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <div class="filter-header">
        <div class="date-filter">
          <el-radio-group v-model="dateType" size="large" @change="handleDateTypeChange">
            <el-radio-button label="week">近7天</el-radio-button>
            <el-radio-button label="month">近30天</el-radio-button>
            <el-radio-button label="quarter">近90天</el-radio-button>
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
    
    <!-- 销售概览 -->
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span>销售概览</span>
          <el-button type="primary" link @click="exportSalesData">导出数据</el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, index) in overviewData" :key="index">
          <div class="overview-item">
            <div class="item-label">{{ item.label }}</div>
            <div class="item-value">{{ item.prefix || '' }}{{ formatValue(item.value, item.type) }}{{ item.suffix || '' }}</div>
            <div class="item-compare" v-if="item.compare !== undefined">
              <span :class="getCompareClass(item.compare)">
                <el-icon><component :is="getCompareIcon(item.compare)" /></el-icon>
                {{ Math.abs(item.compare) }}%
              </span>
              <span class="compare-text">较上期</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 销售趋势 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>销售趋势</span>
          <div class="chart-actions">
            <el-radio-group v-model="trendType" size="small" @change="updateTrendChart">
              <el-radio-button label="day">按日</el-radio-button>
              <el-radio-button label="week">按周</el-radio-button>
              <el-radio-button label="month">按月</el-radio-button>
            </el-radio-group>
            
            <el-select v-model="trendDataType" placeholder="数据类型" style="width: 120px; margin-left: 15px" @change="updateTrendChart">
              <el-option label="销售额" value="amount" />
              <el-option label="订单数" value="order" />
              <el-option label="客单价" value="average" />
            </el-select>
          </div>
        </div>
      </template>
      
      <div class="chart-container" ref="trendChartRef"></div>
    </el-card>
    
    <!-- 销售分布 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销售分类占比</span>
              <el-radio-group v-model="categoryDataType" size="small" @change="updateCategoryChart">
                <el-radio-button label="amount">按销售额</el-radio-button>
                <el-radio-button label="order">按订单数</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <div class="chart-container" ref="categoryChartRef"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销售IP占比</span>
              <el-radio-group v-model="ipDataType" size="small" @change="updateIpChart">
                <el-radio-button label="amount">按销售额</el-radio-button>
                <el-radio-button label="order">按订单数</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <div class="chart-container" ref="ipChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 销售排行 -->
    <el-card class="rank-card">
      <template #header>
        <div class="card-header">
          <span>商品销售排行</span>
          <div class="rank-actions">
            <el-radio-group v-model="rankType" size="small" @change="fetchRankData">
              <el-radio-button label="amount">按销售额</el-radio-button>
              <el-radio-button label="quantity">按销量</el-radio-button>
            </el-radio-group>
            
            <el-select v-model="rankLimit" placeholder="显示数量" style="width: 120px; margin-left: 15px" @change="fetchRankData">
              <el-option label="TOP 10" :value="10" />
              <el-option label="TOP 20" :value="20" />
              <el-option label="TOP 50" :value="50" />
            </el-select>
          </div>
        </div>
      </template>
      
      <el-table :data="rankData" style="width: 100%" :max-height="500" v-loading="rankLoading">
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
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="ipName" label="IP" width="120" />
        <el-table-column :prop="rankType === 'amount' ? 'amount' : 'quantity'" :label="rankType === 'amount' ? '销售额' : '销量'" width="120">
          <template #default="{ row }">
            {{ rankType === 'amount' ? '¥' + formatPrice(row.amount) : row.quantity }}
          </template>
        </el-table-column>
        <el-table-column label="占比" width="180">
          <template #default="{ row }">
            <el-progress
              :percentage="row.percentage"
              :color="getProgressColor(row.percentage)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 销售明细 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>销售明细</span>
          <div class="detail-actions">
            <el-input
              v-model="detailSearch"
              placeholder="搜索订单号/商品名称"
              clearable
              style="width: 200px"
              @input="handleDetailSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-button type="primary" @click="exportDetailData">导出明细</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="detailData" style="width: 100%" :max-height="500" v-loading="detailLoading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="productSpec" label="规格" width="120" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ formatPrice(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            ¥{{ formatPrice(row.price * row.quantity) }}
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="createdAt" label="下单时间" width="180" />
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="detailPage"
          v-model:page-size="detailSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="detailTotal"
          @size-change="handleDetailSizeChange"
          @current-change="handleDetailCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { Refresh, Search, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
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
  getSalesOverview, getSalesTrend, getSalesCategoryData, getSalesIpData,
  getSalesRank, getSalesDetail, exportSales, exportSalesDetail
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
const dateType = ref('week')
const dateRange = ref([])
const updateTime = ref(new Date())
const overviewData = ref([])
const trendType = ref('day')
const trendDataType = ref('amount')
const categoryDataType = ref('amount')
const ipDataType = ref('amount')
const rankType = ref('amount')
const rankLimit = ref(10)
const rankData = ref([])
const rankLoading = ref(false)
const detailSearch = ref('')
const detailData = ref([])
const detailLoading = ref(false)
const detailPage = ref(1)
const detailSize = ref(10)
const detailTotal = ref(0)

// 图表引用
const trendChartRef = ref(null)
const categoryChartRef = ref(null)
const ipChartRef = ref(null)

// 图表实例
let trendChart = null
let categoryChart = null
let ipChart = null

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
  if (dateType.value === 'week') {
    return '近7天'
  } else if (dateType.value === 'month') {
    return '近30天'
  } else if (dateType.value === 'quarter') {
    return '近90天'
  } else if (dateType.value === 'custom' && dateRange.value && dateRange.value.length === 2) {
    return dateRange.value[0] + ' 至 ' + dateRange.value[1]
  }
  return ''
})

// 方法
function formatPrice(price) {
  return parseFloat(price || 0).toFixed(2)
}

function formatValue(value, type) {
  if (type === 'price') {
    return formatPrice(value)
  } else if (type === 'percent') {
    return value + '%'
  } else {
    return value
  }
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
    
    const res = await getSalesOverview(params)
    overviewData.value = res.data
    updateTime.value = new Date()
    
    // 获取其他数据
    fetchTrendData()
    fetchCategoryData()
    fetchIpData()
    fetchRankData()
    fetchDetailData()
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

// 获取趋势数据
async function fetchTrendData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      trendType: trendType.value,
      dataType: trendDataType.value
    }
    
    const res = await getSalesTrend(params)
    updateTrendChart(res.data)
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 获取分类数据
async function fetchCategoryData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      dataType: categoryDataType.value
    }
    
    const res = await getSalesCategoryData(params)
    updateCategoryChart(res.data)
  } catch (error) {
    console.error('获取分类数据失败:', error)
  }
}

// 获取IP数据
async function fetchIpData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      dataType: ipDataType.value
    }
    
    const res = await getSalesIpData(params)
    updateIpChart(res.data)
  } catch (error) {
    console.error('获取IP数据失败:', error)
  }
}

// 获取排行数据
async function fetchRankData() {
  rankLoading.value = true
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      rankType: rankType.value,
      limit: rankLimit.value
    }
    
    const res = await getSalesRank(params)
    rankData.value = res.data
  } catch (error) {
    console.error('获取排行数据失败:', error)
  } finally {
    rankLoading.value = false
  }
}

// 获取明细数据
async function fetchDetailData() {
  detailLoading.value = true
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      keyword: detailSearch.value,
      page: detailPage.value,
      size: detailSize.value
    }
    
    const res = await getSalesDetail(params)
    detailData.value = res.data.records
    detailTotal.value = res.data.total
  } catch (error) {
    console.error('获取明细数据失败:', error)
  } finally {
    detailLoading.value = false
  }
}

// 更新趋势图表
function updateTrendChart(data) {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  let title = ''
  let yAxisFormatter = null
  
  if (trendDataType.value === 'amount') {
    title = '销售额趋势'
    yAxisFormatter = value => '¥' + formatPrice(value)
  } else if (trendDataType.value === 'order') {
    title = '订单数趋势'
    yAxisFormatter = value => value
  } else {
    title = '客单价趋势'
    yAxisFormatter = value => '¥' + formatPrice(value)
  }
  
  const option = {
    title: {
      text: title
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const param = params[0]
        if (trendDataType.value === 'amount' || trendDataType.value === 'average') {
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
        formatter: yAxisFormatter
      }
    },
    series: [
      {
        name: title.replace('趋势', ''),
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
  
  trendChart.setOption(option)
}

// 更新分类图表
function updateCategoryChart(data) {
  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  
  const option = {
    title: {
      text: '分类销售占比',
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
        name: categoryDataType.value === 'amount' ? '销售额' : '订单数',
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
  
  categoryChart.setOption(option)
}

// 更新IP图表
function updateIpChart(data) {
  if (!ipChart) {
    ipChart = echarts.init(ipChartRef.value)
  }
  
  const option = {
    title: {
      text: 'IP销售占比',
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
        name: ipDataType.value === 'amount' ? '销售额' : '订单数',
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
  
  ipChart.setOption(option)
}

// 处理明细搜索
function handleDetailSearch() {
  detailPage.value = 1
  fetchDetailData()
}

// 处理明细分页大小变化
function handleDetailSizeChange() {
  fetchDetailData()
}

// 处理明细页码变化
function handleDetailCurrentChange() {
  fetchDetailData()
}

// 导出销售数据
async function exportSalesData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null
    }
    
    await exportSales(params)
    ElMessage.success('导出成功，请到下载中心查看')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 导出明细数据
async function exportDetailData() {
  try {
    const params = {
      dateType: dateType.value,
      startDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[0] : null,
      endDate: dateType.value === 'custom' && dateRange.value ? dateRange.value[1] : null,
      keyword: detailSearch.value
    }
    
    await exportSalesDetail(params)
    ElMessage.success('导出成功，请到下载中心查看')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 处理窗口大小变化
function handleResize() {
  trendChart && trendChart.resize()
  categoryChart && categoryChart.resize()
  ipChart && ipChart.resize()
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
  trendChart && trendChart.dispose()
  categoryChart && categoryChart.dispose()
  ipChart && ipChart.dispose()
})
</script>

<style lang="scss" scoped>
.sales-analysis-container {
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
  
  .overview-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .overview-item {
      padding: 15px;
      margin-bottom: 20px;
      
      .item-label {
        font-size: 14px;
        color: #666;
        margin-bottom: 10px;
      }
      
      .item-value {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 10px;
      }
      
      .item-compare {
        font-size: 12px;
        
        .up {
          color: #67C23A;
        }
        
        .down {
          color: #F56C6C;
        }
        
        .compare-text {
          color: #999;
          margin-left: 5px;
        }
      }
    }
  }
  
  .chart-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .chart-actions {
        display: flex;
        align-items: center;
      }
    }
    
    .chart-container {
      height: 400px;
    }
  }
  
  .chart-row {
    margin-bottom: 20px;
  }
  
  .rank-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .rank-actions {
        display: flex;
        align-items: center;
      }
    }
  }
  
  .detail-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .detail-actions {
        display: flex;
        align-items: center;
        gap: 15px;
      }
    }
    
    .pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}
</style>
