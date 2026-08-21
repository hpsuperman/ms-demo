package com.example.ms.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.ms.approval.enums.ApprovalAction;
import com.example.ms.approval.enums.ApprovalNode;
import com.example.ms.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@TableName("t_approval_record")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private Long leaveId;
    private ApprovalNode nodeName;
    private Long approverId;
    private ApprovalAction action;
    private String comment;
}
