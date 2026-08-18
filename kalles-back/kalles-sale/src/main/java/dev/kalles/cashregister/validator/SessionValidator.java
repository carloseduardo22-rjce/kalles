package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;

public interface SessionValidator {

    void validate(OpenSessionRequest request);
}
