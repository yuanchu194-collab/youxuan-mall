<template>
  <section class="cart-layout">
    <div class="cart-list glass-card">
      <div class="cart-head">
        <h2>购物车</h2>
        <el-button text type="primary" @click="toggleAll">全选/取消</el-button>
      </div>
      <div v-if="items.length">
        <div v-for="item in items" :key="cartItemId(item)" class="cart-item">
          <el-checkbox :model-value="item.checked === 1" @change="(val: unknown) => check(item, Boolean(val))" />
          <img :src="getImageUrl(displayCartProduct(item))" :alt="displayCartProduct(item).name" @error="setImageFallback" />
          <div class="cart-info">
            <b>{{ displayCartProduct(item).name }}</b>
            <span>¥{{ money(item.price) }}</span>
            <small v-if="item.invalidReason">{{ item.invalidReason }}</small>
          </div>
          <el-input-number :model-value="item.quantity" :min="1" :max="Math.max(item.stock || 99, 1)" @change="(val: number | undefined) => updateQty(item, val || 1)" />
          <el-button text type="danger" @click="remove(item)">删除</el-button>
        </div>
      </div>
      <div v-else class="empty-state">购物车为空，去首页挑选商品</div>
    </div>
    <aside class="summary glass-card">
      <h3>结算信息</h3>
      <p>已选 {{ checkedItems.length }} 件商品</p>
      <div class="summary-price">¥{{ money(total) }}</div>
      <el-button type="primary" size="large" :disabled="checkedItems.length === 0" @click="$router.push('/checkout')">去结算</el-button>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cartApi } from '@/api/modules'
import type { CartItem } from '@/types'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProduct } from '@/utils/product'

const items = ref<CartItem[]>([])
const checkedItems = computed(() => items.value.filter((item) => item.checked === 1))
const total = computed(() => checkedItems.value.reduce((sum, item) => sum + Number(item.price || 0) * item.quantity, 0))
const money = (value?: number) => Number(value || 0).toFixed(2)
const cartItemId = (item: CartItem) => item.cartItemId || item.id || 0
const displayCartProduct = (item: CartItem) => normalizeProduct(item)

const load = async () => {
  items.value = await cartApi.list()
}
const check = async (item: CartItem, checked: boolean) => {
  const id = cartItemId(item)
  if (!id) return ElMessage.error('购物车项 ID 缺失，无法勾选')
  await cartApi.check({ cartItemId: id, checked: checked ? 1 : 0 })
  await load()
}
const toggleAll = async () => {
  const next = checkedItems.value.length === items.value.length ? 0 : 1
  await cartApi.checkAll(next)
  await load()
}
const updateQty = async (item: CartItem, quantity: number) => {
  const id = cartItemId(item)
  if (!id) return ElMessage.error('购物车项 ID 缺失，无法修改数量')
  await cartApi.update({ cartItemId: id, quantity })
  await load()
}
const remove = async (item: CartItem) => {
  const id = cartItemId(item)
  if (!id) return ElMessage.error('购物车项 ID 缺失，无法删除')
  try {
    await cartApi.remove(id)
    ElMessage.success('已删除购物车商品')
    await load()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.cart-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

.cart-list,
.summary {
  padding: 24px;
}

.cart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cart-head h2,
.summary h3 {
  margin: 0 0 18px;
}

.cart-item {
  display: grid;
  grid-template-columns: auto 96px 1fr auto auto;
  align-items: center;
  gap: 18px;
  padding: 18px 0;
  border-top: 1px solid var(--line);
}

.cart-item img {
  width: 96px;
  height: 80px;
  object-fit: cover;
  border-radius: 16px;
}

.cart-info b,
.cart-info span {
  display: block;
}

.cart-info span {
  margin-top: 8px;
  color: #e8392f;
  font-weight: 800;
}

.cart-info small {
  display: block;
  margin-top: 6px;
  color: #e47b22;
}

.summary {
  height: max-content;
  position: sticky;
  top: 96px;
}

.summary-price {
  margin: 24px 0;
  color: #e8392f;
  font-size: 30px;
  font-weight: 900;
}

@media (max-width: 860px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .cart-item {
    grid-template-columns: auto 82px 1fr;
  }
}
</style>
