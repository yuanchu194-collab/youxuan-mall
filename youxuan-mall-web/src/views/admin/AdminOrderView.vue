<template>
  <section class="admin-page order-admin-page">
    <section class="order-tabs-card">
      <button
        v-for="item in tabOptions"
        :key="item.key"
        type="button"
        :class="{ active: activeTab === item.key }"
        @click="changeTab(item.key)"
      >
        {{ item.label }}
      </button>
    </section>

    <section class="admin-filter-card order-filter-card">
      <div class="filter-fields">
        <label>
          <span>订单编号</span>
          <el-input v-model.trim="filters.orderNo" placeholder="请输入订单号" clearable @keyup.enter="search" />
        </label>
        <label>
          <span>用户关键词</span>
          <el-input v-model.trim="filters.userKeyword" placeholder="用户ID / 收货人 / 手机号" clearable @keyup.enter="search" />
        </label>
        <label>
          <span>订单状态</span>
          <el-select v-model="query.status" placeholder="订单状态" clearable @change="reload">
            <el-option label="全部状态" :value="undefined" />
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </label>
        <label>
          <span>下单时间范围</span>
          <el-date-picker
            v-model="filters.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            range-separator="至"
            clearable
          />
        </label>
      </div>
      <div class="filter-actions">
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
      </div>
    </section>

    <div class="admin-table-card order-table-card" v-loading="loading">
      <el-table :data="filteredOrders" row-key="id">
        <el-table-column label="订单号" min-width="185">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户ID" min-width="115">
          <template #default="{ row }">
            <span class="user-code">{{ userLabel(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额（元）" min-width="120">
          <template #default="{ row }">{{ currency(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="优惠（元）" min-width="110">
          <template #default="{ row }">
            <span class="discount-text">- {{ currency(row.discountAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实付（元）" min-width="120">
          <template #default="{ row }">{{ currency(row.payAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <span :class="['order-status-pill', statusTone(row.status)]">{{ statusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收货人" min-width="145">
          <template #default="{ row }">
            <div class="receiver-cell">
              <strong>{{ row.receiverName || '-' }}</strong>
              <span>{{ maskPhone(row.receiverPhone) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收货地址" min-width="250">
          <template #default="{ row }">
            <span class="address-text">{{ row.receiverAddress || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="150">
          <template #default="{ row }">
            <div class="time-cell">
              <span>{{ datePart(row.createTime) }}</span>
              <span>{{ timePart(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <div class="order-actions">
              <button v-if="row.status === 1" type="button" class="ship" @click="openShip(row)">发货</button>
              <button v-else type="button" @click="openDetail(row)">查看详情</button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="admin-empty order-empty">
            <el-icon><Notebook /></el-icon>
            <strong>暂无订单</strong>
            <span>{{ orders.length ? '当前筛选条件下暂无订单' : '真实接口返回后会展示在这里' }}</span>
          </div>
        </template>
      </el-table>
      <div class="table-footer">
        <span>共 {{ total }} 条</span>
        <el-pagination
          class="pager"
          layout="prev, pager, next, sizes, jumper"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          :page-sizes="[10, 20, 30]"
          @size-change="changeSize"
          @current-change="changePage"
        />
      </div>
    </div>

    <el-dialog v-model="shipDialog" title="订单发货" width="500px" class="order-ship-dialog">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-position="top" class="ship-form">
        <el-form-item label="物流公司" prop="deliveryCompany">
          <el-input v-model.trim="shipForm.deliveryCompany" placeholder="请输入物流公司" />
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input v-model.trim="shipForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialog = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="ship">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog" title="订单详情" width="720px" class="order-detail-dialog">
      <div v-if="detailLoading" class="detail-loading">正在加载订单详情...</div>
      <div v-else-if="detailOrder" class="detail-panel">
        <div class="detail-grid">
          <p><span>订单号</span><strong>{{ detailOrder.orderNo }}</strong></p>
          <p><span>订单状态</span><strong>{{ statusText(detailOrder.status) }}</strong></p>
          <p><span>实付金额</span><strong>{{ currency(detailOrder.payAmount) }}</strong></p>
          <p><span>下单时间</span><strong>{{ formatTime(detailOrder.createTime) }}</strong></p>
        </div>
        <div class="detail-section">
          <h3>收货信息</h3>
          <p>{{ detailOrder.receiverName || '-' }} / {{ maskPhone(detailOrder.receiverPhone) }}</p>
          <p>{{ detailOrder.receiverAddress || '-' }}</p>
        </div>
        <div class="detail-section">
          <h3>商品明细</h3>
          <div v-if="detailOrder.items?.length" class="detail-items">
            <div v-for="item in detailOrder.items" :key="item.id || item.productId" class="detail-item">
              <span>{{ item.productName }}</span>
              <em>x{{ item.quantity }}</em>
              <strong>{{ currency(item.totalAmount) }}</strong>
            </div>
          </div>
          <p v-else>接口未返回商品明细</p>
        </div>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Notebook, RefreshLeft, Search } from '@element-plus/icons-vue'
import { orderApi } from '@/api/modules'
import type { Order } from '@/types'

type TabKey = 'all' | '0' | '1' | '2' | '3' | '4'

const orders = ref<Order[]>([])
const total = ref(0)
const loading = ref(false)
const shipping = ref(false)
const shipDialog = ref(false)
const detailDialog = ref(false)
const detailLoading = ref(false)
const currentOrder = ref<Order>()
const detailOrder = ref<Order>()
const shipFormRef = ref<FormInstance>()
const activeTab = ref<TabKey>('all')
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined as number | undefined })
const filters = reactive({
  orderNo: '',
  userKeyword: '',
  range: undefined as [string, string] | undefined
})
const shipForm = reactive({ deliveryCompany: '', trackingNo: '' })

const tabOptions: Array<{ label: string; key: TabKey; value?: number }> = [
  { label: '全部', key: 'all' },
  { label: '待支付', key: '0', value: 0 },
  { label: '待发货', key: '1', value: 1 },
  { label: '待收货', key: '2', value: 2 },
  { label: '已完成', key: '3', value: 3 },
  { label: '已取消', key: '4', value: 4 }
]
const statusOptions = tabOptions.filter((item) => item.key !== 'all').map((item) => ({ label: item.label, value: item.value as number }))
const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const shipRules: FormRules = {
  deliveryCompany: [{ required: true, message: '物流公司不能为空', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '物流单号不能为空', trigger: 'blur' }]
}

const statusText = (status: number) => statusMap[status] || '未知'
const statusTone = (status: number) => {
  const map: Record<number, string> = {
    0: 'paying',
    1: 'shipping',
    2: 'receiving',
    3: 'done',
    4: 'canceled'
  }
  return map[status] || 'canceled'
}
const currency = (value?: number) => `¥ ${Number(value || 0).toFixed(2)}`
const formatTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 19) : '-')
const datePart = (value?: string) => formatTime(value).slice(0, 10)
const timePart = (value?: string) => formatTime(value).slice(11, 19)
const userLabel = (order: Order) => (order.userId ? `user_${String(order.userId).padStart(5, '0')}` : '-')
const maskPhone = (phone?: string) => {
  if (!phone) return '-'
  if (phone.length < 7) return phone
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}
const toTime = (value?: string) => (value ? new Date(value.replace(' ', 'T')).getTime() : 0)

const filteredOrders = computed(() =>
  orders.value.filter((order) => {
    if (filters.orderNo && !order.orderNo.includes(filters.orderNo)) return false
    const keyword = filters.userKeyword
    if (keyword) {
      const haystack = [userLabel(order), String(order.userId || ''), order.receiverName || '', order.receiverPhone || ''].join(' ')
      if (!haystack.includes(keyword)) return false
    }
    if (filters.range?.[0] && filters.range?.[1]) {
      const created = toTime(order.createTime)
      const start = new Date(`${filters.range[0]} 00:00:00`).getTime()
      const end = new Date(`${filters.range[1]} 23:59:59`).getTime()
      if (created < start || created > end) return false
    }
    return true
  })
)

const syncTabFromStatus = () => {
  activeTab.value = query.status === undefined ? 'all' : (String(query.status) as TabKey)
}
const load = async () => {
  loading.value = true
  try {
    const data = await orderApi.adminPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status
    })
    orders.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
const reload = async () => {
  query.pageNum = 1
  syncTabFromStatus()
  await load()
}
const search = async () => {
  query.pageNum = 1
  await load()
}
const resetQuery = async () => {
  Object.assign(filters, { orderNo: '', userKeyword: '', range: undefined })
  query.status = undefined
  activeTab.value = 'all'
  query.pageNum = 1
  await load()
}
const changeTab = async (key: TabKey) => {
  activeTab.value = key
  query.status = key === 'all' ? undefined : Number(key)
  query.pageNum = 1
  await load()
}
const changePage = async (page: number) => {
  query.pageNum = page
  await load()
}
const changeSize = async (size: number) => {
  query.pageSize = size
  query.pageNum = 1
  await load()
}
const openShip = (order: Order) => {
  currentOrder.value = order
  shipForm.deliveryCompany = ''
  shipForm.trackingNo = ''
  shipFormRef.value?.clearValidate()
  shipDialog.value = true
}
const ship = async () => {
  if (!currentOrder.value || !shipFormRef.value) return
  await shipFormRef.value.validate()
  shipping.value = true
  try {
    await orderApi.ship(currentOrder.value.id, {
      deliveryCompany: shipForm.deliveryCompany,
      trackingNo: shipForm.trackingNo
    })
    ElMessage.success('发货成功')
    shipDialog.value = false
    await load()
  } finally {
    shipping.value = false
  }
}
const openDetail = async (order: Order) => {
  detailDialog.value = true
  detailLoading.value = true
  detailOrder.value = undefined
  try {
    detailOrder.value = await orderApi.detail(order.id)
  } finally {
    detailLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.order-admin-page {
  gap: 18px;
}

.order-tabs-card,
.order-filter-card,
.order-table-card {
  border: 1px solid rgba(219, 229, 219, 0.95);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 10px 30px rgba(30, 72, 43, 0.05);
}

.order-tabs-card {
  min-height: 66px;
  display: flex;
  align-items: center;
  gap: 34px;
  padding: 0 22px;
}

.order-tabs-card button {
  min-width: 84px;
  min-height: 38px;
  border: 0;
  border-radius: 999px;
  color: #46554d;
  background: transparent;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.order-tabs-card button.active {
  color: #fff;
  background: linear-gradient(135deg, #22a646, #16813a);
  box-shadow: 0 8px 18px rgba(22, 129, 58, 0.22);
}

.order-filter-card {
  min-height: 86px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 28px;
  padding: 20px 24px;
}

.filter-fields {
  display: grid;
  grid-template-columns: minmax(190px, 1fr) minmax(190px, 1fr) minmax(170px, 0.9fr) minmax(260px, 1.25fr);
  gap: 20px;
}

.filter-fields label,
.filter-fields label > span {
  display: block;
}

.filter-fields label > span {
  margin-bottom: 10px;
  color: #26342c;
  font-size: 14px;
  font-weight: 800;
}

.filter-fields :deep(.el-input__wrapper),
.filter-fields :deep(.el-select__wrapper),
.filter-fields :deep(.el-date-editor) {
  width: 100%;
  min-height: 42px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dfe7df inset;
}

.filter-actions {
  display: flex;
  gap: 16px;
}

.filter-actions :deep(.el-button) {
  min-height: 42px;
  padding: 0 28px;
  border-radius: 8px;
  font-weight: 900;
}

.order-table-card {
  overflow: hidden;
}

.order-no {
  color: #16923d;
  font-weight: 900;
}

.user-code {
  color: #44534a;
  font-weight: 700;
}

.discount-text {
  color: #4d5a52;
}

.receiver-cell strong,
.receiver-cell span,
.time-cell span {
  display: block;
}

.receiver-cell strong {
  color: #2f3c34;
  font-weight: 800;
}

.receiver-cell span {
  margin-top: 5px;
  color: #5c6961;
  font-size: 13px;
}

.address-text {
  display: block;
  max-width: 260px;
  color: #4a584f;
  line-height: 1.65;
}

.time-cell span {
  color: #4a584f;
  line-height: 1.55;
}

.order-status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 900;
}

.order-status-pill.paying {
  color: #f08a18;
  background: #fff3df;
  box-shadow: 0 0 0 1px #ffdca4 inset;
}

.order-status-pill.shipping {
  color: #2a7ee8;
  background: #e9f3ff;
  box-shadow: 0 0 0 1px #b7d7ff inset;
}

.order-status-pill.receiving {
  color: #8a45e6;
  background: #f2e9ff;
  box-shadow: 0 0 0 1px #dbc4ff inset;
}

.order-status-pill.done {
  color: #16813a;
  background: #e6f7e7;
  box-shadow: 0 0 0 1px #c5edc8 inset;
}

.order-status-pill.canceled {
  color: #68736d;
  background: #eef1f0;
  box-shadow: 0 0 0 1px #dce2df inset;
}

.order-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-actions button {
  min-height: 30px;
  border: 1px solid transparent;
  border-radius: 6px;
  padding: 0 10px;
  color: #16923d;
  background: transparent;
  font-weight: 900;
  cursor: pointer;
}

.order-actions button.ship {
  color: #fff;
  background: #16813a;
  box-shadow: 0 8px 16px rgba(22, 129, 58, 0.18);
}

.order-empty .el-icon {
  color: #16813a;
  font-size: 42px;
}

.table-footer {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 24px;
  border-top: 1px solid #edf2ec;
  color: #647269;
}

.ship-form :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 8px;
}

.detail-loading {
  min-height: 160px;
  display: grid;
  place-items: center;
  color: #66756d;
}

.detail-panel {
  display: grid;
  gap: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-grid p,
.detail-section p {
  margin: 0;
}

.detail-grid p {
  padding: 14px;
  border-radius: 10px;
  background: #f7faf7;
}

.detail-grid span,
.detail-grid strong {
  display: block;
}

.detail-grid span {
  color: #748078;
  font-size: 12px;
}

.detail-grid strong {
  margin-top: 6px;
  color: #17231b;
}

.detail-section {
  padding-top: 14px;
  border-top: 1px solid #edf2ec;
}

.detail-section h3 {
  margin: 0 0 10px;
  color: #17231b;
  font-size: 15px;
}

.detail-section p {
  color: #59675e;
  line-height: 1.7;
}

.detail-items {
  display: grid;
  gap: 8px;
}

.detail-item {
  min-height: 42px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 60px 100px;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  border-radius: 8px;
  background: #f7faf7;
}

.detail-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-item em {
  color: #66756d;
  font-style: normal;
}

.detail-item strong {
  text-align: right;
}

@media (max-width: 1320px) {
  .order-filter-card {
    grid-template-columns: 1fr;
  }

  .filter-fields {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 820px) {
  .order-tabs-card {
    flex-wrap: wrap;
    gap: 12px;
    padding: 14px;
  }

  .filter-fields,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .table-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
