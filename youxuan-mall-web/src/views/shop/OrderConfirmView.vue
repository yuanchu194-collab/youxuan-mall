<template>
  <section class="confirm-layout">
    <div class="glass-card confirm-main">
      <h2>确认订单</h2>
      <div v-if="defaultAddress" class="address-line">
        <el-tag type="success">默认地址</el-tag>
        <b>{{ defaultAddress.receiverName }}</b>
        <span>{{ addressText }}</span>
        <RouterLink to="/addresses">管理地址</RouterLink>
      </div>
      <div v-else class="address-line no-address">
        <b>暂无默认地址</b>
        <span>请先新增收货地址并设置为默认地址后再提交订单</span>
        <RouterLink to="/addresses">去设置</RouterLink>
      </div>
      <div v-for="item in confirm?.items || []" :key="item.productId" class="confirm-item">
        <img :src="getImageUrl(displayConfirmProduct(item))" :alt="displayConfirmProduct(item).name" @error="setImageFallback" />
        <div>
          <b>{{ displayConfirmProduct(item).name }}</b>
          <span>¥{{ money(item.price) }} × {{ item.quantity }}</span>
        </div>
        <strong>¥{{ money(item.totalAmount) }}</strong>
      </div>
      <el-select v-model="couponId" placeholder="选择优惠券" clearable @change="load">
        <el-option v-for="coupon in confirm?.availableCoupons || []" :key="coupon.id" :label="`${coupon.name} - ¥${coupon.amount}`" :value="coupon.id" />
      </el-select>
    </div>
    <aside class="glass-card confirm-side">
      <p>商品金额 <b>¥{{ money(confirm?.totalAmount) }}</b></p>
      <p>优惠金额 <b>- ¥{{ money(confirm?.discountAmount) }}</b></p>
      <div>实付款 <strong>¥{{ money(confirm?.payAmount) }}</strong></div>
      <el-button type="primary" size="large" :disabled="!confirm?.items?.length || !defaultAddress" @click="submit">提交订单</el-button>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addressApi, cartApi, orderApi } from '@/api/modules'
import type { Address, OrderConfirm, OrderConfirmItem } from '@/types'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProduct } from '@/utils/product'

const route = useRoute()
const router = useRouter()
const confirm = ref<OrderConfirm>()
const defaultAddress = ref<Address>()
const couponId = ref<number>()
const requestItems = ref<Array<{ productId: number; quantity: number }>>([])
const source = computed(() => String(route.query.source || (route.query.productId ? 'BUY_NOW' : 'CART')))
const addressText = computed(() => {
  const a = defaultAddress.value || confirm.value?.address
  return a ? `${a.receiverPhone} ${a.province}${a.city}${a.district}${a.detailAddress}` : '请先设置默认地址'
})

const money = (value?: number) => Number(value || 0).toFixed(2)
const displayConfirmProduct = (item: OrderConfirmItem) => normalizeProduct(item)
const buildItems = async () => {
  if (route.query.productId) {
    requestItems.value = [{ productId: Number(route.query.productId), quantity: Number(route.query.quantity || 1) }]
    return
  }
  const cartItems = await cartApi.list()
  requestItems.value = cartItems.filter((item) => item.checked === 1).map((item) => ({ productId: item.productId, quantity: item.quantity }))
}
const load = async () => {
  if (!requestItems.value.length) {
    ElMessage.warning('没有可结算商品')
    return
  }
  if (!defaultAddress.value?.id) return
  confirm.value = await orderApi.confirm({
    source: source.value,
    addressId: defaultAddress.value.id,
    couponId: couponId.value,
    items: requestItems.value
  })
}
const submit = async () => {
  if (!defaultAddress.value?.id) {
    ElMessage.warning('请先设置默认收货地址')
    return
  }
  const orderId = await orderApi.create({
    source: source.value,
    addressId: defaultAddress.value.id,
    couponId: couponId.value,
    items: requestItems.value
  })
  ElMessage.success('订单提交成功')
  await router.push('/orders')
  return orderId
}

onMounted(async () => {
  await buildItems()
  try {
    defaultAddress.value = await addressApi.default()
  } catch {
    defaultAddress.value = undefined
  }
  await load()
})
</script>

<style scoped>
.confirm-layout {
  display: grid;
  grid-template-columns: 1fr 310px;
  gap: 20px;
}

.confirm-main,
.confirm-side {
  padding: 24px;
}

.address-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  margin: 16px 0;
  border-radius: 16px;
  background: #f5faf2;
}

.address-line span {
  color: var(--muted);
  flex: 1;
}

.no-address {
  background: #fff7ed;
}

.confirm-item {
  display: grid;
  grid-template-columns: 86px 1fr auto;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  border-top: 1px solid var(--line);
}

.confirm-item img {
  width: 86px;
  height: 70px;
  object-fit: cover;
  border-radius: 14px;
}

.confirm-item span {
  display: block;
  margin-top: 8px;
  color: var(--muted);
}

.confirm-side {
  height: max-content;
  position: sticky;
  top: 96px;
}

.confirm-side p {
  display: flex;
  justify-content: space-between;
}

.confirm-side div {
  margin: 22px 0;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.confirm-side strong {
  color: #e8392f;
  font-size: 28px;
}

@media (max-width: 860px) {
  .confirm-layout {
    grid-template-columns: 1fr;
  }
}
</style>
