package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.security.context.PosContextHolder;
import dev.kalles.shared.exception.ForbiddenOperationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PairedDeviceSessionGuard {

    public void ensureCanOperate(CashRegister cashRegister) {
        UUID pairedPosId = PosContextHolder.getPosId();

        if (pairedPosId == null) {
            throw new ForbiddenOperationException("O dispositivo precisa estar pareado antes da operação.");
        }

        if (!pairedPosId.equals(cashRegister.getId())) {
            throw new ForbiddenOperationException("O dispositivo pareado não corresponde ao caixa informado.");
        }
    }
}
