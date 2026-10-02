package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.application.port.in.GetPaymentProviderAccountStatusUseCase;
import dev.kalles.payment.application.port.in.LinkPaymentProviderAccountUseCase;
import dev.kalles.payment.application.service.PaymentProviderOAuthStateService;
import dev.kalles.payment.config.MercadoPagoProperties;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.security.exception.AuthenticatedAccountNotFoundException;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.testsupport.RequestContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentProviderAccountControllerTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.tenant(TENANT_ID);

    private final PaymentProviderOAuthStateService oAuthStateService = mock(PaymentProviderOAuthStateService.class);
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final PaymentProviderAccountController controller = new PaymentProviderAccountController(
            mock(LinkPaymentProviderAccountUseCase.class),
            mock(GetPaymentProviderAccountStatusUseCase.class),
            oAuthStateService,
            accountRepository,
            new MercadoPagoProperties(null, null, null, null, null, null, null)
    );

    @Test
    void shouldAnswerUnauthorizedWhenTheAuthenticatedAccountNoLongerExists() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("removida@kalles.dev");
        when(accountRepository.findByTenantIdAndEmailIgnoreCase(TENANT_ID, "removida@kalles.dev"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getAuthorizationUrl(PaymentProvider.MERCADO_PAGO, authentication))
                .isInstanceOf(AuthenticatedAccountNotFoundException.class);

        verify(oAuthStateService, never()).generateState(any(), any());
    }

    @Test
    void shouldAnswerUnauthorizedWithoutAnAuthentication() {
        assertThatThrownBy(() -> controller.getAuthorizationUrl(PaymentProvider.MERCADO_PAGO, null))
                .isInstanceOf(AuthenticatedAccountNotFoundException.class);
    }
}
