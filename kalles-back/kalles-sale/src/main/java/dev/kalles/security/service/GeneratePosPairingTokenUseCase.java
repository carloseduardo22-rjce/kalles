package dev.kalles.security.service;

import dev.kalles.security.entity.PosDeviceSession;
import dev.kalles.security.repository.PosDeviceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeneratePosPairingTokenUseCase {

    private final PosDeviceSessionRepository repository;

    @Transactional
    public String execute(UUID companyId, UUID posId) {
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
