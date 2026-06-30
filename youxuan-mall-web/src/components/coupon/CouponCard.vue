<template>
  <article class="coupon-card" :class="[`tone-${coupon.tone}`, `state-${coupon.state}`]">
    <div class="coupon-value">
      <div class="amount"><small>¥</small>{{ money(coupon.amount) }}</div>
      <p>{{ thresholdText }}</p>
    </div>

    <div class="coupon-info">
      <div class="title-row">
        <h3>{{ coupon.title }}</h3>
        <span class="type-tag">{{ coupon.typeLabel }}</span>
      </div>
      <p class="date-line">有效期：{{ dateRange }}</p>
      <p class="meta-line">
        <span>{{ coupon.stockText || coupon.receiveText || coupon.scopeLabel }}</span>
        <b :class="`status-${coupon.state}`">{{ coupon.statusText }}</b>
      </p>
      <div class="minor-actions">
        <button type="button" @click="$emit('view-products', coupon)">查看可用商品</button>
        <button type="button" @click="$emit('share', coupon)">分享</button>
        <button type="button" @click="$emit('rules', coupon)">规则</button>
      </div>
    </div>

    <div class="coupon-action">
      <el-button
        class="main-action"
        :disabled="coupon.actionDisabled"
        :type="coupon.actionDisabled ? 'default' : 'primary'"
        @click="$emit('action', coupon)"
      >
        {{ coupon.actionText }}
      </el-button>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CouponCardItem } from './types'

const props = defineProps<{
  coupon: CouponCardItem
}>()

defineEmits<{
  action: [coupon: CouponCardItem]
  rules: [coupon: CouponCardItem]
  share: [coupon: CouponCardItem]
  'view-products': [coupon: CouponCardItem]
}>()

const money = (value?: number) => {
  const numberValue = Number(value || 0)
  return Number.isInteger(numberValue) ? String(numberValue) : numberValue.toFixed(2)
}

const formatDateTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '-')
const thresholdText = computed(() => (Number(props.coupon.threshold || 0) <= 0 ? '无门槛' : `满${money(props.coupon.threshold)}可用`))
const dateRange = computed(() => `${formatDateTime(props.coupon.startTime)} - ${formatDateTime(props.coupon.endTime)}`)
</script>

<style scoped>
.coupon-card {
  position: relative;
  width: min(100%, 680px);
  min-height: 176px;
  display: grid;
  grid-template-columns: 152px minmax(0, 1fr) 116px;
  overflow: hidden;
  border: 1px solid #efd9c7;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(125, 78, 31, 0.04);
}

.coupon-card::before,
.coupon-card::after {
  position: absolute;
  left: 143px;
  z-index: 2;
  width: 18px;
  height: 18px;
  border: 1px solid rgba(230, 219, 205, 0.92);
  border-radius: 999px;
  background: #fff;
  content: "";
}

.coupon-card::before {
  top: -10px;
}

.coupon-card::after {
  bottom: -10px;
}

.coupon-value {
  position: relative;
  display: grid;
  place-items: center;
  align-content: center;
  padding: 18px 12px;
  color: #fff;
  text-align: center;
}

.coupon-value::after {
  position: absolute;
  top: -8px;
  right: -7px;
  bottom: -8px;
  width: 14px;
  background: radial-gradient(circle at 50% 8px, #fff 0 6px, transparent 7px) 0 0 / 14px 18px repeat-y;
  content: "";
}

.amount {
  font-size: 46px;
  line-height: 1;
  font-weight: 900;
}

.amount small {
  margin-right: 3px;
  font-size: 21px;
  vertical-align: 14px;
}

.coupon-value p {
  margin: 10px 0 0;
  font-size: 17px;
  font-weight: 900;
}

.coupon-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 18px 18px 14px 24px;
}

.title-row {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 9px;
}

.title-row h3 {
  min-width: 0;
  margin: 0;
  overflow: hidden;
  color: #1b231e;
  font-size: 20px;
  line-height: 1.35;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-tag {
  flex: 0 0 auto;
  max-width: 112px;
  overflow: hidden;
  padding: 2px 7px;
  border: 1px solid #ffb09a;
  border-radius: 4px;
  color: #ff6035;
  font-size: 12px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #fff7f1;
}

.date-line,
.meta-line {
  margin: 11px 0 0;
  overflow: hidden;
  color: #65736a;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-line {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-line b {
  flex: 0 0 auto;
  color: #168736;
}

.meta-line .status-expiring {
  color: #f97316;
}

.meta-line .status-used,
.meta-line .status-expired,
.meta-line .status-disabled,
.meta-line .status-soldOut {
  color: #858d88;
}

.minor-actions {
  display: flex;
  gap: 14px;
  margin-top: 13px;
}

.minor-actions button {
  padding: 0;
  border: 0;
  color: #76907c;
  background: transparent;
  font-size: 12px;
  cursor: pointer;
}

.minor-actions button:hover {
  color: #168736;
}

.coupon-action {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px 18px 18px 0;
}

.main-action {
  width: 96px;
  min-height: 38px;
  border: 0;
  border-radius: 9px;
  font-size: 15px;
  font-weight: 900;
}

.main-action:not(.is-disabled) {
  background: linear-gradient(180deg, #ff7b1f, #ff4d12);
  color: #fff;
}

.main-action.is-disabled,
.main-action.is-disabled:hover {
  background: #eeeeee;
  color: #7d8580;
}

.tone-red .coupon-value {
  background: linear-gradient(145deg, #ff9469, #ff5b4b);
}

.tone-orange .coupon-value {
  background: linear-gradient(145deg, #ffba4a, #ff8b2a);
}

.tone-green .coupon-value {
  background: linear-gradient(145deg, #99de68, #48b83a);
}

.tone-blue .coupon-value {
  background: linear-gradient(145deg, #7cc7ff, #4f9df4);
}

.tone-purple .coupon-value {
  background: linear-gradient(145deg, #bd91ff, #8a6cf2);
}

.tone-teal .coupon-value {
  background: linear-gradient(145deg, #82d8ce, #49b7aa);
}

.tone-gray .coupon-value {
  background: linear-gradient(145deg, #dadfdd, #c7cfca);
}

.state-used,
.state-expired,
.state-disabled,
.state-soldOut {
  border-color: #e5e9e6;
  background: #fbfcfb;
}

.state-used .coupon-info,
.state-expired .coupon-info,
.state-disabled .coupon-info,
.state-soldOut .coupon-info {
  opacity: 0.72;
}

@media (max-width: 700px) {
  .coupon-card {
    grid-template-columns: 1fr;
  }

  .coupon-card::before,
  .coupon-card::after,
  .coupon-value::after {
    display: none;
  }

  .coupon-value {
    min-height: 106px;
  }

  .coupon-action {
    justify-content: flex-start;
    padding: 0 20px 18px;
  }
}
</style>
