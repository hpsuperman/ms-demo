# ms-template 服务骨架模板

新建服务时复制本目录，改掉占位符即可。**不用把这个目录加进父 pom 的 `<modules>`**（模板本身不参与构建）。

## 复制步骤（以新建 ms-xxx 为例）

1. 复制整个 `ms-template/` 为 `ms-xxx/`
2. 全局替换占位符：

| 占位符 | 示例值 | 出现位置 |
|---|---|---|
| `{{module}}` | `xxx` | Java 包名（目录 + package 声明） |
| `{{MainClass}}` | `MsXxx` | 启动类名 `MsXxxApplication.java` |
| `{{artifactId}}` | `ms-xxx` | pom.xml |
| `{{description}}` | 一句话描述业务 | pom.xml |
| `{{port}}` | `3086` | application.yml |
| `{{serviceName}}` | `ms-xxx` | application.yml `spring.application.name` |
| `{{flywayTable}}` | `flyway_xxx_history` | application.yml + 根目录 deploy/nacos 两个文件 |

3. 写业务代码（Mapper / Service / Controller / DTO / Converter / Entity / 枚举）
4. 自建表：在 `src/main/resources/db/migration/` 写 `V1__xxx.sql`
5. **Nacos 配置源**：在**根目录 `deploy/nacos/`** 建两份（所有服务统一放这里，不在服务内部）：
   - `{{serviceName}}.yaml`（共享：mybatis-plus 配置）
   - `{{serviceName}}-dev.yaml`（环境：datasource / redis / flyway table）

## 外部接线（新服务必须做，别漏）

1. **父 pom**：`pom.xml` 的 `<modules>` 加 `<module>ms-xxx</module>`
2. **网关路由**：`ms-gateway/src/main/resources/application.yml` 的 `routes` 加
   ```yaml
   - id: ms-xxx
     uri: lb://ms-xxx
     predicates:
       - Path=/xxx/**
   ```
   （网关鉴权白名单：业务接口全要走 JWT，一般不加白名单）
3. **推送 Nacos 配置**（源文件在根目录 `deploy/nacos/`，改完推）：
   ```bash
   curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
     --data-urlencode "dataId=ms-xxx.yaml" \
     --data-urlencode "group=DEFAULT_GROUP" \
     --data-urlencode "type=yaml" \
     --data-urlencode "content@deploy/nacos/ms-xxx.yaml"
   ```
   `ms-xxx-dev.yaml` 同理。**Nacos 未就绪或缺 dataId 时服务启动即失败。**

## 构建 / 启动

```bash
mvn -pl ms-common install -DskipTests
mvn -pl ms-xxx -am package -DskipTests
java -jar ms-xxx/target/ms-xxx-1.0.0.jar
```

- Windows jar 锁：正在运行的 jar 无法覆盖，重新打包前先停进程
- 本机 Clash 127.0.0.1:7890，curl 测 localhost 必须加 `--noproxy "*"`

## 已带能力的说明（ms-common 自动生效，不用自己建）

- 启动类 `scanBasePackages = "com.example.ms"`：扫到 GlobalExceptionHandler / RedisConfig / JacksonConfig / MybatisPlusConfig
- 分页插件 + MetaObjectHandler（created_at/updated_at 自动填充）+ @MapperScan
- RedisUtil / UserContext（取网关透传的 X-User-Id 等头）
- ApiResponse / PageResponse / BusinessException / ErrorCode
- LocalDateTime 统一序列化为 `yyyy-MM-dd HH:mm:ss`