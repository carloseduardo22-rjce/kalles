package dev.kalles.fiscal.adapter.out.persistence.entity;

import dev.kalles.fiscal.domain.FiscalIssuerProfile;
import dev.kalles.fiscal.domain.FiscalTaxRegime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "fiscal_issuer_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fiscal_issuer_profile_company", columnNames = {"tenant_id", "company_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class FiscalIssuerProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 14)
    private String cnpj;

    @Column(name = "legal_name", nullable = false, length = 160)
    private String legalName;

    @Column(name = "trade_name", length = 160)
    private String tradeName;

    @Column(name = "state_registration", nullable = false, length = 20)
    private String stateRegistration;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_regime", nullable = false, length = 30)
    private FiscalTaxRegime taxRegime;

    @Column(length = 10)
    private String cnae;

    public FiscalIssuerProfile toDomain() {
        return new FiscalIssuerProfile(id, tenantId, companyId, cnpj, legalName, tradeName,
                stateRegistration, taxRegime, cnae);
    }
}
