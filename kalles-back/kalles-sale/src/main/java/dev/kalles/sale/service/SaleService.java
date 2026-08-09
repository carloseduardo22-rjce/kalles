package dev.kalles.sale.service;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.cashregister.service.PermissionService;
import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.inventory.entity.Stock;
import dev.kalles.inventory.exception.InsufficientStockException;
import dev.kalles.inventory.repository.StockRepository;
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
    private final StockRepository stockRepository;
    private final FidelityService fidelityService;
    private final ClientRepository clientRepository;
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
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Operator operator = findOperator(operatorId);

        if (!permissionService.canRemoveItens(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para remover itens. Solicite autorização de um supervisor.");
        }

        Sale sale = findActiveSale(sessionToken);
        Product product = findProductByInternalCode(internalCode);
        int qty = sale.getItemQuantity(product);
        sale.removeItem(product);
        saleRepository.save(sale);
        if (qty > 0)
            auditRepository.save(SaleAuditEvent.forItemRemoval(sale, product, qty, operator, null));
    }

    @Transactional
    public void removeItemByBarCode(String sessionToken, String barCode, UUID operatorId) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Operator operator = findOperator(operatorId);

        if (!permissionService.canRemoveItens(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para remover itens. Solicite autorização de um supervisor.");
        }

        Sale sale = findActiveSale(sessionToken);
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

        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Operator operator = findOperator(operatorId);
        Operator authorizer = findOperator(authorizerId);

        validateAuthorization(operator, authorizer);

        Sale sale = findActiveSale(sessionToken);
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

        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Operator operator = findOperator(operatorId);
        Operator authorizer = findOperator(authorizerId);

        validateAuthorization(operator, authorizer);

        Sale sale = findActiveSale(sessionToken);
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
        return operatorRepository.findByIdAndCompanyId(operatorId, getCompanyId())
                .orElseThrow(() -> new NotFoundException("Operador não encontrado com o id: " + operatorId));
    }

    private Sale findActiveSale(String sessionToken) {
        return saleRepository.findActiveSaleBySessionToken(sessionToken)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda em andamento para esta sessão"));
    }

    private Sale findCancellableSale(String sessionToken) {
        return saleRepository.findCancellableSaleBySessionToken(sessionToken)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda cancelável para esta sessão"));
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
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);
        return saleRepository.findActiveSaleBySessionToken(sessionToken)
                .orElseGet(() -> createSaleForSession(sessionToken));
    }

    private Sale createSaleForSession(String sessionToken) {
        try {
            // saveAndFlush força a violação do índice único parcial
            // (uk_sale_active_per_session) aqui, e não no commit.
            return saleRepository.saveAndFlush(Sale.createForSession(sessionToken));
        } catch (DataIntegrityViolationException e) {
            // Corrida: outra requisição criou a venda ativa entre o SELECT e o INSERT.
            // A transação já foi abortada pelo banco; o cliente deve rebuscar a venda atual.
            throw new IllegalStateException(
                    "Já existe uma venda ativa para esta sessão. Recarregue a venda atual.", e);
        }
    }

    @Transactional(readOnly = true)
    public Sale getCurrentSale(String sessionToken) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        // O índice único parcial uk_sale_active_per_session garante no máximo
        // uma venda não finalizada por sessão.
        return saleRepository.findCancellableSaleBySessionToken(sessionToken)
                .orElseThrow(() -> new NotFoundException(
                        "Nenhuma venda em andamento ou pendente de conclusão para esta sessão"));
    }

    @Transactional
    public Sale associateClientWithSale(String sessionToken, UUID clientId) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);
        Sale sale = findActiveSale(sessionToken);
        Client client = clientRepository.findByIdAndCompanyId(clientId, sale.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado com o id: " + clientId));
        sale.setClient(client);
        return saleRepository.save(sale);
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
    }

    @Transactional
    public Sale applyFidelityDiscountToSale(String sessionToken) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);
        Sale sale = findActiveSale(sessionToken);
        if (sale.getClient() == null) {
            throw new IllegalStateException("Nenhum cliente associado à venda.");
        }
        // Apenas calcula e registra na venda; o saldo do cliente só é consumido
        // na conclusão (completeSale). Reaplicar recalcula sem perda.
        BigDecimal applied = fidelityService.calculateDiscount(sale.getClient().getId(), sale.getSubtotal());
        if (applied.compareTo(java.math.BigDecimal.ZERO) > 0) {
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

        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

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

        Sale sale = findActiveSale(sessionToken);
        sale.applyItemDiscount(itemId, discountAmount);
        saleRepository.save(sale);

        Product discountedProduct = sale.getItems().stream()
                .filter(item -> java.util.Objects.equals(item.getId(), itemId))
                .findFirst()
                .map(item -> item.getProduct())
                .orElse(null);
        auditRepository.save(
                SaleAuditEvent.forItemDiscount(sale, discountedProduct, discountAmount, operator, authorizer));
    }

    @Transactional
    public void cancelSale(String sessionToken, UUID operatorId) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Operator operator = findOperator(operatorId);

        if (!permissionService.canCancelSale(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para cancelar vendas. Solicite autorização de um supervisor.");
        }

        Sale sale = findCancellableSale(sessionToken);
        sale.cancel();
        // Fidelidade não precisa de estorno: saldo/pontos só são consumidos
        // na conclusão da venda, que não é um estado cancelável.
        saleRepository.save(sale);
        auditRepository.save(SaleAuditEvent.forCancellation(sale, operator, null));
    }

    @Transactional
    public void cancelSaleWithAuthorization(
            String sessionToken,
            UUID operatorId,
            UUID authorizerId) {

        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Operator operator = findOperator(operatorId);
        Operator authorizer = findOperator(authorizerId);

        validateCancellationAuthorization(operator, authorizer);

        Sale sale = findCancellableSale(sessionToken);
        sale.cancel();
        // Fidelidade não precisa de estorno: saldo/pontos só são consumidos
        // na conclusão da venda, que não é um estado cancelável.
        saleRepository.save(sale);
        auditRepository.save(SaleAuditEvent.forCancellation(sale, operator, authorizer));
    }

    private void validateCancellationAuthorization(Operator operator, Operator authorizer) {
        if (!permissionService.canAuthorizeCancellation(authorizer, operator)) {
            throw new ForbiddenOperationException(
                    "O operador autorizador não possui nível de permissão suficiente para autorizar o cancelamento.");
        }
    }

    @Transactional
    public Sale decrementItemByInternalCode(String sessionToken, String internalCode) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);
        Sale sale = findActiveSale(sessionToken);
        Product product = findProductByInternalCode(internalCode);
        sale.doDecrementItem(product);
        return saleRepository.save(sale);
    }

    @Transactional
    public void completeSale(String sessionToken) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Sale sale = saleRepository.findPaidSaleBySessionToken(sessionToken)
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
        int currentQtyInCart = sale.getItemQuantity(product);
        int totalStock = stockRepository.sumQuantityByProductId(product.getId(), sale.getCompanyId());
        if (totalStock < currentQtyInCart + quantityToAdd) {
            throw new InsufficientStockException(product.getName(), totalStock);
        }
    }

    private void deductStock(Sale sale) {
        sale.getItems().forEach(item -> {
            // Revalidate stock at the moment of deduction to prevent oversell
            int totalStock = stockRepository.sumQuantityByProductId(item.getProduct().getId(), sale.getCompanyId());
            if (totalStock < item.getQuantity()) {
                throw new InsufficientStockException(item.getProduct().getName(), totalStock);
            }

            int remaining = item.getQuantity();
            List<Stock> stocks = stockRepository.findAllByProductIdOrderByQuantityDesc(item.getProduct().getId(),
                    sale.getCompanyId());
            for (var stock : stocks) {
                if (remaining <= 0)
                    break;
                int deducted = Math.min(stock.getQuantity(), remaining);
                stock.setQuantity(stock.getQuantity() - deducted);
                remaining -= deducted;
                stockRepository.save(stock);
            }
        });
    }

}
