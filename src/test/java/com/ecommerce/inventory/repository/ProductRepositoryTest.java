package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("Should save product successfully")
    void shouldSaveProductSuccessfully() {
        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("iPhone 15");
        assertThat(savedProduct.getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("Should find products by name containing")
    void shouldFindProductsByNameContaining() {
        productRepository.save(product);

        Page<Product> found = productRepository.findByNameContainingIgnoreCase("iPhone", PageRequest.of(0, 10));

        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getName()).isEqualTo("iPhone 15");
    }

    @Test
    @DisplayName("Should find products by category id")
    void shouldFindProductsByCategoryId() {
        productRepository.save(product);

        Page<Product> found = productRepository.findByCategoryId(category.getId(), PageRequest.of(0, 10));

        assertThat(found.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should find products by filters")
    void shouldFindProductsByFilters() {
        productRepository.save(product);

        Product another = Product.builder()
                .name("Samsung Galaxy")
                .description("Android phone")
                .basePrice(new BigDecimal("899.99"))
                .brand("Samsung")
                .category(category)
                .build();
        productRepository.save(another);

        Page<Product> byName = productRepository.findByFilters("iPhone", null, PageRequest.of(0, 10));
        assertThat(byName.getContent()).hasSize(1);

        Page<Product> byCategory = productRepository.findByFilters(null, category.getId(), PageRequest.of(0, 10));
        assertThat(byCategory.getContent()).hasSize(2);

        Page<Product> byBoth = productRepository.findByFilters("iPhone", category.getId(), PageRequest.of(0, 10));
        assertThat(byBoth.getContent()).hasSize(1);

        Page<Product> all = productRepository.findByFilters(null, null, PageRequest.of(0, 10));
        assertThat(all.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should check if product exists by name and category")
    void shouldCheckIfProductExistsByNameAndCategory() {
        productRepository.save(product);

        boolean exists = productRepository.existsByNameAndCategoryId("iPhone 15", category.getId());
        boolean notExists = productRepository.existsByNameAndCategoryId("NonExistent", category.getId());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if product exists by name and category excluding specific id")
    void shouldCheckIfProductExistsByNameAndCategoryExcludingId() {
        Product saved = productRepository.save(product);

        Product another = Product.builder()
                .name("Samsung Galaxy")
                .description("Android phone")
                .basePrice(new BigDecimal("899.99"))
                .brand("Samsung")
                .category(category)
                .build();
        productRepository.save(another);

        boolean exists = productRepository.existsByNameAndCategoryIdAndIdNot("Samsung Galaxy", category.getId(), saved.getId());
        boolean notExists = productRepository.existsByNameAndCategoryIdAndIdNot("iPhone 15", category.getId(), saved.getId());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}

