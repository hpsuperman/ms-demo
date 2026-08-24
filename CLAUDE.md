# CLAUDE.md

## 项目概述

**ms-demo**：Maven 多模块分布式微服务学习项目（Spring Cloud Alibaba）。无固定业务主题，用于从 0 到 1 练习微服务落地。

- **当前状态**：骨架已跑通，基础能力齐备——**Nacos 配置中心 / 网关统一 JWT 鉴权 / traceId 全链路日志 / Actuator 健康检查**均已落地。ms-user 已有注册/登录/验证码 + JWT + 部门/角色/公告业务；ms-file 已有 MinIO 上传/下载（完整）；ms-approval 已有请假申请 + 两级审批（主管→HR）+ 已办查询
- **当前目标**：逐个服务重建业务，一次一个服务，**数据库表结构自行设计**
- 技术栈：JDK 17 / Maven / Spring Boot 3.2.4 / Spring Cloud 2023.0.1 / Spring Cloud Alibaba 2023.0.1.0 / Nacos 2.3.2 / MyBatis Plus 3.5.12 / MySQL 8.0 / Redis / MinIO / Lombok / MapStruct

## 模块结构

```
ms-demo/
├── pom.xml         父工程：packaging=pom，三个 BOM 统一版本
├── ms-common/      公共模块（端口无关）：ApiResponse / PageResponse / BaseEntity / 异常体系 / Redis / TraceIdFilter
├── ms-user/        用户服务 3081：用户/部门/角色/公告 + JWT（已有业务）
├── ms-approval/    审批服务 3082：请假申请 + 主管→HR 两级审批
├── ms-file/        文件服务 3083：MinIO 上传/下载（完整）
└── ms-gateway/     网关 3080：路由 → lb://服务名 + 统一 JWT 鉴权 + traceId
```

包名统一 `com.example.ms.*`。

## 端口一览

| 模块 | 端口 |
|---|---|
| ms-gateway | 3080 |
| ms-user | 3081 |
| ms-approval | 3082 |
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
- **Jackson 序列化**：ms-common `JacksonConfig` 统一 LocalDateTime 为 `yyyy-MM-dd HH:mm:ss`、LocalDate 为 `yyyy-MM-dd`
- **Redis**：ms-common 提供 RedisConfig（key string / value JSON 带类型，支持 LocalDateTime）+ RedisUtil；服务加 `spring.data.redis.*` 即生效
- **数据库迁移（Flyway）**：SQL 写 `resources/db/migration/V<版本>__<描述>.sql`；**共用库必须各服务独立 `spring.flyway.table`**（flyway_user_history / flyway_file_history…）；`baseline-on-migrate: true`、`clean-disabled: true`；**已执行迁移文件不能改**（checksum 校验），只能新增 V+1
- **金额**：以「分」存 Integer
- **软删除**：`deleted_at DATETIME`，Java 层 `@TableLogic(value="null", delval="now()")`

## ms-user 业务现状

### 用户模块
- **接口**：GET `/user/captcha` · POST `/user/register` · POST `/user/login` · GET `/user/page` · POST `/user` · GET `/user/{id}` · PUT `/user/{id}` · DELETE `/user/{id}` · GET `/user/me`
- **流程**：注册（BCrypt 加密 + 手机号唯一校验 + 默认 USER 角色）→ 登录（验证码校验 + 密码匹配 + 状态校验 → 签发 JWT）
- **验证码**：`CaptchaService` 校验，开发期通用验证码 `1111` 直接放行（`verifyCaptcha` 先短路）
- **关键类**：UserController / UserService / CaptchaService / UserConverter(MapStruct) / JwtUtil / UserMapper
- **实体**：User（继承 BaseEntity），status 用枚举 UserStatus；`@TableName("t_user")`；含 `departmentId` 字段关联部门
- **权限**：`@RequireRole({"ADMIN"})` 声明式校验（RoleAspect 切面）；用户角色已迁到关联表 t_user_role，User.roles 字段不再使用
- **JWT**：jjwt 0.12.5，`Keys.hmacShaKeyFor` 要求 secret ≥ 32 字节；claims 含 userId/phone/**role（逗号分隔多角色）**；登录时从 t_user_role 关联 t_role 查角色
- **表结构**：见 `V1__create_user_table.sql`，`V2__add_employee_fields.sql`（员工扩展字段）

### 部门模块
- **接口**：GET `/user/department`（树形列表）· GET `/user/department/detail/{id}` · POST `/user/department` · PUT `/user/department/{id}` · DELETE `/user/department/{id}`
- **功能**：树形结构（parentId=0 为根节点）/ 同级名称查重 / 防成环 / 有子部门或员工禁止删除 / 软删除
- **关键类**：DepartmentController / DepartmentService / DepartmentConverter(MapStruct) / DepartmentMapper
- **实体**：Department（继承 BaseEntity），status 用枚举 DepartmentStatus；`@TableName("t_department")`
- **表结构**：见 `V3__create_department_table.sql`（同时给 t_user 加了 department_id 字段）

### 角色模块（RBAC 简化版）
- **接口**：GET `/user/role` · GET `/user/role/detail/{id}` · POST `/user/role` · PUT `/user/role/{id}` · DELETE `/user/role/{id}`
- **功能**：角色增删改查；`name` 唯一（数据库唯一约束 + Service 层查重）；有用户绑定的角色禁止删除
- **关键类**：RoleController / RoleService / RoleConverter(MapStruct) / RoleMapper / UserRoleMapper
- **实体**：Role（继承 BaseEntity）；UserRole（纯关联表，**不继承 BaseEntity**，user_id+role_id 联合主键）
- **多角色**：一用户多角色走 t_user_role；JWT role claim 存逗号分隔字符串；RoleAspect 拆分后任一匹配即通过；网关 X-User-Role 原样透传
- **表结构**：见 `V5__create_role_table.sql`（t_role + 初始三角色），`V6__create_user_role_table.sql`（t_user_role）

### 公告模块
- **接口**：POST `/user/announcement` · GET `/user/announcement/page` · GET `/user/announcement/{id}` · PUT `/user/announcement/{id}` · DELETE `/user/announcement/{id}`
- **功能**：发布（自动取当前用户作发布人，冗余 publisherName）、标题关键字分页（置顶优先，置顶时间倒序 → 发布时间倒序）、软删除；仅 ADMIN 可写，详情/分页所有人可见
- **关键类**：AnnouncementController / AnnouncementService / AnnouncementConverter(MapStruct) / AnnouncementMapper
- **实体**：Announcement（继承 BaseEntity），status 用枚举 AnnouncementStatus（DRAFT/PUBLISHED）；`@TableName("t_announcement")`；`pinned` 置顶标记 + `pinnedAt` 置顶时间
- **表结构**：见 `V7__create_announcement_table.sql`（t_announcement）

## ms-approval 业务现状

### 请假模块（两级审批：主管 → HR）
- **接口**：POST `/leave` · POST `/leave/{id}/review` · GET `/leave/page` · GET `/leave/detail/{id}` · PUT `/leave/cancel/{id}` · GET `/leave/todo` · GET `/leave/done`（已办分页，可按 action 过滤）
- **流程**：申请（Feign 查部门，冗余 applicantLeaderId → PENDING/主管节点）→ 主管通过（APPROVING/HR 节点）→ HR 通过（APPROVED）；任一级拒绝 → REJECTED；流程中本人可撤销 → CANCELED；节点置 null 表示流程结束
- **关键类**：LeaveController / LeaveService / LeaveApprovalConverter(MapStruct) / LeaveMapper / ApprovalRecordMapper；Feign：UserClient / DepartmentClient（依赖 ms-user 的 `GET /user/role/{roleName}` 查 HR 名单）
- **实体**：Leave（继承 BaseEntity）/ ApprovalRecord（无 updated_at/deleted_at，不继承 BaseEntity，手动填充 id+createdAt）；状态/节点/类型/动作全用枚举
- **待办查询**：todoPage 一条嵌套 OR SQL——`(SUPERVISOR节点 且 applicant_leader_id=我) OR (HR节点，仅当我有HR角色)`；HR 判断取 `UserContext.getRole()` 拆逗号，不发 Feign
- **已办查询**：donePage 按 `approver_id=我` 查审批记录，批量查 Leave（`selectByIds` 判空）再 Feign 批量补申请人名，输出 `DoneItemResponse`（继承 LeaveItemResponse 追加审批节点/动作/意见/时间）
- **详情权限**：本人或该单任一审批人可看详情
- **表结构**：见 `V1__create_leave_tables.sql`（t_leave + t_approval_record），`V2__add_applicant_leader_id.sql`；Flyway history 表 flyway_approval_history

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
- **selectByIds 传空集合**：MP 批量查询不判空会生成 `WHERE id IN ()` 直接语法错误，批量查询前必须 `isEmpty()` 提前返回
- **MP wrapper 嵌套 OR**：`and(w -> ...)` 生成括号隔离 OR（否则泄漏绕过软删除条件）；`.eq(布尔, 字段, 值)` 条件式拼接，false 时悬空 OR 自动丢弃

## 教学协作约定

- **回答简洁**：直奔重点，不啰嗦
- **先原型后代码**：每个模块先出手机端原型图（纯中文）+ 字段映射表，确认后再建表写代码
- **一次只讲一个概念**：讲透一个知识点再继续，不连讲一堆
- **给思路不给代码**：说清思路和关键文件位置，代码由你自己写
- **写完我 Review**：带行号指出问题，通过后再进入下一步
