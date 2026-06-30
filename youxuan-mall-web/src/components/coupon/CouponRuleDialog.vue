<template>
  <el-dialog :model-value="modelValue" title="优惠券使用规则" width="460px" @update:model-value="$emit('update:modelValue', $event)">
    <div v-if="coupon" class="rule-dialog">
      <h3>{{ coupon.title }}</h3>
      <p>{{ coupon.typeLabel }} · {{ coupon.scopeLabel }}</p>
      <ul>
        <li v-for="rule in coupon.rules" :key="rule">{{ rule }}</li>
      </ul>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import type { CouponCardItem } from './types'

defineProps<{
  modelValue: boolean
  coupon: CouponCardItem | null
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<style scoped>
.rule-dialog h3 {
  margin: 0;
  color: #142117;
  font-size: 20px;
}

.rule-dialog p {
  margin: 8px 0 14px;
  color: #168736;
  font-weight: 800;
}

.rule-dialog ul {
  margin: 0;
  padding-left: 18px;
  color: #5d6c63;
  line-height: 1.9;
}
</style>
