# 优选商城

## 当前阶段

阶段 4：用户服务。

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

未实现 Gateway 登录校验，未实现商品 CRUD、订单、优惠券、购物车、地址等后续业务。

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
Invoke-RestMethod -Method Get -Uri http://localhost:9000/api/user/me -Headers @{ "X-User-Id" = "1" }
```

服务连通性：

```powershell
Invoke-RestMethod http://localhost:9000/api/user/test/ping
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
