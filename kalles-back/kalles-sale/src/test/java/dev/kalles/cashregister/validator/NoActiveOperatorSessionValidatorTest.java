package dev.kalles.cashregister.validator;

import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.OperatorAlreadyInSessionException;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.valueobject.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoActiveOperatorSessionValidator - Validador de operador com sessao ativa")
class NoActiveOperatorSessionValidatorTest {

    private final OpenSessionRequest request = new OpenSessionRequest("PDV-01", new BigDecimal("100.00"), false);
    private final Operator operator = new Operator("Operador", "OP001");

    @Mock
    private CashRegisterSessionRepository sessionRepository;

    @InjectMocks
    private NoActiveOperatorSessionValidator validator;

    @Test
    @DisplayName("Deve passar quando o operador nao possui sessao ativa")
    void shouldPassWhenOperatorHasNoActiveSession() {
        when(sessionRepository.existsByOperatorAndStatus(operator, SessionStatus.OPEN)).thenReturn(false);

        assertThatCode(() -> validator.validate(request, operator)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve bloquear quando o operador ja possui sessao ativa")
    void shouldThrowWhenOperatorAlreadyHasActiveSession() {
        when(sessionRepository.existsByOperatorAndStatus(operator, SessionStatus.OPEN)).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(request, operator))
                .isInstanceOf(OperatorAlreadyInSessionException.class)
                .hasMessageContaining("OP001");
    }
}
