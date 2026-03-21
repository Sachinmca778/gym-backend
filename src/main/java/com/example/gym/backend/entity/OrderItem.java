package com.example.gym.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "store_order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private StoreOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private StoreProduct product;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_sku")
    private String productSku;

    @Positive
    private Integer quantity;

    @Positive
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Positive
    @Column(name = "discount_percentage")
    private Integer discountPercentage = 0;

    @Positive
    @Column(name = "tax_percentage")
    private Integer taxPercentage = 0;

    @Positive
    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            calculateTotal();
        }
    }

    public void calculateTotal() {
        if (unitPrice != null && quantity != null) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            if (discountPercentage > 0) {
                subtotal = subtotal.multiply(BigDecimal.valueOf(100 - discountPercentage))
                        .divide(BigDecimal.valueOf(100));
            }
            if (taxPercentage > 0) {
                subtotal = subtotal.multiply(BigDecimal.valueOf(100 + taxPercentage))
                        .divide(BigDecimal.valueOf(100));
            }
            this.total = subtotal;
        }
    }

    // Helper method to set product details
    public void setProductDetails(StoreProduct product) {
        this.product = product;
        this.productName = product.getName();
        this.productSku = product.getSku();
    }
}
