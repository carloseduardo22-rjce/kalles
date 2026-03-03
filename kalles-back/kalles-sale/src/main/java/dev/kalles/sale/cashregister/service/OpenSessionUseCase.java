package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.OpenSessionRequest;
import dev.kalles.sale.cashregister.dto.SessionResponse;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.sale.cashregister.exception.OperatorNotFoundException;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.cashregister.validator.SessionValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenSessionUseCase {

    private final CashRegisterRepository cashRegisterRepository;
    private final OperatorRepository operatorRepository;
    private final CashRegisterSessionRepository sessionRepository;
    private final SessionValidator validatorChain;

    public OpenSessionUseCase(
            CashRegisterRepository cashRegisterRepository,
            OperatorRepository operatorRepository,
            CashRegisterSessionRepository sessionRepository,
            @Qualifier("sessionValidatorChain") SessionValidator validatorChain
    ) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.operatorRepository = operatorRepository;
        this.sessionRepository = sessionRepository;
        this.validatorChain = validatorChain;
    }

    @Transactional
    public SessionResponse execute(OpenSessionRequest request) {
        validatorChain.validate(request);

        CashRegister cashRegister = cashRegisterRepository
            .findByCode(request.cashRegisterCode())
            .orElseThrow(() -> new CashRegisterNotFoundException(request.cashRegisterCode()));

        Operator operator = operatorRepository
            .findByCode(request.operatorCode())
            .orElseThrow(() -> new OperatorNotFoundException(request.operatorCode()));

        CashRegisterSession session = CashRegisterSession.open(
            cashRegister,
            operator,
            request.initialAmount()
        );

        CashRegisterSession savedSession = sessionRepository.save(session);

        return SessionResponse.fromEntity(savedSession);
    }
}
