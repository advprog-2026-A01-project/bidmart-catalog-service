package id.ac.ui.cs.advprog.bidmartcatalogservice.consumer;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionEventConsumerTest {

    @Mock
    private ListingService listingService;

    @InjectMocks
    private AuctionEventConsumer consumer;

    private UUID listingId;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
    }

    // bid.placed

    @Test
    void handleBidPlaced_shouldUpdatePriceAndBidCount() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", listingId.toString());
        event.put("newPrice", "150000.00");
        event.put("bidCount", 3);

        consumer.handleBidPlaced(event);

        verify(listingService).updateListingPriceAndBidCount(
                listingId,
                new BigDecimal("150000.00"),
                3
        );
    }

    @Test
    void handleBidPlaced_withMissingListingId_shouldNotCallService() {
        Map<String, Object> event = new HashMap<>();
        event.put("newPrice", "150000.00");
        event.put("bidCount", 3);

        consumer.handleBidPlaced(event);

        verifyNoInteractions(listingService);
    }

    @Test
    void handleBidPlaced_withInvalidListingId_shouldNotCallService() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", "not-a-uuid");
        event.put("newPrice", "150000.00");
        event.put("bidCount", 3);

        consumer.handleBidPlaced(event);

        verifyNoInteractions(listingService);
    }

    // auction.closed WON

    @Test
    void handleAuctionClosed_withStatusWon_shouldUpdateStatusToWon() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", listingId.toString());
        event.put("finalPrice", "200000.00");
        event.put("status", "WON");

        consumer.handleAuctionClosed(event);

        verify(listingService).updateListingStatus(
                listingId,
                ListingStatus.WON,
                new BigDecimal("200000.00")
        );
    }

    // auction.closed UNSOLD

    @Test
    void handleAuctionClosed_withStatusUnsold_shouldUpdateStatusToUnsold() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", listingId.toString());
        event.put("finalPrice", null);
        event.put("status", "UNSOLD");

        consumer.handleAuctionClosed(event);

        verify(listingService).updateListingStatus(
                listingId,
                ListingStatus.UNSOLD,
                null
        );
    }

    @Test
    void handleAuctionClosed_withMissingListingId_shouldNotCallService() {
        Map<String, Object> event = new HashMap<>();
        event.put("finalPrice", "200000.00");
        event.put("status", "WON");

        consumer.handleAuctionClosed(event);

        verifyNoInteractions(listingService);
    }

    @Test
    void handleAuctionClosed_withInvalidStatus_shouldNotCallService() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", listingId.toString());
        event.put("finalPrice", "200000.00");
        event.put("status", "INVALID_STATUS");

        consumer.handleAuctionClosed(event);

        verifyNoInteractions(listingService);
    }

    @Test
    void handleAuctionClosed_withStatusActive_shouldNotCallService() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", listingId.toString());
        event.put("finalPrice", "200000.00");
        event.put("status", "ACTIVE");

        consumer.handleAuctionClosed(event);

        verifyNoInteractions(listingService);
    }

    @Test
    void handleAuctionClosed_withStatusDraft_shouldNotCallService() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", listingId.toString());
        event.put("finalPrice", "200000.00");
        event.put("status", "DRAFT");

        consumer.handleAuctionClosed(event);

        verifyNoInteractions(listingService);
    }

    @Test
    void handleAuctionClosed_withNullStatus_shouldNotCallService() {
        Map<String, Object> event = new HashMap<>();
        event.put("listingId", listingId.toString());
        event.put("finalPrice", "200000.00");
        event.put("status", null);

        consumer.handleAuctionClosed(event);

        verifyNoInteractions(listingService);
    }

}