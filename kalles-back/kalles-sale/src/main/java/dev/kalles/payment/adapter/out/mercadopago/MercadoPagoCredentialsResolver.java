package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.application.port.out.PaymentAccountRepository;
import dev.kalles.payment.config.MercadoPagoProperties;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.security.context.TenantContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MercadoPagoCredentialsResolver {

    private final PaymentAccountRepository paymentAccountRepository;
    private final String fallbackAccessToken;
    private final String fallbackUserId;

    public MercadoPagoCredentialsResolver(
            PaymentAccountRepository paymentAccountRepository,
            MercadoPagoProperties mercadoPagoProperties
    ) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.fallbackAccessToken = mercadoPagoProperties.accessToken();
        this.fallbackUserId = mercadoPagoProperties.userId();
    }

    public String fallbackAccessToken() {
        return fallbackAccessToken;
    }

    public String fallbackUserId() {
        return fallbackUserId;
    }

    public String linkedAccessToken() {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return paymentAccountRepository.findByTenantIdAndProvider(tenantId, PaymentProvider.MERCADO_PAGO)
                .map(account -> account.accessToken())
                .orElse(null);
    }

    public String linkedUserId() {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return paymentAccountRepository.findByTenantIdAndProvider(tenantId, PaymentProvider.MERCADO_PAGO)
                .map(account -> account.providerAccountId())
                .orElse(null);
    }

    public String linkedAccessTokenOrThrow() {
        String token = linkedAccessToken();
        if (token == null || token.isBlank()) {
            throw new MercadoPagoAdapterException("Mercado Pago account is not linked");
        }
        return token;
    }

    public String linkedUserIdOrThrow() {
        String userId = linkedUserId();
        if (userId == null || userId.isBlank()) {
            throw new MercadoPagoAdapterException("Mercado Pago account is not linked");
        }
        return userId;
    }
}
