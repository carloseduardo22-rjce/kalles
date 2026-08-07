package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.dto.SessionResponse;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.cashregister.exception.OperatorNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.validator.SessionValidator;
import dev.kalles.security.context.CompanyContextHolder;
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
    private final CashRegisterPaymentIntegrationService paymentIntegrationService;

    public OpenSessionUseCase(
            CashRegisterRepository cashRegisterRepository,
            OperatorRepository operatorRepository,
            CashRegisterSessionRepository sessionRepository,
            @Qualifier("sessionValidatorChain") SessionValidator validatorChain,
            PairedDeviceSessionGuard pairedDeviceSessionGuard,
            CashRegisterPaymentIntegrationService paymentIntegrationService
    ) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.operatorRepository = operatorRepository;
        this.sessionRepository = sessionRepository;
        this.validatorChain = validatorChain;
        this.pairedDeviceSessionGuard = pairedDeviceSessionGuard;
        this.paymentIntegrationService = paymentIntegrationService;
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

        boolean paymentIntegrationConfigured = paymentIntegrationService.isPaymentIntegrationConfigured(cashRegister);
        boolean cashOnlyOperation = !paymentIntegrationConfigured && request.shouldAllowCashOnlyOperation();

        if (!paymentIntegrationConfigured && !cashOnlyOperation) {
            throw new IllegalStateException(
                "Pagamento nao configurado, neste caixa voce apenas podera operar com dinheiro mas nao podera receber pagamentos via pix, vouchers e cartoes de credito."
            );
        }

        CashRegisterSession session = CashRegisterSession.open(
            cashRegister,
            operator,
            request.initialAmount(),
            cashOnlyOperation
        );

        CashRegisterSession savedSession = sessionRepository.save(session);

        return SessionResponse.fromEntity(savedSession);
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        return companyId;
    }
}
