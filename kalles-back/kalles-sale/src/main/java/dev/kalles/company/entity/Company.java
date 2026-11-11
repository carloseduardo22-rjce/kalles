package dev.kalles.company.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    // Endereço (genérico, não MercadoPago)
    private String streetName;
    private String streetNumber;
    private String cityName;
    private String stateName;
    private Double latitude;
    private Double longitude;

    @Column(name = "cash_closing_tolerance", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashClosingTolerance = BigDecimal.ZERO;

    public Company(
            UUID id,
            String name,
            UUID tenantId,
            String streetName,
            String streetNumber,
            String cityName,
            String stateName,
            Double latitude,
            Double longitude
    ) {
        this.id = id;
        this.name = name;
        this.tenantId = tenantId;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.cityName = cityName;
        this.stateName = stateName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Caso a empresa tenha ativado/desativado, pode adicionar aqui depois
}
