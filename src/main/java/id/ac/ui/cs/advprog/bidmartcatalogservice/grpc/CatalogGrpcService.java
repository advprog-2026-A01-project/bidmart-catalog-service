package id.ac.ui.cs.advprog.bidmartcatalogservice.grpc;

import id.ac.ui.cs.advprog.bidmart.catalog.grpc.CatalogInternalServiceGrpc;
import id.ac.ui.cs.advprog.bidmart.catalog.grpc.ValidateListingRequest;
import id.ac.ui.cs.advprog.bidmart.catalog.grpc.ValidateListingResponse;
import id.ac.ui.cs.advprog.bidmartcatalogservice.service.ListingService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CatalogGrpcService extends CatalogInternalServiceGrpc.CatalogInternalServiceImplBase {

    private final ListingService listingService;

    @Override
    public void validateListing(ValidateListingRequest request,
                                StreamObserver<ValidateListingResponse> responseObserver) {
        ValidateListingResponse response;

        try {
            UUID listingId = UUID.fromString(request.getListingId());
            boolean biddable = listingService.isListingBiddable(listingId);

            String message = biddable ? "Listing is biddable" : "Listing is not biddable";

            response = ValidateListingResponse.newBuilder()
                    .setBiddable(biddable)
                    .setListingId(request.getListingId())
                    .setMessage(message)
                    .build();

            if (log.isDebugEnabled()) {
                log.debug("validateListing: listingId={} biddable={} caller={}",
                        request.getListingId(), biddable, request.getCallerService());
            }

        } catch (IllegalArgumentException e) {
            response = ValidateListingResponse.newBuilder()
                    .setBiddable(false)
                    .setListingId(request.getListingId())
                    .setMessage("Invalid listing ID: " + request.getListingId())
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}