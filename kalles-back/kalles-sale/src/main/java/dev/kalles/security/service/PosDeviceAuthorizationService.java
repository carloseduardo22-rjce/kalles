package dev.kalles.security.service;

import dev.kalles.security.domain.Account;
import dev.kalles.security.repository.PosDeviceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PosDeviceAuthorizationService {

    private final PosBindingAccessPolicy posBindingAccessPolicy;
    private final PosDeviceSessionRepository posDeviceSessionRepository;

    public UUID resolveAuthorizedPosId(Account account, String posToken) {
        if (!posBindingAccessPolicy.requiresPairedDevice(account)) {
            return null;
        }

        if (posToken == null || posToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Terminal não configurado. Por favor, solicite o pareamento do caixa.");
        }

        var session = posDeviceSessionRepository
                .findByTokenAndActiveTrueAndExpiresAtGreaterThan(posToken, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Sessão do terminal inválida ou expirada."));

        if (!session.getCompanyId().equals(account.getCompanyId())) {
            throw new IllegalArgumentException("Este terminal não pertence a filial do caixa.");
        }

        return session.getPosId();
    }
}
