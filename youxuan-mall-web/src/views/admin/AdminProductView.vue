<template>
  <section class="admin-page">
    <div class="admin-toolbar glass-card">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openCreate">新增商品</el-button>
    </div>
    <div class="admin-table-card glass-card">
      <el-table :data="products" row-key="id">
        <el-table-column label="图片" width="96">
          <template #default="{ row }">
            <img class="thumb" :src="getImageUrl(row)" :alt="row.name" @error="setImageFallback" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="categoryName" label="分类" width="130" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="230">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="primary" @click="toggle(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button text type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" layout="prev, pager, next" :total="total" :page-size="query.pageSize" @current-change="changePage" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑商品' : '新增商品'" width="680px">
      <el-form label-position="top">
        <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="item in categories" :key="item.id" :label="categoryName(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主图 URL">
          <div class="image-line">
            <el-input v-model="form.mainImage" />
            <input type="file" accept="image/*" @change="upload" />
          </div>
        </el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <div class="form-grid">
          <el-form-item label="售价"><el-input-number v-model="form.price" :min="0.01" :precision="2" /></el-form-item>
          <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" :disabled="Boolean(form.id)" /></el-form-item>
          <el-form-item label="状态"><el-switch v-model="enabled" active-text="上架" inactive-text="下架" /></el-form-item>
        </div>
        <el-form-item label="详情"><el-input v-model="form.detail" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fileApi, productApi } from '@/api/modules'
import type { Category, Product } from '@/types'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProducts } from '@/utils/product'

const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const dialog = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10 })
const empty = () => ({ id: 0, categoryId: undefined as number | undefined, name: '', subtitle: '', mainImage: '', detail: '', price: 0, originalPrice: 0, sales: 0, status: 1, stock: 0 })
const form = reactive(empty())
const enabled = computed({ get: () => form.status === 1, set: (value: boolean) => { form.status = value ? 1 : 0 } })
const categoryName = (item: Category) => item.name || item.categoryName || '未命名分类'

const load = async () => {
  const data = await productApi.page(query)
  products.value = normalizeProducts(data.records || [])
  total.value = data.total || 0
}
const changePage = async (page: number) => {
  query.pageNum = page
  await load()
}
const openCreate = () => {
  Object.assign(form, empty())
  dialog.value = true
}
const openEdit = (row: Product) => {
  Object.assign(form, { ...empty(), ...row })
  dialog.value = true
}
const save = async () => {
  if (!form.categoryId) return ElMessage.warning('请选择分类')
  const payload = { ...form, categoryId: form.categoryId }
  if (form.id) await productApi.update(form.id, payload)
  else await productApi.create({ ...payload, stock: form.stock })
  dialog.value = false
  await load()
}
const toggle = async (row: Product) => {
  if (row.status === 1) await productApi.down(row.id)
  else await productApi.up(row.id)
  await load()
}
const remove = async (id: number) => {
  await productApi.remove(id)
  await load()
}
const upload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const res = await fileApi.uploadProductImage(file)
  form.mainImage = res.url || res.fileUrl || ''
}

onMounted(async () => {
  categories.value = await productApi.categories()
  await load()
})
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

.thumb {
  width: 62px;
  height: 50px;
  object-fit: cover;
  border-radius: 12px;
}

.pager {
  justify-content: center;
  margin-top: 18px;
}

.image-line {
  display: grid;
  grid-template-columns: 1fr 210px;
  gap: 10px;
  align-items: center;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
</style>
