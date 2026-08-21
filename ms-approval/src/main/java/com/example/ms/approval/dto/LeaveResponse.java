package com.example.ms.approval.dto;

import com.example.ms.approval.enums.LeaveStatus;
import com.example.ms.approval.enums.LeaveType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveResponse {

    private Long id;
    private String applicantName;
    private LeaveType leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationHours;
    private String reason;
    private LeaveStatus status;
    private String currentNode;
    private LocalDateTime createdAt;
}
