package com.example.ms.approval.enums;

import lombok.Getter;

@Getter
public enum ApprovalAction {
    APPROVE("通过"),
    REJECT("驳回");

    private final String desc;

    ApprovalAction(String desc) {
        this.desc = desc;
    }
}
