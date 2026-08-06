package dev.kalles.sale.client.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    indexes = {
        @Index(name = "idx_client_cpf", columnList = "cpf"),
        @Index(name = "idx_client_company_id", columnList = "company_id")
    },
    uniqueConstraints = @UniqueConstraint(name = "uk_client_cpf_company", columnNames = {"cpf", "company_id"}),
    comment = "Clientes cadastrados na loja"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 1)
    private Character gender;

    @Column(length = 14)
    private String cpf;

    @Column(name = "code_country", length = 5)
    private String codeCountry;

    @Column(length = 20)
    private String cellphone;

    @Column(length = 20)
    private String rg;

    @Column(name = "name_father", length = 100)
    private String nameFather;

    @Column(name = "name_mother", length = 100)
    private String nameMother;

    @Column(columnDefinition = "TEXT")
    private String observations;
}

