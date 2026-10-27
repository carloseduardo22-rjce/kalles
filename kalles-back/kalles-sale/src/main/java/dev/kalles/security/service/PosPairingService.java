package dev.kalles.security.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.dto.PairedDeviceResponse;
import dev.kalles.security.dto.PairingCodeResponse;
import dev.kalles.security.entity.PosDeviceSession;
import dev.kalles.security.entity.PosPairingCode;
import dev.kalles.security.exception.PairingCodeInvalidException;
import dev.kalles.security.repository.PosDeviceSessionRepository;
import dev.kalles.security.repository.PosPairingCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PosPairingService {

    static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int CODE_LENGTH = 8;
    static final Duration CODE_LIFETIME = Duration.ofMinutes(10);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PosPairingCodeRepository pairingCodeRepository;
    private final PosDeviceSessionRepository deviceSessionRepository;
    private final CashRegisterRepository cashRegisterRepository;

    @Transactional
    public PairingCodeResponse generateCode(UUID cashRegisterId) {
        CashRegister cashRegister = requireCashRegister(cashRegisterId);
        String code = randomCode();
        LocalDateTime expiresAt = LocalDateTime.now().plus(CODE_LIFETIME);
        pairingCodeRepository.save(new PosPairingCode(
                cashRegister.getCompanyId(), cashRegister.getId(), OpaqueTokens.sha256Hex(code), expiresAt));
        return new PairingCodeResponse(code, expiresAt);
    }

    @Transactional
    public PairedDevice pair(String rawCode) {
        LocalDateTime now = LocalDateTime.now();
        PosPairingCode pairingCode = pairingCodeRepository.findByCodeHash(OpaqueTokens.sha256Hex(normalize(rawCode)))
                .filter(code -> code.isUsableAt(now))
                .orElseThrow(PairingCodeInvalidException::new);
        pairingCode.markUsedAt(now);

        revokeActivePairing(pairingCode.getCashRegisterId(), now);
        String deviceToken = OpaqueTokens.generate();
        deviceSessionRepository.saveAndFlush(new PosDeviceSession(
                pairingCode.getCompanyId(), pairingCode.getCashRegisterId(), OpaqueTokens.sha256Hex(deviceToken)));

        CashRegister cashRegister = cashRegisterRepository.findById(pairingCode.getCashRegisterId()).orElseThrow();
        return new PairedDevice(cashRegister.getId(), cashRegister.getCode(), deviceToken);
    }

    @Transactional
    public void revoke(UUID cashRegisterId) {
        revokeActivePairing(requireCashRegister(cashRegisterId).getId(), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Optional<UUID> findPairedCashRegister(String deviceToken) {
        if (deviceToken == null || deviceToken.isBlank()) {
            return Optional.empty();
        }
        return deviceSessionRepository.findByTokenHashAndActiveTrue(OpaqueTokens.sha256Hex(deviceToken))
                .map(PosDeviceSession::getPosId);
    }

    @Transactional(readOnly = true)
    public Optional<PairedDeviceResponse> describe(UUID cashRegisterId) {
        return cashRegisterRepository.findById(cashRegisterId)
                .map(cashRegister -> new PairedDeviceResponse(cashRegister.getId(), cashRegister.getCode()));
    }

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> pairedAt(UUID cashRegisterId) {
        return deviceSessionRepository.findByPosIdAndActiveTrue(cashRegisterId)
                .map(PosDeviceSession::getCreatedAt);
    }

    private void revokeActivePairing(UUID cashRegisterId, LocalDateTime now) {
        deviceSessionRepository.findByPosIdAndActiveTrue(cashRegisterId).ifPresent(session -> {
            session.revoke(now);
            deviceSessionRepository.saveAndFlush(session);
        });
    }

    private CashRegister requireCashRegister(UUID cashRegisterId) {
        return cashRegisterRepository.findByIdAndCompanyId(cashRegisterId, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new CashRegisterNotFoundException(cashRegisterId.toString()));
    }

    private static String normalize(String rawCode) {
        return rawCode == null ? "" : rawCode.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
    }

    private static String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }
}
