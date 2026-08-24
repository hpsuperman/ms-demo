package com.example.ms.approval.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoneItemResponse extends LeaveItemResponse {
    private String myNodeText;
    private String myActionText;
    private String myComment;
    private LocalDateTime reviewedAt;
}
