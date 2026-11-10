package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.OperatorAlreadyInSessionException;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.valueobject.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@RequiredArgsConstructor
public class NoActiveOperatorSessionValidator implements SessionValidator {

    private final CashRegisterSessionRepository sessionRepository;

    @Override
    public void validate(OpenSessionRequest request, Operator operator) {
        if (sessionRepository.existsByOperatorAndStatus(operator, SessionStatus.OPEN)) {
            throw new OperatorAlreadyInSessionException(operator.getCode());
        }
    }
}
