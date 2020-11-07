import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: '首页', icon: 'dashboard' }
    }]
  },

  {
    path: '/service',
    component: Layout,
    name: 'Service',
    meta: { title: '服务管理', icon: 'example' },
    children: [
      {
        path: 'list',
        name: 'ServiceList',
        component: () => import('@/views/service/serviceList'),
        meta: { title: '服务列表' }
      },
      {
        path: 'route',
        name: 'Route',
        component: () => import('@/views/service/route'),
        meta: { title: '路由管理' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/service/monitorNew'),
        meta: { title: '路由监控' }
      },
      {
        path: 'limit',
        name: 'Limit',
        component: () => import('@/views/service/limit'),
        meta: { title: '限流管理' }
      },
      {
        path: 'blacklist',
        name: 'Blacklist',
        component: () => import('@/views/service/ipBlacklist'),
        meta: { title: 'IP黑名单' }
      }
    ]
  },

  {
    path: '/isv',
    component: Layout,
    name: 'Isv',
    meta: { title: 'ISV管理', icon: 'user' },
    children: [
      {
        path: 'list',
        name: 'IsvList',
        component: () => import('@/views/isv/index'),
        meta: { title: 'ISV列表' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/isv/role'),
        meta: { title: '角色管理' }
      },
      {
        path: 'keys',
        name: 'Keys',
        component: () => import('@/views/isv/keys'),
        hidden: true,
        meta: { title: '秘钥管理' }
      }
    ]
  },
  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
];

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
});

const router = createRouter();

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter();
  router.matcher = newRouter.matcher // reset router
}

export default router
