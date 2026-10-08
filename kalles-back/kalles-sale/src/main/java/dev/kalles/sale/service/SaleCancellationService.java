package dev.kalles.sale.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.service.PermissionService;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.entity.SaleAuditEvent;
import dev.kalles.sale.repository.SaleAuditEventRepository;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.shared.service.CheckoutSessionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleCancellationService {

    private final SaleRepository saleRepository;
    private final CheckoutSessionService checkoutSessionService;
    private final OperatorRepository operatorRepository;
    private final PermissionService permissionService;
    private final SaleAuditEventRepository auditRepository;

    @Transactional
    public void cancelSale(String sessionToken, UUID operatorId) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Operator operator = findOperator(operatorId);

        if (!permissionService.canCancelSale(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para cancelar vendas. Solicite autorização de um supervisor.");
        }

        cancel(findCancellableSale(sessionId), operator, null);
    }

    @Transactional
    public void cancelSaleWithAuthorization(
            String sessionToken,
            UUID operatorId,
            UUID authorizerId) {

        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Operator operator = findOperator(operatorId);
        Operator authorizer = findOperator(authorizerId);

        if (!permissionService.canAuthorizeCancellation(authorizer, operator)) {
            throw new ForbiddenOperationException(
                    "O operador autorizador não possui nível de permissão suficiente para autorizar o cancelamento.");
        }

        cancel(findCancellableSale(sessionId), operator, authorizer);
    }

    private void cancel(Sale sale, Operator operator, Operator authorizer) {
        sale.cancel();
        saleRepository.save(sale);
        auditRepository.save(SaleAuditEvent.forCancellation(sale, operator, authorizer));
    }

    private Operator findOperator(UUID operatorId) {
        return operatorRepository.findByIdAndCompanyId(operatorId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Operador não encontrado com o id: " + operatorId));
    }

    private Sale findCancellableSale(UUID sessionId) {
        return saleRepository.findCancellableSaleBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda cancelável para esta sessão"));
    }
}
