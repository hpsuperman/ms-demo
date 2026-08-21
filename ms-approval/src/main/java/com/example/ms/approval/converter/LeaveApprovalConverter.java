package com.example.ms.approval.converter;

import com.example.ms.approval.dto.*;
import com.example.ms.approval.entity.ApprovalRecord;
import com.example.ms.approval.entity.Leave;
import com.example.ms.approval.enums.ApprovalAction;
import com.example.ms.approval.enums.ApprovalNode;
import com.example.ms.approval.enums.LeaveStatus;
import com.example.ms.approval.enums.LeaveType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LeaveApprovalConverter {

    LeaveResponse toResponse(Leave leave);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "durationHours", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentNode", ignore = true)
    Leave toEntity(LeaveRequest request);

    @Mapping(target = "leaveTypeText", source = "leaveType", qualifiedByName = "typeText")
    @Mapping(target = "statusText", source = "status", qualifiedByName = "statusText")
    LeaveItemResponse toItemResponse(Leave leave);

    @Named("typeText")
    default String typeText(LeaveType type) {
        return type == null ? null : type.getDesc();
    }

    @Named("statusText")
    default String statusText(LeaveStatus status) {
        return status == null ? null : status.getDesc();
    }

    @Named("nodeText")
    default String nodeText(ApprovalNode node) {
        return node == null ? null : node.getDesc();
    }

    @Named("actionText")
    default String actionText(ApprovalAction action) {
        return action == null ? null : action.getDesc();
    }

    @Mapping(target = "leaveTypeText", source = "leaveType", qualifiedByName = "typeText")
    @Mapping(target = "statusText", source = "status", qualifiedByName = "statusText")
    @Mapping(target = "records", ignore = true)
    @Mapping(target = "applicantName", ignore = true)
    LeaveDetailResponse toDetailResponse(Leave leave);


    @Mapping(target = "nodeNameText", source = "nodeName", qualifiedByName = "nodeText")
    @Mapping(target = "actionText", source = "action", qualifiedByName = "actionText")
    ApprovalRecordResponse toResponse(ApprovalRecord record);

    @Mapping(target = "leaveId", ignore = true)
    @Mapping(target = "nodeName", ignore = true)
    @Mapping(target = "approverId", ignore = true)
    ApprovalRecord toEntity(ApprovalRequest request);

}
