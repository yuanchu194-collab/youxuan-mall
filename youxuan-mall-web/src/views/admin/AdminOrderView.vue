<template>
  <section>
    <div class="admin-toolbar glass-card">
      <h2>订单管理</h2>
      <el-select v-model="query.status" clearable placeholder="全部状态" @change="reload">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>

    <div class="admin-table-card glass-card">
      <el-table :data="orders" row-key="id">
        <el-table-column prop="orderNo" label="订单编号" min-width="190" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="totalAmount" label="订单金额" width="105" />
        <el-table-column prop="discountAmount" label="优惠" width="90" />
        <el-table-column prop="payAmount" label="实付" width="95" />
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="240">
          <template #default="{ row }">
            <b>{{ row.receiverName || '-' }}</b>
            <span class="muted-line">{{ row.receiverPhone || '-' }}</span>
            <span class="muted-line">{{ row.receiverAddress || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="165" />
        <el-table-column prop="payTime" label="支付时间" min-width="165" />
        <el-table-column label="发货信息" min-width="180">
          <template #default="{ row }">
            <span>{{ row.deliveryCompany || '未发货' }}</span>
            <span class="muted-line">{{ row.trackingNo || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="105" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" text type="primary" @click="openShip(row)">发货</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" layout="prev, pager, next" :total="total" :page-size="query.pageSize" @current-change="changePage" />
    </div>

    <el-dialog v-model="shipDialog" title="订单发货" width="460px">
      <el-form label-position="top">
        <el-form-item label="物流公司"><el-input v-model="shipForm.deliveryCompany" /></el-form-item>
        <el-form-item label="物流单号"><el-input v-model="shipForm.trackingNo" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialog = false">取消</el-button>
        <el-button type="primary" @click="ship">确认发货</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/modules'
import type { Order } from '@/types'

const orders = ref<Order[]>([])
const total = ref(0)
const shipDialog = ref(false)
const currentOrder = ref<Order>()
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined as number | undefined })
const shipForm = reactive({ deliveryCompany: '顺丰速运', trackingNo: '' })
const statusOptions = [
  { label: '待支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]
const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const statusText = (status: number) => statusMap[status] || '未知'
const statusType = (status: number) => (status === 3 ? 'success' : status === 4 ? 'info' : status === 0 ? 'warning' : 'primary')

const load = async () => {
  const data = await orderApi.adminPage(query)
  orders.value = data.records || []
  total.value = data.total || 0
}
const reload = async () => {
  query.pageNum = 1
  await load()
}
const changePage = async (page: number) => {
  query.pageNum = page
  await load()
}
const openShip = (order: Order) => {
  currentOrder.value = order
  shipForm.deliveryCompany = '顺丰速运'
  shipForm.trackingNo = `SF${Date.now()}`
  shipDialog.value = true
}
const ship = async () => {
  if (!currentOrder.value) return
  if (!shipForm.deliveryCompany || !shipForm.trackingNo) return ElMessage.warning('请填写物流公司和物流单号')
  await orderApi.ship(currentOrder.value.id, shipForm)
  ElMessage.success('发货成功')
  shipDialog.value = false
  await load()
}

onMounted(load)
</script>

<style scoped>
.admin-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px;
  margin-bottom: 18px;
}

.admin-toolbar h2 {
  margin: 0;
}

.muted-line {
  display: block;
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}

.pager {
  justify-content: center;
  margin-top: 18px;
}
</style>
