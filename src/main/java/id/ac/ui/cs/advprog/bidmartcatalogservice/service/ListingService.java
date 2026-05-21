package id.ac.ui.cs.advprog.bidmartcatalogservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.CreateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.UpdateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.ListingResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListingService {

    // seller
    ListingResponse createListing(String sellerId, String sellerUsername, CreateListingRequest request);

    // buyer / publik
    ListingResponse getListingById(UUID id);

    // buyer / publik
    Page<ListingResponse> searchListings(
            String keyword,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Instant endsBefore,
            Instant endsAfter,
            List<ListingStatus> statuses,
            Pageable pageable
    );

    // seller
    List<ListingResponse> getMyListings(String sellerId);

    // seller (hanya saat DRAFT dan belum ada bid)
    ListingResponse updateListing(UUID id, String sellerId, UpdateListingRequest request);

    // seller (hanya saat DRAFT dan belum ada bid)
    void cancelListing(UUID id, String sellerId);

    // internal (dipanggil oleh auction-service via REST)
    boolean isListingBiddable(UUID id);

    // internal (dipanggil oleh RabbitMQ consumer saat ada bid masuk)
    void updateListingPriceAndBidCount(UUID listingId, BigDecimal newPrice, int bidCount);

    // internal (dipanggil oleh RabbitMQ consumer saat lelang selesai)
    void updateListingStatus(UUID listingId, ListingStatus newStatus, BigDecimal finalPrice);

    // DRAFT to ACTIVE transition
    ListingResponse publishListing(UUID id, String sellerId);
}
