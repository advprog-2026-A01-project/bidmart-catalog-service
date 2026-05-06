package id.ac.ui.cs.advprog.bidmartcatalogservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponse> getAllCategoriesAsTree();

    CategoryResponse getCategoryById(UUID id);

    // admin
    CategoryResponse createCategory(String name, String description, UUID parentId);

    // admin (hanya kalau tidak ada listing di dalamnya)
    void deleteCategory(UUID id);
}
