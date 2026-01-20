import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', redirect: '/manager/home'},
    {path: '/login', name: 'Login', meta: { title:'登录'}, component: () => import('../views/Login.vue')},
    {path: '/manager', name: 'Manager', meta: { title:'管理'}, component:() => import('../views/Manager.vue'), children:[
      {path: 'home', name: 'home', meta: { title:'主页'}, component: () => import('../views/Home.vue'),},
      {path: 'data', name: 'data', meta: { title:'数据展示'}, component: () => import('../views/Data.vue')},
      {path: 'user', name:'user', meta: { title:'用户管理'}, component: () => import('../views/User.vue')},
      {path: 'admin', name:'admin', meta: { title:'管理员信息'}, component: () => import('../views/Admin.vue')}
      ]},
    {path: '/Login', name: 'Login', meta: { title:'登录'}, component: () => import('../views/Login.vue')},
    {path: '/Register', name: 'Register', meta: { title:'欢迎注册'}, component: () => import('../views/Register.vue')},
    {path: '/404', name: 'notFound',meta: { title:'404'}, component: () => import('../views/404.vue')},
    //匹配所有路由
    {path: '/:pathMatch(.*)*', redirect: '/404'}
  ]
})
//beforeEach表示路由跳转前执行的操作
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
  next() //调用next()方法，表示可以跳转
})

export default router
