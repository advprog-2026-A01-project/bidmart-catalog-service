package id.ac.ui.cs.advprog.bidmartcatalogservice.grpc;

import id.ac.ui.cs.advprog.bidmart.catalog.grpc.ValidateListingRequest;
import id.ac.ui.cs.advprog.bidmart.catalog.grpc.ValidateListingResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogGrpcServiceTest {

    @Mock
    private ListingService listingService;

    @Mock
    private StreamObserver<ValidateListingResponse> responseObserver;

    @InjectMocks
    private CatalogGrpcService catalogGrpcService;

    @Test
    void validateListing_whenBiddable_returnsTrueWithMessage() {
        UUID listingId = UUID.randomUUID();
        when(listingService.isListingBiddable(listingId)).thenReturn(true);

        ValidateListingRequest request = ValidateListingRequest.newBuilder()
                .setListingId(listingId.toString())
                .setCallerService("bidmart-auction-service")
                .build();

        catalogGrpcService.validateListing(request, responseObserver);

        ArgumentCaptor<ValidateListingResponse> captor =
                ArgumentCaptor.forClass(ValidateListingResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ValidateListingResponse response = captor.getValue();
        assertThat(response.getBiddable()).isTrue();
        assertThat(response.getListingId()).isEqualTo(listingId.toString());
        assertThat(response.getMessage()).isEqualTo("Listing is biddable");
    }

    @Test
    void validateListing_whenNotBiddable_returnsFalseWithMessage() {
        UUID listingId = UUID.randomUUID();
        when(listingService.isListingBiddable(listingId)).thenReturn(false);

        ValidateListingRequest request = ValidateListingRequest.newBuilder()
                .setListingId(listingId.toString())
                .setCallerService("bidmart-auction-service")
                .build();

        catalogGrpcService.validateListing(request, responseObserver);

        ArgumentCaptor<ValidateListingResponse> captor =
                ArgumentCaptor.forClass(ValidateListingResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ValidateListingResponse response = captor.getValue();
        assertThat(response.getBiddable()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Listing is not biddable");
    }

    @Test
    void validateListing_whenInvalidUuid_returnsErrorResponse() {
        ValidateListingRequest request = ValidateListingRequest.newBuilder()
                .setListingId("not-a-valid-uuid")
                .setCallerService("bidmart-auction-service")
                .build();

        catalogGrpcService.validateListing(request, responseObserver);

        ArgumentCaptor<ValidateListingResponse> captor =
                ArgumentCaptor.forClass(ValidateListingResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ValidateListingResponse response = captor.getValue();
        assertThat(response.getBiddable()).isFalse();
        assertThat(response.getMessage()).contains("Invalid listing ID");
    }
}