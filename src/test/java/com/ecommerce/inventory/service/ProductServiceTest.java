package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.ProductMapper;
import com.ecommerce.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;
    private ProductDto productDto;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .build();

        product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Latest iPhone model")
                .basePrice(new BigDecimal("999.99"))
                .brand("Apple")
                .category(category)
                .skus(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Latest iPhone model")
                .basePrice(new BigDecimal("999.99"))
                .brand("Apple")
                .categoryId(1L)
                .categoryName("Electronics")
                .skuCount(0)
                .build();

        createRequest = CreateProductRequest.builder()
                .name("iPhone 15")
                .description("Latest iPhone model")
                .basePrice(new BigDecimal("999.99"))
                .brand("Apple")
                .categoryId(1L)
                .build();

        updateRequest = UpdateProductRequest.builder()
                .name("iPhone 15 Pro")
                .basePrice(new BigDecimal("1099.99"))
                .build();
    }

    @Nested
    @DisplayName("Get All Products Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products")
        void shouldReturnPaginatedProducts() {
            Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            when(productRepository.findByFilters(any(), any(), any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toDto(product)).thenReturn(productDto);

            PagedResponse<ProductDto> result = productService.getAllProducts(null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should filter products by name")
        void shouldFilterProductsByName() {
            Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            when(productRepository.findByFilters(eq("iPhone"), any(), any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toDto(product)).thenReturn(productDto);

            PagedResponse<ProductDto> result = productService.getAllProducts("iPhone", null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter products by category")
        void shouldFilterProductsByCategory() {
            Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            when(productRepository.findByFilters(any(), eq(1L), any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toDto(product)).thenReturn(productDto);

            PagedResponse<ProductDto> result = productService.getAllProducts(null, 1L, 0, 10);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Get Product By ID Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productMapper.toDto(product)).thenReturn(productDto);

            ProductDto result = productService.getProductById(1L);

            assertThat(result.getName()).isEqualTo("iPhone 15");
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            when(categoryService.findCategoryById(1L)).thenReturn(category);
            when(productRepository.existsByNameAndCategoryId(createRequest.getName(), 1L)).thenReturn(false);
            when(productMapper.toEntity(createRequest)).thenReturn(product);
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toDto(product)).thenReturn(productDto);

            ProductDto result = productService.createProduct(createRequest);

            assertThat(result.getName()).isEqualTo("iPhone 15");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when product name exists in category")
        void shouldThrowExceptionWhenProductNameExistsInCategory() {
            when(categoryService.findCategoryById(1L)).thenReturn(category);
            when(productRepository.existsByNameAndCategoryId(createRequest.getName(), 1L)).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(createRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(categoryService.findCategoryById(999L))
                    .thenThrow(new ResourceNotFoundException("Category", "id", 999L));

            createRequest.setCategoryId(999L);
            assertThatThrownBy(() -> productService.createProduct(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.existsByNameAndCategoryIdAndIdNot(any(), any(), any())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

            ProductDto result = productService.updateProduct(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(productMapper).updateEntityFromRequest(any(Product.class), eq(updateRequest));
        }

        @Test
        @DisplayName("Should update product category")
        void shouldUpdateProductCategory() {
            Category newCategory = Category.builder().id(2L).name("Phones").build();
            updateRequest.setCategoryId(2L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(categoryService.findCategoryById(2L)).thenReturn(newCategory);
            when(productRepository.existsByNameAndCategoryIdAndIdNot(any(), any(), any())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

            productService.updateProduct(1L, updateRequest);

            verify(categoryService).findCategoryById(2L);
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.deleteProduct(1L);

            verify(productRepository).delete(product);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

