package dev.kalles.sale.mercadopago.domain;

import java.math.BigDecimal;

public class PointOrder {
    private String orderId;
    private String paymentId;
    private PointOrderStatus status;
    private String externalReference;
    private BigDecimal amount;
    private String idempotencyKey;

    public PointOrder() {}

    public PointOrder(String orderId, String paymentId, PointOrderStatus status, String externalReference, BigDecimal amount, String idempotencyKey) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.status = status;
        this.externalReference = externalReference;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
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
