package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.security.context.PosContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PairedDeviceSessionGuard {

    public void ensureCanOperate(CashRegister cashRegister) {
        UUID pairedPosId = PosContextHolder.getPosId();

        if (pairedPosId == null) {
            throw new IllegalArgumentException("O dispositivo precisa estar pareado antes da operação.");
        }

        if (!pairedPosId.equals(cashRegister.getId())) {
            throw new IllegalArgumentException("O dispositivo pareado não corresponde ao caixa informado.");
        }
    }
}
