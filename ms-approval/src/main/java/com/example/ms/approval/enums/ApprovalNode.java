package com.example.ms.approval.enums;

import lombok.Getter;

@Getter
public enum ApprovalNode {
    SUPERVISOR("主管审批"),
    HR("HR审批");

    private final String desc;

    ApprovalNode(String desc) {
        this.desc = desc;
    }
}
