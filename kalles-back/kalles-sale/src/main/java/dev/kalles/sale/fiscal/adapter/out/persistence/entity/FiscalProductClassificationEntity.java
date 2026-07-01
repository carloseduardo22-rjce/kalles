package dev.kalles.sale.fiscal.adapter.out.persistence.entity;

import dev.kalles.sale.fiscal.domain.FiscalProductClassification;
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
@Table(name = "fiscal_product_classifications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fiscal_product_classification_scope", columnNames = {"tenant_id", "company_id", "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class FiscalProductClassificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 8)
    private String ncm;

    @Column(length = 10)
    private String cest;

    @Column(length = 4)
    private String cfop;

    @Column(name = "cfop_sale", length = 4)
    private String cfopSale;

    @Column(length = 1)
    private String origin;

    @Column(length = 4)
    private String csosn;

    @Column(length = 3)
    private String cst;

    @Column(length = 6)
    private String unit;

    @Column(length = 14)
    private String gtin;

    public FiscalProductClassification toDomain() {
        return new FiscalProductClassification(id, tenantId, companyId, productId, ncm, cest, cfop,
                cfopSale, origin, csosn, cst, unit, gtin);
    }
}
