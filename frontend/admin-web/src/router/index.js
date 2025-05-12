import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/index.vue'
import { useAdminStore } from '../stores/admin'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '控制台', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/merchant',
    component: Layout,
    redirect: '/merchant/list',
    meta: { title: '商家管理', icon: 'Shop' },
    children: [
      {
        path: 'list',
        name: 'MerchantList',
        component: () => import('../views/merchant/list.vue'),
        meta: { title: '商家列表' }
      },
      {
        path: 'audit',
        name: 'MerchantAudit',
        component: () => import('../views/merchant/audit.vue'),
        meta: { title: '商家审核' }
      },
      {
        path: 'detail/:id',
        name: 'MerchantDetail',
        component: () => import('../views/merchant/detail.vue'),
        meta: { title: '商家详情', hidden: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    meta: { title: '用户管理', icon: 'User' },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: () => import('../views/user/list.vue'),
        meta: { title: '用户列表' }
      },
      {
        path: 'detail/:id',
        name: 'UserDetail',
        component: () => import('../views/user/detail.vue'),
        meta: { title: '用户详情', hidden: true }
      }
    ]
  },
  {
    path: '/product',
    component: Layout,
    redirect: '/product/list',
    meta: { title: '商品管理', icon: 'Goods' },
    children: [
      {
        path: 'list',
        name: 'ProductList',
        component: () => import('../views/product/list.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'category',
        name: 'ProductCategory',
        component: () => import('../views/product/category.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'detail/:id',
        name: 'ProductDetail',
        component: () => import('../views/product/detail.vue'),
        meta: { title: '商品详情', hidden: true }
      }
    ]
  },
  {
    path: '/order',
    component: Layout,
    redirect: '/order/list',
    meta: { title: '订单管理', icon: 'List' },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('../views/order/list.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'detail/:id',
        name: 'OrderDetail',
        component: () => import('../views/order/detail.vue'),
        meta: { title: '订单详情', hidden: true }
      }
    ]
  },
  {
    path: '/community',
    component: Layout,
    redirect: '/community/category',
    meta: { title: '社区管理', icon: 'Document' },
    children: [
      {
        path: 'category',
        name: 'CommunityCategory',
        component: () => import('../views/community/category.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'topic',
        name: 'TopicList',
        component: () => import('../views/community/topic.vue'),
        meta: { title: '话题管理' }
      },
      {
        path: 'post',
        name: 'PostList',
        component: () => import('../views/community/post.vue'),
        meta: { title: '帖子管理' }
      },
      {
        path: 'post/detail/:id',
        name: 'PostDetail',
        component: () => import('../views/community/post-detail.vue'),
        meta: { title: '帖子详情', hidden: true }
      }
    ]
  },
  {
    path: '/ip',
    component: Layout,
    redirect: '/ip/list',
    meta: { title: 'IP管理', icon: 'Promotion' },
    children: [
      {
        path: 'list',
        name: 'IpList',
        component: () => import('../views/ip/list.vue'),
        meta: { title: 'IP列表' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/profile',
    meta: { title: '系统管理', hidden: true },
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/system/profile.vue'),
        meta: { title: '个人信息', hidden: true }
      },
      {
        path: 'password',
        name: 'Password',
        component: () => import('../views/system/password.vue'),
        meta: { title: '修改密码', hidden: true }
      }
    ]
  },

  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 管理员后台` : '管理员后台'

  // 登录验证
  const adminStore = useAdminStore()
  const isLoggedIn = adminStore.isLoggedIn

  // 白名单路径，不需要登录就可以访问
  const whiteList = ['/login']

  if (isLoggedIn) {
    // 已登录
    if (to.path === '/login') {
      // 如果已登录且要访问登录页，则重定向到首页
      next({ path: '/' })
    } else {
      // 如果管理员信息不存在，先获取管理员信息
      if (Object.keys(adminStore.adminInfo).length === 0) {
        adminStore.getAdminInfo().then(() => {
          next()
        }).catch(() => {
          // 获取管理员信息失败，可能是token过期，登出并重定向到登录页
          adminStore.logoutAction()
          next(`/login?redirect=${to.path}`)
        })
      } else {
        next()
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 在白名单中，直接访问
      next()
    } else {
      // 不在白名单中，重定向到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
