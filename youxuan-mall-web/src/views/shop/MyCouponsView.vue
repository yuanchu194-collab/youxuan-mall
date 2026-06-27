<template>
  <section>
    <div class="section-head">
      <div>
        <h2 class="section-title">我的优惠券</h2>
        <p class="section-subtitle">查看已领取优惠券和使用状态</p>
      </div>
      <RouterLink to="/coupons">去领券</RouterLink>
    </div>

    <el-tabs v-model="activeStatus">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="未使用" name="0" />
      <el-tab-pane label="已使用" name="1" />
      <el-tab-pane label="已过期" name="2" />
    </el-tabs>

    <div v-if="filteredCoupons.length" class="my-coupon-list">
      <article v-for="coupon in filteredCoupons" :key="coupon.userCouponId" class="my-coupon-card glass-card">
        <div class="coupon-amount">¥{{ money(coupon.amount) }}</div>
        <div>
          <h3>{{ coupon.couponName }}</h3>
          <p>满 ¥{{ money(coupon.minAmount) }} 可用</p>
          <span>领取时间：{{ coupon.receiveTime }}</span>
          <span>有效期：{{ coupon.startTime }} 至 {{ coupon.endTime }}</span>
        </div>
        <el-tag :type="statusType(coupon.status)">{{ statusText(coupon.status) }}</el-tag>
      </article>
    </div>
    <div v-else class="empty-state">暂无优惠券</div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { couponApi } from '@/api/modules'
import type { UserCoupon } from '@/types'

const coupons = ref<UserCoupon[]>([])
const activeStatus = ref('all')
const filteredCoupons = computed(() =>
  activeStatus.value === 'all' ? coupons.value : coupons.value.filter((item) => String(item.status) === activeStatus.value)
)
const money = (value?: number) => Number(value || 0).toFixed(2)
const statusMap: Record<number, string> = { 0: '未使用', 1: '已使用', 2: '已过期' }
const statusText = (status: number) => statusMap[status] || '未知'
const statusType = (status: number) => (status === 0 ? 'success' : status === 1 ? 'info' : 'warning')

onMounted(async () => {
  coupons.value = await couponApi.my()
})
</script>

<style scoped>
.my-coupon-list {
  display: grid;
  gap: 14px;
}

.my-coupon-card {
  display: grid;
  grid-template-columns: 130px 1fr auto;
  align-items: center;
  gap: 18px;
  padding: 20px;
}

.coupon-amount {
  color: #e8392f;
  font-size: 34px;
  font-weight: 900;
}

.my-coupon-card h3 {
  margin: 0 0 8px;
}

.my-coupon-card p,
.my-coupon-card span {
  display: block;
  margin: 5px 0;
  color: var(--muted);
}

@media (max-width: 760px) {
  .my-coupon-card {
    grid-template-columns: 1fr;
  }
}
</style>
