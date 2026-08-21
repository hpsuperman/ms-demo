package com.example.ms.approval.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveDetailResponse extends LeaveItemResponse {
    private String reason;          // 列表没有，详情要展示
    private String applicantName;   // 跨服务查
    private List<ApprovalRecordResponse> records;
}
