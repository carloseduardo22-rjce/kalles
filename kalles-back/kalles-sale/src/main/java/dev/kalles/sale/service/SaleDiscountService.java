package dev.kalles.sale.service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.service.PermissionService;
import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.entity.SaleAuditEvent;
import dev.kalles.sale.exception.SaleWithoutClientException;
import dev.kalles.sale.repository.SaleAuditEventRepository;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.shared.service.CheckoutSessionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleDiscountService {

    private final SaleRepository saleRepository;
    private final CheckoutSessionService checkoutSessionService;
    private final ClientRepository clientRepository;
    private final FidelityService fidelityService;
    private final OperatorRepository operatorRepository;
    private final PermissionService permissionService;
    private final SaleAuditEventRepository auditRepository;

    @Transactional
    public Sale associateClientWithSale(String sessionToken, UUID clientId) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();
        Sale sale = findActiveSale(sessionId);
        Client client = clientRepository.findByIdAndCompanyId(clientId, sale.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado com o id: " + clientId));
        sale.setClient(client);
        return saleRepository.save(sale);
    }

    @Transactional
    public Sale applyFidelityDiscountToSale(String sessionToken) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();
        Sale sale = findActiveSale(sessionId);
        if (sale.getClient() == null) {
            throw new SaleWithoutClientException();
        }
        BigDecimal applied = fidelityService.calculateDiscount(sale.getClient().getId(), sale.getSubtotal());
        if (applied.compareTo(BigDecimal.ZERO) > 0) {
            sale.applyFidelityDiscount(applied);
            saleRepository.save(sale);
        }
        return sale;
    }

    @Transactional
    public void applyItemDiscount(
            String sessionToken,
            UUID itemId,
            BigDecimal discountAmount,
            UUID operatorId,
            UUID authorizerId) {

        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Operator operator = findOperator(operatorId);
        Operator authorizer = null;
        if (authorizerId != null) {
            authorizer = findOperator(authorizerId);
            if (!permissionService.canAuthorizeItemDiscount(authorizer, operator)) {
                throw new ForbiddenOperationException(
                        "O operador autorizador não possui nível de permissão suficiente para autorizar o desconto.");
            }
        } else if (!permissionService.canApplyItemDiscount(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para aplicar descontos. Solicite autorização de um supervisor.");
        }

        Sale sale = findActiveSale(sessionId);
        sale.applyItemDiscount(itemId, discountAmount);
        saleRepository.save(sale);

        Product discountedProduct = sale.getItems().stream()
                .filter(item -> Objects.equals(item.getId(), itemId))
                .findFirst()
                .map(item -> item.getProduct())
                .orElse(null);
        auditRepository.save(
                SaleAuditEvent.forItemDiscount(sale, discountedProduct, discountAmount, operator, authorizer));
    }

    private Operator findOperator(UUID operatorId) {
        return operatorRepository.findByIdAndCompanyId(operatorId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Operador não encontrado com o id: " + operatorId));
    }

    private Sale findActiveSale(UUID sessionId) {
        return saleRepository.findActiveSaleBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda em andamento para esta sessão"));
    }
}
