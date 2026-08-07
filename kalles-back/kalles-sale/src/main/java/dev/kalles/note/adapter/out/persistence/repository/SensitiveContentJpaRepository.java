package dev.kalles.note.adapter.out.persistence.repository;

import dev.kalles.note.domain.SensitiveContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensitiveContentJpaRepository extends JpaRepository<SensitiveContent, UUID> {
    Optional<SensitiveContent> findByTokenAndAccount_Id(String token, UUID accountId);
}
