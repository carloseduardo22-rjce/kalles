package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.exception.ActiveSessionAlreadyExistsException;
import dev.kalles.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.specification.ActiveSessionSpecification;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Scope("prototype")
@Component
@RequiredArgsConstructor
public class NoActiveSessionValidator extends SessionValidator {

    private final CashRegisterRepository cashRegisterRepository;
    private final ActiveSessionSpecification activeSessionSpec;

    @Override
    protected void doValidate(OpenSessionRequest request) {
        UUID companyId = getCompanyId();
        CashRegister cashRegister = cashRegisterRepository
            .findByCodeAndCompanyId(request.cashRegisterCode(), companyId)
            .orElseThrow(() -> new CashRegisterNotFoundException(request.cashRegisterCode()));

        if (activeSessionSpec.isSatisfiedBy(cashRegister)) {
            throw new ActiveSessionAlreadyExistsException(cashRegister.getCode());
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
