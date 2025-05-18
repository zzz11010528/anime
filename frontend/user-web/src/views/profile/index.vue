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

        <el-tab-pane label="我的商品" name="product">
          <div class="my-products">
            <div class="action-bar">
              <el-button type="primary" @click="goToAddProduct">发布新商品</el-button>
            </div>
            <div v-if="myProducts.length > 0">
              <el-table :data="myProducts" border style="width: 100%">
                <el-table-column label="商品信息" min-width="300">
                  <template #default="scope">
                    <div class="product-info">
                      <el-image
                        :src="formatImageUrl(scope.row.mainImage)"
                        class="product-image"
                        fit="cover"
                      />
                      <div class="product-detail">
                        <div class="product-name">{{ scope.row.name }}</div>
                        <div class="product-price">
                          <span class="price">¥{{ formatPrice(scope.row.price) }}</span>
                        </div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="stock" label="库存" width="80" />
                <el-table-column prop="sales" label="销量" width="80" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getProductStatusType(scope.row.status)">
                      {{ getProductStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="180">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200">
                  <template #default="scope">
                    <el-button type="primary" link @click="viewProduct(scope.row.id)">查看</el-button>
                    <el-button type="success" link @click="editProduct(scope.row.id)">编辑</el-button>
                    <el-button
                      :type="scope.row.status === 1 ? 'warning' : 'success'"
                      link
                      @click="toggleProductStatus(scope.row)"
                    >
                      {{ scope.row.status === 1 ? '下架' : '上架' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination">
                <el-pagination
                  v-model:current-page="myProductPage"
                  v-model:page-size="myProductSize"
                  :page-sizes="[5, 10, 20]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="myProductTotal"
                  @size-change="handleMyProductSizeChange"
                  @current-change="handleMyProductCurrentChange"
                />
              </div>
            </div>
            <el-empty v-else description="暂无发布的商品" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="卖家订单" name="seller-order">
          <div class="seller-orders">
            <div class="order-filter">
              <el-form :inline="true" :model="sellerOrderSearchForm" class="search-form">
                <el-form-item label="订单编号">
                  <el-input v-model="sellerOrderSearchForm.orderNo" placeholder="请输入订单编号" clearable></el-input>
                </el-form-item>
                <el-form-item label="订单状态">
                  <el-select v-model="sellerOrderSearchForm.status" placeholder="请选择状态" clearable>
                    <el-option label="全部" value=""></el-option>
                    <el-option label="待付款" :value="0"></el-option>
                    <el-option label="待发货" :value="1"></el-option>
                    <el-option label="待收货" :value="2"></el-option>
                    <el-option label="已完成" :value="3"></el-option>
                    <el-option label="已取消" :value="4"></el-option>
                    <el-option label="退款中" :value="5"></el-option>
                    <el-option label="已退款" :value="6"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchSellerOrders">搜索</el-button>
                  <el-button @click="resetSellerOrderSearch">重置</el-button>
                </el-form-item>
              </el-form>
            </div>

            <div v-if="sellerOrders.length > 0" class="order-list">
              <div v-for="order in sellerOrders" :key="order.id" class="order-card">
                <div class="order-header">
                  <div class="order-info">
                    <span class="order-time">{{ formatDateTime(order.createdAt) }}</span>
                    <span class="order-no">订单号：{{ order.orderNo }}</span>
                  </div>
                  <div class="order-status">
                    <el-tag :type="getOrderStatusType(order.status)">
                      {{ getOrderStatusText(order.status) }}
                    </el-tag>
                  </div>
                </div>

                <div class="order-content">
                  <div v-if="order.orderItems && order.orderItems.length > 0" class="order-items">
                    <div v-for="item in order.orderItems" :key="item.id" class="order-item">
                      <div class="item-image">
                        <el-image
                          :src="formatImageUrl(item.productImage)"
                          fit="cover"
                          @click="viewProduct(item.productId)"
                        />
                      </div>
                      <div class="item-info">
                        <div class="item-name" @click="viewProduct(item.productId)">{{ item.productName }}</div>
                        <div class="item-price">¥{{ formatPrice(item.price) }}</div>
                      </div>
                      <div class="item-quantity">x{{ item.quantity }}</div>
                    </div>
                  </div>
                </div>

                <div class="order-footer">
                  <div class="order-amount">
                    <span>共{{ getTotalQuantity(order.orderItems) }}件商品，实付：</span>
                    <span class="amount">¥{{ formatPrice(order.totalAmount) }}</span>
                  </div>
                  <div class="order-actions">
                    <el-button type="primary" size="small" @click="viewSellerOrder(order.id)">查看详情</el-button>
                    <el-button
                      v-if="order.status === 1"
                      type="success"
                      size="small"
                      @click="handleShipOrder(order.id)"
                    >
                      发货
                    </el-button>
                    <el-button
                      v-if="order.status === 5"
                      type="danger"
                      size="small"
                      @click="handleRefundOrder(order.id)"
                    >
                      处理退款
                    </el-button>
                  </div>
                </div>
              </div>

              <div class="pagination">
                <el-pagination
                  v-model:current-page="sellerOrderPage"
                  v-model:page-size="sellerOrderSize"
                  :page-sizes="[5, 10, 20]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="sellerOrderTotal"
                  @size-change="handleSellerOrderSizeChange"
                  @current-change="handleSellerOrderCurrentChange"
                />
              </div>
            </div>
            <el-empty v-else description="暂无卖家订单" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { updateUserInfo, uploadAvatar, changePassword as apiChangePassword } from '../../api/user'
import { getUserPosts, getUserComments, getUserLikes } from '../../api/community'
import { getUserGroupOrders } from '../../api/group'
import { getMyProducts, updateProductStatus, getSellerOrders, shipOrder, handleRefund } from '../../api/seller'
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

// 我的商品
const myProducts = ref([])
const myProductPage = ref(1)
const myProductSize = ref(10)
const myProductTotal = ref(0)

// 卖家订单
const sellerOrders = ref([])
const sellerOrderPage = ref(1)
const sellerOrderSize = ref(10)
const sellerOrderTotal = ref(0)
const sellerOrderSearchForm = reactive({
  orderNo: '',
  status: ''
})

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

// 商品状态文本和类型
function getProductStatusText(status) {
  switch (status) {
    case 0: return '下架'
    case 1: return '上架'
    case 2: return '审核中'
    case 3: return '审核失败'
    case -1: return '已删除'
    default: return '未知'
  }
}

function getProductStatusType(status) {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    case -1: return 'danger'
    default: return 'info'
  }
}

// 订单状态文本和类型
function getOrderStatusText(status) {
  switch (status) {
    case 0: return '待付款'
    case 1: return '待发货'
    case 2: return '待收货'
    case 3: return '已完成'
    case 4: return '已取消'
    case 5: return '退款中'
    case 6: return '已退款'
    case 7: return '退款拒绝'
    default: return '未知'
  }
}

function getOrderStatusType(status) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'success'
    case 4: return 'info'
    case 5: return 'danger'
    case 6: return 'info'
    case 7: return 'danger'
    default: return 'info'
  }
}

// 获取我的商品
async function fetchMyProducts() {
  try {
    const res = await getMyProducts({
      page: myProductPage.value,
      size: myProductSize.value
    })

    if (res.data) {
      myProducts.value = res.data.records || []
      myProductTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取我的商品失败:', error)
    myProducts.value = []
    myProductTotal.value = 0
  }
}

// 商品分页处理
function handleMyProductSizeChange() {
  fetchMyProducts()
}

function handleMyProductCurrentChange() {
  fetchMyProducts()
}

// 商品操作
function goToAddProduct() {
  router.push('/my-products/add')
}

function viewProduct(id) {
  router.push(`/product/${id}`)
}

function editProduct(id) {
  router.push(`/my-products/edit/${id}`)
}

async function toggleProductStatus(product) {
  try {
    const newStatus = product.status === 1 ? 0 : 1
    await updateProductStatus(product.id, newStatus)
    ElMessage.success(newStatus === 1 ? '商品已上架' : '商品已下架')
    fetchMyProducts()
  } catch (error) {
    console.error('更新商品状态失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 获取卖家订单
async function fetchSellerOrders() {
  try {
    const res = await getSellerOrders({
      page: sellerOrderPage.value,
      size: sellerOrderSize.value,
      orderNo: sellerOrderSearchForm.orderNo,
      status: sellerOrderSearchForm.status
    })

    if (res.data) {
      sellerOrders.value = res.data.records || []
      sellerOrderTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取卖家订单失败:', error)
    sellerOrders.value = []
    sellerOrderTotal.value = 0
  }
}

// 搜索卖家订单
function searchSellerOrders() {
  sellerOrderPage.value = 1
  fetchSellerOrders()
}

// 重置卖家订单搜索
function resetSellerOrderSearch() {
  sellerOrderSearchForm.orderNo = ''
  sellerOrderSearchForm.status = ''
  sellerOrderPage.value = 1
  fetchSellerOrders()
}

// 计算订单商品总数量
function getTotalQuantity(items) {
  if (!items || !Array.isArray(items)) return 0
  return items.reduce((sum, item) => sum + (item.quantity || 0), 0)
}

// 卖家订单分页处理
function handleSellerOrderSizeChange() {
  fetchSellerOrders()
}

function handleSellerOrderCurrentChange() {
  fetchSellerOrders()
}

// 卖家订单操作
function viewSellerOrder(id) {
  router.push(`/seller-orders/${id}`)
}

async function handleShipOrder(id) {
  try {
    await ElMessageBox.confirm('确认发货该订单?', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await shipOrder(id)
    ElMessage.success('发货成功')
    fetchSellerOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败，请重试')
    }
  }
}

async function handleRefundOrder(id) {
  try {
    const { value: action } = await ElMessageBox.prompt(
      '请输入拒绝原因（同意退款可不填）',
      '处理退款申请',
      {
        confirmButtonText: '同意退款',
        cancelButtonText: '拒绝退款',
        inputPlaceholder: '拒绝原因...',
        showCancelButton: true,
        distinguishCancelAndClose: true
      }
    )

    // 同意退款
    await handleRefund(id, 1)
    ElMessage.success('已同意退款')
    fetchSellerOrders()
  } catch (error) {
    if (error === 'cancel') {
      // 用户点击了"拒绝退款"
      const { value: reason } = await ElMessageBox.prompt(
        '请输入拒绝原因',
        '拒绝退款',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          inputValidator: (value) => {
            if (!value) {
              return '拒绝原因不能为空'
            }
            return true
          }
        }
      )

      if (reason) {
        await handleRefund(id, 2, reason)
        ElMessage.success('已拒绝退款')
        fetchSellerOrders()
      }
    } else if (error !== 'close') {
      console.error('处理退款失败:', error)
      ElMessage.error('处理退款失败，请重试')
    }
  }
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
  fetchMyProducts()
  fetchSellerOrders()
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
    .my-groups,
    .my-products {
      .collection-list {
        margin-top: 20px;
      }

      .pagination {
        margin-top: 20px;
        text-align: center;
      }

      .action-bar {
        margin-bottom: 20px;
      }
    }

    .seller-orders {
      .order-filter {
        margin-bottom: 20px;
      }

      .order-list {
        .order-card {
          background-color: #fff;
          border-radius: 4px;
          border: 1px solid #ebeef5;
          margin-bottom: 15px;
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

          .order-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 15px;
            border-bottom: 1px solid #ebeef5;
            background-color: #f5f7fa;

            .order-info {
              .order-time {
                color: #606266;
                margin-right: 15px;
              }

              .order-no {
                color: #303133;
                font-weight: bold;
              }
            }
          }

          .order-content {
            padding: 15px;

            .order-items {
              .order-item {
                display: flex;
                align-items: center;
                margin-bottom: 10px;
                padding-bottom: 10px;
                border-bottom: 1px dashed #ebeef5;

                &:last-child {
                  margin-bottom: 0;
                  padding-bottom: 0;
                  border-bottom: none;
                }

                .item-image {
                  width: 80px;
                  height: 80px;
                  margin-right: 15px;

                  .el-image {
                    width: 100%;
                    height: 100%;
                    border-radius: 4px;
                    cursor: pointer;
                  }
                }

                .item-info {
                  flex: 1;

                  .item-name {
                    font-size: 14px;
                    color: #303133;
                    margin-bottom: 5px;
                    cursor: pointer;

                    &:hover {
                      color: #409eff;
                    }
                  }

                  .item-price {
                    color: #f56c6c;
                    font-weight: bold;
                  }
                }

                .item-quantity {
                  color: #606266;
                  margin-left: 15px;
                }
              }
            }
          }

          .order-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 15px;
            border-top: 1px solid #ebeef5;
            background-color: #f5f7fa;

            .order-amount {
              color: #606266;

              .amount {
                color: #f56c6c;
                font-size: 16px;
                font-weight: bold;
              }
            }

            .order-actions {
              display: flex;
              gap: 10px;
            }
          }
        }
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
          line-clamp: 2;
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
