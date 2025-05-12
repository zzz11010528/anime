<template>
  <div class="category-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>社区分类管理</span>
          <el-button type="primary" @click="handleAdd">添加分类</el-button>
        </div>
      </template>

      <!-- 分类列表 -->
      <el-table
        v-loading="loading"
        :data="categoryList"
        border
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form :model="categoryForm" label-width="100px" :rules="rules" ref="categoryFormRef">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addCommunityCategory, updateCommunityCategory, deleteCommunityCategory } from '../../api/category'
import { getAllCommunityCategories } from '../../api/admin-community'

// 数据
const loading = ref(false)
const categoryList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const categoryFormRef = ref(null)

// 表单数据
const categoryForm = reactive({
  id: null,
  name: '',
  sort: 0,
  description: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 方法
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

async function fetchCategoryList() {
  loading.value = true
  try {
    const res = await getAllCommunityCategories()
    categoryList.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.sort = 0
  categoryForm.description = ''

  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  Object.assign(categoryForm, row)
  isEdit.value = true
  dialogVisible.value = true
}

function handleDelete(row) {

  ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCommunityCategory(row.id)
      ElMessage.success('删除成功')
      fetchCategoryList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

function submitForm() {
  categoryFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      // 确保没有parentId字段
      categoryForm.parentId = 0

      if (isEdit.value) {
        await updateCommunityCategory(categoryForm)
        ElMessage.success('更新成功')
      } else {
        await addCommunityCategory(categoryForm)
        ElMessage.success('添加成功')
      }

      dialogVisible.value = false
      fetchCategoryList()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  })
}

// 生命周期钩子
onMounted(() => {
  fetchCategoryList()
})
</script>

<style lang="scss" scoped>
.category-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
