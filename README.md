# 优选商城

## 当前阶段

阶段 3：公共模块与基础配置。

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

未实现用户注册、登录、JWT、商品 CRUD、订单、优惠券等业务功能，也未创建业务表，未编写 Redis、RabbitMQ、Elasticsearch、MinIO 业务代码。

## 版本

- Java 17
- Spring Boot 3.2.6
- Spring Cloud 2023.0.2
- Spring Cloud Alibaba 2023.0.1.0
- MyBatis-Plus 3.5.7
- Knife4j 4.5.0

## 启动 Nacos

```powershell
docker compose -f docker/docker-compose.yml up -d nacos
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

直连 user-service：

```powershell
Invoke-RestMethod http://localhost:9010/test/ping
```

直连 product-service：

```powershell
Invoke-RestMethod http://localhost:9020/test/ping
```

直连 order-service：

```powershell
Invoke-RestMethod http://localhost:9050/test/ping
```

通过 gateway 访问 user-service：

```powershell
Invoke-RestMethod http://localhost:9000/api/user/test/ping
```

通过 gateway 访问 product-service：

```powershell
Invoke-RestMethod http://localhost:9000/api/product/test/ping
```

测试 product-service 业务异常统一返回：

```powershell
Invoke-RestMethod http://localhost:9020/test/error
```

测试 gateway 转发后的业务异常统一返回：

```powershell
Invoke-RestMethod http://localhost:9000/api/product/test/error
```

通过 gateway 访问 order-service：

```powershell
Invoke-RestMethod http://localhost:9000/api/order/test/ping
```

预期返回：

```json
{
  "code": 200,
  "message": "success",
  "data": "pong"
}
```

异常测试预期返回：

```json
{
  "code": 5000,
  "message": "测试业务异常",
  "data": null
}
```
