package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.Tenant;
import dev.kalles.sale.mercadopago.port.TenantRepository;
import dev.kalles.sale.mercadopago.port.MercadoPagoOAuthPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LinkMercadoPagoAccountUseCase {

    private final MercadoPagoOAuthPort oAuthPort;
    private final TenantRepository tenantRepository;

    public LinkMercadoPagoAccountUseCase(MercadoPagoOAuthPort oAuthPort, TenantRepository tenantRepository) {
        this.oAuthPort = oAuthPort;
        this.tenantRepository = tenantRepository;
    }

    public void execute(String authorizationCode, String tenantIdString) {
        // 1. Exchange auth code for tokens via MP API
        var tokenResponse = oAuthPort.exchangeCodeForToken(authorizationCode);

        // 2. Load tenant to link credentials
        UUID tenantId = UUID.fromString(tenantIdString);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + tenantIdString));

        // 3. Update tenant credentials
        Tenant updatedTenant = tenant.withOAuthCredentials(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                String.valueOf(tokenResponse.userId())
        );

        // 4. Save
        tenantRepository.save(updatedTenant);
    }
}