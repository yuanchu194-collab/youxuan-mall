<template>
  <div class="product-list-page">
    <aside class="product-sidebar">
      <section class="side-card category-card">
        <div class="side-card-head">
          <el-icon><Menu /></el-icon>
          <span>全部商品分类</span>
        </div>
        <div class="category-menu">
          <button class="category-menu-item" :class="{ active: !query.categoryId }" type="button" @click="selectCategory()">
            <span class="category-icon"><el-icon><Goods /></el-icon></span>
            <span class="category-text">全部商品</span>
            <el-icon class="category-arrow"><ArrowRight /></el-icon>
          </button>
          <button
            v-for="(item, index) in visibleCategories"
            :key="item.id"
            class="category-menu-item"
            :class="{ active: query.categoryId === item.id }"
            type="button"
            @click="selectCategory(item.id)"
          >
            <span class="category-icon">
              <el-icon><component :is="categoryIcon(index)" /></el-icon>
            </span>
            <span class="category-text">{{ categoryName(item) }}</span>
            <el-icon class="category-arrow"><ArrowRight /></el-icon>
          </button>
          <div v-if="!visibleCategories.length" class="category-empty">暂无分类</div>
        </div>
      </section>

      <section class="side-card filter-card">
        <div class="filter-head">
          <h3>筛选条件</h3>
          <button type="button" @click="clearFilters">清空</button>
        </div>
        <div class="filter-block">
          <div class="filter-label">价格区间</div>
          <div class="price-filter">
            <el-input-number v-model="query.minPrice" :min="0" :controls="false" placeholder="最低价" />
            <span>-</span>
            <el-input-number v-model="query.maxPrice" :min="0" :controls="false" placeholder="最高价" />
            <el-button type="primary" @click="applyFilters">确定</el-button>
          </div>
        </div>
        <div class="filter-block muted-filter">
          <div class="filter-label">当前条件</div>
          <p>{{ activeCategoryName }}</p>
          <p v-if="query.keyword">搜索：{{ query.keyword }}</p>
          <p v-if="query.minPrice || query.maxPrice">价格：{{ query.minPrice || 0 }} - {{ query.maxPrice || '不限' }}</p>
        </div>
      </section>
    </aside>

    <section class="product-content">
      <section class="list-hero">
        <div class="hero-copy">
          <h1>全部商品</h1>
          <p>优选好物，品质生活每一天</p>
          <div class="hero-crumb">
            <RouterLink to="/">首页</RouterLink>
            <span>/</span>
            <span>全部商品</span>
          </div>
        </div>
        <img src="/banners/banner-fresh.png" alt="优选生鲜" />
      </section>

      <section v-if="activeCouponId" class="coupon-use-tip">
        <div>
          <el-icon><Ticket /></el-icon>
          <span>当前正在查看该优惠券可用商品。</span>
        </div>
        <small>优惠券 #{{ activeCouponId }}，最终适用结果以订单确认接口为准。</small>
        <button type="button" @click="clearCouponParam">清除优惠券筛选</button>
      </section>

      <section v-loading="loading" class="product-list-panel">
        <div v-if="couponFilterError" class="coupon-filter-error">
          <el-icon><Ticket /></el-icon>
          <span>{{ couponFilterError }}</span>
          <button type="button" @click="clearCouponParam">返回普通商品列表</button>
        </div>

        <div class="sort-bar">
          <div class="sort-tabs">
            <button
              v-for="item in sortOptions"
              :key="item.key"
              type="button"
              :class="{ active: query.sortKey === item.key }"
              @click="selectSort(item.key)"
            >
              {{ item.label }}
              <el-icon v-if="item.key === 'price'"><Sort /></el-icon>
            </button>
          </div>
          <div class="result-meta">
            <span>共 {{ total }} 件商品</span>
            <span class="view-switch active"><el-icon><Grid /></el-icon></span>
            <span class="view-switch"><el-icon><List /></el-icon></span>
          </div>
        </div>

        <div class="category-chips">
          <button type="button" :class="{ active: !query.categoryId }" @click="selectCategory()">全部</button>
          <button
            v-for="item in visibleCategories"
            :key="item.id"
            type="button"
            :class="{ active: query.categoryId === item.id }"
            @click="selectCategory(item.id)"
          >
            {{ categoryName(item) }}
          </button>
        </div>

        <div v-if="products.length" class="product-list-grid">
          <ProductCard
            v-for="product in products"
            :key="product.id"
            :product="product"
            :favorited="favoriteIds.has(product.id)"
            :favorite-busy="favoriteBusyIds.has(product.id)"
            show-sales
            @add="addCart"
            @favorite-toggle="handleFavoriteToggle"
          />
        </div>
        <div v-else class="empty-state product-empty">
          <strong>{{ couponFilterError ? '优惠券暂不可用' : '没有找到匹配商品' }}</strong>
          <span>{{ couponFilterError || '换个分类、关键词或价格区间再试试' }}</span>
        </div>

        <div class="pager-wrap">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :page-size="query.pageSize"
            :current-page="query.pageNum"
            :total="total"
            @current-change="changePage"
          />
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Apple,
  ArrowRight,
  Bowl,
  Box,
  CoffeeCup,
  ColdDrink,
  Food,
  Goods,
  Grid,
  List,
  Menu,
  MilkTea,
  ShoppingBag,
  Sort,
  Ticket
} from '@element-plus/icons-vue'
import ProductCard from '@/components/ProductCard.vue'
import { cartApi, favoriteApi, productApi, searchApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { Category, Product } from '@/types'
import { normalizeProducts, productIdOf } from '@/utils/product'

type SortKey = 'default' | 'sales' | 'price' | 'newest' | 'review'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const total = ref(0)
const loading = ref(false)
const couponFilterError = ref('')
const syncedCouponId = ref('')
const favoriteIds = ref(new Set<number>())
const favoriteBusyIds = ref(new Set<number>())
const query = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  sortKey: 'default' as SortKey,
  priceOrder: 'asc' as 'asc' | 'desc',
  pageNum: 1,
  pageSize: 12
})

const sortOptions: Array<{ key: SortKey; label: string }> = [
  { key: 'default', label: '综合排序' },
  { key: 'sales', label: '销量' },
  { key: 'price', label: '价格' },
  { key: 'newest', label: '新品' },
  { key: 'review', label: '评价' }
]
const categoryIcons = [Apple, Food, Bowl, MilkTea, CoffeeCup, Box, ColdDrink, ShoppingBag]
const dirtyNamePattern = /(\?\?|阶段|Stage|\d{8,}|1[3-9]\d{9})/i
const categoryName = (item: Category) => item.name || item.categoryName || '商品分类'
const isCleanCategory = (item: Category) => {
  const name = categoryName(item)
  return Boolean(name) && !dirtyNamePattern.test(name)
}
const visibleCategories = computed(() => categories.value.filter(isCleanCategory).slice(0, 8))
const activeCategoryName = computed(() => {
  const current = categories.value.find((item) => item.id === query.categoryId)
  return current ? categoryName(current) : '全部商品'
})
const categoryIcon = (index: number): Component => categoryIcons[index % categoryIcons.length]

const routeCategoryId = () => {
  const raw = route.query.categoryId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numberValue = Number(value)
  return value && Number.isFinite(numberValue) ? numberValue : undefined
}

const routeKeyword = () => {
  const raw = route.query.keyword
  return String(Array.isArray(raw) ? raw[0] || '' : raw || '').trim()
}

const routeCouponId = () => {
  const raw = route.query.couponId
  return String(Array.isArray(raw) ? raw[0] || '' : raw || '').trim()
}

const activeCouponId = computed(() => routeCouponId())
const rememberCouponId = (couponId: string) => {
  if (couponId) {
    window.sessionStorage.setItem('youxuan_selected_coupon_id', couponId)
  } else {
    window.sessionStorage.removeItem('youxuan_selected_coupon_id')
  }
}

const syncRoute = () => {
  query.keyword = routeKeyword()
  query.categoryId = routeCategoryId()
  syncedCouponId.value = routeCouponId()
}

const updateRoute = () =>
  router.replace({
    path: '/products',
    query: {
      keyword: query.keyword || undefined,
      categoryId: query.categoryId ? String(query.categoryId) : undefined,
      couponId: activeCouponId.value || undefined
    }
  })

const clearCouponParam = () => {
  rememberCouponId('')
  router.replace({
    path: '/products',
    query: {
      keyword: query.keyword || undefined,
      categoryId: query.categoryId ? String(query.categoryId) : undefined
    }
  })
}

const shouldUseSearch = computed(() =>
  Boolean(!activeCouponId.value && (query.keyword || query.minPrice !== undefined || query.maxPrice !== undefined || query.sortKey !== 'default'))
)

const searchPayload = () => {
  const payload: Record<string, unknown> = {
    keyword: query.keyword || undefined,
    categoryId: query.categoryId,
    minPrice: query.minPrice,
    maxPrice: query.maxPrice,
    pageNum: query.pageNum,
    pageSize: query.pageSize
  }
  if (query.sortKey === 'sales' || query.sortKey === 'review') {
    payload.sortField = 'sales'
    payload.sortOrder = 'desc'
  }
  if (query.sortKey === 'price') {
    payload.sortField = 'price'
    payload.sortOrder = query.priceOrder
  }
  if (query.sortKey === 'newest') {
    payload.sortField = 'createTime'
    payload.sortOrder = 'desc'
  }
  return payload
}

const load = async () => {
  loading.value = true
  couponFilterError.value = ''
  try {
    const data = shouldUseSearch.value
      ? await searchApi.product(searchPayload())
      : await productApi.page({
          pageNum: query.pageNum,
          pageSize: query.pageSize,
          categoryId: query.categoryId,
          keyword: query.keyword || undefined,
          status: activeCouponId.value ? 1 : undefined,
          couponId: activeCouponId.value || undefined
        })
    products.value = normalizeProducts(data.records || []).filter((product) => product.id > 0 && product.name)
    total.value = data.total || 0
    await syncFavoriteStates()
  } catch (error) {
    if (activeCouponId.value) {
      couponFilterError.value = error instanceof Error ? error.message : '优惠券可用商品加载失败'
      products.value = []
      total.value = 0
      favoriteIds.value = new Set()
      return
    }
    throw error
  } finally {
    loading.value = false
  }
}

const selectCategory = async (categoryId?: number) => {
  query.categoryId = categoryId
  query.pageNum = 1
  await updateRoute()
  await load()
}

const selectSort = async (sortKey: SortKey) => {
  if (sortKey === 'price' && query.sortKey === 'price') {
    query.priceOrder = query.priceOrder === 'asc' ? 'desc' : 'asc'
  } else {
    query.sortKey = sortKey
    query.priceOrder = 'asc'
  }
  query.pageNum = 1
  await load()
}

const applyFilters = async () => {
  query.pageNum = 1
  await load()
}

const clearFilters = async () => {
  query.minPrice = undefined
  query.maxPrice = undefined
  query.sortKey = 'default'
  query.priceOrder = 'asc'
  query.pageNum = 1
  await load()
}

const changePage = async (page: number) => {
  query.pageNum = page
  await load()
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

const ensureLoginForFavorite = () => {
  if (auth.isLogin) return true
  ElMessage.warning('请先登录后再收藏商品')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
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
  products.value = products.value.map((item) => {
    const currentId = productIdOf(item)
    return currentId === productId ? { ...item, collected: favorited, isFavorite: favorited } : item
  })
}

const syncFavoriteStates = async () => {
  const productIds = products.value.map((product) => productIdOf(product)).filter((id): id is number => Boolean(id))
  if (!auth.isLogin || !productIds.length) {
    favoriteIds.value = new Set()
    products.value = products.value.map((product) => ({ ...product, collected: false, isFavorite: false }))
    return
  }
  try {
    const collectedProductIds = new Set(await favoriteApi.checkBatch(productIds))
    favoriteIds.value = collectedProductIds
    products.value = products.value.map((product) => {
      const productId = productIdOf(product)
      const favorited = productId ? collectedProductIds.has(productId) : false
      return { ...product, collected: favorited, isFavorite: favorited }
    })
  } catch {
    favoriteIds.value = new Set()
    products.value = products.value.map((product) => ({ ...product, collected: false, isFavorite: false }))
  }
}

const handleFavoriteToggle = async (product: Product) => {
  const productId = productIdOf(product)
  console.log('[ProductListView] 收到收藏事件', {
    productId,
    product
  })
  if (!ensureLoginForFavorite()) return
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
  } finally {
    markFavoriteBusy(productId, false)
  }
}

watch(
  () => [route.query.keyword, route.query.categoryId, route.query.couponId],
  async () => {
    const nextKeyword = routeKeyword()
    const nextCategoryId = routeCategoryId()
    const nextCouponId = routeCouponId()
    if (nextKeyword === query.keyword && nextCategoryId === query.categoryId && nextCouponId === syncedCouponId.value) return
    query.keyword = nextKeyword
    query.categoryId = nextCategoryId
    syncedCouponId.value = nextCouponId
    query.pageNum = 1
    await load()
  }
)

watch(activeCouponId, rememberCouponId, { immediate: true })

watch(
  () => auth.token,
  () => syncFavoriteStates()
)

onMounted(async () => {
  syncRoute()
  categories.value = (await productApi.categories()).filter(isCleanCategory)
  await load()
})
</script>

<style scoped>
.product-list-page {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 28px;
  align-items: start;
  width: 100%;
}

.product-sidebar {
  display: grid;
  gap: 14px;
}

.side-card {
  overflow: hidden;
  border: 1px solid rgba(218, 232, 216, 0.92);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 30px rgba(38, 94, 54, 0.07);
}

.side-card-head {
  height: 54px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  color: #146b2d;
  font-size: 16px;
  font-weight: 800;
  background: linear-gradient(135deg, #eaf9e5 0%, #f7fff4 100%);
}

.category-menu {
  padding: 8px 0;
}

.category-menu-item {
  width: 100%;
  height: 42px;
  display: grid;
  grid-template-columns: 26px minmax(0, 1fr) 16px;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border: 0;
  border-bottom: 1px solid rgba(230, 238, 228, 0.72);
  color: #33483b;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.category-menu-item:last-child {
  border-bottom: 0;
}

.category-menu-item:hover,
.category-menu-item.active {
  color: var(--yx-primary-dark);
  background: #f1fbef;
}

.category-icon {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 9px;
  color: var(--yx-primary);
  background: #edf9ed;
}

.category-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-arrow {
  color: #9baaa0;
  font-size: 12px;
}

.category-empty {
  padding: 22px 18px;
  color: var(--yx-muted);
  text-align: center;
}

.filter-card {
  padding: 18px;
}

.filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.filter-head h3 {
  margin: 0;
  color: #1f2c24;
  font-size: 18px;
}

.filter-head button {
  border: 0;
  color: var(--yx-primary);
  background: transparent;
  cursor: pointer;
}

.filter-block {
  padding-top: 14px;
  border-top: 1px solid rgba(229, 238, 228, 0.86);
}

.filter-block + .filter-block {
  margin-top: 16px;
}

.filter-label {
  margin-bottom: 10px;
  color: #243529;
  font-size: 14px;
  font-weight: 800;
}

.price-filter {
  display: grid;
  grid-template-columns: 1fr 10px 1fr 54px;
  gap: 6px;
  align-items: center;
}

.price-filter span {
  color: #a2aea5;
  text-align: center;
}

.price-filter :deep(.el-input-number) {
  width: 100%;
}

.price-filter :deep(.el-input__wrapper) {
  min-height: 32px;
  padding-right: 6px;
  padding-left: 6px;
}

.price-filter :deep(.el-button) {
  min-height: 32px;
  padding: 0 10px;
  border-radius: 10px;
}

.muted-filter p {
  margin: 6px 0;
  overflow: hidden;
  color: var(--yx-muted);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-content {
  min-width: 0;
}

.list-hero {
  position: relative;
  min-height: 122px;
  overflow: hidden;
  border: 1px solid rgba(223, 235, 215, 0.88);
  border-radius: 20px;
  background: linear-gradient(100deg, #ecfae2 0%, #f9ffe9 44%, #fff6df 100%);
  box-shadow: 0 12px 30px rgba(55, 117, 63, 0.08);
}

.hero-copy {
  position: relative;
  z-index: 1;
  padding: 26px 34px;
}

.hero-copy h1 {
  margin: 0;
  color: #0f4f20;
  font-size: 28px;
  line-height: 1.1;
}

.hero-copy p {
  margin: 12px 0 14px;
  color: #2f7b40;
  font-size: 15px;
}

.hero-crumb {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--yx-primary-dark);
  font-size: 13px;
}

.list-hero img {
  position: absolute;
  top: 0;
  right: 0;
  width: 58%;
  height: 100%;
  object-fit: cover;
  object-position: right center;
  mix-blend-mode: multiply;
}

.coupon-use-tip {
  min-height: 48px;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
  padding: 10px 14px;
  border: 1px solid #cfe8ce;
  border-radius: 14px;
  background: linear-gradient(90deg, #f0fbef, #fffdf7);
  color: #31543a;
}

.coupon-use-tip div {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
}

.coupon-use-tip .el-icon {
  flex: 0 0 auto;
  color: #168736;
}

.coupon-use-tip span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-use-tip small {
  margin-left: auto;
  color: #6b7c70;
}

.coupon-use-tip button {
  flex: 0 0 auto;
  border: 0;
  color: #168736;
  background: transparent;
  font-weight: 900;
  cursor: pointer;
}

.product-list-panel {
  min-height: 520px;
  margin-top: 14px;
}

.coupon-filter-error {
  min-height: 46px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding: 10px 14px;
  border: 1px solid #f2d4bd;
  border-radius: 12px;
  background: #fff7f3;
  color: #c6531f;
}

.coupon-filter-error button {
  margin-left: auto;
  border: 0;
  color: #168736;
  background: transparent;
  font-weight: 900;
  cursor: pointer;
}

.sort-bar {
  min-height: 46px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 2px 0 0;
  border-bottom: 1px solid rgba(228, 236, 226, 0.92);
}

.sort-tabs {
  display: flex;
  align-items: center;
  gap: 18px;
}

.sort-tabs button {
  height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: 12px;
  color: #4e5d52;
  background: transparent;
  cursor: pointer;
}

.sort-tabs button.active {
  border-color: #cce9cf;
  color: var(--yx-primary-dark);
  font-weight: 800;
  background: #eff9ed;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #7d8a80;
  font-size: 13px;
}

.view-switch {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 1px solid var(--yx-border);
  border-radius: 12px;
  color: #839188;
  background: #fff;
}

.view-switch.active {
  color: var(--yx-primary);
  background: #f1fbef;
}

.category-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  padding: 14px 0 18px;
}

.category-chips button {
  min-width: 72px;
  height: 32px;
  padding: 0 18px;
  border: 1px solid var(--yx-border);
  border-radius: 12px;
  color: #526158;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(31, 41, 55, 0.03);
}

.category-chips button.active {
  border-color: transparent;
  color: #fff;
  font-weight: 800;
  background: linear-gradient(135deg, var(--yx-primary), var(--yx-primary-dark));
  box-shadow: 0 8px 18px rgba(22, 163, 74, 0.22);
}

.product-list-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}

.product-list-grid :deep(.product-card) {
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 10px 26px rgba(39, 77, 48, 0.06);
}

.product-list-grid :deep(.product-cover) {
  height: 142px;
  padding: 10px;
  object-fit: contain;
  background: linear-gradient(180deg, #fbfdf9 0%, #f3faf1 100%);
}

.product-list-grid :deep(.product-info) {
  padding: 12px 14px 13px;
}

.product-list-grid :deep(.product-name) {
  font-size: 15px;
}

.product-list-grid :deep(.product-desc) {
  margin: 6px 0 9px;
}

.product-list-grid :deep(.price) {
  font-size: 17px;
}

.product-list-grid :deep(.product-sales) {
  margin: 7px 0 0;
  color: #8b978f;
  font-size: 12px;
}

.product-list-grid :deep(.el-button.is-circle) {
  width: 30px;
  height: 30px;
  min-height: 30px;
}

.product-empty {
  min-height: 260px;
  display: grid;
  place-items: center;
  gap: 6px;
}

.product-empty strong,
.product-empty span {
  display: block;
}

.pager-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.pager-wrap :deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--yx-primary);
}

.pager-wrap :deep(.el-pagination .btn-prev),
.pager-wrap :deep(.el-pagination .btn-next),
.pager-wrap :deep(.el-pagination .el-pager li),
.pager-wrap :deep(.el-pagination .el-input__wrapper) {
  border-radius: 10px;
}

@media (max-width: 1280px) {
  .product-list-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .product-list-page {
    grid-template-columns: 1fr;
  }

  .product-sidebar {
    display: none;
  }

  .product-list-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .coupon-use-tip {
    align-items: flex-start;
    flex-direction: column;
  }

  .coupon-use-tip small {
    margin-left: 0;
  }
}
</style>
