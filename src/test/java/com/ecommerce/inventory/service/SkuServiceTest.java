package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.entity.Sku;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.SkuMapper;
import com.ecommerce.inventory.repository.SkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkuServiceTest {

    @Mock
    private SkuRepository skuRepository;

    @Mock
    private SkuMapper skuMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private SkuService skuService;

    private Product product;
    private Sku sku;
    private SkuDto skuDto;
    private CreateSkuRequest createRequest;
    private UpdateSkuRequest updateRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .basePrice(new BigDecimal("999.99"))
                .build();

        sku = Sku.builder()
                .id(1L)
                .skuCode("IPHONE15-128-BLK")
                .name("iPhone 15 - 128GB - Black")
                .attributes("Color: Black, Storage: 128GB")
                .price(new BigDecimal("999.99"))
                .quantity(100)
                .product(product)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        skuDto = SkuDto.builder()
                .id(1L)
                .skuCode("IPHONE15-128-BLK")
                .name("iPhone 15 - 128GB - Black")
                .attributes("Color: Black, Storage: 128GB")
                .price(new BigDecimal("999.99"))
                .quantity(100)
                .productId(1L)
                .productName("iPhone 15")
                .build();

        createRequest = CreateSkuRequest.builder()
                .skuCode("IPHONE15-128-BLK")
                .name("iPhone 15 - 128GB - Black")
                .attributes("Color: Black, Storage: 128GB")
                .price(new BigDecimal("999.99"))
                .quantity(100)
                .build();

        updateRequest = UpdateSkuRequest.builder()
                .name("iPhone 15 - 128GB - Space Black")
                .quantity(150)
                .build();
    }

    @Nested
    @DisplayName("Get SKUs By Product ID Tests")
    class GetSkusByProductIdTests {

        @Test
        @DisplayName("Should return all SKUs for a product")
        void shouldReturnAllSkusForProduct() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByProductId(1L)).thenReturn(List.of(sku));
            when(skuMapper.toDto(sku)).thenReturn(skuDto);

            List<SkuDto> result = skuService.getSkusByProductId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSkuCode()).isEqualTo("IPHONE15-128-BLK");
        }

        @Test
        @DisplayName("Should return empty list when no SKUs exist")
        void shouldReturnEmptyListWhenNoSkusExist() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByProductId(1L)).thenReturn(Collections.emptyList());

            List<SkuDto> result = skuService.getSkusByProductId(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(productService.findProductById(999L))
                    .thenThrow(new ResourceNotFoundException("Product", "id", 999L));

            assertThatThrownBy(() -> skuService.getSkusByProductId(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get SKU By ID Tests")
    class GetSkuByIdTests {

        @Test
        @DisplayName("Should return SKU when found")
        void shouldReturnSkuWhenFound() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByIdAndProductId(1L, 1L)).thenReturn(Optional.of(sku));
            when(skuMapper.toDto(sku)).thenReturn(skuDto);

            SkuDto result = skuService.getSkuById(1L, 1L);

            assertThat(result.getSkuCode()).isEqualTo("IPHONE15-128-BLK");
        }

        @Test
        @DisplayName("Should throw exception when SKU not found")
        void shouldThrowExceptionWhenSkuNotFound() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByIdAndProductId(999L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skuService.getSkuById(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create SKU Tests")
    class CreateSkuTests {

        @Test
        @DisplayName("Should create SKU successfully")
        void shouldCreateSkuSuccessfully() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.existsBySkuCode(createRequest.getSkuCode())).thenReturn(false);
            when(skuMapper.toEntity(createRequest)).thenReturn(sku);
            when(skuRepository.save(any(Sku.class))).thenReturn(sku);
            when(skuMapper.toDto(sku)).thenReturn(skuDto);

            SkuDto result = skuService.createSku(1L, createRequest);

            assertThat(result.getSkuCode()).isEqualTo("IPHONE15-128-BLK");
            verify(skuRepository).save(any(Sku.class));
        }

        @Test
        @DisplayName("Should throw exception when SKU code already exists")
        void shouldThrowExceptionWhenSkuCodeExists() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.existsBySkuCode(createRequest.getSkuCode())).thenReturn(true);

            assertThatThrownBy(() -> skuService.createSku(1L, createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Update SKU Tests")
    class UpdateSkuTests {

        @Test
        @DisplayName("Should update SKU successfully")
        void shouldUpdateSkuSuccessfully() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByIdAndProductId(1L, 1L)).thenReturn(Optional.of(sku));
            when(skuRepository.save(any(Sku.class))).thenReturn(sku);
            when(skuMapper.toDto(any(Sku.class))).thenReturn(skuDto);

            SkuDto result = skuService.updateSku(1L, 1L, updateRequest);

            assertThat(result).isNotNull();
            verify(skuMapper).updateEntityFromRequest(any(Sku.class), eq(updateRequest));
        }

        @Test
        @DisplayName("Should throw exception when updating to existing SKU code")
        void shouldThrowExceptionWhenUpdatingToExistingSkuCode() {
            updateRequest.setSkuCode("EXISTING-CODE");
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByIdAndProductId(1L, 1L)).thenReturn(Optional.of(sku));
            when(skuRepository.existsBySkuCodeAndIdNot("EXISTING-CODE", 1L)).thenReturn(true);

            assertThatThrownBy(() -> skuService.updateSku(1L, 1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Delete SKU Tests")
    class DeleteSkuTests {

        @Test
        @DisplayName("Should delete SKU successfully")
        void shouldDeleteSkuSuccessfully() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByIdAndProductId(1L, 1L)).thenReturn(Optional.of(sku));

            skuService.deleteSku(1L, 1L);

            verify(skuRepository).delete(sku);
        }

        @Test
        @DisplayName("Should throw exception when SKU not found")
        void shouldThrowExceptionWhenSkuNotFound() {
            when(productService.findProductById(1L)).thenReturn(product);
            when(skuRepository.findByIdAndProductId(999L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skuService.deleteSku(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

