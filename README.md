# 优选商城

## 当前阶段

阶段 2A：Spring Cloud 基础骨架。

本阶段只包含：

- 父工程 `pom.xml`
- `youxuan-common`
- `youxuan-gateway`
- `youxuan-user-service`

未实现用户注册、登录、JWT、数据库、MyBatis-Plus，也未创建商品、订单、优惠券、购物车、首页、搜索、文件等后续业务模块。

## 版本

- Java 17
- Spring Boot 3.2.6
- Spring Cloud 2023.0.2
- Spring Cloud Alibaba 2023.0.1.0

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

启动 user-service：

```powershell
mvn -pl youxuan-user-service -am spring-boot:run
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

通过 gateway：

```powershell
Invoke-RestMethod http://localhost:9000/api/user/test/ping
```

预期返回：

```json
{
  "code": 200,
  "message": "success",
  "data": "pong"
}
```
