package dev.kalles.sale.fiscal.domain;

public record SefazAuthorizationResult(
        boolean authorized,
        String accessKey,
        String authorizationProtocol,
        String rejectionReason,
        String authorizedXml
) {
    public static SefazAuthorizationResult authorized(String accessKey, String authorizationProtocol) {
        return authorized(accessKey, authorizationProtocol, null);
    }

    public static SefazAuthorizationResult authorized(String accessKey, String authorizationProtocol, String authorizedXml) {
        return new SefazAuthorizationResult(true, accessKey, authorizationProtocol, null, authorizedXml);
    }

    public static SefazAuthorizationResult rejected(String rejectionReason) {
        return new SefazAuthorizationResult(false, null, null, rejectionReason, null);
    }
}
