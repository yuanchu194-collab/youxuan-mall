<template>
  <section>
    <div class="admin-toolbar glass-card">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="dialog = true">新增优惠券</el-button>
    </div>
    <div class="admin-table-card glass-card">
      <el-table :data="coupons" row-key="id">
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="amount" label="面额" width="110" />
        <el-table-column prop="minAmount" label="门槛" width="110" />
        <el-table-column prop="availableStock" label="剩余库存" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button text type="primary" @click="preheat(row.id)">预热库存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" layout="prev, pager, next" :total="total" :page-size="query.pageSize" @current-change="changePage" />
    </div>

    <el-dialog v-model="dialog" title="新增优惠券" width="560px">
      <el-form label-position="top">
        <el-form-item label="优惠券名称"><el-input v-model="form.name" /></el-form-item>
        <div class="form-grid">
          <el-form-item label="优惠金额"><el-input-number v-model="form.amount" :min="0.01" :precision="2" /></el-form-item>
          <el-form-item label="使用门槛"><el-input-number v-model="form.minAmount" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="总库存"><el-input-number v-model="form.totalStock" :min="1" /></el-form-item>
          <el-form-item label="剩余库存"><el-input-number v-model="form.availableStock" :min="0" /></el-form-item>
        </div>
        <el-form-item label="有效期">
          <el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss" start-placeholder="开始时间" end-placeholder="结束时间" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { couponApi } from '@/api/modules'
import type { Coupon } from '@/types'

const coupons = ref<Coupon[]>([])
const total = ref(0)
const dialog = ref(false)
const range = ref<[string, string]>()
const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ name: '', amount: 10, minAmount: 99, totalStock: 100, availableStock: 100, status: 1 })

const load = async () => {
  const data = await couponApi.page(query)
  coupons.value = data.records || []
  total.value = data.total || 0
}
const changePage = async (page: number) => {
  query.pageNum = page
  await load()
}
const save = async () => {
  if (!range.value) return ElMessage.warning('请选择有效期')
  await couponApi.create({ ...form, startTime: range.value[0], endTime: range.value[1] })
  dialog.value = false
  await load()
}
const preheat = async (id: number) => {
  await couponApi.preheat(id)
  ElMessage.success('预热成功，用户可到优惠券中心领取')
}

onMounted(load)
</script>

<style scoped>
.admin-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px;
  margin-bottom: 18px;
}

.admin-toolbar h2 {
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.pager {
  justify-content: center;
  margin-top: 18px;
}
</style>
