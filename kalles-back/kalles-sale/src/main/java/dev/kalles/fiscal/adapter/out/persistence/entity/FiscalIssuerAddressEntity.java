package dev.kalles.fiscal.adapter.out.persistence.entity;

import dev.kalles.fiscal.domain.FiscalIssuerAddress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "fiscal_issuer_addresses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fiscal_issuer_address_company", columnNames = {"tenant_id", "company_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class FiscalIssuerAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "state_ibge_code", nullable = false)
    private Integer stateIbgeCode;

    @Column(name = "city_name", nullable = false, length = 80)
    private String cityName;

    @Column(name = "city_ibge_code", nullable = false)
    private Integer cityIbgeCode;

    @Column(nullable = false, length = 80)
    private String district;

    @Column(nullable = false, length = 120)
    private String street;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(length = 80)
    private String complement;

    @Column(name = "country_name", nullable = false, length = 60)
    private String countryName;

    @Column(name = "country_code", nullable = false)
    private Integer countryCode;

    public FiscalIssuerAddress toDomain() {
        return new FiscalIssuerAddress(id, tenantId, companyId, zipCode, stateCode, stateIbgeCode,
                cityName, cityIbgeCode, district, street, number, complement, countryName, countryCode);
    }
}
