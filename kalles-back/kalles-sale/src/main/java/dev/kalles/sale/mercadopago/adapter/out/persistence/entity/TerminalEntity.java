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
@Table(name = "terminal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TerminalEntity {

    @Id
    private String id;

    @Column(name = "pos_id")
    private String posId;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "external_pos_id")
    private String externalPosId;

    @Column(name = "operating_mode")
    private String operationMode;
}
