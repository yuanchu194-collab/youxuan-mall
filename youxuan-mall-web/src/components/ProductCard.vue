<template>
  <article class="product-card">
    <RouterLink class="product-link" :to="productLink">
      <img class="product-cover" :src="imageSrc" :alt="displayProduct.name" @error="setImageFallback" />
      <div class="product-info">
        <h3 class="product-name">{{ displayProduct.name }}</h3>
        <p class="product-desc">{{ displayProduct.subtitle || displayProduct.categoryName || '优选好物，品质严选' }}</p>
        <div class="price-row">
          <div>
            <span class="price">¥{{ money(displayProduct.price) }}</span>
            <span v-if="displayProduct.originalPrice" class="old-price">¥{{ money(displayProduct.originalPrice) }}</span>
            <p v-if="props.showSales" class="product-sales">已售 {{ salesText }}</p>
          </div>
          <el-button circle type="primary" :icon="Plus" :disabled="!productId" @click.stop.prevent="$emit('add', displayProduct)" />
        </div>
      </div>
    </RouterLink>
    <button
      type="button"
      class="favorite-button"
      :class="{ active: isFavorited }"
      :disabled="props.favoriteBusy || !productId"
      :title="isFavorited ? '取消收藏' : '收藏商品'"
      @pointerdown.stop
      @mousedown.stop
      @click.stop.prevent="handleFavoriteToggle"
    >
      <el-icon><component :is="isFavorited ? StarFilled : Star" /></el-icon>
    </button>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Plus, Star, StarFilled } from '@element-plus/icons-vue'
import type { Product } from '@/types'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProduct, productIdOf } from '@/utils/product'

const props = withDefaults(defineProps<{ product: Product; showSales?: boolean; favorited?: boolean; favoriteBusy?: boolean }>(), {
  showSales: false,
  favorited: false,
  favoriteBusy: false
})
const emit = defineEmits<{ add: [product: Product]; favorite: [product: Product]; 'favorite-toggle': [product: Product] }>()

const displayProduct = computed(() => normalizeProduct(props.product))
const productId = computed(() => productIdOf(displayProduct.value))
const productLink = computed(() => (productId.value ? `/products/${productId.value}` : '/products'))
const imageSrc = computed(() => getImageUrl(displayProduct.value))
const isFavorited = computed(() => props.favorited || Boolean(displayProduct.value.collected || displayProduct.value.isFavorite))
const money = (value?: number) => Number(value || 0).toFixed(2)
const salesText = computed(() => {
  const sales = Number(displayProduct.value.sales || 0)
  return sales >= 10000 ? `${(sales / 10000).toFixed(1)}万+` : `${sales || 0}`
})
const handleFavoriteToggle = () => {
  console.log('[ProductCard] 收藏按钮被点击', {
    productId: productId.value,
    product: displayProduct.value
  })
  emit('favorite-toggle', displayProduct.value)
  emit('favorite', displayProduct.value)
}
</script>

<style scoped>
.product-card {
  position: relative;
  isolation: isolate;
}

.product-link {
  position: relative;
  z-index: 1;
  display: block;
}

.favorite-button {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 30;
  pointer-events: auto;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(217, 229, 214, 0.96);
  border-radius: 50%;
  color: #7b8a80;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 8px 18px rgba(31, 41, 55, 0.08);
  cursor: pointer;
}

.favorite-button:hover,
.favorite-button.active {
  border-color: #ffb7a6;
  color: #f04438;
  background: #fff7f5;
}

.favorite-button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}
</style>
