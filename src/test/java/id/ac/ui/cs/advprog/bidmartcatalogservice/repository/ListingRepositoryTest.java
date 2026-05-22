package id.ac.ui.cs.advprog.bidmartcatalogservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:listingrepotest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ListingRepositoryTest {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Listing activeListing;
    private Listing draftListing;
    private Listing expiredListing;
    private final String sellerId = "user-123";

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Elektronik")
                .build());

        activeListing = listingRepository.save(Listing.builder()
                .title("Laptop Gaming")
                .description("Laptop bagus")
                .sellerId(sellerId)
                .sellerUsername("seller1")
                .category(category)
                .startingPrice(new BigDecimal("5000000"))
                .currentPrice(new BigDecimal("5500000"))
                .durationMinutes(60)
                .status(ListingStatus.ACTIVE)
                .startTime(Instant.now().minus(30, ChronoUnit.MINUTES))
                .endTime(Instant.now().plus(30, ChronoUnit.MINUTES))
                .bidCount(2)
                .build());

        draftListing = listingRepository.save(Listing.builder()
                .title("Kamera DSLR")
                .sellerId(sellerId)
                .sellerUsername("seller1")
                .category(category)
                .startingPrice(new BigDecimal("3000000"))
                .currentPrice(new BigDecimal("3000000"))
                .durationMinutes(120)
                .status(ListingStatus.DRAFT)
                .bidCount(0)
                .build());

        expiredListing = listingRepository.save(Listing.builder()
                .title("HP Bekas")
                .sellerId("other-seller")
                .sellerUsername("seller2")
                .category(category)
                .startingPrice(new BigDecimal("1000000"))
                .currentPrice(new BigDecimal("1200000"))
                .durationMinutes(60)
                .status(ListingStatus.ACTIVE)
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .bidCount(1)
                .build());
    }

    // -------------------------------------------------------------------------
    // findBySellerId
    // -------------------------------------------------------------------------

    @Test
    void findBySellerId_returnsOnlySellerListings() {
        Page<Listing> result = listingRepository.findBySellerId(sellerId, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(l -> l.getSellerId().equals(sellerId));
    }

    @Test
    void findBySellerId_withUnknownSeller_returnsEmpty() {
        Page<Listing> result = listingRepository.findBySellerId("unknown", PageRequest.of(0, 20));

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // existsByIdAndStatusIn
    // -------------------------------------------------------------------------

    @Test
    void existsByIdAndStatusIn_whenStatusMatches_returnsTrue() {
        boolean exists = listingRepository.existsByIdAndStatusIn(
                activeListing.getId(),
                List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED));

        assertThat(exists).isTrue();
    }

    @Test
    void existsByIdAndStatusIn_whenStatusDoesNotMatch_returnsFalse() {
        boolean exists = listingRepository.existsByIdAndStatusIn(
                draftListing.getId(),
                List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED));

        assertThat(exists).isFalse();
    }

    // -------------------------------------------------------------------------
    // findExpiredListings
    // -------------------------------------------------------------------------

    @Test
    void findExpiredListings_returnsListingsPassedEndTime() {
        List<Listing> expired = listingRepository.findExpiredListings(
                List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED),
                Instant.now());

        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getTitle()).isEqualTo("HP Bekas");
    }

    @Test
    void findExpiredListings_withFutureNow_returnsAllActive() {
        List<Listing> expired = listingRepository.findExpiredListings(
                List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED),
                Instant.now().plus(1, ChronoUnit.DAYS));

        assertThat(expired).hasSize(2);
    }

    @Test
    void findExpiredListings_withDraftStatus_returnsEmpty() {
        List<Listing> expired = listingRepository.findExpiredListings(
                List.of(ListingStatus.DRAFT),
                Instant.now());

        assertThat(expired).isEmpty();
    }

    // -------------------------------------------------------------------------
    // searchListings via ListingSpecification
    // -------------------------------------------------------------------------

    @Test
    void searchListings_withKeyword_returnsMatchingListings() {
        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build("laptop", null, null, null, null, null,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Laptop Gaming");
    }

    @Test
    void searchListings_withBlankKeyword_returnsAll() {
        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build("   ", null, null, null, null, null,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void searchListings_withCategoryId_returnsMatchingListings() {
        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build(null, category.getId(), null, null, null, null,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void searchListings_withMinPrice_filtersBelow() {
        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build(null, null, new BigDecimal("5000000"), null, null, null,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Laptop Gaming");
    }

    @Test
    void searchListings_withMaxPrice_filtersAbove() {
        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build(null, null, null, new BigDecimal("2000000"), null, null,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("HP Bekas");
    }

    @Test
    void searchListings_withEndsBefore_filtersCorrectly() {
        Instant cutoff = Instant.now();

        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build(null, null, null, null, cutoff, null,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("HP Bekas");
    }

    @Test
    void searchListings_withEndsAfter_filtersCorrectly() {
        Instant cutoff = Instant.now();

        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build(null, null, null, null, null, cutoff,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Laptop Gaming");
    }

    @Test
    void searchListings_withNoMatch_returnsEmpty() {
        Page<Listing> result = listingRepository.findAll(
                ListingSpecification.build("nonexistent", null, null, null, null, null,
                        List.of(ListingStatus.ACTIVE)),
                PageRequest.of(0, 20));

        assertThat(result).isEmpty();
    }
}