package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.exception.ActiveSessionAlreadyExistsException;
import dev.kalles.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.specification.ActiveSessionSpecification;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(1)
@Component
@RequiredArgsConstructor
public class NoActiveSessionValidator implements SessionValidator {

    private final CashRegisterRepository cashRegisterRepository;
    private final ActiveSessionSpecification activeSessionSpec;

    @Override
    public void validate(OpenSessionRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        CashRegister cashRegister = cashRegisterRepository
            .findByCodeAndCompanyId(request.cashRegisterCode(), companyId)
            .orElseThrow(() -> new CashRegisterNotFoundException(request.cashRegisterCode()));

        if (activeSessionSpec.isSatisfiedBy(cashRegister)) {
            throw new ActiveSessionAlreadyExistsException(cashRegister.getCode());
        }
    }
}
