CREATE TABLE t_purchase_order
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no      VARCHAR(32)  NOT NULL UNIQUE COMMENT '单号',
    supplier_id   BIGINT       NOT NULL COMMENT '供应商id  ',
    supplier_name VARCHAR(100) NOT NULL COMMENT '冗余供应商名    ',
    total_amount  INT NOT NULL COMMENT '总金额（分）',
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    remark        VARCHAR(255) COMMENT '备注',
    stocked_at    DATETIME COMMENT '入库时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at    DATETIME COMMENT '软删除',
    INDEX idx_supplier (supplier_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE t_purchase_order_item
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT       NOT NULL COMMENT '采购单id',
    product_id   BIGINT       NOT NULL COMMENT '商品id',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名',
    price        INT NOT NULL COMMENT '单价（分）',
    quantity     INT NOT NULL COMMENT '数量',
    amount       INT NOT NULL COMMENT '小计（分）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME COMMENT '软删除',
    INDEX idx_order_id (order_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
