package id.ac.ui.cs.advprog.bidmartcatalogservice.service.impl;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.CreateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.UpdateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.ListingResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.CategoryNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.ListingNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.ListingNotEditableException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingImage;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.ListingRepository;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.ListingSpecification;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ListingResponse createListing(String sellerId, String sellerUsername, CreateListingRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId().toString()));

        Listing listing = Listing.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .sellerId(sellerId)
                .sellerUsername(sellerUsername)
                .category(category)
                .startingPrice(request.getStartingPrice())
                .reservePrice(request.getReservePrice())
                .currentPrice(request.getStartingPrice())
                .durationMinutes(request.getDurationMinutes())
                .status(ListingStatus.DRAFT)
                .build();

        List<ListingImage> images = buildImages(request.getImageUrls(), listing);
        listing.setImages(images);

        return ListingResponse.from(listingRepository.save(listing));
    }

    @Override
    @Transactional(readOnly = true)
    public ListingResponse getListingById(UUID id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id.toString()));
        return ListingResponse.from(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingResponse> searchListings(
            String keyword, UUID categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Instant endsBefore, Instant endsAfter,
            List<ListingStatus> statuses, Pageable pageable) {

        List<ListingStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED)
                : statuses;

        return listingRepository
                .findAll(ListingSpecification.build(
                        keyword, categoryId, minPrice, maxPrice,
                        endsBefore, endsAfter, effectiveStatuses), pageable)
                .map(ListingResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingResponse> getMyListings(String sellerId, Pageable pageable) {
        return listingRepository.findBySellerId(sellerId, pageable)
                .map(ListingResponse::from);
    }

    @Override
    @Transactional
    public ListingResponse updateListing(UUID id, String sellerId, UpdateListingRequest request) {
        Listing listing = findListingOwnedBy(id, sellerId);

        if (!listing.isEditable()) {
            throw new ListingNotEditableException(id.toString());
        }

        if (request.getTitle() != null) {
            listing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            listing.setDescription(request.getDescription());
        }
        if (request.getImageUrls() != null) {
            listing.getImages().clear();
            listing.getImages().addAll(buildImages(request.getImageUrls(), listing));
        }

        listing.setUpdatedAt(Instant.now());
        return ListingResponse.from(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public void cancelListing(UUID id, String sellerId) {
        Listing listing = findListingOwnedBy(id, sellerId);

        if (!listing.isEditable()) {
            throw new ListingNotEditableException(id.toString());
        }

        listing.setStatus(ListingStatus.CANCELLED);
        listing.setUpdatedAt(Instant.now());
        listingRepository.save(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isListingBiddable(UUID id) {
        return listingRepository.existsByIdAndStatusIn(
                id, List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED));
    }

    @Override
    @Transactional
    public void updateListingPriceAndBidCount(UUID listingId, BigDecimal newPrice, int bidCount) {
        listingRepository.findById(listingId).ifPresent(listing -> {
            listing.setCurrentPrice(newPrice);
            listing.setBidCount(bidCount);
            listing.setUpdatedAt(Instant.now());
            listingRepository.save(listing);
        });
    }

    @Override
    @Transactional
    public void updateListingStatus(UUID listingId, ListingStatus newStatus, BigDecimal finalPrice) {
        listingRepository.findById(listingId).ifPresent(listing -> {
            listing.setStatus(newStatus);
            if (finalPrice != null) {
                listing.setCurrentPrice(finalPrice);
            }
            listing.setUpdatedAt(Instant.now());
            listingRepository.save(listing);
        });
    }

    @Override
    @Transactional
    public ListingResponse publishListing(UUID id, String sellerId) {
        Listing listing = findListingOwnedBy(id, sellerId);

        if (listing.getStatus() != ListingStatus.DRAFT) {
            throw new ListingNotEditableException(id.toString());
        }

        Instant now = Instant.now();
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setStartTime(now);
        listing.setEndTime(now.plus(listing.getDurationMinutes(), ChronoUnit.MINUTES));
        listing.setUpdatedAt(now);

        return ListingResponse.from(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public void closeExpiredListings() {
        List<Listing> expiredListings = listingRepository.findExpiredListings(
                List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED),
                Instant.now()
        );

        for (Listing listing : expiredListings) {
            ListingStatus finalStatus = determineClosingStatus(listing);
            listing.setStatus(finalStatus);
            listing.setUpdatedAt(Instant.now());
            listingRepository.save(listing);
            log.info("Closed listing {} with status {}", listing.getId(), finalStatus);
        }
    }

    // helpers

    private Listing findListingOwnedBy(UUID id, String sellerId) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id.toString()));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new ListingNotFoundException(id.toString());
        }

        return listing;
    }

    private List<ListingImage> buildImages(List<String> imageUrls, Listing listing) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return new ArrayList<>();
        }
        List<ListingImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(ListingImage.builder()
                    .imageUrl(imageUrls.get(i))
                    .displayOrder(i)
                    .listing(listing)
                    .build());
        }
        return images;
    }

    private ListingStatus determineClosingStatus(Listing listing) {
        if (listing.getBidCount() == 0) {
            return ListingStatus.UNSOLD;
        }
        if (listing.getReservePrice() == null) {
            return ListingStatus.WON;
        }
        return listing.getCurrentPrice().compareTo(listing.getReservePrice()) >= 0
                ? ListingStatus.WON
                : ListingStatus.UNSOLD;
    }
}