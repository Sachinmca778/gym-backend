package com.example.gym.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "store_products", indexes = {
    @Index(name = "idx_product_gym", columnList = "gym_id"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_active", columnList = "isActive"),
    @Index(name = "idx_product_stock", columnList = "stockQuantity")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private StoreCategory category;

    @NotBlank
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Positive
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Positive
    @Column(name = "member_price", precision = 10, scale = 2)
    private BigDecimal memberPrice;

    @Positive
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;

    @Column(name = "sku", unique = true)
    private String sku;

    private String brand;

    private String unit; // e.g., "kg", "pcs", "bottle"

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_featured")
    private boolean isFeatured = false;

    @Column(name = "discount_percentage")
    private Integer discountPercentage = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Auto-generate SKU if not provided
        if (sku == null || sku.isEmpty()) {
            this.sku = "PRD-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to get effective price
    public BigDecimal getEffectivePrice(boolean isMember) {
        if (isMember && memberPrice != null) {
            return memberPrice;
        }
        if (discountPercentage > 0) {
            return price.multiply(BigDecimal.valueOf(100 - discountPercentage))
                    .divide(BigDecimal.valueOf(100));
        }
        return price;
    }
}
