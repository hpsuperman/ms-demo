package com.example.ms.product.dto;

import com.example.ms.product.enums.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "名字不能为空")
    @Size(min = 4)
    private String name;

    @NotNull(message = "价格不能为空")
    @Min(1)
    private Integer price;

    @NotNull(message = "库存不能为空")
    @Min(1)
    private Integer stock;

    @NotNull(message = "状态不能为空")
    private ProductStatus status;
}
