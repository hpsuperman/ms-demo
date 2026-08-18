package com.example.ms.user.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_user")
public class User extends BaseEntity {
    private String phone;
    private String passwordHash;
    private String nickname;
    private String avatar;
    private String gender;
    private LocalDate birthday;
    private UserStatus status;
    private String roles;
    private String employeeNo;
    private Long departmentId;
    private String position;
    private String email;

    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
