package id.ac.ui.cs.advprog.bidmartcatalogservice.consumer;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventConsumer {

    private final ListingService listingService;

    @RabbitListener(queues = "${app.rabbitmq.queue.bid-placed}")
    public void handleBidPlaced(Map<String, Object> event) {
        UUID listingId = parseListingId(event);
        if (listingId == null) return;

        try {
            BigDecimal newPrice = parseBigDecimal(event.get("newPrice"));
            int bidCount = (int) event.get("bidCount");
            listingService.updateListingPriceAndBidCount(listingId, newPrice, bidCount);
            log.info("Updated listing {} price to {} bidCount {}", listingId, newPrice, bidCount);
        } catch (Exception e) {
            log.error("Failed to handle bid.placed for listing {}: {}", listingId, e.getMessage());
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.auction-closed}")
    public void handleAuctionClosed(Map<String, Object> event) {
        UUID listingId = parseListingId(event);
        if (listingId == null) return;

        try {
            String statusStr = event.get("status").toString();
            ListingStatus status = ListingStatus.valueOf(statusStr);

            if (status != ListingStatus.WON && status != ListingStatus.UNSOLD) {
                log.warn("Invalid status {} for auction.closed event", statusStr);
                return;
            }

            BigDecimal finalPrice = parseBigDecimal(event.get("finalPrice"));
            listingService.updateListingStatus(listingId, status, finalPrice);
            log.info("Updated listing {} status to {}", listingId, status);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status in auction.closed event for listing {}: {}", listingId, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to handle auction.closed for listing {}: {}", listingId, e.getMessage());
        }
    }

    private UUID parseListingId(Map<String, Object> event) {
        try {
            Object raw = event.get("listingId");
            if (raw == null) {
                log.warn("Missing listingId in event");
                return null;
            }
            return UUID.fromString(raw.toString());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid listingId in event: {}", event.get("listingId"));
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        return new BigDecimal(value.toString());
    }
}