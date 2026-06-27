<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <RouterLink to="/" class="admin-brand">优选商城</RouterLink>
      <RouterLink to="/admin/products">商品管理</RouterLink>
      <RouterLink to="/admin/coupons">优惠券管理</RouterLink>
      <RouterLink to="/admin/orders">订单管理</RouterLink>
      <RouterLink to="/">返回商城</RouterLink>
    </aside>
    <main class="admin-main">
      <div class="admin-top">
        <span>优选商城后台管理</span>
        <span>{{ auth.user?.username }}</span>
      </div>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 230px 1fr;
}

.admin-sidebar {
  padding: 24px 18px;
  background: linear-gradient(180deg, #173523, #13241c);
  color: #cfe5d5;
}

.admin-brand {
  display: block;
  margin-bottom: 28px;
  color: #fff;
  font-size: 22px;
  font-weight: 800;
}

.admin-sidebar a {
  display: block;
  padding: 13px 14px;
  margin: 6px 0;
  border-radius: 14px;
}

.admin-sidebar .router-link-active {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
}

.admin-main {
  padding: 26px;
}

.admin-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  color: var(--muted);
}

@media (max-width: 760px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    display: flex;
    gap: 10px;
    overflow-x: auto;
  }

  .admin-brand {
    margin: 0;
    white-space: nowrap;
  }
}
</style>
