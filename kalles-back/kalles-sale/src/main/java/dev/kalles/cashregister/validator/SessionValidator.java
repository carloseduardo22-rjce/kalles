package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.entity.Operator;

public interface SessionValidator {

    void validate(OpenSessionRequest request, Operator operator);
}
