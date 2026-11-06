package dev.kalles.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_device_session")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class PosDeviceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "pos_id", nullable = false)
    private UUID posId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public PosDeviceSession(UUID companyId, UUID posId, String tokenHash) {
        this.companyId = companyId;
        this.posId = posId;
        this.tokenHash = tokenHash;
    }

    public void revoke(LocalDateTime now) {
        this.active = false;
        this.revokedAt = now;
    }
}
