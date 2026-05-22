package id.ac.ui.cs.advprog.bidmartcatalogservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogservice.model.ListingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListingSpecification {
    public static Specification<Listing> build(
            String keyword, UUID categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Instant endsBefore, Instant endsAfter,
            List<ListingStatus> statuses) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%"));
            if (categoryId != null)
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            if (minPrice != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("currentPrice"), minPrice));
            if (maxPrice != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("currentPrice"), maxPrice));
            if (endsBefore != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), endsBefore));
            if (endsAfter != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("endTime"), endsAfter));

            predicates.add(root.get("status").in(statuses));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}