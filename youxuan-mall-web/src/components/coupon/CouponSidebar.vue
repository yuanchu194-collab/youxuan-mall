<template>
  <aside class="coupon-sidebar">
    <section class="account-card">
      <h2>账户中心</h2>
      <nav aria-label="账户中心">
        <button v-for="item in menuItems" :key="item.label" type="button" :class="{ active: item.active }" @click="item.action">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>
    </section>

    <section class="promo-card">
      <strong>优选好物 新鲜到家</strong>
      <span>每日精选 产地直供 品质保障</span>
      <RouterLink to="/products">去逛逛</RouterLink>
      <div class="fruit-basket" aria-hidden="true">
        <i class="orange"></i>
        <i class="apple"></i>
        <i class="grape"></i>
      </div>
    </section>
  </aside>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  Coin,
  Location,
  Lock,
  Message,
  Notebook,
  Star,
  Tickets,
  User,
  Wallet
} from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  active?: 'coupons' | 'my-coupons'
}>(), {
  active: 'coupons'
})

const emit = defineEmits<{
  todo: [feature: string]
}>()

const router = useRouter()

const menuItems = [
  { label: '个人中心', icon: User, active: false, action: () => emit('todo', '个人中心') },
  { label: '我的订单', icon: Notebook, active: false, action: () => router.push('/orders') },
  { label: '我的收藏', icon: Star, active: false, action: () => emit('todo', '我的收藏') },
  { label: '收货地址', icon: Location, active: false, action: () => router.push('/addresses') },
  { label: '优惠券', icon: Tickets, active: props.active === 'coupons' || props.active === 'my-coupons', action: () => router.push(props.active === 'my-coupons' ? '/my-coupons' : '/coupons') },
  { label: '我的余额', icon: Wallet, active: false, action: () => emit('todo', '我的余额') },
  { label: '我的积分', icon: Coin, active: false, action: () => emit('todo', '我的积分') },
  { label: '消息中心', icon: Message, active: false, action: () => emit('todo', '消息中心') },
  { label: '安全设置', icon: Lock, active: false, action: () => emit('todo', '安全设置') }
]
</script>

<style scoped>
.coupon-sidebar {
  display: grid;
  gap: 18px;
}

.account-card,
.promo-card {
  border: 1px solid #dce9d8;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 30px rgba(49, 113, 58, 0.06);
}

.account-card {
  overflow: hidden;
}

.account-card h2 {
  height: 66px;
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0 24px;
  border-bottom: 1px solid #e5efe2;
  color: #0f7a2e;
  font-size: 22px;
}

.account-card nav {
  display: grid;
  gap: 6px;
  padding: 18px 16px;
}

.account-card button {
  min-height: 44px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 12px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #26352b;
  font-size: 16px;
  text-align: left;
  cursor: pointer;
}

.account-card .el-icon {
  color: #20a043;
  font-size: 19px;
}

.account-card button.active {
  background: #edf7ea;
  color: #168336;
  font-weight: 900;
}

.promo-card {
  position: relative;
  min-height: 226px;
  overflow: hidden;
  padding: 24px 22px;
  background:
    radial-gradient(circle at 86% 88%, rgba(255, 137, 48, 0.24) 0 18%, transparent 19%),
    radial-gradient(circle at 68% 74%, rgba(129, 204, 45, 0.24) 0 16%, transparent 17%),
    linear-gradient(160deg, #ecfae5 0%, #fff8dd 62%, #f7fee9 100%);
}

.promo-card strong,
.promo-card span,
.promo-card a {
  position: relative;
  z-index: 1;
}

.promo-card strong,
.promo-card span {
  display: block;
}

.promo-card strong {
  color: #0f7a2e;
  font-size: 23px;
  line-height: 1.32;
}

.promo-card span {
  margin-top: 9px;
  color: #43634b;
  font-size: 14px;
}

.promo-card a {
  display: inline-flex;
  align-items: center;
  margin-top: 20px;
  padding: 9px 20px;
  border-radius: 999px;
  background: #209c3f;
  color: #fff;
  font-weight: 900;
}

.fruit-basket {
  position: absolute;
  right: 8px;
  bottom: 8px;
  width: 164px;
  height: 104px;
}

.fruit-basket i {
  position: absolute;
  display: block;
  border-radius: 999px;
  box-shadow: inset -8px -10px 0 rgba(0, 0, 0, 0.06), 0 8px 18px rgba(88, 121, 52, 0.12);
}

.fruit-basket .orange {
  right: 8px;
  bottom: 8px;
  width: 74px;
  height: 74px;
  background: #ffa334;
}

.fruit-basket .apple {
  right: 86px;
  bottom: 3px;
  width: 58px;
  height: 58px;
  background: #f45b48;
}

.fruit-basket .grape {
  right: 74px;
  bottom: 42px;
  width: 46px;
  height: 46px;
  background: #9bd84a;
}
</style>
