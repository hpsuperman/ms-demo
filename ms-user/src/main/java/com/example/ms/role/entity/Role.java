package com.example.ms.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_role")
public class Role extends BaseEntity {
    private String name;
    private String description;
}
