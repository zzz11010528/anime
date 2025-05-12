<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="profile-header">
          <h2>个人中心</h2>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人资料" name="info">
          <div class="profile-info">
            <div class="avatar-container">
              <el-avatar :size="100" :src="formatAvatarUrl(userInfo.avatar) || defaultAvatar"></el-avatar>
              <el-upload
                class="avatar-uploader"
                action="/api/file/upload/avatar"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-button size="small" type="primary">更换头像</el-button>
              </el-upload>
            </div>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled></el-input>
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileForm.nickname"></el-input>
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone"></el-input>
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email"></el-input>
              </el-form-item>

              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                  <el-radio :label="0">保密</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="个人简介">
                <el-input v-model="profileForm.bio" type="textarea" rows="4"></el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updateProfile" :loading="loading">保存修改</el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="passwordLoading">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="collection">
          <div class="collection-container">
            <el-tabs v-model="collectionTab">
              <el-tab-pane label="收藏的商品" name="product">
                <div v-if="collectedProducts.length > 0" class="collection-list">
                  <el-row :gutter="20">
                    <el-col v-for="product in collectedProducts" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
                      <product-card :product="product" />
                    </el-col>
                  </el-row>
                  <div class="pagination">
                    <el-pagination
                      v-model:current-page="productPage"
                      v-model:page-size="productSize"
                      :page-sizes="[8, 16, 24, 32]"
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="productTotal"
                      @size-change="handleProductSizeChange"
                      @current-change="handleProductCurrentChange"
                    />
                  </div>
                </div>
                <el-empty v-else description="暂无收藏的商品" />
              </el-tab-pane>

              <el-tab-pane label="收藏的帖子" name="post">
                <div v-if="collectedPosts.length > 0" class="collection-list">
                  <el-row :gutter="20">
                    <el-col v-for="post in collectedPosts" :key="post.id" :xs="24" :sm="12" :md="8">
                      <post-card :post="post" />
                    </el-col>
                  </el-row>
                  <div class="pagination">
                    <el-pagination
                      v-model:current-page="postPage"
                      v-model:page-size="postSize"
                      :page-sizes="[6, 12, 18, 24]"
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="postTotal"
                      @size-change="handlePostSizeChange"
                      @current-change="handlePostCurrentChange"
                    />
                  </div>
                </div>
                <el-empty v-else description="暂无收藏的帖子" />
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的帖子" name="post">
          <div v-if="myPosts.length > 0" class="my-posts">
            <el-row :gutter="20">
              <el-col v-for="post in myPosts" :key="post.id" :xs="24" :sm="12" :md="8">
                <post-card :post="post" />
              </el-col>
            </el-row>
            <div class="pagination">
              <el-pagination
                v-model:current-page="myPostPage"
                v-model:page-size="myPostSize"
                :page-sizes="[6, 12, 18, 24]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="myPostTotal"
                @size-change="handleMyPostSizeChange"
                @current-change="handleMyPostCurrentChange"
              />
            </div>
          </div>
          <el-empty v-else description="暂无发布的帖子" />
        </el-tab-pane>

        <el-tab-pane label="我的评论" name="comment">
          <div v-if="myComments.length > 0" class="my-comments">
            <el-row :gutter="20">
              <el-col v-for="comment in myComments" :key="comment.id" :xs="24">
                <comment-card :comment="comment" />
              </el-col>
            </el-row>
            <div class="pagination">
              <el-pagination
                v-model:current-page="myCommentPage"
                v-model:page-size="myCommentSize"
                :page-sizes="[6, 12, 18, 24]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="myCommentTotal"
                @size-change="handleMyCommentSizeChange"
                @current-change="handleMyCommentCurrentChange"
              />
            </div>
          </div>
          <el-empty v-else description="暂无发表的评论" />
        </el-tab-pane>

        <el-tab-pane label="我的点赞" name="like">
          <div class="like-tabs">
            <el-tabs v-model="likeTab">
              <el-tab-pane label="帖子点赞" name="post">
                <div v-if="myLikedPosts.length > 0" class="my-likes">
                  <el-row :gutter="20">
                    <el-col v-for="like in myLikedPosts" :key="like.id" :xs="24" :sm="12" :md="8">
                      <post-card :post="like.target" />
                    </el-col>
                  </el-row>
                  <div class="pagination">
                    <el-pagination
                      v-model:current-page="myLikedPostPage"
                      v-model:page-size="myLikedPostSize"
                      :page-sizes="[6, 12, 18, 24]"
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="myLikedPostTotal"
                      @size-change="handleMyLikedPostSizeChange"
                      @current-change="handleMyLikedPostCurrentChange"
                    />
                  </div>
                </div>
                <el-empty v-else description="暂无点赞的帖子" />
              </el-tab-pane>

              <el-tab-pane label="评论点赞" name="comment">
                <div v-if="myLikedComments.length > 0" class="my-likes">
                  <el-row :gutter="20">
                    <el-col v-for="like in myLikedComments" :key="like.id" :xs="24">
                      <comment-card :comment="like.target" />
                    </el-col>
                  </el-row>
                  <div class="pagination">
                    <el-pagination
                      v-model:current-page="myLikedCommentPage"
                      v-model:page-size="myLikedCommentSize"
                      :page-sizes="[6, 12, 18, 24]"
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="myLikedCommentTotal"
                      @size-change="handleMyLikedCommentSizeChange"
                      @current-change="handleMyLikedCommentCurrentChange"
                    />
                  </div>
                </div>
                <el-empty v-else description="暂无点赞的评论" />
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的拼团" name="group">
          <div class="group-tabs">
            <el-tabs v-model="groupTab">
              <el-tab-pane label="拼团中" name="grouping">
                <div v-if="myGroupingOrders.length > 0" class="my-groups">
                  <el-table :data="myGroupingOrders" border style="width: 100%">
                    <el-table-column label="商品信息" min-width="300">
                      <template #default="scope">
                        <div class="product-info">
                          <el-image
                            :src="formatImageUrl(scope.row.productImage)"
                            class="product-image"
                            fit="cover"
                          />
                          <div class="product-detail">
                            <div class="product-name">{{ scope.row.productName }}</div>
                            <div class="product-price">
                              <span class="group-price">¥{{ formatPrice(scope.row.groupPrice) }}</span>
                              <span class="original-price">¥{{ formatPrice(scope.row.productPrice) }}</span>
                            </div>
                          </div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column prop="minGroupSize" label="成团人数" width="100" />
                    <el-table-column prop="currentSize" label="当前人数" width="100" />
                    <el-table-column label="状态" width="120">
                      <template #default="scope">
                        <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
                          {{ scope.row.status === 0 ? '拼团中' : '已成团' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="过期时间" width="180">
                      <template #default="scope">
                        {{ formatDateTime(scope.row.expireTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="150">
                      <template #default="scope">
                        <el-button type="primary" size="small" @click="viewGroupOrder(scope.row.id)">
                          查看详情
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div class="pagination">
                    <el-pagination
                      v-model:current-page="groupingPage"
                      v-model:page-size="groupingSize"
                      :page-sizes="[5, 10, 20]"
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="groupingTotal"
                      @size-change="handleGroupingSizeChange"
                      @current-change="handleGroupingCurrentChange"
                    />
                  </div>
                </div>
                <el-empty v-else description="暂无拼团中的订单" />
              </el-tab-pane>

              <el-tab-pane label="已成团" name="grouped">
                <div v-if="myGroupedOrders.length > 0" class="my-groups">
                  <el-table :data="myGroupedOrders" border style="width: 100%">
                    <el-table-column label="商品信息" min-width="300">
                      <template #default="scope">
                        <div class="product-info">
                          <el-image
                            :src="formatImageUrl(scope.row.productImage)"
                            class="product-image"
                            fit="cover"
                          />
                          <div class="product-detail">
                            <div class="product-name">{{ scope.row.productName }}</div>
                            <div class="product-price">
                              <span class="group-price">¥{{ formatPrice(scope.row.groupPrice) }}</span>
                              <span class="original-price">¥{{ formatPrice(scope.row.productPrice) }}</span>
                            </div>
                          </div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column prop="minGroupSize" label="成团人数" width="100" />
                    <el-table-column prop="currentSize" label="参与人数" width="100" />
                    <el-table-column label="状态" width="120">
                      <template #default="scope">
                        <el-tag type="success">已成团</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="成团时间" width="180">
                      <template #default="scope">
                        {{ formatDateTime(scope.row.updatedAt) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="150">
                      <template #default="scope">
                        <el-button type="primary" size="small" @click="viewGroupOrder(scope.row.id)">
                          查看详情
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div class="pagination">
                    <el-pagination
                      v-model:current-page="groupedPage"
                      v-model:page-size="groupedSize"
                      :page-sizes="[5, 10, 20]"
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="groupedTotal"
                      @size-change="handleGroupedSizeChange"
                      @current-change="handleGroupedCurrentChange"
                    />
                  </div>
                </div>
                <el-empty v-else description="暂无已成团的订单" />
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { updateUserInfo, uploadAvatar, changePassword as apiChangePassword } from '../../api/user'
import { getUserPosts, getUserComments, getUserLikes } from '../../api/community'
import { getUserGroupOrders } from '../../api/group'
import request from '../../utils/request'
import ProductCard from '../../components/ProductCard.vue'
import PostCard from '../../components/PostCard.vue'
import CommentCard from '../../components/CommentCard.vue'
import { formatImageUrl } from '../../utils/image'
import defaultAvatar from '../../assets/images/default-avatar.png'

const userStore = useUserStore()
const router = useRouter()

// 状态
const activeTab = ref('info')
const collectionTab = ref('product')
const likeTab = ref('post')
const groupTab = ref('grouping')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)

// 个人资料表单
const userInfo = computed(() => userStore.userInfo)
const profileForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  bio: ''
})

// 上传头像的请求头
const uploadHeaders = computed(() => {
  return {
    'token': localStorage.getItem('token')
  }
})

// 格式化头像URL
function formatAvatarUrl(url) {
  return formatImageUrl(url)
}

// 格式化价格
function formatPrice(price) {
  if (!price || isNaN(price)) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 格式化日期时间
function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 收藏的商品
const collectedProducts = ref([])
const productPage = ref(1)
const productSize = ref(8)
const productTotal = ref(0)

// 收藏的帖子
const collectedPosts = ref([])
const postPage = ref(1)
const postSize = ref(6)
const postTotal = ref(0)

// 我的帖子
const myPosts = ref([])
const myPostPage = ref(1)
const myPostSize = ref(6)
const myPostTotal = ref(0)

// 我的评论
const myComments = ref([])
const myCommentPage = ref(1)
const myCommentSize = ref(6)
const myCommentTotal = ref(0)

// 我的点赞 - 帖子
const myLikedPosts = ref([])
const myLikedPostPage = ref(1)
const myLikedPostSize = ref(6)
const myLikedPostTotal = ref(0)

// 我的点赞 - 评论
const myLikedComments = ref([])
const myLikedCommentPage = ref(1)
const myLikedCommentSize = ref(6)
const myLikedCommentTotal = ref(0)

// 我的拼团 - 拼团中
const myGroupingOrders = ref([])
const groupingPage = ref(1)
const groupingSize = ref(5)
const groupingTotal = ref(0)

// 我的拼团 - 已成团
const myGroupedOrders = ref([])
const groupedPage = ref(1)
const groupedSize = ref(5)
const groupedTotal = ref(0)

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 方法
function initProfileForm() {
  profileForm.nickname = userInfo.value.nickname || ''
  profileForm.phone = userInfo.value.phone || ''
  profileForm.email = userInfo.value.email || ''
  profileForm.gender = userInfo.value.gender || 0
  profileForm.bio = userInfo.value.bio || ''
}

async function updateProfile() {
  profileFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await updateUserInfo(profileForm)
      await userStore.fetchUserInfo()
      ElMessage.success('个人资料更新成功')
    } catch (error) {
      ElMessage.error(error.message || '更新失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

function resetForm() {
  profileFormRef.value.resetFields()
  initProfileForm()
}

async function changePassword() {
  passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      await apiChangePassword(passwordForm.oldPassword, passwordForm.newPassword)
      ElMessage.success('密码修改成功，请重新登录')
      resetPasswordForm()

      // 退出登录
      setTimeout(() => {
        userStore.logoutAction()
      }, 1500)
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败，请重试')
    } finally {
      passwordLoading.value = false
    }
  })
}

function resetPasswordForm() {
  passwordFormRef.value.resetFields()
}

function handleAvatarSuccess(res) {
  if (res.code === 200) {
    const avatarUrl = res.data
    console.log('上传头像成功，返回URL:', avatarUrl)

    // 更新用户信息
    updateUserInfo({ avatar: avatarUrl }).then(() => {
      userStore.fetchUserInfo()
      ElMessage.success('头像更新成功')
    }).catch(error => {
      console.error('更新头像失败:', error)
      ElMessage.error('更新头像失败，请重试')
    })
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function beforeAvatarUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return (isJPG || isPNG) && isLt2M
}

// 获取收藏的商品
async function fetchCollectedProducts() {
  try {
    const res = await request({
      url: '/collection/product',
      method: 'get',
      params: {
        page: productPage.value,
        size: productSize.value
      }
    })
    collectedProducts.value = res.data.records
    productTotal.value = res.data.total
  } catch (error) {
    console.error('获取收藏的商品失败:', error)
    collectedProducts.value = []
    productTotal.value = 0
  }
}

// 获取收藏的帖子
async function fetchCollectedPosts() {
  try {
    const res = await request({
      url: '/collection/post',
      method: 'get',
      params: {
        page: postPage.value,
        size: postSize.value
      }
    })
    collectedPosts.value = res.data.records
    postTotal.value = res.data.total
  } catch (error) {
    console.error('获取收藏的帖子失败:', error)
    collectedPosts.value = []
    postTotal.value = 0
  }
}

// 获取我的帖子
async function fetchMyPosts() {
  try {
    const res = await getUserPosts({
      page: myPostPage.value,
      size: myPostSize.value
    })
    myPosts.value = res.data.records
    myPostTotal.value = res.data.total
  } catch (error) {
    console.error('获取我的帖子失败:', error)
  }
}

// 获取我的评论
async function fetchMyComments() {
  try {
    const res = await getUserComments({
      page: myCommentPage.value,
      size: myCommentSize.value
    })
    myComments.value = res.data.records
    myCommentTotal.value = res.data.total
  } catch (error) {
    console.error('获取我的评论失败:', error)
  }
}

// 获取我的点赞 - 帖子
async function fetchMyLikedPosts() {
  try {
    const res = await getUserLikes({
      page: myLikedPostPage.value,
      size: myLikedPostSize.value,
      type: 1 // 帖子
    })
    myLikedPosts.value = res.data.records
    myLikedPostTotal.value = res.data.total
  } catch (error) {
    console.error('获取我点赞的帖子失败:', error)
  }
}

// 获取我的点赞 - 评论
async function fetchMyLikedComments() {
  try {
    const res = await getUserLikes({
      page: myLikedCommentPage.value,
      size: myLikedCommentSize.value,
      type: 2 // 评论
    })
    myLikedComments.value = res.data.records
    myLikedCommentTotal.value = res.data.total
  } catch (error) {
    console.error('获取我点赞的评论失败:', error)
  }
}

// 分页处理
function handleProductSizeChange() {
  fetchCollectedProducts()
}

function handleProductCurrentChange() {
  fetchCollectedProducts()
}

function handlePostSizeChange() {
  fetchCollectedPosts()
}

function handlePostCurrentChange() {
  fetchCollectedPosts()
}

function handleMyPostSizeChange() {
  fetchMyPosts()
}

function handleMyPostCurrentChange() {
  fetchMyPosts()
}

function handleMyCommentSizeChange() {
  fetchMyComments()
}

function handleMyCommentCurrentChange() {
  fetchMyComments()
}

function handleMyLikedPostSizeChange() {
  fetchMyLikedPosts()
}

function handleMyLikedPostCurrentChange() {
  fetchMyLikedPosts()
}

function handleMyLikedCommentSizeChange() {
  fetchMyLikedComments()
}

function handleMyLikedCommentCurrentChange() {
  fetchMyLikedComments()
}

// 获取我的拼团 - 拼团中
async function fetchMyGroupingOrders() {
  try {
    const res = await getUserGroupOrders({
      page: groupingPage.value,
      size: groupingSize.value,
      status: 0 // 拼团中
    })

    console.log('拼团中订单原始数据:', res.data)

    if (res.data && res.data.records) {
      // 处理数据，确保所有必要的字段都存在
      const records = res.data.records.map(item => {
        // 创建一个新对象，添加默认值
        const newItem = {
          ...item,
          productName: item.productName || '未知商品',
          productImage: item.productImage || '',
          productPrice: item.productPrice || 0,
          groupPrice: item.groupPrice || 0,
          minGroupSize: item.minGroupSize || 2
        }

        // 确保图片URL正确
        if (newItem.productImage) {
          newItem.productImage = formatImageUrl(newItem.productImage)
        }

        // 确保价格字段为数字
        if (newItem.groupPrice && !isNaN(newItem.groupPrice)) {
          newItem.groupPrice = parseFloat(newItem.groupPrice)
        }

        if (newItem.productPrice && !isNaN(newItem.productPrice)) {
          newItem.productPrice = parseFloat(newItem.productPrice)
        }

        return newItem
      })

      myGroupingOrders.value = records
      groupingTotal.value = res.data.total || 0

      // 调试输出
      console.log('处理后的拼团中订单数据:', records)
    } else {
      myGroupingOrders.value = []
      groupingTotal.value = 0
    }
  } catch (error) {
    console.error('获取拼团中的订单失败:', error)
    myGroupingOrders.value = []
    groupingTotal.value = 0
  }
}

// 获取我的拼团 - 已成团
async function fetchMyGroupedOrders() {
  try {
    const res = await getUserGroupOrders({
      page: groupedPage.value,
      size: groupedSize.value,
      status: 1 // 已成团
    })

    console.log('已成团订单原始数据:', res.data)

    if (res.data && res.data.records) {
      // 处理数据，确保所有必要的字段都存在
      const records = res.data.records.map(item => {
        // 创建一个新对象，添加默认值
        const newItem = {
          ...item,
          productName: item.productName || '未知商品',
          productImage: item.productImage || '',
          productPrice: item.productPrice || 0,
          groupPrice: item.groupPrice || 0,
          minGroupSize: item.minGroupSize || 2
        }

        // 确保图片URL正确
        if (newItem.productImage) {
          newItem.productImage = formatImageUrl(newItem.productImage)
        }

        // 确保价格字段为数字
        if (newItem.groupPrice && !isNaN(newItem.groupPrice)) {
          newItem.groupPrice = parseFloat(newItem.groupPrice)
        }

        if (newItem.productPrice && !isNaN(newItem.productPrice)) {
          newItem.productPrice = parseFloat(newItem.productPrice)
        }

        return newItem
      })

      myGroupedOrders.value = records
      groupedTotal.value = res.data.total || 0

      // 调试输出
      console.log('处理后的已成团订单数据:', records)
    } else {
      myGroupedOrders.value = []
      groupedTotal.value = 0
    }
  } catch (error) {
    console.error('获取已成团的订单失败:', error)
    myGroupedOrders.value = []
    groupedTotal.value = 0
  }
}

// 查看拼团订单详情
function viewGroupOrder(id) {
  router.push(`/group-order/${id}`)
}

// 拼团分页处理
function handleGroupingSizeChange() {
  fetchMyGroupingOrders()
}

function handleGroupingCurrentChange() {
  fetchMyGroupingOrders()
}

function handleGroupedSizeChange() {
  fetchMyGroupedOrders()
}

function handleGroupedCurrentChange() {
  fetchMyGroupedOrders()
}

// 生命周期钩子
onMounted(() => {
  initProfileForm()
  fetchCollectedProducts()
  fetchCollectedPosts()
  fetchMyPosts()
  fetchMyComments()
  fetchMyLikedPosts()
  fetchMyLikedComments()
  fetchMyGroupingOrders()
  fetchMyGroupedOrders()
})
</script>

<style lang="scss" scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;

  .profile-card {
    margin-bottom: 20px;

    .profile-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .profile-info {
      display: flex;
      flex-direction: column;
      align-items: center;

      .avatar-container {
        margin-bottom: 20px;
        text-align: center;

        .el-avatar {
          margin-bottom: 10px;
        }
      }

      .profile-form {
        width: 100%;
        max-width: 500px;
      }
    }

    .password-form {
      width: 100%;
      max-width: 500px;
      margin: 0 auto;
    }

    .collection-container,
    .my-posts,
    .my-comments,
    .my-likes,
    .my-groups {
      .collection-list {
        margin-top: 20px;
      }

      .pagination {
        margin-top: 20px;
        text-align: center;
      }
    }

    .like-tabs,
    .group-tabs {
      .el-tabs {
        margin-top: 10px;
      }
    }

    .product-info {
      display: flex;
      align-items: center;

      .product-image {
        width: 60px;
        height: 60px;
        margin-right: 10px;
        border-radius: 4px;
      }

      .product-detail {
        flex: 1;

        .product-name {
          font-size: 14px;
          margin-bottom: 5px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .product-price {
          .group-price {
            color: #f56c6c;
            font-weight: bold;
            margin-right: 5px;
          }

          .original-price {
            color: #999;
            text-decoration: line-through;
            font-size: 12px;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .profile-container {
    .profile-card {
      .profile-info {
        .profile-form {
          max-width: 100%;
        }
      }

      .password-form {
        max-width: 100%;
      }
    }
  }
}
</style>
