package dev.kalles.sale.payment.support;

import io.restassured.response.Response;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class LocalHttpTestClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private LocalHttpTestClient() {
    }

    public static Response get(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10));

            headers.forEach(requestBuilder::header);

            HttpResponse<String> response = HTTP_CLIENT.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return RestAssuredResponseAdapter.from(response.statusCode(), response.headers().map(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Fail to execute local HTTP request for tests", e);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to execute local HTTP request for tests", e);
        }
    }
}
