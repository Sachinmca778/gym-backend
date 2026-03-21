package com.example.gym.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShoppingCartDto {
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private Integer totalItems;
    
    public void calculateTotals() {
        subtotal = items.stream()
            .map(CartItemDto::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalItems = items.stream()
            .mapToInt(CartItemDto::getQuantity)
            .sum();
        total = subtotal.subtract(discount != null ? discount : BigDecimal.ZERO)
            .add(tax != null ? tax : BigDecimal.ZERO);
    }
}
