package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CreateCashRegisterRequest;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.exception.CashRegisterCodeAlreadyExistsException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.security.context.RequestContext;
import dev.kalles.security.exception.CompanyContextRequiredException;
import dev.kalles.testsupport.RequestContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegisterCommandService - Cadastro de caixas")
class CashRegisterCommandServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("7ec7d531-95a4-4f19-b989-459e2c1ea701");

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.company(COMPANY_ID);

    @Mock
    private CashRegisterRepository repository;

    @InjectMocks
    private CashRegisterCommandService service;

    @Test
    @DisplayName("Deve rejeitar codigo de caixa duplicado na mesma filial")
    void shouldRejectDuplicateCodeWithinSameCompany() {
        CreateCashRegisterRequest request = new CreateCashRegisterRequest("PDV-01", "Caixa Principal", null);
        when(repository.findByCodeAndCompanyId("PDV-01", COMPANY_ID))
                .thenReturn(Optional.of(new CashRegister("PDV-01", "Caixa existente", COMPANY_ID)));

        CashRegisterCodeAlreadyExistsException exception =
                assertThrows(CashRegisterCodeAlreadyExistsException.class, () -> service.create(request));

        assertEquals("CASH_REGISTER_CODE_ALREADY_EXISTS", exception.getCode());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir filial no contexto ou na requisicao")
    void shouldRequireCompanyWhenNeitherContextNorRequestHasOne() {
        CreateCashRegisterRequest request = new CreateCashRegisterRequest("PDV-01", "Caixa Principal", null);

        RequestContext.runWithin(RequestContext.empty(),
                () -> assertThrows(CompanyContextRequiredException.class, () -> service.create(request)));
        verify(repository, never()).save(any());
    }
}
