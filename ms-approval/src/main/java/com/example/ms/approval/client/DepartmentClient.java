package com.example.ms.approval.client;

import com.example.ms.approval.dto.DepartmentDTO;

import com.example.ms.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-user")
public interface DepartmentClient {

    @GetMapping("/department/detail/{id}")
    ApiResponse<DepartmentDTO> getDepartment(@PathVariable("id") Long id);
}