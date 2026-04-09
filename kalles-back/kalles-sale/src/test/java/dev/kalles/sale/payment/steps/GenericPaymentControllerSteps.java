package dev.kalles.sale.payment.steps;

import dev.kalles.sale.payment.adapter.in.web.PaymentController;
import dev.kalles.sale.payment.adapter.in.web.PaymentProviderAccountController;
import dev.kalles.sale.payment.adapter.in.web.PaymentStoreController;
import dev.kalles.sale.payment.adapter.in.web.dto.ClosePaymentOrderRequest;
import dev.kalles.sale.payment.adapter.in.web.dto.CreatePaymentStoreRequest;
import dev.kalles.sale.payment.adapter.in.web.dto.CreatePaymentStoreResponse;
import dev.kalles.sale.payment.adapter.in.web.dto.LinkPaymentProviderAccountRequest;
import dev.kalles.sale.payment.adapter.in.web.dto.PaymentResponse;
import dev.kalles.sale.payment.adapter.in.web.dto.PrintPaymentDocumentRequest;
import dev.kalles.sale.payment.adapter.in.web.dto.ProcessPaymentRequest;
import dev.kalles.sale.payment.application.port.in.CancelPaymentUseCase;
import dev.kalles.sale.payment.application.port.in.ClosePaymentOrderUseCase;
import dev.kalles.sale.payment.application.port.in.CreatePaymentStoreUseCase;
import dev.kalles.sale.payment.application.port.in.GetPaymentProviderAccountStatusUseCase;
import dev.kalles.sale.payment.application.port.in.GetPaymentStoreStatusUseCase;
import dev.kalles.sale.payment.application.port.in.GetPaymentUseCase;
import dev.kalles.sale.payment.application.port.in.LinkPaymentProviderAccountUseCase;
import dev.kalles.sale.payment.application.port.in.ListPaymentStoresUseCase;
import dev.kalles.sale.payment.application.port.in.PrintPaymentDocumentUseCase;
import dev.kalles.sale.payment.application.port.in.ProcessPaymentUseCase;
import dev.kalles.sale.payment.application.port.in.RefundPaymentUseCase;
import dev.kalles.sale.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.sale.payment.application.port.in.command.LinkPaymentProviderAccountCommand;
import dev.kalles.sale.payment.domain.PaymentCommand;
import dev.kalles.sale.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.sale.payment.domain.PaymentDocumentType;
import dev.kalles.sale.payment.domain.PaymentFlow;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentResult;
import dev.kalles.sale.payment.domain.PaymentStatus;
import dev.kalles.sale.payment.domain.PaymentStore;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class GenericPaymentControllerSteps {

    private final LinkPaymentProviderAccountUseCase linkPaymentProviderAccountUseCase =
            mock(LinkPaymentProviderAccountUseCase.class);
    private final GetPaymentProviderAccountStatusUseCase getPaymentProviderAccountStatusUseCase =
            mock(GetPaymentProviderAccountStatusUseCase.class);
    private final CreatePaymentStoreUseCase createPaymentStoreUseCase =
            mock(CreatePaymentStoreUseCase.class);
    private final GetPaymentStoreStatusUseCase getPaymentStoreStatusUseCase =
            mock(GetPaymentStoreStatusUseCase.class);
    private final ListPaymentStoresUseCase listPaymentStoresUseCase =
            mock(ListPaymentStoresUseCase.class);
    private final ProcessPaymentUseCase processPaymentUseCase =
            mock(ProcessPaymentUseCase.class);
    private final GetPaymentUseCase getPaymentUseCase =
            mock(GetPaymentUseCase.class);
    private final CancelPaymentUseCase cancelPaymentUseCase =
            mock(CancelPaymentUseCase.class);
    private final ClosePaymentOrderUseCase closePaymentOrderUseCase =
            mock(ClosePaymentOrderUseCase.class);
    private final PrintPaymentDocumentUseCase printPaymentDocumentUseCase =
            mock(PrintPaymentDocumentUseCase.class);
    private final RefundPaymentUseCase refundPaymentUseCase =
            mock(RefundPaymentUseCase.class);

    private final PaymentProviderAccountController paymentProviderAccountController =
            new PaymentProviderAccountController(
                    linkPaymentProviderAccountUseCase,
                    getPaymentProviderAccountStatusUseCase
            );
    private final PaymentStoreController paymentStoreController =
            new PaymentStoreController(
                    createPaymentStoreUseCase,
                    getPaymentStoreStatusUseCase,
                    listPaymentStoresUseCase
            );
    private final PaymentController paymentController =
            new PaymentController(
                    processPaymentUseCase,
                    getPaymentUseCase,
                    cancelPaymentUseCase,
                    closePaymentOrderUseCase,
                    printPaymentDocumentUseCase,
                    refundPaymentUseCase
            );

    private LinkPaymentProviderAccountRequest linkRequest;
    private CreatePaymentStoreRequest createStoreRequest;
    private ProcessPaymentRequest processPaymentRequest;
    private ClosePaymentOrderRequest closePaymentOrderRequest;
    private PrintPaymentDocumentRequest printPaymentDocumentRequest;

    private LinkPaymentProviderAccountCommand capturedLinkCommand;
    private CreatePaymentStoreCommand capturedCreateStoreCommand;
    private PaymentCommand capturedPaymentCommand;
    private PaymentProvider capturedCloseProvider;
    private String capturedCloseOrderId;
    private PaymentStatus capturedCloseStatus;
    private PaymentProvider capturedPrintProvider;
    private String capturedPrintOrderId;
    private PaymentDocumentPrintCommand capturedPrintCommand;
    private PaymentProvider currentProviderForClose;
    private String currentOrderIdForClose;
    private PaymentProvider currentProviderForPrint;
    private String currentOrderIdForPrint;

    private ResponseEntity<Void> linkResponse;
    private ResponseEntity<CreatePaymentStoreResponse> createStoreResponse;
    private ResponseEntity<PaymentResponse> paymentResponse;
    private ResponseEntity<Void> closePaymentResponse;
    private ResponseEntity<Void> printDocumentResponse;

    private UUID companyId;

    @Dado("uma solicitacao generica de vinculacao com provider {string}, authorizationCode {string}, state {string}, metadata {string} {string}")
    public void givenGenericLinkRequest(
            String provider,
            String authorizationCode,
            String state,
            String metadataKey,
            String metadataValue
    ) {
        reset(linkPaymentProviderAccountUseCase);
        capturedLinkCommand = null;
        linkRequest = new LinkPaymentProviderAccountRequest(
                PaymentProvider.valueOf(provider),
                authorizationCode,
                state,
                Map.of(metadataKey, metadataValue)
        );
        doAnswer(invocation -> {
            capturedLinkCommand = invocation.getArgument(0);
            return null;
        }).when(linkPaymentProviderAccountUseCase).execute(any(LinkPaymentProviderAccountCommand.class));
    }

    @Quando("o controller generico de vinculacao processar a solicitacao")
    public void whenLinkControllerProcessesRequest() {
        linkResponse = paymentProviderAccountController.linkAccount(linkRequest);
    }

    @Entao("o caso de uso de vinculacao deve receber o provider {string}")
    public void thenLinkUseCaseReceivesProvider(String provider) {
        assertThat(capturedLinkCommand).isNotNull();
        assertThat(capturedLinkCommand.provider()).isEqualTo(PaymentProvider.valueOf(provider));
    }

    @Entao("o caso de uso de vinculacao deve receber o authorizationCode {string}")
    public void thenLinkUseCaseReceivesAuthorizationCode(String authorizationCode) {
        assertThat(capturedLinkCommand).isNotNull();
        assertThat(capturedLinkCommand.authorizationCode()).isEqualTo(authorizationCode);
    }

    @Entao("o caso de uso de vinculacao deve receber o state {string}")
    public void thenLinkUseCaseReceivesState(String state) {
        assertThat(capturedLinkCommand).isNotNull();
        assertThat(capturedLinkCommand.state()).isEqualTo(state);
    }

    @Entao("o caso de uso de vinculacao deve receber metadata {string} {string}")
    public void thenLinkUseCaseReceivesMetadata(String key, String value) {
        assertThat(capturedLinkCommand).isNotNull();
        assertThat(capturedLinkCommand.metadata()).containsEntry(key, value);
    }

    @Entao("a resposta de vinculacao deve ter status HTTP {int}")
    public void thenLinkResponseHasHttpStatus(int httpStatus) {
        assertThat(linkResponse).isNotNull();
        assertThat(linkResponse.getStatusCode().value()).isEqualTo(httpStatus);
    }

    @Dado("uma solicitacao generica de loja com provider {string}, externalReference {string} e companyId {string}")
    public void givenGenericStoreRequest(String provider, String externalReference, String rawCompanyId) {
        reset(createPaymentStoreUseCase, getPaymentStoreStatusUseCase, listPaymentStoresUseCase);
        companyId = UUID.fromString(rawCompanyId);
        createStoreRequest = new CreatePaymentStoreRequest(
                PaymentProvider.valueOf(provider),
                externalReference,
                companyId,
                Map.of("channel", "erp")
        );
    }

    @Dado("o caso de uso generico de loja retornara o providerStoreId {string}")
    public void givenGenericStoreUseCaseResponse(String providerStoreId) {
        when(createPaymentStoreUseCase.execute(any(CreatePaymentStoreCommand.class)))
                .thenAnswer(invocation -> {
                    capturedCreateStoreCommand = invocation.getArgument(0);
                    return new PaymentStore(
                            UUID.randomUUID(),
                            companyId,
                            createStoreRequest.provider(),
                            createStoreRequest.externalReference(),
                            providerStoreId
                    );
                });
        when(getPaymentStoreStatusUseCase.findByExternalReference(any(PaymentProvider.class), any(String.class)))
                .thenReturn(Optional.empty());
        when(getPaymentStoreStatusUseCase.findCurrentTenant(any(PaymentProvider.class)))
                .thenReturn(Optional.empty());
    }

    @Quando("o controller generico de loja processar a solicitacao")
    public void whenStoreControllerProcessesRequest() {
        createStoreResponse = paymentStoreController.createStore(createStoreRequest);
    }

    @Entao("o caso de uso de loja deve receber o provider {string}")
    public void thenStoreUseCaseReceivesProvider(String provider) {
        assertThat(capturedCreateStoreCommand).isNotNull();
        assertThat(capturedCreateStoreCommand.provider()).isEqualTo(PaymentProvider.valueOf(provider));
    }

    @Entao("o caso de uso de loja deve receber o externalReference {string}")
    public void thenStoreUseCaseReceivesExternalReference(String externalReference) {
        assertThat(capturedCreateStoreCommand).isNotNull();
        assertThat(capturedCreateStoreCommand.externalReference()).isEqualTo(externalReference);
    }

    @Entao("a resposta de loja deve conter o provider {string}")
    public void thenStoreResponseContainsProvider(String provider) {
        assertThat(createStoreResponse).isNotNull();
        assertThat(createStoreResponse.getBody()).isNotNull();
        assertThat(createStoreResponse.getBody().provider()).isEqualTo(PaymentProvider.valueOf(provider));
    }

    @Entao("a resposta de loja deve conter o providerStoreId {string}")
    public void thenStoreResponseContainsProviderStoreId(String providerStoreId) {
        assertThat(createStoreResponse).isNotNull();
        assertThat(createStoreResponse.getBody()).isNotNull();
        assertThat(createStoreResponse.getBody().providerStoreId()).isEqualTo(providerStoreId);
    }

    @Dado("uma solicitacao generica de pagamento com provider {string}, flow {string}, externalReference {string}, amount {string}, targetId {string}, metadata {string} {string}")
    public void givenGenericPaymentRequest(
            String provider,
            String flow,
            String externalReference,
            String amount,
            String targetId,
            String metadataKey,
            String metadataValue
    ) {
        reset(processPaymentUseCase, getPaymentUseCase, cancelPaymentUseCase, closePaymentOrderUseCase, printPaymentDocumentUseCase, refundPaymentUseCase);
        processPaymentRequest = new ProcessPaymentRequest(
                PaymentProvider.valueOf(provider),
                PaymentFlow.valueOf(flow),
                externalReference,
                new BigDecimal(amount),
                targetId,
                null,
                "Pagamento generico",
                PaymentMethodType.CREDIT_CARD,
                Map.of(metadataKey, metadataValue)
        );
    }

    @Dado("o caso de uso generico de pagamento retornara orderId {string}, status {string}, metadata {string} {string}")
    public void givenGenericPaymentUseCaseResponse(
            String orderId,
            String status,
            String metadataKey,
            String metadataValue
    ) {
        when(processPaymentUseCase.execute(any(PaymentCommand.class)))
                .thenAnswer(invocation -> {
                    capturedPaymentCommand = invocation.getArgument(0);
                    return new PaymentResult(
                            orderId,
                            null,
                            PaymentStatus.valueOf(status),
                            Map.of(metadataKey, metadataValue)
                    );
                });
    }

    @Quando("o controller generico de pagamentos processar a solicitacao")
    public void whenPaymentControllerProcessesRequest() {
        paymentResponse = paymentController.process(processPaymentRequest);
    }

    @Entao("o caso de uso de pagamento deve receber o provider {string}")
    public void thenPaymentUseCaseReceivesProvider(String provider) {
        assertThat(capturedPaymentCommand).isNotNull();
        assertThat(capturedPaymentCommand.provider()).isEqualTo(PaymentProvider.valueOf(provider));
    }

    @Entao("o caso de uso de pagamento deve receber o flow {string}")
    public void thenPaymentUseCaseReceivesFlow(String flow) {
        assertThat(capturedPaymentCommand).isNotNull();
        assertThat(capturedPaymentCommand.flow()).isEqualTo(PaymentFlow.valueOf(flow));
    }

    @Entao("o caso de uso de pagamento deve receber o targetId {string}")
    public void thenPaymentUseCaseReceivesTargetId(String targetId) {
        assertThat(capturedPaymentCommand).isNotNull();
        assertThat(capturedPaymentCommand.targetId()).isEqualTo(targetId);
    }

    @Entao("o caso de uso de pagamento deve receber metadata {string} {string}")
    public void thenPaymentUseCaseReceivesMetadata(String key, String value) {
        assertThat(capturedPaymentCommand).isNotNull();
        assertThat(capturedPaymentCommand.metadata()).containsEntry(key, value);
    }

    @Entao("a resposta de pagamento deve conter o provider {string}")
    public void thenPaymentResponseContainsProvider(String provider) {
        assertThat(paymentResponse).isNotNull();
        assertThat(paymentResponse.getBody()).isNotNull();
        assertThat(paymentResponse.getBody().provider()).isEqualTo(PaymentProvider.valueOf(provider));
    }

    @Entao("a resposta de pagamento deve conter o orderId {string}")
    public void thenPaymentResponseContainsOrderId(String orderId) {
        assertThat(paymentResponse).isNotNull();
        assertThat(paymentResponse.getBody()).isNotNull();
        assertThat(paymentResponse.getBody().providerOrderId()).isEqualTo(orderId);
    }

    @Entao("a resposta de pagamento deve conter o status {string}")
    public void thenPaymentResponseContainsStatus(String status) {
        assertThat(paymentResponse).isNotNull();
        assertThat(paymentResponse.getBody()).isNotNull();
        assertThat(paymentResponse.getBody().status()).isEqualTo(PaymentStatus.valueOf(status));
    }

    @Entao("a resposta de pagamento deve conter metadata {string} {string}")
    public void thenPaymentResponseContainsMetadata(String key, String value) {
        assertThat(paymentResponse).isNotNull();
        assertThat(paymentResponse.getBody()).isNotNull();
        assertThat(paymentResponse.getBody().metadata()).containsEntry(key, value);
    }

    @Dado("um pedido generico existente com provider {string} e orderId {string}")
    public void givenExistingGenericOrder(String provider, String orderId) {
        reset(closePaymentOrderUseCase);
        capturedCloseProvider = null;
        capturedCloseOrderId = null;
        capturedCloseStatus = null;
        currentProviderForClose = PaymentProvider.valueOf(provider);
        currentOrderIdForClose = orderId;
        doAnswer(invocation -> {
            capturedCloseProvider = invocation.getArgument(0);
            capturedCloseOrderId = invocation.getArgument(1);
            capturedCloseStatus = invocation.getArgument(2);
            return null;
        }).when(closePaymentOrderUseCase).execute(any(PaymentProvider.class), any(String.class), any(PaymentStatus.class));
    }

    @Quando("o controller generico de pagamentos solicitar o fechamento com status {string}")
    public void whenPaymentControllerClosesOrder(String status) {
        closePaymentOrderRequest = new ClosePaymentOrderRequest(PaymentStatus.valueOf(status));
        closePaymentResponse = paymentController.closePaymentOrder(currentProviderForClose, currentOrderIdForClose, closePaymentOrderRequest);
    }

    @Entao("o caso de uso de fechamento deve receber o provider {string}")
    public void thenCloseUseCaseReceivesProvider(String provider) {
        assertThat(capturedCloseProvider).isEqualTo(PaymentProvider.valueOf(provider));
    }

    @Entao("o caso de uso de fechamento deve receber o orderId {string}")
    public void thenCloseUseCaseReceivesOrderId(String orderId) {
        assertThat(capturedCloseOrderId).isEqualTo(orderId);
    }

    @Entao("o caso de uso de fechamento deve receber o status {string}")
    public void thenCloseUseCaseReceivesStatus(String status) {
        assertThat(capturedCloseStatus).isEqualTo(PaymentStatus.valueOf(status));
    }

    @Entao("a resposta de fechamento generico deve ter status HTTP {int}")
    public void thenCloseResponseHasStatus(int httpStatus) {
        assertThat(closePaymentResponse.getStatusCode().value()).isEqualTo(httpStatus);
    }

    @Dado("uma solicitacao generica de impressao com provider {string}, orderId {string}, type {string}, sizeVertical {int}, sizeHorizontal {int}, format {string} e content {string}")
    public void givenGenericPrintRequest(
            String provider,
            String orderId,
            String type,
            int sizeVertical,
            int sizeHorizontal,
            String format,
            String content
    ) {
        reset(printPaymentDocumentUseCase);
        capturedPrintProvider = null;
        capturedPrintOrderId = null;
        capturedPrintCommand = null;
        currentProviderForPrint = PaymentProvider.valueOf(provider);
        currentOrderIdForPrint = orderId;
        printPaymentDocumentRequest = new PrintPaymentDocumentRequest(
                PaymentDocumentType.valueOf(type),
                sizeVertical,
                sizeHorizontal,
                format,
                content,
                Map.of("channel", "erp")
        );
        doAnswer(invocation -> {
            capturedPrintProvider = invocation.getArgument(0);
            capturedPrintOrderId = invocation.getArgument(1);
            capturedPrintCommand = invocation.getArgument(2);
            return null;
        }).when(printPaymentDocumentUseCase).execute(any(PaymentProvider.class), any(String.class), any(PaymentDocumentPrintCommand.class));
    }

    @Quando("o controller generico de pagamentos solicitar a impressao do documento")
    public void whenPaymentControllerPrintsDocument() {
        printDocumentResponse = paymentController.printDocument(currentProviderForPrint, currentOrderIdForPrint, printPaymentDocumentRequest);
    }

    @Entao("o caso de uso de impressao deve receber o provider {string}")
    public void thenPrintUseCaseReceivesProvider(String provider) {
        assertThat(capturedPrintProvider).isEqualTo(PaymentProvider.valueOf(provider));
    }

    @Entao("o caso de uso de impressao deve receber o orderId {string}")
    public void thenPrintUseCaseReceivesOrderId(String orderId) {
        assertThat(capturedPrintOrderId).isEqualTo(orderId);
    }

    @Entao("o caso de uso de impressao deve receber o type {string}")
    public void thenPrintUseCaseReceivesType(String type) {
        assertThat(capturedPrintCommand).isNotNull();
        assertThat(capturedPrintCommand.type()).isEqualTo(PaymentDocumentType.valueOf(type));
    }

    @Entao("a resposta de impressao generica deve ter status HTTP {int}")
    public void thenPrintResponseHasStatus(int httpStatus) {
        assertThat(printDocumentResponse.getStatusCode().value()).isEqualTo(httpStatus);
    }
}
