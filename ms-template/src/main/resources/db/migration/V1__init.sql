-- 服务首个迁移文件示例。数据库表结构自行设计。
-- 已执行迁移文件不能改（Flyway checksum 校验），只能新增 V+1。

CREATE TABLE t_example
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL COMMENT '名称',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME COMMENT '软删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;