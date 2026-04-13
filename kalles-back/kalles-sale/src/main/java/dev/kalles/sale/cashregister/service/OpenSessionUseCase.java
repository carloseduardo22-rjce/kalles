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
import dev.kalles.sale.security.context.CompanyContextHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OpenSessionUseCase {

    private final CashRegisterRepository cashRegisterRepository;
    private final OperatorRepository operatorRepository;
    private final CashRegisterSessionRepository sessionRepository;
    private final SessionValidator validatorChain;
    private final PairedDeviceSessionGuard pairedDeviceSessionGuard;

    public OpenSessionUseCase(
            CashRegisterRepository cashRegisterRepository,
            OperatorRepository operatorRepository,
            CashRegisterSessionRepository sessionRepository,
            @Qualifier("sessionValidatorChain") SessionValidator validatorChain,
            PairedDeviceSessionGuard pairedDeviceSessionGuard
    ) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.operatorRepository = operatorRepository;
        this.sessionRepository = sessionRepository;
        this.validatorChain = validatorChain;
        this.pairedDeviceSessionGuard = pairedDeviceSessionGuard;
    }

    @Transactional
    public SessionResponse execute(OpenSessionRequest request) {
        validatorChain.validate(request);

        UUID companyId = getCompanyId();

        CashRegister cashRegister = cashRegisterRepository
            .findByCodeAndCompanyId(request.cashRegisterCode(), companyId)
            .orElseThrow(() -> new CashRegisterNotFoundException(request.cashRegisterCode()));

        pairedDeviceSessionGuard.ensureCanOperate(cashRegister);

        Operator operator = operatorRepository
            .findByCodeAndCompanyId(request.operatorCode(), companyId)
            .orElseThrow(() -> new OperatorNotFoundException(request.operatorCode()));

        CashRegisterSession session = CashRegisterSession.open(
            cashRegister,
            operator,
            request.initialAmount()
        );

        CashRegisterSession savedSession = sessionRepository.save(session);

        return SessionResponse.fromEntity(savedSession);
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
    }
}
