package dev.kalles.payment.adapter.out.mercadopago;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class MercadoPagoWebClient {

    private final WebClient webClient;

    public MercadoPagoWebClient() {
        this.webClient = WebClient.builder().build();
    }

    public ResponseEntity<String> exchange(HttpMethod method, String url, String body, Map<String, String> headers) {
        WebClient.RequestBodySpec request = webClient.method(method).uri(url);
        request.headers(httpHeaders -> applyHeaders(httpHeaders, headers));

        WebClient.RequestHeadersSpec<?> requestSpec = body != null ? request.bodyValue(body) : request;

        ResponseEntity<String> response;
        try {
            response = requestSpec.exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)).block();
        } catch (RuntimeException e) {
            log.error("Falha de comunicacao com o Mercado Pago: {} {}", method, url, e);
            throw e;
        }

        if (response == null) {
            log.error("Mercado Pago nao devolveu resposta: {} {}", method, url);
        } else if (response.getStatusCode().isError()) {
            log.warn("Mercado Pago respondeu {} para {} {}", response.getStatusCode().value(), method, url);
        } else {
            log.debug("Mercado Pago respondeu {} para {} {}", response.getStatusCode().value(), method, url);
        }

        return Objects.requireNonNull(response, "Mercado Pago response must not be null");
    }

    private void applyHeaders(HttpHeaders target, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        headers.forEach(target::add);
    }
}
