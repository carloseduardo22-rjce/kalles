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
@Table(name = "mercadopago_company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoCompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false, unique = true)
    private UUID companyId;

    @Column(name = "external_id", unique = true, nullable = false, length = 60)
    private String externalId;

    @Column(name = "mp_store_id")
    private Long mpStoreId;
}
