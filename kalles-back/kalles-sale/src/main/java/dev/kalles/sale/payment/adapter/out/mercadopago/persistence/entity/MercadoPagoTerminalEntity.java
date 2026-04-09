package dev.kalles.sale.payment.adapter.out.mercadopago.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "terminal")
public class MercadoPagoTerminalEntity {

    @Id
    private String id;

    @Column(name = "pos_id")
    private String pointId;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "external_pos_id")
    private String externalPointId;

    @Column(name = "operating_mode")
    private String operationMode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getExternalPointId() {
        return externalPointId;
    }

    public void setExternalPointId(String externalPointId) {
        this.externalPointId = externalPointId;
    }

    public String getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(String operationMode) {
        this.operationMode = operationMode;
    }
}
