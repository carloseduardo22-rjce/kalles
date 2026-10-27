package dev.kalles.security.exception;

import dev.kalles.shared.exception.ForbiddenOperationException;

public class DeviceNotPairedException extends ForbiddenOperationException {

    public DeviceNotPairedException() {
        super("DEVICE_NOT_PAIRED", "Este navegador não está pareado com o caixa. Peça o código de pareamento ao administrador.");
    }
}
