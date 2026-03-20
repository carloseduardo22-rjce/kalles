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

    @Column(unique = true, nullable = false, length = 60)
    private String externalId;

    private String name;
    private String streetName;
    private String streetNumber;
    private String cityName;
    private String stateName;
    private double latitude;
    private double longitude;

    private Long mpStoreId;
}
