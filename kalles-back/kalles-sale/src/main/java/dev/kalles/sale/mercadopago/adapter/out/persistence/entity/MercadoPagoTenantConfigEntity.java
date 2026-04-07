package dev.kalles.sale.mercadopago.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoTenantConfigEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID tenantId;

    @Column(name = "mp_access_token")
    private String mpAccessToken;

    @Column(name = "mp_refresh_token")
    private String mpRefreshToken;

    @Column(name = "mp_user_id", length = 60)
    private String mpUserId;
}
