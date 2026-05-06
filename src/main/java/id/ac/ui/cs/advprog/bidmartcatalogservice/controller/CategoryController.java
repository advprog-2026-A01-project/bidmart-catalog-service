package id.ac.ui.cs.advprog.bidmartcatalogservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // GET /api/categories
    // publik
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesAsTree());
    }

    // GET /api/categories/{id}
    // publik
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // POST /api/categories
    // admin
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UUID parentId) {

        CategoryResponse response = categoryService.createCategory(name, description, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE /api/categories/{id}
    // admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
