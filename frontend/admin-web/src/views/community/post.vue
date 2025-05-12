<template>
  <div class="post-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>帖子管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
          </el-form-item>
          <el-form-item label="用户ID">
            <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
              <el-option
                v-for="category in categoryOptions"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="IP">
            <el-select v-model="searchForm.ipId" placeholder="请选择IP" clearable>
              <el-option
                v-for="ip in ipOptions"
                :key="ip.id"
                :label="ip.name"
                :value="ip.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="删除" :value="0" />
              <el-option label="正常" :value="1" />
              <el-option label="审核中" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="postList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="ipName" label="IP" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="80" />
        <el-table-column prop="likeCount" label="点赞数" width="80" />
        <el-table-column prop="commentCount" label="评论数" width="80" />
        <el-table-column prop="collectCount" label="收藏数" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="danger">删除</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning">审核中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <router-link :to="`/community/post/detail/${scope.row.id}`">
              <el-button type="primary" link>详情</el-button>
            </router-link>
            <el-button
              v-if="scope.row.status === 2"
              type="success"
              link
              @click="handleAudit(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 2"
              type="danger"
              link
              @click="handleAudit(scope.row, 0)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="danger"
              link
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>



    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="400px"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectFormRules"
        label-width="100px"
      >
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostList, auditPost, deletePost } from '../../api/post'
import { getCommunityCategories } from '../../api/category'
import { getIpList } from '../../api/ip'

// 状态
const loading = ref(false)
const postList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const rejectDialogVisible = ref(false)
const categoryOptions = ref([])
const ipOptions = ref([])

// 表单引用
const rejectFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  title: '',
  userId: '',
  categoryId: '',
  ipId: '',
  status: ''
})

// 拒绝表单
const rejectForm = reactive({
  id: null,
  rejectReason: ''
})

// 表单校验规则
const rejectFormRules = {
  rejectReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

// 方法
function fetchPostList() {
  loading.value = true
  const params = {
    page: currentPage.value,
    size: pageSize.value,
    keyword: searchForm.title || undefined,  // 使用keyword参数进行标题搜索
    userId: searchForm.userId || undefined,
    categoryId: searchForm.categoryId || undefined,
    ipId: searchForm.ipId || undefined
  }

  // 只有当选择了状态时才传递status参数
  if (searchForm.status !== '') {
    params.status = searchForm.status
  }

  getPostList(params).then(res => {
    postList.value = res.data.records || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function fetchCategoryList() {
  getCommunityCategories().then(res => {
    categoryOptions.value = res.data
  })
}

function fetchIpList() {
  getIpList().then(res => {
    ipOptions.value = res.data
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchPostList()
}

function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  // 重置后不传递status参数，查询所有状态的帖子
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchPostList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchPostList()
}



function handleAudit(row, status) {
  if (status === 1) {
    // 通过审核
    ElMessageBox.confirm(`确定要通过帖子 "${row.title}" 的审核吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      auditPost(row.id, status).then(() => {
        ElMessage.success('审核通过成功')
        fetchPostList()
      }).catch(error => {
        console.error('审核通过失败:', error)
        ElMessage.error(error.response?.data?.msg || '审核通过失败')
      })
    }).catch(() => {})
  } else {
    // 拒绝审核
    rejectForm.id = row.id
    rejectForm.rejectReason = ''
    rejectDialogVisible.value = true
  }
}

function submitReject() {
  rejectFormRef.value.validate(valid => {
    if (valid) {
      auditPost(rejectForm.id, 0, rejectForm.rejectReason).then(() => {
        ElMessage.success('拒绝成功')
        rejectDialogVisible.value = false
        fetchPostList()
      }).catch(error => {
        console.error('拒绝失败:', error)
        ElMessage.error(error.response?.data?.msg || '拒绝失败')
      })
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除帖子 "${row.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deletePost(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchPostList()
    })
  }).catch(() => {})
}

// 生命周期钩子
onMounted(() => {
  fetchPostList()
  fetchCategoryList()
  fetchIpList()
})
</script>

<style lang="scss" scoped>
.post-list-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-container {
    margin-bottom: 20px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .post-detail {
    .post-content {
      margin-top: 20px;

      .content-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .content-body {
        background-color: #f5f7fa;
        padding: 15px;
        border-radius: 4px;
        white-space: pre-wrap;
      }
    }

    .post-images {
      margin-top: 20px;

      .images-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .images-list {
        display: flex;
        flex-wrap: wrap;

        .image-item {
          margin-right: 10px;
          margin-bottom: 10px;
        }
      }
    }

    .post-topics {
      margin-top: 20px;

      .topics-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .topics-list {
        display: flex;
        flex-wrap: wrap;

        .topic-item {
          margin-right: 10px;
          margin-bottom: 10px;
        }
      }
    }
  }
}
</style>
