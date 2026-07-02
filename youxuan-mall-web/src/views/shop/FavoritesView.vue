<template>
  <div class="favorites-page">
    <CouponSidebar active="favorites" @todo="todo" />

    <section class="favorites-main">
      <header class="favorites-hero">
        <div>
          <h1>我的收藏</h1>
          <p>把常买好物放在这里，随时回到商品详情继续选购。</p>
        </div>
        <el-button type="primary" @click="$router.push('/products')">继续逛逛</el-button>
      </header>

      <section v-loading="loading" class="favorites-panel">
        <div v-if="favorites.length" class="favorite-grid">
          <article v-for="item in favorites" :key="item.productId" class="favorite-item">
            <ProductCard :product="toProduct(item)" favorited @add="addCart" @favorite="cancelFavorite" />
            <div class="favorite-meta">
              <span>{{ item.categoryName || '优选商品' }}</span>
              <span>{{ formatTime(item.collectedTime) }} 收藏</span>
            </div>
            <button type="button" class="cancel-button" @click="cancelFavorite(toProduct(item))">取消收藏</button>
          </article>
        </div>

        <div v-else-if="!loading" class="empty-state favorites-empty">
          <strong>还没有收藏商品</strong>
          <span>在商品详情、商品卡片或购物车中点击收藏后，会出现在这里。</span>
          <el-button type="primary" @click="$router.push('/products')">去收藏商品</el-button>
        </div>

        <div v-if="total > query.pageSize" class="pager-wrap">
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import CouponSidebar from '@/components/coupon/CouponSidebar.vue'
import ProductCard from '@/components/ProductCard.vue'
import { cartApi, favoriteApi } from '@/api/modules'
import type { FavoriteProduct, Product } from '@/types'
import { showBackendTodo } from '@/utils/feature'
import { normalizeProduct, productIdOf } from '@/utils/product'

const favorites = ref<FavoriteProduct[]>([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 12 })

const todo = (feature: string) => showBackendTodo(feature)
const toProduct = (item: FavoriteProduct): Product =>
  normalizeProduct({
    ...item,
    id: item.productId,
    productId: item.productId,
    sales: 0
  })

const formatTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const load = async () => {
  loading.value = true
  try {
    const data = await favoriteApi.page(query)
    favorites.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const addCart = async (product: Product) => {
  const productId = productIdOf(product)
  if (!productId) return ElMessage.error('商品数据缺少ID，暂不能加入购物车')
  await cartApi.add({ productId, quantity: 1 })
  ElMessage.success('已加入购物车')
}

const cancelFavorite = async (product: Product) => {
  const productId = productIdOf(product)
  if (!productId) return ElMessage.error('商品数据缺少ID，暂不能取消收藏')
  await favoriteApi.cancel(productId)
  ElMessage.success('已取消收藏')
  await load()
}

const changePage = async (page: number) => {
  query.pageNum = page
  await load()
}

onMounted(load)
</script>

<style scoped>
.favorites-page {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.favorites-main {
  min-width: 0;
}

.favorites-hero,
.favorites-panel {
  border: 1px solid #dce9d8;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 30px rgba(49, 113, 58, 0.06);
}

.favorites-hero {
  min-height: 110px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 28px;
  background: linear-gradient(115deg, #edf9df 0%, #fff8df 100%);
}

.favorites-hero h1 {
  margin: 0;
  color: #15351f;
  font-size: 30px;
  letter-spacing: 0;
}

.favorites-hero p {
  margin: 10px 0 0;
  color: #53665a;
}

.favorites-panel {
  min-height: 480px;
  margin-top: 16px;
  padding: 20px;
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.favorite-item {
  position: relative;
  overflow: hidden;
  border: 1px solid #e4eee1;
  border-radius: 14px;
  background: #fff;
}

.favorite-item :deep(.product-card) {
  border: 0;
  box-shadow: none;
}

.favorite-item :deep(.product-cover) {
  height: 178px;
  padding: 12px;
  object-fit: contain;
  background: linear-gradient(180deg, #fbfdf9 0%, #f3faf1 100%);
}

.favorite-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 0 14px 12px;
  color: #7b887f;
  font-size: 12px;
}

.cancel-button {
  width: calc(100% - 28px);
  height: 34px;
  margin: 0 14px 14px;
  border: 1px solid #dfe8dc;
  border-radius: 8px;
  color: #4e5f54;
  background: #fff;
  cursor: pointer;
}

.cancel-button:hover {
  border-color: #ffb7a6;
  color: #f04438;
  background: #fff7f5;
}

.favorites-empty {
  min-height: 360px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
}

.favorites-empty strong {
  color: #1f2c24;
  font-size: 22px;
}

.favorites-empty span {
  color: #7c8a80;
}

.pager-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 1180px) {
  .favorite-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .favorites-page {
    grid-template-columns: 1fr;
  }

  .favorite-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
