<template>
  <section class="address-page">
    <div class="address-list">
      <div class="section-head">
        <div>
          <h2 class="section-title">收货地址</h2>
          <p class="section-subtitle">下单时会写入订单地址快照</p>
        </div>
      </div>
      <div v-if="addresses.length" class="address-grid">
        <article v-for="item in addresses" :key="item.id" class="address-card glass-card">
          <el-tag v-if="item.defaultFlag === 1" type="success">默认地址</el-tag>
          <h3>{{ item.receiverName }} <span>{{ item.receiverPhone }}</span></h3>
          <p>{{ item.province }} {{ item.city }} {{ item.district }} {{ item.detailAddress }}</p>
          <div>
            <el-button text type="primary" @click="edit(item)">编辑</el-button>
            <el-button text type="primary" @click="setDefault(item.id)">设为默认</el-button>
            <el-button text type="danger" @click="remove(item.id)">删除</el-button>
          </div>
        </article>
      </div>
      <div v-else class="empty-state">暂无地址，请先新增收货地址</div>
    </div>
    <aside class="address-form glass-card">
      <h3>{{ form.id ? '编辑地址' : '新增地址' }}</h3>
      <el-form label-position="top">
        <el-form-item label="收货人"><el-input v-model="form.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.receiverPhone" /></el-form-item>
        <el-form-item label="省市区">
          <div class="three-inputs">
            <el-input v-model="form.province" placeholder="省" />
            <el-input v-model="form.city" placeholder="市" />
            <el-input v-model="form.district" placeholder="区" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detailAddress" /></el-form-item>
        <el-checkbox v-model="isDefault">设为默认地址</el-checkbox>
        <el-button type="primary" class="save-btn" @click="save">保存地址</el-button>
      </el-form>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { addressApi } from '@/api/modules'
import type { Address } from '@/types'

const emptyForm = () => ({ id: 0, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', defaultFlag: 0 })
const addresses = ref<Address[]>([])
const form = reactive<Address>(emptyForm())
const isDefault = computed({ get: () => form.defaultFlag === 1, set: (value: boolean) => { form.defaultFlag = value ? 1 : 0 } })

const load = async () => {
  addresses.value = await addressApi.list()
}
const reset = () => Object.assign(form, emptyForm())
const edit = (item: Address) => Object.assign(form, item)
const save = async () => {
  const data = { ...form, defaultFlag: form.defaultFlag ? 1 : 0 }
  if (form.id) await addressApi.update(form.id, data)
  else await addressApi.create(data)
  reset()
  await load()
}
const remove = async (id: number) => {
  await addressApi.remove(id)
  await load()
}
const setDefault = async (id: number) => {
  await addressApi.setDefault(id)
  await load()
}

onMounted(load)
</script>

<style scoped>
.address-page {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
}

.address-grid {
  display: grid;
  gap: 14px;
}

.address-card,
.address-form {
  padding: 22px;
}

.address-card h3 {
  margin: 12px 0 8px;
}

.address-card h3 span {
  margin-left: 12px;
  color: var(--muted);
  font-size: 14px;
}

.address-card p {
  color: #4a5e50;
}

.three-inputs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.save-btn {
  width: 100%;
  margin-top: 18px;
}

@media (max-width: 860px) {
  .address-page {
    grid-template-columns: 1fr;
  }
}
</style>
