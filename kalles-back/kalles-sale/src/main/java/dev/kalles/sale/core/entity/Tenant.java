package dev.kalles.sale.core.entity;

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
@Table(name = "tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    // TODO: Adicionar campos genéricos de empresa global/multi-tenant se precisar
}
