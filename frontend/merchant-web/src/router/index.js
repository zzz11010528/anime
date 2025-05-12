import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '商家登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/register/index.vue'),
    meta: { title: '商家注册' }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/product/list',
    children: [
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('../views/product/list.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'product/add',
        name: 'ProductAdd',
        component: () => import('../views/product/SimpleProductForm.vue'),
        meta: { title: '添加商品' }
      },
      {
        path: 'product/edit/:id',
        name: 'ProductEdit',
        component: () => import('../views/product/SimpleProductForm.vue'),
        meta: { title: '编辑商品' }
      },
      {
        path: 'product/detail/:id',
        name: 'ProductDetail',
        component: () => import('../views/product/detail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'product/group',
        name: 'ProductGroup',
        component: () => import('../views/product/group.vue'),
        meta: { title: '拼团管理' }
      },
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('../views/order/list.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('../views/order/detail.vue'),
        meta: { title: '订单详情' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('../views/message/index.vue'),
        meta: { title: '私信管理' }
      },

      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '店铺设置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 商家中心` : '多元社区交互系统 - 商家中心'

  // 获取token
  const token = localStorage.getItem('token')

  // 如果访问登录或注册页面，且已登录，则跳转到首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next({ path: '/' })
    return
  }

  // 如果访问需要登录的页面，且未登录，则跳转到登录页
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next({ path: '/login' })
    return
  }

  // 获取认证状态
  const merchantInfoStr = localStorage.getItem('merchantInfo')
  if (merchantInfoStr && to.path !== '/login' && to.path !== '/register') {
    try {
      const merchantInfo = JSON.parse(merchantInfoStr)

      // 如果用户未认证或认证被拒绝，且访问的不是店铺设置页面，则跳转到店铺设置页面
      // 注意：如果认证状态为0（审核中）或2（被拒绝），也需要跳转到认证页面
      if ((merchantInfo.role !== 1 ||
          (merchantInfo.certificationStatus !== 1 && merchantInfo.certificationStatus !== undefined)) &&
          to.path !== '/profile') {

        // 显示提示信息
        if (from.path !== '/profile') {
          // 使用setTimeout确保在路由跳转后显示消息
          setTimeout(() => {
            const { ElMessage } = require('element-plus')

            // 根据认证状态显示不同的提示信息
            if (merchantInfo.certificationStatus === 0) {
              ElMessage.warning('您的商家认证正在审核中，暂时无法使用其他功能')
            } else if (merchantInfo.certificationStatus === 2) {
              ElMessage.error('您的商家认证被拒绝，请重新提交认证申请')
            } else {
              ElMessage.warning('您需要先完成商家认证才能使用其他功能')
            }
          }, 100)
        }

        next({ path: '/profile', query: { tab: 'certification' } })
        return
      }
    } catch (error) {
      console.error('解析商家信息失败:', error)
    }
  }

  next()
})

export default router
