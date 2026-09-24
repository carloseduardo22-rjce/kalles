package dev.kalles.sale.service;

import java.util.Comparator;
import java.util.UUID;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.service.PermissionService;
import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.inventory.service.StockService;
import dev.kalles.product.entity.CompanyProduct;
import dev.kalles.product.entity.Product;
import dev.kalles.product.repository.CompanyProductRepository;
import dev.kalles.product.repository.ProductRepository;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.entity.SaleAuditEvent;
import dev.kalles.sale.repository.SaleAuditEventRepository;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.shared.service.CheckoutSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CheckoutSessionService checkoutSessionService;
    private final OperatorRepository operatorRepository;
    private final PermissionService permissionService;
    private final SaleAuditEventRepository auditRepository;
    private final StockService stockService;
    private final FidelityService fidelityService;
    private final CompanyProductRepository companyProductRepository;

    @Transactional
    public Sale addItemByInternalCode(String sessionToken, String internalCode) {
        return addItemByInternalCode(sessionToken, internalCode, 1);
    }

    @Transactional
    public Sale addItemByInternalCode(String sessionToken, String internalCode, int quantity) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Sale sale = getOrCreateSale(sessionToken);
        UUID tenantId = TenantContextHolder.getTenantId();

        Product product = productRepository.findByInternalCodeAndTenantId(internalCode, tenantId)
                .orElseThrow(
                        () -> new NotFoundException("Produto não encontrado com o código interno: " + internalCode));

        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(sale.getCompanyId(), product.getId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado na empresa"));

        validateStock(product, sale, quantity);
        sale.addItem(product, cp.getPrice(), quantity);

        return saleRepository.save(sale);
    }

    @Transactional
    public Sale addItemByBarCode(String sessionToken, String barcode) {
        return addItemByBarCode(sessionToken, barcode, 1);
    }

    @Transactional
    public Sale addItemByBarCode(String sessionToken, String barcode, int quantity) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Sale sale = getOrCreateSale(sessionToken);
        UUID tenantId = TenantContextHolder.getTenantId();

        Product product = productRepository.findByBarcodeAndTenantId(barcode, tenantId)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado com o código de barras: " + barcode));

        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(sale.getCompanyId(), product.getId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado na empresa"));

        validateStock(product, sale, quantity);
        sale.addItem(product, cp.getPrice(), quantity);

        return saleRepository.save(sale);
    }

    @Transactional
    public void removeItemByInternalCode(String sessionToken, String internalCode, UUID operatorId) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Operator operator = findOperator(operatorId);

        if (!permissionService.canRemoveItens(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para remover itens. Solicite autorização de um supervisor.");
        }

        Sale sale = findActiveSale(sessionId);
        Product product = findProductByInternalCode(internalCode);
        int qty = sale.getItemQuantity(product);
        sale.removeItem(product);
        saleRepository.save(sale);
        if (qty > 0)
            auditRepository.save(SaleAuditEvent.forItemRemoval(sale, product, qty, operator, null));
    }

    @Transactional
    public void removeItemByBarCode(String sessionToken, String barCode, UUID operatorId) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Operator operator = findOperator(operatorId);

        if (!permissionService.canRemoveItens(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para remover itens. Solicite autorização de um supervisor.");
        }

        Sale sale = findActiveSale(sessionId);
        Product product = findProductByBarCode(barCode);
        int qty = sale.getItemQuantity(product);
        sale.removeItem(product);
        saleRepository.save(sale);
        if (qty > 0)
            auditRepository.save(SaleAuditEvent.forItemRemoval(sale, product, qty, operator, null));
    }

    @Transactional
    public void removeItemByInternalCodeWithAuthorization(
            String sessionToken,
            String internalCode,
            UUID operatorId,
            UUID authorizerId) {

        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Operator operator = findOperator(operatorId);
        Operator authorizer = findOperator(authorizerId);

        validateAuthorization(operator, authorizer);

        Sale sale = findActiveSale(sessionId);
        Product product = findProductByInternalCode(internalCode);
        int qty = sale.getItemQuantity(product);
        sale.removeItem(product);
        saleRepository.save(sale);
        if (qty > 0)
            auditRepository.save(SaleAuditEvent.forItemRemoval(sale, product, qty, operator, authorizer));
    }

    @Transactional
    public void removeItemByBarCodeWithAuthorization(
            String sessionToken,
            String barCode,
            UUID operatorId,
            UUID authorizerId) {

        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Operator operator = findOperator(operatorId);
        Operator authorizer = findOperator(authorizerId);

        validateAuthorization(operator, authorizer);

        Sale sale = findActiveSale(sessionId);
        Product product = findProductByBarCode(barCode);
        int qty = sale.getItemQuantity(product);
        sale.removeItem(product);
        saleRepository.save(sale);
        if (qty > 0)
            auditRepository.save(SaleAuditEvent.forItemRemoval(sale, product, qty, operator, authorizer));
    }

    private void validateAuthorization(Operator operator, Operator authorizer) {
        if (!permissionService.canAuthorizeRemoval(authorizer, operator)) {
            throw new ForbiddenOperationException(
                    "O operador autorizador não possui nível de permissão suficiente para autorizar esta operação.");
        }
    }

    private Operator findOperator(UUID operatorId) {
        return operatorRepository.findByIdAndCompanyId(operatorId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Operador não encontrado com o id: " + operatorId));
    }

    private Sale findActiveSale(UUID sessionId) {
        return saleRepository.findActiveSaleBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda em andamento para esta sessão"));
    }

    private Product findProductByBarCode(String barCode) {
        return productRepository.findByBarcodeAndTenantId(barCode, TenantContextHolder.getTenantId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado com o código de barras: " + barCode));
    }

    private Product findProductByInternalCode(String internalCode) {
        return productRepository.findByInternalCodeAndTenantId(internalCode, TenantContextHolder.getTenantId())
                .orElseThrow(
                        () -> new NotFoundException("Produto não encontrado com o código interno: " + internalCode));
    }

    @Transactional
    public Sale getOrCreateSale(String sessionToken) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();
        return saleRepository.findActiveSaleBySessionId(sessionId)
                .orElseGet(() -> createSaleForSession(sessionId));
    }

    private Sale createSaleForSession(UUID sessionId) {
        try {
            // saveAndFlush força a violação do índice único parcial
            // (uk_sale_active_per_session) aqui, e não no commit.
            return saleRepository.saveAndFlush(Sale.createForSession(sessionId));
        } catch (DataIntegrityViolationException e) {
            // Corrida: outra requisição criou a venda ativa entre o SELECT e o INSERT.
            // A transação já foi abortada pelo banco; o cliente deve rebuscar a venda atual.
            throw new IllegalStateException(
                    "Já existe uma venda ativa para esta sessão. Recarregue a venda atual.", e);
        }
    }

    @Transactional(readOnly = true)
    public Sale getCurrentSale(String sessionToken) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        // O índice único parcial uk_sale_active_per_session garante no máximo
        // uma venda não finalizada por sessão.
        return saleRepository.findCancellableSaleBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException(
                        "Nenhuma venda em andamento ou pendente de conclusão para esta sessão"));
    }

    @Transactional
    public Sale decrementItemByInternalCode(String sessionToken, String internalCode) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();
        Sale sale = findActiveSale(sessionId);
        Product product = findProductByInternalCode(internalCode);
        sale.doDecrementItem(product);
        return saleRepository.save(sale);
    }

    @Transactional
    public void completeSale(String sessionToken) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Sale sale = saleRepository.findPaidSaleBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda paga encontrada para esta sessão."));

        if (sale.getAmountDue().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Não é possível finalizar a venda: ainda há valores pendentes de pagamento.");
        }

        sale.completeSale();
        sale.setCompletedAt(java.time.LocalDateTime.now());
        deductStock(sale);
        if (sale.getClient() != null) {
            int pointsEarned = fidelityService.processCompletedSale(
                    sale.getClient().getId(), sale.getSubtotal(), sale.getFidelityDiscountApplied());
            sale.setPointsEarned(pointsEarned);
        }
        saleRepository.save(sale);
    }

    private void validateStock(Product product, Sale sale, int quantityToAdd) {
        stockService.requireAvailable(product, sale.getItemQuantity(product) + quantityToAdd, sale.getCompanyId());
    }

    private void deductStock(Sale sale) {
        sale.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .forEach(item -> stockService.deduct(item.getProduct(), item.getQuantity(), sale.getCompanyId()));
    }
}
