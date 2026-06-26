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

## 阶段 8：Elasticsearch 商品搜索

本阶段只实现 `youxuan-search-service` 的商品索引创建、手动全量导入和搜索查询，不实现 RabbitMQ 商品变更自动同步，也不涉及 MinIO、购物车、订单、优惠券、首页运营、Canal、Sentinel、Seata 和秒杀。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos elasticsearch
```

检查 Elasticsearch：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9200
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-search-service -am spring-boot:run
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

### 手动全量导入商品到 ES

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product/importAll -Headers $headers
```

确认索引存在：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9200/youxuan_product
```

### 商品搜索测试

关键词搜索：

```powershell
$searchBody = @{
  keyword = "耳机"
  pageNum = 1
  pageSize = 10
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $searchBody
```

分类和价格区间筛选：

```powershell
$filterBody = @{
  categoryId = 1
  minPrice = 100
  maxPrice = 500
  pageNum = 1
  pageSize = 10
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $filterBody
```

按价格升序和降序：

```powershell
$priceAscBody = @{
  sortField = "price"
  sortOrder = "asc"
  pageNum = 1
  pageSize = 10
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $priceAscBody

$priceDescBody = @{
  sortField = "price"
  sortOrder = "desc"
  pageNum = 1
  pageSize = 10
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $priceDescBody
```

按销量降序和分页：

```powershell
$salesBody = @{
  sortField = "sales"
  sortOrder = "desc"
  pageNum = 1
  pageSize = 5
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $salesBody
```

本阶段索引名：

```text
youxuan_product
```

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- `http://localhost:9200` 可访问，Elasticsearch 版本为 `7.17.18`。
- `mvn -DskipTests package` 已验证通过，结果为 `BUILD SUCCESS`。
- 临时启动 `youxuan-product-service` 和 `youxuan-search-service` 后，`/test/ping` 均返回统一 `Result` 成功。
- 调用 `POST /product/importAll` 可创建并重建 `youxuan_product` 索引，本地导入商品数为 `1`。
- 调用 `POST /product` 搜索关键词 `耳机` 返回 `1` 条商品。
- 分类筛选、价格区间筛选、`price asc` 排序、`sales desc` 排序和分页参数均完成直连验收。
- 临时启动 `youxuan-user-service`、`youxuan-product-service`、`youxuan-search-service`、`youxuan-gateway` 后，通过 Gateway 登录获取 token 成功。
- 通过 Gateway 携带 `Authorization: Bearer token` 调用 `POST /api/search/product/importAll` 成功，导入商品数为 `1`。
- 通过 Gateway 调用 `POST /api/search/product` 搜索关键词 `耳机` 成功，返回 `1` 条商品。
- 本阶段未实现 RabbitMQ 商品变更自动同步，商品变更后不会自动同步 ES；自动同步属于阶段 9。

## 阶段 9：RabbitMQ 同步商品变更到 ES

本阶段只实现商品变更后的 RabbitMQ 异步同步链路：`youxuan-product-service` 在商品新增、修改、删除、上架、下架后发送消息，`youxuan-search-service` 消费消息后更新或删除 Elasticsearch 文档。不实现 MinIO、购物车、订单、优惠券、首页运营、Canal、Sentinel、Seata 和秒杀。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos redis rabbitmq elasticsearch
```

检查 RabbitMQ 管理端和 Elasticsearch：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9200

$pair = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("guest:guest"))
$mqHeaders = @{ Authorization = "Basic $pair" }
Invoke-RestMethod -Method Get -Uri "http://localhost:15672/api/overview" -Headers $mqHeaders
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-search-service -am spring-boot:run
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

### 检查 RabbitMQ Exchange 和 Queue

```powershell
$pair = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("guest:guest"))
$mqHeaders = @{ Authorization = "Basic $pair" }

Invoke-RestMethod -Method Get -Uri "http://localhost:15672/api/exchanges/%2F/youxuan.product.exchange" -Headers $mqHeaders
Invoke-RestMethod -Method Get -Uri "http://localhost:15672/api/queues/%2F/youxuan.product.sync-es.queue" -Headers $mqHeaders
```

本阶段使用的 RabbitMQ 配置：

```text
Exchange：youxuan.product.exchange
Queue：youxuan.product.sync-es.queue
RoutingKey：product.sync.es
```

### 测试新增商品自动同步 ES

```powershell
$productBody = @{
  categoryId = 1
  name = "阶段9MQ耳机"
  subtitle = "RabbitMQ同步ES测试"
  mainImage = "http://localhost/demo.jpg"
  detail = "阶段9测试商品"
  price = 199.00
  originalPrice = 299.00
  sales = 9
  status = 1
  stock = 20
} | ConvertTo-Json

$created = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/product -Headers $headers -ContentType "application/json" -Body $productBody
$productId = $created.data.id
Start-Sleep -Seconds 5

$searchBody = @{
  keyword = "阶段9MQ耳机"
  pageNum = 1
  pageSize = 10
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $searchBody
```

### 测试修改、上下架和删除同步

```powershell
$updateBody = @{
  categoryId = 1
  name = "阶段9MQ耳机已修改"
  subtitle = "RabbitMQ修改同步ES测试"
  mainImage = "http://localhost/demo2.jpg"
  detail = "阶段9修改商品"
  price = 188.00
  originalPrice = 288.00
  sales = 19
  status = 1
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/product/$productId" -Headers $headers -ContentType "application/json" -Body $updateBody
Start-Sleep -Seconds 5

$searchUpdatedBody = @{
  keyword = "阶段9MQ耳机已修改"
  pageNum = 1
  pageSize = 10
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $searchUpdatedBody

Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/product/$productId/down" -Headers $headers
Start-Sleep -Seconds 5
Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $searchUpdatedBody

Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/product/$productId/up" -Headers $headers
Start-Sleep -Seconds 5
Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $searchUpdatedBody

Invoke-RestMethod -Method Delete -Uri "http://localhost:9000/api/product/$productId" -Headers $headers
Start-Sleep -Seconds 5
Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $searchUpdatedBody
```

预期结果：

- 新增商品后，不手动调用 `importAll`，搜索接口能查到新商品。
- 修改商品名称后，不手动调用 `importAll`，搜索接口能查到新名称。
- 下架商品后，搜索接口查不到该商品。
- 上架商品后，搜索接口能重新查到该商品。
- 删除商品后，ES 中对应文档被删除，搜索接口查不到该商品。
- product-service 日志包含“商品同步 ES 消息发送成功”。
- search-service 日志包含“收到商品同步 ES 消息”和“商品 ES 文档保存成功 / 删除成功”。

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- `mvn -DskipTests package` 已验证通过，结果为 `BUILD SUCCESS`。
- RabbitMQ 管理端 `http://localhost:15672` 可访问。
- RabbitMQ 中已创建 `Exchange = youxuan.product.exchange`。
- RabbitMQ 中已创建 `Queue = youxuan.product.sync-es.queue`。
- 临时启动 `youxuan-product-service` 和 `youxuan-search-service` 后，`/test/ping` 均返回统一 `Result` 成功。
- 新增商品后不手动调用 `importAll`，搜索接口返回 `createSearchTotal = 1`。
- 修改商品名称后不手动调用 `importAll`，搜索接口返回 `updateSearchTotal = 1`。
- 下架商品后不手动调用 `importAll`，搜索接口返回 `downSearchTotal = 0`。
- 上架商品后不手动调用 `importAll`，搜索接口返回 `upSearchTotal = 1`。
- 删除商品后不手动调用 `importAll`，搜索接口返回 `deleteSearchTotal = 0`。
- RabbitMQ 队列验收时 `queueMessages = 0`，消息已被 search-service 消费。
- 阶段 7 Redis 缓存删除逻辑保留，阶段 9 仅在商品变更后追加 MQ 同步 ES。

## 阶段 10：MinIO 文件服务与商品图片上传

本阶段只完善 `youxuan-file-service` 的 MinIO 商品图片上传和删除能力，并验证商品 `mainImage` 从商品服务到 ES 搜索结果的返回链路。不实现购物车、订单、优惠券、首页运营、Canal、Sentinel、Seata、秒杀、真实支付和复杂权限系统。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos redis rabbitmq elasticsearch minio
```

检查 MinIO：

```powershell
Invoke-WebRequest -UseBasicParsing -Uri http://localhost:9100/minio/health/live
Invoke-WebRequest -UseBasicParsing -Uri http://localhost:9101
```

MinIO Console：

```text
地址：http://localhost:9101
账号：minioadmin
密码：minioadmin
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-search-service -am spring-boot:run
mvn -pl youxuan-file-service -am spring-boot:run
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

### 上传商品图片

表单字段名必须是 `file`，支持 `jpg`、`jpeg`、`png`、`webp`，单文件最大 `5MB`。

```powershell
$imagePath = "D:\temp\demo.png"

$upload = curl.exe -s -X POST `
  -H "Authorization: Bearer $token" `
  -F "file=@$imagePath;type=image/png" `
  http://localhost:9000/api/file/product/image | ConvertFrom-Json

$upload.data
```

返回字段：

```json
{
  "url": "http://localhost:9100/youxuan-mall/products/2026/06/xxx.png",
  "bucket": "youxuan-mall",
  "objectName": "products/2026/06/xxx.png"
}
```

验证返回 URL 可以打开：

```powershell
Invoke-WebRequest -UseBasicParsing -Uri $upload.data.url
```

### 新增商品保存 mainImage

```powershell
$productBody = @{
  categoryId = 1
  name = "阶段10图片商品"
  subtitle = "MinIO图片验收"
  mainImage = $upload.data.url
  detail = "阶段10商品图片测试"
  price = 166.00
  originalPrice = 199.00
  sales = 66
  status = 1
  stock = 10
} | ConvertTo-Json

$created = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/product -Headers $headers -ContentType "application/json" -Body $productBody
$productId = $created.data.id
```

验证商品详情、分页、热门商品和 ES 搜索结果返回 `mainImage`：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/$productId" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/page?pageNum=1&pageSize=10" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/product/home/hot?limit=10" -Headers $headers

$searchBody = @{
  keyword = "阶段10图片商品"
  pageNum = 1
  pageSize = 10
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/search/product -Headers $headers -ContentType "application/json" -Body $searchBody
```

可选：直接确认数据库 `product.main_image` 已保存 URL：

```powershell
docker exec youxuan-mysql mysql -uroot -proot -D youxuan_mall -N -e "SELECT main_image FROM product WHERE id=$productId"
```

### 删除商品图片

删除接口传 `objectName`，不是完整 URL。

```powershell
Invoke-RestMethod -Method Delete `
  -Uri "http://localhost:9000/api/file/product/image?objectName=$([uri]::EscapeDataString($upload.data.objectName))" `
  -Headers $headers
```

删除后再次访问原 URL，应返回 `404`。

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- `mvn -DskipTests package` 已验证通过，结果为 `BUILD SUCCESS`。
- `http://localhost:9100/minio/health/live` 返回 `200`。
- `http://localhost:9101` 返回 `200`，MinIO Console 可打开。
- `youxuan-file-service` 临时启动后注册到 Nacos，Gateway 访问 `/api/file/test/ping` 返回 `filePingCode = 200`。
- 通过 Gateway 登录获取 token 成功，`loginCode = 200`。
- 通过 Gateway 上传 `png`、`jpg`、`webp` 扩展名图片均成功，返回码均为 `200`。
- 上传返回 `bucket = youxuan-mall`，`objectName` 符合 `products/{yyyy}/{MM}/{uuid}.{ext}`。
- 上传返回 URL 可直接访问，HTTP 状态为 `200`。
- 新增商品传入 `mainImage` 后，数据库 `product.main_image` 与上传 URL 一致。
- 商品详情返回 `mainImage` 与上传 URL 一致。
- 商品分页 `records` 中对应商品返回 `mainImage`。
- 热门商品接口返回对应商品 `mainImage`。
- ES 搜索结果返回 `mainImage` 与上传 URL 一致。
- 删除图片接口成功删除 MinIO 对象，删除后访问原 URL 返回 `404`。
- 阶段 7 Redis 缓存删除逻辑和阶段 9 RabbitMQ 同步 ES 链路保持原有行为。
