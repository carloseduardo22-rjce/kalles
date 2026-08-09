package dev.kalles.fiscal.adapter.out.persistence.entity;

import dev.kalles.fiscal.domain.FiscalCertificate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fiscal_certificates")
@Getter
@Setter
@NoArgsConstructor
public class FiscalCertificateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "protected_content", columnDefinition = "TEXT")
    private String protectedContent;

    @Column(name = "protected_password", columnDefinition = "TEXT")
    private String protectedPassword;

    public FiscalCertificate toDomain() {
        return new FiscalCertificate(id, tenantId, companyId, expiresAt, active, protectedContent, protectedPassword);
    }
}
