package com.example.ms.approval.client;

import com.example.ms.approval.dto.UserDTO;

import com.example.ms.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-user")
public interface UserClient {

    @GetMapping("/user/{id}")
    ApiResponse<UserDTO> getUser(@PathVariable("id") Long id);

    @GetMapping("/user/role/{roleName}")
    ApiResponse<List<UserDTO>> listByRole(@PathVariable("roleName") String roleName);
}