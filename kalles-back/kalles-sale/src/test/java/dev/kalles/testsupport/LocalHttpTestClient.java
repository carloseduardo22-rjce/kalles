package dev.kalles.testsupport;

import io.restassured.response.Response;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public final class LocalHttpTestClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(TestHttpTimeout.CONNECT)
            .build();

    private LocalHttpTestClient() {
    }

    public static Response get(String url, Map<String, String> headers) {
        return send(url, "GET", headers, null);
    }

    public static Response delete(String url, Map<String, String> headers) {
        return send(url, "DELETE", headers, null);
    }

    public static Response put(String url, Map<String, String> headers, String body) {
        return send(url, "PUT", headers, body);
    }

    private static Response send(String url, String method, Map<String, String> headers, String body) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TestHttpTimeout.REQUEST);

            if ("DELETE".equalsIgnoreCase(method)) {
                requestBuilder.DELETE();
            } else if ("PUT".equalsIgnoreCase(method)) {
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
            } else {
                requestBuilder.GET();
            }

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
