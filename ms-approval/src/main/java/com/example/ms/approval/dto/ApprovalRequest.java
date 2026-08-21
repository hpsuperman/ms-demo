package com.example.ms.approval.dto;

import com.example.ms.approval.enums.ApprovalAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalRequest {
    @NotNull(message = "审批动作不能为空")
    private ApprovalAction action;

    private String comment;
}
