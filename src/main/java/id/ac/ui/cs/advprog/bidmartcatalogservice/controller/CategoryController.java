package id.ac.ui.cs.advprog.bidmartcatalogservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.CategoryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.ForbiddenException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UUID parentId) {

        if (!"ADMIN".equals(userRole)) {
            throw new ForbiddenException("create category");
        }

        CategoryResponse response = categoryService.createCategory(name, description, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE /api/categories/{id}
    // admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"ADMIN".equals(userRole)) {
            throw new ForbiddenException("delete category");
        }

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
