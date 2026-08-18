ALTER TABLE t_department
    ADD UNIQUE KEY uk_parent_name (parent_id, name)

