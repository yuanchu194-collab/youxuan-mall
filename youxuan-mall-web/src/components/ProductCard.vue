<template>
  <article class="product-card">
    <RouterLink :to="`/products/${displayProduct.id}`">
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
          <el-button circle type="primary" :icon="Plus" @click.prevent="$emit('add', displayProduct)" />
        </div>
      </div>
    </RouterLink>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import type { Product } from '@/types'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProduct } from '@/utils/product'

const props = withDefaults(defineProps<{ product: Product; showSales?: boolean }>(), {
  showSales: false
})
defineEmits<{ add: [product: Product] }>()

const displayProduct = computed(() => normalizeProduct(props.product))
const imageSrc = computed(() => getImageUrl(displayProduct.value))
const money = (value?: number) => Number(value || 0).toFixed(2)
const salesText = computed(() => {
  const sales = Number(displayProduct.value.sales || 0)
  return sales >= 10000 ? `${(sales / 10000).toFixed(1)}万+` : `${sales || 0}`
})
</script>
