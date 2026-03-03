package dev.kalles.sale.cashregister.validator;

import dev.kalles.sale.cashregister.dto.OpenSessionRequest;

public abstract class SessionValidator {

    protected SessionValidator next;

    public SessionValidator setNext(SessionValidator next) {
        this.next = next;
        return next;
    }

    public void validate(OpenSessionRequest request) {
        doValidate(request);
        if (next != null) {
            next.validate(request);
        }
    }

    protected abstract void doValidate(OpenSessionRequest request);
}
