import http from './http'
import type {
  Address,
  Banner,
  CartItem,
  Category,
  Coupon,
  HomeIndex,
  FavoriteProduct,
  LoginResult,
  Order,
  OrderConfirm,
  PageResult,
  Product,
  ProductReview,
  ProductReviewSummary,
  UserCoupon,
  User
} from '@/types'

export const authApi = {
  login: (data: { username: string; password: string }) => http.post<unknown, LoginResult>('/api/user/login', data),
  register: (data: { username: string; password: string; nickname?: string; phone?: string }) =>
    http.post<unknown, void>('/api/user/register', data),
  me: () => http.get<unknown, User>('/api/user/me')
}

export const homeApi = {
  index: () => http.get<unknown, HomeIndex>('/api/home/index')
}

export const productApi = {
  categories: () => http.get<unknown, Category[]>('/api/product/category/list'),
  createCategory: (data: { name: string; parentId?: number; sort?: number; status?: number }) =>
    http.post<unknown, number>('/api/product/category', data),
  page: (params: { pageNum: number; pageSize: number; categoryId?: number; keyword?: string; name?: string; status?: number; couponId?: number | string }) =>
    http.get<unknown, PageResult<Product>>('/api/product/page', { params: { ...params, name: params.name || params.keyword } }),
  detail: (id: number) => http.get<unknown, Product>(`/api/product/${id}`),
  hot: () => http.get<unknown, Product[]>('/api/product/home/hot'),
  create: (data: Partial<Product> & { stock: number }) => http.post<unknown, number>('/api/product', data),
  update: (id: number, data: Partial<Product>) => http.put<unknown, void>(`/api/product/${id}`, data),
  remove: (id: number) => http.delete<unknown, void>(`/api/product/${id}`),
  up: (id: number) => http.put<unknown, void>(`/api/product/${id}/up`),
  down: (id: number) => http.put<unknown, void>(`/api/product/${id}/down`)
}

export const searchApi = {
  product: (data: Record<string, unknown>) => http.post<unknown, PageResult<Product>>('/api/search/product', data)
}

export const fileApi = {
  uploadProductImage: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return http.post<unknown, { url: string; fileUrl?: string }>('/api/file/product/image', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const cartApi = {
  add: (data: { productId: number; quantity: number }) => http.post<unknown, void>('/api/cart/add', data),
  list: () => http.get<unknown, CartItem[]>('/api/cart/list'),
  update: (data: { cartItemId: number; quantity: number }) => http.put<unknown, void>('/api/cart/update', data),
  check: (data: { cartItemId: number; checked: number }) => http.put<unknown, void>('/api/cart/check', data),
  checkAll: (checked: number) => http.put<unknown, void>('/api/cart/checkAll', { checked }),
  remove: (id: number) => http.delete<unknown, void>(`/api/cart/${id}`),
  removeChecked: () => http.delete<unknown, void>('/api/cart/checked')
}

export const favoriteApi = {
  collect: (productId: number) => http.post<unknown, void>(`/api/favorites/${productId}`),
  cancel: (productId: number) => http.delete<unknown, void>(`/api/favorites/${productId}`),
  page: (params: { pageNum: number; pageSize: number }) =>
    http.get<unknown, PageResult<FavoriteProduct>>('/api/favorites/page', { params }),
  check: (productId: number) => http.get<unknown, boolean>(`/api/favorites/check/${productId}`),
  checkBatch: (productIds: number[]) =>
    http.get<unknown, number[]>('/api/favorites/check/batch', { params: { productIds: productIds.join(',') } })
}

export const reviewApi = {
  page: (productId: number, params: { pageNum: number; pageSize: number }) =>
    http.get<unknown, PageResult<ProductReview>>(`/api/product/${productId}/reviews/page`, { params }),
  summary: (productId: number) => http.get<unknown, ProductReviewSummary>(`/api/product/${productId}/reviews/summary`),
  create: (productId: number, data: { rating: number; content: string }) =>
    http.post<unknown, ProductReview>(`/api/product/${productId}/reviews`, data)
}

export const addressApi = {
  list: () => http.get<unknown, Address[]>('/api/user/address/list'),
  default: () => http.get<unknown, Address>('/api/user/address/default'),
  create: (data: Omit<Address, 'id'>) => http.post<unknown, number>('/api/user/address', data),
  update: (id: number, data: Omit<Address, 'id'>) => http.put<unknown, void>(`/api/user/address/${id}`, data),
  remove: (id: number) => http.delete<unknown, void>(`/api/user/address/${id}`),
  setDefault: (id: number) => http.put<unknown, void>(`/api/user/address/${id}/default`)
}

export const couponApi = {
  page: (params: { pageNum: number; pageSize: number }) => http.get<unknown, PageResult<Coupon>>('/api/coupon/page', { params }),
  adminPage: (params: { pageNum: number; pageSize: number }) => http.get<unknown, PageResult<Coupon>>('/api/coupon/admin/page', { params }),
  create: (data: Omit<Coupon, 'id'>) => http.post<unknown, Coupon>('/api/coupon', data),
  update: (id: number, data: Partial<Omit<Coupon, 'id'>>) => http.put<unknown, Coupon>(`/api/coupon/${id}`, data),
  up: (id: number) => http.put<unknown, void>(`/api/coupon/${id}/up`),
  down: (id: number) => http.put<unknown, void>(`/api/coupon/${id}/down`),
  remove: (id: number) => http.delete<unknown, void>(`/api/coupon/${id}`),
  preheat: (id: number) => http.post<unknown, void>(`/api/coupon/${id}/preheat`),
  receive: (id: number) => http.post<unknown, void>(`/api/coupon/${id}/receive`),
  my: () => http.get<unknown, UserCoupon[]>('/api/coupon/my'),
  available: (amount: number) => http.get<unknown, Coupon[]>('/api/coupon/available', { params: { amount } })
}

export const orderApi = {
  confirm: (data: { source: string; addressId: number; couponId?: number; items: Array<{ productId: number; quantity: number }> }) =>
    http.post<unknown, OrderConfirm>('/api/order/confirm', data),
  create: (data: { source: string; addressId: number; couponId?: number; items: Array<{ productId: number; quantity: number }> }) =>
    http.post<unknown, number>('/api/order', data),
  detail: (id: number) => http.get<unknown, Order>(`/api/order/${id}`),
  my: (params: { pageNum: number; pageSize: number }) => http.get<unknown, PageResult<Order>>('/api/order/my', { params }),
  adminPage: (params: { pageNum: number; pageSize: number; status?: number }) =>
    http.get<unknown, PageResult<Order>>('/api/order/admin/page', { params }),
  pay: (id: number) => http.post<unknown, void>(`/api/order/${id}/pay`),
  cancel: (id: number) => http.post<unknown, void>(`/api/order/${id}/cancel`),
  ship: (id: number, data: { deliveryCompany: string; trackingNo: string }) => http.post<unknown, void>(`/api/order/${id}/ship`, data),
  receive: (id: number) => http.post<unknown, void>(`/api/order/${id}/receive`)
}
