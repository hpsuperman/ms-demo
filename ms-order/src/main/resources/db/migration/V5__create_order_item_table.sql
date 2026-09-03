ALTER TABLE t_order
    DROP COLUMN product_name,
    DROP COLUMN quantity;


CREATE TABLE t_order_item
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT       NOT NULL COMMENT '订单ID',
    product_id   BIGINT       NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    price        INT          NOT NULL COMMENT '单价(分)',
    quantity     INT          NOT NULL DEFAULT 1 COMMENT '数量',
    amount       INT          NOT NULL COMMENT '小计(分)',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME COMMENT '软删除',
    INDEX idx_order_id (order_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;