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

## 阶段 11：购物车服务

本阶段只完善 `youxuan-cart-service` 的购物车能力：加入购物车、修改数量、删除、查询、勾选、全选和删除已勾选商品。购物车表只保存 `userId`、`productId`、`quantity`、`checked`，商品名称、价格、主图、库存和上下架状态均通过 OpenFeign 实时调用 `youxuan-product-service` 获取。

本阶段不实现收货地址、优惠券、订单、订单确认页、支付、首页运营服务、前端、Canal、Sentinel、Seata 和秒杀，也未修改 `docker/docker-compose.yml`。

### 数据库表

新增表：

- `cart_item`

建表 SQL 已追加到 `docker/mysql/init/01-init.sql`。如果本机 MySQL 容器已经初始化过数据卷，Docker 不会自动重新执行 init SQL，需要手动连接 `localhost:3307` 的 `youxuan_mall` 数据库执行 `cart_item` 建表语句。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos redis rabbitmq elasticsearch minio
```

阶段 11 核心链路至少需要 `mysql`、`nacos`，购物车查询商品最新信息需要 `youxuan-product-service` 正常运行。

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-cart-service -am spring-boot:run
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

### 购物车接口测试

加入购物车，重复加入同一商品会增加数量：

```powershell
$addBody = @{
  productId = 1
  quantity = 2
} | ConvertTo-Json

$cartItem = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/cart/add -Headers $headers -ContentType "application/json" -Body $addBody
$cartItemId = $cartItem.data.cartItemId
```

查询我的购物车：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/cart/list -Headers $headers
```

返回字段包含：

```text
cartItemId, productId, productName, mainImage, price, quantity, checked, stock, status, totalAmount, offShelf, stockInsufficient, invalidReason
```

修改数量：

```powershell
$updateBody = @{
  cartItemId = $cartItemId
  quantity = 3
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri http://localhost:9000/api/cart/update -Headers $headers -ContentType "application/json" -Body $updateBody
```

勾选或取消勾选单个商品：

```powershell
$checkBody = @{
  cartItemId = $cartItemId
  checked = 0
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri http://localhost:9000/api/cart/check -Headers $headers -ContentType "application/json" -Body $checkBody
```

全选或取消全选：

```powershell
$checkAllBody = @{
  checked = 1
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri http://localhost:9000/api/cart/checkAll -Headers $headers -ContentType "application/json" -Body $checkAllBody
```

删除单个购物车项：

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:9000/api/cart/$cartItemId" -Headers $headers
```

删除已勾选商品：

```powershell
Invoke-RestMethod -Method Delete -Uri http://localhost:9000/api/cart/checked -Headers $headers
```

### 验收要点

- Gateway 已有 `/api/cart/**` 路由，访问时会透传 `X-User-Id`。
- `youxuan-cart-service` 从 `UserContext` 获取当前用户 ID。
- 用户只能查询和操作自己的购物车项。
- 同一用户同一商品只保留一条购物车记录。
- 商品价格、名称、图片、库存、上下架状态不存入购物车表，查询时实时从商品服务获取。
- 商品下架时 `offShelf = true`，库存不足时 `stockInsufficient = true`，并返回 `invalidReason`。

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- `mvn -q -DskipTests package` 已验证通过。
- Docker 中 `mysql`、`nacos`、`redis`、`rabbitmq`、`elasticsearch`、`minio` 均处于运行状态。
- 既有 MySQL 数据卷不会自动重放 init SQL，本地验收前已手动执行 `cart_item` 建表语句。
- 临时启动 `youxuan-user-service`、`youxuan-product-service`、`youxuan-cart-service`、`youxuan-gateway` 后，Gateway 访问 `/api/cart/test/ping` 返回 `200`。
- 通过 Gateway 注册、登录并获取 token 成功。
- 新增测试商品后，携带 token 调用 `POST /api/cart/add` 成功。
- 重复加入同一商品复用同一 `cartItemId`，数量从 `1` 累加到 `2`，未新增重复记录。
- `PUT /api/cart/update` 将数量改为 `3` 后，因测试商品库存为 `2`，返回 `stockInsufficient = true`。
- `PUT /api/cart/check` 可将单个购物车项取消勾选，返回 `checked = 0`。
- `PUT /api/cart/checkAll` 可全选当前用户购物车。
- 商品下架后查询购物车返回 `offShelf = true`。
- `DELETE /api/cart/checked` 可删除已勾选商品。
- `DELETE /api/cart/{id}` 可删除单个购物车项。
- 第二个测试用户查询购物车返回空列表，验证用户数据隔离。
- 购物车列表返回商品 `mainImage`、`price`、`stock` 等来自商品服务的实时字段。

## 阶段 12：收货地址模块

本阶段只在 `youxuan-user-service` 中实现收货地址管理，不新建独立 address-service，不实现订单确认页、创建订单、优惠券、支付、发货、确认收货、首页运营、前端、Canal、Sentinel、Seata 和秒杀，也未修改 `docker/docker-compose.yml`。

### 数据库表

新增表：

- `user_address`

建表 SQL 已追加到 `docker/mysql/init/01-init.sql`。如果本机 MySQL 容器已经初始化过数据卷，Docker 不会自动重新执行 init SQL，需要手动连接 `localhost:3307` 的 `youxuan_mall` 数据库执行 `user_address` 建表语句。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-gateway -am spring-boot:run
```

所有地址接口统一从 Gateway 访问：

```text
http://localhost:9000/api/user/address/**
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

### 收货地址接口测试

新增地址：

```powershell
$addressBody = @{
  receiverName = "张三"
  receiverPhone = "13800000000"
  province = "广东省"
  city = "深圳市"
  district = "南山区"
  detailAddress = "科技园1号楼"
  defaultFlag = 1
} | ConvertTo-Json

$address = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/user/address -Headers $headers -ContentType "application/json" -Body $addressBody
$addressId = $address.data.id
```

查询我的地址列表：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/user/address/list -Headers $headers
```

查询默认地址：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/user/address/default -Headers $headers
```

修改地址：

```powershell
$updateAddressBody = @{
  receiverName = "李四"
  receiverPhone = "13900000000"
  province = "广东省"
  city = "广州市"
  district = "天河区"
  detailAddress = "体育西路100号"
  defaultFlag = 0
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/user/address/$addressId" -Headers $headers -ContentType "application/json" -Body $updateAddressBody
```

设置默认地址：

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:9000/api/user/address/$addressId/default" -Headers $headers
```

删除地址：

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:9000/api/user/address/$addressId" -Headers $headers
```

### 验收要点

- 地址接口通过 `UserContext` 获取当前登录用户 ID。
- 用户只能查询、修改、删除自己的地址。
- 新增第一个地址会自动设置为默认地址。
- 新增或修改地址时如果 `defaultFlag = 1`，同一用户其他地址会自动置为 `0`。
- 设置默认地址时，同一用户其他地址会自动置为 `0`。
- 删除地址使用逻辑删除，删除默认地址后不强制自动选择新的默认地址。
- 查询默认地址没有数据时返回统一 `Result`，`data = null`。

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- `mvn -q -DskipTests package` 已验证通过。
- Docker 中 `mysql`、`nacos` 处于运行状态。
- 既有 MySQL 数据卷不会自动重放 init SQL，本地验收前已手动执行 `user_address` 建表语句。
- 临时启动 `youxuan-user-service` 和 `youxuan-gateway` 后，Gateway 访问 `/api/user/test/ping` 返回 `200`。
- 通过 Gateway 注册、登录并获取 token 成功。
- 无默认地址时，`GET /api/user/address/default` 返回 `data = null`。
- 第一次新增地址即使传入 `defaultFlag = 0`，也会自动返回 `defaultFlag = 1`。
- 第二次新增地址传入 `defaultFlag = 1` 后，默认地址切换为第二个地址。
- 查询地址列表返回当前用户的两条地址。
- 修改第一个地址并传入 `defaultFlag = 1` 后，默认地址切换为第一个地址，且同一用户默认地址数量保持为 `1`。
- 调用 `PUT /api/user/address/{id}/default` 可设置默认地址，且同一用户默认地址数量保持为 `1`。
- 第二个测试用户查询地址列表返回空列表，验证用户数据隔离。
- 第二个测试用户尝试修改第一个用户地址，返回统一 `Result` 业务码 `404`，提示 `收货地址不存在`。
- 删除默认地址后，地址列表不再显示该地址，且不自动选择新的默认地址。
- 本阶段未实现订单、优惠券、支付等后续阶段能力。

## 阶段 13：优惠券服务与 Redis + Lua 抢券

本阶段只完善 `youxuan-coupon-service` 的优惠券创建、分页、库存预热、Redis + Lua 领取、RabbitMQ 异步落库、我的优惠券和下单可用优惠券查询。不实现订单确认页、创建订单、支付、订单取消、发货、确认收货、首页运营、前端、Canal、Sentinel、Seata、秒杀，也未修改 `docker/docker-compose.yml`。

### 数据库表

新增表：

- `coupon`
- `user_coupon`

建表 SQL 已追加到 `docker/mysql/init/01-init.sql`。如果本机 MySQL 容器已经初始化过数据卷，Docker 不会自动重新执行 init SQL，需要手动连接 `localhost:3307` 的 `youxuan_mall` 数据库执行这两张表的建表语句。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos redis rabbitmq
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-coupon-service -am spring-boot:run
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

### 优惠券接口测试

创建优惠券：

```powershell
$couponBody = @{
  name = "新人满100减20"
  amount = 20
  minAmount = 100
  totalStock = 100
  availableStock = 100
  startTime = "2026-06-26 00:00:00"
  endTime = "2026-12-31 23:59:59"
  status = 1
} | ConvertTo-Json

$coupon = Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/coupon -Headers $headers -ContentType "application/json" -Body $couponBody
$couponId = $coupon.data.id
```

分页查询：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/coupon/page?pageNum=1&pageSize=10" -Headers $headers
```

预热库存到 Redis：

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:9000/api/coupon/$couponId/preheat" -Headers $headers
docker exec youxuan-redis redis-cli GET "youxuan:coupon:stock:$couponId"
```

领取优惠券：

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:9000/api/coupon/$couponId/receive" -Headers $headers
```

查询我的优惠券：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/coupon/my -Headers $headers
```

查询下单可用优惠券：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/coupon/available?amount=199" -Headers $headers
```

### 本阶段 Redis Key

```text
youxuan:coupon:stock:{couponId}
youxuan:coupon:user:{couponId}
```

### 本阶段 RabbitMQ

```text
Exchange：youxuan.coupon.exchange
Queue：youxuan.coupon.receive.queue
RoutingKey：coupon.receive
```

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- `mvn -q -DskipTests package` 已验证通过。
- Docker 中 `mysql`、`nacos`、`redis`、`rabbitmq` 均处于运行状态。
- 既有 MySQL 数据卷不会自动重放 init SQL，本地验收前已手动执行 `coupon`、`user_coupon` 建表语句。
- 临时启动 `youxuan-user-service`、`youxuan-coupon-service`、`youxuan-gateway` 后，Gateway 访问 `/api/coupon/test/ping` 返回 `200`。
- 通过 Gateway 注册、登录并获取 token 成功。
- 调用 `POST /api/coupon` 创建优惠券成功。
- 调用 `GET /api/coupon/page` 分页查询可返回新建优惠券。
- 调用 `POST /api/coupon/{id}/preheat` 成功，Redis 中 `youxuan:coupon:stock:{couponId}` 写入数据库剩余库存。
- 调用 `POST /api/coupon/{id}/receive` 领取成功，Lua 会原子校验重复领取、库存、扣减 Redis 库存和记录已领取用户。
- Redis 中 `youxuan:coupon:stock:{couponId}` 从 `1` 扣减为 `0`，`youxuan:coupon:user:{couponId}` 记录当前用户。
- 重复领取返回统一 `Result` 业务码 `5000`，提示 `不能重复领取优惠券`。
- 第二个用户在库存为 `0` 时领取返回统一 `Result` 业务码 `5000`，提示库存不足，验证不超卖。
- RabbitMQ 领取消息已被 coupon-service 消费，`user_coupon` 表有领取记录。
- `coupon.available_stock` 已从 `1` 扣减为 `0`。
- 当前用户调用 `GET /api/coupon/my` 返回已领取优惠券，另一个用户返回空列表。
- 当前用户调用 `GET /api/coupon/available?amount=199` 返回可用券，`amount=50` 返回空列表。
- 本阶段未实现订单、支付、核销、恢复等后续阶段能力。

## 阶段 14：订单确认页与价格结算

本阶段只在 `youxuan-order-service` 中实现 `POST /api/order/confirm`，用于生成订单确认页展示数据和价格计算。不创建订单、不生成订单号、不扣库存、不核销优惠券、不恢复优惠券、不删除购物车、不支付、不取消订单、不处理发货收货，也未修改 `docker/docker-compose.yml`。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos redis rabbitmq
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-coupon-service -am spring-boot:run
mvn -pl youxuan-order-service -am spring-boot:run
mvn -pl youxuan-gateway -am spring-boot:run
```

### 订单确认接口

接口：

```http
POST http://localhost:9000/api/order/confirm
Authorization: Bearer <token>
Content-Type: application/json
```

BUY_NOW 示例：

```powershell
$confirmBody = @{
  source = "BUY_NOW"
  addressId = 1
  couponId = 1
  items = @(
    @{
      productId = 1
      quantity = 2
    }
  )
} | ConvertTo-Json -Depth 6

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/order/confirm -Headers $headers -ContentType "application/json" -Body $confirmBody
```

CART 示例：

```powershell
$cartConfirmBody = @{
  source = "CART"
  addressId = 1
  items = @(
    @{
      productId = 1
      quantity = 1
    }
  )
} | ConvertTo-Json -Depth 6

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/order/confirm -Headers $headers -ContentType "application/json" -Body $cartConfirmBody
```

### 实现说明

- `order-service` 通过 `UserContext` 获取当前用户 ID。
- 通过 OpenFeign 调用 `product-service` 查询商品最新名称、图片、价格、库存、上下架状态。
- 通过 OpenFeign 调用 `user-service` 查询当前用户的地址详情，并由 user-service 继续校验地址归属。
- 通过 OpenFeign 调用 `coupon-service` 查询当前用户在订单金额下可用的优惠券。
- `couponId` 为空时，`discountAmount = 0`。
- `couponId` 不为空时，必须存在于当前用户可用优惠券列表，否则返回业务异常。
- `payAmount = totalAmount - discountAmount`，最低为 `0`。

### 本地验收记录

验收日期：2026-06-26

已验证内容：

- `mvn -q -DskipTests package` 已验证通过。
- Docker 中 `mysql`、`nacos`、`redis`、`rabbitmq` 均处于运行状态。
- 临时启动 `youxuan-user-service`、`youxuan-product-service`、`youxuan-coupon-service`、`youxuan-order-service`、`youxuan-gateway` 后，Gateway 访问 `/api/order/test/ping` 返回 `200`。
- 通过 Gateway 注册、登录并获取 token 成功。
- 准备测试商品、收货地址、已领取优惠券后，`BUY_NOW` 模式确认订单成功。
- `couponId` 为空时，`totalAmount = 398.00`、`discountAmount = 0`、`payAmount = 398.00`。
- `couponId` 有效时，`discountAmount = 20.00`、`payAmount = 378.00`。
- `CART` 模式确认订单成功，本阶段仅使用请求中的 items 计算，不删除购物车。
- 响应中返回商品名称、图片、价格、数量、小计、库存、状态。
- 响应中返回收货地址、可用优惠券列表和选中优惠券。
- 商品库存不足时返回统一 `Result` 业务码 `5000`，提示 `商品库存不足`。
- 地址不存在时返回统一 `Result` 业务码 `404`，提示 `收货地址不存在`。
- 优惠券不可用时返回统一 `Result` 业务码 `5000`，提示 `优惠券不可用`。
- 商品下架后确认订单返回统一 `Result` 业务码 `5000`，提示 `商品已下架`。
- 确认前后 `product_stock.stock` 保持不变，未扣减库存。
- 确认前后 `user_coupon.status` 保持 `0`，未核销优惠券。
- 本阶段未创建 `mall_order` 和 `mall_order_item` 数据；本地库中这两张表也未被本阶段创建。

## 阶段 15：订单服务、下单、防重复提交

本阶段在 `youxuan-order-service` 中实现创建待支付订单、订单详情查询和我的订单分页查询。创建订单时会重新查询商品、地址和优惠券，不信任阶段 14 确认页结果；会扣减商品库存、核销优惠券、保存收货地址快照，并使用 Redis `SETNX` 防止短时间重复提交。本阶段不实现支付、取消订单、订单超时取消消费者、发货、确认收货，也未修改 `docker/docker-compose.yml`。

### 数据库表

已在 `docker/mysql/init/01-init.sql` 追加：

- `mall_order`
- `mall_order_item`

已有 MySQL 数据卷不会自动重新执行 init SQL，需要手动执行本阶段新增的两张订单表建表语句。

### 启动依赖

```powershell
docker compose -f docker/docker-compose.yml up -d mysql nacos redis rabbitmq
```

启动服务：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
mvn -pl youxuan-product-service -am spring-boot:run
mvn -pl youxuan-coupon-service -am spring-boot:run
mvn -pl youxuan-cart-service -am spring-boot:run
mvn -pl youxuan-order-service -am spring-boot:run
mvn -pl youxuan-gateway -am spring-boot:run
```

### 订单接口

创建订单：

```http
POST http://localhost:9000/api/order
Authorization: Bearer <token>
Content-Type: application/json
```

BUY_NOW 示例：

```powershell
$orderBody = @{
  source = "BUY_NOW"
  addressId = 1
  couponId = 1
  items = @(
    @{
      productId = 1
      quantity = 2
    }
  )
} | ConvertTo-Json -Depth 6

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/order -Headers $headers -ContentType "application/json" -Body $orderBody
```

CART 示例：

```powershell
$cartOrderBody = @{
  source = "CART"
  addressId = 1
  items = @(
    @{
      productId = 1
      quantity = 1
    }
  )
} | ConvertTo-Json -Depth 6

Invoke-RestMethod -Method Post -Uri http://localhost:9000/api/order -Headers $headers -ContentType "application/json" -Body $cartOrderBody
```

查询订单详情：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/order/1" -Headers $headers
```

查询我的订单：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:9000/api/order/my?pageNum=1&pageSize=10" -Headers $headers
```

### 实现说明

- 创建订单通过 `UserContext` 获取当前用户 ID。
- 防重复提交 Key：`youxuan:order:submit:{userId}:{productId}`，过期时间 10 秒。
- 商品名称、图片、价格、库存、上下架状态均通过 `product-service` 实时查询。
- 创建订单时调用 `product-service` 的 `/stock/deduct` 扣减库存；失败时调用 `/stock/restore` 补偿已扣减库存。
- 使用优惠券时调用 `coupon-service` 的 `/use` 核销优惠券；后续失败时调用 `/restore` 恢复优惠券。
- 收货地址通过 `user-service` 查询，并在 `mall_order` 保存 `receiver_name`、`receiver_phone`、`receiver_address` 快照。
- `mall_order.status` 初始固定为 `0`，表示待支付。
- 查询订单详情和我的订单分页只返回当前用户自己的订单。
- `source = CART` 创建成功后，会在事务提交后调用 `cart-service` 删除当前用户已勾选购物车项。

### 本地验收记录

验收日期：2026-06-27

已验证内容：

- `mvn -q -DskipTests package` 已验证通过。
- 当前机器 8848 端口位于 Windows TCP 排除范围内，Compose 中的 Nacos 无法绑定 8848；本次验收未修改 `docker/docker-compose.yml`，临时使用 `youxuan-nacos-stage15` 容器映射 `18848:8848`，并通过启动参数覆盖各服务 Nacos 地址为 `localhost:18848`。
- 已手动执行 `docker/mysql/init/01-init.sql`，确认 `mall_order` 和 `mall_order_item` 表存在。
- Gateway 访问 `/api/user/test/ping`、`/api/product/test/ping`、`/api/coupon/test/ping`、`/api/cart/test/ping`、`/api/order/test/ping` 均返回 `200`。
- 登录后 `BUY_NOW` 模式创建订单成功，`mall_order` 和 `mall_order_item` 均写入记录。
- 新建订单状态为 `0` 待支付。
- 创建订单后 `product_stock.stock` 从 `8` 扣减为 `6`，`locked_stock` 从 `0` 增加为 `2`。
- 使用优惠券下单后，`totalAmount = 398.00`、`discountAmount = 20.00`、`payAmount = 378.00`。
- 使用优惠券后 `user_coupon.status = 1`，并写入对应 `order_id`。
- 创建订单时已保存收货地址快照；通过 `HEX(receiver_name)` 确认中文收货人真实保存为 UTF-8，终端中显示 `??` 是 Docker/MySQL 输出到 PowerShell 的显示编码问题。
- 快速重复提交同一商品订单返回业务码 `5000`，提示 `请勿重复提交订单`。
- `GET /api/order/{id}` 当前用户查询成功，其他用户查询同一订单返回 `404`。
- `GET /api/order/my?pageNum=1&pageSize=10` 可以返回当前用户订单分页。
- `CART` 模式创建订单成功，库存正常扣减。
- `CART` 模式创建订单成功后，已勾选购物车项被删除。
- 商品库存不足时创建订单失败，返回 `商品库存不足`。
- 商品下架时创建订单失败，返回 `商品已下架`。
- 优惠券不可用时创建订单失败，返回 `优惠券不可用`。
- 本阶段代码未实现支付、取消订单、订单超时取消消费者、发货、确认收货。
- 本阶段未修改 `docker/docker-compose.yml`。
