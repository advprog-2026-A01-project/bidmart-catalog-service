package id.ac.ui.cs.advprog.bidmartcatalogservice.exception;

import id.ac.ui.cs.advprog.bidmartcatalogservice.controller.ListingController;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;

    private final UUID listingId = UUID.randomUUID();
    private final String userId = "user-123";

    @Test
    void listingNotFound_returns404WithBody() throws Exception {
        when(listingService.getListingById(listingId))
                .thenThrow(new ListingNotFoundException(listingId.toString()));

        mockMvc.perform(get("/api/listings/" + listingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void listingNotEditable_returns409WithBody() throws Exception {
        doThrow(new ListingNotEditableException(listingId.toString()))
                .when(listingService).cancelListing(eq(listingId), eq(userId));

        mockMvc.perform(delete("/api/listings/" + listingId)
                        .header("X-User-Id", userId)
                        .header("X-User-Role", "SELLER"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void forbidden_returns403WithBody() throws Exception {
        mockMvc.perform(delete("/api/listings/" + listingId)
                        .header("X-User-Id", userId)
                        .header("X-User-Role", "BUYER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void missingRequiredHeader_returns400WithBody() throws Exception {
        mockMvc.perform(delete("/api/listings/" + listingId)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("X-User-Role")));
    }

    @Test
    void validationFailed_returns400WithFieldMessage() throws Exception {
        String body = """
                {
                  "categoryId": "00000000-0000-0000-0000-000000000001",
                  "startingPrice": 5000000,
                  "durationMinutes": 60
                }
                """;

        mockMvc.perform(post("/api/listings")
                        .header("X-User-Id", userId)
                        .header("X-Username", "seller1")
                        .header("X-User-Role", "SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void generalException_returns500WithBody() throws Exception {
        when(listingService.getListingById(any()))
                .thenThrow(new RuntimeException("unexpected error"));

        mockMvc.perform(get("/api/listings/" + listingId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("unexpected error"));
    }
}
