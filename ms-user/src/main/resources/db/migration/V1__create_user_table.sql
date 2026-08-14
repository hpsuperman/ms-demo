CREATE TABLE t_user
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone         VARCHAR(11)  NOT NULL UNIQUE COMMENT '手机号',
    password_hash VARCHAR(100) NOT NULL COMMENT '哈希密码',
    nickname      VARCHAR(50) COMMENT '名称',
    avatar        VARCHAR(255) COMMENT '头像',
    gender        VARCHAR(10) COMMENT '性别',
    birthday      DATE COMMENT '生日',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    roles         VARCHAR(100) NOT NULL DEFAULT 'USER' COMMENT '角色',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at    DATETIME COMMENT '软删除时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;