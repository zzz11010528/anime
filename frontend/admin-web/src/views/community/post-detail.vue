<template>
  <div class="post-detail-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>帖子详情</span>
          <el-button type="primary" size="small" @click="goBack">返回列表</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 帖子基本信息 -->
        <div class="post-header">
          <h2 class="post-title">{{ postInfo.title }}</h2>
          <div class="post-meta">
            <span>作者：{{ postInfo.username }}</span>
            <span>发布时间：{{ formatDate(postInfo.createdAt) }}</span>
            <span>浏览量：{{ postInfo.viewCount }}</span>
            <span>点赞数：{{ postInfo.likeCount }}</span>
            <span>评论数：{{ postInfo.commentCount }}</span>
          </div>
          <div class="post-status">
            <el-tag :type="getStatusType(postInfo.status)">{{ getStatusText(postInfo.status) }}</el-tag>
          </div>
        </div>

        <!-- 帖子内容 -->
        <div class="post-content">
          <div v-html="postInfo.content"></div>
        </div>

        <!-- 帖子图片 -->
        <div class="post-images" v-if="postInfo.images && postInfo.images.length > 0">
          <h3>帖子图片</h3>
          <div class="image-list">
            <el-image
              v-for="(image, index) in postInfo.images"
              :key="index"
              :src="formatImageUrl(image.imageUrl)"
              :preview-src-list="postInfo.images.map(img => formatImageUrl(img.imageUrl))"
              :initial-index="index"
              fit="contain"
              style="max-width: 200px; max-height: 200px; margin: 10px;"
            ></el-image>
          </div>
        </div>

        <!-- 审核操作 -->
        <div class="post-actions" v-if="postInfo.status === 0">
          <el-button type="success" @click="handleAudit(1)">通过审核</el-button>
          <el-button type="danger" @click="handleAudit(2)">拒绝审核</el-button>
        </div>

        <!-- 其他操作 -->
        <div class="post-actions" v-else>
          <el-button type="danger" @click="handleDelete">删除帖子</el-button>
        </div>
      </div>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>评论列表</span>
        </div>
      </template>

      <div v-loading="commentsLoading">
        <!-- 主评论 -->
        <div class="comment-item" v-for="comment in comments" :key="comment.id">
          <div class="comment-header">
            <div class="comment-user">
              <el-avatar :src="comment.avatar ? formatImageUrl(comment.avatar) : ''" :size="40"></el-avatar>
              <span class="comment-username">{{ comment.username }}</span>
            </div>
            <div class="comment-time">{{ formatDate(comment.createdAt) }}</div>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-actions">
            <el-button type="danger" link @click="handleDeleteComment(comment.id)">删除</el-button>
          </div>

          <!-- 回复列表 -->
          <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
            <div v-for="reply in comment.replies" :key="reply.id" class="comment-item reply-item">
              <div class="comment-header">
                <div class="comment-user">
                  <el-avatar :src="reply.avatar ? formatImageUrl(reply.avatar) : ''" :size="30"></el-avatar>
                  <span class="comment-username">{{ reply.username }}</span>
                </div>
                <div class="comment-time">{{ formatDate(reply.createdAt) }}</div>
              </div>
              <div class="comment-content">
                <div class="reply-info">
                  回复 <span class="reply-name">@{{ reply.replyUsername }}</span>:
                </div>
                <div class="content-text">{{ reply.content }}</div>
              </div>
              <div class="comment-actions">
                <el-button type="danger" link size="small" @click="handleDeleteComment(reply.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="empty-block" v-if="comments.length === 0">
          <el-empty description="暂无评论" />
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="comments.length > 0">
          <el-pagination
            v-model:current-page="commentQuery.page"
            v-model:page-size="commentQuery.size"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="commentTotal"
            @size-change="handleCommentSizeChange"
            @current-change="handleCommentCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="500px"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" prop="reason" :rules="[{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          ></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, auditPost, deletePost } from '../../api/post'
import { getCommentList, deleteComment } from '../../api/comment'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()
const postId = route.params.id

// 数据
const loading = ref(false)
const commentsLoading = ref(false)
const postInfo = ref({})
const comments = ref([])
const commentTotal = ref(0)
const commentQuery = reactive({
  page: 1,
  size: 10,
  postId,
  parentId: 0 // 只查询一级评论
})

// 拒绝审核相关
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  reason: ''
})

// 方法
function goBack() {
  router.push('/community/post')
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

function getStatusType(status) {
  if (status === 0) return 'warning'  // 待审核
  if (status === 1) return 'success'  // 已发布
  if (status === 2) return 'danger'   // 已拒绝
  return 'info'
}

function getStatusText(status) {
  if (status === 0) return '待审核'
  if (status === 1) return '已发布'
  if (status === 2) return '已拒绝'
  return '未知'
}

async function fetchPostDetail() {
  loading.value = true
  try {
    const res = await getPostDetail(postId)
    postInfo.value = res.data || {}
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  commentsLoading.value = true
  try {
    // 获取一级评论（parentId=0）
    const res = await getCommentList(commentQuery)

    // 获取所有回复评论（parentId>0）
    const replyRes = await getCommentList({
      postId,
      parentId: -1, // 使用-1表示获取所有非一级评论
      page: 1,
      size: 1000 // 设置较大的size以获取所有回复
    })

    // 构建评论树
    let commentMap = {}
    let rootComments = []
    let allComments = []

    // 先处理一级评论
    if (res.data && res.data.records) {
      res.data.records.forEach(comment => {
        comment.replies = []
        commentMap[comment.id] = comment
        rootComments.push(comment)
        allComments.push(comment)
      })
    }

    // 再处理回复评论，先将所有评论放入map中
    if (replyRes.data && replyRes.data.records) {
      replyRes.data.records.forEach(reply => {
        reply.replies = []
        commentMap[reply.id] = reply
        allComments.push(reply)
      })
    }

    // 构建评论树结构
    allComments.forEach(comment => {
      if (comment.parentId && comment.parentId > 0) {
        // 如果是回复评论，找到其父评论
        const parent = commentMap[comment.parentId]
        if (parent) {
          // 如果父评论存在，将当前评论添加到父评论的回复列表中
          if (!parent.replies) {
            parent.replies = []
          }
          parent.replies.push(comment)
        } else if (!rootComments.includes(comment)) {
          // 如果父评论不存在且当前评论不在根评论列表中，将其添加到根评论列表
          rootComments.push(comment)
        }
      }
    })

    // 按时间排序
    rootComments.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))

    // 对每个评论的回复也按时间排序
    allComments.forEach(comment => {
      if (comment.replies && comment.replies.length > 0) {
        comment.replies.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
      }
    })

    comments.value = rootComments
    commentTotal.value = res.data.total || 0
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

function handleCommentSizeChange(size) {
  commentQuery.size = size
  fetchComments()
}

function handleCommentCurrentChange(page) {
  commentQuery.page = page
  fetchComments()
}

function handleAudit(status) {
  if (status === 1) {
    ElMessageBox.confirm('确定要通过该帖子的审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      auditPost(postId, status).then(() => {
        ElMessage.success('审核通过成功')
        postInfo.value.status = 1
      }).catch(error => {
        console.error('审核失败:', error)
        ElMessage.error('审核失败')
      })
    }).catch(() => {})
  } else {
    rejectForm.reason = ''
    rejectDialogVisible.value = true
  }
}

function confirmReject() {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  auditPost(postId, 2, rejectForm.reason).then(() => {
    ElMessage.success('审核拒绝成功')
    postInfo.value.status = 2
    rejectDialogVisible.value = false
  }).catch(error => {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  })
}

function handleDelete() {
  ElMessageBox.confirm('确定要删除该帖子吗？删除后无法恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deletePost(postId).then(() => {
      ElMessage.success('删除成功')
      router.push('/community/post')
    }).catch(error => {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

function handleDeleteComment(commentId) {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteComment(commentId).then(() => {
      ElMessage.success('删除成功')
      fetchComments()
    }).catch(error => {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

// 生命周期钩子
onMounted(() => {
  fetchPostDetail()
  fetchComments()
})
</script>

<style lang="scss" scoped>
.post-detail-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .post-header {
    margin-bottom: 20px;

    .post-title {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 10px;
    }

    .post-meta {
      color: #909399;
      font-size: 14px;

      span {
        margin-right: 15px;
      }
    }

    .post-status {
      margin-top: 10px;
    }
  }

  .post-content {
    margin-bottom: 20px;
    line-height: 1.6;
  }

  .post-images {
    margin-bottom: 20px;

    h3 {
      margin-bottom: 10px;
    }

    .image-list {
      display: flex;
      flex-wrap: wrap;
    }
  }

  .post-actions {
    margin-top: 20px;
    display: flex;
    gap: 10px;
  }

  .comment-item {
    padding: 15px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .comment-user {
        display: flex;
        align-items: center;

        .comment-username {
          margin-left: 10px;
          font-weight: bold;
        }
      }

      .comment-time {
        color: #909399;
        font-size: 14px;
      }
    }

    .comment-content {
      margin-bottom: 10px;
      line-height: 1.6;

      .reply-info {
        color: #909399;
        margin-bottom: 5px;

        .reply-name {
          color: #409EFF;
          font-weight: bold;
        }
      }

      .content-text {
        word-break: break-all;
      }
    }

    .comment-actions {
      display: flex;
      justify-content: flex-end;
    }

    .reply-list {
      margin-top: 10px;
      margin-left: 40px;
      background-color: #f5f7fa;
      border-radius: 4px;
      padding: 10px;

      .reply-item {
        padding: 10px 0;

        &:first-child {
          padding-top: 0;
        }

        &:last-child {
          padding-bottom: 0;
        }
      }
    }
  }

  .empty-block {
    padding: 20px 0;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
