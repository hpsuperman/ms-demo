CREATE TABLE t_announcement
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(100) NOT NULL COMMENT '标题',
    content        TEXT         NOT NULL COMMENT '内容',
    publisher_id   BIGINT       NOT NULL COMMENT '发布人id',
    publisher_name VARCHAR(100) NOT NULL COMMENT '发布人名称',
    status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    published_at   DATETIME COMMENT '发布时间',
    pinned         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否置顶',
    pinned_at      DATETIME COMMENT '置顶时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at     DATETIME COMMENT '软删除时间',
    KEY idx_publisher_id (publisher_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;