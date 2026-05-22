package id.ac.ui.cs.advprog.bidmartcatalogservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.CategoryNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private CategoryResponse sampleCategory;
    private UUID categoryId;
    private final String gatewaySecret = "test-secret";

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        sampleCategory = CategoryResponse.builder()
                .id(categoryId)
                .name("Elektronik")
                .description("Kategori elektronik")
                .children(new ArrayList<>())
                .build();
    }

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------

    @Test
    void getAllCategories_returnsOk() throws Exception {
        when(categoryService.getAllCategoriesAsTree()).thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/categories")
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Elektronik"));
    }

    @Test
    void getAllCategories_withNoCategories_returnsEmptyList() throws Exception {
        when(categoryService.getAllCategoriesAsTree()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories")
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------

    @Test
    void getCategoryById_withExistingId_returnsOk() throws Exception {
        when(categoryService.getCategoryById(categoryId)).thenReturn(sampleCategory);

        mockMvc.perform(get("/api/categories/" + categoryId)
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Elektronik"));
    }

    @Test
    void getCategoryById_withNonExistingId_returns404() throws Exception {
        when(categoryService.getCategoryById(categoryId))
                .thenThrow(new CategoryNotFoundException(categoryId.toString()));

        mockMvc.perform(get("/api/categories/" + categoryId)
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST
    // -------------------------------------------------------------------------

    @Test
    void createCategory_withValidParams_returnsCreated() throws Exception {
        when(categoryService.createCategory(eq("Elektronik"), any(), any()))
                .thenReturn(sampleCategory);

        mockMvc.perform(post("/api/categories")
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Role", "ADMIN")
                        .param("name", "Elektronik"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Elektronik"));
    }

    @Test
    void createCategory_withParentId_returnsCreated() throws Exception {
        UUID parentId = UUID.randomUUID();
        CategoryResponse child = CategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Handphone")
                .parentId(parentId)
                .children(new ArrayList<>())
                .build();

        when(categoryService.createCategory(eq("Handphone"), any(), eq(parentId)))
                .thenReturn(child);

        mockMvc.perform(post("/api/categories")
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Role", "ADMIN")
                        .param("name", "Handphone")
                        .param("parentId", parentId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentId").value(parentId.toString()));
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Test
    void deleteCategory_withExistingId_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_withNonExistingId_returns404() throws Exception {
        doThrow(new CategoryNotFoundException(categoryId.toString()))
                .when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_withNonAdminRole_returns403() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Role", "SELLER")
                        .param("name", "Elektronik"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_withMissingRoleHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("X-Gateway-Secret", gatewaySecret)
                        .param("name", "Elektronik"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_withNonAdminRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Role", "BUYER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_withMissingRoleHeader_returns400() throws Exception {
        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isBadRequest());
    }
}
