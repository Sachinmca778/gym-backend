package com.example.gym.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer discountPercentage;
    private Integer taxPercentage;
    private BigDecimal total;
    private String notes;
}
