package id.ac.ui.cs.advprog.bidmartcatalogservice.exception;

public class ListingNotEditableException extends RuntimeException {
    public ListingNotEditableException(String id) {
        super("Listing cannot be edited because it already has bids or is not in DRAFT status: " + id);
    }
}
