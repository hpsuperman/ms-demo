package com.example.ms.approval.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalRecordResponse {

    private String nodeNameText;
    private String approverName;
    private String actionText;
    private String comment;
    private LocalDateTime createdAt;
}
