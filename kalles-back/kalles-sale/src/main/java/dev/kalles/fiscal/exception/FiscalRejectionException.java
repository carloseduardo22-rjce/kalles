package dev.kalles.fiscal.exception;

import dev.kalles.fiscal.domain.FiscalDocument;

public class FiscalRejectionException extends RuntimeException {

    private final FiscalDocument document;

    public FiscalRejectionException(FiscalDocument document) {
        super(document.rejectionReason());
        this.document = document;
    }

    public FiscalDocument document() {
        return document;
    }
}
