package dev.kalles.payment.adapter.out.mercadopago.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "mercadopago_company")
public class MercadoPagoStoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false, unique = true)
    private UUID companyId;

    @Column(name = "external_id", unique = true, nullable = false, length = 60)
    private String externalReference;

    @Column(name = "mp_store_id")
    private Long providerStoreId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Long getProviderStoreId() {
        return providerStoreId;
    }

    public void setProviderStoreId(Long providerStoreId) {
        this.providerStoreId = providerStoreId;
    }
}
