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
@Table(name = "pos_pairing_code")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class PosPairingCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "cash_register_id", nullable = false)
    private UUID cashRegisterId;

    @Column(name = "code_hash", nullable = false, unique = true, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PosPairingCode(UUID companyId, UUID cashRegisterId, String codeHash, LocalDateTime expiresAt) {
        this.companyId = companyId;
        this.cashRegisterId = cashRegisterId;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    public boolean isUsableAt(LocalDateTime now) {
        return usedAt == null && now.isBefore(expiresAt);
    }

    public void markUsedAt(LocalDateTime now) {
        this.usedAt = now;
    }
}
