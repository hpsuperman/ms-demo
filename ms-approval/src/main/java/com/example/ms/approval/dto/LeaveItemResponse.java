package com.example.ms.approval.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveItemResponse {
    private Long id;
    private String leaveTypeText;
    private String applicantName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationHours;
    private String statusText;
    private LocalDateTime createdAt;
}
