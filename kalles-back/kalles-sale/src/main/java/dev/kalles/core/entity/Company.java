package dev.kalles.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    // Caso a empresa tenha ativado/desativado, pode adicionar aqui depois
}
