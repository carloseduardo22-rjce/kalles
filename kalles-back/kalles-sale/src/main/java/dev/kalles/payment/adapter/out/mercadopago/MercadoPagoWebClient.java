package dev.kalles.payment.adapter.out.mercadopago;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
public class MercadoPagoWebClient {

    private final RestClient restClient;

    public MercadoPagoWebClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public ResponseEntity<String> exchange(HttpMethod method, String url, String body, Map<String, String> headers) {
        RestClient.RequestBodySpec request = restClient.method(method).uri(URI.create(url));
        applyHeaders(request, headers);

        RestClient.RequestHeadersSpec<?> requestSpec = body != null ? request.body(body) : request;

        ResponseEntity<String> response;
        try {
            response = requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, (failedRequest, failedResponse) -> {
                    })
                    .toEntity(String.class);
        } catch (RuntimeException e) {
            log.error("Falha de comunicacao com o Mercado Pago: {} {}", method, url, e);
            throw e;
        }

        if (response.getStatusCode().isError()) {
            log.warn("Mercado Pago respondeu {} para {} {}", response.getStatusCode().value(), method, url);
        } else {
            log.debug("Mercado Pago respondeu {} para {} {}", response.getStatusCode().value(), method, url);
        }

        return response;
    }

    private void applyHeaders(RestClient.RequestBodySpec request, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        headers.forEach(request::header);
    }
}
