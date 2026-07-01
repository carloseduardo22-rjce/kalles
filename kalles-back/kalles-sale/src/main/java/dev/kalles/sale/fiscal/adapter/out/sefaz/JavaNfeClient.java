package dev.kalles.sale.fiscal.adapter.out.sefaz;

public interface JavaNfeClient {
    JavaNfeAuthorizationResponse authorizeNfce(JavaNfeAuthorizationRequest request);
}
