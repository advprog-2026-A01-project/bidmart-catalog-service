package id.ac.ui.cs.advprog.bidmartcatalogservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.CreateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.UpdateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.ListingResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.CategoryNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.ListingNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.ListingNotEditableException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.ListingRepository;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.impl.ListingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceImplTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ListingServiceImpl listingService;

    private Category category;
    private Listing listing;
    private UUID listingId;
    private UUID categoryId;
    private String sellerId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        listingId = UUID.randomUUID();
        sellerId = "user-123";

        category = Category.builder()
                .id(categoryId)
                .name("Elektronik")
                .build();

        listing = Listing.builder()
                .id(listingId)
                .title("Laptop Gaming")
                .description("Laptop bagus")
                .sellerId(sellerId)
                .sellerUsername("seller1")
                .category(category)
                .startingPrice(new BigDecimal("5000000"))
                .currentPrice(new BigDecimal("5000000"))
                .durationMinutes(60)
                .status(ListingStatus.DRAFT)
                .bidCount(0)
                .build();
    }

    // -------------------------------------------------------------------------
    // createListing
    // -------------------------------------------------------------------------

    @Test
    void createListing_withValidRequest_returnsListingResponse() {
        CreateListingRequest request = CreateListingRequest.builder()
                .title("Laptop Gaming")
                .description("Laptop bagus")
                .categoryId(categoryId)
                .startingPrice(new BigDecimal("5000000"))
                .durationMinutes(60)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        ListingResponse response = listingService.createListing(sellerId, "seller1", request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Laptop Gaming");
        assertThat(response.getSellerId()).isEqualTo(sellerId);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void createListing_withInvalidCategory_throwsCategoryNotFoundException() {
        CreateListingRequest request = CreateListingRequest.builder()
                .title("Laptop Gaming")
                .categoryId(categoryId)
                .startingPrice(new BigDecimal("5000000"))
                .durationMinutes(60)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.createListing(sellerId, "seller1", request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // getListingById
    // -------------------------------------------------------------------------

    @Test
    void getListingById_withExistingId_returnsListingResponse() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        ListingResponse response = listingService.getListingById(listingId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(listingId);
    }

    @Test
    void getListingById_withNonExistingId_throwsListingNotFoundException() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getListingById(listingId))
                .isInstanceOf(ListingNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // searchListings
    // -------------------------------------------------------------------------

    @Test
    void searchListings_returnsPageOfListings() {
        Page<Listing> page = new PageImpl<>(List.of(listing));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<ListingResponse> result = listingService.searchListings(
                null, null, null, null, null, null, null, PageRequest.of(0, 20));

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchListings_withKeyword_passesKeywordToRepository() {
        Page<Listing> page = new PageImpl<>(List.of(listing));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<ListingResponse> result = listingService.searchListings(
                "laptop", null, null, null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // getMyListings
    // -------------------------------------------------------------------------

    @Test
    void getMyListings_returnsPaginatedSellerListings() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Listing> page = new PageImpl<>(List.of(listing));
        when(listingRepository.findBySellerId(sellerId, pageable)).thenReturn(page);

        Page<ListingResponse> result = listingService.getMyListings(sellerId, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getSellerId()).isEqualTo(sellerId);
    }

    @Test
    void getMyListings_withNoListings_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(listingRepository.findBySellerId(sellerId, pageable))
                .thenReturn(Page.empty(pageable));

        Page<ListingResponse> result = listingService.getMyListings(sellerId, pageable);

        assertThat(result).isEmpty();
    }

    @Test
    void getMyListings_withCustomPageSize_respectsPageSize() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Listing> page = new PageImpl<>(List.of(listing), pageable, 1);
        when(listingRepository.findBySellerId(sellerId, pageable)).thenReturn(page);

        Page<ListingResponse> result = listingService.getMyListings(sellerId, pageable);

        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getMyListings_secondPage_returnsCorrectPage() {
        Pageable pageable = PageRequest.of(1, 10);
        when(listingRepository.findBySellerId(sellerId, pageable))
                .thenReturn(Page.empty(pageable));

        Page<ListingResponse> result = listingService.getMyListings(sellerId, pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // updateListing
    // -------------------------------------------------------------------------

    @Test
    void updateListing_withValidRequest_updatesTitle() {
        UpdateListingRequest request = UpdateListingRequest.builder()
                .title("Laptop Gaming Updated")
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.updateListing(listingId, sellerId, request);

        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void updateListing_withListingHasBids_throwsListingNotEditableException() {
        listing.setBidCount(1);
        UpdateListingRequest request = UpdateListingRequest.builder()
                .title("New Title")
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.updateListing(listingId, sellerId, request))
                .isInstanceOf(ListingNotEditableException.class);
    }

    @Test
    void updateListing_withActiveStatus_throwsListingNotEditableException() {
        listing.setStatus(ListingStatus.ACTIVE);
        UpdateListingRequest request = UpdateListingRequest.builder()
                .title("New Title")
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.updateListing(listingId, sellerId, request))
                .isInstanceOf(ListingNotEditableException.class);
    }

    @Test
    void updateListing_byDifferentSeller_throwsListingNotFoundException() {
        UpdateListingRequest request = UpdateListingRequest.builder()
                .title("New Title")
                .build();

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.updateListing(listingId, "other-seller", request))
                .isInstanceOf(ListingNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // cancelListing
    // -------------------------------------------------------------------------

    @Test
    void cancelListing_withDraftListing_setsStatusToCancelled() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.cancelListing(listingId, sellerId);

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.CANCELLED);
        verify(listingRepository).save(listing);
    }

    @Test
    void cancelListing_withActiveListing_throwsListingNotEditableException() {
        listing.setStatus(ListingStatus.ACTIVE);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.cancelListing(listingId, sellerId))
                .isInstanceOf(ListingNotEditableException.class);
    }

    @Test
    void cancelListing_byDifferentSeller_throwsListingNotFoundException() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.cancelListing(listingId, "other-seller"))
                .isInstanceOf(ListingNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // isListingBiddable
    // -------------------------------------------------------------------------

    @Test
    void isListingBiddable_withActiveListing_returnsTrue() {
        when(listingRepository.existsByIdAndStatusIn(eq(listingId), any()))
                .thenReturn(true);

        assertThat(listingService.isListingBiddable(listingId)).isTrue();
    }

    @Test
    void isListingBiddable_withDraftListing_returnsFalse() {
        when(listingRepository.existsByIdAndStatusIn(eq(listingId), any()))
                .thenReturn(false);

        assertThat(listingService.isListingBiddable(listingId)).isFalse();
    }

    // -------------------------------------------------------------------------
    // updateListingPriceAndBidCount
    // -------------------------------------------------------------------------

    @Test
    void updateListingPriceAndBidCount_withExistingListing_updatesPriceAndCount() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.updateListingPriceAndBidCount(listingId, new BigDecimal("6000000"), 3);

        assertThat(listing.getCurrentPrice()).isEqualByComparingTo("6000000");
        assertThat(listing.getBidCount()).isEqualTo(3);
    }

    @Test
    void updateListingPriceAndBidCount_withNonExistingListing_doesNothing() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        listingService.updateListingPriceAndBidCount(listingId, new BigDecimal("6000000"), 3);

        verify(listingRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateListingStatus
    // -------------------------------------------------------------------------

    @Test
    void updateListingStatus_withExistingListing_updatesStatus() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.updateListingStatus(listingId, ListingStatus.WON, new BigDecimal("7000000"));

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.WON);
        assertThat(listing.getCurrentPrice()).isEqualByComparingTo("7000000");
    }

    @Test
    void updateListingStatus_withNullFinalPrice_doesNotUpdatePrice() {
        BigDecimal originalPrice = listing.getCurrentPrice();
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.updateListingStatus(listingId, ListingStatus.UNSOLD, null);

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.UNSOLD);
        assertThat(listing.getCurrentPrice()).isEqualByComparingTo(originalPrice);
    }

    // -------------------------------------------------------------------------
    // publishListing
    // -------------------------------------------------------------------------

    @Test
    void publishListing_withDraftListing_transitionsToActive() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.publishListing(listingId, sellerId);

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(listing.getStartTime()).isNotNull();
        assertThat(listing.getEndTime()).isNotNull();
        assertThat(listing.getEndTime()).isAfter(listing.getStartTime());
        verify(listingRepository).save(listing);
    }

    @Test
    void publishListing_endTimeIsStartTimePlusDuration() {
        listing.setDurationMinutes(60);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        listingService.publishListing(listingId, sellerId);

        long diffMinutes = java.time.Duration.between(
                listing.getStartTime(), listing.getEndTime()).toMinutes();
        assertThat(diffMinutes).isEqualTo(60);
    }

    @Test
    void publishListing_withAlreadyActiveListing_throwsListingNotEditableException() {
        listing.setStatus(ListingStatus.ACTIVE);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.publishListing(listingId, sellerId))
                .isInstanceOf(ListingNotEditableException.class);
    }

    @Test
    void publishListing_byDifferentSeller_throwsListingNotFoundException() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.publishListing(listingId, "other-seller"))
                .isInstanceOf(ListingNotFoundException.class);
    }

    @Test
    void publishListing_withNonExistingId_throwsListingNotFoundException() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.publishListing(listingId, sellerId))
                .isInstanceOf(ListingNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // closeExpiredListings
    // -------------------------------------------------------------------------

    @Test
    void closeExpiredListings_withExpiredActiveListings_closesThemAll() {
        Listing expiredListing = Listing.builder()
                .id(UUID.randomUUID())
                .title("Expired Auction")
                .sellerId(sellerId)
                .sellerUsername("seller1")
                .category(category)
                .startingPrice(new BigDecimal("1000000"))
                .currentPrice(new BigDecimal("1000000"))
                .reservePrice(new BigDecimal("500000"))
                .durationMinutes(60)
                .status(ListingStatus.ACTIVE)
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .bidCount(0)
                .build();

        when(listingRepository.findExpiredListings(any(), any()))
                .thenReturn(List.of(expiredListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(expiredListing);

        listingService.closeExpiredListings();

        assertThat(expiredListing.getStatus()).isEqualTo(ListingStatus.UNSOLD);
        verify(listingRepository).save(expiredListing);
    }

    @Test
    void closeExpiredListings_reservePriceMet_setsStatusToWon() {
        Listing expiredListing = Listing.builder()
                .id(UUID.randomUUID())
                .title("Expired Auction with Bids")
                .sellerId(sellerId)
                .sellerUsername("seller1")
                .category(category)
                .startingPrice(new BigDecimal("1000000"))
                .currentPrice(new BigDecimal("2000000"))
                .reservePrice(new BigDecimal("1500000"))
                .durationMinutes(60)
                .status(ListingStatus.ACTIVE)
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .bidCount(3)
                .build();

        when(listingRepository.findExpiredListings(any(), any()))
                .thenReturn(List.of(expiredListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(expiredListing);

        listingService.closeExpiredListings();

        assertThat(expiredListing.getStatus()).isEqualTo(ListingStatus.WON);
        verify(listingRepository).save(expiredListing);
    }

    @Test
    void closeExpiredListings_reservePriceNotMet_setsStatusToUnsold() {
        Listing expiredListing = Listing.builder()
                .id(UUID.randomUUID())
                .title("Expired Auction Reserve Not Met")
                .sellerId(sellerId)
                .sellerUsername("seller1")
                .category(category)
                .startingPrice(new BigDecimal("1000000"))
                .currentPrice(new BigDecimal("1200000"))
                .reservePrice(new BigDecimal("2000000"))
                .durationMinutes(60)
                .status(ListingStatus.ACTIVE)
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .bidCount(2)
                .build();

        when(listingRepository.findExpiredListings(any(), any()))
                .thenReturn(List.of(expiredListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(expiredListing);

        listingService.closeExpiredListings();

        assertThat(expiredListing.getStatus()).isEqualTo(ListingStatus.UNSOLD);
        verify(listingRepository).save(expiredListing);
    }

    @Test
    void closeExpiredListings_noReservePrice_withBids_setsStatusToWon() {
        Listing expiredListing = Listing.builder()
                .id(UUID.randomUUID())
                .title("Expired No Reserve")
                .sellerId(sellerId)
                .sellerUsername("seller1")
                .category(category)
                .startingPrice(new BigDecimal("1000000"))
                .currentPrice(new BigDecimal("1500000"))
                .reservePrice(null)
                .durationMinutes(60)
                .status(ListingStatus.ACTIVE)
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .bidCount(1)
                .build();

        when(listingRepository.findExpiredListings(any(), any()))
                .thenReturn(List.of(expiredListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(expiredListing);

        listingService.closeExpiredListings();

        assertThat(expiredListing.getStatus()).isEqualTo(ListingStatus.WON);
        verify(listingRepository).save(expiredListing);
    }

    @Test
    void closeExpiredListings_withNoExpiredListings_doesNothing() {
        when(listingRepository.findExpiredListings(any(), any()))
                .thenReturn(List.of());

        listingService.closeExpiredListings();

        verify(listingRepository, never()).save(any());
    }
}