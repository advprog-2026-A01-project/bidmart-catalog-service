package id.ac.ui.cs.advprog.bidmartcatalogservice.service.impl;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.CategoryNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesAsTree() {
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryResponse::fromDeep)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        return CategoryResponse.fromDeep(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(String name, String description, UUID parentId) {
        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new CategoryNotFoundException(parentId.toString()));
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .parent(parent)
                .build();

        return CategoryResponse.fromShallow(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        categoryRepository.delete(category);
    }
}
