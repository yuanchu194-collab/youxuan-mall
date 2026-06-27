<template>
  <section>
    <div class="section-head">
      <div>
        <h2 class="section-title">我的订单</h2>
        <p class="section-subtitle">模拟支付、取消订单、确认收货</p>
      </div>
    </div>
    <div v-if="orders.length" class="order-list">
      <article v-for="order in orders" :key="order.id" class="order-card glass-card">
        <div>
          <b>订单号：{{ order.orderNo }}</b>
          <span>{{ order.createTime }}</span>
        </div>
        <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
        <strong>¥{{ money(order.payAmount) }}</strong>
        <div class="order-actions">
          <el-button v-if="order.status === 0" type="primary" @click="pay(order.id)">模拟支付</el-button>
          <el-button v-if="order.status === 0" @click="cancel(order.id)">取消订单</el-button>
          <el-button v-if="order.status === 2" type="primary" @click="receive(order.id)">确认收货</el-button>
        </div>
      </article>
    </div>
    <div v-else class="empty-state">暂无订单</div>
    <el-pagination class="pager" layout="prev, pager, next" :total="total" :page-size="query.pageSize" @current-change="changePage" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/modules'
import type { Order } from '@/types'

const orders = ref<Order[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const money = (value?: number) => Number(value || 0).toFixed(2)
const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const statusText = (status: number) => statusMap[status] || '未知'
const statusType = (status: number) => (status === 3 ? 'success' : status === 4 ? 'info' : status === 0 ? 'warning' : 'primary')

const load = async () => {
  const data = await orderApi.my(query)
  orders.value = data.records || []
  total.value = data.total || 0
}
const changePage = async (page: number) => {
  query.pageNum = page
  await load()
}
const pay = async (id: number) => {
  await orderApi.pay(id)
  ElMessage.success('支付成功')
  await load()
}
const cancel = async (id: number) => {
  await orderApi.cancel(id)
  await load()
}
const receive = async (id: number) => {
  await orderApi.receive(id)
  ElMessage.success('确认收货成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.order-list {
  display: grid;
  gap: 14px;
}

.order-card {
  display: grid;
  grid-template-columns: 1fr auto auto auto;
  align-items: center;
  gap: 18px;
  padding: 20px;
}

.order-card span {
  display: block;
  margin-top: 8px;
  color: var(--muted);
  font-size: 13px;
}

.order-card strong {
  color: #e8392f;
  font-size: 20px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 24px;
  justify-content: center;
}

@media (max-width: 780px) {
  .order-card {
    grid-template-columns: 1fr;
    align-items: start;
  }
}
</style>
