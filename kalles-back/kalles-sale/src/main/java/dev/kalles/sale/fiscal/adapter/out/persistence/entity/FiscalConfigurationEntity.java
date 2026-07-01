package dev.kalles.sale.fiscal.adapter.out.persistence.entity;

import dev.kalles.sale.fiscal.domain.FiscalConfiguration;
import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;
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
@Table(name = "fiscal_configurations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fiscal_config_company_model_env", columnNames = {"tenant_id", "company_id", "model", "environment"})
})
@Getter
@Setter
@NoArgsConstructor
public class FiscalConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalDocumentModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalEnvironment environment;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "csc_id", length = 20)
    private String cscId;

    @Column(name = "csc_token", length = 200)
    private String cscToken;

    @Column(nullable = false)
    private Integer series = 1;

    @Column(name = "next_number", nullable = false)
    private Long nextNumber = 1L;

    public FiscalConfiguration toDomain() {
        return new FiscalConfiguration(id, tenantId, companyId, model, environment, stateCode, cscId, cscToken, series, nextNumber);
    }
}
