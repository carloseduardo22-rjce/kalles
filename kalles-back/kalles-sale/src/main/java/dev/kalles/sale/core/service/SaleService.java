package dev.kalles.sale.core.service;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import dev.kalles.sale.core.state.SaleState;

import org.springframework.stereotype.Service;

import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.core.entity.Client;
import dev.kalles.sale.core.entity.CompanyProduct;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.entity.SaleAuditEvent;
import dev.kalles.sale.core.entity.Stock;
import dev.kalles.sale.core.exception.ForbiddenOperationException;
import dev.kalles.sale.core.exception.InsufficientStockException;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.ClientRepository;
import dev.kalles.sale.core.repository.CompanyProductRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.SaleAuditEventRepository;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.repository.StockRepository;
import dev.kalles.sale.core.state.CompletedState;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
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
                .orElseGet(() -> {
                    Sale newSale = Sale.createForSession(sessionToken);
                    return saleRepository.save(newSale);
                });
    }

    @Transactional(readOnly = true)
    public Sale getCurrentSale(String sessionToken) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        List<SaleState> activeStates = List.of(
                new dev.kalles.sale.core.state.OpenState(),
                new dev.kalles.sale.core.state.OnHoldState(),
                new dev.kalles.sale.core.state.PaymentInProgressState(),
                new dev.kalles.sale.core.state.PaidState());

        List<Sale> sales = saleRepository.findAllBySessionTokenAndStateIn(sessionToken, activeStates);

        if (sales.isEmpty()) {
            throw new NotFoundException("Nenhuma venda em andamento ou pendente de conclusão para esta sessão");
        }

        // Em casos de testes não concluídos corretamente, pode haver múltiplas vendas
        // PAID. Retornamos a última encontrada.
        return sales.get(sales.size() - 1);
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
        BigDecimal applied = fidelityService.applyDiscount(sale.getClient().getId(), sale.getSubtotal());
        if (applied.compareTo(java.math.BigDecimal.ZERO) > 0) {
            sale.applyFidelityDiscount(applied);
            saleRepository.save(sale);
        }
        return sale;
    }



    @Transactional
    public void applyItemDiscount(String sessionToken, UUID itemId, BigDecimal discountAmount) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);
        Sale sale = findActiveSale(sessionToken);
        sale.applyItemDiscount(itemId, discountAmount);
        saleRepository.save(sale);
    }

    @Transactional
    public void cancelSale(String sessionToken, UUID operatorId) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Operator operator = findOperator(operatorId);

        if (!permissionService.canCancelSale(operator)) {
            throw new ForbiddenOperationException(
                    "Operador não possui permissão para cancelar vendas. Solicite autorização de um supervisor.");
        }

        Sale sale = findActiveSale(sessionToken);
        boolean stockWasDeducted = sale.getStateName().equals(CompletedState.NAME);
        sale.cancel();
        if (stockWasDeducted) {
            restoreStock(sale);
        }
        if (sale.getClient() != null) {
            fidelityService.rollbackSale(
                    sale.getClient().getId(),
                    sale.getFidelityDiscountApplied(),
                    sale.getPointsEarned(),
                    sale.getSubtotal());
        }
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

        Sale sale = findActiveSale(sessionToken);
        boolean stockWasDeducted = sale.getStateName().equals(CompletedState.NAME);
        sale.cancel();
        if (stockWasDeducted) {
            restoreStock(sale);
        }
        if (sale.getClient() != null) {
            fidelityService.rollbackSale(
                    sale.getClient().getId(),
                    sale.getFidelityDiscountApplied(),
                    sale.getPointsEarned(),
                    sale.getSubtotal());
        }
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
        deductStock(sale);
        if (sale.getClient() != null) {
            int pointsEarned = fidelityService.processCompletedSale(sale.getClient().getId(), sale.getSubtotal());
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

    private void restoreStock(Sale sale) {
        sale.getItems().forEach(item -> {
            List<Stock> stocks = stockRepository.findAllByProductIdOrderByQuantityDesc(item.getProduct().getId(),
                    sale.getCompanyId());

            if (!stocks.isEmpty()) {
                var stock = stocks.get(0);
                stock.setQuantity(stock.getQuantity() + item.getQuantity());

                stockRepository.save(stock);
            }
        });
    }
}
