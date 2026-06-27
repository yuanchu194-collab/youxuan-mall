<template>
  <section>
    <div class="section-head">
      <div>
        <h2 class="section-title">优惠券中心</h2>
        <p class="section-subtitle">先由管理员预热库存，再由用户领取使用</p>
      </div>
      <RouterLink to="/my-coupons">我的优惠券</RouterLink>
    </div>

    <div v-loading="loading" class="coupon-grid">
      <article v-for="coupon in coupons" :key="coupon.id" class="coupon-card glass-card">
        <div class="coupon-amount">
          <small>¥</small>{{ money(coupon.amount) }}
        </div>
        <div class="coupon-info">
          <h3>{{ coupon.name }}</h3>
          <p>满 ¥{{ money(coupon.minAmount) }} 可用</p>
          <span>{{ coupon.startTime }} 至 {{ coupon.endTime }}</span>
          <el-tag :type="coupon.status === 1 ? 'success' : 'info'">{{ coupon.status === 1 ? '可领取' : '已停用' }}</el-tag>
          <b>剩余 {{ coupon.availableStock }} 张</b>
        </div>
        <el-button type="primary" :disabled="coupon.status !== 1 || coupon.availableStock <= 0" @click="receive(coupon)">
          {{ coupon.availableStock <= 0 ? '已抢光' : '立即领取' }}
        </el-button>
      </article>
    </div>
    <div v-if="!loading && !coupons.length" class="empty-state">暂无可领取优惠券</div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { couponApi } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'
import type { Coupon } from '@/types'

const router = useRouter()
const auth = useAuthStore()
const coupons = ref<Coupon[]>([])
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 50 })
const money = (value?: number) => Number(value || 0).toFixed(2)

const load = async () => {
  loading.value = true
  try {
    const data = await couponApi.page(query)
    coupons.value = data.records || []
  } finally {
    loading.value = false
  }
}

const receive = async (coupon: Coupon) => {
  if (!auth.isLogin) {
    await router.push({ name: 'login', query: { redirect: '/coupons' } })
    return
  }
  try {
    await couponApi.receive(coupon.id)
    ElMessage.success('领取成功，可在我的优惠券查看')
    await router.push('/my-coupons')
  } catch (error) {
    const message = error instanceof Error ? error.message : '领取失败'
    if (message.includes('重复') || message.includes('已领取')) ElMessage.warning('已领取')
    else if (message.includes('库存') || message.includes('抢光')) ElMessage.warning('已抢光')
    else if (message.includes('预热')) ElMessage.warning('优惠券未预热，请先让管理员预热库存')
  }
}

onMounted(load)
</script>

<style scoped>
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  min-height: 320px;
}

.coupon-card {
  display: grid;
  grid-template-columns: 140px 1fr auto;
  align-items: center;
  gap: 18px;
  padding: 22px;
}

.coupon-amount {
  color: #e8392f;
  font-size: 42px;
  font-weight: 900;
}

.coupon-amount small {
  font-size: 18px;
}

.coupon-info h3 {
  margin: 0 0 8px;
}

.coupon-info p,
.coupon-info span,
.coupon-info b {
  display: block;
  margin: 5px 0;
  color: var(--muted);
}

@media (max-width: 820px) {
  .coupon-grid,
  .coupon-card {
    grid-template-columns: 1fr;
  }
}
</style>
