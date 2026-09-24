package dev.kalles.sale.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kalles.fidelity.service.FidelityService;
import dev.kalles.inventory.service.StockService;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.shared.service.CheckoutSessionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleCompletionService {

    private final SaleRepository saleRepository;
    private final CheckoutSessionService checkoutSessionService;
    private final StockService stockService;
    private final FidelityService fidelityService;

    @Transactional
    public void completeSale(String sessionToken) {
        UUID sessionId = checkoutSessionService.getOpenSessionOrThrow(sessionToken).getId();

        Sale sale = saleRepository.findPaidSaleBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda paga encontrada para esta sessão."));

        if (sale.getAmountDue().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Não é possível finalizar a venda: ainda há valores pendentes de pagamento.");
        }

        sale.completeSale();
        sale.setCompletedAt(LocalDateTime.now());
        deductStock(sale);
        if (sale.getClient() != null) {
            int pointsEarned = fidelityService.processCompletedSale(
                    sale.getClient().getId(), sale.getSubtotal(), sale.getFidelityDiscountApplied());
            sale.setPointsEarned(pointsEarned);
        }
        saleRepository.save(sale);
    }

    private void deductStock(Sale sale) {
        sale.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .forEach(item -> stockService.deduct(item.getProduct(), item.getQuantity(), sale.getCompanyId()));
    }
}
