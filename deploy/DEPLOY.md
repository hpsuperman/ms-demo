# ms-demo Linux 服务器部署手册

> 目标：一台全新 Ubuntu 22.04 服务器，从零把 4 个微服务跑起来。
> 前提：**必须先做第 0 步配置外置化**，否则 yml 里的写死地址/密码无法被环境变量覆盖。

---

## 0. 配置外置化（已完成，本机已改好）

每个服务三个 yml：`application.yml`（默认 `active: dev`）+ `application-dev.yml` + `application-prod.yml`。
**密钥一律 `${环境变量}` 占位、无默认值**，本地靠用户级环境变量注入，服务器由 systemd 的 `Environment` 注入。
生产环境用 `SPRING_PROFILES_ACTIVE=prod` 覆盖。

各服务外置化字段：

| 服务         | 外置化字段                                                               |
|------------|---------------------------------------------------------------------|
| ms-user    | NACOS_ADDR、REDIS_HOST/REDIS_PASSWORD、DB_URL/DB_USERNAME/DB_PASSWORD、**JWT_SECRET** |
| ms-order   | NACOS_ADDR、DB_URL/DB_USERNAME/DB_PASSWORD                           |
| ms-file    | NACOS_ADDR、MINIO_ENDPOINT/MINIO_ACCESS_KEY/MINIO_SECRET_KEY         |
| ms-gateway | NACOS_ADDR                                                          |

环境变量名与 `deploy/*.service` 模板里 `Environment` 一一对应，部署时无需再改代码。
**注意**：所有密钥均无 yml 默认值，漏配任一个变量服务即启动失败。

---

## 1. 服务器准备

```bash
sudo apt update && sudo apt upgrade -y
sudo useradd -m -s /bin/bash app          # systemd 模板里 User=app 依赖这个用户
# 放行端口（ufw 或云安全组）：80(nginx)、3080(网关)、8848(nacos 控制台)
```

## 2. 安装 JDK 17

```bash
sudo apt install -y openjdk-17-jdk
java -version    # 确认 17.x
```

## 3. 安装 MySQL 8

```bash
sudo apt install -y mysql-server
sudo systemctl enable --now mysql
sudo mysql_secure_installation            # 交互设置 root 密码
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS ms_demo DEFAULT CHARSET utf8mb4;"
# 表不用建，Flyway 启动时自动建
```

## 4. 安装 Nacos（必须先于所有服务启动）

```bash
wget https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.tar.gz
sudo mkdir -p /opt/nacos && sudo tar -xzf nacos-server-2.3.2.tar.gz -C /opt/nacos --strip-components=1
sudo chown -R app:app /opt/nacos
```

创建 `/etc/systemd/system/nacos.service`：

```ini
[Unit]
Description=Nacos Server
After=network.target

[Service]
Type=forking
User=app
ExecStart=/opt/nacos/bin/startup.sh -m standalone
ExecStop=/opt/nacos/bin/shutdown.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload && sudo systemctl enable --now nacos
# 等 20 秒后验证
curl http://127.0.0.1:8848/nacos   # 返回 200 即就绪
```

## 5. 安装 Redis

```bash
sudo apt install -y redis-server
# 设密码（要和 yml 默认值/环境变量一致）
sudo sed -i 's/^# requirepass.*/requirepass 969798/' /etc/redis/redis.conf
sudo systemctl enable --now redis-server
redis-cli -a 969798 ping    # PONG 即就绪
```

## 6. 安装 MinIO

```bash
wget https://dl.min.io/server/minio/release/linux-amd64/minio
sudo install -m 0755 minio /usr/local/bin/minio
sudo mkdir -p /data/minio
```

创建 `/etc/systemd/system/minio.service`：

```ini
[Unit]
Description=MinIO
After=network.target

[Service]
Type=simple
User=app
Environment="MINIO_ROOT_USER=minioadmin"
Environment="MINIO_ROOT_PASSWORD=minioadmin"
ExecStart=/usr/local/bin/minio server /data/minio --address ":9000" --console-address ":9001"
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload && sudo systemctl enable --now minio
```

## 7. 部署 jar + systemd

```bash
# 本机打包后上传（在 ms-demo 目录执行）
scp ms-user/target/ms-user-1.0.0.jar   root@服务器:/tmp/
scp ms-order/target/ms-order-1.0.0.jar root@服务器:/tmp/
scp ms-file/target/ms-file-1.0.0.jar   root@服务器:/tmp/
scp ms-gateway/target/ms-gateway-1.0.0.jar root@服务器:/tmp/
```

```bash
# 服务器上
sudo mkdir -p /opt/ms-user /opt/ms-order /opt/ms-file /opt/ms-gateway
sudo mv /tmp/ms-*.jar /opt/ms-*/app.jar    # 每个目录一个，统一叫 app.jar
sudo chown -R app:app /opt/ms-*
sudo cp deploy/ms-user.service deploy/ms-order.service deploy/ms-file.service /etc/systemd/system/
sudo cp deploy/ms-gateway.service /etc/systemd/system/
# 记得把模板里 3 个"改这里填"的密码改成实际值
sudo systemctl daemon-reload
```

## 8. nginx

```bash
sudo apt install -y nginx
sudo systemctl enable --now nginx
```

`/etc/nginx/sites-available/ms-demo`：

```nginx
server {
    listen 80;
    server_name 你的域名或IP;

    location / {
        proxy_pass http://127.0.0.1:3080;   # 全部交给网关
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/ms-demo /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

## 9. 启动顺序 + 验证

```bash
# 顺序：Nacos(已自启) → mysql/redis/minio(已自启) → 4 个应用服务
sudo systemctl enable --now ms-user ms-order ms-file
sudo systemctl enable --now ms-gateway        # 网关最后，等其它服务注册完
```

```bash
# 验证（服务器本机）
curl http://127.0.0.1:3081/user/1              # ms-user 直连
curl -X POST "http://127.0.0.1:3082/order/create?userId=1&amount=100"  # Feign 跨服务
curl http://127.0.0.1:3080/user/1              # 网关路由
# 对外
curl http://你的域名/user/1                      # nginx → 网关 → ms-user
```

**坑提醒**（CLAUDE.md 踩坑记录在服务器上同样适用）：

- Nacos 没就绪，任何服务启动即失败——先确认 8848 通
- MinIO 没起，ms-file 起不来（ensureBucket 启动建桶）
- Flyway 共用库 ms_demo，各服务独立 history 表，别删改已执行的迁移文件
- 服务日志：`journalctl -u ms-user -f` 查看
