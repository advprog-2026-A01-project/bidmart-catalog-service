package id.ac.ui.cs.advprog.bidmartcatalogservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.CategoryNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category rootCategory;
    private Category childCategory;
    private UUID rootId;
    private UUID childId;

    @BeforeEach
    void setUp() {
        rootId = UUID.randomUUID();
        childId = UUID.randomUUID();

        rootCategory = Category.builder()
                .id(rootId)
                .name("Elektronik")
                .description("Kategori elektronik")
                .children(new ArrayList<>())
                .build();

        childCategory = Category.builder()
                .id(childId)
                .name("Handphone")
                .parent(rootCategory)
                .children(new ArrayList<>())
                .build();

        rootCategory.getChildren().add(childCategory);
    }

    // -------------------------------------------------------------------------
    // getAllCategoriesAsTree
    // -------------------------------------------------------------------------

    @Test
    void getAllCategoriesAsTree_returnsRootCategoriesWithChildren() {
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(rootCategory));

        List<CategoryResponse> result = categoryService.getAllCategoriesAsTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Elektronik");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Handphone");
    }

    @Test
    void getAllCategoriesAsTree_withNoCategories_returnsEmptyList() {
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategoriesAsTree();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getCategoryById
    // -------------------------------------------------------------------------

    @Test
    void getCategoryById_withExistingId_returnsCategoryWithChildren() {
        when(categoryRepository.findById(rootId)).thenReturn(Optional.of(rootCategory));

        CategoryResponse result = categoryService.getCategoryById(rootId);

        assertThat(result.getId()).isEqualTo(rootId);
        assertThat(result.getName()).isEqualTo("Elektronik");
        assertThat(result.getChildren()).hasSize(1);
    }

    @Test
    void getCategoryById_withNonExistingId_throwsCategoryNotFoundException() {
        when(categoryRepository.findById(rootId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(rootId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // createCategory
    // -------------------------------------------------------------------------

    @Test
    void createCategory_withoutParent_createsRootCategory() {
        Category saved = Category.builder()
                .id(UUID.randomUUID())
                .name("Fashion")
                .children(new ArrayList<>())
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.createCategory("Fashion", null, null);

        assertThat(result.getName()).isEqualTo("Fashion");
        assertThat(result.getParentId()).isNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_withValidParent_createsChildCategory() {
        Category saved = Category.builder()
                .id(UUID.randomUUID())
                .name("Handphone")
                .parent(rootCategory)
                .children(new ArrayList<>())
                .build();

        when(categoryRepository.findById(rootId)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.createCategory("Handphone", null, rootId);

        assertThat(result.getName()).isEqualTo("Handphone");
        assertThat(result.getParentId()).isEqualTo(rootId);
    }

    @Test
    void createCategory_withInvalidParentId_throwsCategoryNotFoundException() {
        UUID invalidParentId = UUID.randomUUID();
        when(categoryRepository.findById(invalidParentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory("Test", null, invalidParentId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // deleteCategory
    // -------------------------------------------------------------------------

    @Test
    void deleteCategory_withExistingId_deletesCategory() {
        when(categoryRepository.findById(rootId)).thenReturn(Optional.of(rootCategory));

        categoryService.deleteCategory(rootId);

        verify(categoryRepository).delete(rootCategory);
    }

    @Test
    void deleteCategory_withNonExistingId_throwsCategoryNotFoundException() {
        when(categoryRepository.findById(rootId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(rootId))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
