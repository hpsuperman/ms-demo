# CLAUDE.md

## 项目概述

**ms-demo**：Maven 多模块分布式微服务学习骨架（Spring Cloud Alibaba），把单体 `java-app` 的功能重新从 0 到 1 实现为微服务。

- **当前状态**：骨架已跑通。ms-common / ms-file / ms-gateway 完整可用；**ms-user / ms-order 只有启动类和配置，无业务代码**——业务重建尚未开始
- **当前目标**：从 0 到 1 重建业务，一次一个服务。功能与接口参考 java-app（见"目标功能清单"），**数据库表结构自行设计，不沿用 java-app**
- 技术栈：JDK 17 / Maven 3.9.9 / Spring Boot 3.2.4 / Spring Cloud 2023.0.1 / Spring Cloud Alibaba 2023.0.1.0 / Nacos 2.3.2 / MyBatis Plus 3.5.12 / MySQL 8.0 / Redis / MinIO / Lombok
- 参考实现：`D:\hpsuperman\project\java-app`（单体，功能与接口的参考）
- 版本控制：git 已初始化（main 分支），`.gitignore` 排除 `target/` `.idea/` `logs/` `work/` `derby.log` 等

## 常用命令

```bash
mvn -pl ms-common install -DskipTests      # 编译安装公共模块
mvn -pl ms-user -am package -DskipTests    # 构建单个服务（-am 连带依赖模块）
mvn -pl ms-file -am package -DskipTests    # 构建文件服务（MinIO）
mvn install -DskipTests                     # 构建全部模块
java -jar ms-user/target/ms-user-1.0.0.jar  # 启动服务
```

## 基础设施（全部已配置开机自启，无需手动启动）

| 组件 | 地址 | 自启方式 |
|---|---|---|
| MySQL80 | localhost:3306，root/969798 | Windows 服务（唯一，Auto） |
| Redis | localhost:6379，密码 969798，database=1 | Windows 服务（Auto） |
| Nacos | localhost:8848（控制台 /nacos，nacos/nacos） | 计划任务 `ms-demo Nacos`（standalone） |
| MinIO | localhost:9000/9001（minioadmin/minioadmin） | 计划任务 `ms-demo MinIO` |

- 数据库：骨架开发用 `ms_demo`（java-app 参考数据在 `java_app_dev`）
- 自查：`Get-Service`（服务）、`Get-NetTCPConnection -LocalPort <port>`（端口）
- **Nacos 未起或未就绪时，任何服务启动即失败**（报 `Client not connected, current status:STARTING`）——因为 Nacos 客户端走 gRPC 端口 `主端口+1000 = 9848`

## 模块结构

```
ms-demo/
├── pom.xml         父工程：packaging=pom，三个 BOM 统一版本
├── ms-common/      公共模块：ApiResponse / PageResponse / BaseEntity / ErrorCode / BusinessException / GlobalExceptionHandler / RedisConfig / RedisUtil
├── ms-user/        用户服务 8081（骨架：仅启动类 + MybatisPlusConfig，业务待重建）
├── ms-order/       订单服务 8082（骨架：仅启动类 + MybatisPlusConfig，业务待重建）
├── ms-file/        文件服务 8083（完整：MinIO 上传下载，见 ms-file）
└── ms-gateway/     网关 8080：路由 /api/user/** → lb://ms-user 等
```

包名统一 `com.example.ms.*`（common 在 `com.example.ms.common` / `.exception`，服务在 `com.example.ms.{user,order,file,gateway}`）。

## 核心架构约定

- **版本管理**：父 pom `<dependencyManagement>` 引入三个 BOM（spring-boot / spring-cloud / spring-cloud-alibaba），子模块依赖**不写版本号**；minio SDK 不在 BOM，必须写版本（8.5.17）
- **注册中心**：`spring-cloud-starter-alibaba-nacos-discovery`；服务名即 `spring.application.name`；启动自动注册；**网关本身也是一个服务**
- **网关**：Spring Cloud Gateway（WebFlux 响应式），**不能引 spring-boot-starter-web**（启动冲突）；路由用 `lb://服务名`（需 spring-cloud-starter-loadbalancer）
- **服务间调用**：Feign —— `@FeignClient(name="...")` + 启动类 `@EnableFeignClients`；**跨服务只传 DTO**
- **公共模块扫描**：服务启动类必须 `@SpringBootApplication(scanBasePackages = "com.example.ms")`，否则扫不到 ms-common 的 GlobalExceptionHandler / RedisConfig
- **分层**：Controller → Service → Mapper（MyBatis Plus BaseMapper）；实体继承 BaseEntity（id/createdAt/updatedAt 自动填充），MybatisPlusConfig 提供分页插件 + MetaObjectHandler
- **响应/异常**：`ApiResponse<T>(code, message, data, timestamp)`；分页 `PageResponse` + `Page`；`BusinessException(ErrorCode)` + GlobalExceptionHandler
- **Redis 基础设施**：ms-common 提供 RedisConfig（key string / value JSON 带类型，支持 LocalDateTime）+ RedisUtil；服务加 `spring.data.redis.*` 即自动生效
- **数据库迁移（Flyway）**：SQL 写 `resources/db/migration/V<版本>__<描述>.sql`；**共用库必须各服务独立 `spring.flyway.table`**（`flyway_user_history` / `flyway_order_history`…），否则 checksum 冲突；`baseline-on-migrate: true`、`clean-disabled: true`；**已执行迁移文件不能改内容**（校验 checksum），只能新增 V+1
- **金额**：以「分」存 Integer
- **软删除**：`deleted_at DATETIME`，Java 层 `@TableLogic`（删除=now()，未删=null）

## 关键接口现状

| 模块 | 端口 | 端点 | 状态 |
|---|---|---|---|
| ms-user | 8081 | — | 骨架，无业务接口 |
| ms-order | 8082 | — | 骨架，无业务接口 |
| ms-file | 8083 | POST `/api/file/upload` · GET `/api/file/download/{*key}` | 已实现（MinIO） |
| ms-gateway | 8080 | 路由 `/api/{user,order,file}/**` | 已配 |

## 目标功能清单（java-app 功能 → 规划微服务，功能参考）

> 功能与接口参考 java-app，从 0 到 1 逐个重建；**数据库表结构自行设计**。当前只有骨架。

**横切约定**：角色 USER / MERCHANT / ADMIN / LANDLORD；JWT(HMAC，7 天)；订单/合同/预约状态机；软删除；金额以「分」存 Integer；Redis 缓存；限流。

| 现有功能（java-app） | 规划微服务 | 关键接口 |
|---|---|---|
| 登录/注册/验证码/登出 | auth-service | /auth/login · register · logout · captcha |
| 用户信息/密码/头像/列表(ADMIN) | user-service | /user/info · password · avatar · list |
| 商家入驻/审核 | merchant-service | /merchant/apply · review |
| 店铺 CRUD | shop-service | /shops |
| 商品/分类 | product-service | /products · /categories/tree |
| 购物车(Redis) | cart-service | /carts · /carts/items · /carts/clear |
| 订单状态机 | order-service | /orders + cancel/ship/complete/refund |
| 模拟支付 | payment-service | /payment/pay |
| 拆单结算 | settlement-service | /settlement |
| 优惠券 | coupon-service | /coupons · claim · my |
| 评价/回复 | review-service | /reviews · reply |
| 房源 CRUD/筛选 | house-service | /houses |
| 收藏 | house-service（或独立） | /favorites |
| 房东入驻/审核 | landlord-service | /landlord/apply · review |
| 预约看房 | viewing-service | /viewings + confirm/reject/cancel/complete |
| 合同/租金/退租 | contract-service | /contracts · /rent-payments · /checkouts |
| 文件上传 | file-service（公共） | /file/upload |
| 金价 / OpenDota 代理 | tool-service（第三方网关） | /gold · /dota |
| 聊天(WebSocket/DeepSeek) | chat-service | WS /ws/events · /chat |

## 开发计划

### 当前：业务重建（骨架 → 真实服务）
按"目标功能清单"从 java-app 逐个重建（功能参考），**一次一个服务，用户手写业务代码，数据库表自行设计**。
首个目标：**认证 ms-auth**（注册/登录/JWT），路由 `/api/auth/**`。

### 后续
- 服务治理：Sentinel 限流熔断、Feign 超时重试、网关统一 JWT 鉴权
- 部署：Docker Compose 一键起全部 + 监控（Actuator / Spring Boot Admin）

## 踩坑记录

- **Windows jar 锁**：正在运行的 jar 无法被覆盖，重新打包前先停对应进程
- **代理假响应**：本机 Clash 127.0.0.1:7890（环境变量 HTTP_PROXY），curl 测 localhost 必须加 `--noproxy "*"`
- **repackage 未绑定**：自定义父工程没有 starter-parent，spring-boot-maven-plugin 必须在 pluginManagement 显式配 `<execution><goal>repackage</goal>`，否则 jar 无 Main-Class
- **扫不到异常处理器**：启动类不配 scanBasePackages=com.example.ms，GlobalExceptionHandler 不生效
- **Redis 序列化崩**：value 用 GenericJackson2JsonRedisSerializer 必须配 JavaTimeModule + activateDefaultTyping，否则 LocalDateTime 反序列化崩溃、对象丢类型（见 ms-common RedisConfig）
- **minio 版本要写死**：minio SDK 不在任何 BOM，pom 里必须写版本号（当前 8.5.17）
- **MinIO 未起则 ms-file 起不来**：MinioConfig.ensureBucket 启动建桶，MinIO 必须先启动
- **Nacos 未起则服务启动失败**：报 `Client not connected, current status:STARTING`（gRPC 端口 9848 不通）——先确认 Nacos 已就绪
- **Flyway 共用库冲突**：多服务共用一个库时，必须各配独立 `spring.flyway.table`；已执行的迁移文件**不能改内容**（checksum 校验），只能新增 V+1 文件
- **Flyway 共用库基线竞态**：共用库 + 独立 history 表，清空库后若多服务**同时**启动，后启动的服务会因库里已有前者的 history 表而触发 `baseline-on-migrate`，打上版本 1 基线，导致它的 `V1__*.sql` 被静默忽略（版本 ≤ 基线）。恢复：`DELETE FROM <该服务的 history 表>`（表已存在则重启不会重复基线）
- **MP 3.5.9+ 分页插件拆包**：`PaginationInnerInterceptor` 拆到独立依赖 `mybatis-plus-jsqlparser`，只引 starter 会 `Cannot resolve symbol` 或分页 total=0 静默失效；必须按主版本引 `mybatis-plus-jsqlparser`（ms-user / ms-order 已加，配 `<version>${mybatis-plus.version}</version>`）
- **重复注册 Windows 服务**：MySQL 曾有两个同配置服务（MySQL / MySQL80）双 Auto 抢 3306，输家一直卡 StartPending；已清理为单一 MySQL80——不要再注册重复服务

## 教学协作约定

用户正在**重新学习微服务**：先讲为什么 · 一次一个模块 · 说清文件路径 · 给思路不给代码 · **用户自己写业务代码，我只搭框架/给思路** · 每步检查指出行号问题 · 每完成一个接口展示预期 JSON
