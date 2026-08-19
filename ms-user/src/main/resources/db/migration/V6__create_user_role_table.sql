CREATE TABLE t_user_role
(
    user_id BIGINT NOT NULL COMMENT '用户id',
    role_id BIGINT NOT NULL COMMENT '角色id',
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES t_user (id),
    FOREIGN KEY (role_id) REFERENCES t_role (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO t_role(name, description)
VALUES ('ADMIN', '系统管理员'),
       ('HR', '人事管理'),
       ('USER', '普通员工')
