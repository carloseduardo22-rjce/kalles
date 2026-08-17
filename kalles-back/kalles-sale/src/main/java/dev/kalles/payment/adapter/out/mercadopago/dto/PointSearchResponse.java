package dev.kalles.payment.adapter.out.mercadopago.dto;

import java.util.List;

public record PointSearchResponse(String id, Paging paging, List<PointResponse> results) {

    public record Paging(Integer total) {
    }

    public boolean hasResults() {
        return paging != null && paging.total() != null && paging.total() > 0 && results != null && !results.isEmpty();
    }
}
