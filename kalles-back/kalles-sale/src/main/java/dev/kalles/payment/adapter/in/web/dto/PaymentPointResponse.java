package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentPointView;

import java.util.Map;

public record PaymentPointResponse(
        String providerPointId,
        String name,
        String providerStoreId,
        String externalReference,
        String externalStoreReference,
        Boolean fixedAmount,
        String status,
        String createdAt,
        String updatedAt,
        Map<String, Object> metadata
) {

    public static PaymentPointResponse from(PaymentPointView point) {
        return new PaymentPointResponse(
                point.providerPointId(),
                point.name(),
                point.providerStoreId(),
                point.externalReference(),
                point.externalStoreReference(),
                point.fixedAmount(),
                point.status(),
                point.createdAt(),
                point.updatedAt(),
                point.metadata()
        );
    }
}
