ALTER TABLE t_leave
    ADD COLUMN applicant_leader_id BIGINT COMMENT '申请人主管id',
    ADD INDEX idx_status_node_leader (status, current_node, applicant_leader_id);