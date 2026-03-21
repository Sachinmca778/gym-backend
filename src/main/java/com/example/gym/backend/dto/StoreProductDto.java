package com.example.gym.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreProductDto {
    private Long id;
    private Long gymId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String ingredients;
    private BigDecimal price;
    private BigDecimal memberPrice;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private String sku;
    private String brand;
    private String unit;
    private String imageUrl;
    private List<String> images;
    private boolean isActive;
    private boolean isFeatured;
    private Integer discountPercentage;
    private LocalDateTime createdAt;
    
    // Transient fields for UI
    private BigDecimal effectivePrice;
    private boolean isLowStock;
    private boolean isOutOfStock;
}
