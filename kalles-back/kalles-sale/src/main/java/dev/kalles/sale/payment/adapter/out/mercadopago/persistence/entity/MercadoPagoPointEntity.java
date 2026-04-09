package dev.kalles.sale.payment.adapter.out.mercadopago.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "mercadopago_caixa")
public class MercadoPagoPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", unique = true, nullable = false, length = 60)
    private String externalReference;

    @Column(name = "cash_register_id", nullable = false)
    private UUID cashRegisterId;

    @Column(name = "mp_pos_id")
    private Long providerPointId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public UUID getCashRegisterId() {
        return cashRegisterId;
    }

    public void setCashRegisterId(UUID cashRegisterId) {
        this.cashRegisterId = cashRegisterId;
    }

    public Long getProviderPointId() {
        return providerPointId;
    }

    public void setProviderPointId(Long providerPointId) {
        this.providerPointId = providerPointId;
    }
}
