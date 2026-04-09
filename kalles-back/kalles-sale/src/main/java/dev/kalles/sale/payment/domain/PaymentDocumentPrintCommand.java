package dev.kalles.sale.payment.domain;

import java.util.Map;
import java.util.Objects;

public record PaymentDocumentPrintCommand(
        PaymentDocumentType type,
        Integer sizeVertical,
        Integer sizeHorizontal,
        String format,
        String content,
        Map<String, Object> metadata
) {

    public PaymentDocumentPrintCommand {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(sizeVertical, "sizeVertical is required");
        Objects.requireNonNull(sizeHorizontal, "sizeHorizontal is required");
        Objects.requireNonNull(format, "format is required");
        Objects.requireNonNull(content, "content is required");
        metadata = DomainMetadata.immutableCopy(metadata);
    }
}
