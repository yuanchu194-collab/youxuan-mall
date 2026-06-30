<template>
  <section class="admin-page product-admin-page">
    <section class="admin-filter-card product-filter-card">
      <div class="filter-fields">
        <label>
          <span>商品名称</span>
          <el-input v-model="query.name" placeholder="请输入商品名称" clearable :suffix-icon="Search" @keyup.enter="search" />
        </label>
        <label>
          <span>分类</span>
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="item in categories" :key="item.id" :label="categoryName(item)" :value="item.id" />
          </el-select>
        </label>
        <label>
          <span>状态</span>
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </label>
        <label>
          <span>价格区间</span>
          <el-input v-model="query.priceRange" placeholder="最低价      ~      最高价" clearable @keyup.enter="search" />
        </label>
      </div>
      <div class="filter-actions">
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        <el-button type="primary" :icon="Plus" class="create-button" @click="openCreate">新增商品</el-button>
      </div>
    </section>

    <div class="admin-table-card product-table-card" v-loading="loading">
      <el-table :data="filteredProducts" row-key="id">
        <el-table-column label="商品" min-width="330">
          <template #default="{ row }">
            <div class="product-cell">
              <img class="thumb" :src="getImageUrl(row)" :alt="row.name" @error="setImageFallback" />
              <div>
                <strong>{{ row.name }}</strong>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" min-width="150" />
        <el-table-column label="价格（元）" min-width="150">
          <template #default="{ row }">
            <span class="price-text">{{ money(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" min-width="130" />
        <el-table-column label="状态" min-width="130">
          <template #default="{ row }">
            <span :class="['admin-status-pill', row.status === 1 ? 'success' : 'muted']">{{ row.status === 1 ? '上架中' : '下架中' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <button type="button" @click="openEdit(row)">编辑</button>
              <i />
              <button type="button" @click="toggle(row)">{{ row.status === 1 ? '下架' : '上架' }}</button>
              <i />
              <button type="button" class="danger" @click="remove(row)">删除</button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="admin-empty product-empty">
            <el-icon><Goods /></el-icon>
            <strong>暂无商品</strong>
            <span>{{ products.length ? '当前筛选条件下暂无商品' : '新增商品后会展示在这里' }}</span>
            <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
          </div>
        </template>
      </el-table>
      <div class="table-footer">
        <span>共 {{ total }} 条</span>
        <el-pagination
          class="pager"
          layout="prev, pager, next, sizes"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          :page-sizes="[10, 20, 30]"
          @size-change="changeSize"
          @current-change="changePage"
        />
      </div>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑商品' : '新增商品'" width="760px" class="product-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="product-form">
        <div class="form-grid two">
          <el-form-item label="商品名称" prop="name"><el-input v-model.trim="form.name" placeholder="请输入商品名称" /></el-form-item>
          <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="item in categories" :key="item.id" :label="categoryName(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        </div>
        <el-form-item label="商品图片">
          <div class="image-line">
            <img :src="getImageUrl(form)" alt="商品预览" @error="setImageFallback" />
            <div>
              <el-input v-model="form.mainImage" placeholder="图片 URL，可通过上传自动填充" />
              <label class="upload-button">
                <input type="file" accept="image/*" @change="upload" />
                上传商品图片
              </label>
              <p>图片不是必填，缺失时前台使用占位图。</p>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="商品描述"><el-input v-model="form.subtitle" placeholder="商品副标题或卖点" /></el-form-item>
        <div class="form-grid four">
          <el-form-item label="售价" prop="price"><el-input-number v-model="form.price" :min="0.01" :precision="2" /></el-form-item>
          <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" :precision="0" :disabled="Boolean(form.id)" /></el-form-item>
          <el-form-item label="状态"><el-switch v-model="enabled" active-text="上架" inactive-text="下架" /></el-form-item>
        </div>
        <el-form-item label="详情"><el-input v-model="form.detail" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Goods, Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import { fileApi, productApi } from '@/api/modules'
import type { Category, Product } from '@/types'
import { getImageUrl, setImageFallback } from '@/utils/media'
import { normalizeProducts } from '@/utils/product'

const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const dialog = ref(false)
const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
  priceRange: ''
})
const empty = () => ({ id: 0, categoryId: undefined as number | undefined, name: '', subtitle: '', mainImage: '', detail: '', price: 0, originalPrice: 0, sales: 0, status: 1, stock: 0 })
const form = reactive(empty())
const enabled = computed({ get: () => form.status === 1, set: (value: boolean) => { form.status = value ? 1 : 0 } })
const categoryName = (item: Category) => item.name || item.categoryName || '未命名分类'

const rules: FormRules = {
  name: [{ required: true, message: '商品名称不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '分类不能为空', trigger: 'change' }],
  price: [{ type: 'number', min: 0.01, message: '价格必须大于 0', trigger: 'change' }],
  stock: [{ type: 'number', min: 0, message: '库存不能小于 0', trigger: 'change' }]
}

const priceBounds = computed(() => {
  const parts = query.priceRange
    .split(/[~～-]/)
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isFinite(item))
  return { min: parts[0], max: parts[1] }
})

const filteredProducts = computed(() =>
  products.value.filter((item) => {
    const price = Number(item.price || 0)
    if (priceBounds.value.min !== undefined && price < priceBounds.value.min) return false
    if (priceBounds.value.max !== undefined && price > priceBounds.value.max) return false
    return true
  })
)

const load = async () => {
  loading.value = true
  try {
    const data = await productApi.page({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      name: query.name || undefined,
      categoryId: query.categoryId,
      status: query.status
    })
    products.value = normalizeProducts(data.records || [])
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
const search = async () => {
  query.pageNum = 1
  await load()
}
const resetQuery = async () => {
  Object.assign(query, {
    pageNum: 1,
    pageSize: 10,
    name: '',
    categoryId: undefined,
    status: undefined,
    priceRange: ''
  })
  await load()
}
const changePage = async (page: number) => {
  query.pageNum = page
  await load()
}
const changeSize = async (size: number) => {
  query.pageSize = size
  query.pageNum = 1
  await load()
}
const openCreate = () => {
  Object.assign(form, empty())
  formRef.value?.clearValidate()
  dialog.value = true
}
const openEdit = (row: Product) => {
  Object.assign(form, { ...empty(), ...row })
  formRef.value?.clearValidate()
  dialog.value = true
}
const save = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  if (!form.categoryId) return ElMessage.warning('请选择分类')
  saving.value = true
  try {
    const payload = { ...form, categoryId: form.categoryId }
    if (form.id) {
      await productApi.update(form.id, payload)
      ElMessage.success('商品已更新')
    } else {
      await productApi.create({ ...payload, stock: form.stock })
      ElMessage.success('商品已新增')
    }
    dialog.value = false
    await load()
  } finally {
    saving.value = false
  }
}
const toggle = async (row: Product) => {
  const action = row.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确认${action}商品「${row.name}」吗？`, `${action}商品`, {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  if (row.status === 1) await productApi.down(row.id)
  else await productApi.up(row.id)
  ElMessage.success(`商品已${action}`)
  await load()
}
const remove = async (row: Product) => {
  try {
    await ElMessageBox.confirm(`确认删除商品「${row.name}」吗？删除后不可恢复。`, '删除商品', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  await productApi.remove(row.id)
  ElMessage.success('商品已删除')
  await load()
}
const upload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const res = await fileApi.uploadProductImage(file)
  form.mainImage = res.url || res.fileUrl || ''
  input.value = ''
}
const money = (value?: number) => Number(value || 0).toFixed(2)

onMounted(async () => {
  categories.value = await productApi.categories()
  await load()
})
</script>

<style scoped>
.product-filter-card {
  min-height: 116px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 22px 28px;
}

.filter-fields {
  min-width: 0;
  flex: 1;
  display: grid;
  grid-template-columns: minmax(210px, 1.2fr) minmax(170px, 1fr) minmax(170px, 1fr) minmax(190px, 1fr);
  gap: 28px;
}

.filter-fields label,
.filter-fields span {
  display: block;
}

.filter-fields label > span {
  margin-bottom: 12px;
  color: #26342c;
  font-size: 15px;
  font-weight: 700;
}

.filter-fields :deep(.el-input__wrapper),
.filter-fields :deep(.el-select__wrapper) {
  width: 100%;
  min-height: 42px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dfe7df inset;
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  gap: 18px;
  padding-top: 26px;
}

.filter-actions :deep(.el-button) {
  min-height: 42px;
  padding: 0 22px;
  border-radius: 8px;
  font-weight: 800;
}

.filter-actions .create-button {
  margin-left: 20px;
  padding: 0 24px;
}

.product-table-card {
  border-radius: 14px;
}

.product-cell {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  align-items: center;
  gap: 18px;
}

.thumb {
  width: 76px;
  height: 56px;
  object-fit: cover;
  border: 1px solid #eef2ed;
  border-radius: 9px;
  background: #f7faf7;
}

.product-cell strong,
.product-cell span {
  display: block;
}

.product-cell strong {
  overflow: hidden;
  color: #17231b;
  font-size: 16px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-text {
  color: #17231b;
  font-weight: 500;
}

.admin-status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 58px;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
}

.admin-status-pill.success {
  color: #16813a;
  background: #e5f7e5;
  box-shadow: 0 0 0 1px #c8edc9 inset;
}

.admin-status-pill.muted {
  color: #6c7880;
  background: #edf1f2;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.table-actions button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #1e87e8;
  font-weight: 700;
  cursor: pointer;
}

.table-actions i {
  width: 1px;
  height: 14px;
  background: #d9e0dc;
}

.table-actions .danger {
  color: #ef4444;
}

.product-empty .el-icon {
  color: #16813a;
  font-size: 42px;
}

.table-footer {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 24px;
  border-top: 1px solid #edf2ec;
  color: #647269;
}

.image-line {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.image-line img {
  width: 108px;
  height: 88px;
  object-fit: cover;
  border: 1px solid #e5eee4;
  border-radius: 10px;
  background: #f7faf7;
}

.image-line p {
  margin: 8px 0 0;
  color: #8a978f;
  font-size: 12px;
}

.upload-button {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  margin-top: 10px;
  padding: 0 12px;
  border-radius: 8px;
  color: #16813a;
  font-weight: 800;
  background: #edf8ec;
  cursor: pointer;
}

.upload-button input {
  display: none;
}

.form-grid {
  display: grid;
  gap: 12px;
}

.form-grid.two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-grid.four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.product-form :deep(.el-select),
.product-form :deep(.el-input-number) {
  width: 100%;
}

@media (max-width: 1280px) {
  .filter-fields {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .product-filter-card {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-actions {
    align-items: center;
  }
}

@media (max-width: 900px) {
  .form-grid.two,
  .form-grid.four {
    grid-template-columns: 1fr;
  }

  .table-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-fields {
    grid-template-columns: 1fr;
  }
}
</style>
