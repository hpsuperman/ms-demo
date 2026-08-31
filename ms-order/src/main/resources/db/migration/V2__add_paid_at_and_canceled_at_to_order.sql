ALTER TABLE t_order
    ADD COLUMN paid_at     DATETIME COMMENT '支付时间' AFTER created_at,
    ADD COLUMN canceled_at DATETIME COMMENT '取消时间' AFTER created_at
