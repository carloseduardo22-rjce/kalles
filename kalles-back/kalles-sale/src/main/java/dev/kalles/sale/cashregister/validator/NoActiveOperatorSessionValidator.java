package dev.kalles.sale.cashregister.validator;

import dev.kalles.sale.cashregister.dto.OpenSessionRequest;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.exception.OperatorAlreadyInSessionException;
import dev.kalles.sale.cashregister.exception.OperatorNotFoundException;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Valida que o operador não está vinculado a nenhuma outra sessão ativa.
 * Um operador só pode estar em um caixa por vez.
 */
@Scope("prototype")
@Component
@RequiredArgsConstructor
public class NoActiveOperatorSessionValidator extends SessionValidator {

    private final OperatorRepository operatorRepository;
    private final CashRegisterSessionRepository sessionRepository;

    @Override
    protected void doValidate(OpenSessionRequest request) {
        Operator operator = operatorRepository
            .findByCode(request.operatorCode())
            .orElseThrow(() -> new OperatorNotFoundException(request.operatorCode()));

        if (sessionRepository.existsByOperatorAndStatus(operator, SessionStatus.OPEN)) {
            throw new OperatorAlreadyInSessionException(request.operatorCode());
        }
    }
}
