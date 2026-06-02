package com.fibank.transfer.repository.spec;

import com.fibank.transfer.entity.LedgerEntryEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic {@link Specification} from a {@link LedgerFilter}. Each present
 * filter contributes one predicate; all predicates are AND-combined. This keeps the
 * query logic declarative and open for extension (a new filter = a new {@code if}).
 */
public final class LedgerSpecifications {

    private LedgerSpecifications() {
    }

    public static Specification<LedgerEntryEntity> withFilter(LedgerFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.accountIban() != null) {
                predicates.add(cb.equal(root.get("accountIban"), filter.accountIban()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.dateTo()));
            }
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (filter.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }
            if (filter.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
