package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.payment.domain.PaymentDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public record PrintPaymentDocumentRequest(
        @NotNull(message = "type is required")
        PaymentDocumentType type,

        @NotNull(message = "sizeVertical is required")
        @Positive(message = "sizeVertical must be greater than zero")
        Integer sizeVertical,

        @NotNull(message = "sizeHorizontal is required")
        @Positive(message = "sizeHorizontal must be greater than zero")
        Integer sizeHorizontal,

        @NotBlank(message = "format is required")
        String format,

        @NotBlank(message = "content is required")
        String content,

        Map<String, Object> metadata
) {

    public PaymentDocumentPrintCommand toCommand() {
        return new PaymentDocumentPrintCommand(
                type,
                sizeVertical,
                sizeHorizontal,
                format,
                content,
                metadata
        );
    }
}
