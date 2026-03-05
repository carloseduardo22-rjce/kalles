package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.CashRegisterStatusResponse;
import dev.kalles.sale.cashregister.dto.OperatorResponse;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional(readOnly = true)
    public List<CashRegisterStatusResponse> listAllWithSessionStatus() {
        List<CashRegister> registers = cashRegisterRepository.findAllByActiveTrueOrderByCodeAsc();

        return registers.stream()
            .map(this::toStatusResponse)
            .toList();
    }


    @Transactional(readOnly = true)
    public List<OperatorResponse> listOperators() {
        return operatorRepository.findAllByActiveTrueOrderByNameAsc()
            .stream()
            .map(OperatorResponse::fromEntity)
            .toList();
    }

    private CashRegisterStatusResponse toStatusResponse(CashRegister cashRegister) {
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
            activeSession.map(CashRegisterSession::getOpenedAt).orElse(null)
        );
    }
}
