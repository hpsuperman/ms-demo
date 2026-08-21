CREATE TABLE t_leave
(
    id             Bigint AUTO_INCREMENT Primary Key,
    user_id        BIGINT       NOT NULL COMMENT '申请人id',
    leave_type     VARCHAR(16)  NOT NULL COMMENT '请假类型 SICK/PERSONAL/ANNUAL',
    start_time     DATETIME     NOT NULL COMMENT '开始时间',
    end_time       DATETIME     NOT NULL COMMENT '结束时间',
    duration_hours INT          NOT NULL COMMENT '时长(小时)',
    reason         VARCHAR(255) NOT NULL COMMENT '请假原因',
    status         VARCHAR(16)  NOT NULL COMMENT 'PENDING/APPROVING/APPROVED/REJECTED/CANCELED',
    current_node   VARCHAR(32) COMMENT '当前节点 SUPERVISOR/HR',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at     DATETIME COMMENT '软删除',
    INDEX idx_user (user_id),
    INDEX idx_status_node (status, current_node)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE t_approval_record
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    leave_id    BIGINT      NOT NULL COMMENT '请假单id',
    node_name   VARCHAR(32) NOT NULL COMMENT '节点 SUPERVISOR/HR',
    approver_id BIGINT      NOT NULL COMMENT '审批人id',
    action      VARCHAR(16) NOT NULL COMMENT 'APPROVE/REJECT',
    comment     VARCHAR(255) COMMENT '审批意见',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_leave (leave_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;