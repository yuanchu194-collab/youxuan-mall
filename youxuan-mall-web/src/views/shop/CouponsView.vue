<template>
  <section class="coupon-page">
    <CouponSidebar active="coupons" @todo="todo" />

    <main class="coupon-main">
      <CouponHero title="优惠券中心" subtitle="领券购物更优惠，享受更多超值福利" />

      <section class="summary-strip" aria-label="优惠券统计">
        <div>
          <strong>{{ coupons.length }}</strong>
          <span>全部优惠券</span>
        </div>
        <div>
          <strong>{{ statCount('claimable') }}</strong>
          <span>可领取</span>
        </div>
        <div>
          <strong>{{ receivedCount }}</strong>
          <span>已领取</span>
        </div>
        <div>
          <strong>{{ statCount('expiring') }}</strong>
          <span>即将过期</span>
        </div>
      </section>

      <section class="coupon-toolbar">
        <CouponTabs v-model="activeTab" :tabs="tabs" />
        <div class="toolbar-actions">
          <button type="button" @click="advancedFilter">
            <el-icon><Filter /></el-icon>
            高级筛选
          </button>
          <button type="button" @click="subscribeNotice">
            <el-icon><Bell /></el-icon>
            订阅提醒
          </button>
          <RouterLink to="/my-coupons">
            <el-icon><Tickets /></el-icon>
            领券记录
          </RouterLink>
        </div>
      </section>

      <div v-if="loadError" class="load-error">
        <el-icon><InfoFilled /></el-icon>
        <span>{{ loadError }}</span>
        <button type="button" @click="load">重新加载</button>
      </div>

      <section v-loading="loading" class="coupon-list-wrap">
        <div v-if="filteredCards.length" class="coupon-list">
          <CouponCard
            v-for="card in filteredCards"
            :key="card.id"
            :coupon="card"
            @action="handleCardAction"
            @rules="openRules"
            @share="shareCoupon"
            @view-products="viewProducts"
          />
        </div>

        <div v-else-if="!loading" class="empty-coupons">
          <el-icon><Ticket /></el-icon>
          <h3>{{ emptyTitle }}</h3>
          <p>{{ coupons.length ? '换个分类看看，更多优惠正在上新。' : '优惠券上新后会在这里展示。' }}</p>
          <div>
            <RouterLink to="/products">去逛逛</RouterLink>
            <button v-if="activeTab !== 'all'" type="button" @click="activeTab = 'all'">查看全部优惠券</button>
          </div>
        </div>
      </section>

      <section class="coupon-tips">
        <strong>
          <el-icon><InfoFilled /></el-icon>
          使用小贴士
        </strong>
        <span>每张优惠券仅限使用一次</span>
        <span>订单提交时自动抵扣</span>
        <span>部分商品不参与优惠</span>
        <span>如有疑问请联系客服</span>
      </section>
    </main>

    <CouponRuleDialog v-model="rulesVisible" :coupon="selectedCard" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Filter, InfoFilled, Ticket, Tickets } from '@element-plus/icons-vue'
import CouponCard from '@/components/coupon/CouponCard.vue'
import CouponHero from '@/components/coupon/CouponHero.vue'
import CouponRuleDialog from '@/components/coupon/CouponRuleDialog.vue'
import CouponSidebar from '@/components/coupon/CouponSidebar.vue'
import CouponTabs from '@/components/coupon/CouponTabs.vue'
import type { CouponCardItem, CouponCardState, CouponCardTone, CouponTabItem } from '@/components/coupon/types'
import { couponApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { Coupon, UserCoupon } from '@/types'
import { showBackendTodo } from '@/utils/feature'

type TabKey = 'all' | 'claimable' | 'received' | 'expiring' | 'newUser' | 'fullCut' | 'category'

const router = useRouter()
const auth = useAuthStore()
const coupons = ref<Coupon[]>([])
const userCoupons = ref<UserCoupon[]>([])
const receivedOverrides = ref<Set<number>>(new Set())
const loading = ref(false)
const receivingId = ref<number | null>(null)
const loadError = ref('')
const activeTab = ref<TabKey>('all')
const rulesVisible = ref(false)
const selectedCard = ref<CouponCardItem | null>(null)
const query = reactive({ pageNum: 1, pageSize: 50 })

const todo = (feature: string) => showBackendTodo(feature)

const parseTime = (value?: string) => {
  if (!value) return 0
  return new Date(value.replace(/-/g, '/')).getTime()
}

const isExpiredByTime = (endTime?: string) => {
  const end = parseTime(endTime)
  return Boolean(end && end < Date.now())
}

const isExpiringSoon = (endTime?: string) => {
  const end = parseTime(endTime)
  if (!end || isExpiredByTime(endTime)) return false
  return end - Date.now() <= 3 * 24 * 60 * 60 * 1000
}

const receivedCouponMap = computed(() => {
  const map = new Map<number, UserCoupon>()
  userCoupons.value.forEach((item) => map.set(Number(item.couponId), item))
  return map
})

const receivedCouponIds = computed(() => {
  const ids = new Set<number>(receivedOverrides.value)
  userCoupons.value.forEach((item) => ids.add(Number(item.couponId)))
  return ids
})

const receivedCount = computed(() => receivedCouponIds.value.size)

const couponScope = (coupon: Coupon) => {
  if (coupon.scopeType === 'CATEGORY') return '指定分类商品'
  if (coupon.scopeType === 'ALL') return '全部商品可用'
  if (coupon.scope) return coupon.scope
  const name = coupon.name || ''
  if (name.includes('新人')) return '新人专享'
  if (name.includes('水果')) return '水果专享'
  if (name.includes('蔬菜')) return '蔬菜专享'
  if (name.includes('粮油')) return '粮油调味专享'
  if (name.includes('肉') || name.includes('禽')) return '肉禽专享'
  if (name.includes('乳品') || name.includes('烘焙')) return '乳品烘焙专享'
  if (name.includes('酒水') || name.includes('饮料')) return '酒水饮料专享'
  return '全场通用'
}

const couponType = (coupon: Coupon) => {
  if (coupon.couponType) return coupon.couponType
  if (coupon.scopeType === 'CATEGORY') return '品类券'
  if (couponScope(coupon) === '新人专享') return '新人专享'
  if (couponScope(coupon) !== '全场通用') return '品类券'
  return Number(coupon.minAmount || 0) <= 0 ? '无门槛券' : '满减券'
}

const isCategoryCoupon = (coupon: Coupon) => couponType(coupon) === '品类券'
const isFullCutCoupon = (coupon: Coupon) => couponType(coupon) === '满减券' || Number(coupon.minAmount || 0) > 0

const resolveState = (coupon: Coupon): CouponCardState => {
  const userCoupon = receivedCouponMap.value.get(Number(coupon.id))
  if (userCoupon) {
    if (Number(userCoupon.status) === 1) return 'used'
    if (Number(userCoupon.status) === 2 || isExpiredByTime(userCoupon.endTime || coupon.endTime)) return 'expired'
    return isExpiringSoon(userCoupon.endTime || coupon.endTime) ? 'expiring' : 'usable'
  }
  if (receivedCouponIds.value.has(Number(coupon.id))) return isExpiringSoon(coupon.endTime) ? 'expiring' : 'usable'
  if (coupon.status !== 1) return 'disabled'
  if (isExpiredByTime(coupon.endTime)) return 'expired'
  if (Number(coupon.availableStock || 0) <= 0) return 'soldOut'
  return 'claimable'
}

const stateText = (state: CouponCardState) => {
  const map: Record<CouponCardState, string> = {
    claimable: '可领取',
    usable: '已领取',
    expiring: '即将过期',
    used: '已使用',
    expired: '已过期',
    disabled: '已停用',
    soldOut: '已抢光'
  }
  return map[state]
}

const actionText = (state: CouponCardState) => {
  if (state === 'claimable') return '立即领取'
  if (state === 'usable' || state === 'expiring') return '去使用'
  return stateText(state)
}

const toneOf = (coupon: Coupon, state: CouponCardState, index: number): CouponCardTone => {
  if (['used', 'expired', 'disabled', 'soldOut'].includes(state)) return 'gray'
  const scope = couponScope(coupon)
  if (scope.includes('水果') || scope.includes('蔬菜')) return 'green'
  if (scope.includes('乳品')) return 'blue'
  if (scope.includes('酒水')) return 'teal'
  if (scope.includes('休闲') || index % 5 === 4) return 'purple'
  return index % 2 === 0 ? 'red' : 'orange'
}

const formatDateTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '-')

const couponRules = (coupon: Coupon) => [
  Number(coupon.minAmount || 0) <= 0 ? '无使用门槛。' : `订单金额满 ${Number(coupon.minAmount).toFixed(2)} 元可用。`,
  `有效期：${formatDateTime(coupon.startTime)} - ${formatDateTime(coupon.endTime)}。`,
  `适用范围：${couponScope(coupon)}。`,
  '每笔订单仅可使用一张优惠券，不可与其他优惠券叠加。',
  '最终优惠金额与是否可用以订单确认接口返回为准。'
]

const cards = computed<CouponCardItem[]>(() =>
  coupons.value.map((coupon, index) => {
    const state = resolveState(coupon)
    const userCoupon = receivedCouponMap.value.get(Number(coupon.id))
    return {
      id: coupon.id,
      title: coupon.name || '优惠券',
      typeLabel: couponType(coupon),
      scopeLabel: couponScope(coupon),
      amount: Number(coupon.amount || 0),
      threshold: Number(coupon.minAmount || 0),
      startTime: coupon.startTime,
      endTime: coupon.endTime,
      stockText: userCoupon?.receiveTime ? `已领取：${formatDateTime(userCoupon.receiveTime)}` : `剩余：${Number(coupon.availableStock || 0)} 张`,
      receiveText: userCoupon?.receiveTime ? `已领取：${formatDateTime(userCoupon.receiveTime)}` : undefined,
      statusText: stateText(state),
      actionText: receivingId.value === coupon.id ? '领取中' : actionText(state),
      actionDisabled: !['claimable', 'usable', 'expiring'].includes(state) || receivingId.value === coupon.id,
      state,
      tone: toneOf(coupon, state, index),
      rules: couponRules(coupon)
    }
  })
)

const matchTab = (coupon: Coupon, tab: TabKey) => {
  const state = resolveState(coupon)
  if (tab === 'all') return true
  if (tab === 'claimable') return state === 'claimable'
  if (tab === 'received') return receivedCouponIds.value.has(Number(coupon.id)) || ['usable', 'expiring', 'used', 'expired'].includes(state)
  if (tab === 'expiring') return state === 'expiring' || (state === 'claimable' && isExpiringSoon(coupon.endTime))
  if (tab === 'newUser') return couponType(coupon) === '新人专享'
  if (tab === 'fullCut') return isFullCutCoupon(coupon)
  return isCategoryCoupon(coupon)
}

const filteredCards = computed(() =>
  cards.value.filter((card) => {
    const raw = coupons.value.find((coupon) => coupon.id === card.id)
    return raw ? matchTab(raw, activeTab.value) : false
  })
)

const statCount = (tab: TabKey) => coupons.value.filter((coupon) => matchTab(coupon, tab)).length

const tabDefs: Array<{ key: TabKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'claimable', label: '可领取' },
  { key: 'received', label: '已领取' },
  { key: 'expiring', label: '即将过期' },
  { key: 'newUser', label: '新人专享' },
  { key: 'fullCut', label: '满减券' },
  { key: 'category', label: '品类券' }
]

const tabs = computed<CouponTabItem[]>(() =>
  tabDefs.map((tab) => ({
    ...tab,
    count: coupons.value.filter((coupon) => matchTab(coupon, tab.key)).length
  }))
)

const emptyTitle = computed(() => {
  const current = tabDefs.find((tab) => tab.key === activeTab.value)
  if (!coupons.value.length) return '暂无可领取优惠券'
  return `暂无${current?.label || ''}优惠券`
})

const loadReceivedCoupons = async () => {
  if (!auth.isLogin) {
    userCoupons.value = []
    return
  }
  try {
    userCoupons.value = await couponApi.my()
  } catch {
    userCoupons.value = []
  }
}

const load = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const data = await couponApi.page(query)
    coupons.value = data.records || []
    await loadReceivedCoupons()
  } catch (error) {
    coupons.value = []
    loadError.value = error instanceof Error ? error.message : '优惠券列表加载失败'
  } finally {
    loading.value = false
  }
}

const receiveCoupon = async (id: number) => {
  if (!auth.isLogin) {
    await router.push({ name: 'login', query: { redirect: '/coupons' } })
    return
  }
  receivingId.value = id
  try {
    await couponApi.receive(id)
    ElMessage.success('领取成功，可在我的优惠券查看')
    receivedOverrides.value = new Set(receivedOverrides.value).add(id)
    await load()
  } finally {
    receivingId.value = null
  }
}

const useCoupon = (id: number) => {
  window.sessionStorage.setItem('youxuan_selected_coupon_id', String(id))
  router.push({ path: '/products', query: { couponId: String(id) } })
}

const handleCardAction = async (card: CouponCardItem) => {
  if (card.state === 'claimable') {
    await receiveCoupon(card.id)
    return
  }
  if (card.state === 'usable' || card.state === 'expiring') {
    useCoupon(card.id)
  }
}

const openRules = (card: CouponCardItem) => {
  selectedCard.value = card
  rulesVisible.value = true
}

const viewProducts = (card: CouponCardItem) => {
  useCoupon(card.id)
}

const shareCoupon = () => {
  ElMessage.info('分享功能待后端或分享组件支持。')
}

const subscribeNotice = () => {
  ElMessage.info('订阅提醒功能待后端支持。')
}

const advancedFilter = () => {
  showBackendTodo('优惠券高级筛选')
}

onMounted(load)
</script>

<style scoped>
.coupon-page {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 28px;
  color: #1f2d24;
}

.coupon-main {
  min-width: 0;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.summary-strip div {
  min-height: 72px;
  padding: 14px 18px;
  border: 1px solid #dcefd8;
  border-radius: 12px;
  background: linear-gradient(180deg, #fbfffb, #f2fbef);
  box-shadow: 0 10px 24px rgba(40, 94, 50, 0.04);
}

.summary-strip strong,
.summary-strip span {
  display: block;
}

.summary-strip strong {
  color: #168736;
  font-size: 28px;
  line-height: 1;
}

.summary-strip span {
  margin-top: 8px;
  color: #68776d;
  font-size: 13px;
}

.coupon-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-top: 18px;
  padding: 2px 0 16px;
  border-bottom: 1px solid #e4ece1;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-actions button,
.toolbar-actions a {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  color: #3d4f44;
  background: transparent;
  font-size: 14px;
  font-weight: 800;
  white-space: nowrap;
  cursor: pointer;
}

.toolbar-actions a {
  color: #168736;
}

.load-error {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0 0;
  padding: 12px 16px;
  border: 1px solid #f2d4bd;
  border-radius: 10px;
  background: #fff7f3;
  color: #c6531f;
}

.load-error button {
  margin-left: auto;
  border: 0;
  color: #168736;
  background: transparent;
  font-weight: 900;
  cursor: pointer;
}

.coupon-list-wrap {
  min-height: 420px;
  margin-top: 18px;
}

.coupon-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 680px));
  justify-content: start;
  gap: 16px 22px;
}

.empty-coupons {
  min-height: 360px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  padding: 40px 22px;
  border: 1px dashed #cadfc8;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.86);
  color: #647466;
  text-align: center;
}

.empty-coupons .el-icon {
  width: 66px;
  height: 66px;
  border-radius: 50%;
  background: #eef8ee;
  color: #1f9a42;
  font-size: 32px;
}

.empty-coupons h3 {
  margin: 6px 0 0;
  color: #1d2f22;
  font-size: 20px;
}

.empty-coupons p {
  margin: 0;
}

.empty-coupons div {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.empty-coupons a,
.empty-coupons button {
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  padding: 0 22px;
  border: 0;
  border-radius: 999px;
  background: #209c3f;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

.empty-coupons button {
  background: #edf7ea;
  color: #168736;
}

.coupon-tips {
  display: flex;
  align-items: center;
  gap: 22px;
  margin-top: 18px;
  padding: 14px 18px;
  border: 1px solid #dbeed9;
  border-radius: 12px;
  background: linear-gradient(90deg, #f0faec, #fbfff7);
  color: #6d7c70;
  font-size: 13px;
}

.coupon-tips strong {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #168736;
  font-size: 14px;
}

.coupon-tips span::before {
  margin-right: 18px;
  color: #a8b5aa;
  content: "·";
}

@media (max-width: 1180px) {
  .coupon-page {
    grid-template-columns: 1fr;
  }

  .coupon-page :deep(.coupon-sidebar) {
    display: none;
  }

  .coupon-list {
    grid-template-columns: minmax(0, 680px);
  }
}

@media (max-width: 760px) {
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .coupon-toolbar,
  .toolbar-actions,
  .coupon-tips {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
