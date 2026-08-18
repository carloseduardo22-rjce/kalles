package dev.kalles.security.service;

import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.entity.PosDeviceSession;
import dev.kalles.security.repository.PosDeviceSessionRepository;
import dev.kalles.shared.exception.ForbiddenOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeneratePosPairingTokenUseCase {

    private final PosDeviceSessionRepository repository;
    private final CompanyRepository companyRepository;

    @Transactional
    public String execute(UUID companyId, UUID posId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        if (!companyRepository.existsByIdAndTenantId(companyId, tenantId)) {
            throw new ForbiddenOperationException("Filial nao acessivel para o tenant autenticado.");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        
        PosDeviceSession session = new PosDeviceSession();
        session.setToken(token);
        session.setCompanyId(companyId);
        session.setPosId(posId);
        session.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days expiration for pairing
        session.setActive(true);
        session.setCreatedAt(LocalDateTime.now());
        
        repository.save(session);
        return token;
    }
}
