package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.OperatorAlreadyInSessionException;
import dev.kalles.cashregister.exception.OperatorNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.valueobject.SessionStatus;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(2)
@Component
@RequiredArgsConstructor
public class NoActiveOperatorSessionValidator implements SessionValidator {

    private final OperatorRepository operatorRepository;
    private final CashRegisterSessionRepository sessionRepository;

    @Override
    public void validate(OpenSessionRequest request) {
        UUID companyId = getCompanyId();
        Operator operator = operatorRepository
            .findByCodeAndCompanyId(request.operatorCode(), companyId)
            .orElseThrow(() -> new OperatorNotFoundException(request.operatorCode()));

        if (sessionRepository.existsByOperatorAndStatus(operator, SessionStatus.OPEN)) {
            throw new OperatorAlreadyInSessionException(request.operatorCode());
        }
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        return companyId;
    }
}
