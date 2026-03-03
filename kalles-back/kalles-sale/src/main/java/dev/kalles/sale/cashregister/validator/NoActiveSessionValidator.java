package dev.kalles.sale.cashregister.validator;

import dev.kalles.sale.cashregister.dto.OpenSessionRequest;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.exception.ActiveSessionAlreadyExistsException;
import dev.kalles.sale.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.specification.ActiveSessionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
@RequiredArgsConstructor
public class NoActiveSessionValidator extends SessionValidator {

    private final CashRegisterRepository cashRegisterRepository;
    private final ActiveSessionSpecification activeSessionSpec;

    @Override
    protected void doValidate(OpenSessionRequest request) {
        CashRegister cashRegister = cashRegisterRepository
            .findByCode(request.cashRegisterCode())
            .orElseThrow(() -> new CashRegisterNotFoundException(request.cashRegisterCode()));

        if (activeSessionSpec.isSatisfiedBy(cashRegister)) {
            throw new ActiveSessionAlreadyExistsException(cashRegister.getCode());
        }
    }
}
