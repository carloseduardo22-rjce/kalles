package dev.kalles.sale.core.service;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.core.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashRegisterCheckoutSessionServiceTest {

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    private CashRegisterCheckoutSessionService service;

    @BeforeEach
    void setUp() {
        service = new CashRegisterCheckoutSessionService(sessionRepository);
    }

    private CashRegisterSession buildOpenSession() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal");
        Operator operator = new Operator("João Silva", "OP001");
        return CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
    }

    // --- findByToken ---

    @Test
    void shouldReturnSessionWhenTokenIsValidAndSessionIsOpen() {
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        Optional<Session> result = service.findByToken(id.toString());

        assertTrue(result.isPresent());
        assertTrue(result.get().isOpen());
    }

    @Test
    void shouldReturnEmptyWhenTokenIsNull() {
        Optional<Session> result = service.findByToken(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(sessionRepository);
    }

    @Test
    void shouldReturnEmptyWhenTokenIsNotValidUuid() {
        Optional<Session> result = service.findByToken("isto-nao-e-um-uuid");

        assertTrue(result.isEmpty());
        verifyNoInteractions(sessionRepository);
    }

    @Test
    void shouldReturnEmptyWhenSessionNotFoundInRepository() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Session> result = service.findByToken(id.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenSessionFoundButIsClosed() {
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        session.close();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        Optional<Session> result = service.findByToken(id.toString());

        assertTrue(result.isEmpty());
    }

    // --- isSessionOpen ---

    @Test
    void shouldReturnTrueWhenSessionIsOpen() {
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        assertTrue(service.isSessionOpen(id.toString()));
    }

    @Test
    void shouldReturnFalseWhenSessionNotFound() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        assertFalse(service.isSessionOpen(id.toString()));
    }

    // --- getOpenSessionOrThrow ---

    @Test
    void shouldThrowNotFoundWhenSessionTokenNotFound() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getOpenSessionOrThrow(id.toString()));
    }

    @Test
    void shouldThrowNotFoundWhenSessionIsClosedOnGetOrThrow() {
        // findByToken filters out closed sessions (isOpen = false), so getOpenSessionOrThrow
        // receives an empty Optional and throws NotFoundException — not IllegalStateException
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        session.close();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        assertThrows(NotFoundException.class,
                () -> service.getOpenSessionOrThrow(id.toString()));
    }

    @Test
    void shouldReturnSessionWhenOpenOnGetOrThrow() {
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        Session result = assertDoesNotThrow(() -> service.getOpenSessionOrThrow(id.toString()));

        assertNotNull(result);
        assertTrue(result.isOpen());
    }
}
