package id.ac.ui.cs.advprog.bidmartcatalogservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.CreateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.UpdateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.ListingResponse;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // GET /api/listings?keyword=&categoryId=&minPrice=&maxPrice=&page=&size=
    // (public) buyer browse listing aktif
    @GetMapping
    public ResponseEntity<Page<ListingResponse>> searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<ListingStatus> statuses,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                listingService.searchListings(keyword, categoryId, minPrice, maxPrice, statuses, pageable));
    }

    // GET /api/listings/{id}
    // publik - lihat detail listing
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable UUID id) {
        return ResponseEntity.ok(listingService.getListingById(id));
    }

    // GET /api/listings/my
    // seller - lihat semua listing miliknya sendiri
    @GetMapping("/my")
    public ResponseEntity<List<ListingResponse>> getMyListings(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(listingService.getMyListings(userId));
    }

    // POST /api/listings
    // seller - buat listing baru
    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody CreateListingRequest request) {

        ListingResponse response = listingService.createListing(userId, username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/listings/{id}
    // seller - update listing miliknya (hanya saat DRAFT dan belum ada bid)
    @PutMapping("/{id}")
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateListingRequest request) {

        return ResponseEntity.ok(listingService.updateListing(id, userId, request));
    }

    // DELETE /api/listings/{id}
    // seller - cancel listing miliknya (hanya saat DRAFT dan belum ada bid)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelListing(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId) {

        listingService.cancelListing(id, userId);
        return ResponseEntity.noContent().build();
    }
}
