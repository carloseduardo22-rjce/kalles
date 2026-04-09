package dev.kalles.sale.payment.adapter.out.mercadopago.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tenant")
public class MercadoPagoTenantAccountEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID tenantId;

    @Column(name = "mp_access_token")
    private String accessToken;

    @Column(name = "mp_refresh_token")
    private String refreshToken;

    @Column(name = "mp_user_id", length = 60)
    private String providerAccountId;

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getProviderAccountId() {
        return providerAccountId;
    }

    public void setProviderAccountId(String providerAccountId) {
        this.providerAccountId = providerAccountId;
    }
}
