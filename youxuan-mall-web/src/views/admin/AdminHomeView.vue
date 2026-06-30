<template>
  <section class="admin-home">
    <div class="admin-toolbar">
      <div>
        <h2>数据概览</h2>
        <p>后台首页当前只提供入口，核心数据看板后续阶段补齐。</p>
      </div>
      <el-button type="primary" @click="todo('后台首页')">查看概览</el-button>
    </div>

    <div class="admin-home-grid">
      <RouterLink to="/admin/products" class="admin-home-card">
        <el-icon><Goods /></el-icon>
        <h2>商品管理</h2>
        <p>维护商品资料、上下架状态、主图和库存。</p>
      </RouterLink>
      <RouterLink to="/admin/coupons" class="admin-home-card">
        <el-icon><Tickets /></el-icon>
        <h2>优惠券管理</h2>
        <p>创建优惠券、预热库存并查看活动状态。</p>
      </RouterLink>
      <RouterLink to="/admin/orders" class="admin-home-card">
        <el-icon><Notebook /></el-icon>
        <h2>订单管理</h2>
        <p>处理订单发货，查看订单状态和收货信息。</p>
      </RouterLink>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Goods, Notebook, Tickets } from '@element-plus/icons-vue'
import { showBackendTodo } from '@/utils/feature'

const todo = (feature: string) => showBackendTodo(feature)
</script>
