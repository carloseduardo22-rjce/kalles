package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.security.context.RequestContext;
import dev.kalles.shared.exception.ForbiddenOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PairedDeviceSessionGuard - Pareamento do dispositivo com o caixa")
class PairedDeviceSessionGuardTest {

    private final PairedDeviceSessionGuard guard = new PairedDeviceSessionGuard();

    @Test
    @DisplayName("Deve permitir operacao no caixa pareado ao dispositivo")
    void shouldAllowPairedCashRegister() {
        CashRegister cashRegister = cashRegisterWithId(UUID.randomUUID());

        RequestContext.runWithin(RequestContext.empty().withPos(cashRegister.getId()),
                () -> assertDoesNotThrow(() -> guard.ensureCanOperate(cashRegister)));
    }

    @Test
    @DisplayName("Deve rejeitar dispositivo nao pareado")
    void shouldRejectUnpairedDevice() {
        CashRegister cashRegister = cashRegisterWithId(UUID.randomUUID());

        RequestContext.runWithin(RequestContext.empty(),
                () -> assertThrows(ForbiddenOperationException.class, () -> guard.ensureCanOperate(cashRegister)));
    }

    @Test
    @DisplayName("Deve rejeitar dispositivo pareado com outro caixa")
    void shouldRejectDevicePairedWithAnotherCashRegister() {
        CashRegister cashRegister = cashRegisterWithId(UUID.randomUUID());

        RequestContext.runWithin(RequestContext.empty().withPos(UUID.randomUUID()),
                () -> assertThrows(ForbiddenOperationException.class, () -> guard.ensureCanOperate(cashRegister)));
    }

    private CashRegister cashRegisterWithId(UUID id) {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", UUID.randomUUID());
        ReflectionTestUtils.setField(cashRegister, "id", id);
        return cashRegister;
    }
}
