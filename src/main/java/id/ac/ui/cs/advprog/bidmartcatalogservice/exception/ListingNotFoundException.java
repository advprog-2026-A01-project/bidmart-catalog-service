package id.ac.ui.cs.advprog.bidmartcatalogservice.exception;

public class ListingNotFoundException extends RuntimeException {
    public ListingNotFoundException(String id) {
        super("Listing not found: " + id);
    }
}
