<template>
  <section class="coupon-page">
    <aside class="account-panel">
      <div class="account-title">账户中心</div>
      <nav class="account-menu" aria-label="账户中心">
        <button v-for="item in accountMenus" :key="item.label" type="button" :class="{ active: item.active }" @click="item.action">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>
      <div class="side-promo">
        <strong>优选好物 新鲜到家</strong>
        <span>每日精选 产地直供 品质保障</span>
        <RouterLink to="/products">去逛逛</RouterLink>
        <div class="promo-fruits" aria-hidden="true">
          <i class="fruit fruit-orange"></i>
          <i class="fruit fruit-green"></i>
          <i class="fruit fruit-red"></i>
        </div>
      </div>
    </aside>

    <div class="coupon-main">
      <section class="coupon-hero">
        <div class="hero-copy">
          <h1>优惠券中心</h1>
          <p>领券购物更优惠，享受更多超值福利</p>
        </div>
        <div class="hero-art" aria-hidden="true">
          <div class="gift-box"></div>
          <div class="ticket ticket-large"><span>¥30</span></div>
          <div class="ticket ticket-mid"><span>¥20</span></div>
          <div class="ticket ticket-small"><span>¥10</span></div>
          <i class="leaf leaf-a"></i>
          <i class="leaf leaf-b"></i>
          <i class="confetti confetti-a"></i>
          <i class="confetti confetti-b"></i>
        </div>
      </section>

      <section class="coupon-toolbar">
        <div class="coupon-tabs" role="tablist" aria-label="优惠券状态">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            {{ tab.label }} <span>({{ tab.count }})</span>
          </button>
        </div>
        <div class="toolbar-actions">
          <button type="button" class="text-action" @click="todo('优惠券筛选高级条件')">
            <el-icon><Filter /></el-icon>
            高级筛选
          </button>
          <button type="button" class="text-action" @click="todo('订阅优惠券提醒')">
            <el-icon><Bell /></el-icon>
            订阅提醒
          </button>
          <RouterLink to="/my-coupons" class="record-link">
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

      <div v-loading="loading" class="coupon-list">
        <article
          v-for="(coupon, index) in filteredCoupons"
          :key="coupon.id"
          class="coupon-card"
          :class="[toneClass(coupon, index), `state-${couponState(coupon)}`]"
        >
          <div class="coupon-value">
            <div class="amount"><small>¥</small>{{ money(coupon.amount) }}</div>
            <p>满{{ money(coupon.minAmount) }}可用</p>
          </div>
          <div class="coupon-detail">
            <div class="coupon-title-row">
              <h3>{{ coupon.name }}</h3>
              <span class="coupon-tag">{{ couponScope(coupon) }}</span>
            </div>
            <p>有效期：{{ formatDateTime(coupon.startTime) }} - {{ formatDateTime(coupon.endTime) }}</p>
            <p v-if="isReceived(coupon) && receiveTime(coupon)">已领取：{{ formatDateTime(receiveTime(coupon) || '') }}</p>
            <p v-else>剩余：<b>{{ coupon.availableStock || 0 }}</b> 张</p>
            <div class="coupon-links">
              <button type="button" @click="todo('查看可用商品')">查看可用商品</button>
              <button type="button" @click="todo('分享优惠券')">分享</button>
              <button type="button" @click="todo('查看领取规则')">规则</button>
            </div>
          </div>
          <div class="coupon-action">
            <span class="status-badge">{{ stateText(coupon) }}</span>
            <el-button
              class="claim-button"
              :loading="receivingId === coupon.id"
              :disabled="!canAct(coupon)"
              @click="handleCouponAction(coupon)"
            >
              {{ actionText(coupon) }}
            </el-button>
          </div>
        </article>

        <div v-if="!loading && !filteredCoupons.length" class="empty-coupons">
          <el-icon><Ticket /></el-icon>
          <h3>{{ coupons.length ? '当前分类暂无优惠券' : '暂无可领取优惠券' }}</h3>
          <p>{{ coupons.length ? '换个分类看看，更多优惠正在上新' : '优惠券上新后会在这里展示' }}</p>
          <RouterLink to="/products">去逛逛</RouterLink>
        </div>
      </div>

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
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  Coin,
  Filter,
  InfoFilled,
  Location,
  Lock,
  Message,
  Notebook,
  Star,
  Ticket,
  Tickets,
  User,
  Wallet
} from '@element-plus/icons-vue'
import { couponApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { Coupon, UserCoupon } from '@/types'
import { showBackendTodo } from '@/utils/feature'

type TabKey = 'all' | 'claimable' | 'received' | 'expiring' | 'newUser' | 'fullCut' | 'category'
type CouponState = 'available' | 'received' | 'used' | 'expired' | 'soldOut' | 'disabled'

const router = useRouter()
const auth = useAuthStore()
const coupons = ref<Coupon[]>([])
const userCoupons = ref<UserCoupon[]>([])
const receivedOverrides = ref<Set<number>>(new Set())
const loading = ref(false)
const receivingId = ref<number | null>(null)
const loadError = ref('')
const activeTab = ref<TabKey>('all')
const query = reactive({ pageNum: 1, pageSize: 50 })

const money = (value?: number) => {
  const numberValue = Number(value || 0)
  return Number.isInteger(numberValue) ? String(numberValue) : numberValue.toFixed(2)
}

const todo = (feature: string) => showBackendTodo(feature)

const accountMenus = [
  { label: '个人中心', icon: User, active: false, action: () => todo('个人中心') },
  { label: '我的订单', icon: Notebook, active: false, action: () => router.push('/orders') },
  { label: '我的收藏', icon: Star, active: false, action: () => router.push('/favorites') },
  { label: '收货地址', icon: Location, active: false, action: () => router.push('/addresses') },
  { label: '优惠券', icon: Tickets, active: true, action: () => undefined },
  { label: '我的余额', icon: Wallet, active: false, action: () => todo('我的余额') },
  { label: '我的积分', icon: Coin, active: false, action: () => todo('我的积分') },
  { label: '消息中心', icon: Message, active: false, action: () => todo('消息中心') },
  { label: '安全设置', icon: Lock, active: false, action: () => todo('安全设置') }
]

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

const isReceived = (coupon: Coupon) => receivedCouponIds.value.has(Number(coupon.id))

const receiveTime = (coupon: Coupon) => receivedCouponMap.value.get(Number(coupon.id))?.receiveTime

const receivedState = (coupon: Coupon) => {
  const userCoupon = receivedCouponMap.value.get(Number(coupon.id))
  if (!userCoupon) return undefined
  if (Number(userCoupon.status) === 1) return 'used'
  if (Number(userCoupon.status) === 2) return 'expired'
  return 'received'
}

const parseTime = (value?: string) => {
  if (!value) return 0
  return new Date(value.replace(/-/g, '/')).getTime()
}

const isExpired = (coupon: Coupon) => {
  const end = parseTime(coupon.endTime)
  return Boolean(end && end < Date.now())
}

const isExpiring = (coupon: Coupon) => {
  const end = parseTime(coupon.endTime)
  if (!end || isExpired(coupon)) return false
  return end - Date.now() <= 7 * 24 * 60 * 60 * 1000
}

const couponState = (coupon: Coupon): CouponState => {
  const state = receivedState(coupon)
  if (state) return state
  if (isReceived(coupon)) return 'received'
  if (coupon.status !== 1) return 'disabled'
  if (isExpired(coupon)) return 'expired'
  if ((coupon.availableStock || 0) <= 0) return 'soldOut'
  return 'available'
}

const canReceive = (coupon: Coupon) => couponState(coupon) === 'available'
const canUse = (coupon: Coupon) => couponState(coupon) === 'received'
const canAct = (coupon: Coupon) => canReceive(coupon) || canUse(coupon)

const stateText = (coupon: Coupon) => {
  const state = couponState(coupon)
  if (state === 'available') return isExpiring(coupon) ? '即将过期' : '可领取'
  if (state === 'received') return '已领取'
  if (state === 'used') return '已使用'
  if (state === 'expired') return '已过期'
  if (state === 'soldOut') return '已抢光'
  return '已停用'
}

const actionText = (coupon: Coupon) => {
  if (canReceive(coupon)) return '立即领取'
  if (canUse(coupon)) return '去使用'
  return stateText(coupon)
}

const couponScope = (coupon: Coupon) => {
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

const isCategoryCoupon = (coupon: Coupon) => couponScope(coupon) !== '全场通用' && couponScope(coupon) !== '新人专享'
const isFullCutCoupon = (coupon: Coupon) => couponScope(coupon) === '全场通用' || (coupon.minAmount || 0) > 0

const tabDefs: Array<{ key: TabKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'claimable', label: '可领取' },
  { key: 'received', label: '已领取' },
  { key: 'expiring', label: '即将过期' },
  { key: 'newUser', label: '新人专享' },
  { key: 'fullCut', label: '满减券' },
  { key: 'category', label: '品类券' }
]

const tabs = computed(() =>
  tabDefs.map((tab) => ({
    ...tab,
    count: coupons.value.filter((coupon) => matchTab(coupon, tab.key)).length
  }))
)

const matchTab = (coupon: Coupon, tab: TabKey) => {
  if (tab === 'all') return true
  if (tab === 'claimable') return couponState(coupon) === 'available'
  if (tab === 'received') return isReceived(coupon)
  if (tab === 'expiring') return isExpiring(coupon)
  if (tab === 'newUser') return couponScope(coupon) === '新人专享'
  if (tab === 'fullCut') return isFullCutCoupon(coupon)
  return isCategoryCoupon(coupon)
}

const filteredCoupons = computed(() => coupons.value.filter((coupon) => matchTab(coupon, activeTab.value)))

const toneClass = (coupon: Coupon, index: number) => {
  if (couponState(coupon) !== 'available') return 'tone-gray'
  const scope = couponScope(coupon)
  if (scope.includes('水果') || scope.includes('蔬菜')) return 'tone-green'
  if (scope.includes('乳品')) return 'tone-blue'
  if (scope.includes('酒水')) return 'tone-teal'
  if (scope.includes('休闲') || index % 5 === 4) return 'tone-purple'
  return index % 2 === 0 ? 'tone-red' : 'tone-orange'
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

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
    const message = error instanceof Error ? error.message : '优惠券列表加载失败'
    loadError.value = message || '优惠券列表加载失败'
  } finally {
    loading.value = false
  }
}

const receive = async (coupon: Coupon) => {
  if (!auth.isLogin) {
    await router.push({ name: 'login', query: { redirect: '/coupons' } })
    return
  }
  if (!canReceive(coupon)) return
  receivingId.value = coupon.id
  try {
    await couponApi.receive(coupon.id)
    receivedOverrides.value = new Set(receivedOverrides.value).add(coupon.id)
    ElMessage.success('领取成功，可在我的优惠券查看')
    await load()
  } catch (error) {
    if (!(error instanceof Error)) ElMessage.error('领取失败，请稍后重试')
  } finally {
    receivingId.value = null
  }
}

const useCoupon = (coupon: Coupon) => {
  if (!canUse(coupon)) return
  router.push({ path: '/products', query: { couponId: String(coupon.id) } })
}

const handleCouponAction = (coupon: Coupon) => {
  if (canUse(coupon)) {
    useCoupon(coupon)
    return
  }
  receive(coupon)
}

onMounted(load)
</script>

<style scoped>
.coupon-page {
  display: grid;
  grid-template-columns: 230px minmax(0, 1fr);
  gap: 32px;
  color: #1f2d24;
}

.account-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.account-title,
.account-menu,
.side-promo,
.coupon-hero,
.coupon-card,
.coupon-tips,
.load-error,
.empty-coupons {
  border: 1px solid rgba(200, 222, 196, 0.78);
  box-shadow: 0 12px 32px rgba(49, 113, 58, 0.06);
}

.account-title {
  padding: 22px 20px;
  border-radius: 12px 12px 0 0;
  background: linear-gradient(180deg, #f5fbf1 0%, #fbfffa 100%);
  color: #13812f;
  font-size: 18px;
  font-weight: 800;
}

.account-menu {
  display: grid;
  gap: 6px;
  margin-top: -18px;
  padding: 16px 14px 18px;
  border-top: 0;
  border-radius: 0 0 12px 12px;
  background: rgba(255, 255, 255, 0.92);
}

.account-menu button {
  height: 42px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 10px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #23342a;
  font-size: 15px;
  text-align: left;
  cursor: pointer;
}

.account-menu .el-icon {
  color: #20a043;
  font-size: 18px;
}

.account-menu button.active {
  background: #edf7ea;
  color: #168336;
  font-weight: 800;
}

.side-promo {
  position: relative;
  min-height: 220px;
  overflow: hidden;
  padding: 22px 20px;
  border-radius: 12px;
  background:
    radial-gradient(circle at 86% 88%, rgba(255, 137, 48, 0.28) 0 18%, transparent 19%),
    radial-gradient(circle at 68% 74%, rgba(129, 204, 45, 0.28) 0 16%, transparent 17%),
    linear-gradient(160deg, #ecfae5 0%, #fff8dd 62%, #f7fee9 100%);
}

.side-promo strong,
.side-promo span {
  position: relative;
  z-index: 1;
  display: block;
}

.side-promo strong {
  color: #0f7a2e;
  font-size: 22px;
  line-height: 1.3;
}

.side-promo span {
  margin-top: 8px;
  color: #43634b;
  font-size: 13px;
}

.side-promo a {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  margin-top: 18px;
  padding: 8px 17px;
  border-radius: 999px;
  background: #209c3f;
  color: #fff;
  font-weight: 800;
}

.promo-fruits {
  position: absolute;
  right: 6px;
  bottom: 10px;
  width: 150px;
  height: 100px;
}

.fruit {
  position: absolute;
  display: block;
  border-radius: 999px;
  box-shadow: inset -8px -10px 0 rgba(0, 0, 0, 0.06), 0 8px 18px rgba(88, 121, 52, 0.12);
}

.fruit-orange {
  right: 8px;
  bottom: 8px;
  width: 74px;
  height: 74px;
  background: #ffa334;
}

.fruit-green {
  right: 74px;
  bottom: 35px;
  width: 48px;
  height: 48px;
  background: #9bd84a;
}

.fruit-red {
  right: 86px;
  bottom: 2px;
  width: 58px;
  height: 58px;
  background: #f45b48;
}

.coupon-main {
  min-width: 0;
}

.coupon-hero {
  position: relative;
  min-height: 128px;
  overflow: hidden;
  border-radius: 12px;
  background:
    radial-gradient(circle at 92% 12%, rgba(255, 203, 95, 0.35), transparent 20%),
    linear-gradient(100deg, #ecfae4 0%, #fbf8d9 55%, #dff7ce 100%);
}

.hero-copy {
  position: relative;
  z-index: 2;
  padding: 24px 48px;
}

.hero-copy h1 {
  margin: 0;
  color: #087327;
  font-size: 46px;
  line-height: 1.08;
  font-weight: 900;
}

.hero-copy p {
  margin: 14px 0 0;
  color: #2f7d3a;
  font-size: 18px;
}

.hero-art {
  position: absolute;
  right: 28px;
  bottom: -12px;
  width: 430px;
  height: 160px;
}

.gift-box {
  position: absolute;
  right: 70px;
  bottom: 8px;
  width: 190px;
  height: 70px;
  border-radius: 12px 12px 6px 6px;
  background: linear-gradient(180deg, #35ad4d, #168734);
  box-shadow: 0 16px 24px rgba(27, 112, 44, 0.18);
}

.gift-box::before {
  position: absolute;
  top: -14px;
  right: -14px;
  left: -14px;
  height: 30px;
  border-radius: 10px;
  background: #47bd5b;
  content: "";
}

.ticket {
  position: absolute;
  display: grid;
  place-items: center;
  border: 2px solid #ffbf76;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffe0ae, #ffc071);
  color: #f26b25;
  font-weight: 900;
  transform: rotate(-12deg);
  box-shadow: 0 8px 18px rgba(239, 121, 35, 0.16);
}

.ticket::before,
.ticket::after {
  position: absolute;
  top: 50%;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #eef8d8;
  content: "";
  transform: translateY(-50%);
}

.ticket::before {
  left: -8px;
}

.ticket::after {
  right: -8px;
}

.ticket-large {
  right: 195px;
  bottom: 42px;
  width: 108px;
  height: 78px;
  font-size: 34px;
}

.ticket-mid {
  right: 96px;
  bottom: 52px;
  width: 96px;
  height: 68px;
  font-size: 31px;
  transform: rotate(8deg);
}

.ticket-small {
  right: 24px;
  bottom: 48px;
  width: 72px;
  height: 54px;
  font-size: 23px;
  transform: rotate(12deg);
}

.leaf,
.confetti {
  position: absolute;
  display: block;
}

.leaf {
  width: 36px;
  height: 18px;
  border-radius: 100% 0 100% 0;
  background: linear-gradient(135deg, #7ecb38, #2ca83e);
}

.leaf-a {
  right: 5px;
  top: 38px;
  transform: rotate(-25deg);
}

.leaf-b {
  left: 88px;
  top: 18px;
  transform: rotate(24deg);
}

.confetti {
  width: 13px;
  height: 13px;
  border-radius: 3px;
  background: #ff6f5f;
}

.confetti-a {
  left: 46px;
  top: 42px;
  transform: rotate(28deg);
}

.confetti-b {
  right: 326px;
  top: 32px;
  background: #f7b34a;
  transform: rotate(46deg);
}

.coupon-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding: 14px 0;
}

.coupon-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.coupon-tabs button {
  height: 36px;
  padding: 0 20px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #26352b;
  font-weight: 700;
  cursor: pointer;
}

.coupon-tabs button.active {
  background: linear-gradient(180deg, #2da844, #168736);
  color: #fff;
  box-shadow: 0 8px 18px rgba(22, 135, 54, 0.22);
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #2e3b33;
}

.text-action,
.record-link,
.coupon-links button,
.load-error button {
  border: 0;
  background: transparent;
  color: #405047;
  cursor: pointer;
}

.text-action,
.record-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.record-link {
  color: #1d7c38;
  font-weight: 800;
}

.load-error {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 10px 0;
  padding: 12px 16px;
  border-radius: 10px;
  background: #fff7f3;
  color: #c6531f;
}

.load-error button {
  margin-left: auto;
  color: #168736;
  font-weight: 800;
}

.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(520px, 720px));
  justify-content: start;
  gap: 14px 18px;
  min-height: 430px;
}

.coupon-card {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr) 112px;
  width: 100%;
  max-width: 760px;
  min-height: 98px;
  overflow: hidden;
  border-color: #f1d9c7;
  border-radius: 10px;
  background: linear-gradient(90deg, #fffaf6 0%, #fff 100%);
  box-shadow: 0 10px 24px rgba(125, 78, 31, 0.04);
}

.coupon-value {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 16px 12px;
  color: #fff;
  text-align: center;
}

.coupon-value::after {
  position: absolute;
  top: -8px;
  right: -7px;
  bottom: -8px;
  width: 14px;
  background: radial-gradient(circle at 50% 8px, #fff 0 6px, transparent 7px) 0 0 / 14px 18px repeat-y;
  content: "";
}

.amount {
  font-size: 44px;
  line-height: 1;
  font-weight: 900;
}

.amount small {
  margin-right: 4px;
  font-size: 18px;
  vertical-align: 14px;
}

.coupon-value p {
  margin: 7px 0 0;
  font-size: 15px;
  font-weight: 800;
}

.coupon-detail {
  min-width: 0;
  padding: 18px 18px 12px 22px;
}

.coupon-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.coupon-title-row h3 {
  min-width: 0;
  margin: 0;
  overflow: hidden;
  color: #1d2620;
  font-size: 18px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-tag {
  flex: none;
  padding: 2px 7px;
  border: 1px solid #ffb09a;
  border-radius: 4px;
  background: #fff7f1;
  color: #ff6035;
  font-size: 12px;
  font-weight: 800;
}

.coupon-detail p {
  margin: 10px 0 0;
  color: #68756b;
  font-size: 13px;
}

.coupon-detail b {
  color: #1f2d24;
}

.coupon-links {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.coupon-links button {
  padding: 0;
  color: #75907b;
  font-size: 12px;
}

.coupon-action {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 13px;
  padding: 14px 18px 14px 0;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 58px;
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  background: #eaf8ed;
  color: #1b8a3b;
  font-size: 12px;
  font-weight: 800;
}

.claim-button {
  width: 96px;
  height: 36px;
  border: 0;
  border-radius: 8px;
  background: linear-gradient(180deg, #ff7b1f, #ff4d12);
  color: #fff;
  font-size: 15px;
  font-weight: 900;
}

.claim-button:hover,
.claim-button:focus {
  color: #fff;
  background: linear-gradient(180deg, #ff8a2e, #f04410);
}

.claim-button.is-disabled,
.claim-button.is-disabled:hover {
  background: #eeeeee;
  color: #7d8580;
}

.tone-red .coupon-value {
  background: linear-gradient(145deg, #ff9469, #ff5b4b);
}

.tone-orange .coupon-value {
  background: linear-gradient(145deg, #ffba4a, #ff8b2a);
}

.tone-green .coupon-value {
  background: linear-gradient(145deg, #99de68, #48b83a);
}

.tone-blue .coupon-value {
  background: linear-gradient(145deg, #7cc7ff, #4f9df4);
}

.tone-purple .coupon-value {
  background: linear-gradient(145deg, #bd91ff, #8a6cf2);
}

.tone-teal .coupon-value {
  background: linear-gradient(145deg, #82d8ce, #49b7aa);
}

.tone-gray .coupon-value {
  background: linear-gradient(145deg, #dadfdd, #c7cfca);
}

.state-received,
.state-used,
.state-expired,
.state-soldOut,
.state-disabled {
  border-color: #e6eae7;
  background: #fbfcfb;
}

.state-received .coupon-detail,
.state-used .coupon-detail,
.state-expired .coupon-detail,
.state-soldOut .coupon-detail,
.state-disabled .coupon-detail {
  opacity: 0.72;
}

.state-received .status-badge,
.state-used .status-badge,
.state-expired .status-badge,
.state-soldOut .status-badge,
.state-disabled .status-badge {
  background: #f1f2f1;
  color: #7d8580;
}

.empty-coupons {
  grid-column: 1 / -1;
  min-height: 350px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  border-style: dashed;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.82);
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

.empty-coupons a {
  margin-top: 8px;
  padding: 10px 24px;
  border-radius: 999px;
  background: #209c3f;
  color: #fff;
  font-weight: 800;
}

.coupon-tips {
  display: flex;
  align-items: center;
  gap: 22px;
  margin-top: 16px;
  padding: 13px 18px;
  border-radius: 10px;
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

  .account-panel {
    display: none;
  }

  .coupon-card {
    grid-template-columns: 124px minmax(0, 1fr) 104px;
  }
}

@media (max-width: 860px) {
  .coupon-hero {
    min-height: 170px;
  }

  .hero-copy {
    padding: 24px;
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .hero-art {
    right: -70px;
    opacity: 0.72;
  }

  .coupon-toolbar,
  .toolbar-actions,
  .coupon-tips {
    align-items: flex-start;
    flex-direction: column;
  }

  .coupon-list {
    grid-template-columns: 1fr;
  }

  .coupon-card {
    max-width: 100%;
  }
}

@media (max-width: 560px) {
  .coupon-card {
    grid-template-columns: 1fr;
  }

  .coupon-value::after {
    display: none;
  }

  .coupon-action {
    align-items: stretch;
    padding: 0 18px 16px;
  }

  .claim-button {
    width: 100%;
  }
}
</style>
