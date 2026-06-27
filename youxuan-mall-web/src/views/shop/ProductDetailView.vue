<template>
  <section v-if="product" class="detail-card glass-card">
    <img class="detail-image" :src="getImageUrl(product)" :alt="product.name" @error="setImageFallback" />
    <div class="detail-info">
      <el-tag type="success">{{ product.categoryName || '优选商品' }}</el-tag>
      <h1>{{ product.name }}</h1>
      <p>{{ product.subtitle || '品质严选，安心到家' }}</p>
      <div class="detail-price">
        ¥{{ money(product.price) }}
        <span v-if="product.originalPrice">¥{{ money(product.originalPrice) }}</span>
      </div>
      <div class="meta-row">
        <span>销量 {{ product.sales || 0 }}</span>
        <span>库存 {{ product.stock || 0 }}</span>
        <span>{{ product.status === 1 ? '已上架' : '已下架' }}</span>
      </div>
      <el-input-number v-model="quantity" :min="1" :max="Math.max(product.stock || 1, 1)" />
      <div class="actions">
        <el-button size="large" @click="addCart">加入购物车</el-button>
        <el-button size="large" type="primary" @click="buyNow">立即购买</el-button>
      </div>
      <div class="detail-text">{{ product.detail || '暂无商品详情。' }}</div>
    </div>
  </section>
  <div v-else class="empty-state">商品加载中</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartApi, productApi } from '@/api/modules'
import type { Product } from '@/types'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProduct } from '@/utils/product'

const route = useRoute()
const router = useRouter()
const product = ref<Product>()
const quantity = ref(1)

const money = (value?: number) => Number(value || 0).toFixed(2)
const addCart = async () => {
  if (!product.value) return
  await cartApi.add({ productId: product.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}
const buyNow = () => {
  if (!product.value) return
  router.push({ path: '/checkout', query: { productId: product.value.id, quantity: quantity.value, source: 'BUY_NOW' } })
}

onMounted(async () => {
  product.value = normalizeProduct(await productApi.detail(Number(route.params.id)))
})
</script>

<style scoped>
.detail-card {
  display: grid;
  grid-template-columns: 48% 1fr;
  gap: 34px;
  padding: 28px;
}

.detail-image {
  width: 100%;
  aspect-ratio: 1.05;
  border-radius: 22px;
  object-fit: cover;
  background: #eef8e9;
}

.detail-info h1 {
  margin: 18px 0 12px;
  font-size: 34px;
}

.detail-info p {
  color: var(--muted);
}

.detail-price {
  margin: 22px 0;
  color: #e8392f;
  font-size: 34px;
  font-weight: 900;
}

.detail-price span {
  margin-left: 12px;
  color: #a4ada6;
  font-size: 16px;
  text-decoration: line-through;
}

.meta-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  color: var(--muted);
}

.meta-row span {
  padding: 8px 12px;
  border-radius: 999px;
  background: #f3f7f1;
}

.actions {
  display: flex;
  gap: 12px;
  margin: 22px 0;
}

.detail-text {
  padding-top: 20px;
  color: #47594d;
  line-height: 1.8;
  border-top: 1px solid var(--line);
}

@media (max-width: 820px) {
  .detail-card {
    grid-template-columns: 1fr;
  }
}
</style>
