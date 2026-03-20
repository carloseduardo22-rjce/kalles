package dev.kalles.sale.mercadopago.adapter.out.persistence.entity;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 60)
    private String externalId;

    private String name;
    private String companyId;

    private Long mpPosId;
}
