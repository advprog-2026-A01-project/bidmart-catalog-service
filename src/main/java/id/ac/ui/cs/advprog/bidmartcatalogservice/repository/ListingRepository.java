package id.ac.ui.cs.advprog.bidmartcatalogservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

    List<Listing> findBySellerId(String sellerId);

    // search listing aktif dengan filter, untuk buyer browse
    @Query("""
            SELECT l FROM Listing l
            WHERE (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:categoryId IS NULL OR l.category.id = :categoryId)
            AND (:minPrice IS NULL OR l.currentPrice >= :minPrice)
            AND (:maxPrice IS NULL OR l.currentPrice <= :maxPrice)
            AND l.status IN :statuses
            """)
    Page<Listing> searchListings(
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("statuses") List<ListingStatus> statuses,
            Pageable pageable
    );

    // untuk internal endpoint, dicall auction-service untuk validasi
    boolean existsByIdAndStatusIn(UUID id, List<ListingStatus> statuses);
}
