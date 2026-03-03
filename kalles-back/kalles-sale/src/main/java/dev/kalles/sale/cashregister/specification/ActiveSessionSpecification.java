package dev.kalles.sale.cashregister.specification;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveSessionSpecification implements Specification<CashRegister> {

    private final CashRegisterSessionRepository sessionRepository;

    @Override
    public boolean isSatisfiedBy(CashRegister cashRegister) {
        return sessionRepository.existsByCashRegisterAndStatus(
            cashRegister,
            SessionStatus.OPEN
        );
    }
}
