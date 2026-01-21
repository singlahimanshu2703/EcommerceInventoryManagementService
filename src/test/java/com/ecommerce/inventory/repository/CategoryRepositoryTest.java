package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        
        category = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
    }

    @Test
    @DisplayName("Should save category successfully")
    void shouldSaveCategorySuccessfully() {
        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should find category by name")
    void shouldFindCategoryByName() {
        categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findByName("Electronics");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should return empty when category not found by name")
    void shouldReturnEmptyWhenCategoryNotFoundByName() {
        Optional<Category> found = categoryRepository.findByName("NonExistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if category exists by name")
    void shouldCheckIfCategoryExistsByName() {
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsByName("Electronics");
        boolean notExists = categoryRepository.existsByName("NonExistent");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if category exists by name excluding specific id")
    void shouldCheckIfCategoryExistsByNameExcludingId() {
        Category saved = categoryRepository.save(category);

        Category another = Category.builder()
                .name("Apparel")
                .description("Clothing items")
                .build();
        categoryRepository.save(another);

        boolean exists = categoryRepository.existsByNameAndIdNot("Apparel", saved.getId());
        boolean notExists = categoryRepository.existsByNameAndIdNot("Electronics", saved.getId());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find all categories")
    void shouldFindAllCategories() {
        categoryRepository.save(category);

        Category another = Category.builder()
                .name("Apparel")
                .description("Clothing items")
                .build();
        categoryRepository.save(another);

        assertThat(categoryRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Should delete category")
    void shouldDeleteCategory() {
        Category saved = categoryRepository.save(category);
        categoryRepository.delete(saved);

        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }
}

