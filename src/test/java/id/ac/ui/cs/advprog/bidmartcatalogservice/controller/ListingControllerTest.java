package id.ac.ui.cs.advprog.bidmartcatalogservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.request.CreateListingRequest;
import id.ac.ui.cs.advprog.bidmartcatalogservice.dto.response.ListingResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.exception.ListingNotFoundException;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ListingService listingService;

    private ListingResponse sampleResponse;
    private UUID listingId;
    private final String gatewaySecret = "test-secret";
    private final String userId = "user-123";
    private final String username = "seller1";

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        sampleResponse = ListingResponse.builder()
                .id(listingId)
                .title("Laptop Gaming")
                .description("Laptop bagus")
                .sellerId(userId)
                .sellerUsername(username)
                .startingPrice(new BigDecimal("5000000"))
                .currentPrice(new BigDecimal("5000000"))
                .status(ListingStatus.DRAFT)
                .bidCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/listings
    // -------------------------------------------------------------------------

    @Test
    void searchListings_returnsOk() throws Exception {
        when(listingService.searchListings(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/listings")
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Laptop Gaming"));
    }

    @Test
    void searchListings_withKeyword_returnsFilteredResults() throws Exception {
        when(listingService.searchListings(eq("laptop"), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/listings?keyword=laptop")
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Laptop Gaming"));
    }

    // -------------------------------------------------------------------------
    // GET /api/listings/{id}
    // -------------------------------------------------------------------------

    @Test
    void getListingById_withExistingId_returnsOk() throws Exception {
        when(listingService.getListingById(listingId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/listings/" + listingId)
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listingId.toString()))
                .andExpect(jsonPath("$.title").value("Laptop Gaming"));
    }

    @Test
    void getListingById_withNonExistingId_returns404() throws Exception {
        when(listingService.getListingById(listingId))
                .thenThrow(new ListingNotFoundException(listingId.toString()));

        mockMvc.perform(get("/api/listings/" + listingId)
                        .header("X-Gateway-Secret", gatewaySecret))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/listings/my
    // -------------------------------------------------------------------------

    @Test
    void getMyListings_returnsSellerListings() throws Exception {
        when(listingService.getMyListings(userId)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/listings/my")
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(userId));
    }

    // -------------------------------------------------------------------------
    // POST /api/listings
    // -------------------------------------------------------------------------

    @Test
    void createListing_withValidRequest_returnsCreated() throws Exception {
        CreateListingRequest request = CreateListingRequest.builder()
                .title("Laptop Gaming")
                .categoryId(UUID.randomUUID())
                .startingPrice(new BigDecimal("5000000"))
                .durationMinutes(60)
                .build();

        when(listingService.createListing(eq(userId), eq(username), any(CreateListingRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/listings")
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Laptop Gaming"));
    }

    @Test
    void createListing_withMissingTitle_returns400() throws Exception {
        CreateListingRequest request = CreateListingRequest.builder()
                .categoryId(UUID.randomUUID())
                .startingPrice(new BigDecimal("5000000"))
                .durationMinutes(60)
                .build();

        mockMvc.perform(post("/api/listings")
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/listings/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateListing_withValidRequest_returnsOk() throws Exception {
        when(listingService.updateListing(eq(listingId), eq(userId), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/listings/" + listingId)
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Updated Title\"}"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/listings/{id}
    // -------------------------------------------------------------------------

    @Test
    void cancelListing_withValidRequest_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/listings/" + listingId)
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelListing_withNonExistingId_returns404() throws Exception {
        doThrow(new ListingNotFoundException(listingId.toString()))
                .when(listingService).cancelListing(eq(listingId), eq(userId));

        mockMvc.perform(delete("/api/listings/" + listingId)
                        .header("X-Gateway-Secret", gatewaySecret)
                        .header("X-User-Id", userId))
                .andExpect(status().isNotFound());
    }
}
