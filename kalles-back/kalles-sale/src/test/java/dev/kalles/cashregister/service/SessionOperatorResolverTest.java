package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.exception.OperatorAccountNotLinkedException;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.service.AuthenticatedAccountProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionOperatorResolver - Operador da sessao pela conta autenticada")
class SessionOperatorResolverTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private AuthenticatedAccountProvider authenticatedAccountProvider;

    @InjectMocks
    private SessionOperatorResolver resolver;

    @Test
    @DisplayName("Devolve o cadastro ativo da conta sem criar outro")
    void shouldReturnActiveOperatorOfTheAccount() {
        Operator operator = new Operator("Maria", "maria");
        authenticatedAs(AccountRole.OPERATOR);
        when(operatorRepository.findByAccountIdAndCompanyId(ACCOUNT_ID, COMPANY_ID)).thenReturn(Optional.of(operator));

        assertThat(resolver.resolve(COMPANY_ID)).isSameAs(operator);
        verify(operatorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Recusa cadastro inativo, mesmo de administrador")
    void shouldRejectInactiveOperator() {
        Operator operator = new Operator("Admin", "adm-1");
        operator.setActive(false);
        authenticatedAs(AccountRole.ADMIN);
        when(operatorRepository.findByAccountIdAndCompanyId(ACCOUNT_ID, COMPANY_ID)).thenReturn(Optional.of(operator));

        assertThatThrownBy(() -> resolver.resolve(COMPANY_ID)).isInstanceOf(OperatorAccountNotLinkedException.class);
        verify(operatorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Recusa conta de operador sem cadastro")
    void shouldRejectOperatorAccountWithoutOperator() {
        authenticatedAs(AccountRole.OPERATOR);
        when(operatorRepository.findByAccountIdAndCompanyId(ACCOUNT_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolve(COMPANY_ID)).isInstanceOf(OperatorAccountNotLinkedException.class);
        verify(operatorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cria o cadastro do administrador com a proxima matricula livre da filial")
    void shouldEnrollAdministratorWithNextFreeCode() {
        authenticatedAs(AccountRole.ADMIN);
        when(operatorRepository.findByAccountIdAndCompanyId(ACCOUNT_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(operatorRepository.existsByCodeAndCompanyId("adm-1", COMPANY_ID)).thenReturn(true);
        when(operatorRepository.existsByCodeAndCompanyId("adm-2", COMPANY_ID)).thenReturn(false);
        when(operatorRepository.save(any(Operator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operator operator = resolver.resolve(COMPANY_ID);

        assertThat(operator.getCode()).isEqualTo("adm-2");
        assertThat(operator.getName()).isEqualTo("Pessoa da conta");
        assertThat(operator.getCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(operator.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(operator.getPermissionLevel()).isEqualTo(PermissionLevel.ADMIN);
    }

    private void authenticatedAs(AccountRole role) {
        Account account = new Account(UUID.randomUUID(), "Pessoa da conta", "pessoa@loja.com", "hash", role);
        ReflectionTestUtils.setField(account, "id", ACCOUNT_ID);
        when(authenticatedAccountProvider.require()).thenReturn(account);
    }
}
