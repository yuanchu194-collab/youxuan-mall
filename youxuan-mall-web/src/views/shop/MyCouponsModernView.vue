<template>
  <section class="coupon-page">
    <CouponSidebar active="my-coupons" @todo="todo" />

    <main class="coupon-main">
      <CouponHero title="我的优惠券" subtitle="管理已领取的优惠券，购物结算更省钱" />

      <section class="summary-strip" aria-label="我的优惠券统计">
        <div>
          <strong>{{ coupons.length }}</strong>
          <span>全部优惠券</span>
        </div>
        <div>
          <strong>{{ statCount('unused') }}</strong>
          <span>未使用</span>
        </div>
        <div>
          <strong>{{ statCount('expiring') }}</strong>
          <span>即将过期</span>
        </div>
        <div>
          <strong>{{ statCount('expired') }}</strong>
          <span>已过期</span>
        </div>
      </section>

      <section class="coupon-toolbar">
        <CouponTabs v-model="activeTab" :tabs="tabs" />
        <div class="toolbar-actions">
          <button type="button" @click="openGeneralRules">优惠券使用规则</button>
          <button type="button" @click="subscribeNotice">
            <el-icon><Bell /></el-icon>
            订阅提醒
          </button>
          <RouterLink to="/coupons">
            <el-icon><Tickets /></el-icon>
            去领券
          </RouterLink>
        </div>
      </section>

      <div v-if="loadError" class="load-error">
        <el-icon><Warning /></el-icon>
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
          <p>{{ coupons.length ? '切换其他状态查看已有优惠券。' : '去领券中心看看吧。' }}</p>
          <div>
            <RouterLink to="/coupons">去领券</RouterLink>
            <button v-if="activeTab !== 'all'" type="button" @click="activeTab = 'all'">查看全部优惠券</button>
          </div>
        </div>
      </section>

      <section class="coupon-tips">
        <strong>
          <el-icon><Ticket /></el-icon>
          温馨提示
        </strong>
        <span>优惠券仅限在有效期内使用</span>
        <span>每笔订单仅可使用一张优惠券</span>
        <span>适用范围以结算页可用结果为准</span>
      </section>
    </main>

    <CouponRuleDialog v-model="rulesVisible" :coupon="selectedCard" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Ticket, Tickets, Warning } from '@element-plus/icons-vue'
import CouponCard from '@/components/coupon/CouponCard.vue'
import CouponHero from '@/components/coupon/CouponHero.vue'
import CouponRuleDialog from '@/components/coupon/CouponRuleDialog.vue'
import CouponSidebar from '@/components/coupon/CouponSidebar.vue'
import CouponTabs from '@/components/coupon/CouponTabs.vue'
import type { CouponCardItem, CouponCardState, CouponCardTone, CouponTabItem } from '@/components/coupon/types'
import { couponApi } from '@/api/modules'
import type { UserCoupon } from '@/types'
import { showBackendTodo } from '@/utils/feature'

type TabKey = 'all' | 'unused' | 'expiring' | 'used' | 'expired'

const router = useRouter()
const coupons = ref<UserCoupon[]>([])
const loading = ref(false)
const loadError = ref('')
const activeTab = ref<TabKey>('unused')
const rulesVisible = ref(false)
const selectedCard = ref<CouponCardItem | null>(null)

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

const couponScope = (coupon: UserCoupon) => {
  const name = coupon.couponName || ''
  if (name.includes('新人')) return '新人专享'
  if (name.includes('水果')) return '水果专享'
  if (name.includes('蔬菜')) return '蔬菜专享'
  if (name.includes('粮油')) return '粮油调味专享'
  if (name.includes('肉') || name.includes('禽')) return '肉禽专享'
  if (name.includes('乳品') || name.includes('烘焙')) return '乳品烘焙专享'
  if (name.includes('酒水') || name.includes('饮料')) return '酒水饮料专享'
  return '全场通用'
}

const couponType = (coupon: UserCoupon) => {
  const scope = couponScope(coupon)
  if (scope === '新人专享') return '新人专享'
  if (scope !== '全场通用') return '品类券'
  return Number(coupon.minAmount || 0) <= 0 ? '无门槛券' : '满减券'
}

const resolveState = (coupon: UserCoupon): CouponCardState => {
  if (Number(coupon.status) === 1) return 'used'
  if (Number(coupon.status) === 2 || isExpiredByTime(coupon.endTime)) return 'expired'
  return isExpiringSoon(coupon.endTime) ? 'expiring' : 'usable'
}

const stateText = (state: CouponCardState) => {
  if (state === 'used') return '已使用'
  if (state === 'expired') return '已过期'
  if (state === 'expiring') return '即将过期'
  return '未使用'
}

const toneOf = (coupon: UserCoupon, state: CouponCardState, index: number): CouponCardTone => {
  if (state === 'used' || state === 'expired') return 'gray'
  const scope = couponScope(coupon)
  if (scope.includes('水果') || scope.includes('蔬菜')) return 'green'
  if (scope.includes('乳品')) return 'blue'
  if (scope.includes('酒水')) return 'teal'
  return index % 2 === 0 ? 'orange' : 'green'
}

const formatDateTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '-')

const couponRules = (coupon: UserCoupon) => [
  Number(coupon.minAmount || 0) <= 0 ? '无使用门槛。' : `订单金额满 ${Number(coupon.minAmount).toFixed(2)} 元可用。`,
  `有效期至：${formatDateTime(coupon.endTime)}。`,
  `适用范围：${couponScope(coupon)}。`,
  '每笔订单仅可使用一张优惠券，不可与其他优惠券叠加。',
  '最终优惠金额与是否可用以订单确认接口返回为准。'
]

const cards = computed<CouponCardItem[]>(() =>
  coupons.value.map((coupon, index) => {
    const state = resolveState(coupon)
    return {
      id: Number(coupon.couponId),
      title: coupon.couponName || '优惠券',
      typeLabel: couponType(coupon),
      scopeLabel: couponScope(coupon),
      amount: Number(coupon.amount || 0),
      threshold: Number(coupon.minAmount || 0),
      startTime: coupon.startTime,
      endTime: coupon.endTime,
      receiveText: coupon.receiveTime ? `已领取：${formatDateTime(coupon.receiveTime)}` : undefined,
      stockText: coupon.useTime ? `已使用：${formatDateTime(coupon.useTime)}` : coupon.receiveTime ? `已领取：${formatDateTime(coupon.receiveTime)}` : undefined,
      statusText: stateText(state),
      actionText: state === 'usable' || state === 'expiring' ? '去使用' : stateText(state),
      actionDisabled: !(state === 'usable' || state === 'expiring'),
      state,
      tone: toneOf(coupon, state, index),
      rules: couponRules(coupon)
    }
  })
)

const matchTab = (coupon: UserCoupon, tab: TabKey) => {
  const state = resolveState(coupon)
  if (tab === 'all') return true
  if (tab === 'unused') return state === 'usable'
  if (tab === 'expiring') return state === 'expiring'
  return state === tab
}

const filteredCards = computed(() =>
  cards.value.filter((card) => {
    const raw = coupons.value.find((coupon) => Number(coupon.couponId) === card.id)
    return raw ? matchTab(raw, activeTab.value) : false
  })
)

const statCount = (tab: TabKey) => coupons.value.filter((coupon) => matchTab(coupon, tab)).length

const tabDefs: Array<{ key: TabKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'unused', label: '未使用' },
  { key: 'expiring', label: '即将过期' },
  { key: 'used', label: '已使用' },
  { key: 'expired', label: '已过期' }
]

const tabs = computed<CouponTabItem[]>(() =>
  tabDefs.map((tab) => ({
    ...tab,
    count: coupons.value.filter((coupon) => matchTab(coupon, tab.key)).length
  }))
)

const emptyTitle = computed(() => {
  const current = tabDefs.find((tab) => tab.key === activeTab.value)
  if (!coupons.value.length) return '暂无可用优惠券'
  return `暂无${current?.label || ''}优惠券`
})

const load = async () => {
  loading.value = true
  loadError.value = ''
  try {
    coupons.value = await couponApi.my()
  } catch (error) {
    coupons.value = []
    loadError.value = error instanceof Error ? error.message : '我的优惠券加载失败'
  } finally {
    loading.value = false
  }
}

const useCoupon = (id: number) => {
  window.sessionStorage.setItem('youxuan_selected_coupon_id', String(id))
  router.push({ path: '/products', query: { couponId: String(id) } })
}

const handleCardAction = (card: CouponCardItem) => {
  if (card.state === 'usable' || card.state === 'expiring') useCoupon(card.id)
}

const openRules = (card: CouponCardItem) => {
  selectedCard.value = card
  rulesVisible.value = true
}

const openGeneralRules = () => {
  selectedCard.value = {
    id: 0,
    title: '我的优惠券',
    typeLabel: '使用规则',
    scopeLabel: '全部优惠券',
    amount: 0,
    threshold: 0,
    statusText: '规则',
    actionText: '规则',
    actionDisabled: true,
    state: 'disabled',
    tone: 'gray',
    rules: [
      '优惠券需在有效期内使用，过期自动失效。',
      '结算金额需满足使用门槛后才可抵扣。',
      '每笔订单仅可使用一张优惠券。',
      '如有适用范围限制，以结算页可用结果为准。'
    ]
  }
  rulesVisible.value = true
}

const viewProducts = () => {
  ElMessage.info('当前需要后端提供可用商品接口。')
}

const shareCoupon = () => {
  ElMessage.info('分享功能待后端或分享组件支持。')
}

const subscribeNotice = () => {
  ElMessage.info('订阅提醒功能待后端支持。')
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
