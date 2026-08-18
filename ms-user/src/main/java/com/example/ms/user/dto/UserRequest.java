package com.example.ms.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data

public class UserRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "名称不能为空")
    @Size(max = 20)
    private String nickname;

    @NotBlank(message = "编号不能为空")
    @Size(max = 20)
    private String employeeNo;

    private String gender;
    @Past(message = "生日不能晚于今天")
    private LocalDate birthday;
    private Long departmentId;
    private String position;
    @Email(message = "邮箱格式不正确")
    private String email;

}
