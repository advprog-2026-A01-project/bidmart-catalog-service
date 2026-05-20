package id.ac.ui.cs.advprog.bidmartcatalogservice.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String id) {
        super("Category not found: " + id);
    }
}
