<template>
  <div class="home-page">
    <section class="home-hero">
      <aside class="category-panel">
        <div class="category-title">
          <el-icon><Menu /></el-icon>
          <h3>全部商品分类</h3>
        </div>
        <RouterLink
          v-for="(item, index) in visibleCategories"
          :key="item.id"
          class="category-link"
          :to="{ path: '/products', query: { categoryId: item.id } }"
        >
          <span class="category-icon">
            <el-icon><component :is="categoryIcon(index)" /></el-icon>
          </span>
          <span class="category-name">{{ categoryName(item) }}</span>
          <el-icon class="category-arrow"><ArrowRight /></el-icon>
        </RouterLink>
        <div v-if="!visibleCategories.length" class="category-empty">暂无分类</div>
      </aside>
      <div class="home-banner-frame">
        <HomeBannerCarousel :banners="banners" />
      </div>
    </section>

    <section class="quick-row">
      <button
        v-for="item in quickItems"
        :key="item.title"
        class="quick-card"
        :class="`quick-card-${item.tone}`"
        @click="$router.push(item.to)"
      >
        <span class="quick-icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div class="quick-copy">
          <b>{{ item.title }}</b>
          <small>{{ item.desc }}</small>
          <em>{{ item.action }}</em>
        </div>
      </button>
    </section>

    <section class="home-product-section" v-loading="loading">
      <div class="home-section-head">
        <div class="home-section-title">
          <span class="section-leaf">
            <el-icon><Goods /></el-icon>
          </span>
          <div>
            <h2>热门推荐</h2>
            <p>大家都在买的优质好物</p>
          </div>
        </div>
        <RouterLink class="section-more" to="/products">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </RouterLink>
      </div>
      <div v-if="hotProducts.length" class="home-product-grid">
        <ProductCard
          v-for="product in hotProducts"
          :key="product.id"
          :product="product"
          :favorited="favoriteIds.has(product.id)"
          :favorite-busy="favoriteBusyIds.has(product.id)"
          @add="addCart"
          @favorite-toggle="handleFavoriteToggle"
        />
      </div>
      <div v-else class="empty-state">暂无热门商品，请先在后台新增商品</div>
    </section>

    <section class="home-product-section" v-loading="loading">
      <div class="home-section-head">
        <div class="home-section-title">
          <span class="section-leaf">
            <el-icon><StarFilled /></el-icon>
          </span>
          <div>
            <h2>优选推荐</h2>
            <p>来自首页运营推荐位</p>
          </div>
        </div>
        <RouterLink class="section-more" to="/products">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </RouterLink>
      </div>
      <div v-if="recommendProducts.length" class="home-product-grid">
        <ProductCard
          v-for="product in recommendProducts"
          :key="product.id"
          :product="product"
          :favorited="favoriteIds.has(product.id)"
          :favorite-busy="favoriteBusyIds.has(product.id)"
          @add="addCart"
          @favorite-toggle="handleFavoriteToggle"
        />
      </div>
      <div v-else class="empty-state">暂无推荐商品，请先配置首页推荐位</div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, type Component } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Apple,
  ArrowRight,
  Bowl,
  Box,
  Clock,
  CoffeeCup,
  ColdDrink,
  Food,
  Goods,
  Menu,
  Present,
  ShoppingBag,
  StarFilled,
  Tickets
} from '@element-plus/icons-vue'
import HomeBannerCarousel from '@/components/HomeBannerCarousel.vue'
import ProductCard from '@/components/ProductCard.vue'
import { cartApi, favoriteApi, homeApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { Category, Product, Banner } from '@/types'
import { normalizeProducts, productIdOf } from '@/utils/product'

const banners = ref<Banner[]>([])
const categories = ref<Category[]>([])
const hotProducts = ref<Product[]>([])
const recommendProducts = ref<Product[]>([])
const favoriteIds = ref(new Set<number>())
const favoriteBusyIds = ref(new Set<number>())
const auth = useAuthStore()
const loading = ref(false)
const quickItems = [
  { icon: Tickets, title: '优惠券中心', desc: '领券更优惠', action: '立即领取', to: '/coupons', tone: 'coupon' },
  { icon: Present, title: '新人专享', desc: '新人领取大礼包', action: '立即领取', to: '/coupons', tone: 'new' },
  { icon: Clock, title: '限时上新', desc: '发现当季新品', action: '立即抢购', to: '/products', tone: 'season' },
  { icon: StarFilled, title: '优选推荐', desc: '品质好物精选', action: '立即查看', to: '/products', tone: 'pick' }
]

const categoryIcons: Component[] = [
  Apple,
  Food,
  Bowl,
  CoffeeCup,
  ShoppingBag,
  ColdDrink,
  Box,
  Goods
]
const categoryIcon = (index: number) => categoryIcons[index % categoryIcons.length]

const dirtyNamePattern = /(\?\?|阶段|Stage|\d{8,}|1[3-9]\d{9})/i
const categoryName = (item: Category) => (item.name || item.categoryName || '').trim()
const visibleCategories = computed(() =>
  categories.value.filter((item) => {
    const name = categoryName(item)
    return name && !dirtyNamePattern.test(name)
  }).slice(0, 8)
)

const cleanProducts = (products: Product[]) =>
  normalizeProducts(products).filter((product) => product.id > 0 && product.name && product.name !== '未命名商品')

const load = async () => {
  loading.value = true
  try {
    const data = await homeApi.index()
    banners.value = data.banners || []
    categories.value = data.categories || []
    hotProducts.value = cleanProducts(data.hotProducts || [])
    recommendProducts.value = cleanProducts(data.recommendProducts || [])
    await syncFavoriteStates()
  } catch (error) {
    ElMessage.error('首页数据加载失败，请检查 Gateway 和 home-service')
  } finally {
    loading.value = false
  }
}

const addCart = async (product: Product) => {
  const productId = productIdOf(product)
  if (!productId) {
    ElMessage.error('商品数据缺少ID，暂不能加入购物车')
    return
  }
  await cartApi.add({ productId, quantity: 1 })
  ElMessage.success('已加入购物车')
}

const markFavoriteBusy = (productId: number, busy: boolean) => {
  const next = new Set(favoriteBusyIds.value)
  if (busy) {
    next.add(productId)
  } else {
    next.delete(productId)
  }
  favoriteBusyIds.value = next
}

const setFavorite = (productId: number, favorited: boolean) => {
  const next = new Set(favoriteIds.value)
  if (favorited) {
    next.add(productId)
  } else {
    next.delete(productId)
  }
  favoriteIds.value = next
  // Sync state into product arrays so ProductCard picks it up
  hotProducts.value = hotProducts.value.map((p) =>
    p.id === productId ? { ...p, collected: favorited, isFavorite: favorited } : p
  )
  recommendProducts.value = recommendProducts.value.map((p) =>
    p.id === productId ? { ...p, collected: favorited, isFavorite: favorited } : p
  )
}

const syncFavoriteStates = async () => {
  const allProducts = [...hotProducts.value, ...recommendProducts.value]
  const productIds = allProducts.map((product) => productIdOf(product)).filter((id): id is number => Boolean(id))
  if (!auth.isLogin || !productIds.length) {
    favoriteIds.value = new Set()
    hotProducts.value = hotProducts.value.map((p) => ({ ...p, collected: false, isFavorite: false }))
    recommendProducts.value = recommendProducts.value.map((p) => ({ ...p, collected: false, isFavorite: false }))
    return
  }
  try {
    const collectedProductIds = new Set(await favoriteApi.checkBatch(productIds))
    favoriteIds.value = collectedProductIds
    hotProducts.value = hotProducts.value.map((p) => ({
      ...p,
      collected: collectedProductIds.has(p.id),
      isFavorite: collectedProductIds.has(p.id)
    }))
    recommendProducts.value = recommendProducts.value.map((p) => ({
      ...p,
      collected: collectedProductIds.has(p.id),
      isFavorite: collectedProductIds.has(p.id)
    }))
  } catch {
    favoriteIds.value = new Set()
  }
}

const handleFavoriteToggle = async (product: Product) => {
  const productId = productIdOf(product)
  if (!productId) {
    ElMessage.error('商品数据缺少ID，暂不能收藏')
    return
  }
  markFavoriteBusy(productId, true)
  try {
    if (favoriteIds.value.has(productId)) {
      await favoriteApi.cancel(productId)
      setFavorite(productId, false)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.collect(productId)
      setFavorite(productId, true)
      ElMessage.success('已收藏')
    }
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    markFavoriteBusy(productId, false)
  }
}

watch(() => auth.token, () => syncFavoriteStates())
onMounted(load)
</script>

<style scoped>
.home-page {
  padding-bottom: 18px;
}

.home-hero {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  align-items: stretch;
  gap: 28px;
}

.category-panel {
  overflow: hidden;
  border: 1px solid #dcebd7;
  border-radius: var(--yx-radius-xl);
  background:
    linear-gradient(180deg, rgba(235, 250, 231, 0.94), rgba(255, 255, 255, 0.96) 26%),
    var(--yx-card);
  box-shadow: var(--yx-shadow-card);
}

.category-title {
  height: 48px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  color: var(--yx-primary-dark);
  background: linear-gradient(90deg, rgba(220, 252, 231, 0.92), rgba(242, 251, 238, 0.68));
}

.category-title h3 {
  margin: 0;
  font-size: 16px;
}

.category-link {
  min-height: 29px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  color: #405146;
  border-bottom: 1px solid rgba(229, 238, 228, 0.86);
  transition: color 0.18s ease, background 0.18s ease;
}

.category-link:hover {
  color: var(--yx-primary-dark);
  background: rgba(240, 249, 239, 0.72);
}

.category-icon {
  width: 20px;
  height: 20px;
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  color: var(--yx-primary);
  background: rgba(220, 252, 231, 0.72);
}

.category-name {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-arrow {
  flex: 0 0 auto;
  color: #8aa58d;
  font-size: 13px;
}

.category-empty {
  padding: 24px 20px;
  color: var(--muted);
  text-align: center;
}

.home-banner-frame {
  min-width: 0;
}

.home-banner-frame :deep(.home-banner-carousel),
.home-banner-frame :deep(.banner-slide) {
  height: 286px;
}

.quick-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
  margin-top: 18px;
}

.quick-card {
  min-height: 112px;
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 20px 24px;
  border: 1px solid var(--yx-border);
  border-radius: 18px;
  background: var(--yx-card);
  box-shadow: var(--yx-shadow-soft);
  cursor: pointer;
  text-align: left;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.quick-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--yx-shadow-card);
}

.quick-icon {
  width: 66px;
  height: 66px;
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 20px;
  color: #fff;
  font-size: 34px;
}

.quick-card-coupon {
  border-color: #fde6ce;
  background: linear-gradient(135deg, rgba(255, 251, 247, 0.98), rgba(255, 243, 229, 0.92));
}

.quick-card-coupon .quick-icon {
  background: linear-gradient(135deg, #ffad3d, #ff7a1a);
}

.quick-card-new {
  border-color: #dcefd7;
  background: linear-gradient(135deg, rgba(250, 255, 249, 0.98), rgba(238, 250, 236, 0.92));
}

.quick-card-new .quick-icon {
  background: linear-gradient(135deg, #77d66f, #25a946);
}

.quick-card-season {
  border-color: #f8e2ba;
  background: linear-gradient(135deg, rgba(255, 253, 247, 0.98), rgba(255, 247, 230, 0.92));
}

.quick-card-season .quick-icon {
  background: linear-gradient(135deg, #ffc14d, #f59e0b);
}

.quick-card-pick {
  border-color: #d8e6ff;
  background: linear-gradient(135deg, rgba(249, 252, 255, 0.98), rgba(238, 245, 255, 0.92));
}

.quick-card-pick .quick-icon {
  background: linear-gradient(135deg, #74a5ff, #3b82f6);
}

.quick-copy b,
.quick-copy small,
.quick-copy em {
  display: block;
}

.quick-copy b {
  color: var(--yx-text);
  font-size: 18px;
}

.quick-copy small {
  margin-top: 4px;
  color: var(--muted);
  font-size: 14px;
}

.quick-copy em {
  width: fit-content;
  margin-top: 10px;
  padding: 4px 10px;
  border: 1px solid currentColor;
  border-radius: 999px;
  color: var(--yx-primary-dark);
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.quick-card-coupon .quick-copy em,
.quick-card-season .quick-copy em {
  color: var(--yx-orange);
}

.quick-card-pick .quick-copy em {
  color: var(--yx-blue);
}

.home-product-section {
  margin-top: 18px;
  padding: 18px 20px 20px;
  border: 1px solid rgba(229, 238, 228, 0.9);
  border-radius: var(--yx-radius-xl);
  background:
    linear-gradient(180deg, rgba(248, 253, 247, 0.98), rgba(255, 255, 255, 0.96) 36%),
    var(--yx-card);
  box-shadow: var(--yx-shadow-soft);
}

.home-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.home-section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-leaf {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: var(--yx-primary-dark);
  background: var(--yx-primary-soft);
}

.home-section-title h2 {
  margin: 0;
  color: var(--yx-text);
  font-size: 22px;
}

.home-section-title p {
  margin: 4px 0 0;
  color: var(--yx-muted);
  font-size: 14px;
}

.section-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--yx-text-soft);
  font-size: 14px;
}

.section-more:hover {
  color: var(--yx-primary-dark);
}

.home-product-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 18px;
}

.home-product-grid :deep(.product-card) {
  box-shadow: 0 8px 22px rgba(31, 41, 55, 0.06);
}

.home-product-grid :deep(.product-cover) {
  height: 176px;
  aspect-ratio: auto;
  object-fit: contain;
  padding: 10px;
  background: linear-gradient(135deg, #f3fbef, #fff8ed);
}

@media (max-width: 900px) {
  .home-hero {
    grid-template-columns: 1fr;
  }

  .quick-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1280px) {
  .home-product-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .home-product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quick-card {
    padding: 18px;
  }

  .home-banner-frame :deep(.home-banner-carousel),
  .home-banner-frame :deep(.banner-slide) {
    height: 280px;
  }
}

@media (max-width: 560px) {
  .quick-row,
  .home-product-grid {
    grid-template-columns: 1fr;
  }

  .home-product-section {
    padding: 16px;
  }

  .home-section-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
