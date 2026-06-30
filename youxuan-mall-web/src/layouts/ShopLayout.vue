<template>
  <div>
    <header class="shop-header" :class="{ 'shop-header-home': isHome, 'shop-header-products': isProducts, 'shop-header-coupons': isCoupons, 'shop-header-cart': isCart, 'shop-header-checkout': isCheckout, 'shop-header-orders': isOrders }">
      <div class="page-shell header-inner">
        <RouterLink to="/" class="brand">
          <span class="brand-mark">优</span>
          <span>
            <b>优选商城</b>
            <small>YOUXUAN MALL</small>
          </span>
        </RouterLink>
        <nav class="nav-links">
          <RouterLink to="/" :class="{ 'route-active': isHome }">首页</RouterLink>
          <RouterLink to="/products" :class="{ 'route-active': isProducts }">分类</RouterLink>
          <RouterLink to="/coupons" :class="{ 'route-active': isCoupons }">优惠券</RouterLink>
          <RouterLink to="/cart" :class="{ 'route-active': isCart }">购物车</RouterLink>
          <RouterLink to="/orders" :class="{ 'route-active': isOrders }">我的订单</RouterLink>
          <RouterLink v-if="auth.isAdmin" to="/admin/products" :class="{ 'route-active': isAdminSection }">后台管理</RouterLink>
        </nav>
        <div class="header-actions">
          <el-input v-model="keyword" class="header-search" placeholder="搜索商品名称" :prefix-icon="Search" @keyup.enter="goSearch" />
          <el-button type="primary" @click="goSearch">搜索</el-button>
          <el-dropdown v-if="auth.isLogin">
            <span class="user-chip">{{ auth.user?.nickname || auth.user?.username }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/addresses')">收货地址</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/my-coupons')">我的优惠券</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <RouterLink v-else to="/login" class="login-link">登录</RouterLink>
        </div>
      </div>
    </header>
    <main class="page-shell main-content" :class="{ 'home-main-content': isHome, 'product-main-content': isProducts, 'coupons-main-content': isCoupons, 'cart-main-content': isCart, 'checkout-main-content': isCheckout, 'orders-main-content': isOrders }">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const keyword = ref('')
const isHome = computed(() => route.path === '/')
const isProducts = computed(() => route.path.startsWith('/products'))
const isCoupons = computed(() => route.path === '/coupons' || route.path === '/my-coupons')
const isCart = computed(() => route.path === '/cart')
const isCheckout = computed(() => route.path === '/checkout')
const isOrders = computed(() => route.path === '/orders' || route.path === '/addresses')
const isAdminSection = computed(() => route.path.startsWith('/admin'))

const goSearch = () => router.push({ path: '/products', query: { keyword: keyword.value || undefined } })
const logout = () => {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.shop-header {
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(222, 232, 219, 0.82);
  background: rgba(250, 253, 248, 0.88);
  backdrop-filter: blur(18px);
}

.header-inner {
  height: 68px;
  display: flex;
  align-items: center;
  gap: 22px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 160px;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 13px;
  color: #fff;
  font-weight: 800;
  background: linear-gradient(135deg, #50c66b, #1d9344);
}

.brand b,
.brand small {
  display: block;
}

.brand small {
  margin-top: 2px;
  color: var(--muted);
  font-size: 10px;
}

.nav-links {
  display: flex;
  gap: 24px;
  color: #34483b;
  font-size: 14px;
}

.nav-links .route-active {
  color: var(--primary-dark);
  font-weight: 700;
}

.shop-header-home .nav-links .route-active,
.shop-header-products .nav-links .route-active,
.shop-header-coupons .nav-links .route-active,
.shop-header-cart .nav-links .route-active,
.shop-header-checkout .nav-links .route-active,
.shop-header-orders .nav-links .route-active {
  position: relative;
}

.shop-header-home .nav-links .route-active::after,
.shop-header-products .nav-links .route-active::after,
.shop-header-coupons .nav-links .route-active::after,
.shop-header-cart .nav-links .route-active::after,
.shop-header-checkout .nav-links .route-active::after,
.shop-header-orders .nav-links .route-active::after {
  position: absolute;
  right: 0;
  bottom: -24px;
  left: 0;
  height: 3px;
  border-radius: 999px 999px 0 0;
  background: var(--yx-primary);
  content: "";
}

.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 9px;
}

.header-search {
  width: 250px;
}

.shop-header-home .header-search {
  width: 360px;
}

.shop-header :deep(.el-input__wrapper) {
  min-height: 40px;
  border-radius: 12px;
}

.shop-header :deep(.el-button) {
  min-height: 40px;
  padding-right: 22px;
  padding-left: 22px;
  border-radius: 12px;
}

.user-chip,
.login-link {
  padding: 9px 12px;
  border-radius: 999px;
  background: #eef8ee;
  color: #237c3c;
  cursor: pointer;
}

.main-content {
  padding: 18px 0 56px;
}

.home-main-content {
  width: min(1480px, calc(100vw - 48px));
  padding-top: 18px;
}

.shop-header-products .header-inner,
.product-main-content,
.shop-header-coupons .header-inner,
.coupons-main-content,
.shop-header-cart .header-inner,
.cart-main-content,
.shop-header-checkout .header-inner,
.checkout-main-content,
.shop-header-orders .header-inner,
.orders-main-content {
  width: min(1480px, calc(100vw - 48px));
}

@media (max-width: 980px) {
  .header-inner {
    height: auto;
    padding: 14px 0;
    flex-wrap: wrap;
  }

  .header-actions {
    width: 100%;
    margin-left: 0;
  }

  .header-search {
    flex: 1;
    width: auto;
  }
}
</style>
