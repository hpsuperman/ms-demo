package com.example.ms.department.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_department")
public class Department extends BaseEntity {
    private Long parentId;
    private String name;
    private Long leaderId;
    private Integer sort;
    private DepartmentStatus status;

    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
