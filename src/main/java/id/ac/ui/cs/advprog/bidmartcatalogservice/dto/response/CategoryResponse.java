package id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID parentId;

    // untuk response tree lengkap (GET /api/categories)
    private List<CategoryResponse> children;

    // shallow: tanpa children, untuk ditempel di ListingResponse
    public static CategoryResponse fromShallow(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }

    // deep: dengan children, untuk GET /api/categories tree
    public static CategoryResponse fromDeep(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(category.getChildren().stream()
                        .map(CategoryResponse::fromDeep)
                        .toList())
                .build();
    }
}
