package dev.kalles.security.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.security.context.RequestContext;
import dev.kalles.security.dto.PairingCodeResponse;
import dev.kalles.security.entity.PosDeviceSession;
import dev.kalles.security.entity.PosPairingCode;
import dev.kalles.security.exception.PairingCodeInvalidException;
import dev.kalles.security.repository.PosDeviceSessionRepository;
import dev.kalles.security.repository.PosPairingCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PosPairingService")
class PosPairingServiceTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Mock
    private PosPairingCodeRepository pairingCodeRepository;

    @Mock
    private PosDeviceSessionRepository deviceSessionRepository;

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @InjectMocks
    private PosPairingService pairingService;

    private CashRegister cashRegister;

    @BeforeEach
    void setUp() {
        cashRegister = new CashRegister("CAIXA-01", "Caixa principal", COMPANY_ID);
        ReflectionTestUtils.setField(cashRegister, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("gera codigo de 8 caracteres do alfabeto, valido por 10 minutos, guardando so o hash")
    void shouldGenerateShortLivedCodeAndStoreOnlyItsHash() {
        when(cashRegisterRepository.findByIdAndCompanyId(cashRegister.getId(), COMPANY_ID))
                .thenReturn(Optional.of(cashRegister));

        PairingCodeResponse response = withinCompany(() -> pairingService.generateCode(cashRegister.getId()));

        assertThat(response.code()).hasSize(8);
        assertThat(response.code().chars()).allMatch(c -> PosPairingService.CODE_ALPHABET.indexOf(c) >= 0);
        assertThat(Duration.between(LocalDateTime.now(), response.expiresAt()).toSeconds())
                .isCloseTo(600L, within(5L));

        ArgumentCaptor<PosPairingCode> saved = ArgumentCaptor.forClass(PosPairingCode.class);
        verify(pairingCodeRepository).save(saved.capture());
        assertThat(saved.getValue().getCodeHash()).isEqualTo(OpaqueTokens.sha256Hex(response.code()));
        assertThat(saved.getValue().getCashRegisterId()).isEqualTo(cashRegister.getId());
    }

    @Test
    @DisplayName("nao gera codigo para caixa de outra loja")
    void shouldNotGenerateCodeForCashRegisterOfAnotherCompany() {
        when(cashRegisterRepository.findByIdAndCompanyId(cashRegister.getId(), COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> withinCompany(() -> pairingService.generateCode(cashRegister.getId())))
                .isInstanceOf(CashRegisterNotFoundException.class);
        verify(pairingCodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("recusa codigo vencido")
    void shouldRejectExpiredCode() {
        PosPairingCode expired = pairingCode(LocalDateTime.now().minusMinutes(1));
        when(pairingCodeRepository.findByCodeHash(OpaqueTokens.sha256Hex("ABCD2345"))).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> pairingService.pair("ABCD2345")).isInstanceOf(PairingCodeInvalidException.class);
        verify(deviceSessionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("recusa codigo ja usado")
    void shouldRejectUsedCode() {
        PosPairingCode used = pairingCode(LocalDateTime.now().plusMinutes(5));
        used.markUsedAt(LocalDateTime.now().minusMinutes(1));
        when(pairingCodeRepository.findByCodeHash(OpaqueTokens.sha256Hex("ABCD2345"))).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> pairingService.pair("ABCD2345")).isInstanceOf(PairingCodeInvalidException.class);
    }

    @Test
    @DisplayName("parear de novo revoga o dispositivo anterior e consome o codigo")
    void shouldRevokePreviousDeviceAndConsumeCode() {
        PosPairingCode code = pairingCode(LocalDateTime.now().plusMinutes(5));
        PosDeviceSession previous = new PosDeviceSession(COMPANY_ID, cashRegister.getId(), "hash-anterior");
        when(pairingCodeRepository.findByCodeHash(OpaqueTokens.sha256Hex("ABCD2345"))).thenReturn(Optional.of(code));
        when(deviceSessionRepository.findByPosIdAndActiveTrue(cashRegister.getId())).thenReturn(Optional.of(previous));
        when(cashRegisterRepository.findById(cashRegister.getId())).thenReturn(Optional.of(cashRegister));

        PairedDevice device = pairingService.pair(" abcd 2345 ");

        assertThat(previous.isActive()).isFalse();
        assertThat(previous.getRevokedAt()).isNotNull();
        assertThat(code.getUsedAt()).isNotNull();
        assertThat(device.cashRegisterCode()).isEqualTo("CAIXA-01");

        ArgumentCaptor<PosDeviceSession> saved = ArgumentCaptor.forClass(PosDeviceSession.class);
        verify(deviceSessionRepository, times(2)).saveAndFlush(saved.capture());
        assertThat(saved.getAllValues().getLast().getTokenHash()).isEqualTo(OpaqueTokens.sha256Hex(device.deviceToken()));
    }

    @Test
    @DisplayName("revogar sem pareamento ativo nao falha")
    void shouldRevokeWithoutActivePairing() {
        when(cashRegisterRepository.findByIdAndCompanyId(cashRegister.getId(), COMPANY_ID))
                .thenReturn(Optional.of(cashRegister));
        when(deviceSessionRepository.findByPosIdAndActiveTrue(cashRegister.getId())).thenReturn(Optional.empty());

        withinCompany(() -> {
            pairingService.revoke(cashRegister.getId());
            return null;
        });

        verify(deviceSessionRepository, never()).saveAndFlush(any());
    }

    private PosPairingCode pairingCode(LocalDateTime expiresAt) {
        return new PosPairingCode(COMPANY_ID, cashRegister.getId(), OpaqueTokens.sha256Hex("ABCD2345"), expiresAt);
    }

    private <T> T withinCompany(ScopedValue.CallableOp<T, RuntimeException> operation) {
        return RequestContext.callWithin(RequestContext.empty().withCompany(COMPANY_ID), operation);
    }
}
