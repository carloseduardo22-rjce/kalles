package dev.kalles.sale.fiscal.adapter.out.sefaz;

import org.springframework.stereotype.Component;

@Component
public class LocalJavaNfeClient implements JavaNfeClient {

    @Override
    public JavaNfeAuthorizationResponse authorizeNfce(JavaNfeAuthorizationRequest request) {
        String accessKey = "NFCe-HOM-" + Integer.toUnsignedString(request.xml().hashCode());
        String protocol = "HOM-" + System.currentTimeMillis();
        return new JavaNfeAuthorizationResponse(true, accessKey, protocol, null, request.xml());
    }
}
