package id.ac.ui.cs.advprog.bidmartcatalogservice.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListingTest {

    @Test
    void isBiddable_whenActive_returnsTrue() {
        Listing listing = Listing.builder().status(ListingStatus.ACTIVE).build();
        assertThat(listing.isBiddable()).isTrue();
    }

    @Test
    void isBiddable_whenExtended_returnsTrue() {
        Listing listing = Listing.builder().status(ListingStatus.EXTENDED).build();
        assertThat(listing.isBiddable()).isTrue();
    }

    @Test
    void isBiddable_whenDraft_returnsFalse() {
        Listing listing = Listing.builder().status(ListingStatus.DRAFT).build();
        assertThat(listing.isBiddable()).isFalse();
    }

    @Test
    void isBiddable_whenCancelled_returnsFalse() {
        Listing listing = Listing.builder().status(ListingStatus.CANCELLED).build();
        assertThat(listing.isBiddable()).isFalse();
    }

    @Test
    void isEditable_whenDraftAndNoBids_returnsTrue() {
        Listing listing = Listing.builder().status(ListingStatus.DRAFT).bidCount(0).build();
        assertThat(listing.isEditable()).isTrue();
    }

    @Test
    void isEditable_whenDraftButHasBids_returnsFalse() {
        Listing listing = Listing.builder().status(ListingStatus.DRAFT).bidCount(1).build();
        assertThat(listing.isEditable()).isFalse();
    }

    @Test
    void isEditable_whenActive_returnsFalse() {
        Listing listing = Listing.builder().status(ListingStatus.ACTIVE).bidCount(0).build();
        assertThat(listing.isEditable()).isFalse();
    }
}
