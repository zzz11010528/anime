import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'layout',
      component: () => import('../views/layout/index.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'home',
          component: () => import('../views/home/index.vue'),
          meta: { title: '首页', requiresAuth: false }
        },
        {
          path: 'product',
          name: 'product-list',
          component: () => import('../views/product/list.vue'),
          meta: { title: '商品列表', requiresAuth: false }
        },
        {
          path: 'product/:id',
          name: 'product-detail',
          component: () => import('../views/product/detail.vue'),
          meta: { title: '商品详情', requiresAuth: false }
        },
        {
          path: 'community',
          name: 'community',
          component: () => import('../views/community/index.vue'),
          meta: { title: '社区', requiresAuth: false }
        },
        {
          path: 'post/:id',
          name: 'post-detail',
          component: () => import('../views/community/detail.vue'),
          meta: { title: '帖子详情', requiresAuth: false }
        },
        {
          path: 'post/publish',
          name: 'post-publish',
          component: () => import('../views/community/publish.vue'),
          meta: { title: '发布帖子', requiresAuth: true }
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('../views/cart/index.vue'),
          meta: { title: '购物车', requiresAuth: true }
        },
        {
          path: 'order',
          name: 'order-list',
          component: () => import('../views/order/list.vue'),
          meta: { title: '我的订单', requiresAuth: true }
        },
        {
          path: 'order/confirm',
          name: 'order-confirm',
          component: () => import('../views/order/confirm.vue'),
          meta: { title: '确认订单', requiresAuth: true }
        },
        {
          path: 'order/:id',
          name: 'order-detail',
          component: () => import('../views/order/detail.vue'),
          meta: { title: '订单详情', requiresAuth: true }
        },
        {
          path: 'message',
          name: 'message',
          component: () => import('../views/message/index.vue'),
          meta: { title: '消息中心', requiresAuth: true }
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/profile/index.vue'),
          meta: { title: '个人中心', requiresAuth: true }
        },
        {
          path: 'gesture',
          name: 'gesture',
          component: () => import('../views/gesture/index.vue'),
          meta: { title: '手势交互', requiresAuth: false }
        },
        {
          path: 'gesture-gallery',
          name: 'gesture-gallery',
          component: () => import('../views/gesture-gallery/index.vue'),
          meta: { title: '手势图片浏览', requiresAuth: false }
        },
        {
          path: 'group-buying',
          name: 'group-buying-list',
          component: () => import('../views/group-buying/index.vue'),
          meta: { title: '拼团活动', requiresAuth: false }
        },
        {
          path: 'group-buying/:id',
          name: 'group-buying-detail',
          component: () => import('../views/group-buying/detail.vue'),
          meta: { title: '拼团详情', requiresAuth: false }
        },
        {
          path: 'group-order/:id',
          name: 'group-order-detail',
          component: () => import('../views/group-buying/order.vue'),
          meta: { title: '拼团订单', requiresAuth: true }
        },
        {
          path: 'payment/result',
          name: 'payment-result',
          component: () => import('../views/payment/result.vue'),
          meta: { title: '支付结果', requiresAuth: false }
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/index.vue'),
      meta: { title: '登录', requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/register/index.vue'),
      meta: { title: '注册', requiresAuth: false }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/error/404.vue'),
      meta: { title: '页面不存在', requiresAuth: false }
    }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 动漫社区` : '动漫社区'

  // 判断是否需要登录
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
