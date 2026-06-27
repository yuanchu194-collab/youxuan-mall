<template>
  <div>
    <section class="list-toolbar soft-panel">
      <el-input v-model="query.keyword" placeholder="搜索商品" clearable />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable>
        <el-option v-for="item in categories" :key="item.id" :label="categoryName(item)" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
    </section>
    <section v-loading="loading" class="list-result">
      <div v-if="products.length" class="product-grid">
        <ProductCard v-for="product in products" :key="product.id" :product="product" @add="addCart" />
      </div>
      <div v-else class="empty-state">没有找到匹配商品</div>
      <el-pagination
        class="pager"
        layout="prev, pager, next"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        :total="total"
        @current-change="(page: number) => { query.pageNum = page; load() }"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/ProductCard.vue'
import { cartApi, productApi, searchApi } from '@/api/modules'
import type { Category, Product } from '@/types'
import { normalizeProducts } from '@/utils/product'

const route = useRoute()
const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', categoryId: undefined as number | undefined, pageNum: 1, pageSize: 12 })
const dirtyNamePattern = /(\?\?|阶段|Stage|\d{8,}|1[3-9]\d{9})/i
const categoryName = (item: Category) => item.name || item.categoryName || '未命名分类'
const isCleanCategory = (item: Category) => {
  const name = categoryName(item)
  return Boolean(name) && !dirtyNamePattern.test(name)
}

const syncRoute = () => {
  query.keyword = String(route.query.keyword || '')
  query.categoryId = route.query.categoryId ? Number(route.query.categoryId) : undefined
}

const load = async () => {
  loading.value = true
  try {
    const data = query.keyword
      ? await searchApi.product({ ...query, sortField: 'sales', sortOrder: 'desc' })
      : await productApi.page(query)
    products.value = normalizeProducts(data.records || [])
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const addCart = async (product: Product) => {
  await cartApi.add({ productId: product.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}

watch(() => route.query, async () => {
  syncRoute()
  query.pageNum = 1
  await load()
})

onMounted(async () => {
  syncRoute()
  categories.value = (await productApi.categories()).filter(isCleanCategory)
  await load()
})
</script>

<style scoped>
.list-toolbar {
  display: grid;
  grid-template-columns: 1fr 220px auto;
  gap: 12px;
  padding: 18px;
  margin-bottom: 22px;
}

.pager {
  margin-top: 24px;
  justify-content: center;
}

.list-result {
  min-height: 360px;
}

@media (max-width: 760px) {
  .list-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
