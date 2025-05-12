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
                v-else
                v-for="conversation in conversations"
                :key="conversation.fromUserId"
                class="conversation-item"
                :class="{ active: currentConversation && currentConversation.fromUserId === conversation.fromUserId }"
                @click="selectConversation(conversation)"
              >
                <el-avatar :src="formatAvatarUrl(conversation.fromAvatar)" :size="40" />
                <div class="conversation-info">
                  <div class="conversation-name">
                    {{ conversation.fromNickname || conversation.fromUsername }}
                    <el-badge v-if="conversation.unreadCount > 0" :value="conversation.unreadCount" class="unread-badge" />
                  </div>
                  <div class="conversation-preview">{{ conversation.lastMessage }}</div>
                </div>
                <div class="conversation-meta">
                  <div class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</div>
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
            <div v-if="activeTab === 'notification'" class="notification-items">
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
                  :class="{ 'unread': !notification.isRead }"
                  @click="handleNotificationClick(notification)"
                >
                  <el-avatar :src="formatAvatarUrl(notification.operateAvatar)" :size="40" />
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
                <span>{{ currentConversation.fromNickname || currentConversation.fromUsername }}</span>
              </div>
            </div>

            <div class="chat-messages" ref="messagesRef">
              <div v-if="chatLoading" class="loading-container">
                <el-skeleton :rows="3" animated v-for="i in 3" :key="i" style="margin-bottom: 20px;" />
              </div>

              <template v-else>
                <div v-if="messages.length === 0" class="empty-messages">
                  <el-empty description="暂无消息记录" />
                </div>

                <template v-else>
                  <!-- 日期显示 -->
                  <div class="date-header">
                    <span>{{ formatDate(messages[0]?.createdAt) || '今天' }}</span>
                  </div>

                  <!-- 消息列表 -->
                  <div class="messages-list">
                    <div v-for="message in messages" :key="message.id" class="message-item">
                      <!-- 对方发送的消息 -->
                      <div v-if="message.fromUserId !== merchantInfo.id" class="message-row other">
                        <div class="avatar-wrapper">
                          <el-avatar
                            :src="formatAvatarUrl(message.fromAvatar)"
                            :size="40"
                            class="avatar"
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
                            :src="formatAvatarUrl(merchantInfo.avatar)"
                            :size="40"
                            class="avatar"
                          ></el-avatar>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </template>
            </div>

            <div class="chat-input">
              <div class="input-area">
                <el-input
                  v-model="messageInput"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入消息内容..."
                  resize="none"
                  @keydown.enter.prevent="sendMessage"
                />
                <el-button type="primary" @click="sendMessage" :disabled="!messageInput.trim()">
                  发送
                </el-button>
              </div>
            </div>
          </template>

          <div v-else class="empty-chat">
            <el-empty description="请选择联系人开始聊天" />
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
import { Delete } from '@element-plus/icons-vue'
import { useMerchantStore } from '../../stores/merchant'
import { formatImageUrl } from '../../utils/image'
import { getConversationList, getMessageList, sendMessage as apiSendMessage, markConversationAsRead, deleteConversation as apiDeleteConversation } from '../../api/message'
import { getNotificationList, markNotificationAsRead, markAllNotificationsAsRead as apiMarkAllNotificationsAsRead } from '../../api/message'
import wsClient from '../../utils/websocket'
import defaultAvatar from '../../assets/images/default-avatar.png'

const route = useRoute()
const router = useRouter()
const merchantStore = useMerchantStore()

// 状态
const loading = ref(false)
const chatLoading = ref(false)

const messageInput = ref('')
const activeTab = ref('message')
const currentConversation = ref(null)
const conversations = ref([])
const messages = ref([])
const messagesRef = ref(null)

// 通知相关
const notifications = ref([])
const notificationPage = ref(1)
const notificationSize = ref(10)
const notificationTotal = ref(0)

// 消息相关
const messagePage = ref(1)
const messageSize = ref(20)
const messageTotal = ref(0)

// 计算属性
const merchantInfo = computed(() => merchantStore.merchantInfo)
const hasUnreadNotifications = computed(() => {
  return notifications.value.some(notification => !notification.isRead)
})

// 监听路由参数变化
watch(
  () => route.query,
  (query) => {
    if (query.toUserId) {
      // 从其他页面跳转过来，打开指定用户的私信
      const userId = Number(query.toUserId)
      openConversationWithUser(userId)
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
    conversations.value = res.data
  } catch (error) {
    console.error('获取会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开与指定用户的会话
async function openConversationWithUser(userId) {
  // 先查找是否已有该用户的会话
  let conversation = conversations.value.find(c => c.fromUserId === userId)

  if (!conversation) {
    // 如果没有，创建一个新会话
    try {
      // 获取用户信息
      const userInfo = await getUserInfo(userId)

      // 创建新会话
      conversation = {
        fromUserId: userId,
        fromUsername: userInfo.username,
        fromNickname: userInfo.nickname,
        fromAvatar: userInfo.avatar,
        toUserId: merchantInfo.value.id,
        lastMessage: '',
        lastMessageTime: new Date().toISOString(),
        unreadCount: 0
      }

      // 添加到会话列表
      conversations.value.unshift(conversation)
      selectConversation(conversation)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    }
  } else {
    selectConversation(conversation)
  }
}

// 模拟获取用户信息
async function getUserInfo(userId) {
  // 实际项目中应该调用API获取用户信息
  return {
    id: userId,
    username: `user${userId}`,
    nickname: `用户${userId}`,
    avatar: ''
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
      await markConversationAsRead(conversation.fromUserId)
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
    // 确保fromUserId参数存在
    if (!currentConversation.value.fromUserId) {
      console.error('fromUserId不存在', currentConversation.value)
      ElMessage.error('获取消息失败：用户ID不存在')
      chatLoading.value = false
      return
    }

    const res = await getMessageList({
      targetUserId: currentConversation.value.fromUserId,
      page: messagePage.value,
      size: messageSize.value
    })

    if (messagePage.value === 1) {
      messages.value = res.data.records.reverse()
    } else {
      messages.value = [...res.data.records.reverse(), ...messages.value]
    }

    messageTotal.value = res.data.total
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
  } finally {
    chatLoading.value = false
  }
}

// 发送消息
async function sendMessage() {
  if (!messageInput.value.trim() || !currentConversation.value) return

  try {
    const content = messageInput.value

    // 添加到本地消息列表
    const newMessage = {
      id: Date.now(),
      fromUserId: merchantInfo.value.id,
      fromUsername: merchantInfo.value.username,
      fromNickname: merchantInfo.value.nickname,
      fromAvatar: merchantInfo.value.avatar,
      toUserId: currentConversation.value.fromUserId,
      content: content,
      createdAt: new Date().toISOString()
    }

    messages.value.push(newMessage)

    // 清空输入框
    messageInput.value = ''

    // 滚动到底部
    await nextTick()
    scrollToBottom()

    // 尝试通过WebSocket发送消息
    const wsSuccess = wsClient.sendMessage(currentConversation.value.fromUserId, content)

    // 如果WebSocket发送失败，使用HTTP API发送
    if (!wsSuccess) {
      await apiSendMessage(currentConversation.value.fromUserId, content)
    }

    // 更新会话的最后消息
    currentConversation.value.lastMessage = content
    currentConversation.value.lastMessageTime = new Date().toISOString()
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
function formatTime(timeStr) {
  if (!timeStr) return ''

  const date = new Date(timeStr)
  const now = new Date()

  // 如果是今天
  if (date.toDateString() === now.toDateString()) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }

  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }

  // 如果是今年
  if (date.getFullYear() === now.getFullYear()) {
    return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }

  // 其他情况
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化头像URL
function formatAvatarUrl(url) {
  if (!url) return defaultAvatar

  // 调试日志
  console.log('商家端处理头像URL:', url)

  try {
    return formatImageUrl(url)
  } catch (error) {
    console.error('商家端头像URL处理失败:', error)
    return defaultAvatar
  }
}



// 删除会话
async function deleteConversationHandler(conversation) {
  try {
    await apiDeleteConversation(conversation.fromUserId)

    // 从会话列表中移除
    const index = conversations.value.findIndex(conv => conv.fromUserId === conversation.fromUserId)
    if (index !== -1) {
      conversations.value.splice(index, 1)
    }

    // 如果当前选中的会话被删除，清空当前会话
    if (currentConversation.value && currentConversation.value.fromUserId === conversation.fromUserId) {
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
    console.log('商家端开始获取通知列表...')
    const res = await getNotificationList({
      page: notificationPage.value,
      size: notificationSize.value
    })

    console.log('商家端通知API返回数据:', res.data)

    if (res.data && res.data.records) {
      notifications.value = res.data.records
      notificationTotal.value = res.data.total

      // 详细调试日志
      console.log('商家端通知数据:', notifications.value)

      // 检查第一条通知的字段
      if (notifications.value.length > 0) {
        const firstNotification = notifications.value[0]
        console.log('商家端第一条通知详情:', {
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
      console.error('商家端通知API返回数据格式不正确:', res)
      notifications.value = []
      notificationTotal.value = 0
    }
  } catch (error) {
    console.error('商家端获取通知列表失败:', error)
    notifications.value = []
    notificationTotal.value = 0
  } finally {
    loading.value = false
  }
}

// 处理通知点击
async function handleNotificationClick(notification) {
  if (!notification.isRead) {
    try {
      await markNotificationAsRead(notification.id)
      notification.isRead = true
    } catch (error) {
      console.error('标记通知已读失败:', error)
    }
  }

  // 根据通知类型处理点击事件
  if (notification.type === 1) {
    // 私信通知
    router.push({
      path: '/message',
      query: { toUserId: notification.fromUserId }
    })
  } else if (notification.type === 2 || notification.type === 3) {
    // 评论通知
    router.push({
      path: '/community/post',
      query: { id: notification.sourceId }
    })
  } else if (notification.type === 4) {
    // 点赞通知
    router.push({
      path: '/community/post',
      query: { id: notification.sourceId }
    })
  } else if (notification.type === 5) {
    // 收藏通知
    router.push({
      path: '/community/post',
      query: { id: notification.sourceId }
    })
  } else if (notification.type === 6) {
    // 订单通知
    router.push({
      path: '/order',
      query: { id: notification.sourceId }
    })
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
    console.error('标记所有通知已读失败:', error)
    ElMessage.error('操作失败')
  }
}

// 处理通知分页大小变化
function handleNotificationSizeChange() {
  notificationPage.value = 1
  fetchNotifications()
}

// 处理通知页码变化
function handleNotificationCurrentChange() {
  fetchNotifications()
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
    if (currentConversation.value && currentConversation.value.fromUserId === data.fromUserId) {
      messages.value.push({
        id: data.messageId || Date.now(),
        fromUserId: data.fromUserId,
        fromUsername: currentConversation.value.fromUsername,
        fromNickname: currentConversation.value.fromNickname,
        fromAvatar: currentConversation.value.fromAvatar,
        toUserId: merchantInfo.value.id,
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
    const conversation = conversations.value.find(conv => conv.fromUserId === data.fromUserId)
    if (conversation) {
      // 更新现有会话
      conversation.lastMessage = data.content
      conversation.lastMessageTime = new Date().toISOString()

      // 如果不是当前会话，增加未读数
      if (!currentConversation.value || currentConversation.value.fromUserId !== data.fromUserId) {
        conversation.unreadCount = (conversation.unreadCount || 0) + 1
      }

      // 将会话移到顶部
      const index = conversations.value.findIndex(conv => conv.fromUserId === data.fromUserId)
      if (index > 0) {
        conversations.value.splice(index, 1)
        conversations.value.unshift(conversation)
      }
    } else {
      // 创建新会话
      const newConversation = {
        fromUserId: data.fromUserId,
        fromUsername: data.fromUsername || `用户${data.fromUserId}`,
        fromNickname: data.fromNickname || `用户${data.fromUserId}`,
        fromAvatar: data.fromAvatar || '',
        toUserId: merchantInfo.value.id,
        lastMessage: data.content,
        lastMessageTime: new Date().toISOString(),
        unreadCount: 1
      }

      conversations.value.unshift(newConversation)
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
          padding: 0;

          .conversation-item,
          .notification-item {
            display: flex;
            align-items: center;
            padding: 15px;
            cursor: pointer;
            transition: background-color 0.3s;
            border-bottom: 1px solid #f5f5f5;

            &:hover {
              background-color: #f5f7fa;
            }

            &.active {
              background-color: #ecf5ff;
            }

            &.unread {
              background-color: #f0f9eb;
            }

            .conversation-info,
            .notification-content {
              flex: 1;
              margin-left: 10px;
              overflow: hidden;

              .conversation-name,
              .notification-text {
                font-weight: bold;
                margin-bottom: 5px;
                display: flex;
                align-items: center;

                .unread-badge {
                  margin-left: 5px;
                }
              }

              .conversation-preview {
                color: #999;
                font-size: 12px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }

              .notification-time {
                font-size: 12px;
                color: #999;
                margin-top: 5px;
              }
            }

            .conversation-meta {
              display: flex;
              flex-direction: column;
              align-items: flex-end;
              min-width: 60px;

              .conversation-time {
                font-size: 12px;
                color: #999;
                white-space: nowrap;
                margin-bottom: 5px;
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
        }

        .pagination {
          padding: 10px;
          text-align: center;
        }
      }

      .chat-area {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        padding: 0;

        .chat-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px;
          border-bottom: 1px solid #eee;

          .chat-title {
            font-weight: bold;
            font-size: 16px;
          }
        }

        .chat-messages {
          flex: 1;
          padding: 0;
          overflow-y: auto;
          background-color: #f9fafc;
          background-image: linear-gradient(
            to bottom,
            rgba(255, 255, 255, 0.9),
            rgba(240, 245, 255, 0.8)
          );
          display: flex;
          flex-direction: column;
          align-items: stretch;
          box-sizing: border-box;
          width: 100%;
          padding-left: 0;
          padding-right: 0;

          .loading-container {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100%;
          }

          .empty-messages {
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
          }

          .date-header {
            text-align: center;
            margin: 15px 0;
            padding: 0;

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
        }

        .chat-input {
          padding: 15px 20px;
          border-top: 1px solid #ebeef5;
          background-color: #fff;
          box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);

          .input-toolbar {
            display: flex;
            margin-bottom: 10px;
          }

          .input-area {
            display: flex;
            align-items: flex-end;

            .el-textarea {
              flex: 1;
              margin-right: 12px;

              :deep(.el-textarea__inner) {
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

  .product-search {
    margin-bottom: 15px;
  }

  .pagination-container {
    margin-top: 15px;
    text-align: right;
    padding: 10px;
  }
}
</style>
