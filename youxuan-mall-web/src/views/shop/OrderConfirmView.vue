<template>
  <section class="checkout-page">
    <nav class="checkout-crumb">
      <RouterLink to="/">首页</RouterLink>
      <el-icon><ArrowRight /></el-icon>
      <span>确认订单</span>
    </nav>

    <section class="checkout-shell">
      <header class="checkout-hero">
        <div class="hero-title">
          <el-icon><Checked /></el-icon>
          <h1>确认订单</h1>
          <i></i>
          <p>请核对订单信息，提交后我们将尽快为您发货</p>
        </div>
        <div class="hero-promises">
          <span><el-icon><CircleCheck /></el-icon>100%正品保障</span>
          <span><el-icon><Van /></el-icon>全场冷链配送</span>
          <span><el-icon><RefreshLeft /></el-icon>售后无忧</span>
        </div>
      </header>

      <section v-loading="loading" class="checkout-body">
        <main class="checkout-main">
          <section class="checkout-section address-section">
            <div class="section-heading">
              <div>
                <el-icon><Location /></el-icon>
                <h2>收货地址</h2>
              </div>
              <RouterLink to="/addresses">管理收货地址</RouterLink>
            </div>

            <div v-if="addresses.length" class="address-grid">
              <button
                v-for="address in addresses"
                :key="address.id"
                type="button"
                class="address-card"
                :class="{ active: address.id === selectedAddressId }"
                @click="selectAddress(address)"
              >
                <span v-if="address.defaultFlag === 1" class="default-badge">默认地址</span>
                <strong>
                  {{ address.receiverName }}
                  <em>{{ maskPhone(address.receiverPhone) }}</em>
                </strong>
                <p>{{ fullAddress(address) }}</p>
                <i class="radio-dot"></i>
                <el-icon v-if="address.id === selectedAddressId" class="checked-corner"><Check /></el-icon>
              </button>

              <RouterLink to="/addresses" class="address-card add-address-card">
                <el-icon><CirclePlus /></el-icon>
                <span>新增收货地址</span>
              </RouterLink>
            </div>

            <div v-else-if="!loading" class="empty-address">
              <el-icon><Location /></el-icon>
              <strong>暂无收货地址</strong>
              <span>请先新增收货地址，提交订单必须传入 addressId。</span>
              <RouterLink to="/addresses">新增收货地址</RouterLink>
            </div>
          </section>

          <section class="checkout-section delivery-section">
            <div class="section-heading">
              <div>
                <el-icon><Van /></el-icon>
                <h2>配送方式</h2>
              </div>
              <span>当前仅展示入口</span>
            </div>
            <div class="option-grid three">
              <button v-for="item in deliveryOptions" :key="item.title" type="button" @click="todo('配送方式')">
                <el-icon><component :is="item.icon" /></el-icon>
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </button>
            </div>
          </section>

          <section class="checkout-section goods-section">
            <div class="section-heading">
              <div>
                <el-icon><ShoppingBag /></el-icon>
                <h2>商品清单</h2>
              </div>
              <span>共 {{ confirmItems.length }} 件商品</span>
            </div>

            <div v-if="loadError" class="empty-goods error-state">
              <strong>订单信息加载失败</strong>
              <span>{{ loadError }}</span>
              <el-button type="primary" @click="bootstrap">重新加载</el-button>
            </div>

            <div v-else-if="confirmItems.length" class="goods-table">
              <div class="goods-head">
                <span>商品信息</span>
                <span>单价</span>
                <span>数量</span>
                <span>小计</span>
              </div>
              <article v-for="item in confirmItems" :key="item.productId" class="goods-row">
                <div class="goods-info">
                  <img :src="getImageUrl(displayConfirmProduct(item))" :alt="displayConfirmProduct(item).name" @error="setImageFallback" />
                  <div>
                    <h3>{{ displayConfirmProduct(item).name }}</h3>
                    <p>{{ productSpec(item) }}</p>
                    <span>冷链</span>
                  </div>
                </div>
                <strong class="goods-price">¥{{ money(item.price) }}</strong>
                <span class="goods-qty">{{ item.quantity }}</span>
                <strong class="goods-subtotal">¥{{ money(item.totalAmount) }}</strong>
              </article>
              <footer class="goods-count">共 {{ confirmItems.length }} 件商品</footer>
            </div>

            <div v-else-if="!loading" class="empty-goods">
              <el-icon><ShoppingBag /></el-icon>
              <strong>暂无可结算商品</strong>
              <span>请先在购物车勾选商品，或从商品详情页立即购买。</span>
              <RouterLink to="/cart">返回购物车</RouterLink>
            </div>
          </section>

          <section class="checkout-section pay-section">
            <div class="section-heading">
              <div>
                <el-icon><Wallet /></el-icon>
                <h2>支付方式</h2>
              </div>
              <span>提交后进入我的订单进行模拟支付</span>
            </div>
            <div class="option-grid four">
              <button type="button" class="active">
                <el-icon><Wallet /></el-icon>
                <strong>模拟支付</strong>
                <span>当前项目支持</span>
              </button>
              <button type="button" @click="todo('余额支付')">
                <el-icon><CreditCard /></el-icon>
                <strong>余额支付</strong>
                <span>待后端接入</span>
              </button>
              <button type="button" @click="todo('微信支付')">
                <el-icon><ChatDotRound /></el-icon>
                <strong>微信支付</strong>
                <span>待后端接入</span>
              </button>
              <button type="button" @click="todo('支付宝支付')">
                <el-icon><Money /></el-icon>
                <strong>支付宝</strong>
                <span>待后端接入</span>
              </button>
            </div>
          </section>
        </main>

        <aside class="checkout-side">
          <section class="side-card coupon-card">
            <div class="side-title">
              <el-icon><Ticket /></el-icon>
              <h2>优惠券</h2>
            </div>
            <button v-if="selectedCoupon" type="button" class="coupon-choice active" @click="clearCoupon">
              <span>
                <strong>{{ selectedCoupon.name }}</strong>
                <em>满{{ money(selectedCoupon.minAmount) }}元可用</em>
              </span>
              <b>- ¥{{ money(selectedCoupon.amount) }}</b>
              <el-icon><CircleCheckFilled /></el-icon>
            </button>
            <button
              v-else-if="firstCoupon"
              type="button"
              class="coupon-choice"
              @click="selectCoupon(firstCoupon.id)"
            >
              <span>
                <strong>{{ firstCoupon.name }}</strong>
                <em>满{{ money(firstCoupon.minAmount) }}元可用</em>
              </span>
              <b>- ¥{{ money(firstCoupon.amount) }}</b>
            </button>
            <button v-else type="button" class="coupon-choice disabled" @click="todo('优惠券选择')">
              <span>
                <strong>暂无可用优惠券</strong>
                <em>可用券以订单确认接口返回为准</em>
              </span>
              <b>0张可用</b>
            </button>
            <button type="button" class="more-line" @click="todo('优惠券选择')">
              更多优惠券 <span>{{ availableCoupons.length }}张可用</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </section>

          <section class="side-card invoice-card">
            <div class="side-title">
              <el-icon><Document /></el-icon>
              <h2>发票信息</h2>
            </div>
            <div class="invoice-tabs">
              <button type="button" class="active" @click="todo('发票')">不开票</button>
              <button type="button" @click="todo('个人发票')">个人发票</button>
              <button type="button" @click="todo('企业发票')">企业发票</button>
            </div>
          </section>

          <section class="side-card remark-card">
            <div class="side-title">
              <el-icon><Memo /></el-icon>
              <h2>订单备注</h2>
            </div>
            <el-input
              v-model="remark"
              type="textarea"
              maxlength="100"
              show-word-limit
              :rows="3"
              placeholder="选填：给商家留言（如：放前台、请尽快配送等）"
            />
          </section>

          <section class="side-card amount-card">
            <p><span>商品总额</span><b>¥{{ money(totalAmount) }}</b></p>
            <p class="discount"><span>优惠金额</span><b>- ¥{{ money(discountAmount) }}</b></p>
            <p class="discount"><span>优惠券</span><b>- ¥{{ money(couponAmount) }}</b></p>
            <p><button type="button" @click="todo('订单运费险')">配送费用 <i>?</i></button><b>¥{{ money(freightAmount) }}</b></p>
            <div class="pay-amount">
              <span>实付金额</span>
              <strong>¥{{ money(payAmount) }}</strong>
            </div>
          </section>
        </aside>
      </section>

      <footer class="checkout-bottom">
        <div class="service-strip">
          <div v-for="item in serviceItems" :key="item.title">
            <el-icon><component :is="item.icon" /></el-icon>
            <strong>{{ item.title }}</strong>
            <span>{{ item.desc }}</span>
          </div>
        </div>
        <div class="submit-bar">
          <span>实付金额：</span>
          <strong>¥{{ money(payAmount) }}</strong>
          <el-button type="primary" size="large" :disabled="submitDisabled" :loading="submitting" @click="submit">
            提交订单
          </el-button>
          <p>
            <el-icon><Lock /></el-icon>
            提交即表示您已阅读并同意《用户协议》和《隐私政策》
          </p>
        </div>
      </footer>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Bicycle,
  ChatDotRound,
  Check,
  Checked,
  CircleCheck,
  CircleCheckFilled,
  CirclePlus,
  CreditCard,
  Document,
  Goods,
  Location,
  Lock,
  Memo,
  Money,
  RefreshLeft,
  School,
  ShoppingBag,
  Ticket,
  Van,
  Wallet
} from '@element-plus/icons-vue'
import { addressApi, cartApi, orderApi } from '@/api/modules'
import type { Address, OrderConfirm, OrderConfirmItem } from '@/types'
import { showBackendTodo } from '@/utils/feature'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProduct } from '@/utils/product'

const route = useRoute()
const router = useRouter()
const confirm = ref<OrderConfirm>()
const addresses = ref<Address[]>([])
const selectedAddressId = ref<number>()
const couponId = ref<number>()
const requestItems = ref<Array<{ productId: number; quantity: number }>>([])
const loading = ref(false)
const submitting = ref(false)
const loadError = ref('')
const remark = ref('')
const source = computed(() => String(route.query.source || (route.query.productId ? 'BUY_NOW' : 'CART')))

const serviceItems = [
  { title: '安心购物', desc: '品质保障 售后无忧', icon: Checked },
  { title: '准时达', desc: '今日下单 明日送达', icon: Van },
  { title: '冷链配送', desc: '全程冷链 新鲜到家', icon: Goods },
  { title: '售后保障', desc: '7天无理由退换', icon: RefreshLeft }
]

const deliveryOptions = [
  { title: '普通配送', desc: '优选配送到家', icon: Van },
  { title: '极速配送', desc: '预约更快送达', icon: Bicycle },
  { title: '到店自提', desc: '附近门店自取', icon: School }
]

const confirmItems = computed(() => confirm.value?.items || [])
const availableCoupons = computed(() => confirm.value?.availableCoupons || [])
const selectedCoupon = computed(() => confirm.value?.selectedCoupon)
const firstCoupon = computed(() => availableCoupons.value.find((coupon) => coupon.id !== couponId.value))
const totalAmount = computed(() => confirm.value?.totalAmount ?? confirmItems.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0))
const discountAmount = computed(() => Number(confirm.value?.discountAmount || 0))
const couponAmount = computed(() => Number(selectedCoupon.value?.amount || discountAmount.value || 0))
const freightAmount = computed(() => 0)
const payAmount = computed(() => Math.max(Number(confirm.value?.payAmount ?? totalAmount.value - discountAmount.value + freightAmount.value), 0))
const submitDisabled = computed(() => !requestItems.value.length || !selectedAddressId.value || !confirmItems.value.length)

const money = (value?: number) => Number(value || 0).toFixed(2)
const displayConfirmProduct = (item: OrderConfirmItem) => normalizeProduct(item)
const productSpec = (item: OrderConfirmItem) => {
  const product = displayConfirmProduct(item)
  return product.subtitle || `规格：标准装｜库存 ${product.stock ?? '以订单确认为准'}`
}
const fullAddress = (address: Address) => `${address.province}${address.city}${address.district}${address.detailAddress}`
const maskPhone = (phone = '') => phone.replace(/^(\d{3})\d{4}(\d+)/, '$1 **** $2')
const todo = (feature: string) => showBackendTodo(feature)

const routeCouponId = () => {
  const raw = route.query.couponId
  const value = Array.isArray(raw) ? raw[0] : raw || window.sessionStorage.getItem('youxuan_selected_coupon_id') || ''
  const numberValue = Number(value)
  return value && Number.isFinite(numberValue) ? numberValue : undefined
}

const selectAddress = async (address: Address) => {
  if (selectedAddressId.value === address.id) return
  selectedAddressId.value = address.id
  await loadConfirm()
}

const selectCoupon = async (id: number) => {
  couponId.value = id
  window.sessionStorage.setItem('youxuan_selected_coupon_id', String(id))
  await loadConfirm()
}

const clearCoupon = async () => {
  couponId.value = undefined
  window.sessionStorage.removeItem('youxuan_selected_coupon_id')
  await loadConfirm()
}

const buildItems = async () => {
  if (route.query.productId) {
    requestItems.value = [{ productId: Number(route.query.productId), quantity: Number(route.query.quantity || 1) }]
    return
  }
  const cartItems = await cartApi.list()
  requestItems.value = cartItems
    .filter((item) => item.checked === 1)
    .map((item) => ({ productId: item.productId, quantity: item.quantity }))
}

const loadAddresses = async () => {
  const list = await addressApi.list()
  addresses.value = list || []
  const defaultAddress = addresses.value.find((item) => item.defaultFlag === 1)
  selectedAddressId.value = defaultAddress?.id || addresses.value[0]?.id

  try {
    const backendDefault = await addressApi.default()
    if (backendDefault?.id) {
      selectedAddressId.value = backendDefault.id
      if (!addresses.value.some((item) => item.id === backendDefault.id)) {
        addresses.value.unshift(backendDefault)
      }
    }
  } catch {
    // 地址列表已经可用时，不因默认地址接口失败阻断确认页。
  }
}

const loadConfirm = async () => {
  if (!requestItems.value.length || !selectedAddressId.value) {
    confirm.value = undefined
    return
  }
  confirm.value = await orderApi.confirm({
    source: source.value,
    addressId: selectedAddressId.value,
    couponId: couponId.value,
    items: requestItems.value
  })
}

const bootstrap = async () => {
  loading.value = true
  loadError.value = ''
  try {
    couponId.value = routeCouponId()
    await buildItems()
    await loadAddresses()
    await loadConfirm()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '请稍后重试'
    confirm.value = undefined
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请先选择或新增收货地址')
    return
  }
  if (!requestItems.value.length) {
    ElMessage.warning('没有可提交的订单商品')
    return
  }
  submitting.value = true
  try {
    await orderApi.create({
      source: source.value,
      addressId: selectedAddressId.value,
      couponId: couponId.value,
      items: requestItems.value
    })
    window.sessionStorage.removeItem('youxuan_selected_coupon_id')
    ElMessage.success('订单提交成功，请在我的订单中完成模拟支付')
    await router.push('/orders')
  } finally {
    submitting.value = false
  }
}

onMounted(bootstrap)
</script>

<style scoped>
.checkout-page {
  width: 100%;
}

.checkout-crumb {
  height: 46px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #7f8b83;
  font-size: 14px;
}

.checkout-crumb a {
  color: #1f8d3e;
  font-weight: 700;
}

.checkout-shell {
  overflow: hidden;
  border: 1px solid #cfe5cc;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 42px rgba(35, 100, 49, 0.06);
}

.checkout-hero {
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 0 24px;
  background:
    linear-gradient(90deg, rgba(236, 250, 235, 0.98), rgba(250, 255, 247, 0.92)),
    radial-gradient(circle at 46% 34%, rgba(22, 163, 74, 0.12), transparent 22rem);
}

.hero-title,
.hero-promises,
.section-heading,
.section-heading > div,
.side-title,
.submit-bar {
  display: flex;
  align-items: center;
}

.hero-title {
  gap: 14px;
}

.hero-title > .el-icon {
  color: #218c3e;
  font-size: 36px;
}

.hero-title h1 {
  margin: 0;
  color: #138032;
  font-size: 30px;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-title i {
  width: 1px;
  height: 20px;
  background: #dce9d9;
}

.hero-title p,
.hero-promises span {
  color: #7a877f;
  font-size: 14px;
}

.hero-promises {
  gap: 30px;
}

.hero-promises span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.hero-promises .el-icon {
  color: #219542;
}

.checkout-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 334px;
  gap: 20px;
  padding: 24px 22px 16px;
}

.checkout-main,
.checkout-side {
  display: grid;
  gap: 16px;
  align-content: start;
}

.checkout-section,
.side-card {
  border: 1px solid #e2ebe0;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.96);
}

.checkout-section {
  padding: 18px 18px 20px;
}

.section-heading {
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.section-heading > div,
.side-title {
  gap: 9px;
}

.section-heading .el-icon,
.side-title .el-icon {
  color: #259743;
  font-size: 21px;
}

.section-heading h2,
.side-title h2 {
  margin: 0;
  color: #1d2922;
  font-size: 20px;
  font-weight: 900;
}

.section-heading a,
.section-heading > span {
  color: #7c8981;
  font-size: 14px;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(190px, 1fr);
  gap: 18px;
}

.address-card {
  position: relative;
  min-height: 104px;
  display: block;
  padding: 19px 38px 16px 18px;
  overflow: hidden;
  border: 1px solid #dde7dc;
  border-radius: 8px;
  color: inherit;
  text-align: left;
  background: #fff;
  cursor: pointer;
}

.address-card.active {
  border-color: #1e983f;
  box-shadow: 0 0 0 1px #1e983f inset;
}

.address-card strong {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  color: #1f2a23;
  font-size: 16px;
}

.address-card em {
  color: #6f7b74;
  font-style: normal;
  font-weight: 500;
}

.address-card p {
  margin: 12px 0 0;
  color: #5d6961;
  font-size: 14px;
  line-height: 1.8;
}

.default-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 5px 10px;
  border-radius: 0 8px 0 8px;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  background: #2d9845;
}

.radio-dot {
  position: absolute;
  right: 13px;
  bottom: 17px;
  width: 16px;
  height: 16px;
  border: 1px solid #cfd8d0;
  border-radius: 50%;
}

.checked-corner {
  position: absolute;
  right: -1px;
  bottom: -1px;
  width: 38px;
  height: 38px;
  display: grid;
  place-items: end;
  padding: 0 6px 5px 0;
  clip-path: polygon(100% 0, 0 100%, 100% 100%);
  color: #fff;
  background: #2f9b49;
}

.add-address-card {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  border-style: dashed;
  color: #7a847e;
  text-align: center;
}

.add-address-card .el-icon {
  font-size: 22px;
}

.empty-address,
.empty-goods {
  min-height: 138px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  border: 1px dashed #d9e5d7;
  border-radius: 10px;
  color: #7a867f;
  background: #fbfdf9;
}

.empty-address .el-icon,
.empty-goods .el-icon {
  color: #219542;
  font-size: 32px;
}

.empty-address strong,
.empty-goods strong {
  color: #243128;
  font-size: 18px;
}

.empty-address a,
.empty-goods a {
  color: #178437;
  font-weight: 800;
}

.option-grid {
  display: grid;
  gap: 12px;
}

.option-grid.three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.option-grid.four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.option-grid button {
  min-height: 78px;
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr);
  column-gap: 10px;
  align-items: center;
  padding: 12px 14px;
  border: 1px solid #dce8da;
  border-radius: 9px;
  text-align: left;
  background: #fff;
  cursor: pointer;
}

.option-grid button.active {
  border-color: #279445;
  background: #f5fff4;
}

.option-grid .el-icon {
  grid-row: span 2;
  color: #259743;
  font-size: 25px;
}

.option-grid strong {
  color: #1e2a23;
  font-size: 15px;
}

.option-grid span {
  color: #7c8982;
  font-size: 13px;
}

.goods-table {
  overflow: hidden;
  border: 1px solid #e0e7df;
  border-radius: 9px;
}

.goods-head,
.goods-row {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) 126px 118px 126px;
  align-items: center;
}

.goods-head {
  min-height: 38px;
  padding: 0 18px;
  color: #5f6963;
  font-size: 14px;
  background: #fbfcfa;
}

.goods-row {
  min-height: 94px;
  padding: 12px 18px;
  border-top: 1px solid #edf1ec;
}

.goods-info {
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.goods-info img {
  width: 78px;
  height: 62px;
  display: block;
  object-fit: contain;
  border-radius: 8px;
  background: #f8f8f5;
}

.goods-info h3 {
  margin: 0;
  overflow: hidden;
  color: #1d2922;
  font-size: 16px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-info p {
  margin: 8px 0 0;
  overflow: hidden;
  color: #7c8780;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-info span {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  margin-top: 7px;
  padding: 0 8px;
  border: 1px solid #bddfc0;
  border-radius: 5px;
  color: #23823c;
  font-size: 12px;
  background: #f1fbef;
}

.goods-price,
.goods-qty,
.goods-subtotal {
  justify-self: center;
}

.goods-price {
  color: #ff2f1f;
}

.goods-qty {
  color: #2c3730;
}

.goods-subtotal {
  color: #ff2f1f;
  font-size: 16px;
}

.goods-count {
  padding: 16px 18px 0;
  border-top: 1px solid #edf1ec;
  color: #313c35;
  font-size: 16px;
}

.error-state {
  border-color: #ffd7c2;
  color: #c05621;
  background: #fff8f4;
}

.checkout-side {
  position: sticky;
  top: 88px;
}

.side-card {
  padding: 14px;
}

.side-title {
  margin-bottom: 12px;
}

.side-title h2 {
  font-size: 18px;
}

.coupon-choice,
.more-line {
  width: 100%;
  border-radius: 7px;
  background: #fff;
  cursor: pointer;
}

.coupon-choice {
  min-height: 56px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto 20px;
  gap: 10px;
  align-items: center;
  padding: 8px 10px;
  border: 1px solid #dfe8de;
  text-align: left;
}

.coupon-choice.active {
  border-color: #269544;
  background: #f6fff5;
}

.coupon-choice.disabled {
  color: #8a958e;
}

.coupon-choice strong,
.coupon-choice em {
  display: block;
}

.coupon-choice strong {
  color: #263129;
  font-size: 14px;
}

.coupon-choice em {
  margin-top: 3px;
  color: #7a867f;
  font-size: 12px;
  font-style: normal;
}

.coupon-choice b {
  color: #18843a;
  font-weight: 800;
}

.coupon-choice .el-icon {
  color: #2e9947;
}

.more-line {
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  margin-top: 8px;
  padding: 0 10px;
  border: 1px solid #e6ece5;
  color: #7c8780;
  font-size: 13px;
}

.invoice-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.invoice-tabs button {
  min-height: 34px;
  border: 1px solid #dfe8de;
  border-radius: 7px;
  color: #59645e;
  background: #fff;
  cursor: pointer;
}

.invoice-tabs button.active {
  border-color: #269544;
  color: #178437;
  background: #f6fff5;
}

.amount-card {
  padding: 16px;
}

.amount-card p {
  min-height: 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0;
  color: #4c5751;
  font-size: 14px;
}

.amount-card p + p {
  margin-top: 4px;
}

.amount-card button {
  padding: 0;
  border: 0;
  color: inherit;
  background: transparent;
  cursor: pointer;
}

.amount-card i {
  width: 15px;
  height: 15px;
  display: inline-grid;
  place-items: center;
  margin-left: 4px;
  border: 1px solid #b5beb8;
  border-radius: 50%;
  font-size: 11px;
  font-style: normal;
}

.amount-card b {
  color: #1e2923;
}

.amount-card .discount b {
  color: #ff2f1f;
}

.pay-amount {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid #edf1ec;
}

.pay-amount span {
  color: #1f2a23;
  font-size: 17px;
  font-weight: 900;
}

.pay-amount strong {
  color: #ff2f1f;
  font-size: 28px;
  line-height: 1;
}

.checkout-bottom {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 510px;
  gap: 18px;
  align-items: center;
  padding: 0 22px 16px;
}

.service-strip,
.submit-bar {
  min-height: 78px;
  border: 1px solid #e3ece1;
  border-radius: 10px;
  background: #fff;
}

.service-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 13px 22px;
}

.service-strip div {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr);
  column-gap: 10px;
  align-items: center;
}

.service-strip .el-icon {
  grid-row: span 2;
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #219542;
  font-size: 21px;
  background: #e8f7e8;
}

.service-strip strong {
  color: #1e2923;
  font-size: 14px;
}

.service-strip span {
  color: #7a867f;
  font-size: 12px;
}

.submit-bar {
  position: relative;
  justify-content: flex-end;
  gap: 20px;
  padding: 12px 18px 22px;
}

.submit-bar span {
  color: #1d2922;
  font-size: 17px;
  font-weight: 900;
}

.submit-bar strong {
  color: #ff2f1f;
  font-size: 30px;
  line-height: 1;
}

.submit-bar :deep(.el-button) {
  width: 220px;
  min-height: 50px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 900;
  background: linear-gradient(180deg, #2fa247 0%, #178334 100%);
  border-color: #178334;
}

.submit-bar p {
  position: absolute;
  right: 22px;
  bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0;
  color: #6d7b72;
  font-size: 12px;
}

.submit-bar p .el-icon {
  color: #239441;
}

:deep(.el-textarea__inner) {
  min-height: 72px;
  border-radius: 8px;
  font-size: 13px;
  box-shadow: 0 0 0 1px #e0e7df inset;
}

@media (max-width: 1180px) {
  .checkout-body,
  .checkout-bottom {
    grid-template-columns: 1fr;
  }

  .checkout-side {
    position: static;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .amount-card {
    grid-column: 1 / -1;
  }

  .address-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .checkout-hero,
  .checkout-bottom,
  .submit-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-title,
  .hero-promises {
    flex-wrap: wrap;
  }

  .checkout-body,
  .checkout-bottom {
    padding-right: 12px;
    padding-left: 12px;
  }

  .checkout-side,
  .address-grid,
  .option-grid.three,
  .option-grid.four,
  .service-strip {
    grid-template-columns: 1fr;
  }

  .goods-head {
    display: none;
  }

  .goods-row {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .goods-price,
  .goods-qty,
  .goods-subtotal {
    justify-self: start;
  }

  .submit-bar {
    display: grid;
    justify-items: stretch;
  }

  .submit-bar :deep(.el-button) {
    width: 100%;
  }

  .submit-bar p {
    position: static;
  }
}
</style>
