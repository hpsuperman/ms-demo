package com.example.ms.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.approval.client.DepartmentClient;
import com.example.ms.approval.client.UserClient;
import com.example.ms.approval.converter.LeaveApprovalConverter;
import com.example.ms.approval.dto.*;
import com.example.ms.approval.entity.ApprovalRecord;
import com.example.ms.approval.entity.Leave;
import com.example.ms.approval.enums.ApprovalAction;
import com.example.ms.approval.enums.ApprovalNode;
import com.example.ms.approval.enums.LeaveStatus;
import com.example.ms.approval.mapper.ApprovalRecordMapper;
import com.example.ms.approval.mapper.LeaveMapper;
import com.example.ms.common.PageResponse;
import com.example.ms.common.context.UserContext;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveApprovalConverter leaveApprovalConverter;
    private final LeaveMapper leaveMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final DepartmentClient departmentClient;
    private final UserClient userClient;

    @Transactional
    public Long create(Long userId, LeaveRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
        Long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        if (hours <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请假时长必须大于 0");
        }
        UserDTO user = userClient.getUser(userId).getData();
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到用户信息");
        }
        DepartmentDTO department = departmentClient.getDepartment(user.getDepartmentId()).getData();
        if (department == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该部门");
        }
        if (department.getLeaderId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "所在部门未设置负责人，无法提交申请");
        }
        Leave leave = leaveApprovalConverter.toEntity(request);
        leave.setUserId(userId);
        leave.setDurationHours(hours.intValue());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setCurrentNode(ApprovalNode.SUPERVISOR.name());
        leave.setApplicantLeaderId(department.getLeaderId());
        leaveMapper.insert(leave);
        return leave.getId();
    }

    @Transactional
    public Long review(Long userId, Long id, ApprovalRequest request) {
        Leave leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该单据");
        }
        if (!LeaveStatus.PENDING.equals(leave.getStatus()) && !LeaveStatus.APPROVING.equals(leave.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前单据状态不可审批");
        }

        String nodeName = leave.getCurrentNode();
        if (nodeName == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前节点不明确");
        }
        ApprovalNode currentNode = ApprovalNode.valueOf(nodeName);

        UserDTO user = userClient.getUser(leave.getUserId()).getData();
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该用户");
        }
        DepartmentDTO department = departmentClient.getDepartment(user.getDepartmentId()).getData();
        if (department == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该部门");
        }

        if (currentNode == ApprovalNode.SUPERVISOR) {
            if (userId.equals(leave.getUserId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "不能审批自己的申请");
            }
            if (!userId.equals(department.getLeaderId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "暂无权限");
            }
        } else if (currentNode == ApprovalNode.HR) {
            List<UserDTO> hrUsers = userClient.listByRole("HR").getData();
            boolean isHr = hrUsers != null && hrUsers.stream().anyMatch(u -> u.getId().equals(userId));
            if (!isHr) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "仅 HR 可审批");
            }
        }

        ApprovalRecord record = leaveApprovalConverter.toEntity(request);
        record.setLeaveId(leave.getId());
        record.setNodeName(currentNode);
        record.setApproverId(userId);
        approvalRecordMapper.insert(record);

        if (request.getAction() == ApprovalAction.APPROVE) {
            if (currentNode == ApprovalNode.SUPERVISOR) {
                leave.setStatus(LeaveStatus.APPROVING);
                leave.setCurrentNode(ApprovalNode.HR.name());
            } else {
                leave.setStatus(LeaveStatus.APPROVED);
                leave.setCurrentNode(null);
            }
        } else {
            leave.setStatus(LeaveStatus.REJECTED);
            leave.setCurrentNode(null);
        }
        leaveMapper.updateById(leave);
        return leave.getId();
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveItemResponse> page(Long userId, LeaveStatus status, Pageable pageable) {
        IPage<Leave> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<Leave> wrapper = new LambdaQueryWrapper<Leave>().eq(Leave::getUserId, userId).eq(status != null, Leave::getStatus, status).orderByDesc(Leave::getCreatedAt);
        IPage<Leave> page = leaveMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, leaveApprovalConverter::toItemResponse);
    }

    @Transactional(readOnly = true)
    public LeaveDetailResponse detail(Long userId, Long id) {
        Leave leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到请假单");
        }
        if (!userId.equals(leave.getUserId())) {
            boolean isApprover = approvalRecordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                    .eq(ApprovalRecord::getLeaveId, id)
                    .eq(ApprovalRecord::getApproverId, userId)) > 0;
            if (!isApprover) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "暂无权限");
            }
        }
        UserDTO user = userClient.getUser(leave.getUserId()).getData();
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到用户信息");
        }

        List<ApprovalRecord> records = approvalRecordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>().eq(ApprovalRecord::getLeaveId, id).orderByAsc(ApprovalRecord::getCreatedAt));
        List<ApprovalRecordResponse> recordResponses = records.stream().map(r -> {
            ApprovalRecordResponse item = leaveApprovalConverter.toResponse(r);
            UserDTO approver = userClient.getUser(r.getApproverId()).getData();
            if (approver != null) {
                item.setApproverName(approver.getNickname());
            }
            return item;
        }).toList();
        LeaveDetailResponse resp = leaveApprovalConverter.toDetailResponse(leave);
        resp.setApplicantName(user.getNickname());
        resp.setReason(leave.getReason());
        resp.setRecords(recordResponses);
        return resp;
    }

    @Transactional
    public void cancel(Long userId, Long id) {
        Leave leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到当前请假单");
        }
        if (!userId.equals(leave.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "暂无权限");
        }
        if (!LeaveStatus.PENDING.equals(leave.getStatus()) && !LeaveStatus.APPROVING.equals(leave.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不在当前流程");
        }
        leave.setStatus(LeaveStatus.CANCELED);
        leave.setCurrentNode(null);
        leaveMapper.updateById(leave);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveItemResponse> todoPage(Long userId, Pageable pageable) {
        String role = UserContext.getRole();
        boolean isHr = role != null && Arrays.asList(role.split(",")).contains("HR");
        IPage<Leave> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<Leave> wrapper = new LambdaQueryWrapper<Leave>().and(w -> w.eq(Leave::getCurrentNode, ApprovalNode.SUPERVISOR.name()).eq(Leave::getApplicantLeaderId, userId).or().eq(isHr, Leave::getCurrentNode, ApprovalNode.HR.name())).orderByDesc(Leave::getCreatedAt);
        IPage<Leave> page = leaveMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, leaveApprovalConverter::toItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<DoneItemResponse> donePage(Long userId, ApprovalAction action, Pageable pageable) {
        IPage<ApprovalRecord> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<ApprovalRecord>().eq(ApprovalRecord::getApproverId, userId).eq(action != null, ApprovalRecord::getAction, action).orderByDesc(ApprovalRecord::getCreatedAt);
        IPage<ApprovalRecord> page = approvalRecordMapper.selectPage(mpPage, wrapper);

        List<ApprovalRecord> records = page.getRecords();

        List<Long> leaveIds = records.stream().map(ApprovalRecord::getLeaveId).toList();
        Map<Long, Leave> leaveMap = leaveIds.isEmpty()
                ? Map.of()
                : leaveMapper.selectByIds(leaveIds).stream()
                .collect(Collectors.toMap(Leave::getId, l -> l));

        Map<Long, String> applicantNames = new HashMap<>();
        leaveMap.values().stream().map(Leave::getUserId).distinct().forEach(applicantId -> {
            UserDTO user = userClient.getUser(applicantId).getData();
            if (user != null) {
                applicantNames.put(applicantId, user.getNickname());
            }
        });
        List<DoneItemResponse> items = records.stream().map(record -> {
            Leave leave = leaveMap.get(record.getLeaveId());
            if (leave == null) {
                return null;
            }
            DoneItemResponse item = new DoneItemResponse();
            item.setId(leave.getId());
            item.setLeaveTypeText(leaveApprovalConverter.typeText(leave.getLeaveType()));
            item.setApplicantName(applicantNames.get(leave.getUserId()));
            item.setStartTime(leave.getStartTime());
            item.setEndTime(leave.getEndTime());
            item.setDurationHours(leave.getDurationHours());
            item.setStatusText(leaveApprovalConverter.statusText(leave.getStatus()));
            item.setMyNodeText(leaveApprovalConverter.nodeText(record.getNodeName()));
            item.setMyActionText(leaveApprovalConverter.actionText(record.getAction()));
            item.setMyComment(record.getComment());
            item.setReviewedAt(record.getCreatedAt());
            return item;
        }).filter(Objects::nonNull).toList();
        return PageResponse.of(items, (int) page.getCurrent(), (int) page.getSize(), page.getTotal());
    }
}
