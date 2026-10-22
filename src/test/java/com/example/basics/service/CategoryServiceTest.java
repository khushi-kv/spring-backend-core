package com.example.basics.service;

import com.example.basics.dto.CategoryRequestDto;
import com.example.basics.dto.CategoryResponseDto;
import com.example.basics.exception.DuplicateResourceException;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.model.Category;
import com.example.basics.model.Item;
import com.example.basics.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategorySavesNewCategoryWithZeroItems() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CategoryResponseDto response = categoryService.createCategory(new CategoryRequestDto("Electronics", "Gadgets"));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
        assertThat(response.getDescription()).isEqualTo("Gadgets");
        assertThat(response.getItemCount()).isZero();
    }

    @Test
    void createCategoryWithDuplicateNameThrowsAndDoesNotSave() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);
        CategoryRequestDto request = new CategoryRequestDto("Electronics", null);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Electronics");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getAllCategoriesCountsItemsOfEachCategory() {
        Category electronics = new Category(1L, "Electronics", null);
        electronics.addItem(new Item(1L, "Laptop", "ELEC-001", BigDecimal.TEN, 1, electronics));
        electronics.addItem(new Item(2L, "Mouse", "ELEC-002", BigDecimal.ONE, 1, electronics));
        Category empty = new Category(2L, "Furniture", null);
        empty.setItems(null);
        when(categoryRepository.findAll()).thenReturn(List.of(electronics, empty));

        List<CategoryResponseDto> categories = categoryService.getAllCategories();

        assertThat(categories).extracting(CategoryResponseDto::getItemCount).containsExactly(2, 0);
    }

    @Test
    void getCategoryByIdReturnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category(1L, "Electronics", null)));

        assertThat(categoryService.getCategoryById(1L).getName()).isEqualTo("Electronics");
    }

    @Test
    void getCategoryByIdWithUnknownIdThrowsNotFound() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with ID: 9");
    }
}
