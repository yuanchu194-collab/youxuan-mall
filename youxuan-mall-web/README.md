# 优选商城前端

阶段 21 第一版展示前端，使用 Vue 3 + Vite + TypeScript + Pinia + Vue Router + Axios + Element Plus。

## 启动

```powershell
cd D:\code\youxuan-mall\youxuan-mall-web
npm install
npm run dev
```

默认访问：

```text
http://127.0.0.1:5173
```

如果需要固定为 3000 端口：

```powershell
npm run dev -- --host 127.0.0.1 --port 3000
```

## 配置

默认接口地址为：

```text
http://localhost:9000
```

如需覆盖，可在本目录新增 `.env.local`：

```text
VITE_API_BASE_URL=http://localhost:9000
```

登录后前端会将 token 写入 `localStorage`，请求头统一使用：

```text
Authorization: Bearer token
```

## 页面

- `/login` 登录页
- `/register` 注册页
- `/` 商城首页
- `/products` 商品列表页
- `/products/:id` 商品详情页
- `/cart` 购物车页
- `/addresses` 收货地址管理页
- `/checkout` 订单确认页
- `/orders` 我的订单页
- `/coupons` 优惠券中心
- `/my-coupons` 我的优惠券
- `/admin/products` 后台商品管理页
- `/admin/coupons` 后台优惠券管理页
- `/admin/orders` 后台订单管理页

## 说明

- 本阶段只实现前端，不实现 Canal、Sentinel、Seata、秒杀。
- 商品图片使用后端已有 MinIO 图片 URL，后台商品管理页只调用已有文件上传接口。
- 后台页面依赖登录用户角色为 `ADMIN`。

## 阶段 21-3 联调修复

已修复：

- 首页分类只展示分类名称，最多展示 8 个，过滤 `??`、`阶段`、`Stage`、长数字串等测试分类名。
- 首页 Banner 无图时展示默认视觉区域，不再大面积空白。
- 商品图片字段统一适配 `mainImage`，兼容 `main_image`、`imageUrl`、`productImage`。
- 商品图片为空或加载失败时展示本地占位图。
- 购物车删除、勾选、改数量统一使用后端返回的 `cartItemId`。
- 订单确认页进入时加载默认地址，并向 `/api/order/confirm`、`/api/order` 传 `addressId`。
- 没有默认地址时引导到 `/addresses` 设置默认地址。
- 后台商品表格图片使用同一套图片兜底逻辑。

后端联调配置：

- Gateway 已放行游客访问 `GET /api/home/index`、商品分类、商品分页、商品详情、热门商品和商品搜索。
- 购物车、地址、订单、后台页面仍需要登录。
- 修改 Gateway 配置后需要重启 `youxuan-gateway` 才会生效。

演示账号：

```text
管理员账号：admin
管理员密码：Admin@123456
```

## 阶段 21-4 演示补全

已补全：

- 首页、商品列表、商品详情、后台商品管理统一使用 `mainImage`，并兼容 `main_image`、`imageUrl`、`productImage`。
- 商品主图为空或加载失败时展示前端本地占位图；后台商品管理更新 `mainImage` 后，首页、列表、详情和推荐商品会显示真实图片。
- 商品变更后后端会清理商品详情、首页聚合、热门商品和推荐商品 Redis 缓存，避免首页继续显示旧图。
- 新增优惠券中心 `/coupons`，支持游客查看，登录用户领取。
- 新增我的优惠券 `/my-coupons`，展示未使用、已使用、已过期状态。
- 新增后台订单管理 `/admin/orders`，支持按订单状态筛选、查看收货信息和对待发货订单发货。
- 后台优惠券预热后，用户可到优惠券中心领取。

## 阶段 21-5 真实接口逐项修复

已修复：

- `normalizeProduct` 统一兼容 `id/productId`、`name/productName/title`、`subtitle/productSubtitle/description`、`mainImage/main_image/imageUrl/productImage`、`price/productPrice`、`originalPrice/original_price`。
- 首页热门推荐使用真实 `hotProducts`，优选推荐使用真实 `recommendProducts`，不再因为推荐商品返回 `productId/productName` 而显示“未命名商品”。
- `ProductCard` 跳转和展示均使用归一化后的商品 ID、名称和图片。
- 商品列表、商品详情、购物车、订单确认继续复用同一套图片兜底规则；真实 `mainImage` 优先，空图或加载失败才显示本地占位图。
- 优惠券中心领取成功后可在 `/my-coupons` 查看；重复领取、抢光、未预热会显示明确提示。
- 后台订单管理路由 `/admin/orders` 已注册在路由和后台菜单中，普通用户会被路由守卫拦回首页。
- 管理员订单分页接口在当前构建 jar 中验证可返回订单编号、用户 ID、金额、状态、收货信息、支付时间和发货信息；如 9050 旧实例返回 `code=500`，需要重启 order-service。

## 阶段 21-6-1 / 21-6-2 设计系统与首页 Banner 轮播

已完成：

- 新增统一设计系统样式 `src/styles/theme.css` 和 `src/styles/components.css`，集中管理生鲜商城主题色、圆角、阴影、卡片、商品卡片、价格、标签、空状态和 Element Plus 按钮/输入框外观。
- 新增首页 Banner fallback 常量 `src/constants/banners.ts`，集中维护 4 张本地 Banner 图、跳转地址和按钮文案。
- 新增 `src/components/HomeBannerCarousel.vue`，支持自动轮播、圆点切换、鼠标悬停暂停和图片加载失败渐变兜底。
- 首页 `/` 已接入 `HomeBannerCarousel`，仍优先使用 `/api/home/index` 返回的真实 `banners`；后端 Banner 为空或少于 4 张时用本地 fallback 补足展示。
- `public/banners` 已补充文档约定访问路径：`banner-fresh.png`、`banner-coupon.png`、`banner-new-user.png`、`banner-season.png`。

验证：

```powershell
npm run build
```

已验证构建通过。首次沙箱内执行仍可能因 Vite/esbuild 子进程 `spawn EPERM` 失败，提升权限重跑同一命令可通过。

## 阶段 21-6-3 首页整体重构

已完成：

- 首页 `/` 按参考图重构首屏布局：左侧 8 个以内分类栏，右侧接入已有 `HomeBannerCarousel`，Banner 与分类栏对齐。
- 分类栏改为浅绿色卡片，分类项包含图标、名称和右箭头，长分类名会省略，分类为空时显示空状态。
- 四个快捷入口改为大图标卡片，分别跳转优惠券中心、新人专享、限时上新和优选推荐对应页面。
- 热门推荐和优选推荐改为独立白色圆角分区，商品仍使用 `ProductCard`，数据继续来自 `/api/home/index` 的 `hotProducts` 和 `recommendProducts`。
- 首页商品继续通过 `normalizeProduct` 字段兼容，过滤无效商品项，不新增商品 mock，不用假商品掩盖接口问题。
- 首页导航激活态和搜索区域在首页做了轻量视觉增强，不影响后端接口。

验证：

```powershell
npm run build
```

已验证构建通过。若沙箱内因 Vite/esbuild 子进程 `spawn EPERM` 失败，提升权限重跑同一命令即可。

### 阶段 21-6-3 精修

已完成：

- 本地 fallback Banner 增加 `fullImage` 标记，使用完整设计图时只渲染图片，不再叠加标题、副标题和按钮，避免文字重叠。
- 首页 Banner 高度收紧到约 286px，分类栏宽度调整为 260px，首屏间距和比例更贴近 `docs/ui-reference/01-首页.png`。
- 快捷入口、热门推荐、优选推荐的上下间距进一步压缩，宽屏下商品区保持每行 6 个商品。
- 商品卡片图片改为 `object-fit: contain`，首页商品图固定展示高度，缺图仍使用绿色商城 SVG 占位图，不使用二次元图作为兜底。
- 顶部导航首页态微调高度、搜索框宽度和 active 短线位置。

## 阶段 21-6-4 商品列表页重构

已完成：

- 商品列表页 `/products` 按 `docs/ui-reference/02-商品列表.png` 重构为左侧分类/筛选栏、右侧头图、排序栏、分类 chips、商品 Grid 和分页的商城布局。
- 左侧分类最多展示 8 个，分类名称过长会省略，分类为空时显示空状态。
- 排序区保留综合、销量、价格、新品、评价入口；默认综合查询继续使用商品分页接口，关键词、价格区间和排序查询使用商品搜索接口。
- 商品卡片继续复用 `ProductCard`，列表页开启销量展示；商品图片仍优先显示真实 `mainImage`，缺失或加载失败才使用本地商城占位图。
- 页面未新增商品 mock，不用静态假商品覆盖接口问题。

验证：

```powershell
npm run build
```

已验证构建通过。若沙箱内因 Vite/esbuild 子进程 `spawn EPERM` 失败，提升权限重跑同一命令即可。

## 阶段 21-6-5 商品详情页重构

已完成：

- 商品详情页 `/products/:id` 按 `docs/ui-reference/03-商品详情.png` 重构为面包屑、左侧商品大图、中间购买信息、右侧店铺和保障、下方详情 Tabs 与搭配推荐的电商详情布局。
- 商品大图继续优先使用真实 `mainImage`，兼容已有图片字段；缺图或加载失败时使用本地商城占位图，不新增假缩略图覆盖接口问题。
- 商品标题、副标题、分类、销量、库存、价格、原价、折扣、优惠提示、配送说明、服务保障和数量选择器已统一为生鲜商城样式。
- 数量选择器最小为 1，最大不超过当前库存；无库存或已下架时禁用购买按钮。
- 加入购物车继续调用真实 `POST /api/cart/add`；未登录时按现有登录页跳转逻辑处理。
- 立即购买当前采用“先加入购物车，再跳转购物车结算”的前端处理，避免后端立即购买链路不可用时页面报错。
- 商品详情 Tabs 包含商品详情、规格参数、服务保障；后端无 `detail` 字段时显示基础空状态。
- 搭配推荐优先使用真实热门商品接口，空时再尝试商品分页接口，商品卡片复用 `ProductCard`。

验证：

```powershell
npm run build
```

## 阶段 21-7-1 购物车页面高保真重构

已完成：

- 购物车页 `/cart` 按 `docs/ui-reference/06-购物车.png` 返工为左侧约 72% 购物车列表、右侧约 28% 订单结算卡的高保真商城布局，页面宽度跟首页统一到 1480px。
- 左侧列表不再使用普通 table，表头和商品行共用同一套 CSS Grid 列模板，勾选、商品信息、单价、数量、小计和操作列完全对齐。
- 商品行补齐 96px 商品图、商品名、副标题、规格描述、绿色分类标签、现价、原价删除线、折扣标签、小计红色价格和固定宽度数量加减器。
- 商品列表保留真实购物车接口：`GET /api/cart/list`、`PUT /api/cart/check`、`PUT /api/cart/checkAll`、`PUT /api/cart/update`、`DELETE /api/cart/{id}`。
- 单项删除继续使用后端返回的 `cartItemId` 或 `id`，不会用商品 ID 代替购物车项 ID。
- 批量删除使用既有 `DELETE /api/cart/checked`，删除前要求已勾选商品并弹出确认。
- 金额区域展示商品总额、优惠金额、优惠券、运费和应付金额；购物车页只做前端展示，最终价格仍以 `/checkout` 订单确认页和后端结算为准。
- 空购物车、加载失败、重新加载、图片失败兜底状态已补齐；商品图片继续优先使用真实 `mainImage`，缺失或加载失败时使用购物车专用默认商品图。
- 补齐移入收藏、降价提醒、查看相似商品、领取可用优惠券、选择配送方式、清空失效商品、批量收藏、凑单推荐、客服咨询等入口。
- 后端未实现的入口统一调用 `src/utils/feature.ts` 中的 `showBackendTodo`，固定提示“后端功能暂未实现”，不伪造成功状态。

验证：

```powershell
npm run build
```

## 阶段 21-7-2 订单确认页视觉重构 + 前端功能入口补齐

已完成：

- 订单确认页 `/checkout` 按 `docs/ui-reference/07-订单确认.png` 重构为宽屏商城确认单布局，包含顶部淡绿确认条、左侧订单信息、右侧优惠券/备注/金额卡和底部结算保障栏。
- 收货地址优先调用真实 `GET /api/user/address/list`，再尝试真实默认地址接口；默认地址优先选中，用户也可在确认页选择其他地址。
- 无地址时显示空状态并引导到 `/addresses` 新增地址；提交订单前强校验 `addressId`，没有地址不允许提交。
- 商品清单继续来自真实购物车勾选商品或立即购买参数，并通过 `/api/order/confirm` 获取后端确认金额、商品、优惠券和地址快照。
- 商品图片继续复用真实 `mainImage` 和本地占位图兜底；不会为了视觉展示写死假商品。
- 优惠券优先使用 `/api/order/confirm` 返回的真实 `availableCoupons` 和 `selectedCoupon`，不会前端伪造优惠券核销成功。
- 订单提交继续调用真实 `POST /api/order`，只传后端 DTO 支持的 `source`、`addressId`、`couponId`、`items`。
- 订单备注仅前端展示输入；当前后端 `OrderCreateRequest` 未包含 `remark` 字段，因此不会提交备注。
- 配送方式、更多优惠券、发票、余额支付、微信支付、支付宝支付、订单运费险等后端未实现入口统一调用 `showBackendTodo('xxx')`，提示“xxx后端功能暂未实现”。
- `/checkout` 页面宽度与购物车保持一致，使用 1480px 商城宽屏容器。

验证：

```powershell
npm run build
```

已验证构建通过。沙箱内 Vite/esbuild 子进程可能出现 `spawn EPERM`，提升权限重跑同一命令可通过。本地预览服务已验证：

```text
http://127.0.0.1:5173/checkout
```

## 阶段 21-7-3 我的订单页高保真重构 + 前端功能入口补齐

已完成：

- 我的订单页 `/orders` 按 `docs/ui-reference/08-我的订单.png` 重构为用户订单中心布局，包含左侧会员侧栏、右侧标题区、状态 tabs、订单卡片列表、空状态和分页区域。
- 页面宽度与购物车、订单确认页统一为 1480px 商城宽屏容器，顶部 Header 和“我的订单”导航选中态保持一致。
- 订单列表继续调用真实 `GET /api/order/my`，不会用假订单覆盖接口问题。
- 由于订单分页接口不带商品明细，前端新增调用已有真实接口 `GET /api/order/{id}` 获取每个订单的商品明细，并在订单卡片和详情弹窗中展示。
- 状态 tabs 包含全部、待支付、待发货、待收货、已完成、已取消；当前后端我的订单接口未提供 status 查询参数，因此 tabs 基于已加载的真实订单数据做前端筛选。
- 支付、取消订单、确认收货继续调用真实 `POST /api/order/{id}/pay`、`POST /api/order/{id}/cancel`、`POST /api/order/{id}/receive`。
- 查看详情使用真实 `GET /api/order/{id}` 打开订单详情弹窗，展示真实订单主信息、收货信息、商品明细和实付金额。
- 商品缩略图继续使用订单明细中的 `productImage/mainImage`，图片缺失或加载失败时使用统一商城占位图。
- 提醒发货、查看物流、评价商品、再次购买、删除订单、申请售后、联系客服、查看发票、浏览足迹、我的收藏等后端未实现入口统一调用 `showBackendTodo('xxx')`。
- 空订单、接口失败、商品明细不可用状态已补齐，不伪造物流、支付、确认收货或订单删除成功。

验证：

```powershell
npm run build
```

已验证构建通过。沙箱内 Vite/esbuild 子进程可能出现 `spawn EPERM`，提升权限重跑同一命令可通过。

## 阶段 21-8-1 优惠券中心高保真重构 + 前端功能入口补齐

已完成：

- 优惠券中心 `/coupons` 按 `docs/ui-reference/04-优惠券中心.png` 重构为账户中心侧栏、顶部优惠券 Banner、状态 tabs、两列票券卡片、空状态和底部使用小贴士的用户端商城布局。
- 页面宽度与首页、购物车、订单确认页、我的订单页保持 1480px 宽屏容器，顶部 Header 的“优惠券”导航选中态已补齐。
- 优惠券列表继续调用真实 `GET /api/coupon/page`，不会新增假优惠券覆盖接口问题。
- 领取优惠券继续调用真实 `POST /api/coupon/{id}/receive`；未登录点击领取会跳转 `/login?redirect=/coupons`。
- 登录后会额外调用真实 `GET /api/coupon/my` 辅助标记已领取优惠券；领取成功只在真实接口成功后更新已领取状态并刷新列表。
- tabs 包含全部、可领取、已领取、即将过期、新人专享、满减券、品类券；当前后端优惠券列表未返回券类型字段，因此新人专享、品类券等展示分类基于真实优惠券名称和门槛字段做前端归类，不伪造接口数据。
- 优惠券卡片展示优惠金额、使用门槛、名称、适用范围、有效期、剩余数量、状态标签和立即领取按钮，并区分已领取、已过期、库存不足、停用状态样式。
- 领取记录入口跳转真实 `/my-coupons` 页面；空状态提供“去逛逛”按钮跳转 `/products`。
- 查看可用商品、分享优惠券、订阅优惠券提醒、查看领取规则、优惠券筛选高级条件等后端未实现入口统一调用 `showBackendTodo('xxx')`，提示“xxx后端功能暂未实现”。

验证：

```powershell
npm run build
```

已验证构建通过。沙箱内 Vite/esbuild 子进程可能出现 `spawn EPERM`，提升权限重跑同一命令可通过。

## 演示数据

演示数据准备脚本，当前脚本不再从本地图片目录自动匹配或上传商品图片：

```powershell
cd D:\code\youxuan-mall
powershell -ExecutionPolicy Bypass -File .\scripts\prepare-demo-data.ps1
```

脚本会清理本地 `youxuan_mall` 中的业务演示数据，保留 `sys_user`，并重建 8 个分类、40 个商品、3 条 Banner、8 个真实商品推荐位、5 张优惠券和管理员默认地址。Gateway 可用时会自动预热优惠券、清理 Redis 首页/商品缓存并重新导入 ES。详细记录见：

```text
docs/06-演示数据准备.md
```

商品 `mainImage` 可以为空，前端使用本地默认占位图。后续在后台商品管理中上传商品图并保存 `mainImage` 后，首页、列表、详情、购物车和订单确认会优先显示 MinIO 返回的真实图片 URL。如首页仍旧图，清理 `youxuan:home:index`、`youxuan:home:hot-products`、`youxuan:home:recommend-products` 和相关 `youxuan:product:detail:*`。

## 验证

```powershell
npm run build
```

已验证 `npm run build` 通过。首次沙箱内执行曾因 Vite/esbuild 子进程 `spawn EPERM` 失败，提升权限重跑同一命令后通过。
