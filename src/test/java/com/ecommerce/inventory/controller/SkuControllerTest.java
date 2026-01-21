package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.GlobalExceptionHandler;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.service.SkuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SkuControllerTest {

    @Mock
    private SkuService skuService;

    @InjectMocks
    private SkuController skuController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SkuDto skuDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(skuController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

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
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productId}/skus")
    class GetSkusByProductIdTests {

        @Test
        @DisplayName("Should return all SKUs for a product")
        void shouldReturnAllSkusForProduct() throws Exception {
            when(skuService.getSkusByProductId(1L)).thenReturn(List.of(skuDto));

            mockMvc.perform(get("/api/v1/products/1/skus"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].skuCode").value("IPHONE15-128-BLK"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(skuService.getSkusByProductId(999L))
                    .thenThrow(new ResourceNotFoundException("Product", "id", 999L));

            mockMvc.perform(get("/api/v1/products/999/skus"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productId}/skus/{skuId}")
    class GetSkuByIdTests {

        @Test
        @DisplayName("Should return SKU when found")
        void shouldReturnSkuWhenFound() throws Exception {
            when(skuService.getSkuById(1L, 1L)).thenReturn(skuDto);

            mockMvc.perform(get("/api/v1/products/1/skus/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.skuCode").value("IPHONE15-128-BLK"));
        }

        @Test
        @DisplayName("Should return 404 when SKU not found")
        void shouldReturn404WhenSkuNotFound() throws Exception {
            when(skuService.getSkuById(1L, 999L))
                    .thenThrow(new ResourceNotFoundException("SKU not found with id: 999 for product id: 1"));

            mockMvc.perform(get("/api/v1/products/1/skus/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products/{productId}/skus")
    class CreateSkuTests {

        @Test
        @DisplayName("Should create SKU successfully")
        void shouldCreateSkuSuccessfully() throws Exception {
            CreateSkuRequest request = CreateSkuRequest.builder()
                    .skuCode("IPHONE15-128-BLK")
                    .name("iPhone 15 - 128GB - Black")
                    .attributes("Color: Black, Storage: 128GB")
                    .price(new BigDecimal("999.99"))
                    .quantity(100)
                    .build();

            when(skuService.createSku(eq(1L), any(CreateSkuRequest.class))).thenReturn(skuDto);

            mockMvc.perform(post("/api/v1/products/1/skus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.skuCode").value("IPHONE15-128-BLK"));
        }

        @Test
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            CreateSkuRequest request = CreateSkuRequest.builder()
                    .skuCode("")
                    .build();

            mockMvc.perform(post("/api/v1/products/1/skus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 for duplicate SKU code")
        void shouldReturn409ForDuplicateSkuCode() throws Exception {
            CreateSkuRequest request = CreateSkuRequest.builder()
                    .skuCode("IPHONE15-128-BLK")
                    .name("iPhone 15 - 128GB - Black")
                    .price(new BigDecimal("999.99"))
                    .quantity(100)
                    .build();

            when(skuService.createSku(eq(1L), any(CreateSkuRequest.class)))
                    .thenThrow(new DuplicateResourceException("SKU", "skuCode", "IPHONE15-128-BLK"));

            mockMvc.perform(post("/api/v1/products/1/skus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{productId}/skus/{skuId}")
    class UpdateSkuTests {

        @Test
        @DisplayName("Should update SKU successfully")
        void shouldUpdateSkuSuccessfully() throws Exception {
            UpdateSkuRequest request = UpdateSkuRequest.builder()
                    .name("iPhone 15 - 128GB - Space Black")
                    .quantity(150)
                    .build();

            when(skuService.updateSku(eq(1L), eq(1L), any(UpdateSkuRequest.class))).thenReturn(skuDto);

            mockMvc.perform(put("/api/v1/products/1/skus/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{productId}/skus/{skuId}")
    class DeleteSkuTests {

        @Test
        @DisplayName("Should delete SKU successfully")
        void shouldDeleteSkuSuccessfully() throws Exception {
            doNothing().when(skuService).deleteSku(1L, 1L);

            mockMvc.perform(delete("/api/v1/products/1/skus/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 404 when SKU not found")
        void shouldReturn404WhenSkuNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("SKU not found"))
                    .when(skuService).deleteSku(1L, 999L);

            mockMvc.perform(delete("/api/v1/products/1/skus/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}

