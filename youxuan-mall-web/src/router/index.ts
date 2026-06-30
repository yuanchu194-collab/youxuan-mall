import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/auth/RegisterView.vue') },
    {
      path: '/',
      component: () => import('@/layouts/ShopLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/shop/HomeView.vue') },
        { path: 'products', name: 'products', component: () => import('@/views/shop/ProductListView.vue') },
        { path: 'products/:id', name: 'product-detail', component: () => import('@/views/shop/ProductDetailView.vue') },
        { path: 'coupons', name: 'coupons', component: () => import('@/views/shop/CouponsView.vue') },
        { path: 'my-coupons', name: 'my-coupons', component: () => import('@/views/shop/MyCouponsModernView.vue'), meta: { auth: true } },
        { path: 'cart', name: 'cart', component: () => import('@/views/shop/CartView.vue'), meta: { auth: true } },
        { path: 'addresses', name: 'addresses', component: () => import('@/views/shop/AddressView.vue'), meta: { auth: true } },
        { path: 'checkout', name: 'checkout', component: () => import('@/views/shop/OrderConfirmView.vue'), meta: { auth: true } },
        { path: 'orders', name: 'orders', component: () => import('@/views/shop/MyOrdersView.vue'), meta: { auth: true } }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { auth: true, admin: true },
      children: [
        { path: '', name: 'admin-home', component: () => import('@/views/admin/AdminHomeView.vue') },
        { path: 'products', name: 'admin-products', component: () => import('@/views/admin/AdminProductView.vue') },
        { path: 'coupons', name: 'admin-coupons', component: () => import('@/views/admin/AdminCouponView.vue') },
        { path: 'orders', name: 'admin-orders', component: () => import('@/views/admin/AdminOrderView.vue') }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.isLogin) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.meta.admin && !auth.isAdmin) return { name: 'home' }
  return true
})

export default router
