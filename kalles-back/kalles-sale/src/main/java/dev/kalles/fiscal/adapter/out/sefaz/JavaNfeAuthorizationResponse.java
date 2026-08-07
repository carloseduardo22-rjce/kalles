package dev.kalles.fiscal.adapter.out.sefaz;

public record JavaNfeAuthorizationResponse(
        boolean authorized,
        String accessKey,
        String protocol,
        String rejectionReason,
        String authorizedXml
) {
}
