import type { Product } from '@/types'

type ProductInput = Partial<Product> & Record<string, unknown>

const toNumber = (value: unknown, fallback = 0) => {
  if (value === null || value === undefined || value === '') return fallback
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : fallback
}

const toOptionalNumber = (value: unknown) => {
  if (value === null || value === undefined || value === '') return undefined
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : undefined
}

const toText = (value: unknown) => (value === null || value === undefined ? '' : String(value).trim())

export const normalizeProduct = <T extends object>(product: T | null | undefined): T & Product => {
  const raw = (product || {}) as T & ProductInput
  const name = toText(raw.name ?? raw.productName ?? raw.title) || '优选商品'
  return {
    ...raw,
    id: toNumber(raw.id ?? raw.productId),
    categoryId: toNumber(raw.categoryId),
    name,
    subtitle: toText(raw.subtitle ?? raw.productSubtitle ?? raw.description),
    mainImage: toText(raw.mainImage ?? raw.main_image ?? raw.imageUrl ?? raw.productImage),
    price: toNumber(raw.price ?? raw.productPrice),
    originalPrice: toOptionalNumber(raw.originalPrice ?? raw.original_price),
    sales: toNumber(raw.sales),
    stock: toOptionalNumber(raw.stock),
    status: toNumber(raw.status, 1)
  }
}

export const normalizeProducts = <T extends object>(products: T[] = []) => products.map(normalizeProduct)

export const productIdOf = (product: Partial<Product> | null | undefined) => {
  const id = toNumber(product?.id ?? product?.productId)
  return id > 0 ? id : undefined
}
