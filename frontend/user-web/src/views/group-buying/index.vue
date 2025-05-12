<template>
  <div class="group-buying-container">
    <el-card class="group-buying-card">
      <template #header>
        <div class="card-header">
          <h2>拼团活动</h2>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <el-empty v-else-if="groupBuyings.length === 0" description="暂无拼团活动" />

      <div v-else class="group-buying-list">
        <el-row :gutter="24">
          <el-col v-for="groupBuying in groupBuyings" :key="groupBuying.id" :xs="24" :sm="12" :md="8" :lg="6">
            <group-buying-card :group-buying="groupBuying" />
          </el-col>
        </el-row>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[8, 16, 24, 32]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getActiveGroupBuyings } from '../../api/group'
import GroupBuyingCard from '../../components/GroupBuyingCard.vue'

// 状态
const groupBuyings = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 方法
async function fetchGroupBuyings() {
  loading.value = true
  try {
    const res = await getActiveGroupBuyings({
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (res.data && Array.isArray(res.data)) {
      groupBuyings.value = res.data
      total.value = res.data.length // 如果后端没有返回总数，就用当前数组长度
    } else if (res.data && res.data.records) {
      // 如果后端返回的是分页对象
      groupBuyings.value = res.data.records
      total.value = res.data.total
    } else {
      groupBuyings.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取拼团活动失败:', error)
    groupBuyings.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSizeChange() {
  currentPage.value = 1
  fetchGroupBuyings()
}

function handleCurrentChange() {
  fetchGroupBuyings()
}

// 生命周期钩子
onMounted(() => {
  fetchGroupBuyings()
})
</script>

<style lang="scss" scoped>
.group-buying-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .group-buying-card {
    .card-header {
      h2 {
        margin: 0;
        font-size: 20px;
      }
    }

    .loading {
      padding: 20px 0;
    }

    .group-buying-list {
      .pagination-container {
        margin-top: 20px;
        display: flex;
        justify-content: center;
      }
    }
  }
}
</style>
