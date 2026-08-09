package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.shared.service.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("CashRegisterCheckoutSessionService - Serviço de Sessão do Caixa")
class CashRegisterCheckoutSessionServiceTest {

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    private CashRegisterCheckoutSessionService service;

    @BeforeEach
    void setUp() {
        service = new CashRegisterCheckoutSessionService(sessionRepository);
    }

    private CashRegisterSession buildOpenSession() {
        CashRegister cashRegister = new CashRegister("PDV-01", "Caixa Principal", java.util.UUID.randomUUID());
        Operator operator = new Operator("João Silva", "OP001");
        return CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
    }

    // --- findByToken ---

    @Test
    @DisplayName("Deve retornar sessão quando token é válido e sessão está aberta")
    void shouldReturnSessionWhenTokenIsValidAndSessionIsOpen() {
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        Optional<Session> result = service.findByToken(id.toString());

        assertTrue(result.isPresent());
        assertTrue(result.get().isOpen());
    }

    @Test
    @DisplayName("Deve retornar vazio quando token é nulo")
    void shouldReturnEmptyWhenTokenIsNull() {
        Optional<Session> result = service.findByToken(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve retornar vazio quando token não é um UUID válido")
    void shouldReturnEmptyWhenTokenIsNotValidUuid() {
        Optional<Session> result = service.findByToken("isto-nao-e-um-uuid");

        assertTrue(result.isEmpty());
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve retornar vazio quando sessão não encontrada no repositório")
    void shouldReturnEmptyWhenSessionNotFoundInRepository() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Session> result = service.findByToken(id.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar vazio quando sessão está fechada")
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
    @DisplayName("Deve retornar verdadeiro quando sessão está aberta")
    void shouldReturnTrueWhenSessionIsOpen() {
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        assertTrue(service.isSessionOpen(id.toString()));
    }

    @Test
    @DisplayName("Deve retornar falso quando sessão não encontrada")
    void shouldReturnFalseWhenSessionNotFound() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        assertFalse(service.isSessionOpen(id.toString()));
    }

    // --- getOpenSessionOrThrow ---

    @Test
    @DisplayName("Deve lançar exceção quando token de sessão não encontrado")
    void shouldThrowNotFoundWhenSessionTokenNotFound() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getOpenSessionOrThrow(id.toString()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão está fechada ao buscar com validação")
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
    @DisplayName("Deve retornar sessão aberta ao buscar com validação")
    void shouldReturnSessionWhenOpenOnGetOrThrow() {
        UUID id = UUID.randomUUID();
        CashRegisterSession session = buildOpenSession();
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        Session result = assertDoesNotThrow(() -> service.getOpenSessionOrThrow(id.toString()));

        assertNotNull(result);
        assertTrue(result.isOpen());
    }
}
