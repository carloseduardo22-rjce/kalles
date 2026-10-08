package dev.kalles.payment.exception;

import dev.kalles.shared.exception.ConflictException;

public class TerminalSerialAlreadyMappedException extends ConflictException {

    public TerminalSerialAlreadyMappedException() {
        super("TERMINAL_SERIAL_ALREADY_MAPPED", "Este numero de serie ja esta vinculado a outro caixa desta filial.");
    }
}
