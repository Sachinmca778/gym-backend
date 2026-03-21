package com.example.gym.backend.repository;

import com.example.gym.backend.entity.StoreProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {

    @Query("SELECT p FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true ORDER BY p.createdAt DESC")
    Page<StoreProduct> findActiveProductsByGym(@Param("gymId") Long gymId, Pageable pageable);

    @Query("SELECT p FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true AND p.category.id = :categoryId ORDER BY p.createdAt DESC")
    Page<StoreProduct> findActiveProductsByGymAndCategory(@Param("gymId") Long gymId, 
                                                           @Param("categoryId") Long categoryId, 
                                                           Pageable pageable);

    @Query("SELECT p FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true AND p.isFeatured = true ORDER BY p.createdAt DESC")
    List<StoreProduct> findFeaturedProducts(@Param("gymId") Long gymId);

    @Query("SELECT p FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true AND p.stockQuantity <= p.minStockLevel")
    List<StoreProduct> findLowStockProducts(@Param("gymId") Long gymId);

    @Query("SELECT p FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<StoreProduct> searchProducts(@Param("gymId") Long gymId, 
                                      @Param("searchTerm") String searchTerm, 
                                      Pageable pageable);

    @Query("SELECT p FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true AND " +
           "p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<StoreProduct> findProductsByPriceRange(@Param("gymId") Long gymId,
                                                 @Param("minPrice") BigDecimal minPrice,
                                                 @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true AND p.stockQuantity > 0")
    List<StoreProduct> findInStockProducts(@Param("gymId") Long gymId);

    @Query("SELECT COUNT(p) FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true")
    long countActiveProducts(@Param("gymId") Long gymId);

    @Query("SELECT COUNT(p) FROM StoreProduct p WHERE p.gym.id = :gymId AND p.isActive = true AND p.stockQuantity <= p.minStockLevel")
    long countLowStockProducts(@Param("gymId") Long gymId);

    boolean existsBySku(String sku);

    List<StoreProduct> findByGymIdAndIsActiveTrue(Long gymId);
}
