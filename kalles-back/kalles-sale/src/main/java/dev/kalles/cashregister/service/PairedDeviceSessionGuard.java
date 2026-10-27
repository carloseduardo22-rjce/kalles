package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.security.context.PosContextHolder;
import dev.kalles.security.exception.DeviceNotPairedException;
import org.springframework.stereotype.Component;

@Component
public class PairedDeviceSessionGuard {

    public void ensureCanOperate(CashRegister cashRegister) {
        if (!cashRegister.getId().equals(PosContextHolder.getPosId())) {
            throw new DeviceNotPairedException();
        }
    }
}
