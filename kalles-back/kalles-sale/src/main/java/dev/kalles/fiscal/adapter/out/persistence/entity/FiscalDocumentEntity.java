package dev.kalles.fiscal.adapter.out.persistence.entity;

import dev.kalles.fiscal.domain.FiscalDocument;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fiscal_documents", indexes = {
        @Index(name = "idx_fiscal_documents_tenant_company_sale", columnList = "tenant_id,company_id,sale_id")
})
@Getter
@Setter
@NoArgsConstructor
public class FiscalDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "sale_id", nullable = false)
    private UUID saleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalDocumentModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalDocumentStatus status;

    @Column(name = "access_key", length = 60)
    private String accessKey;

    @Column(name = "authorization_protocol", length = 80)
    private String authorizationProtocol;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "authorized_xml", columnDefinition = "TEXT")
    private String authorizedXml;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    public static FiscalDocumentEntity fromDomain(FiscalDocument document) {
        FiscalDocumentEntity entity = new FiscalDocumentEntity();
        entity.setId(document.id());
        entity.setTenantId(document.tenantId());
        entity.setCompanyId(document.companyId());
        entity.setSaleId(document.saleId());
        entity.setModel(document.model());
        entity.setEnvironment(document.environment());
        entity.setStatus(document.status());
        entity.setAccessKey(document.accessKey());
        entity.setAuthorizationProtocol(document.authorizationProtocol());
        entity.setRejectionReason(document.rejectionReason());
        entity.setAuthorizedXml(document.authorizedXml());
        entity.setIssuedAt(document.issuedAt());
        return entity;
    }

    public FiscalDocument toDomain() {
        return new FiscalDocument(id, tenantId, companyId, saleId, model, environment, status,
                accessKey, authorizationProtocol, rejectionReason, authorizedXml, issuedAt);
    }
}
