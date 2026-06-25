# AGENTS.md

# 优选商城 Codex 执行规范

本文件用于指导 Codex 在本仓库中分阶段开发“优选商城”项目。Codex 必须优先阅读并遵守本文档。

项目中文名：优选商城  
项目英文名：youxuan-mall-cloud  
项目类型：Spring Cloud Alibaba 微服务商城  
文档版本：增强修订版，已加入购物车、收货地址、订单确认、发货收货、首页运营位，并修正阶段依赖顺序、版本锁定、订单表最终字段和角色透传规则。  
第一版目标：后端接口完整、Docker 中间件可运行、核心业务链路可演示，并具备接近真实商城的基础购物流程。  
最终目标：后端微服务 + 中间件链路 + 漂亮的 Vue3 前端展示页。

\---

## 1\. 总体执行原则

1. 每次只完成用户指定的阶段，不要提前实现后续阶段。
2. 修改前先阅读已有文件，避免重复创建、覆盖或删除已有内容。
3. 不要一次性生成整个项目，必须小步提交式开发。
4. 每次修改后必须说明：

   * 新增了哪些文件。
   * 修改了哪些文件。
   * 如何启动。
   * 如何测试。
   * 当前阶段是否完成。
5. 不要删除用户已有代码，除非用户明确要求。
6. 不要硬编码本机绝对路径。
7. 不要引入过多无关依赖。
8. 不要做真实支付，只实现模拟支付。
9. 第一版以后端为主，前端最后再做。
10. 第一版先实现 MySQL、Redis、RabbitMQ、Nacos、Elasticsearch、MinIO。
11. Canal、Sentinel、Seata、商品秒杀作为第二阶段增强项。
12. 所有接口统一返回 Result。
13. 所有业务异常统一使用 BusinessException。
14. 每个阶段必须同步更新 README 或 docs 文档。
15. 如果命令失败，先分析原因，不要盲目重建整个项目。
16. 后端依赖版本必须使用本文档指定的稳定兼容组合，不要擅自追最新版。
17. JWT 中必须包含 userId、username、role，Gateway 解析后向下游透传用户 ID 和角色。
18. 所有依赖其他服务的阶段，必须确认被依赖服务已完成并通过验收后再执行。

\---

## 2\. 技术栈

### 2.1 后端

第一版优先使用成熟稳定的兼容组合，不要让 Codex 自动选择最新版本。

* Java 17
* Spring Boot 3.2.x
* Spring Cloud 2023.x
* Spring Cloud Alibaba 2023.x
* Spring Cloud Gateway
* Nacos
* OpenFeign
* MyBatis-Plus 3.5.x
* MySQL 8.0
* Redis 7
* Redisson
* RabbitMQ 3-management
* Elasticsearch 7.17.x
* MinIO
* Knife4j / Swagger
* Docker Compose
* Maven

版本要求：

* 父 pom 中统一管理 Spring Boot、Spring Cloud、Spring Cloud Alibaba、MyBatis-Plus 版本。
* 不要混用 Spring Boot 3.4/3.5 与旧版 Spring Cloud Alibaba。
* 如果依赖冲突，优先调整到本文档指定的稳定组合，不要盲目升级。

### 2.2 前端，最后阶段再做

* Vue3
* Vite
* Element Plus
* Axios
* Pinia，可选
* ECharts，可选

前端要求：浅色、现代、卡片式、适合截图展示，不要做成普通后台管理系统。

\---

## 3\. 项目模块规划

最终项目结构建议：

```text
youxuan-mall-cloud
├── AGENTS.md
├── README.md
├── pom.xml
├── docker
│   ├── docker-compose.yml
│   ├── README.md
│   └── mysql
│       └── init
│           └── 01-init.sql
├── docs
│   ├── 01-docker环境搭建.md
│   ├── 02-项目架构说明.md
│   ├── 03-数据库设计.md
│   ├── 04-接口测试记录.md
│   ├── 05-常见问题排查.md
│   └── 06-后续增强计划.md
├── youxuan-common
├── youxuan-gateway
├── youxuan-user-service
├── youxuan-product-service
├── youxuan-search-service
├── youxuan-file-service
├── youxuan-coupon-service
├── youxuan-order-service
├── youxuan-cart-service
└── youxuan-home-service
```

### 3.1 youxuan-common

公共模块，包含：

* Result
* PageResult
* ErrorCode
* BusinessException
* GlobalExceptionHandler
* BaseEntity
* UserContext，保存当前 userId、username、role
* JwtUtils，生成和解析包含 userId、username、role 的 JWT
* RedisKeyConstants
* RabbitMqConstants
* 通用 DTO / VO

### 3.2 youxuan-gateway

网关服务，端口 9000。

职责：

* 统一入口
* 路由转发
* 跨域配置
* 登录 Token 校验
* 用户 ID 和用户角色透传给下游服务
* 透传请求头：X-User-Id、X-User-Role
* 后续接入 Sentinel 限流

### 3.3 youxuan-user-service

用户服务，端口 9010。

职责：

* 用户注册
* 用户登录
* JWT 生成，JWT 中包含 userId、username、role
* 查询当前用户信息

不做复杂权限系统，只区分：

* USER
* ADMIN

### 3.4 youxuan-product-service

商品服务，端口 9020。

职责：

* 商品分类管理
* 商品管理
* 商品分页查询
* 商品详情查询
* 首页热门商品查询
* 商品库存查询
* 商品库存扣减
* 商品库存恢复
* Redis 缓存商品详情和首页热门商品
* 商品数据变更后发送 RabbitMQ 消息

### 3.5 youxuan-search-service

搜索服务，端口 9030。

职责：

* 创建 Elasticsearch 商品索引
* 导入商品数据到 Elasticsearch
* 商品关键词搜索
* 分类筛选
* 价格区间筛选
* 排序
* 消费 RabbitMQ 商品变更消息
* 更新 Elasticsearch 索引

### 3.6 youxuan-file-service

文件服务，端口 9060。

职责：

* 对接 MinIO
* 创建并检查商品图片 bucket
* 上传商品图片
* 删除商品图片
* 返回可访问的图片 URL
* 为商品服务提供图片上传能力

第一版要求：

* 不使用本地 uploads 目录保存图片。
* 不使用外部 OSS。
* 商品主图统一上传到 MinIO。
* 商品表的 main\_image 保存文件服务返回的图片访问 URL。
* 商品分页、商品详情、ES 搜索结果都必须返回 mainImage。

### 3.7 youxuan-coupon-service

优惠券服务，端口 9040。

职责：

* 优惠券管理
* 优惠券库存预热到 Redis
* 用户领取优惠券
* Redis + Lua 校验库存和重复领取
* RabbitMQ 异步写入用户优惠券表
* 查询用户优惠券
* 查询下单可用优惠券
* 优惠券核销
* 优惠券恢复

### 3.8 youxuan-order-service

订单服务，端口 9050。

职责：

* 创建订单
* 防重复下单
* 查询订单确认信息
* 计算订单价格
* 查询订单详情
* 查询我的订单
* 模拟支付
* 管理员发货
* 用户确认收货
* 手动取消订单
* 订单超时取消
* 库存回滚
* 优惠券恢复



## 3.9 新增真实商城模块规划

为让“优选商城”更接近真实商城项目，在原有用户、商品、搜索、文件、优惠券、订单服务基础上，新增以下第一版增强能力。

### 3.9.1 youxuan-cart-service

购物车服务，端口 9070。

职责：

* 加入购物车
* 修改购物车商品数量
* 删除购物车商品
* 查询我的购物车
* 勾选或取消勾选购物车商品
* 全选或取消全选
* 结算时提供已勾选商品列表

说明：

* 购物车是商城项目的基础链路，第一版建议实现。
* 购物车只保存商品 ID、数量、勾选状态，不保存商品价格。
* 进入订单确认页时，必须重新查询商品服务，获取最新价格、库存、上下架状态。
* 如果商品已下架或库存不足，订单确认页需要返回明确错误。

### 3.9.2 收货地址能力

收货地址能力第一版放在 `youxuan-user-service` 中实现，不单独拆服务，避免微服务数量过多。

职责：

* 新增收货地址
* 修改收货地址
* 删除收货地址
* 查询我的收货地址
* 设置默认收货地址
* 下单时读取地址并写入订单地址快照

说明：

* 用户修改地址后，不能影响历史订单。
* 创建订单时必须把收货人、手机号、完整地址写入订单表。
* 订单表不应只保存 addressId，否则历史订单地址会被后续修改污染。

### 3.9.3 订单确认与价格结算能力

订单确认能力放在 `youxuan-order-service` 中实现。

职责：

* 从购物车勾选商品或立即购买商品生成订单确认信息
* 查询商品最新价格和库存
* 查询用户可用优惠券
* 计算商品总金额
* 计算优惠金额
* 计算实付金额
* 返回订单确认页展示数据

说明：

* `/api/order/confirm` 只计算价格，不创建订单，不扣库存，不核销优惠券。
* `/api/order` 才真正创建订单、扣库存、核销优惠券。
* 创建订单时必须重新校验价格、库存、优惠券，不能完全信任确认页返回结果。

### 3.9.4 发货与确认收货能力

发货与确认收货能力放在 `youxuan-order-service` 中实现。

职责：

* 管理员对已支付订单发货
* 保存物流公司和物流单号
* 用户确认收货
* 订单状态从待收货流转为已完成

说明：

* 第一版不接真实物流接口。
* 只保存物流公司、物流单号、发货时间、收货时间。
* 发货接口只允许 ADMIN 调用。

### 3.9.5 youxuan-home-service

首页运营服务，端口 9080。

职责：

* 首页 Banner 管理
* 首页推荐位管理
* 首页聚合接口
* 返回 Banner、分类、热门商品、推荐商品、新人优惠券等数据

说明：

* 首页运营服务用于让前端更像真实商城，而不是只有普通商品列表。
* 第一版可以只做 Banner 和推荐商品。
* 首页聚合接口可以通过 OpenFeign 调用 product-service 和 coupon-service。
* 首页数据适合使用 Redis 缓存，减少重复查询。

\---

## 4\. 服务端口规划

### 4.1 微服务端口

```text
youxuan-gateway：9000
youxuan-user-service：9010
youxuan-product-service：9020
youxuan-search-service：9030
youxuan-coupon-service：9040
youxuan-order-service：9050
youxuan-file-service：9060
youxuan-cart-service：9070
youxuan-home-service：9080
```

### 4.2 中间件端口

```text
MySQL：宿主机 3307 -> 容器 3306
Redis：宿主机 6379 -> 容器 6379，如果冲突则改成 6380 -> 6379
RabbitMQ：5672
RabbitMQ Management：15672
Nacos：8848
Elasticsearch：9200
MinIO API：宿主机 9100 -> 容器 9000，避免和 gateway 9000 冲突
MinIO Console：宿主机 9101 -> 容器 9001
Kibana：5601，第二阶段可选
Sentinel Dashboard：8858，第二阶段可选
Seata：8091，第二阶段可选
Canal Admin：8089，第二阶段可选
```

注意：用户本机 3306 可能被本地 MySQL 占用，Docker MySQL 必须默认使用 3307:3306。

\---

## 5\. Docker 执行规范

### 5.1 开始前先检查已有容器和镜像

开发 Docker 阶段前，必须提醒用户执行：

```bash
docker ps
docker ps -a
docker images
```

Windows 端口检查命令：

```powershell
netstat -ano | findstr :3306
netstat -ano | findstr :3307
netstat -ano | findstr :6379
netstat -ano | findstr :5672
netstat -ano | findstr :15672
netstat -ano | findstr :8848
netstat -ano | findstr :9200
netstat -ano | findstr :9100
netstat -ano | findstr :9101
```

如果用户已有相关镜像，不要重复下载。  
如果端口冲突，优先修改 docker-compose 的宿主机端口映射，不要强行关闭用户本机服务。

### 5.2 第一版 Docker 容器

第一版需要：

* MySQL 8.0
* Redis 7
* RabbitMQ 3-management
* Nacos standalone
* Elasticsearch 7.17.x
* MinIO

第一版暂不启动：

* Kibana
* Canal
* Sentinel
* Seata

这些作为后续增强。

### 5.3 Docker 网络

所有容器使用统一网络：

```text
youxuan-mall-net
```

### 5.4 MySQL 配置

数据库名：

```text
youxuan\_mall
```

root 密码：

```text
root
```

宿主机连接：

```text
localhost:3307
```

容器内连接：

```text
mysql:3306
```

### 5.5 MinIO 配置

第一版直接使用 MinIO 管理商品图片，不再使用在线图片 URL 或本地 uploads 目录。

容器内端口：

```text
9000：MinIO API
9001：MinIO Console
```

宿主机端口映射：

```text
9100:9000
9101:9001
```

注意：gateway 服务使用 9000 端口，所以 MinIO API 不能映射到宿主机 9000，必须默认使用 9100。

账号：

```text
minioadmin
```

密码：

```text
minioadmin
```

Console 地址：

```text
http://localhost:9101
```

API 地址：

```text
http://localhost:9100
```

默认 bucket：

```text
youxuan-mall
```

商品图片对象路径规则：

```text
products/{yyyy}/{MM}/{uuid}.{ext}
```

文件服务返回给前端和商品服务的图片 URL 格式：

```text
http://localhost:9100/youxuan-mall/products/2026/06/xxx.jpg
```

如果 bucket 不是 public，文件服务需要返回预签名 URL。第一版为了前端展示简单，允许将 youxuan-mall bucket 设置为只读公开访问。

### 5.6 RabbitMQ 配置

账号：

```text
guest
```

密码：

```text
guest
```

管理页面：

```text
http://localhost:15672
```

### 5.7 Nacos 配置

访问地址：

```text
http://localhost:8848/nacos
```

账号：

```text
nacos
```

密码：

```text
nacos
```

### 5.7 Elasticsearch 配置

版本建议：

```text
7.17.18
```

访问地址：

```text
http://localhost:9200
```

单节点配置：

```text
discovery.type=single-node
xpack.security.enabled=false
```

\---

## 6\. 数据库表设计

数据库：

```sql
CREATE DATABASE IF NOT EXISTS youxuan\_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4\_unicode\_ci;
```

### 6.1 sys\_user

```sql
CREATE TABLE sys\_user (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    UNIQUE KEY uk\_username (username)
) COMMENT='用户表';
```

### 6.2 product\_category

```sql
CREATE TABLE product\_category (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '分类ID',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent\_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='商品分类表';
```

### 6.3 product

```sql
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '商品ID',
    category\_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(128) NOT NULL COMMENT '商品名称',
    subtitle VARCHAR(255) DEFAULT NULL COMMENT '副标题',
    main\_image VARCHAR(255) DEFAULT NULL COMMENT '主图',
    detail TEXT COMMENT '商品详情',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original\_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    sales INT NOT NULL DEFAULT 0 COMMENT '销量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1上架 0下架',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    KEY idx\_category\_id (category\_id),
    KEY idx\_status (status),
    KEY idx\_price (price)
) COMMENT='商品表';
```

商品图片说明：

* `main\_image` 保存 MinIO 图片访问 URL，不保存 base64，不保存本地绝对路径。
* 商品新增、修改接口允许传入 `mainImage`。
* 商品分页、商品详情、订单明细、ES 搜索结果都必须返回商品图片。
* 第一版只做商品主图，暂不做商品相册。
* 后续如需多图，再新增 product\_image 表。



### 6.4 product\_stock

```sql
CREATE TABLE product\_stock (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '库存ID',
    product\_id BIGINT NOT NULL COMMENT '商品ID',
    stock INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    locked\_stock INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    UNIQUE KEY uk\_product\_id (product\_id)
) COMMENT='商品库存表';
```

### 6.5 coupon

```sql
CREATE TABLE coupon (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '优惠券ID',
    name VARCHAR(128) NOT NULL COMMENT '优惠券名称',
    amount DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
    min\_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '使用门槛',
    total\_stock INT NOT NULL COMMENT '总库存',
    available\_stock INT NOT NULL COMMENT '剩余库存',
    start\_time DATETIME NOT NULL COMMENT '开始时间',
    end\_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='优惠券表';
```

### 6.6 user\_coupon

```sql
CREATE TABLE user\_coupon (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '用户优惠券ID',
    user\_id BIGINT NOT NULL COMMENT '用户ID',
    coupon\_id BIGINT NOT NULL COMMENT '优惠券ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0未使用 1已使用 2已过期',
    receive\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP COMMENT '领取时间',
    use\_time DATETIME DEFAULT NULL COMMENT '使用时间',
    order\_id BIGINT DEFAULT NULL COMMENT '使用订单ID',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    UNIQUE KEY uk\_user\_coupon (user\_id, coupon\_id),
    KEY idx\_user\_id (user\_id),
    KEY idx\_coupon\_id (coupon\_id)
) COMMENT='用户优惠券表';
```

### 6.7 mall\_order

第一版直接创建最终字段，不要先创建旧表再使用 ALTER TABLE 补字段。

```sql
CREATE TABLE mall\_order (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '订单ID',
    order\_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    user\_id BIGINT NOT NULL COMMENT '用户ID',
    total\_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    discount\_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
    pay\_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待支付 1待发货 2待收货 3已完成 4已取消',
    coupon\_id BIGINT DEFAULT NULL COMMENT '优惠券ID',
    receiver\_name VARCHAR(64) DEFAULT NULL COMMENT '收货人姓名',
    receiver\_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人手机号',
    receiver\_address VARCHAR(512) DEFAULT NULL COMMENT '收货完整地址快照',
    pay\_time DATETIME DEFAULT NULL COMMENT '支付时间',
    cancel\_time DATETIME DEFAULT NULL COMMENT '取消时间',
    delivery\_company VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
    tracking\_no VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
    delivery\_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receive\_time DATETIME DEFAULT NULL COMMENT '确认收货时间',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk\_order\_no (order\_no),
    KEY idx\_user\_id (user\_id),
    KEY idx\_status (status)
) COMMENT='订单表';
```

订单状态固定为：

```text
0 待支付
1 待发货
2 待收货
3 已完成
4 已取消
```

说明：

* 模拟支付成功后，订单状态从待支付变为待发货。
* 管理员发货后，订单状态从待发货变为待收货。
* 用户确认收货后，订单状态从待收货变为已完成。
* 取消订单只允许待支付状态。
* 创建订单时必须保存收货地址快照，不能只保存 addressId。

### 6.8 mall\_order\_item

```sql
CREATE TABLE mall\_order\_item (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '订单明细ID',
    order\_id BIGINT NOT NULL COMMENT '订单ID',
    product\_id BIGINT NOT NULL COMMENT '商品ID',
    product\_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    product\_image VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL COMMENT '下单单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total\_amount DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    KEY idx\_order\_id (order\_id),
    KEY idx\_product\_id (product\_id)
) COMMENT='订单明细表';
```



### 6.9 cart\_item

```sql
CREATE TABLE cart\_item (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '购物车项ID',
    user\_id BIGINT NOT NULL COMMENT '用户ID',
    product\_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    checked TINYINT NOT NULL DEFAULT 1 COMMENT '是否勾选 1勾选 0未勾选',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk\_user\_product (user\_id, product\_id),
    KEY idx\_user\_id (user\_id),
    KEY idx\_product\_id (product\_id)
) COMMENT='购物车表';
```

设计说明：

* 同一用户同一商品只能有一条购物车记录。
* 重复加入购物车时增加数量。
* 购物车不保存商品价格，下单前必须重新查询商品服务。

### 6.10 user\_address

```sql
CREATE TABLE user\_address (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '地址ID',
    user\_id BIGINT NOT NULL COMMENT '用户ID',
    receiver\_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    receiver\_phone VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    province VARCHAR(64) NOT NULL COMMENT '省份',
    city VARCHAR(64) NOT NULL COMMENT '城市',
    district VARCHAR(64) NOT NULL COMMENT '区县',
    detail\_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    default\_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认地址 1是 0否',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    KEY idx\_user\_id (user\_id)
) COMMENT='用户收货地址表';
```

设计说明：

* 每个用户可以有多个地址。
* 设置默认地址时，需要把同一用户的其他地址 default\_flag 改为 0。
* 删除默认地址后，不强制自动选择新的默认地址，第一版可以由用户重新设置。

### 6.11 home\_banner

```sql
CREATE TABLE home\_banner (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT 'Banner ID',
    title VARCHAR(128) NOT NULL COMMENT '标题',
    image\_url VARCHAR(255) NOT NULL COMMENT '图片URL',
    link\_type VARCHAR(32) DEFAULT NULL COMMENT '跳转类型 PRODUCT/CATEGORY/URL/NONE',
    link\_value VARCHAR(255) DEFAULT NULL COMMENT '跳转目标',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    start\_time DATETIME DEFAULT NULL COMMENT '展示开始时间',
    end\_time DATETIME DEFAULT NULL COMMENT '展示结束时间',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    KEY idx\_status\_sort (status, sort)
) COMMENT='首页Banner表';
```

### 6.12 home\_recommend

```sql
CREATE TABLE home\_recommend (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '推荐位ID',
    title VARCHAR(128) NOT NULL COMMENT '推荐标题',
    product\_id BIGINT NOT NULL COMMENT '商品ID',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    KEY idx\_product\_id (product\_id),
    KEY idx\_status\_sort (status, sort)
) COMMENT='首页推荐商品表';
```

### 6.13 订单状态与地址快照说明

`mall\_order` 已在建表 SQL 中包含收货地址快照、发货字段和确认收货字段，不要再额外生成 ALTER TABLE。

订单相关代码必须使用以下固定状态：

```text
0 待支付
1 待发货
2 待收货
3 已完成
4 已取消
```

订单快照规则：

* 用户修改或删除收货地址后，不能影响历史订单。
* 创建订单时必须将收货人、手机号、省市区和详细地址拼接成 `receiver\_address` 保存。
* 订单详情页展示订单表中的地址快照，不再查询当前地址表。

### 6.14 seckill\_activity，第二阶段

```sql
CREATE TABLE seckill\_activity (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '秒杀活动ID',
    product\_id BIGINT NOT NULL COMMENT '商品ID',
    seckill\_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    stock INT NOT NULL COMMENT '秒杀库存',
    start\_time DATETIME NOT NULL COMMENT '开始时间',
    end\_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    update\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='秒杀活动表';
```

### 6.15 seckill\_order，第二阶段

```sql
CREATE TABLE seckill\_order (
    id BIGINT PRIMARY KEY AUTO\_INCREMENT COMMENT '秒杀订单ID',
    user\_id BIGINT NOT NULL COMMENT '用户ID',
    activity\_id BIGINT NOT NULL COMMENT '秒杀活动ID',
    product\_id BIGINT NOT NULL COMMENT '商品ID',
    order\_id BIGINT DEFAULT NULL COMMENT '关联正式订单ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0处理中 1成功 2失败',
    create\_time DATETIME NOT NULL DEFAULT CURRENT\_TIMESTAMP,
    UNIQUE KEY uk\_user\_activity (user\_id, activity\_id)
) COMMENT='秒杀订单表';
```

\---

## 7\. Redis Key 设计

```text
youxuan:login:token:{token}                 -> 用户信息 JSON，过期 24 小时
youxuan:home:hot-products                  -> 首页热门商品 JSON，过期 30 分钟
youxuan:home:categories                     -> 首页分类 JSON，过期 30 分钟
youxuan:product:detail:{productId}          -> 商品详情 JSON，过期 30 分钟
youxuan:coupon:stock:{couponId}             -> 优惠券库存
youxuan:coupon:user:{couponId}              -> 已领取用户 Set
youxuan:order:submit:{userId}:{productId}   -> 防重复下单，过期 10 秒
youxuan:cart:{userId}                       -> 购物车缓存，可选，过期 30 分钟
youxuan:address:default:{userId}            -> 用户默认地址缓存，可选，过期 30 分钟
youxuan:home:banners                        -> 首页 Banner JSON，过期 30 分钟
youxuan:home:recommend-products             -> 首页推荐商品 JSON，过期 30 分钟
youxuan:seckill:stock:{activityId}          -> 秒杀库存，第二阶段
youxuan:seckill:user:{activityId}           -> 秒杀用户记录，第二阶段
```

商品不存在时缓存空值，过期时间 2 分钟，防止缓存穿透。

\---

## 8\. RabbitMQ 设计

### 8.1 Exchange

```text
youxuan.product.exchange
youxuan.coupon.exchange
youxuan.order.exchange
youxuan.seckill.exchange
```

### 8.2 Queue

商品同步 ES：

```text
Queue：youxuan.product.sync-es.queue
Exchange：youxuan.product.exchange
RoutingKey：product.sync.es
```

优惠券领取落库：

```text
Queue：youxuan.coupon.receive.queue
Exchange：youxuan.coupon.exchange
RoutingKey：coupon.receive
```

订单超时取消：

```text
Queue：youxuan.order.timeout.queue
Exchange：youxuan.order.exchange
RoutingKey：order.timeout
```

秒杀异步下单，第二阶段：

```text
Queue：youxuan.seckill.order.queue
Exchange：youxuan.seckill.exchange
RoutingKey：seckill.order
```

### 8.3 消息体

ProductSyncMessage：

```json
{
  "productId": 1,
  "operation": "SAVE\_OR\_UPDATE"
}
```

operation 可选：

```text
SAVE\_OR\_UPDATE
DELETE
DOWN
UP
```

CouponReceiveMessage：

```json
{
  "userId": 1,
  "couponId": 1,
  "receiveTime": "2026-06-24 12:00:00"
}
```

OrderTimeoutMessage：

```json
{
  "orderId": 1,
  "orderNo": "YX202606240001"
}
```

SeckillOrderMessage，第二阶段：

```json
{
  "userId": 1,
  "activityId": 1,
  "productId": 1
}
```

\---

## 9\. Elasticsearch 设计

索引名：

```text
youxuan\_product
```

文档字段：

```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "手机数码",
  "name": "优选无线耳机",
  "subtitle": "高清音质，长续航",
  "mainImage": "https://example.com/a.jpg",
  "price": 199.00,
  "originalPrice": 299.00,
  "sales": 100,
  "status": 1,
  "createTime": "2026-06-24 12:00:00"
}
```

字段类型建议：

```text
id：long
categoryId：long
categoryName：keyword
name：text，同时加 keyword 子字段
subtitle：text
mainImage：keyword
price：double
originalPrice：double
sales：integer
status：integer
createTime：date
```

搜索接口必须支持：

* 关键词搜索
* 分类筛选
* 价格区间筛选
* 按销量排序
* 按价格排序
* 分页

搜索请求 DTO：

```json
{
  "keyword": "耳机",
  "categoryId": 1,
  "minPrice": 100,
  "maxPrice": 500,
  "sortField": "price",
  "sortOrder": "asc",
  "pageNum": 1,
  "pageSize": 10
}
```

\---

## 10\. API 设计

所有接口从 Gateway 访问：

```text
http://localhost:9000
```

### 10.1 用户接口

```http
POST /api/user/register
POST /api/user/login
GET  /api/user/me
```

### 10.2 商品接口

```http
POST   /api/product/category
GET    /api/product/category/list
POST   /api/product
PUT    /api/product/{id}
DELETE /api/product/{id}
GET    /api/product/{id}
GET    /api/product/page?pageNum=1\&pageSize=10
GET    /api/product/home/hot
PUT    /api/product/{id}/up
PUT    /api/product/{id}/down
POST   /api/product/stock/deduct
POST   /api/product/stock/restore
```

### 10.3 搜索接口

```http
POST /api/search/product/importAll
POST /api/search/product
```



### 10.4 购物车接口

```http
POST   /api/cart/add
PUT    /api/cart/update
DELETE /api/cart/{id}
GET    /api/cart/list
PUT    /api/cart/check
PUT    /api/cart/checkAll
DELETE /api/cart/checked
```

接口说明：

* `POST /api/cart/add`：加入购物车，重复加入则增加数量。
* `PUT /api/cart/update`：修改购买数量。
* `PUT /api/cart/check`：勾选或取消勾选单个商品。
* `PUT /api/cart/checkAll`：全选或取消全选。
* `DELETE /api/cart/checked`：删除已勾选商品，订单创建成功后可调用。

### 10.5 收货地址接口

```http
POST   /api/user/address
PUT    /api/user/address/{id}
DELETE /api/user/address/{id}
GET    /api/user/address/list
GET    /api/user/address/default
PUT    /api/user/address/{id}/default
```

### 10.6 首页运营接口

```http
POST   /api/home/banner
PUT    /api/home/banner/{id}
DELETE /api/home/banner/{id}
GET    /api/home/banner/list
POST   /api/home/recommend
DELETE /api/home/recommend/{id}
GET    /api/home/index
```

`GET /api/home/index` 返回：

```json
{
  "banners": \[],
  "categories": \[],
  "hotProducts": \[],
  "recommendProducts": \[],
  "newUserCoupons": \[]
}
```

### 10.7 优惠券接口

```http
POST /api/coupon
GET  /api/coupon/page?pageNum=1\&pageSize=10
POST /api/coupon/{id}/preheat
POST /api/coupon/{id}/receive
GET  /api/coupon/my
GET  /api/coupon/available?amount=199
POST /api/coupon/use
POST /api/coupon/restore
```

### 10.8 订单接口

```http
POST /api/order/confirm
POST /api/order
GET  /api/order/{id}
GET  /api/order/my?pageNum=1\&pageSize=10
POST /api/order/{id}/pay
POST /api/order/{id}/cancel
POST /api/order/{id}/ship
POST /api/order/{id}/receive
```

### 10.9 秒杀接口，第二阶段

```http
GET  /api/seckill/activity/list
POST /api/seckill/{activityId}
GET  /api/seckill/result/{activityId}
```

\---

## 11\. 核心业务流程

### 11.1 商品变更同步 ES

第一版流程：

```text
管理员新增或修改商品
↓
product-service 写入 MySQL
↓
删除商品详情缓存和首页缓存
↓
发送 RabbitMQ 消息 product.sync.es
↓
search-service 消费消息
↓
根据 productId 查询商品详情
↓
写入或更新 Elasticsearch 索引
```

第二阶段增强：

```text
MySQL binlog
↓
Canal
↓
RabbitMQ
↓
search-service
↓
Elasticsearch
```

### 11.2 商品详情缓存

```text
用户查询商品详情
↓
先查 Redis youxuan:product:detail:{productId}
↓
命中则直接返回
↓
未命中则查 MySQL
↓
如果商品不存在，缓存空值 2 分钟
↓
如果商品存在，缓存商品详情 30 分钟
↓
返回结果
```

商品更新时删除缓存。

### 11.3 优惠券领取

```text
用户点击领取优惠券
↓
执行 Redis Lua 脚本
↓
校验优惠券库存是否充足
↓
校验用户是否已经领取
↓
扣减 Redis 库存
↓
记录用户已领取
↓
发送 RabbitMQ 消息
↓
coupon-service 消费消息
↓
写入 user\_coupon 表
↓
扣减 MySQL 优惠券库存
```

Lua 必须保证以下操作原子性：

* 判断库存
* 判断是否重复领取
* 扣库存
* 记录用户领取



### 11.4 购物车结算

```text
用户加入购物车
↓
cart-service 保存 userId、productId、quantity、checked
↓
用户进入购物车页面
↓
cart-service 查询购物车项
↓
通过 OpenFeign 调用 product-service 查询商品最新信息
↓
过滤已下架商品，标记库存不足商品
↓
返回购物车列表
```

### 11.5 订单确认与价格结算

```text
用户从购物车点击结算，或从商品详情点击立即购买
↓
order-service 获取待结算商品
↓
调用 product-service 查询商品最新价格、库存、上下架状态
↓
调用 coupon-service 查询可用优惠券
↓
读取用户默认地址或指定地址
↓
计算 totalAmount、discountAmount、payAmount
↓
返回订单确认页数据
```

注意：

* 订单确认接口不扣库存。
* 订单确认接口不核销优惠券。
* 创建订单时必须重新校验价格、库存、优惠券和地址。

### 11.6 创建订单

```text
用户提交订单
↓
order-service 从请求头获取 userId
↓
使用 Redis SETNX 或 Redisson 防止重复提交
↓
调用 product-service 查询商品
↓
调用 product-service 扣减库存
↓
如果使用优惠券，调用 coupon-service 校验并核销
↓
创建 mall\_order
↓
创建 mall\_order\_item
↓
发送订单超时取消消息
↓
返回订单信息
```

第一版可以暂不使用 Seata。跨服务异常时必须做补偿，例如恢复库存、恢复优惠券。

### 11.7 模拟支付

```text
用户调用支付接口
↓
查询订单
↓
判断订单是否属于当前用户
↓
判断订单状态是否为待支付
↓
修改订单状态为待发货
↓
记录支付时间
```



### 11.8 发货与确认收货

```text
用户完成模拟支付
↓
订单状态变为待发货
↓
管理员调用发货接口
↓
填写物流公司和物流单号
↓
订单状态变为待收货
↓
用户调用确认收货接口
↓
订单状态变为已完成
```

状态校验：

* 只有待发货订单可以发货。
* 只有待收货订单可以确认收货。
* 已取消、已完成订单不能发货或收货。

### 11.9 订单超时取消

```text
订单创建成功
↓
发送延迟消息
↓
延迟时间到达
↓
消费者查询订单状态
↓
如果仍是待支付
↓
取消订单
↓
回滚库存
↓
恢复优惠券状态
```

测试阶段延迟时间可以设为 1 分钟。

### 11.10 商品秒杀，第二阶段

```text
管理员创建秒杀活动
↓
预热秒杀库存到 Redis
↓
用户请求秒杀接口
↓
Lua 判断活动时间、库存、是否重复抢购
↓
Redis 扣减库存
↓
发送 MQ 秒杀下单消息
↓
消费者异步创建订单
↓
用户轮询秒杀结果
```

\---

## 12\. 分阶段任务

阶段执行顺序必须按业务依赖推进，不要跳阶段。

第一版推荐链路：

```text
Docker 环境 → 微服务骨架 → 公共模块与登录鉴权 → 商品/缓存/搜索/图片 → 购物车/地址/优惠券 → 订单确认/下单/支付/发货收货 → 首页运营位 → 联调文档
```

\---

## 阶段 1：Docker 环境搭建

只做 Docker，不创建业务代码。

目标：

* 创建 docker/docker-compose.yml
* 启动 MySQL、Redis、RabbitMQ、Nacos、Elasticsearch、MinIO
* 创建 docker/mysql/init/01-init.sql
* 创建 docker/README.md

要求：

1. 先检查已有 Docker 容器和镜像。
2. 如果镜像已存在，不重复 pull。
3. MySQL 使用 3307:3306。
4. Redis 默认 6379，如果冲突，文档说明改成 6380。
5. MinIO 使用 9100:9000 和 9101:9001，避免和 gateway 9000 冲突。
6. 创建 MinIO bucket：youxuan-mall，并配置为第一版可公开读。
7. 所有容器加入 youxuan-mall-net。
8. 写清楚启动、停止、查看日志、访问地址。

验收：

```bash
docker compose -f docker/docker-compose.yml up -d
docker ps
```

浏览器验证：

```text
Nacos：http://localhost:8848/nacos
RabbitMQ：http://localhost:15672
Elasticsearch：http://localhost:9200
MinIO Console：http://localhost:9101，账号 minioadmin，密码 minioadmin
MinIO API：http://localhost:9100
```

\---

## 阶段 2：Spring Cloud 多模块骨架

目标：

* 创建 Maven 多模块项目
* 创建 common、gateway、user、product、search、file、coupon、order、cart、home 服务
* 所有服务注册到 Nacos
* Gateway 能路由到各服务

要求：

1. Java 17。
2. Spring Boot 3.2.x。
3. Spring Cloud 2023.x。
4. Spring Cloud Alibaba 2023.x。
5. 每个服务有独立 application.yml。
6. 每个服务提供 /test/ping 测试接口。
7. Gateway 统一从 9000 端口访问。
8. 只创建骨架和 ping 接口，不实现业务代码。

验收：

```text
所有服务能启动
Nacos 控制台能看到服务实例
访问 http://localhost:9000/api/product/test/ping 能转发成功
```

\---

## 阶段 3：公共模块与基础配置

目标：

* Result
* PageResult
* ErrorCode
* BusinessException
* GlobalExceptionHandler
* BaseEntity
* UserContext，包含 userId、username、role
* RedisKeyConstants
* RabbitMqConstants
* Knife4j 配置
* MyBatis-Plus 基础配置

验收：

* 接口统一返回 Result。
* 异常能被统一捕获。
* Knife4j 页面可访问。

\---

## 阶段 4：用户服务

目标：

* 用户注册
* 用户登录
* 当前用户查询

接口：

```http
POST /api/user/register
POST /api/user/login
GET  /api/user/me
```

要求：

1. 使用 sys\_user 表。
2. 密码使用 BCrypt。
3. 登录成功返回 JWT。
4. JWT 中必须包含 userId、username、role。
5. 暂不做复杂权限系统，只区分 USER 和 ADMIN。

验收：

* 可以注册用户。
* 可以登录拿到 token。
* 携带 token 可以查询当前用户。

\---

## 阶段 5：Gateway 登录校验与用户透传

目标：

* 网关统一校验 token。
* 解析 userId、role 后透传给下游服务。

要求：

1. 放行登录和注册接口。
2. 其他接口需要 Authorization。
3. Authorization 格式：Bearer token。
4. 网关解析 token。
5. 将 userId 放入请求头 X-User-Id。
6. 将 role 放入请求头 X-User-Role。
7. 下游服务通过拦截器读取 X-User-Id、X-User-Role 并写入 UserContext。
8. 请求结束后清理 UserContext。

验收：

* 不带 token 访问订单接口会被拦截。
* 带 token 可以正常访问。
* 下游服务能拿到当前用户 ID 和用户角色。

\---

## 阶段 6：商品服务 CRUD 与库存

目标：

* 商品分类
* 商品管理
* 商品库存

接口：

```http
POST   /api/product/category
GET    /api/product/category/list
POST   /api/product
PUT    /api/product/{id}
DELETE /api/product/{id}
GET    /api/product/{id}
GET    /api/product/page
PUT    /api/product/{id}/up
PUT    /api/product/{id}/down
POST   /api/product/stock/deduct
POST   /api/product/stock/restore
```

要求：

1. 新增商品时同时初始化库存。
2. 商品详情返回分类信息和库存。
3. 商品分页支持 name、categoryId、status 查询。
4. 扣库存要校验库存是否充足。
5. 恢复库存用于订单取消。

验收：

* 可以新增商品。
* 可以分页查询。
* 可以查看详情。
* 可以扣减库存。
* 库存不足时返回错误。

\---

## 阶段 7：Redis 商品缓存

目标：

* 商品详情缓存。
* 首页热门商品缓存。

要求：

1. 查询商品详情先查 Redis。
2. 未命中查 MySQL，再写入 Redis。
3. 商品不存在时缓存空值，防止缓存穿透。
4. 商品更新、删除、上下架时删除相关缓存。
5. 首页热门商品缓存。
6. 日志打印缓存命中或未命中，方便测试。

验收：

* 第一次查询走 MySQL。
* 第二次查询走 Redis。
* 商品修改后缓存被删除。
* 不存在商品不会反复打到数据库。

\---

## 阶段 8：Elasticsearch 商品搜索

目标：

* 创建商品索引。
* 导入商品到 ES。
* 实现商品搜索。

接口：

```http
POST /api/search/product/importAll
POST /api/search/product
```

要求：

1. 使用索引 youxuan\_product。
2. 支持关键词搜索。
3. 支持分类筛选。
4. 支持价格区间筛选。
5. 支持销量和价格排序。
6. 支持分页。
7. search-service 通过 OpenFeign 调用 product-service 获取商品数据。

验收：

* 商品可以导入 ES。
* 搜索接口可以返回商品。
* 关键词、分类、价格筛选可用。

\---

## 阶段 9：RabbitMQ 同步商品变更到 ES

目标：

* 商品变更后通过 MQ 异步同步 ES。

要求：

1. product-service 商品新增、修改、删除、上下架后发送 MQ 消息。
2. search-service 消费消息。
3. 根据 operation 更新或删除 ES 文档。
4. 消费失败要打印错误日志。

验收：

* 修改商品后不手动导入，ES 也能更新。
* 下架商品后搜索不到。
* 删除商品后 ES 文档被删除。

\---

## 阶段 10：MinIO 文件服务与商品图片上传

目标：

* 接入 MinIO。
* 创建 file-service。
* 实现商品图片上传接口。
* 商品新增、修改、分页、详情、ES 搜索结果均支持 mainImage。

要求：

1. 新增 youxuan-file-service，端口 9060。
2. Gateway 增加路由：/api/file/\*\* -> file-service。
3. file-service 接入 MinIO，连接地址默认 http://localhost:9100。
4. MinIO 账号密码默认 minioadmin/minioadmin。
5. 默认 bucket 为 youxuan-mall。
6. 服务启动时检查 bucket，不存在则创建。
7. 第一版 bucket 允许公开读，方便前端展示商品图片。
8. 上传接口只允许 jpg、jpeg、png、webp。
9. 单文件大小限制 5MB。
10. 文件对象名使用 products/{yyyy}/{MM}/{uuid}.{ext}。
11. 上传成功后返回 url、objectName、bucket。
12. 商品新增和修改接口支持 mainImage 字段。
13. 商品详情、分页、首页热门、ES 文档、搜索结果都返回 mainImage。
14. 前端后续展示时 mainImage 为空使用默认占位图。

接口：

```http
POST /api/file/product/image
DELETE /api/file/product/image?objectName=products/2026/06/xxx.jpg
```

验收：

```text
MinIO Console 可登录
能创建 youxuan-mall bucket
能通过接口上传图片
上传后 MinIO Console 能看到对象
返回的图片 URL 可以在浏览器打开
新增商品时可以保存该图片 URL
商品列表和详情能返回 mainImage
```

\---

## 阶段 11：购物车服务

目标：

* 创建 youxuan-cart-service。
* 实现加入购物车、修改数量、删除、查询、勾选、全选。
* 购物车列表展示商品最新信息。

接口：

```http
POST   /api/cart/add
PUT    /api/cart/update
DELETE /api/cart/{id}
GET    /api/cart/list
PUT    /api/cart/check
PUT    /api/cart/checkAll
DELETE /api/cart/checked
```

要求：

1. 使用 cart\_item 表。
2. 同一用户同一商品只能有一条购物车记录。
3. 重复加入购物车时增加 quantity。
4. 查询购物车时通过 OpenFeign 调用 product-service 查询商品最新信息。
5. 商品下架或库存不足时，列表中要明确标记。
6. 订单创建成功后，可以删除已勾选购物车项。

验收：

* 可以加入购物车。
* 可以修改数量。
* 可以勾选和取消勾选。
* 可以全选和取消全选。
* 可以查询我的购物车。
* 商品价格变化后，购物车展示最新价格。

\---

## 阶段 12：收货地址模块

目标：

* 在 user-service 中实现收货地址管理。
* 下单时支持选择收货地址。

接口：

```http
POST   /api/user/address
PUT    /api/user/address/{id}
DELETE /api/user/address/{id}
GET    /api/user/address/list
GET    /api/user/address/default
PUT    /api/user/address/{id}/default
```

要求：

1. 使用 user\_address 表。
2. 用户只能操作自己的地址。
3. 设置默认地址时，同一用户其他地址 default\_flag 必须置为 0。
4. 删除地址使用逻辑删除。
5. 创建订单时必须把地址写入订单快照字段。

验收：

* 可以新增地址。
* 可以修改地址。
* 可以删除地址。
* 可以设置默认地址。
* 创建订单时能保存收货地址快照。

\---

## 阶段 13：优惠券服务与 Redis + Lua 抢券

目标：

* 优惠券管理。
* 优惠券库存预热。
* 用户领取优惠券。

接口：

```http
POST /api/coupon
GET  /api/coupon/page
POST /api/coupon/{id}/preheat
POST /api/coupon/{id}/receive
GET  /api/coupon/my
GET  /api/coupon/available?amount=199
```

要求：

1. 实现 coupon、user\_coupon 表。
2. 优惠券库存预热到 Redis。
3. 用户领取优惠券使用 Lua 脚本。
4. Lua 脚本判断库存和是否重复领取。
5. 抢券成功后发送 MQ。
6. MQ 消费者异步写入 user\_coupon 表并扣减 MySQL 库存。

验收：

* 优惠券可以创建。
* 库存可以预热。
* 用户可以领取。
* 同一用户不能重复领取。
* 库存不能超卖。
* 领取成功后数据库有记录。

\---

## 阶段 14：订单确认页与价格结算

目标：

* 实现 `/api/order/confirm`。
* 本阶段依赖商品服务、购物车服务、地址模块和优惠券服务均已验收通过。
* 支持从购物车结算或立即购买结算。
* 计算商品总金额、优惠金额、实付金额。

接口：

```http
POST /api/order/confirm
```

请求示例：

```json
{
  "source": "CART",
  "addressId": 1,
  "couponId": 1,
  "items": \[
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

返回示例：

```json
{
  "address": {},
  "items": \[],
  "availableCoupons": \[],
  "selectedCoupon": {},
  "totalAmount": 398.00,
  "discountAmount": 20.00,
  "payAmount": 378.00
}
```

要求：

1. 确认页只计算，不创建订单。
2. 确认页不扣库存。
3. 确认页不核销优惠券。
4. 商品价格必须从 product-service 实时查询。
5. 优惠券必须从 coupon-service 实时查询。
6. 如果商品下架、库存不足、优惠券不可用，要返回明确错误。

验收：

* 可以根据购物车勾选商品生成确认页。
* 可以根据立即购买商品生成确认页。
* 可以返回默认地址。
* 可以返回可用优惠券。
* 可以正确计算应付金额。

\---

## 阶段 15：订单服务、下单、防重复提交

目标：

* 创建订单。
* 防重复下单。
* 查询订单。

接口：

```http
POST /api/order
GET  /api/order/{id}
GET  /api/order/my
```

要求：

1. 使用 mall\_order 和 mall\_order\_item 表。
2. 创建订单时调用 product-service 查询商品和扣库存。
3. 可选 couponId，使用前必须调用 coupon-service 校验并核销。
4. 使用 Redis SETNX 或 Redisson 防止重复提交。
5. 防重复 key：youxuan:order:submit:{userId}:{productId}，过期 10 秒。
6. 订单初始状态为待支付。
7. 创建成功后发送订单超时取消消息。
8. 创建订单时必须保存收货地址快照。
9. 如果创建订单失败，必须补偿恢复库存和优惠券。

验收：

* 可以创建订单。
* 库存会扣减。
* 重复点击不会创建多个订单。
* 可以查询我的订单。
* 订单表中能看到收货地址快照。

\---

## 阶段 16：模拟支付、取消订单、订单超时取消

目标：

* 模拟支付。
* 手动取消订单。
* 超时自动取消订单。

接口：

```http
POST /api/order/{id}/pay
POST /api/order/{id}/cancel
```

要求：

1. 模拟支付只修改订单状态，不接第三方支付。
2. 只有待支付订单可以支付，支付成功后状态变为待发货。
3. 只有待支付订单可以取消。
4. 订单取消后恢复库存。
5. 如果使用了优惠券，恢复优惠券状态。
6. 使用 RabbitMQ 延迟消息或死信队列实现订单超时取消。
7. 测试阶段延迟时间可以设置为 1 分钟。

验收：

* 待支付订单可以支付。
* 已支付订单不能重复支付。
* 待支付订单可以手动取消。
* 超时未支付订单会自动取消。
* 取消后库存恢复。

\---

## 阶段 17：发货与确认收货

目标：

* 本阶段依赖订单创建和模拟支付均已验收通过。
* 模拟支付后订单进入待发货。
* 管理员可以发货。
* 用户可以确认收货。

接口：

```http
POST /api/order/{id}/ship
POST /api/order/{id}/receive
```

要求：

1. 支付成功后订单状态为待发货。
2. 只有 ADMIN 可以调用发货接口。
3. 发货时保存 deliveryCompany、trackingNo、deliveryTime。
4. 发货后订单状态为待收货。
5. 用户确认收货后订单状态为已完成。
6. 只有订单所属用户可以确认收货。

验收：

* 待发货订单可以发货。
* 已取消订单不能发货。
* 待收货订单可以确认收货。
* 已完成订单不能重复确认收货。

\---

## 阶段 18：首页 Banner 与推荐位

目标：

* 本阶段用于补齐商城展示效果，不影响前面交易主链路。
* 创建 youxuan-home-service。
* 实现首页 Banner 管理。
* 实现首页推荐商品管理。
* 实现首页聚合接口。

接口：

```http
POST   /api/home/banner
PUT    /api/home/banner/{id}
DELETE /api/home/banner/{id}
GET    /api/home/banner/list
POST   /api/home/recommend
DELETE /api/home/recommend/{id}
GET    /api/home/index
```

要求：

1. 使用 home\_banner 和 home\_recommend 表。
2. Banner 图片使用 MinIO 返回的 URL。
3. 首页聚合接口返回 Banner、分类、热门商品、推荐商品。
4. 首页聚合数据使用 Redis 缓存。
5. Banner 或推荐位变更后删除首页缓存。

验收：

* 可以新增 Banner。
* 可以查询 Banner 列表。
* 可以新增推荐商品。
* 首页聚合接口可以返回完整首页数据。
* 第二次访问首页聚合接口可以命中 Redis。

\---

## 阶段 19：第一版联调与文档整理

目标：

* 完成第一版后端闭环联调。
* 整理 README 和 docs。

完整演示流程：

```text
启动 Docker
启动所有微服务
注册用户
登录获取 token
新增分类
上传商品图片到 MinIO
新增商品并保存 mainImage
查询商品详情
验证 Redis 缓存
导入商品到 ES
搜索商品
修改商品后验证 MQ 同步 ES
加入购物车并勾选商品
新增收货地址并设置默认地址
创建优惠券
预热优惠券
领取优惠券
进入订单确认页并计算价格
提交订单并扣减库存
模拟支付，订单进入待发货
管理员发货，订单进入待收货
用户确认收货，订单进入已完成
创建订单后不支付，等待超时取消
```

要求：

1. 整理 README.md。
2. 整理 docs/04-接口测试记录.md。
3. 整理 docs/05-常见问题排查.md。
4. 写清楚启动顺序。
5. 写清楚每个服务端口。
6. 写清楚中间件账号密码。

\---

## 阶段 20：第二阶段增强

增强项：

1. Canal 同步商品数据到 ES。
2. Sentinel 对高频接口限流。
3. Seata 处理订单、库存、优惠券跨服务事务。
4. 商品秒杀：Redis + Lua + RabbitMQ 异步下单。
5. Kibana 查看 ES 数据。

注意：这些不要在第一版提前实现。

\---

## 阶段 21：漂亮前端

前端项目名：

```text
youxuan-mall-web
```

页面：

* 登录页
* 注册页
* 商城首页，包含 Banner、分类入口、热门商品、推荐商品
* 商品列表页
* 商品搜索页
* 商品详情页
* 优惠券中心
* 我的优惠券
* 购物车页
* 收货地址管理页
* 订单确认页
* 我的订单页
* 订单物流状态页
* 后台商品管理页
* 后台优惠券管理页

风格要求：

```text
浅色
现代
卡片式布局
圆角
柔和阴影
商品图片展示明显
首页要有 Banner
搜索栏要醒目
页面要适合截图展示
不要做得像后台管理系统
```

推荐配色：

```text
主色：#2F80ED 或 #3B82F6
背景：#F6F8FB
卡片背景：#FFFFFF
文字主色：#1F2937
文字次色：#6B7280
成功色：#22C55E
警告色：#F59E0B
危险色：#EF4444
```

所有接口统一请求：

```text
http://localhost:9000
```

Header：

```text
Authorization: Bearer token
```

后端内部透传请求头：

```text
X-User-Id: 当前用户ID
X-User-Role: 当前用户角色 USER/ADMIN
```

\---

## 13\. 当前推荐执行命令

如果用户刚开始项目，第一次只执行阶段 1。

用户给 Codex 的第一条任务应为：

```text
请阅读 AGENTS.md，然后只执行“阶段 1：Docker 环境搭建”。
不要创建任何 Spring Boot 业务代码。
不要实现微服务。
不要实现数据库业务表。
只创建 docker-compose.yml、MySQL 初始化目录、docker/README.md。
完成后告诉我新增或修改了哪些文件，以及我应该运行哪些命令来测试 Docker 环境。
```

\---

## 14\. 第一版完成标准

第一版完成后应满足：

1. Docker Compose 能启动 MySQL、Redis、RabbitMQ、Nacos、Elasticsearch、MinIO。
2. 所有微服务能启动并注册到 Nacos。
3. Gateway 能统一转发请求。
4. 用户可以注册、登录、获取 token。
5. 商品可以新增、修改、查询、上下架。
6. 商品详情和首页热门商品使用 Redis 缓存。
7. 商品图片可以上传到 MinIO，商品信息可以保存并返回 mainImage。
8. 商品可以导入 Elasticsearch。
9. 商品搜索支持关键词、分类、价格和排序。
10. 商品变更能通过 RabbitMQ 同步到 Elasticsearch。
11. 优惠券可以创建、预热、领取。
12. 优惠券领取使用 Redis + Lua 防止超卖和重复领取。
13. 领取成功后通过 RabbitMQ 异步写入数据库。
14. 用户可以创建订单。
15. 下单时可以扣减库存。
16. 下单时可以使用优惠券。
17. 重复提交订单会被拦截。
18. 用户可以模拟支付。
19. 超时未支付订单可以自动取消。
20. 取消订单后可以恢复库存和优惠券。
21. 用户可以加入购物车、修改数量、勾选商品并结算。
22. 用户可以新增、修改、删除收货地址，并设置默认地址。
23. 订单确认页可以计算商品总价、优惠金额和实付金额。
24. 创建订单时可以保存收货地址快照。
25. 管理员可以对待发货订单发货。
26. 用户可以确认收货，订单变为已完成。
27. 首页可以展示 Banner 和推荐商品。
28. README 和测试文档完整。

\---

## 15\. 禁止行为

Codex 不应执行以下行为：

1. 不要一次性实现所有阶段。
2. 不要强行启动所有增强中间件。
3. 不要默认使用宿主机 3306 作为 Docker MySQL 端口。
4. 不要接入真实支付。
5. 不要生成复杂权限系统。
6. 不要删除用户已有 Docker 容器。
7. 不要删除用户已有项目文件。
8. 不要硬编码绝对路径。
9. 不要写大量无用模板类。
10. 不要在第一版实现 Canal、Sentinel、Seata、秒杀，除非用户明确要求进入第二阶段。

\---

## 16\. 每次完成后的回复格式

Codex 每次完成阶段后，必须按以下格式回复用户：

```text
本阶段已完成：阶段 X：xxx

新增文件：
- xxx

修改文件：
- xxx

如何启动：
1. xxx
2. xxx

如何测试：
1. xxx
2. xxx

注意事项：
- xxx

下一步建议：
- 进入阶段 X+1：xxx
```

