package com.example.ms.role.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_user_role")
public class UserRole {
    @TableId // 明确指定联合主键
    private Long userId;
    private Long roleId;
}
