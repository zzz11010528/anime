<template>
  <div class="category-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品分类管理</span>
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
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.level === 1" type="success">一级</el-tag>
            <el-tag v-else-if="scope.row.level === 2" type="warning">二级</el-tag>
            <el-tag v-else-if="scope.row.level === 3" type="danger">三级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              type="success"
              link
              @click="handleAddChild(scope.row)"
              v-if="scope.row.level < 3"
            >
              添加子分类
            </el-button>
            <el-button
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : (isAddChild ? '添加子分类' : '添加分类')"
      width="500px"
    >
      <el-form :model="categoryForm" label-width="100px" :rules="rules" ref="categoryFormRef">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类" prop="parentId" v-if="!isAddChild">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父级分类" clearable style="width: 100%">
            <el-option label="无（作为一级分类）" :value="0" />
            <el-option
              v-for="item in parentCategories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryDetail, addCategory, updateCategory, updateCategoryStatus } from '../../api/category'
import { getAllCategoryTree } from '../../api/admin-category'

// 数据
const loading = ref(false)
const categoryList = ref([])
const parentCategories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const isAddChild = ref(false)
const categoryFormRef = ref(null)

// 表单数据
const categoryForm = reactive({
  id: null,
  name: '',
  parentId: 0,
  level: 1,
  sort: 0,
  status: 1
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
function fetchCategoryList() {
  loading.value = true
  getAllCategoryTree().then(res => {
    categoryList.value = res.data || []
    // 提取一级分类作为父级分类选项
    parentCategories.value = res.data.filter(item => item.level === 1)
  }).finally(() => {
    loading.value = false
  })
}

function resetForm() {
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.parentId = 0
  categoryForm.level = 1
  categoryForm.sort = 0
  categoryForm.status = 1

  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  isAddChild.value = false
  dialogVisible.value = true
}

function handleAddChild(row) {
  resetForm()
  categoryForm.parentId = row.id
  categoryForm.level = row.level + 1
  isEdit.value = false
  isAddChild.value = true
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  getCategoryDetail(row.id).then(res => {
    Object.assign(categoryForm, res.data)
    isEdit.value = true
    isAddChild.value = false
    dialogVisible.value = true
  })
}

function handleStatusChange(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  ElMessageBox.confirm(`确定要${statusText}分类 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    updateCategoryStatus(row.id, newStatus).then(() => {
      ElMessage.success(`${statusText}成功`)
      fetchCategoryList()
    })
  }).catch(() => {})
}

function submitForm() {
  categoryFormRef.value.validate(valid => {
    if (!valid) return

    if (isEdit.value) {
      updateCategory(categoryForm).then(() => {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchCategoryList()
      })
    } else {
      // 如果是添加子分类，设置正确的level
      if (isAddChild.value && categoryForm.parentId > 0) {
        const parent = findCategoryById(categoryForm.parentId)
        if (parent) {
          categoryForm.level = parent.level + 1
        }
      } else {
        // 如果选择了父级分类，设置正确的level
        if (categoryForm.parentId > 0) {
          const parent = findCategoryById(categoryForm.parentId)
          if (parent) {
            categoryForm.level = parent.level + 1
          }
        } else {
          categoryForm.level = 1
        }
      }

      addCategory(categoryForm).then(() => {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchCategoryList()
      })
    }
  })
}

function findCategoryById(id, categories = categoryList.value) {
  for (const category of categories) {
    if (category.id === id) {
      return category
    }
    if (category.children && category.children.length > 0) {
      const found = findCategoryById(id, category.children)
      if (found) return found
    }
  }
  return null
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
