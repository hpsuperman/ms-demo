package com.example.ms.department.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ms.common.BaseEntity;
import com.example.ms.department.converter.DepartmentConverter;
import com.example.ms.department.dto.DepartmentRequest;
import com.example.ms.department.dto.DepartmentResponse;
import com.example.ms.department.entity.Department;
import com.example.ms.department.mapper.DepartmentMapper;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.user.entity.User;
import com.example.ms.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class DepartmentService {
    private final DepartmentMapper departmentMapper;
    private final DepartmentConverter departmentConverter;
    private final UserMapper userMapper;

    public List<DepartmentResponse> list() {
        List<Department> list = departmentMapper.selectList(null);
        Map<Long, DepartmentResponse> map = list.stream().collect(Collectors.toMap(Department::getId, departmentConverter::toResponse));
        List<DepartmentResponse> roots = new ArrayList<>();
        for (DepartmentResponse node : map.values()) {
            if (node.getParentId() == 0) {
                roots.add(node);
            } else {
                map.get(node.getParentId()).getChildren().add(node);
            }
        }
        fillLeaderNames(map.values());
        fillMemberCounts(map.values());
        return roots;
    }

    public void fillLeaderNames(Collection<DepartmentResponse> nodes) {
        Set<Long> leaderIds = nodes.stream().map(DepartmentResponse::getLeaderId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (leaderIds.isEmpty()) return;
        Map<Long, String> nameMap = new HashMap<>();
        userMapper.selectByIds(leaderIds).forEach(u -> nameMap.put(u.getId(), u.getNickname()));
        nodes.forEach(n -> {
            if (n.getLeaderId() != null) {
                n.setLeaderName(nameMap.get(n.getLeaderId()));
            }
        });
    }

    private void fillMemberCounts(Collection<DepartmentResponse> nodes) {
        Map<Long, Long> countMap = userMapper.selectList(null).stream().filter(u -> u.getDepartmentId() != null).collect(Collectors.groupingBy(User::getDepartmentId, Collectors.counting()));
        nodes.forEach(n -> n.setMemberCount(countMap.getOrDefault(n.getId(), 0L).intValue()));
    }

    public DepartmentResponse create(DepartmentRequest request) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>().eq(Department::getName, request.getName()).eq(Department::getParentId, request.getParentId() == null ? 0L : request.getParentId());

        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "已存在相同名称不能提交");
        }
        if (request.getParentId() != null && request.getParentId() != 0L) {
            if (departmentMapper.selectById(request.getParentId()) == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "父部门不存在");
            }
        }
        Department department = departmentConverter.toEntity(request);
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        departmentMapper.insert(department);
        return departmentConverter.toResponse(department);
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "部门不存在");
        }
        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "上级部门不能是自己");
        }
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>().eq(Department::getName, request.getName()).eq(Department::getParentId, request.getParentId() == null ? 0L : request.getParentId()).ne(Department::getId, id);
        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "同级部门名称已存在");
        }
        departmentConverter.updateEntity(request, department);
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        departmentMapper.updateById(department);
        return departmentConverter.toResponse(department);

    }

    public void delete(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "暂无数据");
        }
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>().eq(Department::getParentId, id);
        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该部门下有子部，无法删除");
        }
        LambdaQueryWrapper<User> wrapper1 = new LambdaQueryWrapper<User>().eq(User::getDepartmentId, id);
        if (userMapper.selectCount(wrapper1) > 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该部门下有员工，无法删除");
        }
        departmentMapper.deleteById(id);
    }

    public DepartmentResponse detail(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "暂无数据");
        }
        return departmentConverter.toResponse(department);
    }
}
