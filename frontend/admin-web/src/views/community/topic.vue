<template>
  <div class="topic-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>话题管理</span>
          <el-button type="primary" @click="handleAdd">添加话题</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="话题名称">
            <el-input v-model="searchForm.name" placeholder="请输入话题名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
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
        :data="filteredTopicList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="话题名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="postCount" label="帖子数量" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(scope.row)"
              :disabled="scope.row.postCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑话题对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑话题' : '添加话题'"
      width="500px"
    >
      <el-form :model="topicForm" label-width="100px" :rules="rules" ref="topicFormRef">
        <el-form-item label="话题名称" prop="name">
          <el-input v-model="topicForm.name" placeholder="请输入话题名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="topicForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入话题描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="topicForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTopicDetail, addTopic, updateTopic, deleteTopic, updateTopicStatus } from '../../api/topic'
import { getAllTopics } from '../../api/admin-community'

// 数据
const loading = ref(false)
const topicList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const topicFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// 话题表单
const topicForm = reactive({
  id: null,
  name: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入话题名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 过滤后的话题列表
const filteredTopicList = computed(() => {
  let result = [...topicList.value]

  if (searchForm.name) {
    result = result.filter(item => item.name.includes(searchForm.name))
  }

  if (searchForm.status !== '') {
    result = result.filter(item => item.status === searchForm.status)
  }

  return result
})

// 方法
async function fetchTopicList() {
  loading.value = true
  try {
    const res = await getAllTopics()
    topicList.value = res.data || []
  } catch (error) {
    console.error('获取话题列表失败:', error)
    ElMessage.error('获取话题列表失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  topicForm.id = null
  topicForm.name = ''
  topicForm.description = ''
  topicForm.status = 1

  if (topicFormRef.value) {
    topicFormRef.value.resetFields()
  }
}

function handleSearch() {
  // 搜索功能由计算属性实现
}

function resetSearch() {
  searchForm.name = ''
  searchForm.status = ''
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  isEdit.value = true

  // 复制数据到表单
  topicForm.id = row.id
  topicForm.name = row.name
  topicForm.description = row.description || ''
  topicForm.status = row.status

  dialogVisible.value = true
}

function handleStatusChange(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  ElMessageBox.confirm(`确定要${statusText}话题 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateTopicStatus(row.id, newStatus)
      ElMessage.success(`${statusText}成功`)
      row.status = newStatus
    } catch (error) {
      console.error(`${statusText}失败:`, error)
      ElMessage.error(`${statusText}失败`)
    }
  }).catch(() => {})
}

function handleDelete(row) {
  if (row.postCount > 0) {
    ElMessage.warning('该话题下有帖子，无法删除')
    return
  }

  ElMessageBox.confirm(`确定要删除话题 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTopic(row.id)
      ElMessage.success('删除成功')
      fetchTopicList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

function submitForm() {
  topicFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await updateTopic(topicForm)
        ElMessage.success('更新成功')
      } else {
        await addTopic(topicForm)
        ElMessage.success('添加成功')
      }

      dialogVisible.value = false
      fetchTopicList()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  })
}

// 生命周期钩子
onMounted(() => {
  fetchTopicList()
})
</script>

<style lang="scss" scoped>
.topic-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-container {
    margin-bottom: 20px;
  }
}
</style>
