package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.entity.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SkuRepositoryTest {

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Product product;
    private Sku sku;

    @BeforeEach
    void setUp() {
        skuRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        category = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
        category = categoryRepository.save(category);

        product = Product.builder()
                .name("iPhone 15")
                .description("Latest iPhone model")
                .basePrice(new BigDecimal("999.99"))
                .brand("Apple")
                .category(category)
                .build();
        product = productRepository.save(product);

        sku = Sku.builder()
                .skuCode("IPHONE15-128-BLK")
                .name("iPhone 15 - 128GB - Black")
                .attributes("Color: Black, Storage: 128GB")
                .price(new BigDecimal("999.99"))
                .quantity(100)
                .product(product)
                .build();
    }

    @Test
    @DisplayName("Should save SKU successfully")
    void shouldSaveSkuSuccessfully() {
        Sku savedSku = skuRepository.save(sku);

        assertThat(savedSku.getId()).isNotNull();
        assertThat(savedSku.getSkuCode()).isEqualTo("IPHONE15-128-BLK");
        assertThat(savedSku.getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    @DisplayName("Should find SKUs by product id")
    void shouldFindSkusByProductId() {
        skuRepository.save(sku);

        Sku another = Sku.builder()
                .skuCode("IPHONE15-256-WHT")
                .name("iPhone 15 - 256GB - White")
                .attributes("Color: White, Storage: 256GB")
                .price(new BigDecimal("1099.99"))
                .quantity(50)
                .product(product)
                .build();
        skuRepository.save(another);

        List<Sku> found = skuRepository.findByProductId(product.getId());

        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("Should find SKU by code")
    void shouldFindSkuByCode() {
        skuRepository.save(sku);

        Optional<Sku> found = skuRepository.findBySkuCode("IPHONE15-128-BLK");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("iPhone 15 - 128GB - Black");
    }

    @Test
    @DisplayName("Should check if SKU exists by code")
    void shouldCheckIfSkuExistsByCode() {
        skuRepository.save(sku);

        boolean exists = skuRepository.existsBySkuCode("IPHONE15-128-BLK");
        boolean notExists = skuRepository.existsBySkuCode("NONEXISTENT");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if SKU exists by code excluding specific id")
    void shouldCheckIfSkuExistsByCodeExcludingId() {
        Sku saved = skuRepository.save(sku);

        Sku another = Sku.builder()
                .skuCode("IPHONE15-256-WHT")
                .name("iPhone 15 - 256GB - White")
                .attributes("Color: White, Storage: 256GB")
                .price(new BigDecimal("1099.99"))
                .quantity(50)
                .product(product)
                .build();
        skuRepository.save(another);

        boolean exists = skuRepository.existsBySkuCodeAndIdNot("IPHONE15-256-WHT", saved.getId());
        boolean notExists = skuRepository.existsBySkuCodeAndIdNot("IPHONE15-128-BLK", saved.getId());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find SKU by id and product id")
    void shouldFindSkuByIdAndProductId() {
        Sku saved = skuRepository.save(sku);

        Optional<Sku> found = skuRepository.findByIdAndProductId(saved.getId(), product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSkuCode()).isEqualTo("IPHONE15-128-BLK");
    }

    @Test
    @DisplayName("Should return empty when SKU not found by id and product id")
    void shouldReturnEmptyWhenSkuNotFoundByIdAndProductId() {
        Sku saved = skuRepository.save(sku);

        Optional<Sku> found = skuRepository.findByIdAndProductId(saved.getId(), 999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete SKU")
    void shouldDeleteSku() {
        Sku saved = skuRepository.save(sku);
        skuRepository.delete(saved);

        assertThat(skuRepository.findById(saved.getId())).isEmpty();
    }
}

