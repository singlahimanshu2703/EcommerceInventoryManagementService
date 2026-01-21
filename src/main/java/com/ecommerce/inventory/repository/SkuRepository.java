package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long> {

    List<Sku> findByProductId(Long productId);

    Optional<Sku> findBySkuCode(String skuCode);

    boolean existsBySkuCode(String skuCode);

    boolean existsBySkuCodeAndIdNot(String skuCode, Long id);

    Optional<Sku> findByIdAndProductId(Long id, Long productId);

    void deleteByIdAndProductId(Long id, Long productId);
}

