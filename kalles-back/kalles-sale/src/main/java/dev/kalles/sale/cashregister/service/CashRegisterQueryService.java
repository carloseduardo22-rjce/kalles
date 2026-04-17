package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.CashRegisterStatusResponse;
import dev.kalles.sale.cashregister.dto.OperatorResponse;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import dev.kalles.sale.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Consultas de leitura para o gerenciamento de caixas pelo ADMIN.
 * Separado de casos de uso de escrita (CQRS lite).
 */
@Service
@RequiredArgsConstructor
public class CashRegisterQueryService {

    private final CashRegisterRepository cashRegisterRepository;
    private final CashRegisterSessionRepository sessionRepository;
    private final OperatorRepository operatorRepository;
    private final CashRegisterPaymentIntegrationService paymentIntegrationService;

    @Transactional(readOnly = true)
    public List<CashRegisterStatusResponse> listAllWithSessionStatus() {
        UUID companyId = getCompanyId();
        List<CashRegister> registers = cashRegisterRepository.findAllByCompanyIdAndActiveTrueOrderByCodeAsc(companyId);
        List<UUID> registersWithPaymentIntegration = paymentIntegrationService.listCashRegistersWithPaymentIntegration();

        return registers.stream()
            .map(cr -> toStatusResponse(cr, registersWithPaymentIntegration.contains(cr.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<OperatorResponse> listOperators() {
        return operatorRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(getCompanyId())
            .stream()
            .map(OperatorResponse::fromEntity)
            .toList();
    }

    private CashRegisterStatusResponse toStatusResponse(CashRegister cashRegister, boolean paymentConfigured) {
        Optional<CashRegisterSession> activeSession = sessionRepository
            .findByCashRegisterAndStatus(cashRegister, SessionStatus.OPEN);

        return new CashRegisterStatusResponse(
            cashRegister.getId(),
            cashRegister.getCode(),
            cashRegister.getDescription(),
            cashRegister.isActive(),
            activeSession.isPresent(),
            activeSession.map(CashRegisterSession::getId).orElse(null),
            activeSession.map(s -> s.getOperator().getName()).orElse(null),
            activeSession.map(CashRegisterSession::getInitialAmountValue).orElse(null),
            activeSession.map(CashRegisterSession::getOpenedAt).orElse(null),
            paymentConfigured,
            activeSession.map(CashRegisterSession::isCashOnlyOperation).orElse(null)
        );
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        return companyId;
    }
}
