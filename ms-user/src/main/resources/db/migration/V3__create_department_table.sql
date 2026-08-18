CREATE TABLE t_department
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id  BIGINT      NOT NULL DEFAULT 0 COMMENT '上级部门id',
    name       VARCHAR(50) NOT NULL COMMENT '部门名称',
    leader_id  BIGINT COMMENT '负责人id',
    sort       INT         NOT NULL DEFAULT 0 COMMENT '排序',
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
ALTER TABLE t_user
    ADD COLUMN department_id BIGINT NULL COMMENT '所属部门id',
    DROP COLUMN department;
