package dev.kalles.sale.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.strategy.CashPaymentStrategy;
import dev.kalles.sale.core.strategy.CreditCardPaymentStrategy;
import dev.kalles.sale.core.strategy.DebitCardPaymentStrategy;
import dev.kalles.sale.core.strategy.PaymentFactory;
import dev.kalles.sale.core.strategy.PaymentResult;
import dev.kalles.sale.core.strategy.PaymentStrategy;
import dev.kalles.sale.core.strategy.PixPaymentStrategy;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService - ServiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§o de Pagamento")
class PaymentServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PaymentFactory paymentFactory;

    @Mock
    private CheckoutSessionService checkoutSessionService;

    @InjectMocks
    private PaymentService paymentService;

    private static final String SESSION_TOKEN = "session-123";

    private Product product;
    private Sale sale;
    private Session session;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setInternalCode("PRD-001");
        product.setBarcode("7891234567890");



        sale = Sale.createForSession(SESSION_TOKEN);
        sale.setId(UUID.randomUUID());
        sale.addItem(product, new BigDecimal("50.00"));

        session = mock(Session.class);
        lenient().when(session.isOpen()).thenReturn(true);
        lenient().when(session.allowsElectronicPayments()).thenReturn(true);
    }

    @Nested
    @DisplayName("Scenario 1 - Single Cash Payment")
    class CashPayment {

        @Test
        @DisplayName("Should register cash payment for the exact sale amount and settle the balance")
        void shouldRegisterCashPaymentAndSettleBalance() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.CASH))
                    .thenReturn(new CashPaymentStrategy());
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("50.00"));

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmountDue()));
            assertEquals(1, result.getPayments().size());
            assertEquals(PaymentMethod.CASH, result.getPayments().stream().findFirst().orElseThrow().getMethod());
            assertTrue(result.getPayments().stream().findFirst().orElseThrow().isConfirmed());
            assertEquals("PAID", result.getStateName());
        }

        @Test
        @DisplayName("Should transition sale from OPEN to PAYMENT_IN_PROGRESS then to PAID")
        void shouldTransitionStatesToPaid() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.CASH))
                    .thenReturn(new CashPaymentStrategy());
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            assertEquals("OPEN", sale.getStateName());

            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("50.00"));

            assertEquals("PAID", result.getStateName());
        }
    }

    @Nested
    @DisplayName("Scenario 2 - Card and PIX Payments")
    class CardAndPixPayments {

        @Test
        @DisplayName("Should register credit card payment and update balance")
        void shouldRegisterCreditCardPayment() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.CREDIT_CARD))
                    .thenReturn(new CreditCardPaymentStrategy(true));
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CREDIT_CARD, new BigDecimal("30.00"));

            assertEquals(new BigDecimal("20.00"), result.getAmountDue());
            assertEquals(1, result.getPayments().size());
            assertEquals(PaymentMethod.CREDIT_CARD, result.getPayments().stream().findFirst().orElseThrow().getMethod());
            assertNotNull(result.getPayments().stream().findFirst().orElseThrow().getTransactionId());
            assertEquals("PAYMENT_IN_PROGRESS", result.getStateName());
        }

        @Test
        @DisplayName("Should register debit card payment and update balance")
        void shouldRegisterDebitCardPayment() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.DEBIT_CARD))
                    .thenReturn(new DebitCardPaymentStrategy(true));
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.DEBIT_CARD, new BigDecimal("50.00"));

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmountDue()));
            assertEquals("PAID", result.getStateName());
        }

        @Test
        @DisplayName("Should register PIX payment and update balance")
        void shouldRegisterPixPayment() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.PIX))
                    .thenReturn(new PixPaymentStrategy(true));
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.PIX, new BigDecimal("25.00"));

            assertEquals(new BigDecimal("25.00"), result.getAmountDue());
            assertEquals(1, result.getPayments().size());
            assertEquals(PaymentMethod.PIX, result.getPayments().stream().findFirst().orElseThrow().getMethod());
            assertNotNull(result.getPayments().stream().findFirst().orElseThrow().getTransactionId());
        }

        @Test
        @DisplayName("Should allow multiple partial payments with different methods")
        void shouldAllowMultiplePartialPayments() {
            sale.startPayment();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            when(paymentFactory.getStrategy(PaymentMethod.PIX)).thenReturn(new PixPaymentStrategy(true));
            paymentService.addPayment(SESSION_TOKEN, PaymentMethod.PIX, new BigDecimal("20.00"));
            assertEquals(new BigDecimal("30.00"), sale.getAmountDue());

            when(paymentFactory.getStrategy(PaymentMethod.CASH)).thenReturn(new CashPaymentStrategy());
            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("30.00"));

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmountDue()));
            assertEquals(2, result.getPayments().size());
            assertEquals("PAID", result.getStateName());
        }
    }

    @Nested
    @DisplayName("BR005 - Sale must contain items")
    class BR005Validation {

        @Test
        @DisplayName("Should reject payment when sale has no items")
        void shouldRejectPaymentWhenSaleHasNoItems() {
            Sale emptySale = Sale.createForSession(SESSION_TOKEN);
            emptySale.setId(UUID.randomUUID());

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(emptySale));

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("10.00"))
            );
            assertNotNull(exception.getMessage());
            verify(saleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("BR006 - Payment amount must be greater than zero")
    class BR006Validation {

        @Test
        @DisplayName("Should reject payment with zero amount")
        void shouldRejectZeroAmount() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, BigDecimal.ZERO)
            );

            assertTrue(exception.getMessage().contains("maior que zero"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject payment with negative amount")
        void shouldRejectNegativeAmount() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.PIX, new BigDecimal("-5.00"))
            );

            assertTrue(exception.getMessage().contains("maior que zero"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject payment with null amount")
        void shouldRejectNullAmount() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, null)
            );

            assertTrue(exception.getMessage().contains("maior que zero"));
            verify(saleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Additional Validations")
    class AdditionalValidations {

        @Test
        @DisplayName("Should reject CREDIT_CARD payment exceeding remaining balance")
        void shouldRejectNonCashPaymentExceedingBalance() {
            sale.startPayment();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CREDIT_CARD, new BigDecimal("100.00"))
            );

            assertTrue(exception.getMessage().contains("excede o saldo devedor"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject PIX payment exceeding remaining balance")
        void shouldRejectPixPaymentExceedingBalance() {
            sale.startPayment();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.PIX, new BigDecimal("100.00"))
            );

            assertTrue(exception.getMessage().contains("excede o saldo devedor"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject DEBIT_CARD payment exceeding remaining balance")
        void shouldRejectDebitCardPaymentExceedingBalance() {
            sale.startPayment();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.DEBIT_CARD, new BigDecimal("100.00"))
            );

            assertTrue(exception.getMessage().contains("excede o saldo devedor"));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when no active sale exists")
        void shouldThrowWhenNoActiveSale() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("10.00"))
            );
        }
    }

    @Nested
    @DisplayName("PaymentFactory Tests")
    class PaymentFactoryTests {

        @Test
        @DisplayName("Should use the correct strategy from the factory")
        void shouldUseCorrectStrategy() {
            PaymentStrategy mockStrategy = mock(PaymentStrategy.class);
            when(mockStrategy.process(any())).thenReturn(PaymentResult.confirmed("Mocked payment"));

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.PIX)).thenReturn(mockStrategy);
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            paymentService.addPayment(SESSION_TOKEN, PaymentMethod.PIX, new BigDecimal("50.00"));

            verify(paymentFactory).getStrategy(PaymentMethod.PIX);
            verify(mockStrategy).process(new BigDecimal("50.00"));
        }
    }

    @Nested
    @DisplayName("BR007 - Cash change (troco)")
    class CashChangeTests {

        @Test
        @DisplayName("Should allow cash payment exceeding remaining balance and calculate change")
        void shouldAllowCashOverpaymentWithChange() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.CASH)).thenReturn(new CashPaymentStrategy());
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("80.00"));

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmountDue()));
            assertEquals(1, result.getPayments().size());
            assertEquals(new BigDecimal("80.00"), result.getPayments().stream().findFirst().orElseThrow().getAmount());
            assertEquals(new BigDecimal("30.00"), result.getPayments().stream().findFirst().orElseThrow().getChangeAmount());
            assertEquals("PAID", result.getStateName());
        }

        @Test
        @DisplayName("Should return zero change when cash payment equals the balance")
        void shouldReturnZeroChangeWhenExactCash() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(paymentFactory.getStrategy(PaymentMethod.CASH)).thenReturn(new CashPaymentStrategy());
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("50.00"));

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmountDue()));
            assertEquals(0, BigDecimal.ZERO.compareTo(result.getPayments().stream().findFirst().orElseThrow().getChangeAmount()));
            assertEquals("PAID", result.getStateName());
        }

        @Test
        @DisplayName("Should calculate change correctly after partial PIX payment followed by cash overpayment")
        void shouldCalculateChangeAfterPartialPixThenCashOverpayment() {
            sale.startPayment();

            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            when(paymentFactory.getStrategy(PaymentMethod.PIX)).thenReturn(new PixPaymentStrategy(true));
            paymentService.addPayment(SESSION_TOKEN, PaymentMethod.PIX, new BigDecimal("10.00"));
            assertEquals(new BigDecimal("40.00"), sale.getAmountDue());

            when(paymentFactory.getStrategy(PaymentMethod.CASH)).thenReturn(new CashPaymentStrategy());
            Sale result = paymentService.addPayment(SESSION_TOKEN, PaymentMethod.CASH, new BigDecimal("50.00"));

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmountDue()));
            assertEquals(2, result.getPayments().size());
            assertEquals(new BigDecimal("10.00"), result.getPayments().stream().skip(1).findFirst().orElseThrow().getChangeAmount());
            assertEquals("PAID", result.getStateName());
        }

    }

    @Nested
    @DisplayName("BR008 - External payments (webhook de provider)")
    class ExternalPaymentTests {

        @Test
        @DisplayName("Should register externally confirmed payment without invoking local strategy")
        void shouldRegisterExternalPaymentWithoutStrategy() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            Sale result = paymentService.registerExternalPayment(
                    SESSION_TOKEN, PaymentMethod.CREDIT_CARD, new BigDecimal("50.00"), "mp-payment-1");

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getAmountDue()));
            assertEquals(1, result.getPayments().size());
            assertEquals("mp-payment-1", result.getPayments().stream().findFirst().orElseThrow().getTransactionId());
            assertTrue(result.getPayments().stream().findFirst().orElseThrow().isConfirmed());
            assertEquals("PAID", result.getStateName());
            verifyNoInteractions(paymentFactory);
        }

        @Test
        @DisplayName("Should be idempotent when the same transactionId is registered twice")
        void shouldIgnoreDuplicateTransactionId() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            paymentService.registerExternalPayment(
                    SESSION_TOKEN, PaymentMethod.CREDIT_CARD, new BigDecimal("50.00"), "mp-payment-1");
            Sale result = paymentService.registerExternalPayment(
                    SESSION_TOKEN, PaymentMethod.CREDIT_CARD, new BigDecimal("50.00"), "mp-payment-1");

            assertEquals(1, result.getPayments().size());
            assertEquals("PAID", result.getStateName());
        }

        @Test
        @DisplayName("Should reject external payment exceeding the amount due")
        void shouldRejectExternalOverpayment() {
            when(checkoutSessionService.getOpenSessionOrThrow(SESSION_TOKEN)).thenReturn(session);
            when(saleRepository.findSaleForPaymentBySessionToken(SESSION_TOKEN)).thenReturn(Optional.of(sale));

            assertThrows(IllegalArgumentException.class, () -> paymentService.registerExternalPayment(
                    SESSION_TOKEN, PaymentMethod.CREDIT_CARD, new BigDecimal("80.00"), "mp-payment-1"));
        }
    }
}
