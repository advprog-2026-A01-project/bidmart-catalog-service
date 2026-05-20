package id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingResponse {

    private UUID id;
    private String title;
    private String description;
    private String sellerId;
    private String sellerUsername;
    private CategoryResponse category;
    private List<String> imageUrls;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentPrice;
    private int durationMinutes;
    private Instant startTime;
    private Instant endTime;
    private ListingStatus status;
    private int bidCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static ListingResponse from(Listing listing) {
        return ListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .sellerId(listing.getSellerId())
                .sellerUsername(listing.getSellerUsername())
                .category(listing.getCategory() != null
                        ? CategoryResponse.fromShallow(listing.getCategory())
                        : null)
                .imageUrls(listing.getImages().stream()
                        .map(img -> img.getImageUrl())
                        .toList())
                .startingPrice(listing.getStartingPrice())
                .reservePrice(listing.getReservePrice())
                .currentPrice(listing.getCurrentPrice())
                .durationMinutes(listing.getDurationMinutes())
                .startTime(listing.getStartTime())
                .endTime(listing.getEndTime())
                .status(listing.getStatus())
                .bidCount(listing.getBidCount())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }
}
