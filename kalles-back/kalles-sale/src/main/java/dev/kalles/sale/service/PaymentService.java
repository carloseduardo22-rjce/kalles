package dev.kalles.sale.service;

import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.sale.repository.SaleRepository;
import dev.kalles.sale.state.OpenState;
import dev.kalles.sale.strategy.PaymentFactory;
import dev.kalles.sale.strategy.PaymentResult;
import dev.kalles.sale.strategy.PaymentStrategy;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.shared.service.CheckoutSessionService;
import dev.kalles.shared.service.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SaleRepository saleRepository;
    private final PaymentFactory paymentFactory;
    private final CheckoutSessionService checkoutSessionService;

    @Transactional
    public Sale addPayment(String sessionToken, PaymentMethod method, BigDecimal amount) {
        Session session = checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Sale sale = findSaleForPaymentOrThrow(sessionToken);

        validatePayment(sale, session, method, amount);

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

    /**
     * Registra um pagamento ja capturado e confirmado por um provider externo
     * (ex.: terminal Mercado Pago/Stone via webhook). Nao executa strategy local
     * e e idempotente por transactionId, pois providers reenviam webhooks.
     */
    @Transactional
    public Sale registerExternalPayment(String sessionToken, PaymentMethod method, BigDecimal amount, String transactionId) {
        Session session = checkoutSessionService.getOpenSessionOrThrow(sessionToken);

        Sale sale = findSaleForPaymentOrThrow(sessionToken);

        if (transactionId != null && !transactionId.isBlank() && sale.getPayments().stream()
                .anyMatch(p -> transactionId.equals(p.getTransactionId()))) {
            return sale;
        }

        validatePayment(sale, session, method, amount);

        if (OpenState.NAME.equals(sale.getStateName())) {
            sale.startPayment();
        }

        if (amount.compareTo(sale.getAmountDue()) > 0) {
            throw new IllegalArgumentException("O valor do pagamento excede o saldo devedor da venda.");
        }

        Payment payment = new Payment(sale, method, amount, BigDecimal.ZERO, transactionId, true);
        sale.addPayment(payment);

        return saleRepository.save(sale);
    }

    private Sale findSaleForPaymentOrThrow(String sessionToken) {
        return saleRepository.findSaleForPaymentBySessionToken(sessionToken)
                .orElseThrow(() -> new NotFoundException("Nenhuma venda ativa encontrada para esta sessao."));
    }

    private void validatePayment(Sale sale, Session session, PaymentMethod method, BigDecimal amount) {
        if (sale.getItems().isEmpty()) {
            throw new IllegalStateException("Nao e possivel processar o pagamento: a venda nao possui itens.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }

        validatePaymentMethodAvailability(session, method);
    }

    private void validatePaymentMethodAvailability(Session session, PaymentMethod method) {
        if (method != PaymentMethod.CASH && !session.allowsElectronicPayments()) {
            throw new IllegalStateException(
                "Esta sessao foi aberta em modo somente dinheiro. PIX, vouchers e cartoes estao indisponiveis."
            );
        }
    }
}
