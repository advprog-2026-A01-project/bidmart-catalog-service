package id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateListingRequest {

    // semua field optional, hanya field yang diisi yang diupdate
    private String title;
    private String description;
    private List<String> imageUrls;
}
