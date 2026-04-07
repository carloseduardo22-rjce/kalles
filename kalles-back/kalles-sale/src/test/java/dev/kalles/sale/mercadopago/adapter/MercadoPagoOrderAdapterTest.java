package dev.kalles.sale.mercadopago.adapter;

import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoOrderAdapterTest {

    @Mock private HttpClient httpClient;
    @Mock private CaixaMpRepository caixaMpRepository;

    private MercadoPagoOrderAdapter adapter;
    private CobrancaQr cobranca;

    @BeforeEach
    void setUp() {
        adapter = new MercadoPagoOrderAdapter("mock-token", httpClient, caixaMpRepository);
        cobranca = new CobrancaQr("CAIXA-123", new BigDecimal("150.50"), "ORDER-444", "idem-456");
    }

    @Test
    void shouldFormatPayloadAndReturnQrData() throws Exception {
        when(caixaMpRepository.findByExternalId(anyString()))
                .thenReturn(Optional.of(new Caixa(UUID.randomUUID(), "CAIXA-123", UUID.randomUUID(), 9999L)));

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"id\":\"MP-ORD-111\",\"type_response\":{\"qr_data\":\"000201010\"}}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);

        ResultadoQr result = adapter.createOrder(cobranca);
        assertEquals("MP-ORD-111", result.orderId());
        assertEquals("000201010", result.qrData());
    }

    @Test
    void throwIfCaixaNotFound() {
        when(caixaMpRepository.findByExternalId(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> adapter.createOrder(cobranca));
    }

    @Test
    void throwIfCaixaHasNoPos() {
        when(caixaMpRepository.findByExternalId(anyString()))
                .thenReturn(Optional.of(new Caixa(UUID.randomUUID(), "CAIXA-123", UUID.randomUUID(), null)));
        assertThrows(IllegalStateException.class, () -> adapter.createOrder(cobranca));
    }

    @Test
    void mapHttpErrors() throws Exception {
        when(caixaMpRepository.findByExternalId(anyString()))
                .thenReturn(Optional.of(new Caixa(UUID.randomUUID(), "CAIXA-123", UUID.randomUUID(), 9999L)));

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("{\"message\":\"bad_request\"}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);

        assertThrows(MercadoPagoIntegrationException.class, () -> adapter.createOrder(cobranca));
    }
}
