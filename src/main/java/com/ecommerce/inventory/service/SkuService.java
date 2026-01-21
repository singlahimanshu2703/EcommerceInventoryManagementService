package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.entity.Sku;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.SkuMapper;
import com.ecommerce.inventory.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SkuService {

    private final SkuRepository skuRepository;
    private final SkuMapper skuMapper;
    private final ProductService productService;

    public List<SkuDto> getSkusByProductId(Long productId) {
        log.info("Fetching all SKUs for product id: {}", productId);

        // Verify product exists
        productService.findProductById(productId);

        return skuRepository.findByProductId(productId).stream()
                .map(skuMapper::toDto)
                .collect(Collectors.toList());
    }

    public SkuDto getSkuById(Long productId, Long skuId) {
        log.info("Fetching SKU with id: {} for product id: {}", skuId, productId);

        // Verify product exists
        productService.findProductById(productId);

        Sku sku = findSkuByIdAndProductId(skuId, productId);
        return skuMapper.toDto(sku);
    }

    @Transactional
    public SkuDto createSku(Long productId, CreateSkuRequest request) {
        log.info("Creating new SKU with code: {} for product id: {}", request.getSkuCode(), productId);

        Product product = productService.findProductById(productId);

        if (skuRepository.existsBySkuCode(request.getSkuCode())) {
            throw new DuplicateResourceException("SKU", "skuCode", request.getSkuCode());
        }

        Sku sku = skuMapper.toEntity(request);
        sku.setProduct(product);
        Sku savedSku = skuRepository.save(sku);

        log.info("SKU created successfully with id: {}", savedSku.getId());
        return skuMapper.toDto(savedSku);
    }

    @Transactional
    public SkuDto updateSku(Long productId, Long skuId, UpdateSkuRequest request) {
        log.info("Updating SKU with id: {} for product id: {}", skuId, productId);

        // Verify product exists
        productService.findProductById(productId);

        Sku sku = findSkuByIdAndProductId(skuId, productId);

        if (request.getSkuCode() != null && !request.getSkuCode().equals(sku.getSkuCode())) {
            if (skuRepository.existsBySkuCodeAndIdNot(request.getSkuCode(), skuId)) {
                throw new DuplicateResourceException("SKU", "skuCode", request.getSkuCode());
            }
        }

        skuMapper.updateEntityFromRequest(sku, request);
        Sku updatedSku = skuRepository.save(sku);

        log.info("SKU updated successfully with id: {}", skuId);
        return skuMapper.toDto(updatedSku);
    }

    @Transactional
    public void deleteSku(Long productId, Long skuId) {
        log.info("Deleting SKU with id: {} for product id: {}", skuId, productId);

        // Verify product exists
        productService.findProductById(productId);

        Sku sku = findSkuByIdAndProductId(skuId, productId);
        skuRepository.delete(sku);

        log.info("SKU deleted successfully with id: {}", skuId);
    }

    private Sku findSkuByIdAndProductId(Long skuId, Long productId) {
        return skuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("SKU not found with id: %d for product id: %d", skuId, productId)));
    }
}

