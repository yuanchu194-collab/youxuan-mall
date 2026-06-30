<template>
  <section class="my-coupons-page">
    <aside class="side-column">
      <section class="category-card">
        <h2>
          <el-icon><Menu /></el-icon>
          全部商品分类
        </h2>
        <RouterLink v-for="item in categories" :key="item.name" :to="item.to">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.name }}</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </RouterLink>
      </section>

      <section class="member-card">
        <h2>
          <el-icon><User /></el-icon>
          个人中心
        </h2>
        <nav aria-label="个人中心">
          <RouterLink to="/orders"><el-icon><Notebook /></el-icon>我的订单</RouterLink>
          <button type="button" @click="todo('我的收藏')"><el-icon><Star /></el-icon>我的收藏</button>
          <RouterLink to="/addresses"><el-icon><Location /></el-icon>收货地址</RouterLink>
          <RouterLink to="/my-coupons" class="active"><el-icon><Ticket /></el-icon>我的优惠券</RouterLink>
          <button type="button" @click="todo('我的余额')"><el-icon><Wallet /></el-icon>我的余额</button>
          <button type="button" @click="todo('账号设置')"><el-icon><Setting /></el-icon>账号设置</button>
          <button type="button" @click="logout"><el-icon><SwitchButton /></el-icon>退出登录</button>
        </nav>
      </section>
    </aside>

    <main class="coupon-panel">
      <header class="coupon-head">
        <div>
          <h1>我的优惠券</h1>
          <p>管理已领取的优惠券，购物结算更省钱</p>
        </div>
        <div class="head-actions">
          <button type="button" class="ghost-action" @click="openGeneralRules">优惠券使用规则</button>
          <RouterLink to="/coupons" class="claim-link">去领券</RouterLink>
        </div>
      </header>

      <section class="stats-strip" aria-label="优惠券统计">
        <div>
          <strong>{{ stats.available }}</strong>
          <span>可用优惠券</span>
        </div>
        <div>
          <strong>{{ stats.expiring }}</strong>
          <span>即将过期</span>
        </div>
        <div>
          <strong>{{ stats.used }}</strong>
          <span>已使用</span>
        </div>
        <div>
          <strong>{{ stats.expired }}</strong>
          <span>已过期</span>
        </div>
      </section>

      <nav class="coupon-tabs" aria-label="优惠券状态">
        <button v-for="tab in tabs" :key="tab.key" type="button" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          {{ tab.label }} <span>({{ tab.count }})</span>
        </button>
        <button type="button" class="tab-tool" @click="todo('优惠券搜索高级筛选')">
          <el-icon><Search /></el-icon>
          高级筛选
        </button>
      </nav>

      <section class="coupon-content">
        <div v-loading="loading" class="coupon-list-wrap">
          <div v-if="loadError" class="state-box error">
            <el-icon><Warning /></el-icon>
            <strong>优惠券加载失败</strong>
            <span>{{ loadError }}</span>
            <el-button type="primary" @click="load">重新加载</el-button>
          </div>

          <div v-else-if="filteredCoupons.length" class="coupon-list">
            <article
              v-for="(coupon, index) in filteredCoupons"
              :key="coupon.userCouponId || `${coupon.couponId}-${coupon.receiveTime}`"
              class="coupon-ticket"
              :class="[ticketTone(coupon, index), `state-${couponState(coupon)}`]"
            >
              <div class="ticket-value">
                <div class="amount"><small>¥</small>{{ money(coupon.amount) }}</div>
                <p>满{{ money(coupon.minAmount) }}元可用</p>
              </div>

              <div class="ticket-detail">
                <div class="title-row">
                  <h2>{{ coupon.couponName || '优惠券' }}</h2>
                  <span :class="['status-label', `status-${couponState(coupon)}`]">{{ stateText(coupon) }}</span>
                </div>
                <p>适用范围：{{ couponScope(coupon) }}</p>
                <p>有效期至：{{ formatDateTime(coupon.endTime) }}</p>
                <p v-if="coupon.receiveTime">领取时间：{{ formatDateTime(coupon.receiveTime) }}</p>
              </div>

              <div class="ticket-actions">
                <el-button v-if="couponState(coupon) === 'unused'" type="primary" @click="useCoupon(coupon)">去使用</el-button>
                <el-button v-else disabled>{{ stateText(coupon) }}</el-button>
                <button type="button" @click="todo('查看可用商品')">查看可用商品 <el-icon><ArrowRight /></el-icon></button>
                <button type="button" @click="openRules(coupon)">查看规则</button>
              </div>
            </article>
          </div>

          <div v-else class="state-box empty">
            <div class="empty-visual" aria-hidden="true">
              <el-icon><Ticket /></el-icon>
            </div>
            <strong>{{ coupons.length ? '当前状态暂无优惠券' : '暂无可用优惠券' }}</strong>
            <span>{{ coupons.length ? '切换其他状态查看已有优惠券' : '去领券中心看看吧' }}</span>
            <RouterLink to="/coupons">去领券</RouterLink>
          </div>
        </div>

        <aside class="right-rail">
          <section class="rail-empty">
            <div class="box-illustration" aria-hidden="true">
              <span>%</span>
            </div>
            <h2>{{ stats.available ? '省钱券包已就绪' : '暂无可用优惠券' }}</h2>
            <p>{{ stats.available ? `当前有 ${stats.available} 张优惠券可用于结算` : '去领券中心看看吧' }}</p>
            <RouterLink to="/coupons">去领券中心</RouterLink>
          </section>

          <section class="rail-tips">
            <h3>温馨提示</h3>
            <p>优惠券仅限在有效期内使用</p>
            <p>每张订单仅可使用一张优惠券</p>
            <p>部分优惠券有特定商品或品类限制</p>
            <p>退款时优惠券不予退回</p>
            <button type="button" @click="todo('订阅过期提醒')">
              <el-icon><Bell /></el-icon>
              订阅过期提醒
            </button>
            <div class="rail-actions">
              <button type="button" @click="todo('分享优惠券')">分享优惠券</button>
              <button type="button" @click="todo('优惠券转赠')">优惠券转赠</button>
            </div>
          </section>
        </aside>
      </section>

      <footer class="benefit-strip">
        <div>
          <el-icon><Ticket /></el-icon>
          <strong>领券更优惠</strong>
          <span>超值优惠券等你来领</span>
        </div>
        <div>
          <el-icon><ShoppingCart /></el-icon>
          <strong>购物更省钱</strong>
          <span>叠加优惠更划算</span>
        </div>
        <div>
          <el-icon><Tickets /></el-icon>
          <strong>专属优惠</strong>
          <span>会员专享福利</span>
        </div>
        <div>
          <el-icon><Lock /></el-icon>
          <strong>安全可靠</strong>
          <span>官方保障放心使用</span>
        </div>
      </footer>
    </main>

    <el-dialog v-model="rulesVisible" title="优惠券使用规则" width="420px">
      <div class="rule-dialog">
        <p>{{ selectedCoupon ? selectedCoupon.couponName : '我的优惠券' }}</p>
        <ul>
          <li>优惠券需在有效期内使用，过期自动失效。</li>
          <li>结算金额需满足使用门槛后才可抵扣。</li>
          <li>每笔订单仅可使用一张优惠券。</li>
          <li>如有适用范围限制，以结算页可用结果为准。</li>
        </ul>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  Bell,
  Goods,
  Location,
  Lock,
  Menu,
  Notebook,
  Search,
  Setting,
  ShoppingCart,
  Star,
  SwitchButton,
  Ticket,
  Tickets,
  User,
  Wallet,
  Warning
} from '@element-plus/icons-vue'
import { couponApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { UserCoupon } from '@/types'
import { showBackendTodo } from '@/utils/feature'

type TabKey = 'all' | 'unused' | 'used' | 'expired' | 'expiring'
type CouponState = 'unused' | 'used' | 'expired'

const router = useRouter()
const auth = useAuthStore()
const coupons = ref<UserCoupon[]>([])
const loading = ref(false)
const loadError = ref('')
const activeTab = ref<TabKey>('unused')
const rulesVisible = ref(false)
const selectedCoupon = ref<UserCoupon | null>(null)

const categories = [
  { name: '新鲜水果', to: '/products?category=fruit', icon: Goods },
  { name: '时令蔬菜', to: '/products?category=vegetable', icon: Goods },
  { name: '肉禽蛋品', to: '/products?category=meat', icon: Goods },
  { name: '乳品烘焙', to: '/products?category=dairy', icon: Goods },
  { name: '粮油调味', to: '/products?category=grain', icon: Goods },
  { name: '休闲零食', to: '/products?category=snack', icon: Goods },
  { name: '酒水饮料', to: '/products?category=drink', icon: Goods },
  { name: '日用百货', to: '/products?category=daily', icon: Goods }
]

const money = (value?: number) => {
  const numberValue = Number(value || 0)
  return Number.isInteger(numberValue) ? String(numberValue) : numberValue.toFixed(2)
}

const parseTime = (value?: string) => {
  if (!value) return 0
  return new Date(value.replace(/-/g, '/')).getTime()
}

const isTimeExpired = (coupon: UserCoupon) => {
  const end = parseTime(coupon.endTime)
  return Boolean(end && end < Date.now())
}

const couponState = (coupon: UserCoupon): CouponState => {
  if (Number(coupon.status) === 1) return 'used'
  if (Number(coupon.status) === 2 || isTimeExpired(coupon)) return 'expired'
  return 'unused'
}

const isExpiring = (coupon: UserCoupon) => {
  if (couponState(coupon) !== 'unused') return false
  const end = parseTime(coupon.endTime)
  if (!end) return false
  return end - Date.now() <= 7 * 24 * 60 * 60 * 1000
}

const matchTab = (coupon: UserCoupon, tab: TabKey) => {
  if (tab === 'all') return true
  if (tab === 'expiring') return isExpiring(coupon)
  return couponState(coupon) === tab
}

const filteredCoupons = computed(() => coupons.value.filter((coupon) => matchTab(coupon, activeTab.value)))

const stats = computed(() => ({
  available: coupons.value.filter((coupon) => couponState(coupon) === 'unused').length,
  expiring: coupons.value.filter(isExpiring).length,
  used: coupons.value.filter((coupon) => couponState(coupon) === 'used').length,
  expired: coupons.value.filter((coupon) => couponState(coupon) === 'expired').length
}))

const tabDefs: Array<{ key: TabKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'unused', label: '未使用' },
  { key: 'used', label: '已使用' },
  { key: 'expired', label: '已过期' },
  { key: 'expiring', label: '即将过期' }
]

const tabs = computed(() =>
  tabDefs.map((tab) => ({
    ...tab,
    count: coupons.value.filter((coupon) => matchTab(coupon, tab.key)).length
  }))
)

const stateText = (coupon: UserCoupon) => {
  const state = couponState(coupon)
  if (state === 'used') return '已使用'
  if (state === 'expired') return '已过期'
  return isExpiring(coupon) ? '即将过期' : '未使用'
}

const couponScope = (coupon: UserCoupon) => {
  const name = coupon.couponName || ''
  if (name.includes('水果')) return '新鲜水果'
  if (name.includes('蔬菜')) return '时令蔬菜'
  if (name.includes('肉') || name.includes('禽')) return '肉禽蛋品'
  if (name.includes('乳品') || name.includes('烘焙')) return '乳品烘焙'
  if (name.includes('粮油')) return '粮油调味'
  if (name.includes('酒水') || name.includes('饮料')) return '酒水饮料'
  if (name.includes('新人')) return '新人专享'
  return '全场商品'
}

const ticketTone = (coupon: UserCoupon, index: number) => {
  const state = couponState(coupon)
  if (state !== 'unused') return 'tone-muted'
  const scope = couponScope(coupon)
  if (scope === '全场商品' || scope.includes('水果') || scope.includes('蔬菜')) return 'tone-green'
  if (scope.includes('乳品')) return 'tone-yellow'
  return index % 2 === 0 ? 'tone-orange' : 'tone-green'
}

const formatDateTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '-')

const todo = (feature: string) => showBackendTodo(feature)

const useCoupon = (coupon: UserCoupon) => {
  if (couponState(coupon) !== 'unused') return
  router.push({ path: '/products', query: { couponId: String(coupon.couponId) } })
}

const openGeneralRules = () => {
  selectedCoupon.value = null
  rulesVisible.value = true
}

const openRules = (coupon: UserCoupon) => {
  selectedCoupon.value = coupon
  rulesVisible.value = true
}

const logout = () => {
  auth.logout()
  router.push('/')
}

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

onMounted(load)
</script>

<style scoped>
.my-coupons-page {
  display: grid;
  grid-template-columns: 250px minmax(0, 1fr);
  gap: 28px;
  color: #1b2c21;
}

.side-column {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.category-card,
.member-card,
.coupon-panel,
.coupon-ticket,
.right-rail > section,
.benefit-strip,
.state-box {
  border: 1px solid rgba(217, 229, 214, 0.92);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 36px rgba(40, 94, 50, 0.06);
}

.category-card,
.member-card {
  overflow: hidden;
  border-radius: 13px;
}

.category-card h2,
.member-card h2 {
  height: 43px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  padding: 0 18px;
  color: #148433;
  font-size: 16px;
  font-weight: 900;
  background: linear-gradient(90deg, #e9f8e7, #f7fff4);
}

.category-card a,
.member-card a,
.member-card button {
  width: 100%;
  min-height: 43px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border: 0;
  border-top: 1px solid rgba(232, 239, 230, 0.9);
  background: transparent;
  color: #26382d;
  font-size: 15px;
  text-align: left;
  cursor: pointer;
}

.category-card .el-icon,
.member-card .el-icon {
  color: #209642;
  font-size: 17px;
}

.category-card .arrow {
  margin-left: auto;
  color: #809181;
  font-size: 13px;
}

.member-card nav {
  padding: 10px;
}

.member-card a,
.member-card button {
  min-height: 40px;
  margin: 2px 0;
  border-top: 0;
  border-radius: 8px;
}

.member-card .active {
  background: #eaf7e8;
  color: #148433;
  font-weight: 900;
}

.coupon-panel {
  min-width: 0;
  padding: 24px 26px 28px;
  border-radius: 16px;
}

.coupon-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.coupon-head h1 {
  margin: 0;
  color: #101d15;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 900;
}

.coupon-head p {
  margin: 11px 0 0;
  color: #66736b;
  font-size: 15px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ghost-action {
  border: 0;
  background: transparent;
  color: #55645b;
  cursor: pointer;
}

.claim-link,
.state-box a,
.rail-empty a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  padding: 0 23px;
  border-radius: 999px;
  background: linear-gradient(180deg, #2caf49, #168332);
  color: #fff;
  font-weight: 900;
  box-shadow: 0 10px 18px rgba(25, 131, 49, 0.16);
}

.stats-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin: 20px 0 14px;
}

.stats-strip div {
  min-height: 68px;
  padding: 13px 16px;
  border: 1px solid rgba(221, 236, 218, 0.92);
  border-radius: 11px;
  background: linear-gradient(180deg, #fbfffb, #f2fbef);
}

.stats-strip strong,
.stats-strip span {
  display: block;
}

.stats-strip strong {
  color: #188737;
  font-size: 26px;
  line-height: 1;
}

.stats-strip span {
  margin-top: 7px;
  color: #68776d;
  font-size: 13px;
}

.coupon-tabs {
  min-height: 50px;
  display: flex;
  align-items: end;
  gap: 35px;
  border-bottom: 1px solid #e3ebe1;
}

.coupon-tabs button {
  position: relative;
  height: 48px;
  border: 0;
  background: transparent;
  color: #4b5d50;
  font-weight: 700;
  cursor: pointer;
}

.coupon-tabs button.active {
  color: #138633;
  font-size: 17px;
  font-weight: 900;
}

.coupon-tabs button.active::after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 3px;
  border-radius: 999px 999px 0 0;
  background: #19a03b;
  content: "";
}

.coupon-tabs .tab-tool {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #178539;
}

.coupon-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 270px;
  gap: 22px;
  margin-top: 20px;
}

.coupon-list-wrap {
  min-height: 468px;
}

.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(520px, 760px));
  justify-content: start;
  gap: 12px;
}

.coupon-ticket {
  position: relative;
  width: 100%;
  max-width: 760px;
  min-height: 118px;
  display: grid;
  grid-template-columns: 210px minmax(0, 1fr) 150px;
  overflow: hidden;
  border-radius: 11px;
}

.coupon-ticket::before,
.coupon-ticket::after {
  position: absolute;
  left: 203px;
  z-index: 2;
  width: 18px;
  height: 18px;
  border: 1px solid rgba(217, 229, 214, 0.92);
  border-radius: 999px;
  background: #fff;
  content: "";
}

.coupon-ticket::before {
  top: -10px;
}

.coupon-ticket::after {
  bottom: -10px;
}

.ticket-value {
  display: grid;
  place-items: center;
  align-content: center;
  border-right: 1px dashed rgba(198, 218, 195, 0.95);
  text-align: center;
}

.amount {
  font-size: 48px;
  line-height: 1;
  font-weight: 900;
  letter-spacing: 0;
}

.amount small {
  margin-right: 4px;
  font-size: 25px;
}

.ticket-value p {
  margin: 10px 0 0;
  font-size: 17px;
  font-weight: 900;
}

.tone-green .ticket-value {
  color: #17873a;
  background: linear-gradient(135deg, #f4fbf3, #e9f8e7);
}

.tone-orange .ticket-value {
  color: #f26a24;
  background: linear-gradient(135deg, #fff8f0, #fff0df);
}

.tone-yellow .ticket-value {
  color: #d99010;
  background: linear-gradient(135deg, #fffbed, #f7f2d7);
}

.tone-muted .ticket-value {
  color: #98a39c;
  background: linear-gradient(135deg, #f6f7f6, #eef1ef);
}

.ticket-detail {
  min-width: 0;
  display: flex;
  justify-content: center;
  flex-direction: column;
  padding: 20px 26px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ticket-detail h2 {
  min-width: 0;
  margin: 0 0 7px;
  overflow: hidden;
  color: #121d15;
  font-size: 20px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-label {
  margin-bottom: 7px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.status-unused {
  color: #168337;
  background: #e6f7e5;
}

.status-used {
  color: #6a7680;
  background: #eef1f2;
}

.status-expired {
  color: #b56b19;
  background: #fff0d9;
}

.ticket-detail p {
  margin: 5px 0;
  overflow: hidden;
  color: #606d66;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ticket-actions {
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 9px;
  padding: 18px 20px 18px 0;
}

.ticket-actions :deep(.el-button) {
  width: 116px;
  height: 38px;
  border-radius: 8px;
  font-weight: 900;
}

.ticket-actions > button {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  border: 0;
  background: transparent;
  color: #17873a;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.state-used,
.state-expired {
  filter: saturate(0.72);
}

.right-rail {
  display: grid;
  gap: 16px;
}

.right-rail > section {
  border-radius: 12px;
  background: linear-gradient(180deg, #fff, #fbfdf9);
}

.rail-empty {
  min-height: 260px;
  display: grid;
  place-items: center;
  align-content: center;
  padding: 28px 18px;
  text-align: center;
}

.box-illustration {
  width: 110px;
  height: 98px;
  position: relative;
  display: grid;
  place-items: center;
  margin-bottom: 12px;
  border-radius: 18px 18px 24px 24px;
  color: #75bf65;
  background: linear-gradient(180deg, #e7f7dd, #98d47b);
  box-shadow: inset 0 -16px 0 rgba(39, 138, 56, 0.1);
}

.box-illustration::before {
  position: absolute;
  top: -14px;
  width: 88px;
  height: 25px;
  border-radius: 8px;
  background: #dff4d4;
  content: "";
  transform: rotate(8deg);
}

.box-illustration span {
  position: relative;
  z-index: 1;
  font-size: 50px;
  font-weight: 900;
}

.rail-empty h2,
.rail-tips h3 {
  margin: 0;
  color: #243028;
  font-size: 19px;
  font-weight: 900;
}

.rail-empty p {
  margin: 9px 0 20px;
  color: #657269;
}

.rail-tips {
  padding: 22px 23px;
}

.rail-tips p {
  position: relative;
  margin: 13px 0;
  padding-left: 14px;
  color: #66736b;
  font-size: 14px;
}

.rail-tips p::before {
  position: absolute;
  top: 8px;
  left: 0;
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: #9aaa9e;
  content: "";
}

.rail-tips button {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-top: 4px;
  padding: 8px 13px;
  border: 1px solid #d7e8d6;
  border-radius: 999px;
  background: #f2faf0;
  color: #178539;
  font-weight: 800;
  cursor: pointer;
}

.rail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.rail-actions button {
  margin-top: 0;
  padding-inline: 12px;
}

.state-box {
  min-height: 360px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 11px;
  padding: 44px 22px;
  border-radius: 12px;
  color: #66736b;
  text-align: center;
}

.state-box .el-icon {
  color: #21a147;
  font-size: 54px;
}

.state-box strong {
  color: #26342a;
  font-size: 20px;
}

.state-box.error .el-icon {
  color: #d97706;
}

.empty-visual {
  width: 86px;
  height: 86px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #edf8ea;
}

.benefit-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 22px;
  padding: 15px 22px;
  border-radius: 12px;
  background: linear-gradient(90deg, #fbfffb, #f4fbf1);
}

.benefit-strip div {
  display: grid;
  grid-template-columns: 36px 1fr;
  column-gap: 10px;
  align-items: center;
}

.benefit-strip .el-icon {
  grid-row: span 2;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #1b963f;
  background: #ddf4dc;
}

.benefit-strip strong {
  color: #2b382e;
  font-size: 14px;
}

.benefit-strip span {
  color: #7b897f;
  font-size: 12px;
}

.rule-dialog p {
  margin: 0 0 12px;
  color: #142117;
  font-weight: 900;
}

.rule-dialog ul {
  margin: 0;
  padding-left: 18px;
  color: #5d6c63;
  line-height: 1.9;
}

@media (max-width: 1180px) {
  .my-coupons-page {
    grid-template-columns: 1fr;
  }

  .side-column {
    display: none;
  }

  .coupon-content {
    grid-template-columns: 1fr;
  }

  .right-rail {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .coupon-panel {
    padding: 18px;
  }

  .coupon-head,
  .head-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .stats-strip,
  .benefit-strip,
  .right-rail {
    grid-template-columns: 1fr;
  }

  .coupon-tabs {
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 12px 18px;
    padding-bottom: 10px;
  }

  .coupon-tabs .tab-tool {
    margin-left: 0;
  }

  .coupon-ticket {
    grid-template-columns: 1fr;
    max-width: 100%;
  }

  .coupon-ticket::before,
  .coupon-ticket::after {
    display: none;
  }

  .ticket-value {
    min-height: 110px;
    border-right: 0;
    border-bottom: 1px dashed rgba(198, 218, 195, 0.95);
  }

  .ticket-actions {
    justify-items: start;
    padding: 0 24px 20px;
  }
}
</style>
