<template>
  <div class="cart-page">
    <section class="cart-title-row">
      <h1>购物车</h1>
      <div class="title-service">
        <el-icon><CircleCheck /></el-icon>
        <span>优选保障</span>
        <i></i>
        <span>100%正品</span>
        <span>售后无忧</span>
      </div>
    </section>

    <section class="cart-layout">
      <section v-loading="loading" class="cart-panel">
        <div v-if="loadError" class="empty-state cart-error">
          <strong>购物车加载失败</strong>
          <span>{{ loadError }}</span>
          <el-button type="primary" @click="load">重新加载</el-button>
        </div>

        <template v-else-if="items.length">
          <div class="cart-grid cart-table-head">
            <div class="select-head">
              <el-checkbox :model-value="allChecked" :indeterminate="isIndeterminate" @change="toggleAll">
                全选
              </el-checkbox>
              <span>（已选 {{ checkedItems.length }} 件）</span>
            </div>
            <span>商品信息</span>
            <span>单价</span>
            <span>数量</span>
            <span>小计</span>
            <span>操作</span>
          </div>

          <div class="cart-items">
            <article v-for="item in items" :key="cartItemId(item)" class="cart-grid cart-item" :class="{ invalid: isInvalid(item) }">
              <div class="cart-check">
                <el-checkbox :model-value="item.checked === 1" @change="(val: unknown) => check(item, Boolean(val))" />
              </div>
              <RouterLink class="cart-product" :to="`/products/${item.productId}`">
                <img :src="cartImageUrl(item)" :alt="displayCartProduct(item).name" @error="setCartImageFallback" />
                <div>
                  <h3>{{ displayCartProduct(item).name }}</h3>
                  <p class="product-subtitle">{{ productSubtitle(item) }}</p>
                  <p class="product-spec">{{ productSpec(item) }}</p>
                  <div class="cart-tags">
                    <span>{{ categoryTag(item) }}</span>
                  </div>
                </div>
              </RouterLink>
              <div class="unit-price">
                <strong>¥ {{ money(item.price) }}</strong>
                <span v-if="originalPrice(item)">¥{{ money(originalPrice(item)) }}</span>
                <em v-if="discountLabel(item)">{{ discountLabel(item) }}</em>
              </div>
              <div class="quantity-cell">
                <div class="qty-stepper">
                  <button type="button" :disabled="item.quantity <= 1 || isInvalid(item)" @click="updateQty(item, item.quantity - 1)">-</button>
                  <span>{{ item.quantity }}</span>
                  <button type="button" :disabled="item.quantity >= quantityMax(item) || isInvalid(item)" @click="updateQty(item, item.quantity + 1)">+</button>
                </div>
              </div>
              <div class="subtotal">¥{{ money(subtotal(item)) }}</div>
              <div class="item-actions">
                <button type="button" @click="remove(item)">删除</button>
                <button type="button" class="hover-action" @click="moveToFavorite(item)">移入收藏</button>
                <button type="button" class="hover-action" @click="todo('降价提醒')">降价提醒</button>
                <button type="button" class="hover-action" @click="todo('相似商品')">相似商品</button>
              </div>
            </article>
          </div>

          <div class="cart-toolbar">
            <div class="toolbar-left">
              <el-checkbox :model-value="allChecked" :indeterminate="isIndeterminate" @change="toggleAll">
                全选
              </el-checkbox>
              <span>已选 {{ checkedItems.length }} 件</span>
            </div>
            <div class="toolbar-actions">
              <button type="button" @click="batchDelete">删除选中</button>
              <button type="button" @click="batchMoveToFavorite">移入收藏</button>
              <button type="button" @click="todo('清空购物车')">清空购物车</button>
            </div>
          </div>
        </template>

        <div v-else-if="!loading" class="empty-state cart-empty">
          <div class="empty-illustration">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <strong>购物车还是空的</strong>
          <span>去挑选一些优选好物，凑满一篮新鲜生活。</span>
          <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
        </div>
      </section>

      <aside class="cart-side">
        <section class="summary-card">
          <h2>订单结算</h2>
          <div class="summary-line">
            <span>商品总额</span>
            <strong>¥ {{ money(goodsAmount) }}</strong>
          </div>
          <div class="summary-line discount">
            <button type="button" @click="todo('购物车优惠金额')">优惠金额</button>
            <strong>- ¥{{ money(discountAmount) }}</strong>
          </div>
          <div class="summary-line discount">
            <button type="button" @click="todo('购物车优惠券')">优惠券</button>
            <strong>- ¥{{ money(couponAmount) }}</strong>
          </div>
          <div class="summary-line">
            <button type="button" @click="todo('购物车运费说明')">运费 <i class="freight-help">?</i></button>
            <em>满99元包邮</em>
          </div>
          <div class="summary-total">
            <span>合计 <small>（已选 {{ checkedItems.length }} 件）</small></span>
            <strong>¥{{ money(payAmount) }}</strong>
          </div>
          <el-button type="warning" size="large" :disabled="checkedItems.length === 0" @click="goCheckout">去结算</el-button>
          <p class="summary-note">
            <el-icon><CircleCheck /></el-icon>
            支持7天无理由退货
          </p>
        </section>

        <section class="promo-card">
          <div class="promo-head">
            <h3>买满再减，更优惠</h3>
          </div>
          <p>
            还差 <strong>¥{{ money(needForFreeShipping) }}</strong> 可享受更多优惠
          </p>
          <el-progress :percentage="promoPercent" :show-text="false" color="#238d3d" />
          <div class="promo-gift">
            <el-icon><Present /></el-icon>
            <div>
              <strong>满99元包邮</strong>
              <span>配送费以订单确认页计算为准</span>
            </div>
          </div>
        </section>
      </aside>
    </section>

    <section class="cart-service-strip">
      <div v-for="item in serviceItems" :key="item.title">
        <el-icon><component :is="item.icon" /></el-icon>
        <strong>{{ item.title }}</strong>
        <span>{{ item.desc }}</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleCheck,
  Present,
  RefreshLeft,
  ShoppingCart,
  Van,
  Wallet
} from '@element-plus/icons-vue'
import cartProductDefault from '@/assets/cart-product-default.svg'
import { cartApi, favoriteApi } from '@/api/modules'
import type { CartItem } from '@/types'
import { showBackendTodo } from '@/utils/feature'
import { normalizeProduct } from '@/utils/product'

const router = useRouter()
const items = ref<CartItem[]>([])
const loading = ref(false)
const loadError = ref('')

const serviceItems = [
  { title: '优选保障', desc: '100%正品，品质保证', icon: CircleCheck },
  { title: '极速配送', desc: '多仓直发，次日达', icon: Van },
  { title: '售后无忧', desc: '7天无理由退货', icon: RefreshLeft },
  { title: '安全支付', desc: '多种支付方式保障', icon: Wallet }
]

const checkedItems = computed(() => items.value.filter((item) => item.checked === 1 && !isInvalid(item)))
const allChecked = computed(() => items.value.length > 0 && items.value.every((item) => item.checked === 1))
const isIndeterminate = computed(() => checkedItems.value.length > 0 && !allChecked.value)
const goodsAmount = computed(() => checkedItems.value.reduce((sum, item) => sum + subtotal(item), 0))
const discountAmount = computed(() => 0)
const couponAmount = computed(() => 0)
const payAmount = computed(() => Math.max(goodsAmount.value - discountAmount.value - couponAmount.value, 0))
const needForFreeShipping = computed(() => Math.max(99 - goodsAmount.value, 0))
const promoPercent = computed(() => Math.min(Math.round((goodsAmount.value / 99) * 100), 100))

const money = (value?: number) => Number(value || 0).toFixed(2)
const cartItemId = (item: CartItem) => item.cartItemId || item.id || 0
const displayCartProduct = (item: CartItem) => normalizeProduct(item)
const isInvalid = (item: CartItem) => Boolean(item.invalidReason || item.status === 0 || (item.stock !== undefined && Number(item.stock) <= 0))
const subtotal = (item: CartItem) => Number(item.totalAmount ?? Number(item.price || 0) * Number(item.quantity || 0))
const quantityMax = (item: CartItem) => Math.max(Number(item.stock || 99), 1)
const todo = (feature: string) => showBackendTodo(feature)

const cartImageUrl = (item: CartItem) => displayCartProduct(item).mainImage || cartProductDefault

const setCartImageFallback = (event: Event) => {
  const image = event.target as HTMLImageElement
  if (!image.src.endsWith('/cart-product-default.svg')) {
    image.src = cartProductDefault
  }
}

const originalPrice = (item: CartItem) => {
  const original = Number(displayCartProduct(item).originalPrice || 0)
  return original > Number(item.price || 0) ? original : 0
}

const discountLabel = (item: CartItem) => {
  const original = originalPrice(item)
  if (!original) return ''
  const discount = (Number(item.price || 0) / original) * 10
  return `${discount.toFixed(1)}折`
}

const productSubtitle = (item: CartItem) => displayCartProduct(item).subtitle || '优选好物｜品质保证'

const productSpec = (item: CartItem) => {
  if (item.invalidReason) return item.invalidReason
  if (item.stock !== undefined) return `规格：标准装｜库存 ${item.stock}`
  return '规格：标准装｜产地直供'
}

const categoryTag = (item: CartItem) => displayCartProduct(item).categoryName || (isInvalid(item) ? '不可购买' : '生鲜')

const load = async () => {
  loading.value = true
  loadError.value = ''
  try {
    items.value = await cartApi.list()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '请稍后重试'
    items.value = []
  } finally {
    loading.value = false
  }
}

const check = async (item: CartItem, checked: boolean) => {
  const id = cartItemId(item)
  if (!id) return ElMessage.error('购物车项 ID 缺失，无法勾选')
  await cartApi.check({ cartItemId: id, checked: checked ? 1 : 0 })
  await load()
}

const toggleAll = async () => {
  if (!items.value.length) return
  const next = allChecked.value ? 0 : 1
  await cartApi.checkAll(next)
  await load()
}

const updateQty = async (item: CartItem, quantity: number) => {
  const id = cartItemId(item)
  if (!id) return ElMessage.error('购物车项 ID 缺失，无法修改数量')
  const nextQuantity = Math.min(Math.max(quantity, 1), quantityMax(item))
  await cartApi.update({ cartItemId: id, quantity: nextQuantity })
  await load()
}

const remove = async (item: CartItem) => {
  const id = cartItemId(item)
  if (!id) return ElMessage.error('购物车项 ID 缺失，无法删除')
  const confirmed = await ElMessageBox.confirm('确定删除该商品吗？', '删除商品', { type: 'warning' }).catch(() => false)
  if (!confirmed) return
  await cartApi.remove(id)
  ElMessage.success('已删除购物车商品')
  await load()
}

const moveToFavorite = async (item: CartItem) => {
  if (!item.productId) return ElMessage.error('商品ID缺失，无法收藏')
  await favoriteApi.collect(item.productId)
  ElMessage.success('已收藏，购物车商品已保留')
}

const batchMoveToFavorite = async () => {
  if (!checkedItems.value.length) return ElMessage.warning('请先选择商品')
  const productIds = Array.from(new Set(checkedItems.value.map((item) => item.productId).filter(Boolean)))
  for (const productId of productIds) {
    await favoriteApi.collect(productId)
  }
  ElMessage.success('已收藏选中商品，购物车商品已保留')
}

const batchDelete = async () => {
  if (!checkedItems.value.length) return ElMessage.warning('请先选择商品')
  const confirmed = await ElMessageBox.confirm('确定删除已选商品吗？', '批量删除', { type: 'warning' }).catch(() => false)
  if (!confirmed) return
  await cartApi.removeChecked()
  ElMessage.success('已删除已选商品')
  await load()
}

const goCheckout = () => {
  if (!checkedItems.value.length) return ElMessage.warning('请先选择商品')
  const couponId = window.sessionStorage.getItem('youxuan_selected_coupon_id') || ''
  router.push({ path: '/checkout', query: { couponId: couponId || undefined } })
}

onMounted(load)
</script>

<style scoped>
.cart-page {
  width: 100%;
}

.cart-title-row {
  min-height: 76px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
}

.cart-title-row h1 {
  margin: 0;
  color: #111827;
  font-size: 34px;
  letter-spacing: 0;
}

.cart-title-row p {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 10px 0 0;
  color: #5d6b61;
  font-size: 15px;
}

.cart-title-row p .el-icon {
  color: var(--yx-primary);
  font-size: 20px;
}

.title-actions {
  display: flex;
  gap: 10px;
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 20px;
  align-items: start;
}

.cart-panel,
.summary-card,
.promo-card {
  border: 1px solid rgba(214, 230, 211, 0.94);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 30px rgba(38, 94, 54, 0.06);
}

.cart-panel {
  min-height: 440px;
  overflow: hidden;
}

.cart-table-head {
  height: 58px;
  display: grid;
  grid-template-columns: 168px minmax(280px, 1fr) 130px 150px 140px 96px;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  border-bottom: 1px solid var(--yx-border);
  color: #23352a;
  font-weight: 800;
  background: #fff;
}

.select-head,
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.select-head span,
.toolbar-left span {
  color: #8b978f;
  font-weight: 400;
}

.cart-items {
  background: #fff;
}

.cart-item {
  display: grid;
  grid-template-columns: 42px minmax(280px, 1fr) 130px 150px 140px 96px;
  align-items: center;
  gap: 12px;
  padding: 22px 24px;
  border-bottom: 1px solid var(--yx-border);
}

.cart-item.invalid {
  opacity: 0.68;
}

.cart-check {
  display: flex;
  justify-content: center;
}

.cart-product {
  min-width: 0;
  display: grid;
  grid-template-columns: 112px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
}

.cart-product img {
  width: 112px;
  height: 88px;
  display: block;
  padding: 8px;
  border-radius: 12px;
  object-fit: contain;
  background: linear-gradient(180deg, #fbfdf9 0%, #f3faf1 100%);
}

.cart-product h3 {
  margin: 0;
  overflow: hidden;
  color: #111827;
  font-size: 17px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-product p {
  margin: 10px 0 8px;
  overflow: hidden;
  color: #6b786f;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cart-tags span {
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  padding: 0 8px;
  border: 1px solid #bde5c1;
  border-radius: 6px;
  color: var(--yx-primary-dark);
  font-size: 12px;
  background: #f0fbef;
}

.unit-price strong,
.unit-price span {
  display: block;
}

.unit-price strong {
  color: #111827;
  font-size: 16px;
}

.unit-price span {
  margin-top: 6px;
  color: #9aa69d;
  font-size: 13px;
  text-decoration: line-through;
}

.quantity-cell :deep(.el-input-number) {
  width: 118px;
}

.subtotal {
  color: #ff2f1f;
  font-size: 18px;
  font-weight: 900;
}

.item-actions {
  display: grid;
  gap: 6px;
  justify-items: start;
}

.item-actions button,
.toolbar-actions button,
.summary-line button,
.promo-head button {
  padding: 0;
  border: 0;
  color: #667469;
  background: transparent;
  cursor: pointer;
}

.item-actions button:hover,
.toolbar-actions button:hover,
.summary-line button:hover,
.promo-head button:hover {
  color: var(--yx-primary-dark);
}

.cart-toolbar {
  min-height: 66px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 0 24px;
  background: #fff;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 28px;
}

.cart-side {
  position: sticky;
  top: 88px;
  display: grid;
  gap: 18px;
}

.summary-card,
.promo-card {
  padding: 24px;
}

.summary-card h2,
.promo-card h3 {
  margin: 0;
  color: #1f2c24;
}

.summary-card h2 {
  margin-bottom: 20px;
  font-size: 22px;
}

.summary-line {
  min-height: 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #4c5b51;
}

.summary-line strong {
  color: #111827;
  font-size: 16px;
}

.summary-line.discount strong {
  color: #ff2f1f;
}

.summary-line em {
  color: var(--yx-primary-dark);
  font-style: normal;
  font-weight: 800;
}

.summary-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  padding-top: 22px;
  border-top: 1px dashed var(--yx-border-strong);
}

.summary-total span {
  color: #1f2c24;
  font-size: 17px;
  font-weight: 800;
}

.summary-total small {
  margin-left: 6px;
  color: #8a978e;
  font-size: 14px;
  font-weight: 400;
}

.summary-total strong {
  color: #ff2f1f;
  font-size: 32px;
  line-height: 1;
}

.summary-card :deep(.el-button) {
  width: 100%;
  min-height: 52px;
  margin-top: 22px;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 900;
  --el-button-bg-color: #ff7a1a;
  --el-button-border-color: #ff7a1a;
  --el-button-hover-bg-color: #f05f14;
  --el-button-hover-border-color: #f05f14;
}

.summary-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 14px 0 0;
  color: #627167;
  font-size: 14px;
}

.summary-note .el-icon {
  color: var(--yx-primary);
}

.promo-card {
  background: linear-gradient(135deg, #effaf0 0%, #f8fff6 100%);
}

.promo-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.promo-head button {
  color: var(--yx-primary-dark);
  font-weight: 800;
}

.promo-card p {
  margin: 14px 0 12px;
  color: #6b786f;
}

.promo-card p strong {
  color: var(--yx-orange);
}

.promo-gift {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid rgba(206, 226, 206, 0.8);
}

.promo-gift .el-icon {
  color: var(--yx-primary);
  font-size: 32px;
}

.promo-gift strong,
.promo-gift span {
  display: block;
}

.promo-gift strong {
  color: var(--yx-primary-dark);
}

.promo-gift span {
  margin-top: 4px;
  color: #708077;
  font-size: 13px;
}

.cart-service-strip {
  min-height: 78px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  margin-top: 26px;
  padding: 16px 34px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 12px 30px rgba(38, 94, 54, 0.06);
}

.cart-service-strip div {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  column-gap: 12px;
  align-items: center;
}

.cart-service-strip .el-icon {
  grid-row: span 2;
  color: var(--yx-primary);
  font-size: 32px;
}

.cart-service-strip strong {
  color: #1f2c24;
}

.cart-service-strip span {
  color: #7c8a80;
  font-size: 13px;
}

.cart-empty,
.cart-error {
  min-height: 420px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  border: 0;
  border-radius: 0;
  background: #fff;
}

.empty-illustration {
  width: 72px;
  height: 72px;
  display: grid;
  place-items: center;
  border-radius: 24px;
  color: var(--yx-primary);
  font-size: 36px;
  background: #eff9ed;
}

.cart-empty strong,
.cart-error strong {
  color: #1f2c24;
  font-size: 22px;
}

.cart-empty span,
.cart-error span {
  color: #7c8a80;
}

.cart-title-row {
  height: 82px;
  min-height: 82px;
  justify-content: flex-start;
  margin-bottom: 14px;
}

.cart-title-row h1 {
  color: #0e1511;
  font-size: 32px;
  font-weight: 900;
}

.title-service {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606b64;
  font-size: 15px;
}

.title-service .el-icon {
  color: #33964a;
  font-size: 20px;
}

.title-service i {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #b6c2ba;
}

.cart-layout {
  grid-template-columns: minmax(0, 1fr) 392px;
  gap: 20px;
}

.cart-panel,
.summary-card,
.promo-card {
  border-color: #e2e9df;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(29, 64, 38, 0.04);
}

.cart-grid {
  display: grid;
  grid-template-columns: 136px minmax(310px, 1fr) 126px 140px 126px 100px;
  align-items: center;
  column-gap: 14px;
}

.cart-grid.cart-table-head {
  height: 58px;
  padding: 0 22px;
  grid-template-columns: 136px minmax(310px, 1fr) 126px 140px 126px 100px;
  color: #4c554f;
  font-size: 15px;
}

.cart-grid.cart-item {
  min-height: 126px;
  padding: 18px 22px;
  grid-template-columns: 136px minmax(310px, 1fr) 126px 140px 126px 100px;
}

.select-head {
  grid-column: auto;
  justify-content: center;
}

.cart-product {
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 18px;
}

.cart-product img {
  width: 96px;
  height: 96px;
  padding: 0;
  border-radius: 9px;
  object-fit: contain;
  background: #f8f8f5;
}

.cart-product h3 {
  display: -webkit-box;
  margin-bottom: 8px;
  white-space: normal;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-subtitle,
.product-spec {
  margin: 0;
  overflow: hidden;
  color: #7b837e;
  font-size: 14px;
  line-height: 1.6;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-tags span {
  border-radius: 5px;
  font-size: 13px;
}

.unit-price {
  display: grid;
  justify-items: start;
  gap: 7px;
}

.unit-price strong {
  font-size: 16px;
}

.unit-price em {
  min-height: 23px;
  padding: 2px 7px;
  border: 1px solid #ff9e4a;
  border-radius: 4px;
  color: #ff7a1a;
  font-size: 12px;
  font-style: normal;
  background: #fff8f1;
}

.quantity-cell {
  display: flex;
  justify-content: center;
}

.quantity-cell :deep(.el-input-number) {
  display: none;
}

.qty-stepper {
  width: 118px;
  height: 38px;
  display: grid;
  grid-template-columns: 36px 46px 36px;
  overflow: hidden;
  border: 1px solid #dfe6df;
  border-radius: 7px;
  background: #fff;
}

.qty-stepper button,
.qty-stepper span {
  display: grid;
  place-items: center;
  border: 0;
  border-right: 1px solid #e3e8e2;
  color: #2f3933;
  background: #fff;
  font-size: 18px;
}

.qty-stepper span {
  font-size: 17px;
  font-weight: 700;
}

.qty-stepper button:last-child {
  border-right: 0;
}

.qty-stepper button {
  cursor: pointer;
}

.qty-stepper button:disabled {
  color: #b9c0bb;
  cursor: not-allowed;
  background: #fafafa;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-items: start;
  overflow: visible;
  white-space: nowrap;
}

.item-actions button {
  font-size: 14px;
  white-space: nowrap;
}

.hover-action {
  max-width: 0;
  opacity: 0;
  overflow: hidden;
  pointer-events: none;
  transition: max-width 0.18s ease, opacity 0.16s ease;
}

.cart-item:hover .hover-action {
  max-width: 72px;
  opacity: 1;
  pointer-events: auto;
}

.summary-card {
  padding: 22px 24px 20px;
}

.summary-card h2 {
  font-size: 21px;
}

.summary-line {
  min-height: 44px;
  color: #303a34;
  font-size: 16px;
}

.summary-line.discount strong {
  color: #ff2f1f;
  font-size: 16px;
}

.summary-line em {
  color: #238d3d;
}

.freight-help {
  width: 16px;
  height: 16px;
  display: inline-grid;
  place-items: center;
  margin-left: 6px;
  border: 1px solid #aab4ae;
  border-radius: 50%;
  color: #7a837d;
  font-size: 12px;
  font-style: normal;
}

.summary-total strong {
  font-size: 32px;
}

.summary-card :deep(.el-button) {
  min-height: 54px;
  border-radius: 8px;
  background: linear-gradient(90deg, #ff9816 0%, #ff5b16 100%);
}

.promo-head {
  display: block;
}

.promo-card {
  padding: 20px 22px 18px;
}

.promo-card h3 {
  color: #178036;
  font-size: 18px;
}

.cart-service-strip {
  min-height: 66px;
  margin-top: 30px;
  padding: 15px 56px;
  border-radius: 12px;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner),
:deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  border-color: #33964a;
  background-color: #33964a;
}

:deep(.el-checkbox__label) {
  color: #151d18;
  font-size: 15px;
  font-weight: 700;
}

@media (max-width: 1180px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .cart-side {
    position: static;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 900px) {
  .cart-title-row,
  .cart-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .cart-table-head {
    display: none;
  }

  .cart-item {
    grid-template-columns: 34px minmax(0, 1fr);
  }

  .unit-price,
  .quantity-cell,
  .subtotal,
  .item-actions {
    grid-column: 2;
  }

  .cart-side,
  .cart-service-strip {
    grid-template-columns: 1fr;
  }

  .cart-service-strip {
    padding: 16px;
  }
}
</style>
