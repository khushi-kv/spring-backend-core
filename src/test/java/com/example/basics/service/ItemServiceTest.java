package com.example.basics.service;

import com.example.basics.dto.ItemRequestDto;
import com.example.basics.dto.ItemResponseDto;
import com.example.basics.exception.DuplicateResourceException;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.model.Category;
import com.example.basics.model.Item;
import com.example.basics.repository.CategoryRepository;
import com.example.basics.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ItemService itemService;

    private Category electronics;
    private ItemRequestDto request;

    @BeforeEach
    void setUp() {
        electronics = new Category(1L, "Electronics", "Gadgets");
        request = new ItemRequestDto("Wireless Mouse", "ELEC-002", new BigDecimal("29.99"), 150, 1L);
    }

    @Test
    void createItemSavesItemLinkedToCategory() {
        when(itemRepository.existsBySku("ELEC-002")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ItemResponseDto response = itemService.createItem(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getSku()).isEqualTo("ELEC-002");
        assertThat(response.getPrice()).isEqualByComparingTo("29.99");
        assertThat(response.getQuantity()).isEqualTo(150);
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void createItemWithDuplicateSkuThrowsAndDoesNotSave() {
        when(itemRepository.existsBySku("ELEC-002")).thenReturn(true);

        assertThatThrownBy(() -> itemService.createItem(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ELEC-002");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItemWithUnknownCategoryThrowsNotFound() {
        when(itemRepository.existsBySku("ELEC-002")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with ID: 1");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getAllItemsMapsEachItemOfThePage() {
        Pageable pageable = PageRequest.of(0, 10);
        Item item = new Item(5L, "Standing Desk", "FURN-001", new BigDecimal("599.99"), 40, electronics);
        when(itemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(item), pageable, 1));

        Page<ItemResponseDto> page = itemService.getAllItems(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Standing Desk");
    }

    @Test
    void getItemsByCategoryIdReturnsItemsOfExistingCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Item item = new Item(5L, "Laptop", "ELEC-001", new BigDecimal("2499.99"), 25, electronics);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByCategoryId(1L, pageable)).thenReturn(new PageImpl<>(List.of(item), pageable, 1));

        Page<ItemResponseDto> page = itemService.getItemsByCategoryId(1L, pageable);

        assertThat(page.getContent()).extracting(ItemResponseDto::getSku).containsExactly("ELEC-001");
    }

    @Test
    void getItemsByCategoryIdWithUnknownCategoryThrowsNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getItemsByCategoryId(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with ID: 99");
    }

    @Test
    void getItemByIdReturnsItem() {
        Item item = new Item(5L, "Laptop", "ELEC-001", new BigDecimal("2499.99"), 25, electronics);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThat(itemService.getItemById(5L).getName()).isEqualTo("Laptop");
    }

    @Test
    void getItemByIdWithUnknownIdThrowsNotFound() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found with ID: 5");
    }

    @Test
    void updateItemChangesFieldsButKeepsSku() {
        Category furniture = new Category(2L, "Furniture", null);
        Item existing = new Item(5L, "Old name", "ELEC-001", BigDecimal.ONE, 1, electronics);
        ItemRequestDto update = new ItemRequestDto("New name", "IGNORED-SKU", new BigDecimal("10.00"), 7, 2L);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(furniture));
        when(itemRepository.save(existing)).thenReturn(existing);

        ItemResponseDto response = itemService.updateItem(5L, update);

        assertThat(response.getName()).isEqualTo("New name");
        assertThat(response.getSku()).isEqualTo("ELEC-001");
        assertThat(response.getQuantity()).isEqualTo(7);
        assertThat(response.getCategoryName()).isEqualTo("Furniture");
    }

    @Test
    void updateItemWithUnknownItemThrowsNotFound() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(5L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found with ID: 5");
    }

    @Test
    void updateItemWithUnknownCategoryThrowsNotFound() {
        Item existing = new Item(5L, "Laptop", "ELEC-001", BigDecimal.ONE, 1, electronics);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(5L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with ID: 1");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void deleteItemDeletesExistingItem() {
        when(itemRepository.existsById(5L)).thenReturn(true);

        itemService.deleteItem(5L);

        verify(itemRepository).deleteById(5L);
    }

    @Test
    void deleteItemWithUnknownIdThrowsNotFound() {
        when(itemRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> itemService.deleteItem(5L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(itemRepository, never()).deleteById(any());
    }
}
