import productPlaceholder from '@/assets/product-placeholder.svg'
import { normalizeProduct } from './product'
import type { Product } from '@/types'

type ImageLike = {
  mainImage?: string
  main_image?: string
  imageUrl?: string
  productImage?: string
  productName?: string
  productId?: number
}

export const getImageUrl = (item?: ImageLike | null) => {
  if (!item) return productPlaceholder
  return normalizeProduct(item as Partial<Product>).mainImage || productPlaceholder
}

export const setImageFallback = (event: Event) => {
  const image = event.target as HTMLImageElement
  if (image.src !== productPlaceholder) {
    image.src = productPlaceholder
  }
}
