<template>
  <div>
    <header class="shop-header" :class="{ 'shop-header-home': isHome }">
      <div class="page-shell header-inner">
        <RouterLink to="/" class="brand">
          <span class="brand-mark">优</span>
          <span>
            <b>优选商城</b>
            <small>YOUXUAN MALL</small>
          </span>
        </RouterLink>
        <nav class="nav-links">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/products">分类</RouterLink>
          <RouterLink to="/coupons">优惠券</RouterLink>
          <RouterLink to="/cart">购物车</RouterLink>
          <RouterLink to="/orders">我的订单</RouterLink>
          <RouterLink v-if="auth.isAdmin" to="/admin/products">后台管理</RouterLink>
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
    <main class="page-shell main-content">
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
  height: 76px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 170px;
}

.brand-mark {
  width: 38px;
  height: 38px;
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
  gap: 22px;
  color: #34483b;
  font-size: 14px;
}

.nav-links .router-link-active {
  color: var(--primary-dark);
  font-weight: 700;
}

.shop-header-home .nav-links .router-link-active {
  position: relative;
}

.shop-header-home .nav-links .router-link-active::after {
  position: absolute;
  right: 0;
  bottom: -27px;
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
  gap: 10px;
}

.header-search {
  width: 250px;
}

.shop-header-home .header-search {
  width: 330px;
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
  padding: 24px 0 64px;
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
