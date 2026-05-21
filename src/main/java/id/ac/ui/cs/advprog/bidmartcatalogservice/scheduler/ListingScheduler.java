package id.ac.ui.cs.advprog.bidmartcatalogservice.scheduler;

import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListingScheduler {

    private final ListingService listingService;

    // jalan setiap 60 detik
    @Scheduled(fixedDelayString = "${app.scheduler.listing-close.fixed-delay-ms:60000}")
    public void closeExpiredListings() {
        log.info("Running closeExpiredListings job...");
        listingService.closeExpiredListings();
    }
}