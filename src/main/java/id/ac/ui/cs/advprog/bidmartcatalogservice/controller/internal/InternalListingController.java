package id.ac.ui.cs.advprog.bidmartcatalogservice.controller.internal;

import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/listings")
@RequiredArgsConstructor
public class InternalListingController {

    private final ListingService listingService;

    // GET /internal/listings/{id}/biddable
    // dipanggil oleh auction-service sebelum menerima bid
    // hanya bisa diakses dari dalam cluster (via X-Gateway-Secret sama)
    @GetMapping("/{id}/biddable")
    public ResponseEntity<Map<String, Boolean>> isListingBiddable(@PathVariable UUID id) {
        boolean biddable = listingService.isListingBiddable(id);
        return ResponseEntity.ok(Map.of("biddable", biddable));
    }
}
