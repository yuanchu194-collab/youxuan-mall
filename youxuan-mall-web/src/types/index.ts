export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  pages: number
  pageNum: number
  pageSize: number
  records: T[]
}

export interface User {
  id: number
  username: string
  nickname?: string
  phone?: string
  role: 'USER' | 'ADMIN' | string
}

export interface LoginResult {
  token: string
  user: User
}

export interface Category {
  id: number
  name?: string
  categoryName?: string
  parentId?: number
  sort?: number
  status?: number
}

export interface Product {
  id: number
  productId?: number
  categoryId: number
  categoryName?: string
  name: string
  productName?: string
  title?: string
  subtitle?: string
  productSubtitle?: string
  description?: string
  mainImage?: string
  main_image?: string
  imageUrl?: string
  productImage?: string
  detail?: string
  price: number
  productPrice?: number
  originalPrice?: number
  original_price?: number
  sales?: number
  status: number
  stock?: number
  lockedStock?: number
  createTime?: string
  updateTime?: string
}

export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkType?: string
  linkValue?: string
  sort: number
  status: number
}

export interface RecommendProduct extends Product {
  recommendId?: number
  recommendTitle?: string
}

export interface HomeIndex {
  banners: Banner[]
  categories: Category[]
  hotProducts: Product[]
  recommendProducts: RecommendProduct[]
}

export interface CartItem {
  cartItemId: number
  id?: number
  productId: number
  productName: string
  mainImage?: string
  productImage?: string
  price: number
  quantity: number
  checked: number
  stock?: number
  status?: number
  totalAmount?: number
  invalidReason?: string
}

export interface Address {
  id: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  defaultFlag: number
  createTime?: string
  updateTime?: string
}

export interface Coupon {
  id: number
  couponId?: number
  name: string
  couponType?: string
  amount: number
  minAmount: number
  totalStock: number
  availableStock: number
  receivedCount?: number
  usedCount?: number
  perLimit?: number
  scope?: string
  scopeType?: 'ALL' | 'CATEGORY' | string
  categoryId?: number
  startTime: string
  endTime: string
  status: number
}

export interface UserCoupon {
  userCouponId: number
  couponId: number
  couponName: string
  couponStatus?: number
  amount: number
  minAmount: number
  status: number
  receiveTime: string
  useTime?: string
  orderId?: number
  scopeType?: 'ALL' | 'CATEGORY' | string
  categoryId?: number
  startTime?: string
  endTime?: string
}

export interface OrderConfirmItem {
  productId: number
  productName: string
  mainImage?: string
  productImage?: string
  price: number
  quantity: number
  totalAmount: number
}

export interface OrderConfirm {
  address?: Address
  items: OrderConfirmItem[]
  availableCoupons: Coupon[]
  selectedCoupon?: Coupon
  totalAmount: number
  discountAmount: number
  payAmount: number
}

export interface Order {
  id: number
  orderNo: string
  userId?: number
  totalAmount: number
  discountAmount: number
  payAmount: number
  status: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  payTime?: string
  deliveryCompany?: string
  trackingNo?: string
  deliveryTime?: string
  receiveTime?: string
  cancelTime?: string
  createTime: string
  items?: OrderItem[]
}

export interface OrderItem {
  id?: number
  productId: number
  productName: string
  productImage?: string
  mainImage?: string
  price: number
  quantity: number
  totalAmount: number
}
