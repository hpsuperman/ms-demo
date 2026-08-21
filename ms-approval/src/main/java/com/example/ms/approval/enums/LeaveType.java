package com.example.ms.approval.enums;

import lombok.Getter;

@Getter
public enum LeaveType {
    SICK("病假"),
    PERSONAL("事假"),
    ANNUAL("年假");

    private final String desc;

    LeaveType(String desc) {
        this.desc = desc;
    }
}