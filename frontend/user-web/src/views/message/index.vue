<template>
  <div class="message-container">
    <el-card class="message-card">
      <div class="message-layout">
        <!-- 左侧会话列表 -->
        <div class="conversation-list">
          <div class="list-header">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="私信" name="message"></el-tab-pane>
              <el-tab-pane label="通知" name="notification"></el-tab-pane>
            </el-tabs>
          </div>



          <div v-if="loading" class="loading">
            <el-skeleton :rows="3" animated v-for="i in 5" :key="i" style="margin-bottom: 20px;" />
          </div>

          <template v-else>
            <!-- 私信会话列表 -->
            <div v-if="activeTab === 'message'" class="conversation-items">
              <el-empty v-if="conversations.length === 0" description="暂无私信" />

              <div
                v-for="conversation in conversations"
                :key="conversation.id"
                class="conversation-item"
                :class="{ active: currentConversation?.id === conversation.id }"
                @click="selectConversation(conversation)"
              >
                <el-badge :is-dot="conversation.unreadCount > 0" class="avatar-badge">
                  <el-avatar
                    :src="formatAvatarUrl(conversation.avatar) || defaultAvatar"
                    :size="40"
                    @click.stop="(event) => goToUserProfile(conversation.targetUserId, event)"
                    class="clickable-avatar"
                  ></el-avatar>
                </el-badge>
                <div class="conversation-info">
                  <div class="conversation-name">{{ conversation.nickname || conversation.username }}</div>
                  <div class="conversation-preview">{{ conversation.lastMessage }}</div>
                </div>
                <div class="conversation-meta">
                  <div class="conversation-time">{{ formatTime(conversation.lastTime) }}</div>
                  <div v-if="conversation.unreadCount > 0" class="unread-count">
                    {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
                  </div>
                  <el-popconfirm
                    title="确定要删除此会话吗？"
                    confirm-button-text="确定"
                    cancel-button-text="取消"
                    @confirm="deleteConversationHandler(conversation)"
                    @click.stop
                  >
                    <template #reference>
                      <el-button
                        type="danger"
                        size="small"
                        circle
                        class="delete-btn"
                        @click.stop
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
            </div>

            <!-- 通知列表 -->
            <div v-else-if="activeTab === 'notification'" class="notification-items">
              <el-empty v-if="notifications.length === 0" description="暂无通知" />

              <div v-else>
                <div class="notification-actions">
                  <el-button type="primary" text @click="markAllNotificationsAsRead" :disabled="!hasUnreadNotifications">
                    全部已读
                  </el-button>
                </div>

                <div
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.isRead }"
                  @click="handleNotificationClick(notification)"
                >
                  <el-avatar
                    :src="formatAvatarUrl(notification.operateAvatar) || defaultAvatar"
                    :size="40"
                    @click.stop="(event) => notification.operateUserId && goToUserProfile(notification.operateUserId, event)"
                    class="clickable-avatar"
                  ></el-avatar>
                  <div class="notification-content">
                    <div class="notification-text" v-html="notification.content"></div>
                    <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
                  </div>
                </div>

                <div class="pagination">
                  <el-pagination
                    v-model:current-page="notificationPage"
                    v-model:page-size="notificationSize"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next"
                    :total="notificationTotal"
                    @size-change="handleNotificationSizeChange"
                    @current-change="handleNotificationCurrentChange"
                  />
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 右侧聊天区域 -->
        <div class="chat-area">
          <template v-if="currentConversation">
            <div class="chat-header">
              <div class="chat-title">
                <span>{{ currentConversation.nickname || currentConversation.username }}</span>
              </div>
              <div class="chat-actions">
                <el-button type="primary" text @click="(event) => goToUserProfile(currentConversation.targetUserId, event)">
                  <el-icon><User /></el-icon>
                  查看资料
                </el-button>
              </div>
            </div>

            <div class="chat-messages" ref="messagesRef">
              <div v-if="chatLoading" class="loading">
                <el-skeleton :rows="3" animated v-for="i in 5" :key="i" style="margin-bottom: 20px;" />
              </div>

              <el-empty v-else-if="messages.length === 0" description="暂无消息" />

              <template v-else>
                <!-- 日期显示 -->
                <div class="date-header">
                  <span>{{ formatDate(messages[0]?.createdAt) || '今天' }}</span>
                </div>

                <!-- 消息列表 -->
                <div class="messages-list">
                  <div v-for="(message, index) in messages" :key="message.id" class="message-item">
                    <!-- 对方发送的消息 -->
                    <div v-if="message.fromUserId !== userInfo.id" class="message-row other">
                      <div class="avatar-wrapper">
                        <el-avatar
                          :src="formatAvatarUrl(currentConversation.avatar) || defaultAvatar"
                          :size="40"
                          class="avatar"
                          @click.stop="(event) => goToUserProfile(message.fromUserId, event)"
                        ></el-avatar>
                      </div>
                      <div class="message-content">
                        <div class="message-bubble">
                          <div class="message-text">{{ message.content }}</div>
                        </div>
                        <div class="message-time">{{ formatMessageTime(message.createdAt) }}</div>
                      </div>
                    </div>

                    <!-- 自己发送的消息 -->
                    <div v-else class="message-row self">
                      <div class="message-content">
                        <div class="message-bubble">
                          <div class="message-text">{{ message.content }}</div>
                        </div>
                        <div class="message-time">{{ formatMessageTime(message.createdAt) }}</div>
                      </div>
                      <div class="avatar-wrapper">
                        <el-avatar
                          :src="formatAvatarUrl(userInfo.avatar) || defaultAvatar"
                          :size="40"
                          class="avatar"
                        ></el-avatar>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="load-more" v-if="hasMoreMessages">
                  <el-button type="primary" text @click="loadMoreMessages" :loading="loadingMore">
                    加载更多
                  </el-button>
                </div>
              </template>
            </div>

            <div class="chat-input">
              <el-input
                v-model="messageContent"
                type="textarea"
                :rows="3"
                placeholder="请输入消息"
                maxlength="500"
                show-word-limit
                @keyup.enter.ctrl="sendMessage"
              />
              <div class="input-actions">
                <span class="tip">按 Ctrl + Enter 发送</span>
                <el-button type="primary" @click="sendMessage" :disabled="!messageContent.trim()">
                  发送
                </el-button>
              </div>
            </div>
          </template>

          <div v-else class="empty-chat">
            <el-empty description="选择一个联系人开始聊天" />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getConversationList, getMessageList, sendMessage as apiSendMessage, markConversationAsRead, deleteConversation as apiDeleteConversation } from '../../api/message'
import { getNotificationList, markNotificationAsRead, markAllNotificationsAsRead as apiMarkAllNotificationsAsRead } from '../../api/message'
import { getCommentDetail } from '../../api/community'
import { getLikeDetail } from '../../api/like'
import { getUserProfile } from '../../api/profile'
import { formatImageUrl } from '../../utils/image'
import wsClient from '../../utils/websocket'
import defaultAvatar from '../../assets/images/default-avatar.png'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const activeTab = ref('message')

const loading = ref(false)
const chatLoading = ref(false)
const loadingMore = ref(false)
const conversations = ref([])
const currentConversation = ref(null)
const messages = ref([])
const messageContent = ref('')
const messagesRef = ref(null)
const messagePage = ref(1)
const messageSize = ref(20)
const messageTotal = ref(0)
const hasMoreMessages = ref(false)

// 通知相关
const notifications = ref([])
const notificationPage = ref(1)
const notificationSize = ref(10)
const notificationTotal = ref(0)

// 计算属性
const userInfo = computed(() => userStore.userInfo)
const hasUnreadNotifications = computed(() => {
  return notifications.value.some(notification => !notification.isRead)
})

// 监听路由参数变化
watch(
  () => route.query,
  (query) => {
    if (query.toUserId) {
      // 从其他页面跳转过来，打开指定用户的私信
      const targetUserId = Number(query.toUserId)
      openConversationWithUser(targetUserId)
    }
  },
  { immediate: true }
)

// 生命周期钩子
onMounted(() => {
  fetchConversations()
  fetchNotifications()
})

// 监听标签页变化
watch(activeTab, (newTab) => {
  if (newTab === 'message') {
    fetchConversations()
  } else if (newTab === 'notification') {
    fetchNotifications()
  }
})

// 方法
async function fetchConversations() {
  loading.value = true
  try {
    const res = await getConversationList()

    // 处理会话列表数据
    const conversationList = res.data.map(item => {
      // 获取当前登录用户的ID
      const currentUserId = userInfo.value.id

      // 确定目标用户ID、用户名、昵称和头像
      let targetUserId, targetUsername, targetNickname, targetAvatar

      if (item.fromUserId === currentUserId) {
        // 如果发送者是当前用户，则目标是接收者
        targetUserId = item.toUserId
        targetUsername = item.toUsername
        targetNickname = item.toNickname
        targetAvatar = item.toAvatar
      } else {
        // 如果发送者不是当前用户，则目标是发送者
        targetUserId = item.fromUserId
        targetUsername = item.fromUsername
        targetNickname = item.fromNickname
        targetAvatar = item.fromAvatar
      }

      // 返回处理后的会话对象
      return {
        id: item.id,
        targetUserId: targetUserId,
        username: targetUsername,
        nickname: targetNickname,
        avatar: targetAvatar,
        lastMessage: item.lastMessage,
        lastTime: item.lastMessageTime,
        unreadCount: item.unreadCount || 0
      }
    })

    conversations.value = conversationList
    console.log('处理后的会话列表:', conversationList)

    // 如果有未读消息，自动选中第一个有未读消息的会话
    if (!currentConversation.value) {
      const unreadConversation = conversations.value.find(conv => conv.unreadCount > 0)
      if (unreadConversation) {
        selectConversation(unreadConversation)
      } else if (conversations.value.length > 0 && !route.query.toUserId) {
        selectConversation(conversations.value[0])
      }
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开与指定用户的会话
async function openConversationWithUser(targetUserId) {
  // 查找是否已有该用户的会话
  let conversation = conversations.value.find(conv => conv.targetUserId === targetUserId)

  if (conversation) {
    selectConversation(conversation)
  } else {
    try {
      // 获取用户信息
      const res = await getUserProfile(targetUserId)
      const userProfile = res.data

      // 创建新会话
      conversation = {
        id: Date.now(),
        targetUserId: targetUserId,
        username: userProfile.username || '用户' + targetUserId,
        nickname: userProfile.nickname || '',
        avatar: userProfile.avatar || '',
        lastMessage: '',
        lastTime: new Date(),
        unreadCount: 0
      }

      conversations.value.unshift(conversation)
      selectConversation(conversation)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')

      // 创建一个基本会话
      conversation = {
        id: Date.now(),
        targetUserId: targetUserId,
        username: '用户' + targetUserId,
        nickname: '',
        avatar: '',
        lastMessage: '',
        lastTime: new Date(),
        unreadCount: 0
      }

      conversations.value.unshift(conversation)
      selectConversation(conversation)
    }
  }
}

// 选择会话
async function selectConversation(conversation) {
  currentConversation.value = conversation
  messagePage.value = 1
  messages.value = []

  await fetchMessages()

  // 标记会话为已读
  if (conversation.unreadCount > 0) {
    try {
      await markConversationAsRead(conversation.targetUserId)
      conversation.unreadCount = 0
    } catch (error) {
      console.error('标记会话已读失败:', error)
    }
  }

  // 滚动到底部
  await nextTick()
  scrollToBottom()
}

// 获取消息列表
async function fetchMessages() {
  if (!currentConversation.value) return

  chatLoading.value = true
  try {
    // 确保targetUserId参数存在
    if (!currentConversation.value.targetUserId) {
      console.error('targetUserId不存在', currentConversation.value)
      ElMessage.error('获取消息失败：目标用户ID不存在')
      chatLoading.value = false
      return
    }

    const res = await getMessageList({
      targetUserId: currentConversation.value.targetUserId,
      page: messagePage.value,
      size: messageSize.value
    })

    if (messagePage.value === 1) {
      messages.value = res.data.records.reverse()
    } else {
      messages.value = [...res.data.records.reverse(), ...messages.value]
    }

    messageTotal.value = res.data.total
    hasMoreMessages.value = messagePage.value * messageSize.value < messageTotal.value
  } catch (error) {
    console.error('获取消息列表失败:', error)
    if (error.response && error.response.status === 400) {
      ElMessage.error('获取消息失败：缺少必要参数')
    } else {
      ElMessage.error('获取消息列表失败，请稍后重试')
    }
  } finally {
    chatLoading.value = false
  }
}

// 加载更多消息
async function loadMoreMessages() {
  if (loadingMore.value || !hasMoreMessages.value) return

  loadingMore.value = true
  try {
    messagePage.value++
    await fetchMessages()
  } finally {
    loadingMore.value = false
  }
}

// 发送消息
async function sendMessage() {
  if (!messageContent.value.trim() || !currentConversation.value) return

  try {
    const content = messageContent.value
    messageContent.value = ''

    // 先添加到本地消息列表
    const tempMessage = {
      id: 'temp-' + Date.now(),
      fromUserId: userInfo.value.id,
      toUserId: currentConversation.value.targetUserId,
      content: content,
      createdAt: new Date()
    }
    messages.value.push(tempMessage)

    // 滚动到底部
    await nextTick()
    scrollToBottom()

    // 尝试通过WebSocket发送消息
    const wsSuccess = wsClient.sendMessage(currentConversation.value.targetUserId, content)

    // 如果WebSocket发送失败，使用HTTP API发送
    if (!wsSuccess) {
      await apiSendMessage(currentConversation.value.targetUserId, content)
    }

    // 更新会话列表
    currentConversation.value.lastMessage = content
    currentConversation.value.lastTime = new Date()

    // 将会话移到顶部
    const index = conversations.value.findIndex(conv => conv.id === currentConversation.value.id)
    if (index > 0) {
      conversations.value.splice(index, 1)
      conversations.value.unshift(currentConversation.value)
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  }
}

// 滚动到底部
function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}



// 删除会话
async function deleteConversationHandler(conversation) {
  try {
    await apiDeleteConversation(conversation.targetUserId)

    // 从会话列表中移除
    const index = conversations.value.findIndex(conv => conv.id === conversation.id)
    if (index !== -1) {
      conversations.value.splice(index, 1)
    }

    // 如果当前选中的会话被删除，清空当前会话
    if (currentConversation.value && currentConversation.value.id === conversation.id) {
      currentConversation.value = null
      messages.value = []
    }

    ElMessage.success('会话已删除')
  } catch (error) {
    console.error('删除会话失败:', error)
    ElMessage.error('删除会话失败')
  }
}

// 获取通知列表
async function fetchNotifications() {
  loading.value = true
  try {
    console.log('开始获取通知列表...')
    const res = await getNotificationList({
      page: notificationPage.value,
      size: notificationSize.value
    })

    console.log('通知API返回数据:', res.data)

    if (res.data && res.data.records) {
      notifications.value = res.data.records
      notificationTotal.value = res.data.total

      // 详细调试日志
      console.log('通知数据:', notifications.value)

      // 检查第一条通知的字段
      if (notifications.value.length > 0) {
        const firstNotification = notifications.value[0]
        console.log('第一条通知详情:', {
          id: firstNotification.id,
          type: firstNotification.type,
          content: firstNotification.content,
          isRead: firstNotification.isRead,
          createdAt: firstNotification.createdAt,
          operateUserId: firstNotification.operateUserId,
          operateUsername: firstNotification.operateUsername,
          operateNickname: firstNotification.operateNickname,
          operateAvatar: firstNotification.operateAvatar
        })
      }
    } else {
      console.error('通知API返回数据格式不正确:', res)
      notifications.value = []
      notificationTotal.value = 0
    }
  } catch (error) {
    console.error('获取通知列表失败:', error)
    notifications.value = []
    notificationTotal.value = 0
  } finally {
    loading.value = false
  }
}

// 处理通知点击
async function handleNotificationClick(notification) {
  // 标记为已读
  if (!notification.isRead) {
    try {
      await markNotificationAsRead(notification.id)
      notification.isRead = true
    } catch (error) {
      console.error('标记通知已读失败:', error)
    }
  }

  try {
    // 根据通知类型跳转
    if (notification.type === 1) {
      // 点赞通知 - 需要查询点赞记录获取实际目标
      const likeRes = await getLikeDetail(notification.targetId)
      if (likeRes.data) {
        const likeData = likeRes.data
        if (likeData.type === 1) {
          // 帖子点赞
          router.push(`/post/${likeData.targetId}`)
        } else if (likeData.type === 2) {
          // 评论点赞 - 需要查询评论获取帖子ID
          const commentRes = await getCommentDetail(likeData.targetId)
          if (commentRes.data) {
            router.push(`/post/${commentRes.data.postId}`)
          }
        }
      }
    } else if (notification.type === 2) {
      // 评论通知 - 直接查询评论获取帖子ID
      const commentRes = await getCommentDetail(notification.targetId)
      if (commentRes.data) {
        router.push(`/post/${commentRes.data.postId}`)
      }
    } else if (notification.type === 3) {
      // 回复通知 - 直接查询评论获取帖子ID
      const commentRes = await getCommentDetail(notification.targetId)
      if (commentRes.data) {
        router.push(`/post/${commentRes.data.postId}`)
      }
    } else if (notification.type === 4) {
      // 系统通知
      // 不跳转
    }
  } catch (error) {
    console.error('处理通知跳转失败:', error)
    ElMessage.error('跳转失败，请稍后再试')
  }
}

// 标记所有通知为已读
async function markAllNotificationsAsRead() {
  try {
    await apiMarkAllNotificationsAsRead()
    notifications.value.forEach(notification => {
      notification.isRead = true
    })
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理通知分页大小变化
function handleNotificationSizeChange() {
  fetchNotifications()
}

// 处理通知页码变化
function handleNotificationCurrentChange() {
  fetchNotifications()
}

// 跳转到用户资料页
function goToUserProfile(userId, event) {
  if (!userId) {
    console.error('用户ID不存在')
    ElMessage.error('用户ID不存在')
    return
  }

  // 防止事件冒泡
  if (event) {
    event.stopPropagation()
  }

  // 确保userId是数字
  const id = Number(userId)
  if (isNaN(id)) {
    console.error('无效的用户ID:', userId)
    ElMessage.error('无效的用户ID')
    return
  }

  console.log('跳转到用户资料页:', id)

  // 跳转到用户资料页
  router.push({
    path: '/community',
    query: { userId: id }
  })
}

// 判断是否显示日期分割线
function showDateDivider(message, index) {
  if (index === 0) return true

  const prevMessage = messages.value[index - 1]
  const prevDate = new Date(prevMessage.createdAt).toDateString()
  const currentDate = new Date(message.createdAt).toDateString()

  return prevDate !== currentDate
}

// 格式化日期
function formatDate(time) {
  if (!time) return ''

  const date = new Date(time)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
}

// 格式化消息时间
function formatMessageTime(time) {
  if (!time) return ''

  const date = new Date(time)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
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

  // 小于24小时
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }

  // 小于7天
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }

  // 格式化日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化头像URL
function formatAvatarUrl(url) {
  if (!url) return defaultAvatar

  // 调试日志
  console.log('处理头像URL:', url)

  try {
    return formatImageUrl(url)
  } catch (error) {
    console.error('头像URL处理失败:', error)
    return defaultAvatar
  }
}

// 监听标签页变化
watch(activeTab, (newTab) => {
  if (newTab === 'message') {
    fetchConversations()
  } else if (newTab === 'notification') {
    fetchNotifications()
  }
})

// 处理WebSocket消息
function handleWebSocketMessage(data) {
  console.log('收到WebSocket消息:', data)

  // 如果是私信消息
  if (data.type === 'message') {
    // 如果当前正在与发送者聊天，则直接添加到消息列表
    if (currentConversation.value && currentConversation.value.targetUserId === data.fromUserId) {
      messages.value.push({
        id: data.messageId || Date.now(),
        fromUserId: data.fromUserId,
        toUserId: userInfo.value.id,
        content: data.content,
        createdAt: new Date().toISOString()
      })

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })

      // 标记为已读
      markConversationAsRead(data.fromUserId).catch(error => {
        console.error('标记会话已读失败:', error)
      })
    }

    // 更新会话列表
    const conversation = conversations.value.find(conv => conv.targetUserId === data.fromUserId)
    if (conversation) {
      // 更新现有会话
      conversation.lastMessage = data.content
      conversation.lastTime = new Date()

      // 如果不是当前会话，增加未读数
      if (!currentConversation.value || currentConversation.value.targetUserId !== data.fromUserId) {
        conversation.unreadCount = (conversation.unreadCount || 0) + 1
      }

      // 将会话移到顶部
      const index = conversations.value.findIndex(conv => conv.targetUserId === data.fromUserId)
      if (index > 0) {
        conversations.value.splice(index, 1)
        conversations.value.unshift(conversation)
      }
    } else {
      // 获取用户信息并创建新会话
      getUserProfile(data.fromUserId).then(res => {
        const userProfile = res.data
        const newConversation = {
          id: Date.now(),
          targetUserId: data.fromUserId,
          username: userProfile.username || '用户' + data.fromUserId,
          nickname: userProfile.nickname || '',
          avatar: userProfile.avatar || '',
          lastMessage: data.content,
          lastTime: new Date(),
          unreadCount: 1
        }

        conversations.value.unshift(newConversation)
      }).catch(error => {
        console.error('获取用户信息失败:', error)
      })
    }
  }
}

// 处理WebSocket通知
function handleWebSocketNotification(data) {
  console.log('收到WebSocket通知:', data)

  // 如果当前在通知标签页，刷新通知列表
  if (activeTab.value === 'notification') {
    fetchNotifications()
  }
}

// 生命周期钩子
onMounted(() => {
  fetchConversations()

  // 添加WebSocket事件监听
  wsClient.on('message', handleWebSocketMessage)
  wsClient.on('notification', handleWebSocketNotification)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  wsClient.off('message', handleWebSocketMessage)
  wsClient.off('notification', handleWebSocketNotification)
})
</script>

<style lang="scss" scoped>
.message-container {
  max-width: 1200px;
  margin: 0 auto;

  .message-card {
    .message-layout {
      display: flex;
      height: 600px;

      .conversation-list {
        width: 300px;
        border-right: 1px solid #eee;
        display: flex;
        flex-direction: column;

        .list-header {
          padding: 0 15px;
        }

        .list-search {
          padding: 10px 15px;
          border-bottom: 1px solid #eee;
        }

        .loading {
          padding: 15px;
          flex: 1;
          overflow-y: auto;
        }

        .conversation-items,
        .notification-items {
          flex: 1;
          overflow-y: auto;

          .conversation-item {
            display: flex;
            align-items: center;
            padding: 15px;
            cursor: pointer;
            transition: all 0.3s;
            border-bottom: 1px solid rgba(0, 0, 0, 0.03);

            &:hover {
              background-color: #f5f7fa;
            }

            &.active {
              background-color: #ecf5ff;
              border-left: 3px solid #409EFF;
            }

            .avatar-badge {
              margin-right: 12px;

              .clickable-avatar {
                cursor: pointer;
                transition: transform 0.2s;
                border: 2px solid #fff;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

                &:hover {
                  transform: scale(1.05);
                }
              }
            }

            .conversation-info {
              flex: 1;
              overflow: hidden;

              .conversation-name {
                font-weight: bold;
                margin-bottom: 5px;
                color: #333;
              }

              .conversation-preview {
                font-size: 12px;
                color: #666;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }
            }

            .conversation-meta {
              text-align: right;
              min-width: 60px;
              display: flex;
              flex-direction: column;
              align-items: flex-end;

              .conversation-time {
                font-size: 12px;
                color: #999;
                margin-bottom: 5px;
              }

              .unread-count {
                display: inline-block;
                background-color: #f56c6c;
                color: white;
                border-radius: 10px;
                min-width: 20px;
                height: 20px;
                line-height: 20px;
                text-align: center;
                font-size: 12px;
                padding: 0 6px;
              }

              .delete-btn {
                margin-top: 5px;
                opacity: 0;
                transition: opacity 0.3s;
              }
            }

            &:hover {
              .delete-btn {
                opacity: 1;
              }
            }
          }

          .notification-actions {
            padding: 10px 15px;
            text-align: right;
            border-bottom: 1px solid #eee;
          }

          .notification-item {
            display: flex;
            padding: 15px;
            cursor: pointer;
            transition: background-color 0.3s;

            &:hover {
              background-color: #f5f7fa;
            }

            &.unread {
              background-color: #f0f9ff;
            }

            .el-avatar {
              margin-right: 10px;

              &.clickable-avatar {
                cursor: pointer;
                transition: transform 0.2s;

                &:hover {
                  transform: scale(1.05);
                }
              }
            }

            .notification-content {
              flex: 1;

              .notification-text {
                margin-bottom: 5px;

                ::v-deep(a) {
                  color: #409EFF;
                  text-decoration: none;

                  &:hover {
                    text-decoration: underline;
                  }
                }
              }

              .notification-time {
                font-size: 12px;
                color: #999;
              }
            }
          }

          .pagination {
            padding: 15px;
            text-align: center;
          }
        }
      }

      .chat-area {
        flex: 1;
        display: flex;
        flex-direction: column;
        background-color: #f9f9f9;
        overflow: hidden;
        padding: 0;

        .chat-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px 20px;
          border-bottom: 1px solid #eee;
          background-color: #fff;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

          .chat-title {
            font-size: 16px;
            font-weight: bold;
            color: #333;
          }
        }

        .chat-messages {
          flex: 1;
          overflow-y: auto;
          background-color: #f9fafc;
          background-image: linear-gradient(
            to bottom,
            rgba(255, 255, 255, 0.9),
            rgba(240, 245, 255, 0.8)
          );
          display: flex;
          flex-direction: column;
          box-sizing: border-box;
          width: 100%;

          .loading {
            padding: 15px;
          }

          .date-header {
            text-align: center;
            margin: 15px 0;

            span {
              display: inline-block;
              padding: 4px 12px;
              font-size: 12px;
              color: #909399;
              background-color: #f2f6fc;
              border-radius: 12px;
            }
          }

          .messages-list {
            display: flex;
            flex-direction: column;
            width: 100%;
            box-sizing: border-box;
          }

          .message-item {
            width: 100%;
            margin-bottom: 16px;
            box-sizing: border-box;
          }

          .message-row {
            display: flex;
            width: 100%;
            box-sizing: border-box;

            &.other {
              .avatar-wrapper {
                padding: 0;
                margin: 0;
              }

              .message-content {
                margin-left: 10px;
                max-width: calc(100% - 50px);
              }

              .message-bubble {
                background-color: #f2f6fc;
                color: #303133;
                border-radius: 4px;
                padding: 8px 12px;
                display: inline-block;
                word-break: break-word;
                line-height: 1.5;
              }

              .message-time {
                text-align: left;
                font-size: 12px;
                color: #909399;
                margin-top: 4px;
              }
            }

            &.self {
              flex-direction: row-reverse;

              .avatar-wrapper {
                padding: 0;
                margin: 0;
              }

              .message-content {
                margin-right: 10px;
                max-width: calc(100% - 50px);
                display: flex;
                flex-direction: column;
                align-items: flex-end;
              }

              .message-bubble {
                background-color: #ecf5ff;
                color: #409EFF;
                border-radius: 4px;
                padding: 8px 12px;
                display: inline-block;
                word-break: break-word;
                line-height: 1.5;
              }

              .message-time {
                text-align: right;
                font-size: 12px;
                color: #909399;
                margin-top: 4px;
              }
            }
          }

          .avatar-wrapper {
            width: 40px;
            height: 40px;
            flex-shrink: 0;

            .avatar {
              cursor: pointer;
              transition: transform 0.2s;

              &:hover {
                transform: scale(1.05);
              }
            }
          }

          .load-more {
            text-align: center;
            margin-bottom: 15px;
          }
        }

        .chat-input {
          padding: 15px 20px;
          border-top: 1px solid #ebeef5;
          background-color: #fff;
          box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);

          .el-textarea :deep(.el-textarea__inner) {
            border-radius: 20px;
            padding: 12px 18px;
            resize: none;
            transition: all 0.3s;
            border-color: #dcdfe6;

            &:hover {
              border-color: #c0c4cc;
            }

            &:focus {
              border-color: #409EFF;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
            }
          }

          .input-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 12px;

            .tip {
              font-size: 12px;
              color: #909399;
            }

            .el-button {
              border-radius: 20px;
              padding: 9px 22px;
              font-weight: 500;
              transition: all 0.3s;

              &:hover {
                transform: translateY(-1px);
                box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
              }
            }
          }
        }

        .empty-chat {
          flex: 1;
          display: flex;
          justify-content: center;
          align-items: center;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .message-container {
    .message-card {
      .message-layout {
        flex-direction: column;
        height: auto;

        .conversation-list {
          width: 100%;
          height: 400px;
          border-right: none;
          border-bottom: 1px solid #eee;
        }

        .chat-area {
          height: 500px;
        }
      }
    }
  }
}
</style>
