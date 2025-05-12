<template>
  <div class="post-detail-container">
    <div v-if="loading" class="loading">
      <el-skeleton style="width: 100%" animated>
        <template #template>
          <div style="padding: 20px;">
            <el-skeleton-item variant="h1" style="width: 50%;" />
            <div style="display: flex; align-items: center; margin-top: 20px;">
              <el-skeleton-item variant="circle" style="width: 40px; height: 40px;" />
              <el-skeleton-item variant="text" style="margin-left: 10px; width: 100px;" />
            </div>
            <el-skeleton-item variant="text" style="margin-top: 20px; width: 100%;" />
            <el-skeleton-item variant="text" style="width: 100%;" />
            <el-skeleton-item variant="text" style="width: 80%;" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <div v-else-if="!post.id" class="not-found">
      <el-empty description="帖子不存在或已删除" />
      <el-button type="primary" @click="goBack">返回社区</el-button>
    </div>

    <template v-else>
      <!-- 帖子内容 -->
      <el-card class="post-card">
        <div class="post-header">
          <h1 class="post-title">{{ post.title }}</h1>

          <div class="post-meta">
            <div class="author-info">
              <el-avatar :src="post.avatar ? (post.avatar.startsWith('http') ? post.avatar : apiBaseUrl + post.avatar) : defaultAvatar" :size="40" @click="goToUserPosts(post.userId)"></el-avatar>
              <div class="author-name" @click="goToUserPosts(post.userId)">
                <div class="nickname">{{ post.nickname || post.username }}</div>
                <div class="post-time">{{ formatTime(post.createdAt) }}</div>
              </div>
            </div>

            <div class="post-actions">
              <el-button v-if="isAuthor" type="primary" text @click="handleEdit">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button v-if="isAuthor" type="danger" text @click="handleDelete">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
              <el-button type="primary" text @click="handleSendMessage" v-if="!isAuthor && isLoggedIn">
                <el-icon><Message /></el-icon>
                发送私信
              </el-button>
            </div>
          </div>

          <div class="post-tags">
            <el-tag v-if="post.categoryName" size="small">{{ post.categoryName }}</el-tag>
            <el-tag
              v-for="topic in post.topics"
              :key="topic.id"
              type="success"
              size="small"
              @click="goToTopic(topic.id)"
            >
              # {{ topic.name }}
            </el-tag>
            <el-tag v-if="post.ipName" type="info" size="small">{{ post.ipName }}</el-tag>
          </div>
        </div>

        <div class="post-content">
          <div class="content-text">{{ post.content }}</div>

          <div class="content-text-actions" v-if="post.images && post.images.length > 0">
            <el-button type="primary" @click="enableGestureMode" class="gesture-button">
              <el-icon><View /></el-icon> 手势浏览图片
            </el-button>
          </div>

          <div v-if="post.images && post.images.length > 0" class="content-images">
            <div class="images-header">
              <span>帖子图片 ({{ post.images.length }})</span>
            </div>
            <div class="images-container">
              <el-image
                v-for="(image, index) in post.images"
                :key="index"
                :src="formatImageUrl(image.imageUrl)"
                :preview-src-list="imageUrls"
                :initial-index="index"
                :preview-teleported="true"
                :hide-on-click-modal="false"
                fit="cover"
                class="content-image"
                @click="currentImageIndex = index"
              />
            </div>
          </div>

          <!-- 手势交互组件 -->
          <GestureImageViewer
            v-if="gestureMode"
            :images="imageUrls"
            :initial-index="currentImageIndex"
            @update:index="currentImageIndex = $event"
            @close="gestureMode = false"
          />
        </div>

        <div class="post-footer">
          <div class="post-stats">
            <div class="stat-item">
              <el-button :type="postLiked ? 'primary' : 'default'" text @click="handleLike">
                <el-icon><Check /></el-icon>
                {{ post.likeCount || 0 }}
              </el-button>
            </div>
            <div class="stat-item">
              <el-button :type="postCollected ? 'warning' : 'default'" text @click="handleCollect">
                <el-icon><Star /></el-icon>
                {{ post.collectCount || 0 }}
              </el-button>
            </div>
            <div class="stat-item">
              <el-button type="default" text @click="scrollToComments">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount || 0 }}
              </el-button>
            </div>
            <div class="stat-item">
              <el-button type="default" text>
                <el-icon><View /></el-icon>
                {{ post.viewCount || 0 }}
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card" id="comments">
        <template #header>
          <div class="card-header">
            <span>评论 ({{ post.commentCount || 0 }})</span>
          </div>
        </template>

        <!-- 评论输入框 -->
        <div class="comment-input">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            maxlength="500"
            show-word-limit
            :disabled="!isLoggedIn"
          />
          <div class="comment-actions">
            <el-button v-if="isLoggedIn" type="primary" @click="submitComment" :loading="commenting">
              发表评论
            </el-button>
            <el-button v-else type="primary" @click="goToLogin">
              登录后评论
            </el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="commentsLoading" class="loading">
            <el-skeleton :rows="3" animated v-for="i in 3" :key="i" style="margin-bottom: 20px;" />
          </div>

          <el-empty v-else-if="comments.length === 0" description="暂无评论" />

          <div v-else>
            <!-- 主评论 -->
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <div class="commenter-info">
                  <el-avatar :src="comment.avatar ? (comment.avatar.startsWith('http') ? comment.avatar : apiBaseUrl + comment.avatar) : defaultAvatar" :size="40" @click="goToUserPosts(comment.userId)"></el-avatar>
                  <div class="commenter-name" @click="goToUserPosts(comment.userId)">
                    <div class="nickname">{{ comment.nickname || comment.username }}</div>
                    <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
                  </div>
                </div>

                <div class="comment-actions">
                  <el-button type="primary" text @click="handleReply(comment)">
                    <el-icon><ChatLineRound /></el-icon>
                    回复
                  </el-button>
                  <el-button v-if="comment.userId === userInfo.id" type="danger" text @click="handleDeleteComment(comment.id)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>

              <div class="comment-content">
                <div v-if="comment.parentId !== 0" class="reply-info">
                  回复 <span class="reply-name">@{{ comment.replyNickname || comment.replyUsername }}</span>:
                </div>
                <div class="content-text">{{ comment.content }}</div>
              </div>

              <div class="comment-footer">
                <el-button :type="comment.isLiked ? 'primary' : 'default'" text size="small" @click="handleLikeComment(comment)">
                  <el-icon><Check /></el-icon>
                  {{ comment.likeCount || 0 }}
                </el-button>
              </div>

              <!-- 回复输入框 -->
              <div v-if="replyingTo === comment.id" class="reply-input">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  :placeholder="`回复 @${comment.nickname || comment.username}:`"
                  maxlength="500"
                  show-word-limit
                />
                <div class="reply-actions">
                  <el-button type="primary" size="small" @click="submitReply(comment)" :loading="replying">
                    发表回复
                  </el-button>
                  <el-button size="small" @click="cancelReply">
                    取消
                  </el-button>
                </div>
              </div>

              <!-- 回复列表 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                <div v-for="reply in comment.replies" :key="reply.id" class="comment-item reply-item">
                  <div class="comment-header">
                    <div class="commenter-info">
                      <el-avatar :src="reply.avatar ? (reply.avatar.startsWith('http') ? reply.avatar : apiBaseUrl + reply.avatar) : defaultAvatar" :size="30" @click="goToUserPosts(reply.userId)"></el-avatar>
                      <div class="commenter-name" @click="goToUserPosts(reply.userId)">
                        <div class="nickname">{{ reply.nickname || reply.username }}</div>
                        <div class="comment-time">{{ formatTime(reply.createdAt) }}</div>
                      </div>
                    </div>

                    <div class="comment-actions">
                      <el-button type="primary" text size="small" @click="handleReply(reply)">
                        <el-icon><ChatLineRound /></el-icon>
                        回复
                      </el-button>
                      <el-button v-if="reply.userId === userInfo.id" type="danger" text size="small" @click="handleDeleteComment(reply.id)">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </div>
                  </div>

                  <div class="comment-content">
                    <div class="reply-info">
                      回复 <span class="reply-name">@{{ reply.replyNickname || reply.replyUsername }}</span>:
                    </div>
                    <div class="content-text">{{ reply.content }}</div>
                  </div>

                  <div class="comment-footer">
                    <el-button :type="reply.isLiked ? 'primary' : 'default'" text size="small" @click="handleLikeComment(reply)">
                      <el-icon><Check /></el-icon>
                      {{ reply.likeCount || 0 }}
                    </el-button>
                  </div>

                  <!-- 回复输入框 -->
                  <div v-if="replyingTo === reply.id" class="reply-input">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 @${reply.nickname || reply.username}:`"
                      maxlength="500"
                      show-word-limit
                    />
                    <div class="reply-actions">
                      <el-button type="primary" size="small" @click="submitReply(reply)" :loading="replying">
                        发表回复
                      </el-button>
                      <el-button size="small" @click="cancelReply">
                        取消
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="pagination">
              <el-pagination
                v-model:current-page="commentPage"
                v-model:page-size="commentSize"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next"
                :total="commentTotal"
                @size-change="handleCommentSizeChange"
                @current-change="handleCommentCurrentChange"
              />
            </div>
          </div>
        </div>
      </el-card>

      <!-- 相关推荐 -->
      <el-card class="related-card">
        <template #header>
          <div class="card-header">
            <span>相关推荐</span>
          </div>
        </template>

        <el-empty v-if="relatedPosts.length === 0" description="暂无相关推荐" />

        <div v-else class="related-list">
          <div v-for="relatedPost in relatedPosts" :key="relatedPost.id" class="related-item" @click="goToPost(relatedPost.id)">
            <div class="related-title">{{ relatedPost.title }}</div>
            <div class="related-meta">
              <span class="author">{{ relatedPost.nickname || relatedPost.username }}</span>
              <span class="stats">
                <el-icon><View /></el-icon> {{ relatedPost.viewCount || 0 }}
                <el-icon><ChatDotRound /></el-icon> {{ relatedPost.commentCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Message, Check, Star, ChatDotRound, View, ChatLineRound } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getPostDetail, getCommentList, publishComment, deleteComment, like, unlike, collect, uncollect } from '../../api/community'
import { checkIsLiked as isLiked, checkIsCollected as isCollected } from '../../api/community'
import defaultAvatar from '../../assets/images/default-avatar.png'
import { formatImageUrl } from '../../utils/image'
import GestureImageViewer from '../../components/GestureImageViewer.vue'

// 获取API基础URL
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const post = ref({})
const comments = ref([])
const relatedPosts = ref([])
const loading = ref(true)
const commentsLoading = ref(false)
const commentPage = ref(1)
const commentSize = ref(10)
const commentTotal = ref(0)
const commentContent = ref('')
const commenting = ref(false)
const replyingTo = ref(null)
const replyContent = ref('')
const replying = ref(false)
const postLiked = ref(false)
const postCollected = ref(false)
const gestureMode = ref(false)
const currentImageIndex = ref(0)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const isAuthor = computed(() => post.value.userId === userInfo.value.id)
const imageUrls = computed(() => post.value.images ? post.value.images.map(img => formatImageUrl(img.imageUrl)) : [])

// 方法
async function fetchPostDetail() {
  loading.value = true
  try {
    const postId = route.params.id
    const res = await getPostDetail(postId)
    post.value = res.data

    // 获取点赞和收藏状态
    if (isLoggedIn.value) {
      checkLikeStatus()
      checkCollectionStatus()
    }

    // 获取评论
    fetchComments()

    // 获取相关推荐
    fetchRelatedPosts()
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    post.value = {}
  } finally {
    loading.value = false
  }
}

// 获取评论列表
async function fetchComments() {
  commentsLoading.value = true
  try {
    // 获取一级评论（parentId=0）
    const res = await getCommentList({
      postId: post.value.id,
      parentId: 0,  // 明确指定获取一级评论
      page: commentPage.value,
      size: commentSize.value
    })

    // 获取所有回复评论（parentId>0）
    const replyRes = await getCommentList({
      postId: post.value.id,
      parentId: -1,  // 使用-1表示获取所有非一级评论
      page: 1,
      size: 1000  // 设置较大的size以获取所有回复
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
    commentTotal.value = res.data.total
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

// 获取相关推荐
async function fetchRelatedPosts() {
  // TODO: 实现获取相关推荐
  relatedPosts.value = []
}

// 检查点赞状态
async function checkLikeStatus() {
  try {
    const res = await isLiked(post.value.id, 1) // 1表示帖子
    postLiked.value = res.data
  } catch (error) {
    console.error('获取点赞状态失败:', error)
  }
}

// 检查收藏状态
async function checkCollectionStatus() {
  try {
    const res = await isCollected(post.value.id, 1) // 1表示帖子
    postCollected.value = res.data
  } catch (error) {
    console.error('获取收藏状态失败:', error)
  }
}

// 处理点赞
async function handleLike() {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  try {
    if (postLiked.value) {
      await unlike(post.value.id, 1)
      post.value.likeCount--
    } else {
      await like(post.value.id, 1)
      post.value.likeCount++
    }
    postLiked.value = !postLiked.value
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理收藏
async function handleCollect() {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  try {
    if (postCollected.value) {
      await uncollect(post.value.id, 1)
      post.value.collectCount--
    } else {
      await collect(post.value.id, 1)
      post.value.collectCount++
    }
    postCollected.value = !postCollected.value
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 发送私信
function handleSendMessage() {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  // 跳转到消息页面，并传递目标用户ID
  router.push({
    path: '/message',
    query: { toUserId: post.value.userId }
  })
}

// 提交评论
async function submitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  commenting.value = true
  try {
    await publishComment({
      postId: post.value.id,
      content: commentContent.value,
      parentId: 0
    })

    ElMessage.success('评论成功')
    commentContent.value = ''

    // 刷新评论列表
    commentPage.value = 1
    await fetchComments()

    // 更新帖子评论数
    post.value.commentCount++
  } catch (error) {
    ElMessage.error(error.message || '评论失败')
  } finally {
    commenting.value = false
  }
}

// 处理回复
function handleReply(comment) {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  replyingTo.value = comment.id
  replyContent.value = ''
}

// 取消回复
function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
}

// 提交回复
async function submitReply(comment) {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }

  replying.value = true
  try {
    // 如果回复的是回复评论，找到其所属的一级评论
    let parentId = comment.parentId > 0 ? comment.parentId : comment.id

    await publishComment({
      postId: post.value.id,
      content: replyContent.value,
      parentId: parentId,
      replyUserId: comment.userId
    })

    ElMessage.success('回复成功')
    replyContent.value = ''
    replyingTo.value = null

    // 刷新评论列表
    await fetchComments()

    // 更新帖子评论数
    post.value.commentCount++
  } catch (error) {
    ElMessage.error(error.message || '回复失败')
  } finally {
    replying.value = false
  }
}

// 处理删除评论
async function handleDeleteComment(commentId) {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteComment(commentId)
      ElMessage.success('删除成功')

      // 刷新评论列表
      await fetchComments()

      // 更新帖子评论数
      post.value.commentCount--
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

// 处理点赞评论
async function handleLikeComment(comment) {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  try {
    if (comment.isLiked) {
      await unlike(comment.id, 2) // 2表示评论
      comment.likeCount--
    } else {
      await like(comment.id, 2)
      comment.likeCount++
    }
    comment.isLiked = !comment.isLiked
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理编辑帖子
function handleEdit() {
  router.push({
    path: '/post/publish',
    query: { id: post.value.id }
  })
}

// 处理删除帖子
function handleDelete() {
  ElMessageBox.confirm('确定要删除该帖子吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePost(post.value.id)
      ElMessage.success('删除成功')
      router.push('/community')
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

// 滚动到评论区
function scrollToComments() {
  document.getElementById('comments').scrollIntoView({ behavior: 'smooth' })
}

// 跳转到用户帖子列表
function goToUserPosts(userId) {
  router.push({
    path: '/community',
    query: { userId }
  })
}

// 跳转到话题
function goToTopic(topicId) {
  router.push({
    path: '/community',
    query: { topicId }
  })
}

// 跳转到帖子
function goToPost(postId) {
  router.push(`/post/${postId}`)
}

// 返回社区
function goBack() {
  router.push('/community')
}

// 跳转到登录页
function goToLogin() {
  router.push({
    path: '/login',
    query: { redirect: route.fullPath }
  })
}

// 启用手势浏览模式
function enableGestureMode() {
  gestureMode.value = true
  currentImageIndex.value = 0
}

// 处理评论分页大小变化
function handleCommentSizeChange(val) {
  commentSize.value = val
  commentPage.value = 1 // 重置页码
  fetchComments()
}

// 处理评论页码变化
function handleCommentCurrentChange(val) {
  commentPage.value = val
  window.scrollTo({
    top: document.getElementById('comments').offsetTop - 20,
    behavior: 'smooth'
  })
  fetchComments()
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }

  // 小于1天
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }

  // 小于1周
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }

  // 格式化日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 生命周期钩子
onMounted(() => {
  fetchPostDetail()
})
</script>

<style lang="scss" scoped>
.post-detail-container {
  max-width: 1000px;
  margin: 0 auto;

  .loading, .not-found {
    padding: 40px 0;
    text-align: center;
  }

  .post-card {
    margin-bottom: 20px;

    .post-header {
      margin-bottom: 20px;

      .post-title {
        font-size: 24px;
        margin-bottom: 15px;
      }

      .post-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .author-info {
          display: flex;
          align-items: center;

          .el-avatar {
            cursor: pointer;
            transition: transform 0.2s;

            &:hover {
              transform: scale(1.05);
            }
          }

          .author-name {
            margin-left: 10px;
            cursor: pointer;

            &:hover {
              .nickname {
                color: #409EFF;
              }
            }

            .nickname {
              font-size: 16px;
              font-weight: bold;
            }

            .post-time {
              font-size: 12px;
              color: #999;
            }
          }
        }
      }

      .post-tags {
        margin-bottom: 15px;

        .el-tag {
          margin-right: 10px;
          cursor: pointer;
        }
      }
    }

    .post-content {
      margin-bottom: 20px;

      .content-text {
        font-size: 16px;
        line-height: 1.6;
        margin-bottom: 20px;
        white-space: pre-wrap;
      }

      .content-text-actions {
        margin: 15px 0;
        display: flex;
        justify-content: center;

        .gesture-button {
          font-size: 16px;
          padding: 10px 20px;
          border-radius: 20px;
          background: linear-gradient(to right, #409EFF, #36D1DC);
          border: none;
          box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
          transition: all 0.3s;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 15px rgba(64, 158, 255, 0.4);
          }

          .el-icon {
            margin-right: 5px;
          }
        }
      }

      .content-images {
        margin-top: 20px;

        .images-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;
          padding-bottom: 10px;
          border-bottom: 1px solid #eee;

          span {
            font-size: 16px;
            font-weight: bold;
          }
        }

        .images-container {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
        }

        .content-image {
          width: 200px;
          height: 200px;
          border-radius: 4px;
          cursor: pointer;
          transition: transform 0.2s;

          &:hover {
            transform: scale(1.03);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }
        }
      }
    }

    .post-footer {
      border-top: 1px solid #eee;
      padding-top: 15px;

      .post-stats {
        display: flex;
        justify-content: space-around;

        .stat-item {
          text-align: center;
        }
      }
    }
  }

  .comment-card {
    margin-bottom: 20px;

    .card-header {
      font-weight: bold;
    }

    .comment-input {
      margin-bottom: 20px;

      .comment-actions {
        margin-top: 10px;
        text-align: right;
      }
    }

    .comment-list {
      .comment-item {
        padding: 15px 0;
        border-bottom: 1px solid #eee;

        &:last-child {
          border-bottom: none;
        }

        .comment-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;

          .commenter-info {
            display: flex;
            align-items: center;

            .el-avatar {
              cursor: pointer;
              transition: transform 0.2s;

              &:hover {
                transform: scale(1.05);
              }
            }

            .commenter-name {
              margin-left: 10px;
              cursor: pointer;

              &:hover {
                .nickname {
                  color: #409EFF;
                }
              }

              .nickname {
                font-size: 14px;
                font-weight: bold;
              }

              .comment-time {
                font-size: 12px;
                color: #999;
              }
            }
          }
        }

        .comment-content {
          margin-bottom: 10px;

          .reply-info {
            color: #666;
            margin-bottom: 5px;

            .reply-name {
              color: #409EFF;
              font-weight: bold;
            }
          }

          .content-text {
            font-size: 14px;
            line-height: 1.5;
            white-space: pre-wrap;
          }
        }

        // 回复列表样式
        .reply-list {
          margin-top: 15px;
          margin-left: 20px;
          border-left: 3px solid #ebeef5;
          padding-left: 15px;

          .reply-item {
            padding: 10px 0;
            border-bottom: 1px dashed #eee;

            &:last-child {
              border-bottom: none;
            }

            .comment-header {
              .comment-actions {
                .el-button {
                  font-size: 12px;
                  padding: 4px 8px;
                }
              }
            }
          }
        }

        .reply-input {
          margin-top: 15px;
          background-color: #f5f7fa;
          padding: 10px;
          border-radius: 4px;

          .reply-actions {
            margin-top: 10px;
            text-align: right;
          }
        }
      }

      .pagination {
        margin-top: 20px;
        text-align: center;
      }
    }
  }

  .related-card {
    .card-header {
      font-weight: bold;
    }

    .related-list {
      .related-item {
        padding: 10px 0;
        border-bottom: 1px solid #eee;
        cursor: pointer;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          .related-title {
            color: #409EFF;
          }
        }

        .related-title {
          font-size: 14px;
          margin-bottom: 5px;
          transition: color 0.3s;
        }

        .related-meta {
          font-size: 12px;
          color: #999;
          display: flex;
          justify-content: space-between;

          .stats {
            .el-icon {
              margin-left: 10px;
              margin-right: 3px;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .post-detail-container {
    .post-card {
      .post-header {
        .post-meta {
          flex-direction: column;
          align-items: flex-start;

          .author-info {
            margin-bottom: 10px;
          }
        }
      }

      .post-content {
        .content-images {
          .content-image {
            width: calc(50% - 5px);
            height: 150px;
          }
        }
      }
    }
  }
}
</style>
