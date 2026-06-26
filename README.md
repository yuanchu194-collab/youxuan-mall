# 优选商城

## 当前阶段

阶段 5：Gateway 登录校验与用户透传。

本阶段包含：

- 父工程 `pom.xml`
- `youxuan-common`
- `youxuan-gateway`
- `youxuan-user-service`
- `youxuan-product-service`
- `youxuan-search-service`
- `youxuan-coupon-service`
- `youxuan-order-service`
- `youxuan-file-service`
- `youxuan-cart-service`
- `youxuan-home-service`

`youxuan-common` 已补充公共基础能力：

- `Result`
- `PageResult`
- `ErrorCode`
- `BusinessException`
- `GlobalExceptionHandler`
- `BaseEntity`
- `UserContext`
- `RedisKeyConstants`
- `RabbitMqConstants`
- `MyBatisPlusConfig`
- `Knife4jConfig`
- `JwtUtils`

`youxuan-user-service` 已实现：

- 用户注册
- 用户登录
- 查询当前用户信息
- BCrypt 密码加密
- 登录成功返回包含 `userId`、`username`、`role` 的 JWT

`youxuan-gateway` 已实现：

- 白名单接口放行
- 非白名单接口校验 `Authorization: Bearer token`
- 解析 JWT 中的 `userId`、`username`、`role`
- 向下游透传 `X-User-Id`、`X-User-Role`

下游服务已通过公共拦截器读取请求头并写入 `UserContext`，请求结束后清理上下文。

未实现商品 CRUD、订单、优惠券、购物车、地址等后续业务，也未实现 ADMIN 接口权限控制。

## 版本

- Java 17
- Spring Boot 3.2.6
- Spring Cloud 2023.0.2
- Spring Cloud Alibaba 2023.0.1.0
- MyBatis-Plus 3.5.7
- Knife4j 4.5.0
- JJWT 0.12.5
- MySQL Connector/J

## 启动 MySQL 和 Nacos

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos
```

## 阶段 6：商品服务 CRUD 与库存

本阶段只实现 `youxuan-product-service` 的商品分类、商品 CRUD、上下架、库存初始化、库存扣减和库存恢复。

未实现内容：Redis 商品缓存、Elasticsearch 搜索、RabbitMQ 商品同步、MinIO 图片上传、购物车、订单、优惠券、首页运营。

### 数据库表

新增表：

- `product_category`
- `product`
- `product_stock`

建表 SQL 已追加到 `docker/mysql/init/01-init.sql`。如果本机 MySQL 容器已经初始化过数据卷，Docker 不会自动重新执行 init SQL，需要手动连接 `localhost:3307` 的 `youxuan_mall` 数据库执行这三张表的建表语句。

### 启动顺序

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos
```

分别启动：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-gateway -am spring-boot:run
```

所有业务接口统一从 Gateway 访问：

```text
http://localhost:9000
```

除注册、登录、`/test/ping` 外，商品接口都需要：

```text
Authorization: Bearer <token>
```

### 登录获取 token

```powershell
$loginBody = @{
  username = "testuser"
  password = "123456"
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/user/login -ContentType "application/json" -Body $loginBody
$token = $login.data.token
$headers = @{ Authorization = "Bearer $token" }
```

### 商品分类测试

```powershell
$categoryBody = @{
  name = "手机数码"
  parentId = 0
  sort = 1
  status = 1
} | ConvertTo-Json

$category = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/product/category -Headers $headers -ContentType "application/json" -Body $categoryBody
$categoryId = $category.data.id

Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/product/category/list -Headers $headers
```

### 商品新增和查询测试

```powershell
$productBody = @{
  categoryId = $categoryId
  name = "优选无线耳机"
  subtitle = "高清音质，长续航"
  mainImage = "http://localhost:9100/youxuan-mall/products/demo.jpg"
  detail = "阶段6先保存图片URL，不实现图片上传。"
  price = 199.00
  originalPrice = 299.00
  sales = 0
  status = 1
  stock = 10
} | ConvertTo-Json

$product = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/product -Headers $headers -ContentType "application/json" -Body $productBody
$productId = $product.data.id

Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/page?pageNum=1&pageSize=10&name=耳机&categoryId=$categoryId&status=1" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/$productId" -Headers $headers
Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/product/$productId/down" -Headers $headers
Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/product/$productId/up" -Headers $headers
```

### 库存扣减和恢复测试

```powershell
$deductBody = @{
  productId = $productId
  quantity = 3
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/product/stock/deduct -Headers $headers -ContentType "application/json" -Body $deductBody

$notEnoughBody = @{
  productId = $productId
  quantity = 999
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/product/stock/deduct -Headers $headers -ContentType "application/json" -Body $notEnoughBody

$restoreBody = @{
  productId = $productId
  quantity = 3
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/product/stock/restore -Headers $headers -ContentType "application/json" -Body $restoreBody
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/$productId" -Headers $headers
```

Nacos 控制台：

```text
http://localhost:8848/nacos
```

账号密码：

```text
nacos / nacos
```

## 启动服务

启动单个服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
```

可替换模块名启动其他服务：

```text
youxuan-product-service  9020
youxuan-search-service   9030
youxuan-coupon-service   9040
youxuan-order-service    9050
youxuan-file-service     9060
youxuan-cart-service     9070
youxuan-home-service     9080
```

启动 gateway：

```powershell
mvn -pl youxuan-gateway -am spring-boot:run
```

## 测试

注册用户：

```powershell
$body = @{
  username = "testuser"
  password = "123456"
  nickname = "测试用户"
  phone = "13800138000"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/user/register -ContentType "application/json" -Body $body
```

登录用户：

```powershell
$body = @{
  username = "testuser"
  password = "123456"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/user/login -ContentType "application/json" -Body $body
```

查询当前用户：

```powershell
$loginBody = @{
  username = "testuser"
  password = "123456"
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/user/login -ContentType "application/json" -Body $loginBody
$token = $login.data.token

Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/user/me -Headers @{ Authorization = "Bearer $token" }
```

服务连通性：

```powershell
Invoke-RestMethod http://localhost:9000/api/user/test/ping
```

无 token 访问受保护接口：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/user/me
```

透传验证：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/user/test/context -Headers @{ Authorization = "Bearer $token" }
```

注册或查询成功预期返回统一 Result：

重复注册预期返回业务异常：

```json
{
  "code": 5000,
  "message": "用户名已存在",
  "data": null
}
```

## 阶段 7：Redis 商品缓存

本阶段只在商品服务中增加 Redis 显式缓存控制，不涉及 Elasticsearch、RabbitMQ、MinIO、购物车、订单、优惠券和首页运营服务。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos redis
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-gateway -am spring-boot:run
```

### 登录获取 Token

```powershell
$loginBody = @{
  username = "testuser"
  password = "123456"
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/user/login -ContentType "application/json" -Body $loginBody
$token = $login.data.token
$headers = @{ Authorization = "Bearer $token" }
```

### 测试商品详情缓存

第一次查询应打印“商品详情缓存未命中”，第二次查询应打印“商品详情缓存命中”。

```powershell
$productId = 1
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/$productId" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/$productId" -Headers $headers
```

不存在商品第一次查询会写入 2 分钟空值缓存，第二次查询应打印“商品详情空值缓存命中”。

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/99999999" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/99999999" -Headers $headers
```

### 测试热门商品缓存

第一次查询应打印“热门商品缓存未命中”，第二次查询应打印“热门商品缓存命中”。

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/home/hot" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/home/hot" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/home/hot?limit=5" -Headers $headers
```

### 测试缓存删除

修改、上下架、扣减库存、恢复库存后，商品详情缓存会删除，热门商品缓存也会删除。

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/product/$productId/down" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/$productId" -Headers $headers

Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/product/$productId/up" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/home/hot" -Headers $headers
```

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- 商品详情接口第一次查询 Redis 未命中，查询 MySQL 后写入商品详情缓存。
- 商品详情接口第二次查询命中 Redis，直接返回缓存中的商品详情。
- 不存在商品会写入 2 分钟空值缓存，重复查询不再反复访问 MySQL。
- 热门商品接口第一次查询 Redis 未命中，按 `status = 1`、`sales DESC`、`id DESC` 查询 MySQL 后写入缓存。
- 热门商品接口第二次查询命中 Redis，直接返回缓存列表。
- 商品上下架、修改、删除、库存扣减、库存恢复后会删除商品详情缓存；涉及展示一致性的场景同时删除热门商品缓存。
- `mvn -DskipTests package` 已验证通过，结果为 `BUILD SUCCESS`。

已验证的 Redis key：

```text
youxuan:product:detail:1
youxuan:home:hot-products
```
