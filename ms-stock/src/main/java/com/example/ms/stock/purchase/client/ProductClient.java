package com.example.ms.stock.purchase.client;

import com.example.ms.common.ApiResponse;
import com.example.ms.stock.purchase.client.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-order")
public interface ProductClient {
    @GetMapping("/order/product/{id}")
    ApiResponse<ProductDTO> detail(@PathVariable("id") Long id);

    @PutMapping("/order/product/{id}/stock/increase")
    ApiResponse<Void> increaseStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}
