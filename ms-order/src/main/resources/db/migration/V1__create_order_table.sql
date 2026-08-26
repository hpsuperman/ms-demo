CREATE TABLE t_order
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no     VARCHAR(32)  NOT NULL COMMENT '订单号',
    user_id      BIGINT       NOT NULL COMMENT '下单用户id',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    quantity     INT          NOT NULL COMMENT '数量',
    amount       INT          NOT NULL COMMENT '金额(分)',
    status       VARCHAR(20)  NOT NULL COMMENT '状态 PENDING/PAID/CANCELED',
    remark       VARCHAR(255) COMMENT '备注',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME     COMMENT '软删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
