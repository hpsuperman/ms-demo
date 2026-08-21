package com.example.ms.approval.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.approval.enums.LeaveStatus;
import com.example.ms.approval.enums.LeaveType;
import com.example.ms.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@TableName("t_leave")
@Getter
@Setter
@NoArgsConstructor
public class Leave extends BaseEntity {
    private Long userId;
    private Long applicantLeaderId;
    private LeaveType leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private LeaveStatus status;
    private Integer durationHours;
    private String currentNode;

    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
