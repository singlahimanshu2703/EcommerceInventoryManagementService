package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> findByFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    boolean existsByNameAndCategoryId(String name, Long categoryId);

    boolean existsByNameAndCategoryIdAndIdNot(String name, Long categoryId, Long id);
}

