<template>
  <div class="product-detail-page">
    <nav class="detail-breadcrumb">
      <RouterLink to="/">首页</RouterLink>
      <el-icon><ArrowRight /></el-icon>
      <RouterLink to="/products">全部商品</RouterLink>
      <el-icon><ArrowRight /></el-icon>
      <span>{{ product?.categoryName || '优选商品' }}</span>
      <el-icon><ArrowRight /></el-icon>
      <strong>{{ product?.name || '商品详情' }}</strong>
    </nav>

    <section v-loading="loading" class="detail-main">
      <template v-if="product">
        <section class="gallery-card">
          <div class="gallery-main">
            <span class="fresh-badge">
              <el-icon><Goods /></el-icon>
              优选生鲜
            </span>
            <img class="detail-image" :src="activeImage" :alt="product.name" @error="setImageFallback" />
          </div>
          <div v-if="galleryImages.length" class="thumb-row">
            <button
              v-for="image in galleryImages"
              :key="image"
              class="thumb-item"
              :class="{ active: image === activeImage }"
              type="button"
              @click="activeImage = image"
            >
              <img :src="image" :alt="product.name" @error="setImageFallback" />
            </button>
          </div>
        </section>

        <section class="buy-card">
          <div class="title-row">
            <span class="hot-tag">热销</span>
            <h1>{{ product.name }}</h1>
          </div>
          <p class="subtitle">{{ product.subtitle || '品质严选，安心到家' }}</p>
          <div class="rating-row">
            <span class="stars">
              <el-icon v-for="index in 5" :key="index"><StarFilled /></el-icon>
            </span>
            <span>{{ displayAvgRating }}分</span>
            <span>{{ reviewSummary.reviewCount || 0 }} 条评价</span>
            <span>{{ salesText }} 件已售</span>
            <span>库存 {{ stockText }}</span>
          </div>

          <div class="price-panel">
            <div class="price-line">
              <span class="price-symbol">¥</span>
              <span class="current-price">{{ money(product.price) }}</span>
              <span v-if="product.originalPrice" class="origin-price">¥{{ money(product.originalPrice) }}</span>
              <span v-if="discountText" class="discount-tag">{{ discountText }}</span>
            </div>
            <div class="coupon-line">
              <span class="coupon-label">优惠</span>
              <span class="coupon-chip">满99元减10元</span>
              <span class="coupon-chip">满199元减25元</span>
              <button type="button" @click="todo('商品优惠券查看')">查看</button>
            </div>
          </div>

          <div class="info-list">
            <div class="info-row">
              <span class="info-label">配送</span>
              <span>同城优先配送，预计次日送达</span>
              <strong>有货</strong>
            </div>
            <div class="info-row service-row">
              <span class="info-label">服务</span>
              <span><el-icon><CircleCheck /></el-icon> 品质保障</span>
              <span><el-icon><Van /></el-icon> 48小时发货</span>
              <span><el-icon><RefreshLeft /></el-icon> 售后无忧</span>
            </div>
            <div class="info-row">
              <span class="info-label">规格</span>
              <button class="spec-chip active" type="button" @click="todo('商品规格选择')">
                {{ product.categoryName || '优选商品' }}
              </button>
            </div>
            <div class="info-row quantity-row">
              <span class="info-label">数量</span>
              <el-input-number v-model="quantity" :min="1" :max="quantityMax" :disabled="!canBuy" />
              <span class="stock-note">库存 {{ stockText }} 件</span>
            </div>
          </div>

          <div class="action-row">
            <el-button size="large" :icon="ShoppingCart" :disabled="!canBuy" @click="addCart">加入购物车</el-button>
            <el-button size="large" type="primary" :disabled="!canBuy" @click="buyNow">立即购买</el-button>
            <el-button
              size="large"
              :icon="favorited ? StarFilled : Star"
              :loading="favoriteLoading"
              @click="toggleFavorite"
            >
              {{ favorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </section>

        <aside class="side-column">
          <section class="shop-card">
            <div class="shop-head">
              <div class="shop-avatar">
                <el-icon><Service /></el-icon>
              </div>
              <div>
                <h3>优选生鲜旗舰店</h3>
                <span>品牌直营</span>
              </div>
            </div>
            <div class="score-row">
              <div>
                <strong>4.9</strong>
                <span>描述相符</span>
              </div>
              <div>
                <strong>4.9</strong>
                <span>服务态度</span>
              </div>
              <div>
                <strong>4.9</strong>
                <span>物流服务</span>
              </div>
            </div>
            <el-button @click="todo('店铺首页')">进入店铺</el-button>
          </section>

          <section class="protect-card">
            <h3>商品保障</h3>
            <div v-for="item in protections" :key="item.title" class="protect-item">
              <el-icon><component :is="item.icon" /></el-icon>
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </div>
            </div>
          </section>
        </aside>
      </template>
      <div v-else-if="!loading" class="empty-state detail-empty">商品不存在或已下架</div>
    </section>

    <section v-if="product" class="detail-lower">
      <section class="tabs-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div v-if="product.detail" class="detail-copy">
              <h2>{{ product.name }}</h2>
              <p>{{ product.detail }}</p>
            </div>
            <div v-else class="empty-state tab-empty">暂无详细图文说明，当前仅展示商品基础信息。</div>
          </el-tab-pane>
          <el-tab-pane label="规格参数" name="spec">
            <dl class="spec-list">
              <div>
                <dt>商品名称</dt>
                <dd>{{ product.name }}</dd>
              </div>
              <div>
                <dt>商品分类</dt>
                <dd>{{ product.categoryName || '优选商品' }}</dd>
              </div>
              <div>
                <dt>商品状态</dt>
                <dd>{{ product.status === 1 ? '上架销售' : '已下架' }}</dd>
              </div>
              <div>
                <dt>库存数量</dt>
                <dd>{{ stockText }} 件</dd>
              </div>
            </dl>
          </el-tab-pane>
          <el-tab-pane label="服务保障" name="service">
            <div class="service-grid">
              <div v-for="item in protections" :key="item.title">
                <el-icon><component :is="item.icon" /></el-icon>
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`商品评价 (${reviewSummary.reviewCount || 0})`" name="reviews">
            <section class="review-section">
              <div class="review-summary">
                <div class="review-score">
                  <strong>{{ displayAvgRating }}</strong>
                  <el-rate :model-value="reviewSummary.avgRating" disabled allow-half />
                  <span>{{ reviewSummary.reviewCount || 0 }} 条评价</span>
                </div>
                <div class="rating-breakdown">
                  <div v-for="item in ratingBreakdown" :key="item.star" class="rating-breakdown-row">
                    <span>{{ item.star }}星</span>
                    <div><i :style="{ width: `${item.percent}%` }"></i></div>
                    <em>{{ item.count }}</em>
                  </div>
                </div>
              </div>

              <div class="review-form">
                <h3>发表评价</h3>
                <template v-if="auth.isLogin">
                  <el-rate v-model="reviewForm.rating" />
                  <el-input
                    v-model.trim="reviewForm.content"
                    type="textarea"
                    :rows="3"
                    maxlength="500"
                    show-word-limit
                    placeholder="分享这件商品的真实体验"
                  />
                  <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
                </template>
                <div v-else class="review-login-tip">
                  <span>登录后可以发表评价</span>
                  <el-button type="primary" plain @click="promptReviewLogin">去登录</el-button>
                </div>
              </div>

              <div v-loading="reviewsLoading" class="review-list">
                <article v-for="item in reviews" :key="item.id" class="review-item">
                  <div class="review-item-head">
                    <strong>{{ item.username || `用户${item.userId}` }}</strong>
                    <el-rate :model-value="item.rating" disabled />
                    <span>{{ formatTime(item.createTime) }}</span>
                  </div>
                  <p>{{ item.content }}</p>
                </article>
                <div v-if="!reviewsLoading && !reviews.length" class="empty-state review-empty">暂无评价，期待你的第一条真实体验。</div>
                <el-pagination
                  v-if="reviewTotal > reviewQuery.pageSize"
                  small
                  background
                  layout="prev, pager, next"
                  :current-page="reviewQuery.pageNum"
                  :page-size="reviewQuery.pageSize"
                  :total="reviewTotal"
                  @current-change="changeReviewPage"
                />
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </section>

      <aside class="recommend-card">
        <div class="recommend-head">
          <h3>搭配推荐</h3>
          <RouterLink to="/products">查看更多</RouterLink>
        </div>
        <div v-if="recommendProducts.length" class="recommend-list">
          <ProductCard
            v-for="item in recommendProducts"
            :key="item.id"
            :product="item"
            :favorited="recommendFavoriteIds.has(item.id)"
            :favorite-busy="recommendFavoriteBusyIds.has(item.id)"
            @add="addRecommendCart"
            @favorite-toggle="handleRecommendFavoriteToggle"
          />
        </div>
        <div v-else class="empty-state recommend-empty">暂无推荐商品</div>
      </aside>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Box,
  CircleCheck,
  Goods,
  RefreshLeft,
  Service,
  ShoppingCart,
  Star,
  StarFilled,
  Van
} from '@element-plus/icons-vue'
import ProductCard from '@/components/ProductCard.vue'
import { cartApi, favoriteApi, productApi, reviewApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { Product, ProductReview, ProductReviewSummary } from '@/types'
import { showBackendTodo } from '@/utils/feature'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProduct, normalizeProducts, productIdOf } from '@/utils/product'

type ProductWithGallery = Product & Record<string, unknown>

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref<ProductWithGallery>()
const recommendProducts = ref<Product[]>([])
const recommendFavoriteIds = ref(new Set<number>())
const recommendFavoriteBusyIds = ref(new Set<number>())
const activeImage = ref('')
const activeTab = ref('detail')
const quantity = ref(1)
const loading = ref(false)
const favorited = ref(false)
const favoriteLoading = ref(false)
const reviews = ref<ProductReview[]>([])
const reviewSummary = ref<ProductReviewSummary>({ avgRating: 0, reviewCount: 0, ratingCount: {} })
const reviewTotal = ref(0)
const reviewsLoading = ref(false)
const reviewSubmitting = ref(false)
const reviewQuery = reactive({ pageNum: 1, pageSize: 5 })
const reviewForm = reactive({ rating: 5, content: '' })

const protections = [
  { title: '品质保证', desc: '优选生鲜，严选好物', icon: CircleCheck },
  { title: '坏果包赔', desc: '生鲜果蔬，放心选购', icon: Box },
  { title: '售后无忧', desc: '问题订单及时处理', icon: RefreshLeft }
]

const money = (value?: number) => Number(value || 0).toFixed(2)
const stockCount = computed(() => Number(product.value?.stock || 0))
const quantityMax = computed(() => Math.max(stockCount.value, 1))
const canBuy = computed(() => Boolean(product.value && product.value.status === 1 && stockCount.value > 0))
const stockText = computed(() => String(stockCount.value))
const salesText = computed(() => {
  const sales = Number(product.value?.sales || 0)
  return sales >= 10000 ? `${(sales / 10000).toFixed(1)}万+` : `${sales || 0}`
})
const discountText = computed(() => {
  const price = Number(product.value?.price || 0)
  const original = Number(product.value?.originalPrice || 0)
  if (!price || !original || original <= price) return ''
  return `${((price / original) * 10).toFixed(1)}折`
})
const todo = (feature: string) => showBackendTodo(feature)
const displayAvgRating = computed(() => Number(reviewSummary.value.avgRating || 0).toFixed(1))
const ratingBreakdown = computed(() => {
  const total = Number(reviewSummary.value.reviewCount || 0)
  return [5, 4, 3, 2, 1].map((star) => {
    const count = Number(reviewSummary.value.ratingCount?.[star] || reviewSummary.value.ratingCount?.[String(star)] || 0)
    return { star, count, percent: total ? Math.round((count / total) * 100) : 0 }
  })
})

const collectImages = (current: ProductWithGallery) => {
  const rawImages = [current.images, current.imageList, current.productImages, current.gallery]
    .flatMap((value) => (Array.isArray(value) ? value : []))
    .map((value) => {
      if (typeof value === 'string') return value
      if (value && typeof value === 'object') {
        const image = value as { url?: string; imageUrl?: string; mainImage?: string; productImage?: string }
        return image.url || image.imageUrl || image.mainImage || image.productImage || ''
      }
      return ''
    })
  return Array.from(new Set([getImageUrl(current), ...rawImages].filter(Boolean)))
}

const galleryImages = computed(() => (product.value ? collectImages(product.value) : []))

const ensureLogin = () => {
  if (auth.isLogin) return true
  ElMessage.warning('请先登录后再加入购物车')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

const ensureLoginForFavorite = () => {
  if (auth.isLogin) return true
  ElMessage.warning('请先登录后再收藏商品')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

const promptReviewLogin = () => {
  ElMessage.warning('请先登录后再发表评价')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

const addCart = async () => {
  if (!product.value || !ensureLogin()) return false
  if (!canBuy.value) {
    ElMessage.warning('当前商品库存不足')
    return false
  }
  const productId = productIdOf(product.value)
  if (!productId) {
    ElMessage.error('商品数据缺少ID，暂不能加入购物车')
    return false
  }
  await cartApi.add({ productId, quantity: quantity.value })
  ElMessage.success('已加入购物车')
  return true
}

const buyNow = async () => {
  const added = await addCart()
  if (!added) return
  ElMessage.info('请先加入购物车结算')
  router.push('/cart')
}

const addRecommendCart = async (item: Product) => {
  if (!ensureLogin()) return
  const productId = productIdOf(item)
  if (!productId) {
    ElMessage.error('商品数据缺少ID，暂不能加入购物车')
    return
  }
  await cartApi.add({ productId, quantity: 1 })
  ElMessage.success('已加入购物车')
}

const loadFavoriteState = async (productId: number) => {
  if (!auth.isLogin) {
    favorited.value = false
    return
  }
  try {
    favorited.value = Boolean(await favoriteApi.check(productId))
  } catch {
    favorited.value = false
  }
}

const toggleFavorite = async () => {
  if (!product.value || !ensureLoginForFavorite()) return
  const productId = productIdOf(product.value)
  if (!productId) {
    ElMessage.error('商品数据缺少ID，暂不能收藏')
    return
  }
  favoriteLoading.value = true
  try {
    if (favorited.value) {
      await favoriteApi.cancel(productId)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.collect(productId)
      favorited.value = true
      ElMessage.success('已收藏')
    }
  } finally {
    favoriteLoading.value = false
  }
}

const markRecommendFavoriteBusy = (productId: number, busy: boolean) => {
  const next = new Set(recommendFavoriteBusyIds.value)
  if (busy) {
    next.add(productId)
  } else {
    next.delete(productId)
  }
  recommendFavoriteBusyIds.value = next
}

const setRecommendFavorite = (productId: number, favorited: boolean) => {
  const next = new Set(recommendFavoriteIds.value)
  if (favorited) {
    next.add(productId)
  } else {
    next.delete(productId)
  }
  recommendFavoriteIds.value = next
  recommendProducts.value = recommendProducts.value.map((p) =>
    p.id === productId ? { ...p, collected: favorited, isFavorite: favorited } : p
  )
}

const syncRecommendFavoriteStates = async () => {
  const productIds = recommendProducts.value
    .map((p) => productIdOf(p))
    .filter((id): id is number => Boolean(id))
  if (!auth.isLogin || !productIds.length) {
    recommendFavoriteIds.value = new Set()
    recommendProducts.value = recommendProducts.value.map((p) => ({ ...p, collected: false, isFavorite: false }))
    return
  }
  try {
    const collectedProductIds = new Set(await favoriteApi.checkBatch(productIds))
    recommendFavoriteIds.value = collectedProductIds
    recommendProducts.value = recommendProducts.value.map((p) => ({
      ...p,
      collected: collectedProductIds.has(p.id),
      isFavorite: collectedProductIds.has(p.id)
    }))
  } catch {
    recommendFavoriteIds.value = new Set()
  }
}

const handleRecommendFavoriteToggle = async (product: Product) => {
  const productId = productIdOf(product)
  if (!productId) {
    ElMessage.error('商品数据缺少ID，暂不能收藏')
    return
  }
  markRecommendFavoriteBusy(productId, true)
  try {
    if (recommendFavoriteIds.value.has(productId)) {
      await favoriteApi.cancel(productId)
      setRecommendFavorite(productId, false)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.collect(productId)
      setRecommendFavorite(productId, true)
      ElMessage.success('已收藏')
    }
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    markRecommendFavoriteBusy(productId, false)
  }
}

const loadReviewSummary = async (productId: number) => {
  try {
    reviewSummary.value = await reviewApi.summary(productId)
  } catch {
    reviewSummary.value = { avgRating: 0, reviewCount: 0, ratingCount: {} }
  }
}

const loadReviews = async (productId: number) => {
  reviewsLoading.value = true
  try {
    const data = await reviewApi.page(productId, reviewQuery)
    reviews.value = data.records || []
    reviewTotal.value = data.total || 0
  } finally {
    reviewsLoading.value = false
  }
}

const loadReviewData = async (productId: number) => {
  await Promise.all([loadReviewSummary(productId), loadReviews(productId)])
}

const submitReview = async () => {
  if (!product.value) return
  if (!auth.isLogin) {
    promptReviewLogin()
    return
  }
  const productId = productIdOf(product.value)
  if (!productId) {
    ElMessage.error('商品数据缺少ID，暂不能评价')
    return
  }
  if (reviewForm.rating < 1 || reviewForm.rating > 5) {
    ElMessage.warning('评分必须在1到5之间')
    return
  }
  if (!reviewForm.content.trim()) {
    ElMessage.warning('评价内容不能为空')
    return
  }
  reviewSubmitting.value = true
  try {
    await reviewApi.create(productId, { rating: reviewForm.rating, content: reviewForm.content.trim() })
    ElMessage.success('评价已提交')
    reviewForm.rating = 5
    reviewForm.content = ''
    reviewQuery.pageNum = 1
    await loadReviewData(productId)
  } finally {
    reviewSubmitting.value = false
  }
}

const changeReviewPage = async (page: number) => {
  if (!product.value) return
  const productId = productIdOf(product.value)
  if (!productId) return
  reviewQuery.pageNum = page
  await loadReviews(productId)
}

const formatTime = (value?: string) => {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

const loadRecommendProducts = async (currentId: number) => {
  try {
    const hotProducts = normalizeProducts(await productApi.hot()).filter((item) => item.id && item.id !== currentId)
    if (hotProducts.length) {
      recommendProducts.value = hotProducts.slice(0, 3)
      await syncRecommendFavoriteStates()
      return
    }
    const page = await productApi.page({ pageNum: 1, pageSize: 6 })
    recommendProducts.value = normalizeProducts(page.records || []).filter((item) => item.id && item.id !== currentId).slice(0, 3)
    await syncRecommendFavoriteStates()
  } catch {
    recommendProducts.value = []
  }
}

const loadProduct = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    if (!Number.isFinite(id)) return
    const data = normalizeProduct(await productApi.detail(id)) as ProductWithGallery
    product.value = data
    activeImage.value = getImageUrl(data)
    quantity.value = 1
    reviewQuery.pageNum = 1
    await loadFavoriteState(data.id)
    await loadReviewData(data.id)
    await loadRecommendProducts(data.id)
  } finally {
    loading.value = false
  }
}

watch(stockCount, (stock) => {
  if (stock > 0 && quantity.value > stock) quantity.value = stock
})

watch(() => auth.token, () => syncRecommendFavoriteStates())

watch(
  () => route.params.id,
  () => loadProduct()
)

onMounted(loadProduct)
</script>

<style scoped>
.product-detail-page {
  width: 100%;
}

.detail-breadcrumb {
  min-height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667469;
  font-size: 14px;
}

.detail-breadcrumb a:hover,
.detail-breadcrumb strong {
  color: #1f3a27;
}

.detail-breadcrumb strong {
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-main {
  min-height: 430px;
  display: grid;
  grid-template-columns: minmax(360px, 520px) minmax(420px, 1fr) 250px;
  gap: 18px;
  align-items: start;
}

.gallery-card,
.buy-card,
.shop-card,
.protect-card,
.tabs-card,
.recommend-card {
  border: 1px solid rgba(214, 230, 211, 0.94);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 30px rgba(38, 94, 54, 0.06);
}

.gallery-card {
  border: 0;
  background: transparent;
  box-shadow: none;
}

.gallery-main {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  background: linear-gradient(135deg, #edf8e8, #fff7e8);
}

.fresh-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  border-radius: 999px;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  background: linear-gradient(135deg, #31b95c, #16863b);
}

.detail-image {
  width: 100%;
  aspect-ratio: 1.38;
  display: block;
  object-fit: contain;
  background: linear-gradient(180deg, #fbfdf9 0%, #f3faf1 100%);
}

.thumb-row {
  display: flex;
  gap: 14px;
  padding: 18px 34px 0;
}

.thumb-item {
  width: 86px;
  height: 64px;
  overflow: hidden;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
}

.thumb-item.active {
  border-color: var(--yx-primary);
}

.thumb-item img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}

.buy-card {
  padding: 24px 26px 20px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.hot-tag {
  flex: 0 0 auto;
  margin-top: 4px;
  padding: 5px 8px;
  border-radius: 6px;
  color: #fff;
  font-size: 14px;
  font-weight: 800;
  background: var(--yx-orange);
}

.title-row h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 1.32;
}

.subtitle {
  margin: 12px 0 14px;
  color: #687569;
  line-height: 1.7;
}

.rating-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--yx-border);
  color: #6a766d;
  font-size: 14px;
}

.stars {
  display: inline-flex;
  color: #ff9f1c;
}

.price-panel {
  margin: 16px 0;
  overflow: hidden;
  border-radius: 14px;
  background: linear-gradient(90deg, #fff5ed 0%, #fffaf5 100%);
}

.price-line {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 16px 18px 10px;
}

.price-symbol,
.current-price {
  color: #f0381f;
  font-weight: 900;
}

.price-symbol {
  font-size: 25px;
}

.current-price {
  font-size: 36px;
  line-height: 1;
}

.origin-price {
  color: #9fa8a1;
  text-decoration: line-through;
}

.discount-tag,
.coupon-chip {
  border: 1px solid #ffb58b;
  border-radius: 7px;
  color: #f05a22;
  background: #fff8f2;
}

.discount-tag {
  padding: 3px 8px;
  font-size: 13px;
}

.coupon-line {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px 14px;
  color: #755c4d;
}

.coupon-label {
  color: #e85f1c;
  font-weight: 800;
}

.coupon-chip {
  padding: 4px 9px;
  font-size: 13px;
}

.coupon-line button {
  margin-left: auto;
  border: 0;
  color: #c97842;
  background: transparent;
  cursor: pointer;
}

.info-list {
  display: grid;
  gap: 0;
}

.info-row {
  min-height: 46px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid var(--yx-border);
  color: #46564b;
  font-size: 14px;
}

.info-label {
  width: 38px;
  flex: 0 0 auto;
  color: #1f2c24;
  font-weight: 800;
}

.info-row strong {
  color: var(--yx-primary-dark);
}

.service-row {
  flex-wrap: wrap;
  row-gap: 8px;
}

.service-row span:not(.info-label) {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.service-row .el-icon {
  color: var(--yx-primary);
}

.spec-chip {
  min-height: 34px;
  padding: 0 18px;
  border: 1px solid #dce7dc;
  border-radius: 9px;
  color: #56665a;
  background: #fff;
}

.spec-chip.active {
  border-color: var(--yx-primary);
  color: var(--yx-primary-dark);
  font-weight: 800;
  background: #f1fbef;
}

.quantity-row :deep(.el-input-number) {
  width: 118px;
}

.stock-note {
  color: #8a978e;
}

.action-row {
  display: grid;
  grid-template-columns: 1fr 1fr 0.82fr;
  gap: 18px;
  margin-top: 22px;
}

.action-row :deep(.el-button) {
  min-height: 48px;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 800;
}

.action-row :deep(.el-button:not(.el-button--primary)) {
  border-color: var(--yx-primary);
  color: var(--yx-primary-dark);
  background: #fff;
}

.side-column {
  display: grid;
  gap: 14px;
}

.shop-card,
.protect-card {
  padding: 20px;
}

.shop-head {
  display: flex;
  gap: 12px;
  align-items: center;
}

.shop-avatar {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 24px;
  background: linear-gradient(135deg, #9fda5d, #3fbf5f);
}

.shop-head h3,
.protect-card h3,
.recommend-head h3 {
  margin: 0;
  color: #1f2c24;
  font-size: 18px;
}

.shop-head span {
  display: inline-block;
  margin-top: 5px;
  padding: 2px 7px;
  border: 1px solid #f7bd7c;
  border-radius: 5px;
  color: #d76b1b;
  font-size: 12px;
}

.score-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin: 20px 0;
  text-align: center;
}

.score-row strong,
.score-row span {
  display: block;
}

.score-row strong {
  color: #1f2c24;
  font-size: 18px;
}

.score-row span {
  margin-top: 6px;
  color: #8a978e;
  font-size: 12px;
}

.shop-card :deep(.el-button) {
  width: 100%;
  border-radius: 10px;
  color: var(--yx-primary-dark);
}

.protect-card {
  display: grid;
  gap: 18px;
}

.protect-item {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.protect-item > .el-icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  color: var(--yx-primary);
  background: #eff9ed;
}

.protect-item strong,
.protect-item span {
  display: block;
}

.protect-item strong {
  color: #1f2c24;
}

.protect-item span {
  margin-top: 4px;
  color: #7c8980;
  font-size: 13px;
}

.detail-lower {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 18px;
  margin-top: 18px;
}

.tabs-card {
  overflow: hidden;
  padding: 0 20px 24px;
}

.tabs-card :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.tabs-card :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--yx-border);
}

.tabs-card :deep(.el-tabs__item) {
  height: 54px;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 800;
}

.tabs-card :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 999px;
  background: var(--yx-primary);
}

.detail-copy {
  min-height: 250px;
  padding: 28px;
  border-radius: 16px;
  background: linear-gradient(100deg, #edf9df 0%, #fff8df 100%);
}

.detail-copy h2 {
  margin: 0 0 14px;
  color: #166522;
  font-size: 28px;
}

.detail-copy p {
  max-width: 760px;
  margin: 0;
  color: #425c45;
  line-height: 1.9;
}

.tab-empty {
  min-height: 220px;
  display: grid;
  place-items: center;
}

.spec-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0;
  overflow: hidden;
  border: 1px solid var(--yx-border);
  border-radius: 14px;
}

.spec-list div {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  border-bottom: 1px solid var(--yx-border);
}

.spec-list div:nth-last-child(-n + 2) {
  border-bottom: 0;
}

.spec-list dt,
.spec-list dd {
  min-height: 46px;
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0 16px;
}

.spec-list dt {
  color: #5c6b60;
  background: #f5faf3;
}

.spec-list dd {
  color: #1f2c24;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.service-grid div {
  min-height: 132px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  padding: 20px;
  border: 1px solid var(--yx-border);
  border-radius: 14px;
  text-align: center;
  background: #fbfef9;
}

.service-grid .el-icon {
  color: var(--yx-primary);
  font-size: 26px;
}

.service-grid strong {
  color: #1f2c24;
}

.service-grid span {
  color: #738078;
  font-size: 13px;
}

.review-section {
  display: grid;
  gap: 18px;
}

.review-summary {
  display: grid;
  grid-template-columns: minmax(220px, 0.7fr) minmax(260px, 1fr);
  gap: 18px;
  padding: 18px;
  border: 1px solid var(--yx-border);
  border-radius: 14px;
  background: #fbfef9;
}

.review-score {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  min-height: 132px;
}

.review-score strong {
  color: #f05a22;
  font-size: 42px;
  line-height: 1;
}

.review-score span {
  color: #667469;
  font-size: 13px;
}

.rating-breakdown {
  display: grid;
  gap: 8px;
  align-content: center;
}

.rating-breakdown-row {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr) 36px;
  gap: 10px;
  align-items: center;
  color: #617068;
  font-size: 13px;
}

.rating-breakdown-row div {
  overflow: hidden;
  height: 8px;
  border-radius: 999px;
  background: #edf3ea;
}

.rating-breakdown-row i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #ffb84d, #f05a22);
}

.rating-breakdown-row em {
  color: #8a978e;
  font-style: normal;
  text-align: right;
}

.review-form {
  display: grid;
  gap: 12px;
  padding: 18px;
  border: 1px solid var(--yx-border);
  border-radius: 14px;
  background: #fff;
}

.review-form h3 {
  margin: 0;
  color: #1f2c24;
  font-size: 17px;
}

.review-form :deep(.el-textarea__inner) {
  border-radius: 12px;
}

.review-form :deep(.el-button) {
  justify-self: start;
  border-radius: 10px;
  font-weight: 800;
}

.review-login-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #667469;
}

.review-list {
  display: grid;
  gap: 12px;
  min-height: 140px;
}

.review-item {
  display: grid;
  gap: 10px;
  padding: 16px;
  border: 1px solid var(--yx-border);
  border-radius: 14px;
  background: #fff;
}

.review-item-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.review-item-head strong {
  color: #1f2c24;
}

.review-item-head span {
  margin-left: auto;
  color: #9aa59d;
  font-size: 13px;
}

.review-item p {
  margin: 0;
  color: #46564b;
  line-height: 1.8;
}

.review-empty {
  min-height: 120px;
}

.recommend-card {
  padding: 20px;
}

.recommend-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.recommend-head a {
  color: #8a978e;
  font-size: 13px;
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.recommend-list :deep(.product-card) {
  border-radius: 12px;
  box-shadow: none;
}

.recommend-list :deep(.product-card:hover) {
  transform: translateY(-2px);
}

.recommend-list :deep(.product-cover) {
  height: 126px;
  padding: 10px;
  object-fit: contain;
}

.recommend-list :deep(.product-info) {
  padding: 10px 10px 12px;
}

.recommend-list :deep(.product-name) {
  font-size: 13px;
}

.recommend-list :deep(.product-desc),
.recommend-list :deep(.old-price) {
  display: none;
}

.recommend-list :deep(.price) {
  font-size: 16px;
}

.recommend-list :deep(.el-button.is-circle) {
  width: 28px;
  height: 28px;
  min-height: 28px;
}

.recommend-empty,
.detail-empty {
  min-height: 240px;
}

@media (max-width: 1280px) {
  .detail-main {
    grid-template-columns: minmax(320px, 0.9fr) minmax(380px, 1.1fr);
  }

  .side-column {
    grid-column: 1 / -1;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .detail-lower {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .detail-main,
  .detail-lower,
  .side-column {
    grid-template-columns: 1fr;
  }

  .detail-image {
    aspect-ratio: 1.15;
  }

  .action-row,
  .service-grid,
  .review-summary,
  .spec-list {
    grid-template-columns: 1fr;
  }

  .spec-list div:nth-last-child(-n + 2) {
    border-bottom: 1px solid var(--yx-border);
  }

  .recommend-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
