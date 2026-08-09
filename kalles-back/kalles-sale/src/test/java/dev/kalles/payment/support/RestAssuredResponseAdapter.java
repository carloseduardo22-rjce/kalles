package dev.kalles.payment.support;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RestAssuredResponseAdapter {

    private RestAssuredResponseAdapter() {
    }

    public static Response from(ResponseEntity<String> entity) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        entity.getHeaders().headerSet().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
        return from(entity.getStatusCode().value(), headers, entity.getBody());
    }

    public static Response from(int statusCode, Map<String, ? extends List<String>> headersMap, String body) {
        ResponseBuilder builder = new ResponseBuilder();
        builder.setStatusCode(statusCode);
        builder.setBody(body == null ? "" : body);

        List<Header> headers = new ArrayList<>();
        headersMap.forEach((name, values) -> values.forEach(value -> headers.add(new Header(name, value))));
        builder.setHeaders(new Headers(headers));

        List<String> contentType = headersMap.get("Content-Type");
        if (contentType != null && !contentType.isEmpty()) {
            builder.setContentType(contentType.getFirst());
        }

        return builder.build();
    }
}
