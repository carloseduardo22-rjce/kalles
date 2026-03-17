package dev.kalles.sale.mercadopago.adapter.out.persistence.entity;

import org.hibernate.validator.constraints.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mercadopago_caixa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoCaixaEntity {

    @Id
    @UUID
    private String id;

    private String name;
    private String companyId;

    private Long mpPosId;
}
