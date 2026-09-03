CREATE TABLE t_supplier
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL UNIQUE COMMENT '供应商名字',
    contact_person VARCHAR(50)  NOT NULL COMMENT '联系人',
    contact_phone  VARCHAR(20)  NOT NULL COMMENT '手机号',
    address        VARCHAR(255) COMMENT '地址',
    status         VARCHAR(20)  NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
    remark         VARCHAR(255) COMMENT '备注',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at     DATETIME COMMENT '软删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

