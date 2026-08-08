package dev.kalles.note.domain;

import dev.kalles.security.entity.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sensitive_contents")
@Getter
@Setter
public class SensitiveContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The random token sent to the frontend that replaces the raw sensitive text 
    @Column(nullable = false, unique = true, length = 120)
    private String token;

    // The symmetrically encrypted content. Stored as byte[] or Base64 String
    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedText;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // In order to secure it completely per-tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
