<template>
  <nav class="coupon-tabs" aria-label="优惠券筛选">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      :class="{ active: modelValue === tab.key }"
      @click="$emit('update:modelValue', tab.key)"
    >
      {{ tab.label }} <span>({{ tab.count }})</span>
    </button>
  </nav>
</template>

<script setup lang="ts">
import type { CouponTabItem } from './types'

defineProps<{
  tabs: CouponTabItem[]
  modelValue: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<style scoped>
.coupon-tabs {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 24px;
}

.coupon-tabs button {
  min-height: 40px;
  padding: 0 22px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #24362b;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.16s ease, color 0.16s ease, box-shadow 0.16s ease;
}

.coupon-tabs button:hover {
  color: #138633;
  background: #eff9ed;
}

.coupon-tabs button.active {
  color: #fff;
  background: linear-gradient(180deg, #2fac49, #168736);
  box-shadow: 0 10px 18px rgba(22, 135, 54, 0.2);
}

.coupon-tabs span {
  font-size: 13px;
  font-weight: 700;
}
</style>
