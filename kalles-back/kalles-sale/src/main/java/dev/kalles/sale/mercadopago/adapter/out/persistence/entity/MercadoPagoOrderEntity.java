package dev.kalles.sale.mercadopago.adapter.out.persistence.entity;

import dev.kalles.sale.mercadopago.domain.PointOrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "mp_point_orders")
public class MercadoPagoOrderEntity {

    @Id
    private String orderId;
    private String paymentId;
    
    @Enumerated(EnumType.STRING)
    private PointOrderStatus status;
    private String externalReference;
    private BigDecimal amount;
    private String idempotencyKey;

    public MercadoPagoOrderEntity() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public PointOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PointOrderStatus status) {
        this.status = status;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
