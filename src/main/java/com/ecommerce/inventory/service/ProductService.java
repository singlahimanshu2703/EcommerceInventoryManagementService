package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.ProductMapper;
import com.ecommerce.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    public PagedResponse<ProductDto> getAllProducts(String name, Long categoryId, int page, int pageSize) {
        log.info("Fetching products with filters - name: {}, categoryId: {}, page: {}, pageSize: {}",
                name, categoryId, page, pageSize);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findByFilters(name, categoryId, pageable);

        return buildPagedResponse(productPage);
    }

    public ProductDto getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = findProductById(id);
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        log.info("Creating new product with name: {}", request.getName());

        Category category = categoryService.findCategoryById(request.getCategoryId());

        if (productRepository.existsByNameAndCategoryId(request.getName(), request.getCategoryId())) {
            throw new DuplicateResourceException(
                    String.format("Product with name '%s' already exists in category '%s'",
                            request.getName(), category.getName()));
        }

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);

        Product product = findProductById(id);

        if (request.getCategoryId() != null) {
            Category newCategory = categoryService.findCategoryById(request.getCategoryId());
            product.setCategory(newCategory);
        }

        if (request.getName() != null && !request.getName().equals(product.getName())) {
            Long categoryId = request.getCategoryId() != null ? request.getCategoryId() : product.getCategory().getId();
            if (productRepository.existsByNameAndCategoryIdAndIdNot(request.getName(), categoryId, id)) {
                throw new DuplicateResourceException(
                        String.format("Product with name '%s' already exists in the category", request.getName()));
            }
        }

        productMapper.updateEntityFromRequest(product, request);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with id: {}", id);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        Product product = findProductById(id);
        productRepository.delete(product);

        log.info("Product deleted successfully with id: {}", id);
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private PagedResponse<ProductDto> buildPagedResponse(Page<Product> productPage) {
        return PagedResponse.<ProductDto>builder()
                .content(productPage.getContent().stream()
                        .map(productMapper::toDto)
                        .toList())
                .page(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }
}

