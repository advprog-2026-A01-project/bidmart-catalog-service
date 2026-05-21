package id.ac.ui.cs.advprog.bidmartcatalogservice.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String action) {
        super("Access denied: insufficient role to perform action: " + action);
    }
}