package dev.kalles.sale.note.adapter.out.persistence;

import dev.kalles.sale.note.adapter.out.persistence.repository.SensitiveContentJpaRepository;
import dev.kalles.sale.note.application.port.out.SensitiveContentRepositoryPort;
import dev.kalles.sale.note.domain.SensitiveContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SensitiveContentPersistenceAdapter implements SensitiveContentRepositoryPort {

    private final SensitiveContentJpaRepository repository;

    @Override
    public SensitiveContent save(SensitiveContent sensitiveContent) {
        return repository.save(sensitiveContent);
    }

    @Override
    public Optional<SensitiveContent> findByTokenAndAccountId(String token, UUID accountId) {
        return repository.findByTokenAndAccount_Id(token, accountId);
    }
}
