package dev.kalles.sale.note.application.port.out;

import dev.kalles.sale.note.domain.SensitiveContent;
import java.util.Optional;
import java.util.UUID;

public interface SensitiveContentRepositoryPort {
    SensitiveContent save(SensitiveContent sensitiveContent);
    Optional<SensitiveContent> findByTokenAndAccountId(String token, UUID accountId);
}
