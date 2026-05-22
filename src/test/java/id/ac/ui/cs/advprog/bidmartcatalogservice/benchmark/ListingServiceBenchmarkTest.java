package id.ac.ui.cs.advprog.bidmartcatalogservice.benchmark;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.*;
import id.ac.ui.cs.advprog.bidmartcatalogservice.repository.*;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.impl.ListingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:benchmarktest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(ListingServiceImpl.class)
public class ListingServiceBenchmarkTest {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ListingServiceImpl listingService;

    @BeforeEach
    void seedData() {
        listingRepository.deleteAll();
        categoryRepository.deleteAll();

        Category cat = categoryRepository.save(Category.builder()
                .name("Elektronik").build());

        List<Listing> listings = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            listings.add(Listing.builder()
                    .title("Laptop Gaming " + i)
                    .description("Deskripsi produk nomor " + i)
                    .sellerId("seller-" + (i % 10))
                    .sellerUsername("seller" + (i % 10))
                    .category(cat)
                    .startingPrice(new BigDecimal(1_000_000 + i * 10_000))
                    .currentPrice(new BigDecimal(1_000_000 + i * 10_000))
                    .durationMinutes(60)
                    .status(i % 3 == 0 ? ListingStatus.DRAFT : ListingStatus.ACTIVE)
                    .startTime(Instant.now().minus(30, ChronoUnit.MINUTES))
                    .endTime(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .bidCount(i % 5)
                    .build());
        }
        listingRepository.saveAll(listings);
    }

    @Test
    void benchmark_searchListings_withKeyword() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            listingService.searchListings(
                    "Laptop", null,
                    null, null,
                    null, null,
                    null,
                    PageRequest.of(0, 20));
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("=== [AFTER] 100x searchListings(keyword): %d ms (avg %.1f ms/call)%n",
                elapsed, elapsed / 100.0);
    }

    @Test
    void benchmark_closeExpiredListings() {
        // seed expired listings
        Category cat = categoryRepository.findAll().get(0);
        List<Listing> expired = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            expired.add(Listing.builder()
                    .title("Expired " + i)
                    .sellerId("seller-1")
                    .sellerUsername("seller1")
                    .category(cat)
                    .startingPrice(new BigDecimal("500000"))
                    .currentPrice(new BigDecimal("500000"))
                    .durationMinutes(60)
                    .status(ListingStatus.ACTIVE)
                    .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                    .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                    .bidCount(i % 2)
                    .build());
        }
        listingRepository.saveAll(expired);

        long start = System.currentTimeMillis();
        listingService.closeExpiredListings();
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("=== [AFTER] closeExpiredListings(100 listings): %d ms%n", elapsed);
    }
}