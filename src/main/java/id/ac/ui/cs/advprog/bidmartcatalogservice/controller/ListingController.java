package id.ac.ui.cs.advprog.bidmartcatalogservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.CreateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.UpdateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.ListingResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.ForbiddenException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // GET /api/listings?keyword=&categoryId=&minPrice=&maxPrice=&page=&size=
    // public
    @GetMapping
    public ResponseEntity<Page<ListingResponse>> searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Instant endsBefore,
            @RequestParam(required = false) Instant endsAfter,
            @RequestParam(required = false) List<ListingStatus> statuses,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                listingService.searchListings(keyword, categoryId, minPrice, maxPrice,
                        endsBefore, endsAfter, statuses, pageable));
    }

    // GET /api/listings/{id}
    // public
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable UUID id) {
        return ResponseEntity.ok(listingService.getListingById(id));
    }

    // GET /api/listings/my?page=&size=
    // seller
    @GetMapping("/my")
    public ResponseEntity<Page<ListingResponse>> getMyListings(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        if (!"SELLER".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new ForbiddenException("view own listings");
        }
        return ResponseEntity.ok(listingService.getMyListings(userId, pageable));
    }

    // POST /api/listings
    // seller
    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody CreateListingRequest request) {

        if (!"SELLER".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new ForbiddenException("create listing");
        }
        ListingResponse response = listingService.createListing(userId, username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/listings/{id}
    // seller (hanya saat DRAFT dan belum ada bid)
    @PutMapping("/{id}")
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody UpdateListingRequest request) {

        if (!"SELLER".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new ForbiddenException("update listing");
        }
        return ResponseEntity.ok(listingService.updateListing(id, userId, request));
    }

    // DELETE /api/listings/{id}
    // seller (hanya saat DRAFT dan belum ada bid)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelListing(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"SELLER".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new ForbiddenException("cancel listing");
        }
        listingService.cancelListing(id, userId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/listings/{id}/publish
    // seller: aktifkan listing dari DRAFT -> ACTIVE
    @PostMapping("/{id}/publish")
    public ResponseEntity<ListingResponse> publishListing(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {

        if (!"SELLER".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new ForbiddenException("publish listing");
        }
        return ResponseEntity.ok(listingService.publishListing(id, userId));
    }
}