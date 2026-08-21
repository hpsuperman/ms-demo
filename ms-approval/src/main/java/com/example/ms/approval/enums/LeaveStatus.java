package com.example.ms.approval.enums;

import lombok.Getter;

@Getter
public enum LeaveStatus {
    PENDING("待审批"),
    APPROVING("审批中"),
    APPROVED("已通过"),
    REJECTED("已驳回"),
    CANCELED("已撤回");

    private final String desc;

    LeaveStatus(String desc) {
        this.desc = desc;
    }
}