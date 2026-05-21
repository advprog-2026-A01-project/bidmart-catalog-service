package id.ac.ui.cs.advprog.bidmartcatalogservice.scheduler;

import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ListingSchedulerTest {

    @Mock
    private ListingService listingService;

    @InjectMocks
    private ListingScheduler listingScheduler;

    @Test
    void closeExpiredListings_callsServiceMethod() {
        listingScheduler.closeExpiredListings();

        verify(listingService).closeExpiredListings();
    }
}