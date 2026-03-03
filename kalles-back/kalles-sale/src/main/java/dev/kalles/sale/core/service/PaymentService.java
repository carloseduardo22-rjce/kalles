package dev.kalles.sale.core.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kalles.sale.core.entity.Payment;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.state.OpenState;
import dev.kalles.sale.core.strategy.PaymentFactory;
import dev.kalles.sale.core.strategy.PaymentResult;
import dev.kalles.sale.core.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SaleRepository saleRepository;
    private final PaymentFactory paymentFactory;
    private final CheckoutSessionService checkoutSessionService;

    @Transactional
    public Sale addPayment(String sessionToken, PaymentMethod method, BigDecimal amount) {
        checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Sale sale = saleRepository.findSaleForPaymentBySessionToken(sessionToken)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda ativa encontrada para esta sessão."));

        if (sale.getItems().isEmpty()) {
            throw new IllegalStateException("Não é possível processar o pagamento: a venda não possui itens.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }

        if (OpenState.NAME.equals(sale.getStateName())) {
            sale.startPayment();
        }

        if (amount.compareTo(sale.getAmountDue()) > 0 && method != PaymentMethod.CASH) {
            throw new IllegalArgumentException("O valor do pagamento excede o saldo devedor da venda.");
        }

        BigDecimal changeAmount = BigDecimal.ZERO;
        if (method == PaymentMethod.CASH && amount.compareTo(sale.getAmountDue()) > 0) {
            changeAmount = amount.subtract(sale.getAmountDue());
        }

        PaymentStrategy strategy = paymentFactory.getStrategy(method);
        PaymentResult result = strategy.process(amount);

        Payment payment = new Payment(sale, method, amount, changeAmount, result.transactionId(), result.confirmed());
        sale.addPayment(payment);

        return saleRepository.save(sale);
    }
}
