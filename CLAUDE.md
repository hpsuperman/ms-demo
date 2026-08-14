# CLAUDE.md

## 项目概述

**ms-demo**：Maven 多模块分布式微服务学习项目（Spring Cloud Alibaba）。无固定业务主题，用于从 0 到 1 练习微服务落地。

- **当前状态**：骨架已跑通，基础能力齐备——**Nacos 配置中心 / 网关统一 JWT 鉴权 / traceId 全链路日志 / Actuator 健康检查**均已落地。ms-user 已有注册/登录/验证码 + JWT 业务；ms-file 已有 MinIO 上传/下载（完整）
- **当前目标**：逐个服务重建业务，一次一个服务，**数据库表结构自行设计**
- 技术栈：JDK 17 / Maven / Spring Boot 3.2.4 / Spring Cloud 2023.0.1 / Spring Cloud Alibaba 2023.0.1.0 / Nacos 2.3.2 / MyBatis Plus 3.5.12 / MySQL 8.0 / Redis / MinIO / Lombok / MapStruct

## 模块结构

```
ms-demo/
├── pom.xml         父工程：packaging=pom，三个 BOM 统一版本
├── ms-common/      公共模块（端口无关）：ApiResponse / PageResponse / BaseEntity / 异常体系 / Redis / TraceIdFilter
├── ms-user/        用户服务 3081：注册/登录/验证码 + JWT（已有业务）
├── ms-file/        文件服务 3083：MinIO 上传/下载（完整）
└── ms-gateway/     网关 3080：路由 → lb://服务名 + 统一 JWT 鉴权 + traceId
```

包名统一 `com.example.ms.*`。

## 端口一览

| 模块 | 端口 |
|---|---|
| ms-gateway | 3080 |
| ms-user | 3081 |
| ms-file | 3083 |

## 基础设施（本机已开机自启）

| 组件 | 地址 | 自启方式 |
|---|---|---|
| MySQL80 | localhost:3306，root/969798 | Windows 服务（Auto） |
| Redis | localhost:6379，密码 969798，database=1 | Windows 服务（Auto） |
| Nacos | localhost:8848（控制台 /nacos，nacos/nacos） | 计划任务 `ms-demo Nacos` |
| MinIO | localhost:9000/9001（minioadmin/minioadmin） | 计划任务 `ms-demo MinIO` |

- 数据库：开发用 `ms_demo`
- **Nacos 未就绪时任何服务启动即失败**（报 `Client not connected, current status:STARTING`，gRPC 走 `主端口+1000 = 9848`）

## 环境变量（密钥注入，绝不写死）

本地/服务器通过**用户级环境变量**注入，yml 只留 `${变量}` 占位、无默认值：

- `DB_PASSWORD` / `DB_USERNAME`（默认 root）
- `REDIS_PASSWORD`
- `JWT_SECRET`（≥32 字节）
- `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY`
- `NACOS_ADDR`（默认 127.0.0.1:8848）
- `DB_URL` / `REDIS_HOST` 等均有 `${VAR:默认值}` 兜底

本机已 setx 用户级变量，新开终端生效。

## 配置方案（Nacos Config 配置中心）

环境相关配置（datasource / redis / minio / mybatis-plus / jwt.expiration）**已迁到 Nacos 配置中心**，本地只留跑得起来的最小骨架：

- 本地 `application.yml`：server.port / 服务名 / profiles.active / nacos 地址 / `spring.config.import` / management / 密钥占位——**不含环境配置**
- Nacos dataId（group=DEFAULT_GROUP）：`<服务名>.yaml`（共享）+ `<服务名>-<profile>.yaml`（环境差异）
- **同步源**：`deploy/nacos/*.yaml` 是配置源文件——改它后推送到 Nacos，或控制台改后同步回来
- **密钥仍走环境变量**：Nacos 文档里也是 `${DB_PASSWORD}` 等占位，不写死
- **Nacos 未就绪或缺 dataId 时服务无法启动**（配置中心优先的设计）；`optional:` 前缀只保证"缺 dataId 不报错"，不是"没配置也能跑"
- 推送到 Nacos：
  ```bash
  curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
    --data-urlencode "dataId=ms-user-dev.yaml" \
    --data-urlencode "group=DEFAULT_GROUP" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@deploy/nacos/ms-user-dev.yaml"
  ```
- `application-prod.yml` 仍保留生产占位兜底（prod profile 时才生效），后续可整体迁到 Nacos `-prod` dataId

## 核心架构约定

- **版本管理**：父 pom `<dependencyManagement>` 引三个 BOM（boot/cloud/cloud-alibaba），子模块依赖不写版本；**minio SDK 不在 BOM，必须写版本（8.5.17）**；MapStruct / springdoc / jjwt 也不在 BOM，写死版本
- **注册中心**：`spring-cloud-starter-alibaba-nacos-discovery`；服务名即 `spring.application.name`；启动自动注册；**网关本身也是服务**
- **配置中心（Nacos Config）**：`spring-cloud-starter-alibaba-nacos-config`；**必须**配 `spring.config.import: optional:nacos:<服务名>.yaml, optional:nacos:<服务名>-<profile>.yaml`，否则启动报 `No spring.config.import property has been defined`；本地与 Nacos **不要重复配置同一键**（避免优先级歧义）
- **网关统一 JWT 鉴权**：`JwtAuthGlobalFilter` 校验 `Authorization: Bearer <token>`（与 ms-user 共用 `jwt.secret`）；白名单 `gateway.auth.whitelist` 免鉴权（登录/注册/验证码/健康检查）；OPTIONS 预检放行；通过后透传 `X-User-Id / X-User-Phone / X-User-Role` 请求头给下游，业务代码直接取这些头当用户身份
- **traceId 全链路日志**：网关 `TraceIdWebFilter` 生成 traceId → 经 `X-Trace-Id` 头透传下游；ms-common `TraceIdFilter`（servlet 服务）读该头写 MDC；logback pattern 含 `[%X{traceId:-}]`；按 traceId 串起整条链路日志
- **Actuator 健康检查**：各服务 `spring-boot-starter-actuator`，暴露 `health,info`；`/actuator/health` 已在网关白名单放行（监控探活不带 token）
- **网关**：Spring Cloud Gateway（WebFlux），**不能引 spring-boot-starter-web**（冲突）；路由 `lb://服务名`（需 loadbalancer）
- **服务间调用**：Feign —— `@FeignClient(name=...)` + `@EnableFeignClients`；**跨服务只传 DTO**
- **公共模块扫描**：启动类必须 `@SpringBootApplication(scanBasePackages = "com.example.ms")`，否则扫不到 GlobalExceptionHandler / RedisConfig
- **分层**：Controller → Service → Mapper（MyBatis Plus BaseMapper）；实体继承 BaseEntity（id/createdAt/updatedAt 自动填充）；MybatisPlusConfig 提供分页插件 + MetaObjectHandler
- **响应/异常**：`ApiResponse<T>(code, message, data, timestamp)`；分页 `PageResponse` + `Page`；`BusinessException(ErrorCode)` + GlobalExceptionHandler
- **Redis**：ms-common 提供 RedisConfig（key string / value JSON 带类型，支持 LocalDateTime）+ RedisUtil；服务加 `spring.data.redis.*` 即生效
- **数据库迁移（Flyway）**：SQL 写 `resources/db/migration/V<版本>__<描述>.sql`；**共用库必须各服务独立 `spring.flyway.table`**（flyway_user_history / flyway_file_history…）；`baseline-on-migrate: true`、`clean-disabled: true`；**已执行迁移文件不能改**（checksum 校验），只能新增 V+1
- **金额**：以「分」存 Integer
- **软删除**：`deleted_at DATETIME`，Java 层 `@TableLogic(value="null", delval="now()")`

## ms-user 业务现状

- **接口**：GET `/user/captcha` · POST `/user/register` · POST `/user/login`
- **流程**：注册（BCrypt 加密 + 手机号唯一校验）→ 登录（验证码校验 + 密码匹配 + 状态校验 → 签发 JWT）
- **关键类**：UserController / UserService / CaptchaService / UserConverter(MapStruct) / JwtUtil / UserMapper
- **实体**：User（继承 BaseEntity），status 用枚举 UserStatus，roles 用字符串（UserRole 枚举）；`@TableName("t_user")`
- **JWT**：jjwt 0.12.5，`Keys.hmacShaKeyFor` 要求 secret ≥ 32 字节；claims 含 userId/phone/role
- **表结构**：见 `ms-user/src/main/resources/db/migration/V1__create_user_table.sql`
- ⚠️ 注意：JwtUtil **目录是 `util/` 但包声明 `utils`**（带 s）——能编译但目录/包名不一致，建议统一为 `utils`

## 常用命令

```bash
mvn -pl ms-common install -DskipTests      # 编译安装公共模块
mvn -pl ms-user -am package -DskipTests    # 构建单个服务（-am 连带依赖模块）
mvn install -DskipTests                     # 构建全部模块
java -jar ms-user/target/ms-user-1.0.0.jar  # 启动服务
```

- Windows jar 锁：正在运行的 jar 无法覆盖，重新打包前先停进程
- 本机 Clash 127.0.0.1:7890，curl 测 localhost 必须加 `--noproxy "*"`

## 踩坑记录

- **repackage 未绑定**：自定义父工程没有 starter-parent，spring-boot-maven-plugin 必须在 pluginManagement 显式配 `<execution><goal>repackage</goal>`，否则 jar 无 Main-Class
- **扫不到异常处理器**：启动类不配 scanBasePackages=com.example.ms，GlobalExceptionHandler 不生效
- **Redis 序列化崩**：value 用 GenericJackson2JsonRedisSerializer 必须配 JavaTimeModule + activateDefaultTyping，否则 LocalDateTime 反序列化崩溃、对象丢类型
- **minio 版本要写死**：minio SDK 不在任何 BOM，pom 必须写版本（8.5.17）
- **MinIO 未起则 ms-file 起不来**：BucketInitializer 启动建桶
- **Nacos 未起则服务启动失败**：报 `Client not connected, current status:STARTING`（gRPC 9848 不通）
- **Flyway 共用库冲突**：共用库必须各配独立 history 表；已执行迁移文件不能改内容（checksum），只能新增 V+1
- **Flyway 基线竞态**：共用库 + 独立 history 表，清空库后多服务同时启动，后启动者可能被 `baseline-on-migrate` 打上版本 1 基线，导致它的 V1 被静默忽略。恢复：`DELETE FROM <该服务 history 表>`
- **MP 3.5.9+ 分页插件拆包**：`PaginationInnerInterceptor` 在独立依赖 `mybatis-plus-jsqlparser`，只引 starter 会分页 total=0 静默失效；需按主版本引

## 教学协作约定

- **先讲为什么**：每个概念先讲清它解决什么问题，再讲怎么做
- **一次只讲一个概念**：讲透一个知识点再继续，不连讲一堆
- **给思路不给代码**：说清思路和关键文件位置，代码由你自己写
- **写完我 Review**：带行号指出问题，通过后再进入下一步
- **回答简洁**：直奔重点，不啰嗦
