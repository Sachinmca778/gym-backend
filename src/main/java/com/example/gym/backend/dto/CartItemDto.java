package com.example.gym.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal memberPrice;
    private Integer discountPercentage;
    private BigDecimal total;
    private String imageUrl;
    private boolean isMember;
    
    public void calculateTotal() {
        BigDecimal price = isMember && memberPrice != null ? memberPrice : unitPrice;
        if (discountPercentage > 0) {
            price = price.multiply(BigDecimal.valueOf(100 - discountPercentage))
                .divide(BigDecimal.valueOf(100));
        }
        this.total = price.multiply(BigDecimal.valueOf(quantity));
    }
}
