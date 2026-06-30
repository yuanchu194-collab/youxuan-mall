<template>
  <section class="admin-page coupon-admin-page">
    <section class="admin-filter-card coupon-filter-card">
      <div class="filter-topline">
        <el-button type="primary" :icon="Plus" class="create-button" @click="openCreate">新增优惠券</el-button>
      </div>

      <div class="filter-fields">
        <label>
          <span>优惠券名称</span>
          <el-input v-model.trim="filters.name" placeholder="请输入优惠券名称" clearable @keyup.enter="search" />
        </label>
        <label>
          <span>状态</span>
          <el-select v-model="filters.status" placeholder="全部状态" clearable>
            <el-option label="全部状态" value="" />
            <el-option label="生效中" value="active" />
            <el-option label="即将失效" value="expiring" />
            <el-option label="已失效" value="expired" />
            <el-option label="已下线" value="offline" />
          </el-select>
        </label>
        <label>
          <span>类型</span>
          <el-select v-model="filters.type" placeholder="全部类型" clearable>
            <el-option label="全部类型" value="" />
            <el-option label="满减券" value="满减券" />
            <el-option label="无门槛券" value="无门槛券" />
            <el-option label="新人专享" value="新人专享" />
          </el-select>
        </label>
        <label>
          <span>有效期</span>
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
        <div class="filter-actions">
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        </div>
      </div>
    </section>

    <section class="coupon-stats">
      <article v-for="item in stats" :key="item.label" class="admin-stat-card coupon-stat-card">
        <span :class="['stat-icon', item.tone]">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div>
          <p>{{ item.label }}</p>
          <strong>{{ item.value }}</strong>
          <em>张</em>
        </div>
      </article>
    </section>

    <div class="admin-table-card coupon-table-card" v-loading="loading">
      <el-table :data="filteredCoupons" row-key="id">
        <el-table-column label="优惠券信息" min-width="250">
          <template #default="{ row }">
            <div class="coupon-info-cell">
              <div :class="['ticket-face', ticketTone(row)]">
                <strong>{{ ticketMain(row) }}</strong>
                <span>{{ thresholdText(row) }}</span>
              </div>
              <div class="coupon-name-block">
                <strong>{{ row.name }}</strong>
                <span>{{ couponType(row) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="优惠内容" min-width="150">
          <template #default="{ row }">
            <div class="amount-cell">
              <strong>{{ amountText(row) }}</strong>
              <span>{{ couponType(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="门槛条件" min-width="135">
          <template #default="{ row }">{{ thresholdFullText(row) }}</template>
        </el-table-column>
        <el-table-column label="总库存" min-width="105">
          <template #default="{ row }">{{ numberText(row.totalStock) }}</template>
        </el-table-column>
        <el-table-column label="剩余库存" min-width="115">
          <template #default="{ row }">
            <span :class="['stock-text', Number(row.availableStock || 0) <= 0 ? 'danger' : '']">{{ numberText(row.availableStock) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已领取" min-width="105">
          <template #default="{ row }">{{ numberText(receivedCount(row)) }}</template>
        </el-table-column>
        <el-table-column label="有效期" min-width="210">
          <template #default="{ row }">
            <div class="time-cell">
              <span>{{ formatTime(row.startTime) }}</span>
              <span>至 {{ formatTime(row.endTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="115">
          <template #default="{ row }">
            <span :class="['coupon-status-pill', statusMeta(row).tone]">{{ statusMeta(row).label }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="245" fixed="right">
          <template #default="{ row }">
            <div class="table-actions coupon-actions">
              <button type="button" @click="openEdit(row)">编辑</button>
              <i />
              <button type="button" @click="preheat(row)">预热库存</button>
              <i />
              <button type="button" :class="row.status === 1 ? 'danger' : ''" @click="toggle(row)">{{ row.status === 1 ? '下线' : '上线' }}</button>
              <i />
              <button type="button" class="danger" @click="remove(row)">删除</button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="admin-empty coupon-empty">
            <el-icon><Tickets /></el-icon>
            <strong>暂无优惠券</strong>
            <span>{{ coupons.length ? '当前筛选条件下暂无优惠券' : '新增优惠券后会展示在这里' }}</span>
            <el-button type="primary" :icon="Plus" @click="openCreate">新增优惠券</el-button>
          </div>
        </template>
      </el-table>
      <div class="table-footer">
        <span>共 {{ total }} 条</span>
        <el-pagination
          class="pager"
          layout="sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          :page-sizes="[10, 20, 30]"
          @size-change="changeSize"
          @current-change="changePage"
        />
      </div>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑优惠券' : '新增优惠券'" width="760px" class="coupon-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="coupon-form">
        <div class="form-grid two">
          <el-form-item label="优惠券名称" prop="name"><el-input v-model.trim="form.name" placeholder="请输入优惠券名称" /></el-form-item>
          <el-form-item label="优惠券类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择优惠券类型">
              <el-option label="满减券" value="满减券" />
              <el-option label="无门槛券" value="无门槛券" />
              <el-option label="新人专享" value="新人专享" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid four">
          <el-form-item label="优惠金额" prop="amount"><el-input-number v-model="form.amount" :min="0.01" :precision="2" /></el-form-item>
          <el-form-item label="使用门槛" prop="minAmount"><el-input-number v-model="form.minAmount" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="发放数量 / 库存" prop="totalStock"><el-input-number v-model="form.totalStock" :min="0" :precision="0" /></el-form-item>
          <el-form-item label="每人限领" prop="perLimit"><el-input-number v-model="form.perLimit" :min="1" :precision="0" /></el-form-item>
        </div>
        <el-form-item label="有效期" prop="range">
          <el-date-picker
            v-model="form.range"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            range-separator="至"
          />
        </el-form-item>
        <div class="form-grid two">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio-button :value="1">上线</el-radio-button>
              <el-radio-button :value="0">下线</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="适用范围" prop="scope">
            <el-select v-model="form.scope" placeholder="请选择适用范围">
              <el-option label="全场通用" value="全场通用" />
              <el-option label="指定商品" value="指定商品" />
              <el-option label="指定分类" value="指定分类" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Clock, CloseBold, Plus, RefreshLeft, Search, Ticket, Tickets } from '@element-plus/icons-vue'
import { couponApi } from '@/api/modules'
import type { Coupon } from '@/types'
import { showBackendTodo } from '@/utils/feature'

type CouponStatusKey = 'active' | 'expiring' | 'expired' | 'offline' | 'upcoming'

interface CouponForm {
  id?: number
  name: string
  type: string
  amount: number
  minAmount: number
  totalStock: number
  availableStock: number
  perLimit: number
  range?: [string, string]
  status: number
  scope: string
}

const coupons = ref<Coupon[]>([])
const total = ref(0)
const dialog = ref(false)
const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10 })
const filters = reactive({
  name: '',
  status: '',
  type: '',
  range: undefined as [string, string] | undefined
})

const emptyForm = (): CouponForm => ({
  name: '',
  type: '满减券',
  amount: 10,
  minAmount: 99,
  totalStock: 100,
  availableStock: 100,
  perLimit: 1,
  range: undefined,
  status: 1,
  scope: '全场通用'
})
const form = reactive<CouponForm>(emptyForm())

const validateRange = (_rule: unknown, value: [string, string] | undefined, callback: (error?: Error) => void) => {
  if (!value?.[0] || !value?.[1]) return callback(new Error('请选择开始时间和结束时间'))
  if (new Date(value[1]).getTime() <= new Date(value[0]).getTime()) return callback(new Error('结束时间必须晚于开始时间'))
  callback()
}

const rules: FormRules = {
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  amount: [{ type: 'number', min: 0.01, message: '优惠金额必须大于 0', trigger: 'change' }],
  minAmount: [{ type: 'number', min: 0, message: '使用门槛不能小于 0', trigger: 'change' }],
  totalStock: [{ type: 'number', min: 0, message: '库存不能小于 0', trigger: 'change' }],
  range: [{ validator: validateRange, trigger: 'change' }]
}

const now = () => Date.now()
const toTime = (value?: string) => (value ? new Date(value.replace(' ', 'T')).getTime() : 0)
const receivedCount = (coupon: Coupon) => Math.max(0, Number(coupon.totalStock || 0) - Number(coupon.availableStock || 0))
const isExpiringSoon = (coupon: Coupon) => {
  const end = toTime(coupon.endTime)
  if (!end) return false
  const diff = end - now()
  return diff > 0 && diff <= 7 * 24 * 60 * 60 * 1000
}
const statusKey = (coupon: Coupon): CouponStatusKey => {
  if (coupon.status !== 1) return 'offline'
  const start = toTime(coupon.startTime)
  const end = toTime(coupon.endTime)
  const current = now()
  if (start && start > current) return 'upcoming'
  if (end && end < current) return 'expired'
  if (isExpiringSoon(coupon)) return 'expiring'
  return 'active'
}
const statusMeta = (coupon: Coupon) => {
  const key = statusKey(coupon)
  const map: Record<CouponStatusKey, { label: string; tone: string }> = {
    active: { label: '生效中', tone: 'success' },
    expiring: { label: '即将失效', tone: 'warning' },
    expired: { label: '已失效', tone: 'muted' },
    offline: { label: '已下线', tone: 'danger' },
    upcoming: { label: '未开始', tone: 'primary' }
  }
  return map[key]
}
const couponType = (coupon: Coupon) => {
  if (coupon.couponType) return coupon.couponType
  if (coupon.name?.includes('新人')) return '新人专享'
  if (Number(coupon.minAmount || 0) <= 0) return '无门槛券'
  return '满减券'
}
const ticketTone = (coupon: Coupon) => {
  const type = couponType(coupon)
  if (type === '新人专享') return 'green'
  if (type === '无门槛券') return 'red'
  if (Number(coupon.availableStock || 0) <= 0) return 'purple'
  return 'orange'
}
const ticketMain = (coupon: Coupon) => `¥${Number(coupon.amount || 0).toFixed(0)}`
const amountText = (coupon: Coupon) => `${Number(coupon.amount || 0).toFixed(0)}元`
const thresholdText = (coupon: Coupon) => (Number(coupon.minAmount || 0) <= 0 ? '无门槛' : `满${Number(coupon.minAmount).toFixed(0)}可用`)
const thresholdFullText = (coupon: Coupon) => (Number(coupon.minAmount || 0) <= 0 ? '无门槛' : `满${Number(coupon.minAmount).toFixed(0)}元可用`)
const numberText = (value?: number) => Number(value || 0).toLocaleString('zh-CN')
const formatTime = (value?: string) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

const filteredCoupons = computed(() =>
  coupons.value.filter((coupon) => {
    if (filters.name && !coupon.name.includes(filters.name)) return false
    if (filters.status && statusKey(coupon) !== filters.status) return false
    if (filters.type && couponType(coupon) !== filters.type) return false
    if (filters.range?.[0] && filters.range?.[1]) {
      const start = toTime(coupon.startTime)
      const end = toTime(coupon.endTime)
      const filterStart = new Date(`${filters.range[0]} 00:00:00`).getTime()
      const filterEnd = new Date(`${filters.range[1]} 23:59:59`).getTime()
      if (end < filterStart || start > filterEnd) return false
    }
    return true
  })
)

const stats = computed(() => [
  { label: '优惠券总数', value: total.value, tone: 'green', icon: Tickets },
  { label: '已生效', value: coupons.value.filter((item) => statusKey(item) === 'active').length, tone: 'orange', icon: Ticket },
  { label: '即将失效', value: coupons.value.filter((item) => statusKey(item) === 'expiring').length, tone: 'blue', icon: Clock },
  { label: '已失效', value: coupons.value.filter((item) => statusKey(item) === 'expired' || statusKey(item) === 'offline').length, tone: 'purple', icon: CloseBold }
])

const load = async () => {
  loading.value = true
  try {
    const data = await couponApi.page(query)
    coupons.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
const search = async () => {
  query.pageNum = 1
  await load()
}
const resetQuery = async () => {
  Object.assign(filters, { name: '', status: '', type: '', range: undefined })
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
const openCreate = () => {
  Object.assign(form, emptyForm())
  formRef.value?.clearValidate()
  dialog.value = true
}
const openEdit = (row: Coupon) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: couponType(row),
    amount: Number(row.amount || 0),
    minAmount: Number(row.minAmount || 0),
    totalStock: Number(row.totalStock || 0),
    availableStock: Number(row.availableStock || 0),
    perLimit: row.perLimit || 1,
    range: [formatTime(row.startTime), formatTime(row.endTime)],
    status: row.status,
    scope: row.scope || '全场通用'
  })
  formRef.value?.clearValidate()
  dialog.value = true
}
const save = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  if (form.id) {
    showBackendTodo('编辑优惠券')
    return
  }
  saving.value = true
  try {
    await couponApi.create({
      name: form.name,
      amount: form.amount,
      minAmount: form.minAmount,
      totalStock: form.totalStock,
      availableStock: form.totalStock,
      startTime: form.range?.[0] || '',
      endTime: form.range?.[1] || '',
      status: form.status
    })
    ElMessage.success('优惠券已新增')
    dialog.value = false
    await load()
  } finally {
    saving.value = false
  }
}
const preheat = async (row: Coupon) => {
  try {
    await ElMessageBox.confirm(`确认预热优惠券「${row.name}」库存吗？`, '预热库存', {
      confirmButtonText: '确认预热',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  await couponApi.preheat(row.id)
  ElMessage.success('预热成功，用户可到优惠券中心领取')
}
const toggle = async (row: Coupon) => {
  const action = row.status === 1 ? '下线' : '上线'
  try {
    await ElMessageBox.confirm(`确认${action}优惠券「${row.name}」吗？`, `${action}优惠券`, {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  showBackendTodo(`${action}优惠券`)
}
const remove = async (row: Coupon) => {
  try {
    await ElMessageBox.confirm(`确认删除优惠券「${row.name}」吗？删除后不可恢复。`, '删除优惠券', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  showBackendTodo('删除优惠券')
}

onMounted(load)
</script>

<style scoped>
.coupon-admin-page {
  gap: 18px;
}

.coupon-filter-card {
  padding: 22px 28px 28px;
}

.filter-topline {
  min-height: 42px;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  margin-bottom: 22px;
}

.create-button {
  min-height: 42px;
  padding: 0 22px;
  border-radius: 8px;
  font-weight: 900;
  box-shadow: 0 8px 18px rgba(21, 145, 59, 0.2);
}

.filter-fields {
  display: grid;
  grid-template-columns: minmax(210px, 1.1fr) minmax(190px, 1fr) minmax(190px, 1fr) minmax(250px, 1.05fr) auto;
  align-items: end;
  gap: 26px;
}

.filter-fields label,
.filter-fields label > span {
  display: block;
}

.filter-fields label > span {
  margin-bottom: 12px;
  color: #26342c;
  font-size: 15px;
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
  gap: 14px;
}

.filter-actions :deep(.el-button) {
  min-height: 42px;
  padding: 0 22px;
  border-radius: 8px;
  font-weight: 900;
}

.coupon-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.coupon-stat-card {
  min-height: 112px;
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  align-items: center;
  gap: 22px;
  padding: 20px 30px;
}

.stat-icon {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  border-radius: 13px;
  font-size: 25px;
}

.stat-icon.green {
  color: #16813a;
  background: #e4f7e7;
}

.stat-icon.orange {
  color: #f28725;
  background: #fff0df;
}

.stat-icon.blue {
  color: #397dec;
  background: #e9f2ff;
}

.stat-icon.purple {
  color: #8367d8;
  background: #f0ebff;
}

.coupon-stat-card p {
  margin: 0 0 8px;
  color: #3f4c44;
  font-size: 15px;
  font-weight: 800;
}

.coupon-stat-card strong {
  color: #121b15;
  font-size: 30px;
  font-weight: 900;
  line-height: 1;
}

.coupon-stat-card em {
  margin-left: 8px;
  padding: 2px 7px;
  border-radius: 6px;
  color: #8a958e;
  font-size: 13px;
  font-style: normal;
  font-weight: 800;
  background: #f2f5f3;
}

.coupon-table-card {
  border-radius: 14px;
}

.coupon-info-cell {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  align-items: center;
  gap: 18px;
}

.ticket-face {
  position: relative;
  width: 72px;
  height: 54px;
  display: grid;
  place-items: center;
  align-content: center;
  border-radius: 6px;
  color: #fff;
  overflow: hidden;
}

.ticket-face::before,
.ticket-face::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #fff;
  transform: translateY(-50%);
}

.ticket-face::before {
  left: -5px;
}

.ticket-face::after {
  right: -5px;
}

.ticket-face.green {
  background: linear-gradient(135deg, #58c76a, #26a247);
}

.ticket-face.orange {
  background: linear-gradient(135deg, #ffb147, #ff8a1d);
}

.ticket-face.red {
  background: linear-gradient(135deg, #ff7584, #ef4059);
}

.ticket-face.purple {
  background: linear-gradient(135deg, #9c83eb, #7358d2);
}

.ticket-face strong,
.ticket-face span,
.coupon-name-block strong,
.coupon-name-block span,
.amount-cell strong,
.amount-cell span,
.time-cell span {
  display: block;
}

.ticket-face strong {
  font-size: 20px;
  font-weight: 900;
  line-height: 1;
}

.ticket-face span {
  margin-top: 5px;
  font-size: 11px;
  font-weight: 800;
}

.coupon-name-block {
  min-width: 0;
}

.coupon-name-block strong {
  overflow: hidden;
  color: #18251d;
  font-size: 15px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-name-block span {
  width: fit-content;
  margin-top: 8px;
  padding: 2px 8px;
  border-radius: 5px;
  color: #16813a;
  font-size: 12px;
  font-weight: 800;
  background: #e7f7e6;
}

.amount-cell strong {
  color: #ff7100;
  font-size: 17px;
  font-weight: 900;
}

.amount-cell span,
.time-cell span {
  margin-top: 6px;
  color: #5d6b62;
  font-size: 13px;
}

.stock-text {
  color: #1ca044;
  font-weight: 900;
}

.stock-text.danger {
  color: #ef4444;
}

.coupon-status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 900;
}

.coupon-status-pill.success {
  color: #16813a;
  background: #e6f7e7;
}

.coupon-status-pill.warning {
  color: #f28a18;
  background: #fff0df;
}

.coupon-status-pill.muted {
  color: #68736d;
  background: #eef1f0;
}

.coupon-status-pill.danger {
  color: #ef4444;
  background: #fff0f0;
}

.coupon-status-pill.primary {
  color: #397dec;
  background: #e9f2ff;
}

.coupon-actions {
  display: flex;
  align-items: center;
  gap: 11px;
}

.coupon-actions button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #1e87e8;
  font-weight: 800;
  cursor: pointer;
  white-space: nowrap;
}

.coupon-actions i {
  width: 1px;
  height: 14px;
  background: #d9e0dc;
}

.coupon-actions .danger {
  color: #ef4444;
}

.coupon-empty .el-icon {
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

.coupon-form :deep(.el-select),
.coupon-form :deep(.el-input-number),
.coupon-form :deep(.el-date-editor) {
  width: 100%;
}

.form-grid {
  display: grid;
  gap: 12px;
}

.form-grid.two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-grid.four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

@media (max-width: 1320px) {
  .filter-fields {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-actions {
    justify-content: flex-start;
  }

  .coupon-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .filter-topline,
  .table-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-fields,
  .coupon-stats,
  .form-grid.two,
  .form-grid.four {
    grid-template-columns: 1fr;
  }
}
</style>
