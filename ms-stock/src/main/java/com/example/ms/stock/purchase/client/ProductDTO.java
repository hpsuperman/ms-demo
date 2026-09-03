package com.example.ms.stock.purchase.client;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
    private String status;
}