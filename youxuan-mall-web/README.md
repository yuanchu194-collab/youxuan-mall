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
