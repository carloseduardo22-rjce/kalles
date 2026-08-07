package dev.kalles.cashregister.specification;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.valueobject.SessionStatus;
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
