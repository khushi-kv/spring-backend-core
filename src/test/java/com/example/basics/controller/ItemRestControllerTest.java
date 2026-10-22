package com.example.basics.controller;

import com.example.basics.config.SecurityConfig;
import com.example.basics.dto.ItemRequestDto;
import com.example.basics.dto.ItemResponseDto;
import com.example.basics.exception.DuplicateResourceException;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.security.JwtAuthenticationEntryPoint;
import com.example.basics.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRestController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class ItemRestControllerTest extends SecuredWebMvcTest {

    private static final String VALID_ITEM = """
            {"name": "Wireless Mouse", "sku": "ELEC-002", "price": 29.99, "quantity": 150, "categoryId": 1}
            """;

    private final ItemResponseDto mouse =
            new ItemResponseDto(2L, "Wireless Mouse", "ELEC-002", new BigDecimal("29.99"), 150, 1L, "Electronics");

    @MockitoBean
    private ItemService itemService;

    @Test
    void anonymousRequestReturns401Json() throws Exception {
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser
    void getAllItemsPassesPagingAndDescendingSortToService() throws Exception {
        when(itemService.getAllItems(any())).thenReturn(new PageImpl<>(List.of(mouse)));

        mockMvc.perform(get("/api/v1/items").param("page", "2").param("size", "5")
                        .param("sortBy", "price").param("sortDir", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("ELEC-002"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(itemService).getAllItems(pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @WithMockUser
    void getItemsByCategoryDefaultsToAscendingSortByName() throws Exception {
        when(itemService.getItemsByCategoryId(eq(1L), any())).thenReturn(new PageImpl<>(List.of(mouse)));

        mockMvc.perform(get("/api/v1/items/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryName").value("Electronics"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(itemService).getItemsByCategoryId(eq(1L), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @WithMockUser
    void getItemByIdReturnsItem() throws Exception {
        when(itemService.getItemById(2L)).thenReturn(mouse);

        mockMvc.perform(get("/api/v1/items/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    @WithMockUser
    void getItemByIdWithUnknownIdReturns404() throws Exception {
        when(itemService.getItemById(99L)).thenThrow(new ResourceNotFoundException("Item not found with ID: 99"));

        mockMvc.perform(get("/api/v1/items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item not found with ID: 99"));
    }

    @Test
    @WithMockUser
    void getItemByIdWithNonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("Parameter 'id' must be of type Long."));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createItemAsStaffReturns201() throws Exception {
        when(itemService.createItem(any(ItemRequestDto.class))).thenReturn(mouse);

        mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON).content(VALID_ITEM))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createItemAsPlainUserReturns403() throws Exception {
        mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON).content(VALID_ITEM))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(itemService, never()).createItem(any());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createItemWithInvalidFieldsReturns400WithFieldDetails() throws Exception {
        String invalid = """
                {"name": "", "sku": "X", "price": -5, "quantity": -1}
                """;

        mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request - Validation Failed"))
                .andExpect(jsonPath("$.details.length()").value(6));
        verify(itemService, never()).createItem(any());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createItemWithMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON).content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON."));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createItemWithWrongContentTypeReturns415() throws Exception {
        mockMvc.perform(post("/api/v1/items").contentType(MediaType.TEXT_PLAIN).content(VALID_ITEM))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createItemWithDuplicateSkuReturns409() throws Exception {
        when(itemService.createItem(any(ItemRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Item with SKU 'ELEC-002' already exists."));

        mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON).content(VALID_ITEM))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Item with SKU 'ELEC-002' already exists."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateItemAsAdminReturns200() throws Exception {
        when(itemService.updateItem(eq(2L), any(ItemRequestDto.class))).thenReturn(mouse);

        mockMvc.perform(put("/api/v1/items/2").contentType(MediaType.APPLICATION_JSON).content(VALID_ITEM))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("ELEC-002"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteItemAsAdminReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/items/2"))
                .andExpect(status().isNoContent());
        verify(itemService).deleteItem(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteItemWithUnknownIdReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Item not found with ID: 99")).when(itemService).deleteItem(99L);

        mockMvc.perform(delete("/api/v1/items/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void deleteItemAsStaffReturns403() throws Exception {
        mockMvc.perform(delete("/api/v1/items/2"))
                .andExpect(status().isForbidden());
        verify(itemService, never()).deleteItem(any());
    }

    @Test
    @WithMockUser
    void unsupportedHttpMethodReturns405() throws Exception {
        mockMvc.perform(patch("/api/v1/items/2"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }
}
