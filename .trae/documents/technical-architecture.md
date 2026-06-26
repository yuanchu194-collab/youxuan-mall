# 优选商城前端技术架构文档

## 1. 架构设计

```mermaid
flowchart TD
    "浏览器" --> "Vue3应用"
    "Vue3应用" --> "路由层(Vue Router)"
    "路由层(Vue Router)" --> "页面组件(Pages)"
    "页面组件(Pages)" --> "业务组件(Components)"
    "业务组件(Components)" --> "状态管理(Pinia)"
    "业务组件(Components)" --> "API请求层(Axios)"
    "API请求层(Axios)" --> "Spring Cloud Gateway"
    "Spring Cloud Gateway" --> "微服务集群"
```

## 2. 技术说明

- 前端框架：Vue 3 (Composition API + `<script setup>`)
- 构建工具：Vite
- UI组件库：Element Plus
- 路由：Vue Router 4
- 状态管理：Pinia
- HTTP客户端：Axios
- 语言：TypeScript
- 样式：SCSS + Element Plus主题定制
- 包管理器：npm

## 3. 项目结构

```text
youxuan-mall-web/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口定义
│   │   ├── user.ts        # 用户相关接口
│   │   ├── product.ts     # 商品相关接口
│   │   ├── cart.ts        # 购物车相关接口
│   │   ├── order.ts       # 订单相关接口
│   │   ├── coupon.ts      # 优惠券相关接口
│   │   ├── home.ts        # 首页相关接口
│   │   └── file.ts        # 文件上传接口
│   ├── assets/            # 资源文件
│   │   └── styles/        # 全局样式
│   ├── components/        # 公共组件
│   │   ├── Header.vue     # 顶部导航栏
│   │   ├── Footer.vue     # 底部
│   │   ├── ProductCard.vue # 商品卡片
│   │   └── AddressDialog.vue # 地址编辑弹窗
│   ├── composables/       # 组合式函数
│   │   └── useAuth.ts     # 认证相关
│   ├── router/            # 路由配置
│   │   └── index.ts
│   ├── stores/            # Pinia状态管理
│   │   ├── user.ts        # 用户状态
│   │   ├── cart.ts        # 购物车状态
│   │   └── app.ts         # 应用状态
│   ├── types/             # TypeScript类型定义
│   │   └── index.ts
│   ├── utils/             # 工具函数
│   │   ├── request.ts     # Axios封装
│   │   └── auth.ts        # 认证工具
│   ├── views/             # 页面组件
│   │   ├── Login.vue      # 登录页
│   │   ├── Register.vue   # 注册页
│   │   ├── Home.vue       # 首页
│   │   ├── ProductList.vue # 商品列表
│   │   ├── ProductSearch.vue # 商品搜索
│   │   ├── ProductDetail.vue # 商品详情
│   │   ├── Cart.vue       # 购物车
│   │   ├── OrderConfirm.vue # 订单确认
│   │   ├── OrderList.vue  # 我的订单
│   │   ├── OrderDetail.vue # 订单详情
│   │   ├── Address.vue    # 收货地址管理
│   │   ├── CouponCenter.vue # 优惠券中心
│   │   ├── MyCoupons.vue  # 我的优惠券
│   │   └── admin/         # 管理员页面
│   │       ├── ProductManage.vue
│   │       └── OrderShip.vue
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── .env.development
```

## 4. 路由定义

| 路由路径 | 页面名称 | 权限要求 |
|----------|----------|----------|
| /login | 登录页 | 游客 |
| /register | 注册页 | 游客 |
| / | 首页 | 公开 |
| /products | 商品列表 | 公开 |
| /search | 商品搜索 | 公开 |
| /product/:id | 商品详情 | 公开 |
| /cart | 购物车 | USER |
| /order/confirm | 订单确认 | USER |
| /orders | 我的订单 | USER |
| /order/:id | 订单详情 | USER |
| /address | 收货地址 | USER |
| /coupons | 优惠券中心 | 公开 |
| /my-coupons | 我的优惠券 | USER |
| /admin/products | 商品管理 | ADMIN |
| /admin/orders | 订单发货 | ADMIN |

## 5. API定义

### 5.1 用户接口
```typescript
// 注册
POST /api/user/register
Request: { username: string, password: string, nickname?: string }
Response: Result<void>

// 登录
POST /api/user/login
Request: { username: string, password: string }
Response: Result<{ token: string, userId: number, username: string, role: string }>

// 获取当前用户信息
GET /api/user/me
Response: Result<UserVO>
```

### 5.2 商品接口
```typescript
// 商品分页
GET /api/product/page
Params: { pageNum: number, pageSize: number, name?: string, categoryId?: number, status?: number }
Response: Result<PageResult<ProductVO>>

// 商品详情
GET /api/product/{id}
Response: Result<ProductVO>

// 热门商品
GET /api/product/home/hot
Response: Result<ProductVO[]>
```

### 5.3 购物车接口
```typescript
// 加入购物车
POST /api/cart/add
Request: { productId: number, quantity: number }
Response: Result<void>

// 修改数量
PUT /api/cart/update
Request: { id: number, quantity: number }
Response: Result<void>

// 删除购物车项
DELETE /api/cart/{id}
Response: Result<void>

// 查询购物车列表
GET /api/cart/list
Response: Result<CartVO[]>

// 勾选/取消勾选
PUT /api/cart/check
Request: { id: number, checked: number }
Response: Result<void>

// 全选/取消全选
PUT /api/cart/checkAll
Request: { checked: number }
Response: Result<void>

// 删除已勾选
DELETE /api/cart/checked
Response: Result<void>
```

### 5.4 订单接口
```typescript
// 订单确认
POST /api/order/confirm
Request: { source: 'CART' | 'BUY_NOW', addressId?: number, couponId?: number, items?: { productId: number, quantity: number }[] }
Response: Result<OrderConfirmVO>

// 创建订单
POST /api/order
Request: { addressId: number, couponId?: number, source: string, items?: { productId: number, quantity: number }[] }
Response: Result<{ orderId: number, orderNo: string }>

// 我的订单
GET /api/order/my
Params: { pageNum: number, pageSize: number }
Response: Result<PageResult<OrderVO>>

// 订单详情
GET /api/order/{id}
Response: Result<OrderDetailVO>

// 支付
POST /api/order/{id}/pay
Response: Result<void>

// 取消
POST /api/order/{id}/cancel
Response: Result<void>

// 确认收货
POST /api/order/{id}/receive
Response: Result<void>

// 发货(ADMIN)
POST /api/order/{id}/ship
Request: { deliveryCompany: string, trackingNo: string }
Response: Result<void>
```

### 5.5 优惠券接口
```typescript
// 领取优惠券
POST /api/coupon/{id}/receive
Response: Result<void>

// 我的优惠券
GET /api/coupon/my
Response: Result<UserCouponVO[]>

// 可用优惠券
GET /api/coupon/available
Params: { amount: number }
Response: Result<CouponVO[]>
```

### 5.6 首页接口
```typescript
// 首页数据
GET /api/home/index
Response: Result<{ banners: BannerVO[], categories: CategoryVO[], hotProducts: ProductVO[], recommendProducts: ProductVO[], newUserCoupons: CouponVO[] }>
```

### 5.7 地址接口
```typescript
// 地址列表
GET /api/user/address/list
Response: Result<AddressVO[]>

// 默认地址
GET /api/user/address/default
Response: Result<AddressVO>

// 新增地址
POST /api/user/address
Request: AddressForm
Response: Result<void>

// 修改地址
PUT /api/user/address/{id}
Request: AddressForm
Response: Result<void>

// 删除地址
DELETE /api/user/address/{id}
Response: Result<void>

// 设置默认
PUT /api/user/address/{id}/default
Response: Result<void>
```

## 6. 数据模型

### 核心类型定义
```typescript
interface UserVO {
  id: number
  username: string
  nickname: string
  phone?: string
  role: 'USER' | 'ADMIN'
}

interface ProductVO {
  id: number
  categoryId: number
  categoryName?: string
  name: string
  subtitle?: string
  mainImage?: string
  price: number
  originalPrice?: number
  sales: number
  status: number
  stock?: number
}

interface CartItemVO {
  id: number
  productId: number
  quantity: number
  checked: number
  product?: ProductVO
}

interface AddressVO {
  id: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  defaultFlag: number
}

interface CouponVO {
  id: number
  name: string
  amount: number
  minAmount: number
  endTime: string
  status: number
  received?: boolean
}

interface OrderVO {
  id: number
  orderNo: string
  totalAmount: number
  discountAmount: number
  payAmount: number
  status: number
  statusText: string
  createTime: string
  items: OrderItemVO[]
}

interface OrderItemVO {
  id: number
  productId: number
  productName: string
  productImage?: string
  price: number
  quantity: number
  totalAmount: number
}
```
