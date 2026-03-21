package com.example.gym.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreCategoryDto {
    private Long id;
    private Long gymId;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
    private List<StoreProductDto> products;
}
