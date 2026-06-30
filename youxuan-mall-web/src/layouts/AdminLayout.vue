<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <RouterLink to="/admin" class="admin-brand">
        <span class="brand-mark">优</span>
        <span>
          <b>优选商城后台</b>
          <small>YOUXUAN ADMIN</small>
        </span>
      </RouterLink>

      <nav class="admin-menu" aria-label="后台管理菜单">
        <RouterLink
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          :class="{ active: item.match(route.path) }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="admin-sidebar-footer">
        <button type="button" class="admin-user-card" @click="todo('用户设置')">
          <div class="admin-avatar">{{ userInitial }}</div>
          <div>
            <strong>{{ displayName }}</strong>
            <span>管理员</span>
          </div>
          <el-icon><ArrowDown /></el-icon>
        </button>
        <button type="button" class="admin-logout" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <section class="admin-workbench">
      <header class="admin-topbar">
        <div class="topbar-title">
          <h1>{{ pageTitle }}</h1>
        </div>

        <div class="topbar-actions">
          <button type="button" class="icon-action" title="通知" @click="todo('通知中心')">
            <el-icon><Bell /></el-icon>
            <em>12</em>
          </button>
          <el-dropdown>
            <button type="button" class="admin-profile">
              <span>{{ userInitial }}</span>
              <strong>{{ displayName }}</strong>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="todo('用户设置')">用户设置</el-dropdown-item>
                <el-dropdown-item @click="todo('操作日志')">操作日志</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-content">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Bell, Goods, House, Notebook, SwitchButton, Tickets } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { showBackendTodo } from '@/utils/feature'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menuItems = [
  { label: '商品管理', path: '/admin/products', icon: Goods, match: (path: string) => path.startsWith('/admin/products') },
  { label: '优惠券管理', path: '/admin/coupons', icon: Tickets, match: (path: string) => path.startsWith('/admin/coupons') },
  { label: '订单管理', path: '/admin/orders', icon: Notebook, match: (path: string) => path.startsWith('/admin/orders') },
  { label: '返回商城', path: '/', icon: House, match: () => false }
]

const pageTitle = computed(() => {
  if (route.path.startsWith('/admin/products')) return '商品管理'
  if (route.path.startsWith('/admin/coupons')) return '优惠券管理'
  if (route.path.startsWith('/admin/orders')) return '订单管理'
  return '数据概览'
})

const displayName = computed(() => auth.user?.nickname || auth.user?.username || '小优同学')
const userInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())
const todo = (feature: string) => showBackendTodo(feature)

const logout = () => {
  auth.logout()
  router.push('/')
}
</script>
