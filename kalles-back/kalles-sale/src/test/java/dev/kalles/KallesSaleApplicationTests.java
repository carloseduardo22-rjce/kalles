package dev.kalles;

import dev.kalles.payment.adapter.in.web.MercadoPagoWebhookController;
import dev.kalles.payment.config.MercadoPagoProperties;
import dev.kalles.payment.adapter.in.web.PaymentController;
import dev.kalles.payment.adapter.in.web.PaymentPointController;
import dev.kalles.payment.adapter.in.web.PaymentProviderAccountController;
import dev.kalles.payment.adapter.in.web.PaymentStoreController;
import dev.kalles.payment.adapter.in.web.PaymentTerminalController;
import dev.kalles.payment.application.port.in.ActivatePaymentTerminalUseCase;
import dev.kalles.payment.application.port.in.CancelPaymentUseCase;
import dev.kalles.payment.application.port.in.ClosePaymentOrderUseCase;
import dev.kalles.payment.application.port.in.CreatePaymentPointUseCase;
import dev.kalles.payment.application.port.in.CreatePaymentStoreUseCase;
import dev.kalles.payment.application.port.in.GetPaymentProviderAccountStatusUseCase;
import dev.kalles.payment.application.port.in.GetPaymentStoreStatusUseCase;
import dev.kalles.payment.application.port.in.GetPaymentUseCase;
import dev.kalles.payment.application.port.in.LinkPaymentProviderAccountUseCase;
import dev.kalles.payment.application.port.in.ListPaymentPointsUseCase;
import dev.kalles.payment.application.port.in.ListPaymentStoresUseCase;
import dev.kalles.payment.application.port.in.ListPaymentTerminalsUseCase;
import dev.kalles.payment.application.port.in.PrintPaymentDocumentUseCase;
import dev.kalles.payment.application.port.in.ProcessPaymentUseCase;
import dev.kalles.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.payment.application.port.in.RefundPaymentUseCase;
import dev.kalles.payment.application.service.PaymentProviderOAuthStateService;
import dev.kalles.security.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(classes = KallesSaleApplicationTests.TestConfiguration.class)
class KallesSaleApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext.getBeansOfType(PaymentController.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(PaymentProviderAccountController.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(PaymentStoreController.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(PaymentPointController.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(PaymentTerminalController.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(MercadoPagoWebhookController.class)).hasSize(1);
    }

    @Configuration
    static class TestConfiguration {

        private static final MercadoPagoProperties TEST_MERCADO_PAGO_PROPERTIES =
                new MercadoPagoProperties(null, null, null, null, null, null, "");

        @Bean
        ProcessPaymentUseCase processPaymentUseCase() {
            return mock(ProcessPaymentUseCase.class);
        }

        @Bean
        GetPaymentUseCase getPaymentUseCase() {
            return mock(GetPaymentUseCase.class);
        }

        @Bean
        CancelPaymentUseCase cancelPaymentUseCase() {
            return mock(CancelPaymentUseCase.class);
        }

        @Bean
        RefundPaymentUseCase refundPaymentUseCase() {
            return mock(RefundPaymentUseCase.class);
        }

        @Bean
        ClosePaymentOrderUseCase closePaymentOrderUseCase() {
            return mock(ClosePaymentOrderUseCase.class);
        }

        @Bean
        PrintPaymentDocumentUseCase printPaymentDocumentUseCase() {
            return mock(PrintPaymentDocumentUseCase.class);
        }

        @Bean
        LinkPaymentProviderAccountUseCase linkPaymentProviderAccountUseCase() {
            return mock(LinkPaymentProviderAccountUseCase.class);
        }

        @Bean
        GetPaymentProviderAccountStatusUseCase getPaymentProviderAccountStatusUseCase() {
            return mock(GetPaymentProviderAccountStatusUseCase.class);
        }

        @Bean
        PaymentProviderOAuthStateService paymentProviderOAuthStateService() {
            return mock(PaymentProviderOAuthStateService.class);
        }

        @Bean
        AccountRepository accountRepository() {
            return mock(AccountRepository.class);
        }

        @Bean
        CreatePaymentStoreUseCase createPaymentStoreUseCase() {
            return mock(CreatePaymentStoreUseCase.class);
        }

        @Bean
        GetPaymentStoreStatusUseCase getPaymentStoreStatusUseCase() {
            return mock(GetPaymentStoreStatusUseCase.class);
        }

        @Bean
        ListPaymentStoresUseCase listPaymentStoresUseCase() {
            return mock(ListPaymentStoresUseCase.class);
        }

        @Bean
        CreatePaymentPointUseCase createPaymentPointUseCase() {
            return mock(CreatePaymentPointUseCase.class);
        }

        @Bean
        ListPaymentPointsUseCase listPaymentPointsUseCase() {
            return mock(ListPaymentPointsUseCase.class);
        }

        @Bean
        ListPaymentTerminalsUseCase listPaymentTerminalsUseCase() {
            return mock(ListPaymentTerminalsUseCase.class);
        }

        @Bean
        ActivatePaymentTerminalUseCase activatePaymentTerminalUseCase() {
            return mock(ActivatePaymentTerminalUseCase.class);
        }

        @Bean
        ProcessPaymentWebhookUseCase processPaymentWebhookUseCase() {
            return mock(ProcessPaymentWebhookUseCase.class);
        }

        @Bean
        PaymentController paymentController(
                ProcessPaymentUseCase processPaymentUseCase,
                GetPaymentUseCase getPaymentUseCase,
                CancelPaymentUseCase cancelPaymentUseCase,
                ClosePaymentOrderUseCase closePaymentOrderUseCase,
                PrintPaymentDocumentUseCase printPaymentDocumentUseCase,
                RefundPaymentUseCase refundPaymentUseCase
        ) {
            return new PaymentController(
                    processPaymentUseCase,
                    getPaymentUseCase,
                    cancelPaymentUseCase,
                    closePaymentOrderUseCase,
                    printPaymentDocumentUseCase,
                    refundPaymentUseCase
            );
        }

        @Bean
        PaymentProviderAccountController paymentProviderAccountController(
                LinkPaymentProviderAccountUseCase linkPaymentProviderAccountUseCase,
                GetPaymentProviderAccountStatusUseCase getPaymentProviderAccountStatusUseCase,
                PaymentProviderOAuthStateService paymentProviderOAuthStateService,
                AccountRepository accountRepository
        ) {
            return new PaymentProviderAccountController(
                    linkPaymentProviderAccountUseCase,
                    getPaymentProviderAccountStatusUseCase,
                    paymentProviderOAuthStateService,
                    accountRepository,
                    TEST_MERCADO_PAGO_PROPERTIES
            );
        }

        @Bean
        PaymentStoreController paymentStoreController(
                CreatePaymentStoreUseCase createPaymentStoreUseCase,
                GetPaymentStoreStatusUseCase getPaymentStoreStatusUseCase,
                ListPaymentStoresUseCase listPaymentStoresUseCase
        ) {
            return new PaymentStoreController(
                    createPaymentStoreUseCase,
                    getPaymentStoreStatusUseCase,
                    listPaymentStoresUseCase
            );
        }

        @Bean
        PaymentPointController paymentPointController(
                CreatePaymentPointUseCase createPaymentPointUseCase,
                ListPaymentPointsUseCase listPaymentPointsUseCase
        ) {
            return new PaymentPointController(
                    createPaymentPointUseCase,
                    listPaymentPointsUseCase
            );
        }

        @Bean
        PaymentTerminalController paymentTerminalController(
                ListPaymentTerminalsUseCase listPaymentTerminalsUseCase,
                ActivatePaymentTerminalUseCase activatePaymentTerminalUseCase
        ) {
            return new PaymentTerminalController(
                    listPaymentTerminalsUseCase,
                    activatePaymentTerminalUseCase
            );
        }

        @Bean
        MercadoPagoWebhookController mercadoPagoWebhookController(
                ProcessPaymentWebhookUseCase processPaymentWebhookUseCase
        ) {
            return new MercadoPagoWebhookController(processPaymentWebhookUseCase, TEST_MERCADO_PAGO_PROPERTIES);
        }

    }
}
