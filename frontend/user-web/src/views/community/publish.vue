<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑帖子' : '发布帖子' }}</h2>
        </div>
      </template>

      <el-form
        ref="publishFormRef"
        :model="publishForm"
        :rules="publishRules"
        label-width="80px"
        class="publish-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="publishForm.title"
            placeholder="请输入标题（2-50个字符）"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="publishForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="话题" prop="topicIds">
          <el-select
            v-model="publishForm.topicIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择话题（最多3个）"
            :max-collapse-tags="2"
            style="width: 100%"
          >
            <el-option
              v-for="topic in topics"
              :key="topic.id"
              :label="topic.name"
              :value="topic.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <div class="editor-toolbar">
            <el-button-group>
              <el-button :type="textFormatting.bold ? 'primary' : ''" @click="toggleBold">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button :type="textFormatting.italic ? 'primary' : ''" @click="toggleItalic">
                <el-icon><DocumentCopy /></el-icon>
              </el-button>
              <el-button :type="textFormatting.underline ? 'primary' : ''" @click="toggleUnderline">
                <el-icon><Document /></el-icon>
              </el-button>
            </el-button-group>

            <el-divider direction="vertical" />

            <el-button-group>
              <el-button @click="insertEmoji">
                <el-icon><ChatRound /></el-icon>
              </el-button>
              <el-button @click="insertLink">
                <el-icon><Link /></el-icon>
              </el-button>
            </el-button-group>
          </div>

          <el-input
            v-model="publishForm.content"
            type="textarea"
            placeholder="请输入内容（5-2000个字符）"
            :rows="10"
            maxlength="2000"
            show-word-limit
            ref="contentInput"
          />
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
            action="/api/file/upload/post"
            list-type="picture-card"
            :limit="9"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            :file-list="fileList"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传9张图片，每张不超过5MB</div>
        </el-form-item>

        <el-form-item label="IP" v-if="ipList.length > 0">
          <el-select v-model="publishForm.ipId" placeholder="请选择IP（可选）" clearable style="width: 100%">
            <el-option
              v-for="ip in ipList"
              :key="ip.id"
              :label="ip.name"
              :value="ip.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" @click="submitPost" :loading="publishing">{{ isEdit ? '保存修改' : '发布帖子' }}</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表情选择器 -->
    <el-dialog
      v-model="emojiDialogVisible"
      title="选择表情"
      width="400px"
    >
      <div class="emoji-container">
        <div
          v-for="(emoji, index) in emojiList"
          :key="index"
          class="emoji-item"
          @click="selectEmoji(emoji)"
        >
          {{ emoji }}
        </div>
      </div>
    </el-dialog>

    <!-- 链接插入对话框 -->
    <el-dialog
      v-model="linkDialogVisible"
      title="插入链接"
      width="400px"
    >
      <el-form :model="linkForm" label-width="80px">
        <el-form-item label="链接文本">
          <el-input v-model="linkForm.text" placeholder="请输入链接文本" />
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="linkForm.url" placeholder="请输入链接地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="linkDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmInsertLink">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, DocumentCopy, Document, Link, ChatRound, Plus } from '@element-plus/icons-vue'
import { getCategoryList, getTopicList, publishPost, getPostDetail, updatePost } from '../../api/community'
import { getIpList } from '../../api/ip'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()

// 状态
const publishFormRef = ref(null)
const contentInput = ref(null)
const publishing = ref(false)
const categories = ref([])
const topics = ref([])
const ipList = ref([])
const fileList = ref([])
const emojiDialogVisible = ref(false)
const linkDialogVisible = ref(false)

// 表单数据
const publishForm = reactive({
  id: null,
  title: '',
  categoryId: null,
  topicIds: [],
  content: '',
  images: [],
  ipId: null
})

// 链接表单
const linkForm = reactive({
  text: '',
  url: ''
})

// 文本格式化状态
const textFormatting = reactive({
  bold: false,
  italic: false,
  underline: false
})

// 表情列表
const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
  '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
  '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗',
  '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯',
  '😦', '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐'
]

// 表单验证规则
const publishRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 5, max: 2000, message: '内容长度在 5 到 2000 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const isEdit = computed(() => !!route.query.id)

// 方法
async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

async function fetchTopics() {
  try {
    const res = await getTopicList()
    topics.value = res.data
  } catch (error) {
    console.error('获取话题列表失败:', error)
  }
}

async function fetchIpList() {
  try {
    const res = await getIpList()
    ipList.value = res.data
  } catch (error) {
    console.error('获取IP列表失败:', error)
  }
}

// 如果是编辑模式，获取帖子详情
async function fetchPostDetail() {
  if (!route.query.id) return

  try {
    const res = await getPostDetail(route.query.id)
    const post = res.data

    // 填充表单
    publishForm.id = post.id
    publishForm.title = post.title
    publishForm.categoryId = post.categoryId
    publishForm.topicIds = post.topics ? post.topics.map(topic => topic.id) : []
    publishForm.content = post.content
    publishForm.ipId = post.ipId

    // 填充图片列表
    if (post.images && post.images.length > 0) {
      publishForm.images = post.images
      fileList.value = post.images.map(img => ({
        name: img.imageUrl.split('/').pop(),
        url: formatImageUrl(img.imageUrl)
      }))
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
    router.push('/community')
  }
}

// 处理图片上传成功
function handleImageSuccess(res, file) {
  publishForm.images.push({
    imageUrl: res.data
  })
}

// 处理图片移除
function handleImageRemove(file, fileList) {
  const imageUrl = file.response?.data || file.url
  const index = publishForm.images.findIndex(img => img.imageUrl === imageUrl)
  if (index !== -1) {
    publishForm.images.splice(index, 1)
  }
}

// 图片上传前验证
function beforeImageUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
  }
  return (isJPG || isPNG) && isLt5M
}

// 提交帖子
function submitPost() {
  publishFormRef.value.validate(async (valid) => {
    if (!valid) return

    publishing.value = true
    try {
      if (isEdit.value) {
        // 更新帖子
        await updatePost(publishForm)
        ElMessage.success('帖子更新成功')
      } else {
        // 发布帖子
        await publishPost(publishForm)
        ElMessage.success('帖子发布成功')
      }

      // 跳转到社区页面
      router.push('/community')
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      publishing.value = false
    }
  })
}

// 返回
function goBack() {
  if (publishForm.title || publishForm.content || publishForm.images.length > 0) {
    ElMessageBox.confirm('是否放弃当前编辑？', '提示', {
      confirmButtonText: '放弃',
      cancelButtonText: '继续编辑',
      type: 'warning'
    }).then(() => {
      router.back()
    }).catch(() => {})
  } else {
    router.back()
  }
}

// 文本格式化
function toggleBold() {
  textFormatting.bold = !textFormatting.bold
  wrapSelectedText('**', '**')
}

function toggleItalic() {
  textFormatting.italic = !textFormatting.italic
  wrapSelectedText('*', '*')
}

function toggleUnderline() {
  textFormatting.underline = !textFormatting.underline
  wrapSelectedText('<u>', '</u>')
}

// 包装选中文本
function wrapSelectedText(prefix, suffix) {
  const textarea = contentInput.value.ref
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = publishForm.content.substring(start, end)

  if (selectedText) {
    const newText = publishForm.content.substring(0, start) +
                    prefix + selectedText + suffix +
                    publishForm.content.substring(end)

    publishForm.content = newText

    // 重新设置光标位置
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + prefix.length, end + prefix.length)
    }, 0)
  }
}

// 插入表情
function insertEmoji() {
  emojiDialogVisible.value = true
}

function selectEmoji(emoji) {
  const textarea = contentInput.value.ref
  if (!textarea) return

  const start = textarea.selectionStart

  const newText = publishForm.content.substring(0, start) +
                  emoji +
                  publishForm.content.substring(start)

  publishForm.content = newText

  // 重新设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + emoji.length, start + emoji.length)
  }, 0)

  emojiDialogVisible.value = false
}

// 插入链接
function insertLink() {
  linkForm.text = ''
  linkForm.url = ''
  linkDialogVisible.value = true
}

function confirmInsertLink() {
  if (!linkForm.text || !linkForm.url) {
    ElMessage.warning('请输入链接文本和地址')
    return
  }

  const textarea = contentInput.value.ref
  if (!textarea) return

  const start = textarea.selectionStart
  const linkMarkdown = `[${linkForm.text}](${linkForm.url})`

  const newText = publishForm.content.substring(0, start) +
                  linkMarkdown +
                  publishForm.content.substring(start)

  publishForm.content = newText

  // 重新设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + linkMarkdown.length, start + linkMarkdown.length)
  }, 0)

  linkDialogVisible.value = false
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchTopics()
  fetchIpList()

  if (isEdit.value) {
    fetchPostDetail()
  }
})
</script>

<style lang="scss" scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;

  .publish-card {
    margin-bottom: 20px;

    .card-header {
      h2 {
        margin: 0;
        font-size: 20px;
      }
    }

    .publish-form {
      .editor-toolbar {
        margin-bottom: 10px;
        display: flex;
        align-items: center;
      }

      .upload-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
      }

      .post-settings {
        display: flex;
        gap: 20px;
      }

      .form-actions {
        display: flex;
        justify-content: center;
        gap: 15px;
      }
    }
  }

  .emoji-container {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .emoji-item {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      cursor: pointer;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f0f0f0;
      }
    }
  }
}
</style>
