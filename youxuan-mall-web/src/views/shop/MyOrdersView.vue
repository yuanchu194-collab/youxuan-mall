<template>
  <section class="orders-page">
    <aside class="member-side">
      <div class="member-profile">
        <div class="avatar">{{ userInitial }}</div>
        <strong>{{ displayName }}</strong>
        <span>{{ roleText }}</span>
        <em>ID: {{ auth.user?.id || '未获取' }}</em>
      </div>

      <nav class="member-menu">
        <strong>订单中心</strong>
        <RouterLink to="/orders" class="active"><el-icon><Tickets /></el-icon>我的订单</RouterLink>
        <RouterLink to="/cart"><el-icon><ShoppingCart /></el-icon>我的购物车</RouterLink>
        <RouterLink to="/my-coupons"><el-icon><Ticket /></el-icon>我的优惠券</RouterLink>
        <button type="button" @click="todo('我的收藏')"><el-icon><Star /></el-icon>我的收藏</button>
        <RouterLink to="/addresses"><el-icon><Location /></el-icon>收货地址</RouterLink>
        <button type="button" @click="todo('浏览足迹')"><el-icon><Clock /></el-icon>浏览足迹</button>
        <button type="button" @click="todo('售后管理')"><el-icon><Service /></el-icon>售后管理</button>
        <button type="button" @click="todo('我的评价')"><el-icon><ChatLineRound /></el-icon>我的评价</button>
      </nav>

      <div class="support-card">
        <el-icon><Headset /></el-icon>
        <strong>客服中心</strong>
        <span>7x12小时在线服务</span>
        <button type="button" @click="todo('联系客服')">联系客服</button>
      </div>
    </aside>

    <main class="orders-main">
      <section class="orders-head">
        <div>
          <h1>我的订单</h1>
          <p>查看订单状态、支付、取消、确认收货等</p>
        </div>
        <div class="head-tools">
          <el-input v-model="keyword" class="order-search" placeholder="搜索订单编号或商品名称" :prefix-icon="Search" clearable />
          <el-select v-model="timeRange" class="time-select">
            <el-option label="全部订单" value="all" />
            <el-option label="近三个月订单" value="recent3" />
            <el-option label="近半年订单" value="recent6" />
          </el-select>
        </div>
      </section>

      <section class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          type="button"
          :class="{ active: activeStatus === tab.value }"
          @click="changeStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </section>

      <section v-loading="loading" class="orders-list-wrap">
        <div v-if="loadError" class="order-empty error">
          <el-icon><Warning /></el-icon>
          <strong>订单加载失败</strong>
          <span>{{ loadError }}</span>
          <el-button type="primary" @click="load">重新加载</el-button>
        </div>

        <div v-else-if="pagedOrders.length" class="order-list">
          <article v-for="order in pagedOrders" :key="order.id" class="order-card">
            <header class="order-card-head">
              <div>
                <span>订单号：</span>
                <strong>{{ order.orderNo }}</strong>
                <button type="button" title="复制订单号" @click="todo('复制订单号')">
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </div>
              <p>下单时间：{{ formatTime(order.createTime) }}</p>
            </header>

            <section class="order-card-body">
              <div class="order-products">
                <div v-if="order.items?.length" class="product-strip">
                  <RouterLink
                    v-for="item in previewItems(order)"
                    :key="`${order.id}-${item.id || item.productId}`"
                    class="order-product"
                    :to="`/products/${item.productId}`"
                  >
                    <img :src="getImageUrl(item)" :alt="item.productName" @error="setImageFallback" />
                    <div>
                      <h3>{{ item.productName }}</h3>
                      <p>{{ itemSpec(item) }}</p>
                      <span>¥{{ money(item.price) }} × {{ item.quantity }}</span>
                    </div>
                  </RouterLink>
                </div>
                <div v-else class="detail-missing">
                  <el-icon><ShoppingBag /></el-icon>
                  <span>订单明细暂不可用，已保留真实订单主信息</span>
                </div>
              </div>

              <div class="amount-col">
                <span>订单金额</span>
                <strong>¥{{ money(order.payAmount) }}</strong>
                <em>含运费 ¥0.00</em>
              </div>

              <div class="status-col" :class="`status-${order.status}`">
                <strong>{{ statusText(order.status) }}</strong>
                <span>{{ statusHint(order) }}</span>
              </div>

              <div class="action-col">
                <template v-if="order.status === 0">
                  <el-button type="primary" @click="pay(order.id)">立即支付</el-button>
                  <el-button @click="cancel(order.id)">取消订单</el-button>
                  <el-button text @click="viewDetail(order)">查看详情</el-button>
                </template>
                <template v-else-if="order.status === 1">
                  <el-button @click="todo('提醒发货')">提醒发货</el-button>
                  <el-button text @click="viewDetail(order)">查看详情</el-button>
                </template>
                <template v-else-if="order.status === 2">
                  <el-button type="primary" @click="receive(order.id)">确认收货</el-button>
                  <el-button @click="todo('查看物流')">查看物流</el-button>
                  <el-button text @click="viewDetail(order)">查看详情</el-button>
                </template>
                <template v-else-if="order.status === 3">
                  <el-button @click="todo('再次购买')">再次购买</el-button>
                  <el-button @click="todo('评价商品')">评价商品</el-button>
                  <el-button text @click="viewDetail(order)">查看详情</el-button>
                </template>
                <template v-else-if="order.status === 4">
                  <el-button @click="todo('删除订单')">删除订单</el-button>
                  <el-button @click="todo('再次购买')">再次购买</el-button>
                  <el-button text @click="viewDetail(order)">查看详情</el-button>
                </template>
              </div>
            </section>
          </article>
        </div>

        <div v-else-if="!loading" class="order-empty">
          <el-icon><ShoppingBag /></el-icon>
          <strong>暂无订单</strong>
          <span>去挑选一些优选好物，开启新鲜生活。</span>
          <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
        </div>
      </section>

      <footer v-if="filteredOrders.length" class="pager-row">
        <span>共 {{ filteredOrders.length }} 条</span>
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="prev, pager, next, sizes, jumper"
          :page-sizes="[5, 10, 20]"
          :total="filteredOrders.length"
        />
      </footer>
    </main>

    <el-dialog v-model="detailVisible" class="order-detail-dialog" title="订单详情" width="720px">
      <div v-if="currentOrder" class="detail-panel">
        <div class="detail-head">
          <strong>{{ currentOrder.orderNo }}</strong>
          <span :class="`status-${currentOrder.status}`">{{ statusText(currentOrder.status) }}</span>
        </div>
        <p>下单时间：{{ formatTime(currentOrder.createTime) }}</p>
        <p v-if="currentOrder.receiverAddress">收货信息：{{ currentOrder.receiverName }} {{ currentOrder.receiverPhone }} {{ currentOrder.receiverAddress }}</p>
        <div class="detail-items">
          <article v-for="item in currentOrder.items || []" :key="item.id || item.productId">
            <img :src="getImageUrl(item)" :alt="item.productName" @error="setImageFallback" />
            <div>
              <strong>{{ item.productName }}</strong>
              <span>{{ itemSpec(item) }}</span>
            </div>
            <em>¥{{ money(item.price) }} × {{ item.quantity }}</em>
          </article>
        </div>
        <div class="detail-total">
          实付金额 <strong>¥{{ money(currentOrder.payAmount) }}</strong>
        </div>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineRound,
  Clock,
  CopyDocument,
  Headset,
  Location,
  Search,
  Service,
  ShoppingBag,
  ShoppingCart,
  Star,
  Ticket,
  Tickets,
  Warning
} from '@element-plus/icons-vue'
import { orderApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { Order, OrderItem } from '@/types'
import { showBackendTodo } from '@/utils/feature'
import { getImageUrl, setImageFallback } from '@/utils/media'

const auth = useAuthStore()
const orders = ref<Order[]>([])
const loading = ref(false)
const loadError = ref('')
const activeStatus = ref<number | 'all'>('all')
const keyword = ref('')
const timeRange = ref('all')
const detailVisible = ref(false)
const currentOrder = ref<Order>()
const query = reactive({ pageNum: 1, pageSize: 5 })

const statusTabs: Array<{ label: string; value: number | 'all' }> = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const displayName = computed(() => auth.user?.nickname || auth.user?.username || '优选用户')
const userInitial = computed(() => displayName.value.slice(0, 1))
const roleText = computed(() => (String(auth.user?.role || '').toUpperCase() === 'ADMIN' ? '管理员' : '普通会员'))

const filteredOrders = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  const cutoff = timeRange.value === 'recent3' ? monthsAgo(3) : timeRange.value === 'recent6' ? monthsAgo(6) : undefined
  return orders.value.filter((order) => {
    const statusMatched = activeStatus.value === 'all' || order.status === activeStatus.value
    const keywordMatched = !key || order.orderNo.toLowerCase().includes(key) || (order.items || []).some((item) => item.productName.toLowerCase().includes(key))
    const timeMatched = !cutoff || !order.createTime || new Date(order.createTime).getTime() >= cutoff.getTime()
    return statusMatched && keywordMatched && timeMatched
  })
})

const pagedOrders = computed(() => {
  const start = (query.pageNum - 1) * query.pageSize
  return filteredOrders.value.slice(start, start + query.pageSize)
})

const monthsAgo = (months: number) => {
  const date = new Date()
  date.setMonth(date.getMonth() - months)
  return date
}

const money = (value?: number) => Number(value || 0).toFixed(2)
const statusText = (status: number) => statusMap[status] || '未知'
const todo = (feature: string) => showBackendTodo(feature)
const formatTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 19) : '-')
const previewItems = (order: Order) => (order.items || []).slice(0, 3)
const itemSpec = (item: OrderItem) => `规格：标准装｜小计 ¥${money(item.totalAmount)}`

const statusHint = (order: Order) => {
  if (order.status === 0) return '等待买家付款'
  if (order.status === 1) return order.payTime ? `支付于 ${formatTime(order.payTime).slice(5)}` : '预计尽快发货'
  if (order.status === 2) return order.trackingNo ? `${order.deliveryCompany || '物流'} ${order.trackingNo}` : '物流运输中'
  if (order.status === 3) return order.receiveTime ? `完成于 ${formatTime(order.receiveTime).slice(5)}` : '交易完成'
  if (order.status === 4) return order.cancelTime ? `取消于 ${formatTime(order.cancelTime).slice(5)}` : '订单已取消'
  return '状态更新中'
}

const hydrateDetails = async (records: Order[]) => {
  const detailed = await Promise.all(records.map(async (order) => {
    try {
      const detail = await orderApi.detail(order.id)
      return { ...order, ...detail, items: detail.items || [] }
    } catch {
      return { ...order, items: [] }
    }
  }))
  orders.value = detailed
}

const load = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const data = await orderApi.my({ pageNum: 1, pageSize: 100 })
    await hydrateDetails(data.records || [])
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '请稍后重试'
    orders.value = []
  } finally {
    loading.value = false
  }
}

const changeStatus = (status: number | 'all') => {
  activeStatus.value = status
  query.pageNum = 1
}

const pay = async (id: number) => {
  await orderApi.pay(id)
  ElMessage.success('支付成功')
  await load()
}

const cancel = async (id: number) => {
  await orderApi.cancel(id)
  ElMessage.success('订单已取消')
  await load()
}

const receive = async (id: number) => {
  await orderApi.receive(id)
  ElMessage.success('确认收货成功')
  await load()
}

const viewDetail = async (order: Order) => {
  try {
    currentOrder.value = await orderApi.detail(order.id)
    detailVisible.value = true
  } catch {
    todo('订单详情弹窗')
  }
}

watch([keyword, timeRange], () => {
  query.pageNum = 1
})

onMounted(load)
</script>

<style scoped>
.orders-page {
  display: grid;
  grid-template-columns: 230px minmax(0, 1fr);
  gap: 22px;
}

.member-side,
.orders-head,
.order-card,
.pager-row {
  border: 1px solid #e1eadf;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 30px rgba(37, 83, 45, 0.04);
}

.member-side {
  padding: 24px 16px 18px;
}

.member-profile {
  display: grid;
  justify-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #edf1ec;
}

.avatar {
  width: 70px;
  height: 70px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 28px;
  font-weight: 900;
  background: linear-gradient(135deg, #6ed884, #1d8d3e);
}

.member-profile strong {
  margin-top: 12px;
  color: #1d2922;
  font-size: 18px;
}

.member-profile span {
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  margin-top: 6px;
  padding: 0 10px;
  border-radius: 999px;
  color: #22863e;
  font-size: 12px;
  background: #eaf8e9;
}

.member-profile em {
  margin-top: 8px;
  color: #6f7d74;
  font-size: 13px;
  font-style: normal;
}

.member-menu {
  display: grid;
  gap: 7px;
  padding: 18px 0;
}

.member-menu strong {
  margin: 0 8px 7px;
  color: #1d2922;
  font-size: 15px;
}

.member-menu a,
.member-menu button {
  min-height: 38px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 10px;
  border: 0;
  border-radius: 7px;
  color: #49564f;
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.member-menu .active,
.member-menu a:hover,
.member-menu button:hover {
  color: #178437;
  font-weight: 800;
  background: #eef8e9;
}

.support-card {
  display: grid;
  gap: 6px;
  padding: 16px 14px;
  border: 1px solid #e5ede4;
  border-radius: 10px;
  background: #fbfdf9;
}

.support-card .el-icon {
  color: #219542;
  font-size: 30px;
}

.support-card strong {
  color: #1d2922;
}

.support-card span {
  color: #75827a;
  font-size: 13px;
}

.support-card button {
  min-height: 30px;
  margin-top: 8px;
  border: 0;
  border-radius: 999px;
  color: #fff;
  font-weight: 800;
  background: linear-gradient(90deg, #2faa48, #188036);
  cursor: pointer;
}

.orders-main {
  min-width: 0;
}

.orders-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 28px 12px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.orders-head h1 {
  margin: 0;
  color: #111a14;
  font-size: 30px;
  font-weight: 900;
  letter-spacing: 0;
}

.orders-head p {
  margin: 8px 0 0;
  color: #66736c;
}

.head-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-search {
  width: 260px;
}

.time-select {
  width: 148px;
}

.status-tabs {
  height: 56px;
  display: flex;
  align-items: flex-end;
  gap: 34px;
  padding: 0 28px;
  border: 1px solid #e1eadf;
  border-top: 0;
  border-bottom-right-radius: 12px;
  border-bottom-left-radius: 12px;
  background: rgba(255, 255, 255, 0.96);
}

.status-tabs button {
  position: relative;
  height: 42px;
  padding: 0 7px;
  border: 0;
  color: #2d3932;
  font-size: 15px;
  background: transparent;
  cursor: pointer;
}

.status-tabs button.active {
  color: #178437;
  font-weight: 900;
}

.status-tabs button.active::after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 3px;
  border-radius: 999px 999px 0 0;
  background: #249243;
  content: "";
}

.orders-list-wrap {
  min-height: 520px;
  margin-top: 20px;
}

.order-list {
  display: grid;
  gap: 12px;
}

.order-card {
  overflow: hidden;
}

.order-card-head {
  min-height: 42px;
  display: flex;
  align-items: center;
  gap: 26px;
  padding: 0 24px;
  color: #637067;
  font-size: 14px;
  background: #fff;
}

.order-card-head div,
.order-card-head p {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
}

.order-card-head strong {
  color: #2e3832;
  font-weight: 700;
}

.order-card-head button {
  display: grid;
  place-items: center;
  padding: 0;
  border: 0;
  color: #9aa59d;
  background: transparent;
  cursor: pointer;
}

.order-card-body {
  min-height: 98px;
  display: grid;
  grid-template-columns: minmax(390px, 1fr) 150px 158px 126px;
  align-items: center;
  padding: 12px 24px 16px;
}

.order-products,
.amount-col,
.status-col,
.action-col {
  min-height: 78px;
}

.order-products {
  min-width: 0;
  display: flex;
  align-items: center;
}

.product-strip {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #e7eee6;
  border-radius: 8px;
}

.order-product {
  min-width: 0;
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
}

.order-product img {
  width: 72px;
  height: 56px;
  display: block;
  object-fit: contain;
  border-radius: 8px;
  background: #f8f8f5;
}

.order-product h3 {
  margin: 0;
  overflow: hidden;
  color: #1f2923;
  font-size: 14px;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-product p,
.order-product span {
  display: block;
  margin: 5px 0 0;
  overflow: hidden;
  color: #69756e;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-missing {
  min-height: 78px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border: 1px dashed #dbe8d9;
  border-radius: 8px;
  color: #77847c;
  background: #fbfdf9;
}

.detail-missing .el-icon {
  color: #249243;
  font-size: 24px;
}

.amount-col,
.status-col,
.action-col {
  display: grid;
  justify-items: center;
  align-content: center;
  border-left: 1px solid #e9eee8;
}

.amount-col span {
  color: #526058;
  font-size: 13px;
}

.amount-col strong {
  margin-top: 6px;
  color: #ff4b1f;
  font-size: 20px;
  font-weight: 900;
}

.amount-col em {
  margin-top: 5px;
  color: #6e7a72;
  font-size: 12px;
  font-style: normal;
}

.status-col strong {
  font-size: 18px;
  font-weight: 900;
}

.status-col span {
  margin-top: 8px;
  color: #6f7b73;
  font-size: 12px;
}

.status-0 strong {
  color: #ff5a1f;
}

.status-1 strong,
.status-2 strong {
  color: #188a38;
}

.status-3 strong {
  color: #4b5563;
}

.status-4 strong {
  color: #6b7280;
}

.action-col {
  gap: 8px;
}

.action-col :deep(.el-button) {
  width: 96px;
  min-height: 32px;
  margin-left: 0;
  border-radius: 7px;
}

.action-col :deep(.el-button--primary) {
  background: linear-gradient(180deg, #2fa247 0%, #178334 100%);
  border-color: #178334;
}

.order-empty {
  min-height: 420px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  border: 1px dashed #d9e6d7;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
}

.order-empty .el-icon {
  color: #249243;
  font-size: 46px;
}

.order-empty strong {
  color: #1f2923;
  font-size: 22px;
}

.order-empty span {
  color: #6f7b73;
}

.order-empty.error {
  border-color: #ffd7c2;
  color: #b45309;
  background: #fff8f4;
}

.pager-row {
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 22px;
  margin-top: 16px;
}

.pager-row > span {
  color: #6e7a72;
}

.detail-panel {
  display: grid;
  gap: 12px;
}

.detail-head,
.detail-items article,
.detail-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.detail-head strong {
  color: #1d2922;
  font-size: 18px;
}

.detail-head span {
  font-weight: 900;
}

.detail-panel p {
  margin: 0;
  color: #66736c;
}

.detail-items {
  display: grid;
  gap: 10px;
  margin-top: 6px;
}

.detail-items article {
  padding: 10px;
  border: 1px solid #e5ede4;
  border-radius: 8px;
  background: #fbfdf9;
}

.detail-items img {
  width: 70px;
  height: 56px;
  object-fit: contain;
  border-radius: 8px;
  background: #f8f8f5;
}

.detail-items div {
  min-width: 0;
  flex: 1;
}

.detail-items div strong,
.detail-items div span {
  display: block;
}

.detail-items div strong {
  overflow: hidden;
  color: #1f2923;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-items div span {
  margin-top: 5px;
  color: #6f7b73;
  font-size: 13px;
}

.detail-items em {
  color: #ff4b1f;
  font-style: normal;
  font-weight: 800;
}

.detail-total {
  padding-top: 12px;
  border-top: 1px solid #edf1ec;
  color: #1d2922;
  font-weight: 900;
}

.detail-total strong {
  color: #ff4b1f;
  font-size: 24px;
}

:deep(.el-pager li.is-active) {
  color: #178437;
  font-weight: 900;
}

@media (max-width: 1180px) {
  .orders-page {
    grid-template-columns: 1fr;
  }

  .member-side {
    display: none;
  }

  .order-card-body {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .amount-col,
  .status-col,
  .action-col {
    min-height: auto;
    border-left: 0;
    justify-items: start;
  }

  .action-col {
    display: flex;
    flex-wrap: wrap;
  }
}

@media (max-width: 760px) {
  .orders-head,
  .head-tools,
  .order-card-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .order-search,
  .time-select {
    width: 100%;
  }

  .status-tabs {
    gap: 14px;
    overflow-x: auto;
  }

  .product-strip {
    grid-template-columns: 1fr;
  }

  .pager-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
