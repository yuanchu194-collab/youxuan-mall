<template>
  <section class="address-page">
    <nav class="address-crumb">
      <RouterLink to="/">首页</RouterLink>
      <el-icon><ArrowRight /></el-icon>
      <span>收货地址</span>
    </nav>

    <section class="address-shell">
      <header class="address-hero">
        <div class="hero-title">
          <el-icon><Location /></el-icon>
          <h1>收货地址</h1>
          <i></i>
          <p>管理常用收货地址，下单时快速选择</p>
        </div>
        <el-button type="primary" :icon="CirclePlus" @click="openCreate">新增地址</el-button>
      </header>

      <section class="stats-grid" aria-label="地址统计">
        <article>
          <span>地址总数</span>
          <strong>{{ stats.total }}</strong>
          <em>真实地址簿记录</em>
        </article>
        <article>
          <span>默认地址</span>
          <strong>{{ stats.defaultCount }}</strong>
          <em>{{ defaultAddress ? defaultAddress.receiverName : '暂未设置' }}</em>
        </article>
        <article class="todo-stat" @click="todo('最近使用地址')">
          <span>最近使用地址</span>
          <strong>{{ stats.recentUsed }}</strong>
          <em>后端暂未返回使用记录</em>
        </article>
        <article>
          <span>可用地址数量</span>
          <strong>{{ stats.available }}</strong>
          <em>可用于订单结算</em>
        </article>
      </section>

      <section class="toolbar-card">
        <div>
          <h2>地址簿</h2>
          <p>默认地址会在订单确认页优先展示</p>
        </div>
        <div class="toolbar-actions">
          <button type="button" @click="todo('地址搜索')"><el-icon><Search /></el-icon>地址搜索</button>
          <button type="button" @click="todo('智能识别地址')"><el-icon><MagicStick /></el-icon>智能识别</button>
          <button type="button" @click="todo('定位当前地址')"><el-icon><Position /></el-icon>定位地址</button>
          <button type="button" @click="todo('地址簿导入')"><el-icon><Upload /></el-icon>导入</button>
        </div>
      </section>

      <section v-loading="loading" class="address-body">
        <div v-if="loadError" class="state-box error">
          <el-icon><Warning /></el-icon>
          <strong>地址加载失败</strong>
          <span>{{ loadError }}</span>
          <el-button type="primary" @click="load">重新加载</el-button>
        </div>

        <div v-else-if="addresses.length" class="address-grid">
          <article
            v-for="(item, index) in addresses"
            :key="item.id"
            class="address-card"
            :class="{ active: item.defaultFlag === 1 }"
          >
            <span v-if="item.defaultFlag === 1" class="default-badge">默认地址</span>
            <span v-else-if="index === 1" class="soft-badge">常用地址</span>
            <strong>
              {{ item.receiverName }}
              <em>{{ maskPhone(item.receiverPhone) }}</em>
            </strong>
            <p class="region">{{ item.province }} {{ item.city }} {{ item.district }}</p>
            <p class="detail">{{ item.detailAddress }}</p>
            <div class="meta-row">
              <span>{{ formatTime(item.updateTime || item.createTime) }}</span>
              <button type="button" @click="todo('地址标签管理')">标签管理</button>
            </div>
            <i class="radio-dot"></i>
            <el-icon v-if="item.defaultFlag === 1" class="checked-corner"><Check /></el-icon>
            <footer class="card-actions">
              <button type="button" @click="openEdit(item)">编辑</button>
              <button type="button" :disabled="item.defaultFlag === 1" @click="setDefault(item)">设置默认</button>
              <button type="button" class="danger" @click="remove(item)">删除</button>
            </footer>
          </article>

          <button type="button" class="address-card add-address-card" @click="openCreate">
            <el-icon><CirclePlus /></el-icon>
            <span>新增收货地址</span>
          </button>
        </div>

        <div v-else class="state-box empty">
          <div class="empty-visual">
            <el-icon><Location /></el-icon>
          </div>
          <strong>暂无收货地址</strong>
          <span>新增一个常用地址，下单时可以快速选择</span>
          <el-button type="primary" :icon="CirclePlus" @click="openCreate">新增地址</el-button>
        </div>
      </section>

      <footer class="service-strip">
        <div>
          <el-icon><CircleCheck /></el-icon>
          <strong>下单快选</strong>
          <span>默认地址优先展示</span>
        </div>
        <div>
          <el-icon><Van /></el-icon>
          <strong>冷链配送</strong>
          <span>覆盖主要城区</span>
        </div>
        <div>
          <el-icon><Lock /></el-icon>
          <strong>信息安全</strong>
          <span>地址仅用于订单配送</span>
        </div>
        <div>
          <el-icon><MapLocation /></el-icon>
          <strong>地图选点</strong>
          <button type="button" @click="todo('地图选点')">查看入口</button>
        </div>
      </footer>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑收货地址' : '新增收货地址'" width="620px" class="address-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <div class="form-row two">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model.trim="form.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="receiverPhone">
            <el-input v-model.trim="form.receiverPhone" placeholder="请输入 11 位手机号" />
          </el-form-item>
        </div>

        <div class="form-row three">
          <el-form-item label="省份" prop="province">
            <el-input v-model.trim="form.province" placeholder="省" />
          </el-form-item>
          <el-form-item label="城市" prop="city">
            <el-input v-model.trim="form.city" placeholder="市" />
          </el-form-item>
          <el-form-item label="区县" prop="district">
            <el-input v-model.trim="form.district" placeholder="区/县" />
          </el-form-item>
        </div>

        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model.trim="form.detailAddress" type="textarea" :rows="3" placeholder="街道、门牌号、楼层房间号等" />
        </el-form-item>

        <div class="form-extras">
          <el-checkbox v-model="isDefault">设为默认地址</el-checkbox>
          <button type="button" @click="todo('智能识别地址')">智能识别地址</button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存地址</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowRight,
  Check,
  CircleCheck,
  CirclePlus,
  Location,
  Lock,
  MagicStick,
  MapLocation,
  Position,
  Search,
  Upload,
  Van,
  Warning
} from '@element-plus/icons-vue'
import { addressApi } from '@/api/modules'
import type { Address } from '@/types'
import { showBackendTodo } from '@/utils/feature'

type AddressForm = Omit<Address, 'createTime' | 'updateTime'>

const emptyForm = (): AddressForm => ({
  id: 0,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  defaultFlag: 0
})

const addresses = ref<Address[]>([])
const loading = ref(false)
const saving = ref(false)
const loadError = ref('')
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<AddressForm>(emptyForm())

const phonePattern = /^1[3-9]\d{9}$/

const rules: FormRules<AddressForm> = {
  receiverName: [{ required: true, message: '收货人不能为空', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: phonePattern, message: '手机号格式必须合法', trigger: 'blur' }
  ],
  province: [{ required: true, message: '省份不能为空', trigger: 'blur' }],
  city: [{ required: true, message: '城市不能为空', trigger: 'blur' }],
  district: [{ required: true, message: '区县不能为空', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '详细地址不能为空', trigger: 'blur' }]
}

const isDefault = computed({
  get: () => form.defaultFlag === 1,
  set: (value: boolean) => {
    form.defaultFlag = value ? 1 : 0
  }
})

const defaultAddress = computed(() => addresses.value.find((item) => item.defaultFlag === 1))

const stats = computed(() => ({
  total: addresses.value.length,
  defaultCount: addresses.value.filter((item) => item.defaultFlag === 1).length,
  recentUsed: 0,
  available: addresses.value.length
}))

const todo = (feature: string) => showBackendTodo(feature)

const fullAddress = (item: Address) => `${item.province}${item.city}${item.district}${item.detailAddress}`

const maskPhone = (phone?: string) => {
  if (!phone || phone.length < 7) return phone || '-'
  return `${phone.slice(0, 3)} **** ${phone.slice(-4)}`
}

const formatTime = (value?: string) => {
  if (!value) return '真实接口未返回更新时间'
  return `更新于 ${value.replace('T', ' ').slice(0, 16)}`
}

const reset = () => {
  Object.assign(form, emptyForm())
  formRef.value?.clearValidate()
}

const openCreate = () => {
  reset()
  if (!addresses.value.length) form.defaultFlag = 1
  dialogVisible.value = true
}

const openEdit = (item: Address) => {
  Object.assign(form, {
    id: item.id,
    receiverName: item.receiverName,
    receiverPhone: item.receiverPhone,
    province: item.province,
    city: item.city,
    district: item.district,
    detailAddress: item.detailAddress,
    defaultFlag: item.defaultFlag || 0
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const payload = () => ({
  receiverName: form.receiverName,
  receiverPhone: form.receiverPhone,
  province: form.province,
  city: form.city,
  district: form.district,
  detailAddress: form.detailAddress,
  defaultFlag: form.defaultFlag ? 1 : 0
})

const load = async () => {
  loading.value = true
  loadError.value = ''
  try {
    addresses.value = await addressApi.list()
  } catch (error) {
    addresses.value = []
    loadError.value = error instanceof Error ? error.message : '地址列表加载失败'
  } finally {
    loading.value = false
  }
}

const save = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await addressApi.update(form.id, payload())
      ElMessage.success('地址已更新')
    } else {
      await addressApi.create(payload())
      ElMessage.success('地址已新增')
    }
    dialogVisible.value = false
    reset()
    await load()
  } finally {
    saving.value = false
  }
}

const remove = async (item: Address) => {
  const message =
    item.defaultFlag === 1
      ? `确认删除默认地址「${fullAddress(item)}」吗？删除后需要重新设置默认地址。`
      : `确认删除地址「${fullAddress(item)}」吗？`
  try {
    await ElMessageBox.confirm(message, '删除收货地址', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  await addressApi.remove(item.id)
  ElMessage.success('地址已删除')
  await load()
}

const setDefault = async (item: Address) => {
  if (item.defaultFlag === 1) return
  await addressApi.setDefault(item.id)
  ElMessage.success('默认地址已更新')
  await load()
}

onMounted(load)
</script>

<style scoped>
.address-page {
  color: #1d2a22;
}

.address-crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 4px 0 18px;
  color: #73907a;
  font-size: 13px;
}

.address-crumb a {
  color: #168339;
  font-weight: 800;
}

.address-shell {
  overflow: hidden;
  border: 1px solid #cfe5cc;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 48px rgba(50, 100, 57, 0.08);
}

.address-hero {
  min-height: 78px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 20px 28px;
  background:
    radial-gradient(circle at 82% 24%, rgba(43, 160, 70, 0.12), transparent 28%),
    linear-gradient(90deg, #f0faed, #fbfff8);
}

.hero-title,
.stats-grid,
.toolbar-card,
.toolbar-actions,
.service-strip,
.service-strip div,
.card-actions,
.form-extras {
  display: flex;
  align-items: center;
}

.hero-title {
  gap: 14px;
}

.hero-title > .el-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border: 2px solid #1d963d;
  border-radius: 50%;
  color: #1d963d;
  font-size: 24px;
}

.hero-title h1 {
  margin: 0;
  color: #0e782b;
  font-size: 32px;
  font-weight: 900;
}

.hero-title i {
  width: 1px;
  height: 18px;
  background: #d4e2d2;
}

.hero-title p {
  margin: 0;
  color: #7a887f;
}

.address-hero :deep(.el-button) {
  min-height: 40px;
  padding: 0 24px;
  border-radius: 9px;
  font-weight: 900;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  padding: 22px 28px 10px;
}

.stats-grid article {
  min-height: 86px;
  padding: 15px 18px;
  border: 1px solid #dcebd9;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff, #f7fcf5);
}

.stats-grid span,
.stats-grid em,
.stats-grid strong {
  display: block;
}

.stats-grid span {
  color: #6a786f;
  font-size: 13px;
}

.stats-grid strong {
  margin-top: 8px;
  color: #178438;
  font-size: 28px;
  line-height: 1;
}

.stats-grid em {
  margin-top: 7px;
  overflow: hidden;
  color: #89958d;
  font-size: 12px;
  font-style: normal;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-stat {
  cursor: pointer;
}

.toolbar-card {
  justify-content: space-between;
  gap: 20px;
  margin: 10px 28px 0;
  padding: 17px 0 15px;
  border-bottom: 1px solid #e3ece2;
}

.toolbar-card h2 {
  margin: 0;
  color: #1e2b23;
  font-size: 22px;
  font-weight: 900;
}

.toolbar-card p {
  margin: 5px 0 0;
  color: #7a867f;
}

.toolbar-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 9px;
}

.toolbar-actions button,
.card-actions button,
.form-extras button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  background: transparent;
  color: #178338;
  font-weight: 800;
  cursor: pointer;
}

.toolbar-actions button {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid #d8e8d5;
  border-radius: 999px;
  background: #f4fbf2;
}

.address-body {
  min-height: 360px;
  padding: 22px 28px 0;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.address-card {
  position: relative;
  min-height: 188px;
  display: block;
  overflow: hidden;
  padding: 20px 42px 54px 20px;
  border: 1px solid #dde7dc;
  border-radius: 9px;
  color: inherit;
  text-align: left;
  background: #fff;
  box-shadow: 0 10px 24px rgba(35, 74, 44, 0.04);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.address-card:hover {
  border-color: #9bd2a4;
  box-shadow: 0 16px 36px rgba(32, 122, 52, 0.1);
  transform: translateY(-2px);
}

.address-card.active {
  border-color: #1e983f;
  box-shadow: 0 0 0 1px #1e983f inset, 0 16px 32px rgba(31, 142, 62, 0.11);
}

.default-badge,
.soft-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 5px 11px;
  border-radius: 0 8px 0 8px;
  color: #fff;
  font-size: 12px;
  font-weight: 900;
  background: #2d9845;
}

.soft-badge {
  color: #1a8038;
  background: #e7f7e4;
}

.address-card strong {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  color: #1f2a23;
  font-size: 18px;
}

.address-card em {
  color: #6f7b74;
  font-size: 14px;
  font-style: normal;
  font-weight: 500;
}

.address-card p {
  margin: 0;
  color: #5d6961;
  font-size: 14px;
  line-height: 1.75;
}

.address-card .region {
  margin-top: 14px;
  color: #39493e;
  font-weight: 800;
}

.address-card .detail {
  margin-top: 4px;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 11px;
  color: #89958d;
  font-size: 12px;
}

.meta-row button {
  border: 0;
  background: transparent;
  color: #178338;
  font-weight: 800;
  cursor: pointer;
}

.radio-dot {
  position: absolute;
  right: 15px;
  bottom: 72px;
  width: 17px;
  height: 17px;
  border: 1px solid #cfd8d0;
  border-radius: 50%;
}

.checked-corner {
  position: absolute;
  right: -1px;
  bottom: 42px;
  width: 40px;
  height: 40px;
  display: grid;
  place-items: end;
  padding: 0 6px 5px 0;
  clip-path: polygon(100% 0, 0 100%, 100% 100%);
  color: #fff;
  background: #2f9b49;
}

.card-actions {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  justify-content: flex-end;
  gap: 16px;
  min-height: 42px;
  padding: 0 18px;
  border-top: 1px solid #edf2eb;
  background: #fbfdf9;
}

.card-actions button:disabled {
  color: #a9b2ac;
  cursor: not-allowed;
}

.card-actions .danger {
  color: #d84b3f;
}

.add-address-card {
  min-height: 188px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  padding: 20px;
  border-style: dashed;
  color: #7a847e;
  text-align: center;
  cursor: pointer;
}

.add-address-card .el-icon {
  color: #1e963f;
  font-size: 30px;
}

.state-box {
  min-height: 320px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 11px;
  padding: 46px 24px;
  border: 1px dashed #d9e5d7;
  border-radius: 10px;
  color: #7a867f;
  text-align: center;
  background: #fbfdf9;
}

.state-box .el-icon {
  color: #219542;
  font-size: 42px;
}

.state-box strong {
  color: #243128;
  font-size: 20px;
}

.state-box.error .el-icon {
  color: #d97706;
}

.empty-visual {
  width: 88px;
  height: 88px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #eaf8e7;
}

.service-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
  margin: 24px 28px 28px;
  padding: 18px 22px;
  border: 1px solid #e0ecde;
  border-radius: 12px;
  background: #fbfef9;
}

.service-strip div {
  display: grid;
  grid-template-columns: 36px 1fr;
  column-gap: 10px;
}

.service-strip .el-icon {
  grid-row: span 2;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #1b963f;
  background: #ddf4dc;
}

.service-strip strong {
  color: #26332b;
  font-size: 14px;
}

.service-strip span,
.service-strip button {
  color: #7b897f;
  font-size: 12px;
}

.service-strip button {
  width: fit-content;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.dialog-form {
  padding-top: 4px;
}

.form-row {
  display: grid;
  gap: 14px;
}

.form-row.two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-row.three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.dialog-form :deep(.el-input__wrapper),
.dialog-form :deep(.el-textarea__inner) {
  border-radius: 9px;
}

.form-extras {
  justify-content: space-between;
  gap: 14px;
  padding: 10px 2px 0;
}

.address-dialog :deep(.el-dialog) {
  border-radius: 14px;
}

.address-dialog :deep(.el-dialog__header) {
  padding: 22px 24px 10px;
}

.address-dialog :deep(.el-dialog__title) {
  color: #1e2b23;
  font-weight: 900;
}

.address-dialog :deep(.el-dialog__footer) {
  padding: 8px 24px 24px;
}

@media (max-width: 1180px) {
  .address-grid,
  .stats-grid,
  .service-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .address-hero,
  .toolbar-card,
  .form-extras {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-title i {
    display: none;
  }

  .stats-grid,
  .address-grid,
  .service-strip,
  .form-row.two,
  .form-row.three {
    grid-template-columns: 1fr;
  }

  .address-body,
  .stats-grid,
  .address-hero {
    padding-right: 18px;
    padding-left: 18px;
  }

  .toolbar-card,
  .service-strip {
    margin-right: 18px;
    margin-left: 18px;
  }
}
</style>
